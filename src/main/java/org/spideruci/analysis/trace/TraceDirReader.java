package org.spideruci.analysis.trace;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.spideruci.analysis.trace.io.TraceReader;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.FlowIdent;
import org.spideruci.cerebro.layout.model.SourceLineNode;

public class TraceDirReader {
  
  public static File[] getFiles(String arg) {
    File file = new File(arg);
    if(!file.exists()) {
      throw new RuntimeException("Specified file path does not point to an exisiting file.");
    }
    
    File[] files = null;
    if(!file.isFile()) {
      System.out.println("Specified file path is not pointing to a file."
          + "\nAttempting to read multiple Trace (.trc) files.");
      files = getTracefiles(file);
    } else {
       files = new File[] { file };
    }
    
    return files;
  }
  
    private static File[] getTracefiles(File file) {
      File[] files = null;
      ArrayList<File> fs  = new ArrayList<>();
      for(File fl : file.listFiles()) {
        if(fl.isFile() && fl.getName().endsWith(".trc")) {
          fs.add(fl);
          System.out.println("Adding file: " + fl.toString());
        }
      }
      files = fs.toArray(new File[fs.size()]);
      return files;
    }
  
  public static DynamicFlowGraph scanTraceFiles(File[] files, final String path) 
      throws IOException {
    if(path == null) {
      throw new RuntimeException("path is null ... fuck it!");
    }
    
    DynamicFlowGraph dynamicFlowGraph = new DynamicFlowGraph();
    for(File f : files) {
      if(f == null || !f.isFile() || !f.getName().endsWith(".trc")) {
        continue;
      }
      
      String name = f.getName();
      System.out.println("Scanning " + name);
      PrintStream jsonTraceStream = new PrintStream(path + name + ".json");
      jsonTraceStream.println("[");
      
      TraceReader traceReader = new TraceReader(f);
      HashMap<String, SourceLineNode> threadedPrevious = new HashMap<>();
      int count = 0;
      
      while(true) {
        TraceEvent event = traceReader.readNextExecutedEvent();
        if(event == null) {
          break;
        }
        
        if(event.getExecInsnType() != EventType.$line$) {
          continue;
        }
        
        String ownerClass = traceReader.getExecutedEventOwnerClass(event);
        String ownerMethod = traceReader.getExecutedEventOwnerMethod(event);
        int lineNumber = traceReader.getExecutedEventSourceLine(event);
        String threadId = event.getExecThreadId();
        
        if(ownerClass.contains("org/spideruci/tacoco"))
        	continue;
        
        SourceLineNode node = new SourceLineNode(ownerClass, ownerMethod, lineNumber);
        
        node = dynamicFlowGraph.addNode(node);
        
        spitJsonTrace(jsonTraceStream, node.id());
        count += 1;
        SourceLineNode previous = threadedPrevious.get(threadId); 
        if(previous != null) {
          dynamicFlowGraph.addEdge(previous, node);
        }
        
        threadedPrevious.put(threadId, node);
      }
      
      jsonTraceStream.println("0]");
      FlowIdent ident = new FlowIdent(name, count);
      dynamicFlowGraph.addFlows(ident);
    }
    
    return dynamicFlowGraph;
  }
  
  protected static void spitJsonTrace(PrintStream out, int nodeId) {
    out.print(nodeId + ",\n");
  }
  
  

}
