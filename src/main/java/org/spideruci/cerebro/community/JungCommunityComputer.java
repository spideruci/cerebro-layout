package org.spideruci.cerebro.community;

import java.util.Set;

import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.SourceLineNode;

import com.google.common.collect.Table.Cell;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class JungCommunityComputer extends AbstractCommunityComputer {
  
  private DirectedSparseMultigraph<Integer, Integer> jungGraph;
  private DynamicFlowGraph flowGraph;
  private Set<Set<Integer>> communities;
   
  public static JungCommunityComputer getInstance(DynamicFlowGraph flowGraph) {
    JungCommunityComputer computer = new JungCommunityComputer(flowGraph);
    computer.populateFromFlows(flowGraph);
    
    return computer;
  }
  
    private JungCommunityComputer(DynamicFlowGraph flowGraph) {
      this.flowGraph = flowGraph;
      this.jungGraph = new DirectedSparseMultigraph<>();
      this.communities = null;
    }

    private void populateFromFlows(DynamicFlowGraph flowGraph) {
      int eid = 0;
      for(Cell<Integer, Integer, Integer> edge : flowGraph.getEdges()) {
        int from = edge.getRowKey();
        int to = edge.getColumnKey();
        if(!jungGraph.containsVertex(from)) {
          jungGraph.addVertex(from);
        }
        
        if(!jungGraph.containsVertex(to)) {
          jungGraph.addVertex(to);
        }
        
        int edgeCount = edge.getValue();
        
        for(int i = 0; i < edgeCount; i += 1) {
          jungGraph.addEdge(eid, from, to, EdgeType.DIRECTED);
          eid += 1;
        }
      }
    }
    
  
    
  /**
   * 
   * @param fraction -- the fraction of edges that should be removed. If set to 
   * {@value 0.0} then a default value of {@value 0.3}, i.e., 
   * {@value 30%} will be used.
   */
  public void compute(double fraction) {
    int edgeCount = jungGraph.getEdgeCount();
    EdgeBetweennessClusterer<Integer, Integer> computer = 
        new EdgeBetweennessClusterer<>((int)(edgeCount * fraction));
    
    this.communities = computer.transform(jungGraph);
  }

  @Override
  public void compute() {
    this.compute(0.2);
  }

  @Override
  public void assignCommunity() {
    int communityId = 0;
    for(Set<Integer> community : communities) {
      for(int nodeId : community) {
        SourceLineNode node = flowGraph.getNode(nodeId);
        node.initCommunity(String.valueOf(communityId));
      }
      communityId += 1;
      System.out.println(communityId);
    }
  }

}
