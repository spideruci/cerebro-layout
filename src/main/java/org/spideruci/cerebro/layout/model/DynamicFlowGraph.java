package org.spideruci.cerebro.layout.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.spideruci.analysis.trace.TraceEvent;
import org.spideruci.cerebro.layout.DynamicDisplay;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

public class DynamicFlowGraph {
  private final HashBasedTable<Integer, Integer, Integer> edges;
  private final ArrayList<SourceLineNode> nodes;  
  private final ArrayList<String> classCodes;
  private final ArrayList<String> methodCodes;
  private int clusterCount;
  private final ArrayList<FlowIdent> flows;
  public boolean cluster = false;

  public DynamicFlowGraph() {
    edges = HashBasedTable.create();
    nodes = new ArrayList<>();
    classCodes = new ArrayList<>();
    methodCodes = new ArrayList<>();
    flows = new ArrayList<>();
    clusterCount = -1;
  }

  public SourceLineNode addNode(TraceEvent event) {
    String className = event.getExecInsnDynHost();
    SourceLineNode lineNode = 
        new SourceLineNode(className, 
            event.getExecInsnDynHost(), 
            Integer.parseInt(event.getExecInsnEventId()));
    int id = nodes.indexOf(lineNode);

    if(id == -1) {
      nodes.add(lineNode);
      id = nodes.size();
      lineNode.initId(id);
    } else {
      id = id + 1;
    }

    int classCode = classCodes.indexOf(className);
    if(classCode == -1) {
      classCodes.add(className);
    }

    String methodName = className + "." + lineNode.methodName();
    int methodCode = methodCodes.indexOf(methodName);
    if(methodCode == -1) {
      methodCodes.add(methodName);
    }

    return nodes.get(id - 1);
  }
  
  public SourceLineNode addNode(SourceLineNode node) {
    int id = nodes.indexOf(node);
    if(id == -1) {
      nodes.add(node);
      id = nodes.size();
      node.initId(id);
    } else {
      id = id + 1;
    }
    
    String className = node.className();
    int classCode = classCodes.indexOf(className);
    if(classCode == -1) {
      classCodes.add(className);
    }

    String methodName = className + "." + node.methodName();
    int methodCode = methodCodes.indexOf(methodName);
    if(methodCode == -1) {
      methodCodes.add(methodName);
    }
    
    return nodes.get(id - 1);
  }

  public void addEdge(SourceLineNode from, SourceLineNode to) {
    addEdge(from, to, 1);
  }
  
  public void addEdge(SourceLineNode from, SourceLineNode to, int count) {
    int fromId = from.id();
    int toId = to.id();
    if(edges.contains(fromId, toId)) {
      int currentCount = edges.get(fromId, toId);
      edges.put(fromId, toId, currentCount + count);
    } else {
      edges.put(fromId, toId, count);
    }
  } 

  public void addFlows(FlowIdent ... idents) {
    for(FlowIdent i : idents) {
      flows.add(i);
    }
  }

  public Set<Cell<Integer,Integer,Integer>> getEdges() {
    return edges.cellSet();
  }

  public int maxEdgeCount() {
    return Collections.max(edges.values());
  }
  
  public int getEdgeCount(int fromId, int toId) {
    int count = edges.get(fromId, toId);
    return count;
  }

  public SourceLineNode getNode(int nodeId) {
    return this.nodes.get(nodeId - 1);
  }

  public int classCodeCount() {
    return classCodes.size();
  }

  public int getNodeClassCode(int nodeId) {
    SourceLineNode node = nodes.get(nodeId - 1);
    String className = node.className();
    int colorCode = classCodes.indexOf(className);
    return colorCode;
  }

  public int methodCodeCount() {
    return methodCodes.size();
  }

  public int getNodeMethodCode(int nodeId) {
    SourceLineNode node = nodes.get(nodeId - 1);
    String methodName = node.className() + "." + node.methodName();
    int methodCode = methodCodes.indexOf(methodName);
    return methodCode;
  }

  public void setClusterCount(int clusterCount) {
    this.clusterCount = clusterCount;
  }

  public int getClusterCount() {
    return this.clusterCount;
  }

  public void spitDynamicFlowGraph(PrintStream out) {
    int count = 0;
    out.println("{\"nodes\":[");
    for(SourceLineNode node : this.nodes) {
      spitJson(out, node);
      if(count >= this.nodes.size() - 1) continue;
      out.println(",");
      count += 1;
    }
    out.println("],");

    count = 0;
    out.println("\"links\":[");
    for(Cell<Integer, Integer, Integer> edge : edges.cellSet()) {
      SourceLineNode start = this.getNode(edge.getRowKey());
      SourceLineNode end = this.getNode(edge.getColumnKey());
      double weight = (double) edge.getValue();
      SourceLineFlow flow = SourceLineFlow.flow(start, end, weight);
      spitJson(out, flow);
      if(count >= this.edges.size() - 1) continue;
      out.println(",");
      count += 1;
    }
    out.println("],");

    count = 0;
    out.println("\"traces\":[");
    for(FlowIdent i : flows) {
      spitJson(out, i);
      if(count >= this.flows.size() - 1) continue;
      out.println(",");
      count += 1;
    }
    out.println("]}");
  }

  private void spitJson(PrintStream out, Object graphElement) {
    String json = JsonWriter.objectToJson(graphElement);
    out.print(json);
  }

}
