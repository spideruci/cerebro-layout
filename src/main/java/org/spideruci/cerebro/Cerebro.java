package org.spideruci.cerebro;

import static org.spideruci.cerebro.layout.LayoutFactory.configLayout;
import static org.spideruci.cerebro.layout.LayoutFactory.getSpringBox;

import java.io.File;
import java.io.IOException;

import org.graphstream.ui.layout.springbox.BarnesHutLayout;

import org.spideruci.analysis.trace.TraceDirReader;
import org.spideruci.cerebro.community.JungCommunityComputer;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.RossalExecCsvReader;
import org.spideruci.cerebro.layout.model.SdgReader;

public class Cerebro {
  
  public static void main(String[] args) throws IOException {
    DynamicFlowGraph dynamicFlowGraph = getDynamicFlowGraph(args);
    
    String subject = args[args.length - 1];
    System.out.println(subject);
    dynamicFlowGraph.spitDynamicFlowGraph(System.out);

    analyzeDynamicFlow(subject, dynamicFlowGraph);
  }

  public static void analyzeDynamicFlow(
      final String subject, final DynamicFlowGraph dynamicFlowGraph) throws IOException {

    Spine spine = Spine.getInstance(dynamicFlowGraph);
    BarnesHutLayout layout = configLayout(getSpringBox());

    spine.setLayoutComputer(layout);
    spine.initGraphicGraph();
    spine.computeLayout();

    spine.computeVisualClusters();

//    JungCommunityComputer communityComputer =
//        JungCommunityComputer.getInstance(dynamicFlowGraph);
//    spine.setCommunityComputer(communityComputer);
//    spine.detectCommunities();

    spine.spitGraph(subject);
  }
  
  public static DynamicFlowGraph getDynamicFlowGraph(String[] args) throws IOException {
    if(args.length == 0 || args[0] == null || args[0].length() == 0) {
      throw new RuntimeException("Specify file path as argument");
    }
    
    DynamicFlowGraph dynamicFlowGraph = null;
    if(args.length > 2) {
      File[] csvFiles = new File[args.length - 1];
      for(int i = 0; i < args.length - 1; i += 1) {
        File csvFile = new File(args[i]);
        csvFiles[i] = csvFile;
      }
      dynamicFlowGraph = new RossalExecCsvReader().readAll(csvFiles);
    } else if(args[0].endsWith(".csv")) {
      dynamicFlowGraph = new RossalExecCsvReader().read(new File(args[0]));
    } else if(args[0].endsWith(".sdg")) {
      dynamicFlowGraph = new SdgReader().read(new File(args[0]));
    } else {
      String tracePath = args[0];
      File[] files = TraceDirReader.getFiles(args[0]);
      dynamicFlowGraph = TraceDirReader.scanTraceFiles(files, tracePath);
    } 

    return dynamicFlowGraph;
  }
  
}
