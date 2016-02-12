package org.spideruci.cerebro.layout.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class SdgReader {
  
  public static final int FROM = 0;
  public static final int TO = 1;
  public static final int WEIGHT = 2;
  
  public DynamicFlowGraph read(File sdg) {
    DynamicFlowGraph graph = new DynamicFlowGraph();
    Scanner scanner = null;
    try {
      scanner = new Scanner(sdg);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    HashMap<String, SourceLineNode> nodes = new HashMap<>();
    
    while(scanner.hasNextLine()) {
      String line = scanner.nextLine();
      
      // get the nodes
      if(line.startsWith("2")) {
        String[] rec = line.split(" ");
        // System.exit(0);
        int lineNumber = Integer.parseInt(rec[4]);
        String className = rec[3];
        String methodName = rec[2];
        SourceLineNode node = new SourceLineNode(className, methodName, lineNumber);
        String ident = rec[1] + methodName;
        nodes.put(ident, node);
        continue;
      }

      // get the edges
      if(line.startsWith("3")) {
        String[] rec = line.split(" ");
        
        String source_line = rec[1];
        String source_method = rec[2];
        SourceLineNode from = nodes.get(source_line + source_method);
        
        String target_line = rec[3];
        String target_method = rec[4];
        SourceLineNode to= nodes.get(target_line + target_method);
        
        if(from == null || to == null) {
          continue;
        }
        
        from = graph.addNode(from);
        to = graph.addNode(to);
        
        graph.addEdge(from, to);
        
        continue;
      }
    }
    
    graph.addFlows(new FlowIdent("dummy", 1));

    return graph;
  }

}
