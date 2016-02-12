package org.spideruci.cerebro.layout.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.google.common.collect.Table.Cell;

public class RossalExecCsvReader {
  
  public static final int FROM = 0;
  public static final int TO = 1;
  public static final int WEIGHT = 2;
  
  public DynamicFlowGraph readAll(File ... rossalCsvs) {
    DynamicFlowGraph graph = new DynamicFlowGraph();
    for(File csv : rossalCsvs) {
      if(csv == null || !csv.exists() || !csv.isFile() 
          || !csv.getName().endsWith(".csv")) {
        continue;
      }
      
      DynamicFlowGraph tempGraph = read(csv);
      for(Cell<Integer, Integer, Integer> edge : tempGraph.getEdges()) {
        int fromId = edge.getRowKey();
        SourceLineNode from = tempGraph.getNode(fromId);
        from = graph.addNode(SourceLineNode.clone(from));
        
        int toId = edge.getColumnKey();
        SourceLineNode to = tempGraph.getNode(toId);
        
        to = graph.addNode(SourceLineNode.clone(to));
        
        int count = edge.getValue();
        graph.addEdge(from, to, count);
      }
    }
    return graph;
  }
  
  public DynamicFlowGraph read(File rossalCsv) {
    DynamicFlowGraph graph = new DynamicFlowGraph();
    Scanner scanner = null;
    try {
      scanner = new Scanner(rossalCsv);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    
    while(scanner.hasNextLine()) {
      String line = scanner.nextLine();
      String[] csvSplit = line.split(",");
      String fromString = csvSplit[FROM];
      String toString = csvSplit[TO];
      int weight = Integer.parseInt(csvSplit[WEIGHT]);
      SourceLineNode toNode = rossalstringToNode(fromString);
      SourceLineNode fromNode = rossalstringToNode(toString);
      graph.addEdge(graph.addNode(fromNode), graph.addNode(toNode), weight);
    }
    scanner.close();
    return graph;
  }
  
  private SourceLineNode rossalstringToNode(String nodeString) {
    String ownerClass = nodeString.split(">>")[0].trim().replace("<", "");
    String ownerMethod = nodeString.split(">>")[1].trim().replace(">", "");
    int line = 1;
    SourceLineNode node = new SourceLineNode(ownerClass, ownerMethod, line);
    return node;
  }

}
