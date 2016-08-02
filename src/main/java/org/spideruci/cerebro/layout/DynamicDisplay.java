package org.spideruci.cerebro.layout;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
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
import org.spideruci.cerebro.db.DatabaseReader;
import org.spideruci.cerebro.db.SQLiteDB;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.SourceLineNode;


public class DynamicDisplay {
	private Graph graph;
	public Viewer viewer;
	private GraphicGraph ggraph;
	SortedSet<Integer> buffer;
	DynamicFlowGraph displayedGraph;

	LinLog layout;

	private static int uniqId = 0;
	public static final String DEFAULT_TITLE = "Cerebro";
	private static final int BUFFER_LIMIT = 300;

	public static DynamicDisplay init(String title, DynamicFlowGraph displayedGraph) {
		System.setProperty("org.graphstream.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		DynamicDisplay display = new DynamicDisplay(title, displayedGraph);

		display.setup();

		display.launchDisplay();
		return display;
	}

	private DynamicDisplay(String title, DynamicFlowGraph displayedGraph) {
		graph = new MultiGraph(title);
		buffer = Collections.synchronizedSortedSet(new TreeSet<Integer>());
		this.displayedGraph = displayedGraph;
	}

	private void setup() {
		this.graph.setStrict(false);
		this.graph.setAutoCreate(true);
		File file = new File("css/stylesheet.css");
		String url = "url(file://" + file.getAbsolutePath() + ")";
		this.graph.addAttribute("ui.stylesheet", url);
		this.graph.addAttribute("layout.quality", 4);
		this.buffer.clear();
	}

	private void launchDisplay() {
		this.viewer = this.graph.display();
		this.viewer.disableAutoLayout();

		this.ggraph = this.viewer.getGraphicGraph();

		View view = this.viewer.getDefaultView();

		ViewerPipe fromViewer = this.viewer.newViewerPipe();
		ViewerEventListener viewerListener =
				new ViewerEventListener(fromViewer, this.displayedGraph);
		view.setMouseManager(viewerListener);
		view.addMouseWheelListener(viewerListener);
		fromViewer.addViewerListener(viewerListener);
		fromViewer.addSink(this.graph);
	}

	public void clear() {
		this.graph.clear();
	}

	public synchronized void showDependence(int from, int to) {
		showDependence(String.valueOf(from), String.valueOf(to), Double.NaN);
	}

	public synchronized void showDependence(int from, int to, double weight) {
		showDependence(String.valueOf(from), String.valueOf(to), weight);
	}

	public void showDependence(String from, String to) {
		showDependence(from, to, Double.NaN);
	}

	public synchronized void showDependence(String from, String to, double weight) {
		showDependence(from, Double.NaN, Double.NaN, to, Double.NaN, Double.NaN, weight);
	}

	public synchronized void showDependence(int from, double fX, double fY,
			int to, double tX, double tY,
			double weight) {
		showDependence(String.valueOf(from), fX, fY, String.valueOf(to), tX, tY, weight);
	}

	public synchronized void showDependence(String from, double fX, double fY,
			String to, double tX, double tY,
			double weight) {
		showNode(from, fX, fY);
		showNode(to, tX, tY);
		int id = uniqId++;

		try {
			Node fromNode = graph.getNode(from);
			if(fromNode == null || !fromNode.hasEdgeToward(to)) {
				Edge edge = graph.addEdge(String.valueOf(id), from, to, true);
				edge.addAttribute("ui.hide");

				final String style =
						String.format("fill-color: rgba(150,150,150,50);size: %spx;",
								Double.isNaN(weight) ? 5 : plateau(weight));
				edge.addAttribute("ui.style", style);
			}
		} catch (IdAlreadyInUseException e) { }
	}

	private double plateau(double value) {
		Preconditions.checkArgument(!Double.isNaN(value));
		return 1.2*(1 + Math.log(value));
	}

	private void showNode(String nodeId, double x, double y) {
		try {
			Node node = graph.addNode(nodeId);
			if(!Double.isNaN(x)) {
				node.addAttribute("x", x);
			}

			if(!Double.isNaN(y)) {
				node.addAttribute("y", y);
			}

		} catch(IdAlreadyInUseException nodeInUse) { }
	}

	private void removeNode(String nodeId) {
		try {
			graph.removeNode(nodeId);
		} catch(ElementNotFoundException nodeNotFound) {
			// Do nothing for now.
		}
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
			//			double weight = count;
			edge.addAttribute("layout.weight", weight);
		}
	}

	public void colorNodes() {
		int clusterCount = displayedGraph.getClusterCount();
		String[] palette = ColorPalette.generatePalette(clusterCount);

		for(Node node : graph.getEachNode()) {
			int nodeId = Integer.valueOf(node.getId());
			SourceLineNode dynamicNode = displayedGraph.getNode(nodeId);
			int colorIndex = dynamicNode.colorGroup;
			if(colorIndex <= -1) {
				continue;
			}

			colorNode(palette, colorIndex, node);
		}
	}

	public void colorNodesByClass() {
		int classCodeCount = displayedGraph.classCodeCount();
		String[] palette = ColorPalette.generatePalette(classCodeCount);

		for(Node node : graph.getEachNode()) {
			int nodeId = Integer.valueOf(node.getId());
			int colorIndex = displayedGraph.getNodeClassCode(nodeId);

			if(colorIndex <= -1) {
				continue;
			}

			colorNode(palette, colorIndex, node);
		}
	}

	public void colorNodesByMethod() {
		int methodCodeCount = displayedGraph.methodCodeCount();		

		//		System.out.println(methodCodeCount);

		String[] palette = ColorPalette.generatePalette(methodCodeCount);
		for(Node node : graph.getEachNode()) {
			int nodeId = Integer.valueOf(node.getId());
			int colorIndex = displayedGraph.getNodeMethodCode(nodeId);

			//			System.out.println(palette.length + "      " + colorIndex);

			if(colorIndex <= -1) {
				continue;
			}

			colorNode(palette, colorIndex, node);
		}
	}

	private void colorNode(String[] palette, int colorIndex, Node node) {
		if(displayedGraph.suspiciousness)
			displayedGraph.suspiciousness = false;

		Preconditions.checkNotNull(palette);
		//		Preconditions.checkElementIndex(colorIndex, palette.length);
		String color = palette[colorIndex % palette.length];
		changeColor(node, color);

	}

	public void decolorNodes() {
		if(displayedGraph.suspiciousness)
			displayedGraph.suspiciousness = false;

		for(Node node : graph.getEachNode()) {
			changeColor(node, "lightyellow");
		}
	}
	
	private int nodeSize = 4;
	
	public void restoreSize() {
		for(Node node : graph.getEachNode()) {
			node.setAttribute("ui.style", "size: 4px;");
		}
	}


	public void expandNodes() {
		if(nodeSize < 20) {
			nodeSize += 1;
		}

		for(Node node : graph.getEachNode()) {
			node.setAttribute("ui.style",
					String.format("size: %spx;", nodeSize));
		}
	}

	public void shrinkNodes() {
		if(nodeSize > 4) {
			nodeSize -= 1;
		}

		for(Node node : graph.getEachNode()) {
			node.setAttribute("ui.style",
					String.format("size: %spx;", nodeSize));
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
			//        int colorGroup = displayedGraph.getNodeClassCode(nodeId);
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

	public void setStartNode(){
		Node node = graph.getNode(0);
		changeColor(node, "purple");
	}

	public void colorNodesBySuspiciousness() {

		Map<Integer, Double> suspicious = new HashMap<>();
		Map<Integer, Double> confidence = new HashMap<>();
		Map<Integer, Map<Integer, Integer>> stmt = new LinkedHashMap<>();
		Map<String, Integer> source = new HashMap<>();

		Scanner in = new Scanner(System.in);
		System.out.print("Please enter the path to a database file : ");
		String path = in.nextLine();      

		if(path.isEmpty()){
			return;
		}

		SQLiteDB sqliteDB = new SQLiteDB();		
		sqliteDB.open(path);

		DatabaseReader dr = sqliteDB.runDatabaseReader();
		dr.getSTMT();
		dr.getSource();
		dr.getSuspicious();
		dr.getConfidence();

		stmt = dr.getStmtMap();
		suspicious = dr.getSuspiciousMap();
		confidence = dr.getConfidenceMap();
		source = dr.getSourceMap();

		for(Node node : graph.getEachNode()) {
			int nodeId = Integer.valueOf(node.getId());
			SourceLineNode sourceNode = displayedGraph.getNode(nodeId);

			String className = sourceNode.className();
			int lineNum = sourceNode.lineNum();

			className = className.replaceAll("/", ".");

			int sourceId = source.get(className);

			double suspiciousValue;
			double confidenceValue;

			if(stmt.get(sourceId).get(lineNum) == null){
				suspiciousValue = 0.0;
				confidenceValue = 1.0;
			}
			else{
				int stmtId = stmt.get(sourceId).get(lineNum);
				suspiciousValue = suspicious.get(stmtId);
				confidenceValue = confidence.get(stmtId);
			}

			String color = ColorPalette.generateSuspiciousnessColor(suspiciousValue, confidenceValue);
			
			changeColor(node, color);

			sourceNode.setSuspiciousness(suspiciousValue);
			sourceNode.setConfidence(confidenceValue);
			sourceNode.setColorString(color);

		}

		displayedGraph.suspiciousness = true;

	}

	public void colorNodesByLastAuthor() {
		Map<Integer, Map<Integer, String>> author = new LinkedHashMap<>();
		Map<String, Integer> source = new HashMap<>();

		List<String> authorList;

		Scanner in = new Scanner(System.in);
		System.out.print("Please enter the path to a database file : ");
		String path = in.nextLine();      

		if(path.isEmpty()){
			return;
		}

		SQLiteDB sqliteDB = new SQLiteDB();		
		sqliteDB.open(path);

		DatabaseReader dr = sqliteDB.runDatabaseReader();
		dr.getSource();
		dr.getSourceLineAuthor();
		dr.getAuthor();

		author = dr.getAuthorMap();
		source = dr.getSourceMap();

		authorList = dr.getAuthorList();
		Collections.shuffle(authorList);

		int authorCount = dr.getAuthorCount();

		String[] palette = ColorPalette.generatePalette(authorCount);

		Map<String, Integer> authorColorMap = new HashMap<>();

		int i = 0;
		for(String s: authorList){
			authorColorMap.put(s, i);
			i++;
		}

		//		System.out.println(authorColorMap.size());

		for(Node node : graph.getEachNode()) {

			int nodeId = Integer.valueOf(node.getId());

			SourceLineNode sourceNode = displayedGraph.getNode(nodeId);

			String className = sourceNode.className();
			int lineNum = sourceNode.lineNum();

			className = className.replaceAll("/", ".");

			int sourceId = source.get(className);
			//			System.out.println(className + " " + sourceId + " " + lineNum);
			String name = author.get(sourceId).get(lineNum);

			if(author.get(sourceId).get(lineNum) == null){
				System.out.println(sourceId + " " + lineNum);
				continue;
			}

			int color = authorList.indexOf(name);

			colorNode(palette, color, node);

			sourceNode.setAuthor(name);
		}

		displayedGraph.author = true;
	}

	public void colorNodesByAuthor() {
		Map<Integer, Map<Integer, String>> author = new LinkedHashMap<>();
		Map<String, Integer> source = new HashMap<>();
		Map<Integer, List<Integer>> sourceLine = new HashMap<>();
		final int included = 1;
		final int excluded = 0;

		List<String> authorList;

		Scanner in = new Scanner(System.in);
		System.out.print("Please enter the path to a database file : ");
		String path = in.nextLine();      

		if(path.isEmpty()){
			return;
		}

		SQLiteDB sqliteDB = new SQLiteDB();		
		sqliteDB.open(path);

		DatabaseReader dr = sqliteDB.runDatabaseReader();
		dr.getSource();
		dr.getSourceLineAuthor();
		dr.getAuthor();

		author = dr.getAuthorMap();
		source = dr.getSourceMap();

		authorList = dr.getAuthorList();

		int i = 1;
		
		System.out.println("=========================================");

		for(String s: authorList){
			System.out.print(s);
			System.out.print(i % 5 == 0 ? "\n": "   ");
			i++;
		}

		System.out.println();
		System.out.println();
		System.out.print("Please select and enter the author name from above: ");
		String authorName = in.nextLine();      

		while(!authorName.isEmpty()){

			String[] palette = ColorPalette.generateAuthorColor();

			sourceLine = dr.getSourceLineByAuthor(authorName);

			int count = 0;
			
			for(Node node : graph.getEachNode())
			{

				int nodeId = Integer.valueOf(node.getId());

				SourceLineNode sourceNode = displayedGraph.getNode(nodeId);

				String className = sourceNode.className();
				int lineNum = sourceNode.lineNum();

				className = className.replaceAll("/", ".");

				int sourceId = source.get(className);
				//			System.out.println(className + " " + sourceId + " " + lineNum);

				if(sourceLine.get(sourceId) == null || !sourceLine.get(sourceId).contains(lineNum))
					colorNode(palette, excluded, node);
				else{
					colorNode(palette, included, node);
					count++;
				}
			}
			System.out.println(count);
			
			System.out.print("Please select and enter the author name from above: ");

			authorName = in.nextLine();      

		}

	}
	
	
	public void sortNodes(){
		List<String> files = new ArrayList<String>();
		
		Scanner in = new Scanner(System.in);

		System.out.print("Please enter the node id: (node id starts from 1)");
		
		if(!in.hasNextInt()){
			System.out.println("The input is invalid!!!");
			return;
		}
		
		String nodeId = in.nextLine();
			
		Node rootNode = graph.getNode(nodeId);
		rootNode.setAttribute("ui.style", String.format("size: %spx; shape: %s;", "6","diamond"));
		changeColor(rootNode, "red");
		
		int rootNodeId = Integer.parseInt(rootNode.getId());
		SourceLineNode sourceNode = displayedGraph.getNode(rootNodeId);
		files.add(sourceNode.className());
		
		Iterator<Node> sortedNodes = rootNode.getBreadthFirstIterator();
		sortedNodes.next();
		
//		for(int i = 0; i < 10; i++){
		for(int i = 0; files.size() < 10; i++){
			Node nodeNext = sortedNodes.next();

			changeColor(nodeNext, "blue");
			
			int nodeNextId = Integer.parseInt(nodeNext.getId());
			
			SourceLineNode nodeNextLine = displayedGraph.getNode(nodeNextId);
						
			if(!files.contains(nodeNextLine.className()))
					files.add(nodeNextLine.className());

			System.out.println(nodeNextLine.id() + " "+ nodeNextLine.className() + "   " + nodeNextLine.methodName() + "    " + nodeNextLine.lineNum());
			
		}
		System.out.println();
		
		for(String s: files)
			System.out.println(s);
		
//		Iterator<Node> closeNodes = node.getDepthFirstIterator();
//		
//		for(int i = 0; i < 10; i++){
//			Node node2 = closeNodes.next();
//			int index = node2.getIndex();
//			
//			node2.setAttribute("ui.style", String.format("fill-color: %s;", "green"));
//			System.out.println(index);
//			SourceLineNode temp = displayedGraph.getNode(index);
//			System.out.println(temp.className() + "   " + temp.methodName() + "    " + temp.lineNum());
//			System.out.println("---------------------------");
//		}
		

	}
	
	
	protected void changeColor(Node node, String color){
		node.setAttribute("ui.style", String.format("fill-color: %s;", color));
		
		SourceLineNode sln = displayedGraph.getNode(Integer.parseInt(node.getId()));
		
		sln.setColorString(color);

	}
	

}
