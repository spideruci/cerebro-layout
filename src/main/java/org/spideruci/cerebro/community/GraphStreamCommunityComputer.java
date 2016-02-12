package org.spideruci.cerebro.community;

import java.util.ArrayList;

import org.graphstream.algorithm.community.Community;
import org.graphstream.algorithm.community.DecentralizedCommunityAlgorithm;
import org.graphstream.algorithm.community.Leung;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.SourceLineNode;

public class GraphStreamCommunityComputer extends AbstractCommunityComputer {

  private final DynamicFlowGraph flowGraph;
  private final Graph graphicGraph;
  private final DecentralizedCommunityAlgorithm communityComputer;
  
  public static GraphStreamCommunityComputer getInstance(DynamicFlowGraph flowGraph,
      Graph graphicGraph) {
    GraphStreamCommunityComputer computer = 
        new GraphStreamCommunityComputer(flowGraph, graphicGraph);
    return computer;
  }
  
  protected GraphStreamCommunityComputer(DynamicFlowGraph flowGraph,
      Graph graphicGraph) {
    this.flowGraph = flowGraph;
    this.graphicGraph = graphicGraph;
    this.communityComputer = new Leung();
  }
  
  @Override
  public void compute() {
    communityComputer.staticMode();
    communityComputer.init(graphicGraph);
    for(int i = 0; i < 100_000; i += 1) {
      communityComputer.compute();
      System.out.println(i);
    }
  }

  @Override
  public void assignCommunity() {
    final String communitMarker = communityComputer.getMarker();
    ArrayList<String> communities = new ArrayList<>();
    for(Node node : graphicGraph.getEachNode()) {
      int nodeId = Integer.valueOf(node.getId());
      SourceLineNode lineNode =  flowGraph.getNode(nodeId);
      Community community = node.getAttribute(communitMarker);
      int communityIndex = communities.indexOf(community.getId());
      if(communityIndex == -1) {
        communities.add(community.getId());
        communityIndex = communities.indexOf(community.getId());
      }
      lineNode.initCommunity("" + communityIndex);
      System.out.println(communityIndex);
    }
  }

}
