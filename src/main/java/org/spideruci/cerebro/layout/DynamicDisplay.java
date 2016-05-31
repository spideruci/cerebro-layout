package org.spideruci.cerebro.layout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.swingViewer.View;
import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerPipe;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.SourceLineNode;

import com.cedarsoftware.util.io.JsonWriter;
import com.cedarsoftware.util.io.Writers.JsonStringWriter;
import com.google.common.collect.Table.Cell;


public class DynamicDisplay {
  private Graph graph;
  public Viewer viewer;
  SortedSet<Integer> buffer;
  DynamicFlowGraph displayedGraph;
  GraphicGraph ggraph;
  LinLog layout;
  
  private static DynamicDisplay display = null;
  private static int uniqId = 0;
  private static final String DEFAULT_TITLE = "";
  private static final int BUFFER_LIMIT = 300;
  
  public static synchronized DynamicDisplay display(DynamicFlowGraph displayedGraph) {
    if(display == null) {
      System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
      display = init(DEFAULT_TITLE, displayedGraph);
    }
    
    return display;
  }
  
  public static DynamicDisplay init(String title, DynamicFlowGraph displayedGraph) {
    DynamicDisplay display = new DynamicDisplay(title, displayedGraph);
    setup(display);
    
    Viewer viewer = launch(display);
    display.viewer = viewer;
    listenForEvents(display);
    return display;
  }
  
    private static synchronized void setup(DynamicDisplay display) {
      Graph graph = display.graph;
      graph.setStrict(false);
      graph.setAutoCreate(true);
      File file = new File("css/stylesheet.css");
      String url = "url(file://" + file.getAbsolutePath() + ")";
      graph.addAttribute("ui.stylesheet", url);
      graph.addAttribute("layout.quality", 4);
//      graph.addAttribute("layout.stabilization-limit", 1);
      display.buffer.clear();
    }
  
    private DynamicDisplay(String title, DynamicFlowGraph displayedGraph) {
      graph = new MultiGraph(title);
      buffer = Collections.synchronizedSortedSet(new TreeSet<Integer>());
      this.displayedGraph = displayedGraph; 
    }
    
    private static Viewer launch(DynamicDisplay display) {
      Graph graph = display.graph;
      Viewer viewer = ((MultiGraph)graph).display();
      display.ggraph = viewer.getGraphicGraph();
      
      return viewer;
    }
    
    private static void listenForEvents(DynamicDisplay display) {
      View view = display.viewer.getDefaultView();
      
      ViewerPipe fromViewer = display.viewer.newViewerPipe();
      ViewerEventListener viewerListener = new ViewerEventListener(fromViewer, display.displayedGraph);
      view.setMouseManager(viewerListener);
      view.addMouseWheelListener(viewerListener);
      fromViewer.addViewerListener(viewerListener);
      fromViewer.addSink(display.graph);
    }
  
  public void clear() {
    this.graph.clear();
  }
    
  public synchronized void showDependence(int from, int to, String label) {
    showDependence(String.valueOf(from), String.valueOf(to), label);
  }
  
    private void showDependence(String from, String to, String label) {
      showNode(from);
      showNode(to);
      int id = uniqId++;
      try {
        Node fromNode = graph.getNode(from);
        if(fromNode == null || !fromNode.hasEdgeToward(to)) {
          Edge edge = graph.addEdge(String.valueOf(id), from, to, true);
          edge.addAttribute("ui.hide");
        }
      } catch (IdAlreadyInUseException e) { }
    }
  
  public synchronized void showDependence(String from, String to, double weight) {
    showNode(from);
    showNode(to);
    int id = uniqId++;
    try {
      Node fromNode = graph.getNode(from);
      if(fromNode == null || !fromNode.hasEdgeToward(to)) {
        Edge edge = graph.addEdge(String.valueOf(id), from, to, true);
        edge.addAttribute("layout.weight", weight);
        edge.addAttribute("ui.hide");
      }
    } catch (IdAlreadyInUseException e) { }
  }
  
  public void showDependence(int f, float fromx, float fromy, 
      int t, float tox, float toy, String label, double wt) {
    String from = String.valueOf(f);
    String to = String.valueOf(t);
    showNodeXY(from, fromx, fromy);
    showNodeXY(to, tox, toy);
    int id = uniqId++;
    try {
      Node fromNode = graph.getNode(from);
      if(fromNode == null || !fromNode.hasEdgeToward(to)) {
        Edge edge = graph.addEdge(String.valueOf(id), from, to, true);
        edge.addAttribute("layout.weight", wt);
        edge.addAttribute("ui.hide");
      }
    } catch (IdAlreadyInUseException e) { }
  }
    
    @SuppressWarnings("unused")
    private synchronized void bufferNodes(int nodeId1, int nodeId2) {
      buffer.add(nodeId1);
      buffer.add(nodeId2);
      
      while(buffer.size() > BUFFER_LIMIT) {
        int oldestNode = buffer.first();
        buffer.remove(oldestNode);
        removeNode(String.valueOf(oldestNode));
      }
      
      for(Node node : graph.getEachNode()) {
        if(node.getEdgeSet().size() == 0) {
          buffer.remove(Integer.parseInt(node.getId()));
          graph.removeNode(node);
        }
      }
    }
    
      private void removeNode(String nodeId) {
        try {
          graph.removeNode(nodeId);
        } catch(ElementNotFoundException nodeNotFound) {
          // Do nothing for now.
        }
      }
  
      private void showNode(String nodeId) {
        try {
          graph.addNode(nodeId);
        } catch(IdAlreadyInUseException nodeInUse) { }
      }
      
      private void showNodeXY(String nodeId, double x, double y) {
        try {
          Node node = graph.addNode(nodeId);
          node.addAttribute("x", x);
          node.addAttribute("y", y);
        } catch(IdAlreadyInUseException nodeInUse) { }
      }
    
    public static void play() throws IOException { }
    
    public void spitImage(int id) throws IOException {
      FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.VGA);
      
      pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
      pic.writeAll(this.graph, this.graph.getId() + id +  ".png");
    }
    
    public void spitImage(String id) throws IOException {
      FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.VGA);
      
      pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
      pic.writeAll(this.graph, id +  ".png");
    }

    public void weighEdges() {
      double maxCount = displayedGraph.maxEdgeCount() + 1.0;
      for(Edge edge : graph.getEachEdge()) {
        Node from = edge.getSourceNode();
        Node to = edge.getTargetNode();
        int fromId = Integer.parseInt(from.toString());
        int toId = Integer.parseInt(to.toString());
        int count = displayedGraph.getEdgeCount(fromId, toId);
        double weight = (maxCount - count)/maxCount;
        edge.addAttribute("layout.weight", weight);
      }
    }

    public void colorNodes() {
      double maxColorDegree = displayedGraph.colorCount() * 1.0;
      for(Node node : graph.getEachNode()) {
        int nodeId = Integer.valueOf(node.getId());
        int colorDegree = displayedGraph.colorCode(nodeId);
        double age = colorDegree / maxColorDegree;
        node.setAttribute("ui.color", age);
      }
    }
    
    public void colorNodes(Object[] palete) {
      for(Node node : graph.getEachNode()) {
        int nodeId = Integer.valueOf(node.getId());
        int colorIndex = displayedGraph.colorCode(nodeId);
        String color = palete[colorIndex % palete.length].toString();
        String colorStyle = "fill-color: " + color + ";";
        node.setAttribute("ui.style", colorStyle);
      }
    }
    
    public void decolorNodes() {
      for(Node node : graph.getEachNode()) {
        node.setAttribute("ui.color", 0);
      }
    }
    
    public void showEdges() {
      for(Edge edge : graph.getEachEdge()) {
        edge.removeAttribute("ui.hide");
      }
    }
    
    public void hideEdges() {
      for(Edge edge : graph.getEachEdge()) {
        edge.addAttribute("ui.hide");
      }
    }
    
    public void pinNodesOnDynamicFlowGraph() {
      for(Node node : ggraph.getEachNode()) {
        int nodeId = Integer.valueOf(node.getId());
        SourceLineNode lineNode =  displayedGraph.getNode(nodeId);
//        int colorGroup = displayedGraph.colorCode(nodeId);
//        lineNode.initColorGroup(colorGroup);
        Object[] xyz = node.getAttribute("xyz");
        double x = (double)xyz[0] * 20;
        double y = (double)xyz[1] * 20;
        lineNode.initXY(x, y);
      }
    }
    
    public Layout getDangerousLayout() 
        throws NoSuchFieldException, 
        IllegalArgumentException, IllegalAccessException {
      Object yahoo = accessField(viewer, "optLayout");
      Layout layout = accessField(yahoo, "layout");
      return layout;
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T accessField(Object myObj, String fieldName) 
        throws NoSuchFieldException, 
        IllegalArgumentException, IllegalAccessException {
      Class<?> myClass = myObj.getClass();
      Field myField = getField(myClass, fieldName);
      myField.setAccessible(true);
      return (T) myField.get(myObj);
    }
    
      private static Field getField(Class<?> clazz, String fieldName)
          throws NoSuchFieldException {
        try {
          return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
          Class<?> superClass = clazz.getSuperclass();
          if (superClass == null) {
            throw e;
          } else {
            return getField(superClass, fieldName);
          }
        }
      }

      
}
