package org.spideruci.cerebro.layout;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.graphstream.ui.layout.Layout;
import org.spideruci.analysis.trace.TraceDirReader;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.RossalExecCsvReader;
import org.spideruci.cerebro.layout.model.SdgReader;
import org.spideruci.cerebro.layout.model.SourceLineNode;

import com.google.common.collect.Table.Cell;

public class Main {

  public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
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

    final String subject = args[args.length - 1];
    dynamicFlowGraph.spitDynamicFlowGraph(System.out);
    
    DynamicDisplay display = DynamicDisplay.init("cerebro", dynamicFlowGraph);
    for(Cell<Integer, Integer, Integer> edge : dynamicFlowGraph.getEdges()) {
      double weight = edge.getValue();
      System.out.format("%s %s %s\n", edge.getRowKey(), edge.getColumnKey(), weight);
      display.showDependence(edge.getRowKey(), edge.getColumnKey(), "");
    }

    //    display.colorNodes();
    display.weighEdges();

    Layout layout = display.getDangerousLayout();
    double limit = layout.getStabilizationLimit();
    System.out.println(limit + " v5");
    int gencount = 0;
    int repeatcount = 0;
    double previousstab = 0.0;
    try {
      while(true) {
        if(gencount++ > 1000) break;
        double stab = layout.getStabilization();
        System.out.println(stab);
        if(previousstab != 0 && stab == previousstab) {
          repeatcount += 1;
          if(repeatcount == 4) {
            break;
          }
        } else {
          repeatcount = 0;
        }
        
        if(stab >= limit) {
          break;
        }
        try {
          Thread.sleep(60000);
        } catch (InterruptedException e) { }
        previousstab = stab;
      }
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
      display.pinNodesOnDynamicFlowGraph();
      String brainPath = subject + "4.json";
      dynamicFlowGraph.spitDynamicFlowGraph(new PrintStream(brainPath));
      System.exit(1);
    }

    display.pinNodesOnDynamicFlowGraph();
    
    System.out.println("starting the clustering!");
    double eps = 30.0;
    int minPts = 2;
    DBSCANClusterer<SourceLineNode> clusterer = new DBSCANClusterer<>(eps, minPts);
    ArrayList<SourceLineNode> nodes = new ArrayList<>(); 
    HashSet<Integer> visitedNodeIds = new HashSet<>();
    for(Cell<Integer, Integer, Integer> flow : dynamicFlowGraph.getEdges()) {
      int fromId = flow.getRowKey();
      if(!visitedNodeIds.contains(fromId)) {
        visitedNodeIds.add(fromId);
        SourceLineNode node = dynamicFlowGraph.getNode(fromId);
        nodes.add(node);
      }
      
      int toId = flow.getColumnKey();
      if(!visitedNodeIds.contains(toId)) {
        visitedNodeIds.add(toId);
        SourceLineNode node = dynamicFlowGraph.getNode(toId);
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
        SourceLineNode node2 = dynamicFlowGraph.getNode(node.id());
        node2.initColorGroup(clusterId);
        System.out.println(node2.toString() + " " + node2.colorGroup);
      }
    }
    
    String brainPath = subject + ".json";
    dynamicFlowGraph.spitDynamicFlowGraph(new PrintStream(brainPath));

    Scanner cmdScanner = new Scanner(System.in);
    System.out.println("\n" + mainMenuCommands);
    System.out.print(">> ");
    while(cmdScanner.hasNextLine()) {
      String line = cmdScanner.nextLine();
      String[] inputs = line.split("\\s+");
      String command = inputs[0];
      String[] params = inputs.length == 1 ? null : Arrays.copyOfRange(inputs, 1, inputs.length);
      if(!mainmenu(display, command, params)) break;
      System.out.print(">> ");
    }

    cmdScanner.close();
    System.exit(0);
  }

  private static final String mainMenuCommands = "Available Commands:"
      + "\n snap image_name \n hide-edges \n show-edges "
      + "\n decolor-nodes \n color-nodes \n done"; 
  private static boolean mainmenu(DynamicDisplay display, 
      String option, 
      String ... params) {
    switch(option) {
    case "snap":
      System.out.println("The [snap] command is currently not working."
          + "\n Open this URL in a browser to open an issue on project's issue "
          + "\n tracker to lobby for this feature -- "
          + "\nhttps://github.com/spideruci/cerebro-layout/issues/new?body=save%20layout%20as%20a%20png%20image%20using%20a%20'snap'%20command&title=snap%20command&labels=enhancement");
      break;
    case "hide-edges":
      display.hideEdges(); break;
    case "show-edges":
      display.showEdges(); break;
    case "decolor-nodes":
      display.decolorNodes(); break;
    case "color-nodes":
      display.colorNodes(); break;
    case "done":
      return false;
    case "help":
    default:
      System.out.println(mainMenuCommands);
    }
    return true;
  }

}
