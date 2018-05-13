package org.spideruci.cerebro.layout.model;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import com.cedarsoftware.util.io.JsonWriter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table.Cell;

public class SimpleGraph<T extends SimpleNode> {

  protected final HashBasedTable<Integer, Integer, Integer> edges;
  protected final ArrayList<T> nodes;
  private int clusterCount; 

  public SimpleGraph() {
    edges = HashBasedTable.create();
    nodes = new ArrayList<>();
    clusterCount = -1;
  }
  
  public T addNode(T node) {
    int id = nodes.indexOf(node);
    if(id == -1) {
      nodes.add(node);
      id = nodes.size();
      node.initId(id);
    } else {
      id = id + 1;
    }
    
    return nodes.get(id - 1);
  }
  
  public void addEdge(T from, T to) {
    addEdge(from, to, 1);
  }
  
  public void addEdge(T from, T to, int count) {
    int fromId = from.id();
    int toId = to.id();
    if(edges.contains(fromId, toId)) {
      int currentCount = edges.get(fromId, toId);
      edges.put(fromId, toId, currentCount + count);
    } else {
      edges.put(fromId, toId, count);
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

  public T getNode(int nodeId) {
    return this.nodes.get(nodeId - 1);
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
    for(T node : this.nodes) {
      spitJson(out, node);
      if(count >= this.nodes.size() - 1) continue;
      out.println(",");
      count += 1;
    }
    out.println("],");

    count = 0;
    out.println("\"links\":[");
    for(Cell<Integer, Integer, Integer> edge : edges.cellSet()) {
      T start = this.getNode(edge.getRowKey());
      T end = this.getNode(edge.getColumnKey());
      double weight = (double) edge.getValue();
      SourceLineFlow flow = SourceLineFlow.flow(start, end, weight);
      spitJson(out, flow);
      if(count >= this.edges.size() - 1) continue;
      out.println(",");
      count += 1;
    }
    out.println("],");
  }

  protected void spitJson(PrintStream out, Object graphElement) {
    String json = JsonWriter.objectToJson(graphElement);
    out.print(json);
  }
  
}
