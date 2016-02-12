package org.spideruci.cerebro;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.IdAlreadyInUseException;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.ui.layout.springbox.BarnesHutLayout;

import org.spideruci.cerebro.community.AbstractCommunityComputer;
import org.spideruci.cerebro.layout.LayoutFactory;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.SourceLineNode;

import com.google.common.collect.Table.Cell;

/**
 * Spine is the pipleline that performs various computations such as layout, 
 * clustering and community detection in the execution flow graph that it houses.
 * <br>
 * It is worth noting that this pipeline does not *implement*, nor does it
 * *configure* any of the computations that it performs. 
 * Instead it is merely an container for executing these computations in an 
 * order that is specified by the user of the instances of this class.
 * <br>
 * That said, the clusterComputer is specified on the {@code SourceLineNode} 
 * instances, and as such is necessarily formed after the layout computation.
 * @author vpalepu
 *
 */
public class Spine {
  private final DynamicFlowGraph flowGraph;
  private final Graph graphicGraph;
  
  private BarnesHutLayout layoutComputer;
  private AbstractCommunityComputer communityComputer;
  
  public static Spine getInstance(DynamicFlowGraph flowGraph) {
    Spine spine = new Spine(flowGraph, new MultiGraph("cerebro"));
    spine.graphicGraph.setStrict(false);
    spine.graphicGraph.setAutoCreate(true);
    return spine;
  }
  
  private Spine(DynamicFlowGraph flowGraph, Graph graphicGraph) {
    this.flowGraph = flowGraph;
    this.graphicGraph = graphicGraph;
  }
  
  public void initGraphicGraph() {
    int uniqId = 0;
    for(Cell<Integer, Integer, Integer> edge : flowGraph.getEdges()) {
      double weight = edge.getValue();
      System.out.format("%s %s %s\n", edge.getRowKey(), edge.getColumnKey(), weight);
      String fromId = String.valueOf(edge.getRowKey());
      String toId = String.valueOf(edge.getColumnKey());
      
      Node from = this.addNode(fromId);
      Node to = this.addNode(toId);
      
      int id = uniqId++;
      addEdge(from, to, id);
    }
    
    weighEdges();
  }
    private Node addNode(String nodeId) {
      try {
        return graphicGraph.addNode(nodeId);
      } catch(IdAlreadyInUseException nodeInUse) { 
        return graphicGraph.getNode(nodeId);
      }
    }
    
    private Edge addEdge(Node from, Node to, int id) {
      try {
        if(!checkNotNull(from).hasEdgeToward(checkNotNull(to))) {
          Edge graphicEdge = graphicGraph.addEdge(String.valueOf(id), from, to, true);
          graphicEdge.addAttribute("ui.hide");
          return graphicEdge;
        }
      } catch (IdAlreadyInUseException e) {  }
      
      return graphicGraph.getEdge(String.valueOf(id));
    }
    
    private void weighEdges() {
      double maxCount = flowGraph.maxEdgeCount() + 1.0;
      for(Edge edge : graphicGraph.getEachEdge()) {
        Node from = edge.getSourceNode();
        Node to = edge.getTargetNode();
        int fromId = Integer.parseInt(from.toString());
        int toId = Integer.parseInt(to.toString());
        int count = flowGraph.getEdgeCount(fromId, toId);
        double weight = (maxCount - count)/maxCount;
        edge.addAttribute("layout.weight", weight);
      }
    }
  
  public Spine setLayoutComputer(BarnesHutLayout computer) {
    if(computer == null) {
      throw new RuntimeException("The layout computer cannot be null!");
    }
    this.layoutComputer = computer;
    graphicGraph.addSink(layoutComputer);
    layoutComputer.addAttributeSink(graphicGraph);
    return this;
  }
  
  public Spine setCommunityComputer(AbstractCommunityComputer computer) {
    if(computer == null) {
      throw new RuntimeException("The community computer cannot be null!");
    }
    
    this.communityComputer = computer;
    return this;
  }
  
  public Spine computeLayout() {
    double limit = layoutComputer.getStabilizationLimit();
    double stab = 0.0;
    int count = 0;
    while(stab < limit) {
      layoutComputer.compute();
      stab = layoutComputer.getStabilization();
      System.out.println(stab);
      if(count > 100_000) {
        break;
      }
    }
    this.pinNodesOnDynamicFlowGraph();
    return this;
  }
  
    private void pinNodesOnDynamicFlowGraph() {
      for(Node node : graphicGraph.getEachNode()) {
        int nodeId = Integer.valueOf(node.getId());
        SourceLineNode lineNode =  flowGraph.getNode(nodeId);
        Object[] xyz = node.getAttribute("xyz");
        System.out.println(Arrays.toString(xyz));
        double x = (double)xyz[0] * 20;
        double y = (double)xyz[1] * 20;
        lineNode.initXY(x, y);
      }
    }
  
  public Spine detectCommunities() {
    
    communityComputer.compute();
    communityComputer.assignCommunity();
    return this;
  }
  
  public Spine computeVisualClusters() {
    System.out.println("starting the clustering!");
    double eps = 30.0;
    int minPts = 2;
    DBSCANClusterer<SourceLineNode> clusterer = new DBSCANClusterer<>(eps, minPts);
    ArrayList<SourceLineNode> nodes = new ArrayList<>(); 
    HashSet<Integer> visitedNodeIds = new HashSet<>();
    for(Cell<Integer, Integer, Integer> flow : flowGraph.getEdges()) {
      int fromId = flow.getRowKey();
      if(!visitedNodeIds.contains(fromId)) {
        visitedNodeIds.add(fromId);
        SourceLineNode node = flowGraph.getNode(fromId);
        nodes.add(node);
      }
      
      int toId = flow.getColumnKey();
      if(!visitedNodeIds.contains(toId)) {
        visitedNodeIds.add(toId);
        SourceLineNode node = flowGraph.getNode(toId);
        nodes.add(node);
      }
    }
    
    List<Cluster<SourceLineNode>> clusters = clusterer.cluster(nodes);
    System.out.println("this graph has been clustered. (drops mike and walks off)");
    
    int clusterId = -1;
    for(Cluster<SourceLineNode> cluster : clusters) {
      clusterId += 1;
      System.out.println(clusterId);
      for(SourceLineNode node : cluster.getPoints()) {
        SourceLineNode node2 = flowGraph.getNode(node.id());
        node2.initColorGroup(clusterId);
        System.out.println(node2.toString() + " " + node2.colorGroup);
      }
    }
    return this;
  }
  
  public void spitGraph(String subject) throws IOException {
    String fileName = subject + LayoutFactory.layoutConfig; 
    String brainPath = fileName + ".json";
    flowGraph.spitDynamicFlowGraph(new PrintStream(brainPath));
    FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.VGA);
    pic.writeAll(graphicGraph, fileName + ".png");
    System.out.println("\n" + fileName);
  }
}
