package org.spideruci.cerebro.layout;

import com.google.common.collect.Table.Cell;
import org.spideruci.cerebro.Cerebro;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.SourceLineNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import static org.spideruci.cerebro.layout.DynamicDisplay.DEFAULT_TITLE;
import static org.spideruci.cerebro.layout.DynamicDisplay.init;

public class Main {

  public static void main(String[] args) throws
      IOException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

    System.out.println("Reading dynamic graph...");
    DynamicFlowGraph dynamicFlowGraph = Cerebro.getDynamicFlowGraph(args);


    final String subject = args[args.length - 1];
    System.out.println(subject);


    System.out.println("Computing layout, visual clusters, and communities...");
    Cerebro.analyzeDynamicFlow(subject, dynamicFlowGraph);

    dynamicFlowGraph.spitDynamicFlowGraph(System.out);

    DynamicDisplay display = init(DEFAULT_TITLE, dynamicFlowGraph);
    for(Cell<Integer, Integer, Integer> edge : dynamicFlowGraph.getEdges()) {
      int fromId = edge.getRowKey();
      int toId = edge.getColumnKey();
      SourceLineNode from = dynamicFlowGraph.getNode(fromId);
      SourceLineNode to = dynamicFlowGraph.getNode(toId);
      double weight = edge.getValue();
      System.out.format("%s %s %s\n", fromId, toId, weight);
      if(Double.isNaN(from.x)) {
        display.showDependence(fromId, toId, weight);
      } else {
        display.showDependence(fromId, from.x, from.y, toId, to.x, to.y, weight);
      }
    }

    display.colorNodes();
    display.showEdges();

    startCli(display);
    System.exit(0);
  }

  private static void startCli(DynamicDisplay display) {
    Scanner cmdScanner = new Scanner(System.in);


    System.out.println("\n" + Commands.mainMenu);
    System.out.print(">> ");
    while(cmdScanner.hasNextLine()) {
      String line = cmdScanner.nextLine();
      String[] inputs = line.split("\\s+");
      String command = inputs[0];
      String[] params = inputs.length == 1 ? null : Arrays.copyOfRange(inputs, 1, inputs.length);
      try {
        if (!Commands.handleInput(display, command, params)) {
          break;
        }
      } catch(Exception ex) {
        System.out.println("\uD83D\uDCA5 Ooops! Something blew up!");
        System.out.println("Error message: " + ex.getMessage());
        System.out.println("\uD83D\uDC4D Try again another command.");
        System.out.println(Commands.mainMenu);
      }
      System.out.print(">> ");
    }

    cmdScanner.close();
  }


}
