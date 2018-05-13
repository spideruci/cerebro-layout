package org.spideruci.cerebro.layout.model;

import java.io.PrintStream;
import java.util.ArrayList;

import org.spideruci.analysis.trace.TraceEvent;

public class DynamicFlowGraph extends SimpleGraph<SourceLineNode> {
  private int clusterCount;
  private final ArrayList<FlowIdent> flows;
  
  public final StringArrayRegistry classCodes;
  public final StringArrayRegistry methodCodes;

  public DynamicFlowGraph() {

    classCodes = new StringArrayRegistry();
    methodCodes = new StringArrayRegistry();
    flows = new ArrayList<>();
    clusterCount = -1;
  }

  public SourceLineNode addNode(TraceEvent event) {
    String className = event.getExecInsnDynHost();
    String methodName = event.getExecInsnDynHost();
    int lineNumber = Integer.parseInt(event.getExecInsnEventId()); 
    SourceLineNode lineNode =  new SourceLineNode(className, methodName, lineNumber);
    return addNode(lineNode);
  }
  
  @Override
  public SourceLineNode addNode(SourceLineNode node) {
    String className = node.className;
    classCodes.register(className);

    String methodName = className + "." + node.methodName;
    methodCodes.register(methodName);
    
    return super.addNode(node);
  }

  public void addFlows(FlowIdent ... idents) {
    for(FlowIdent i : idents) {
      flows.add(i);
    }
  }

  public int getNodeClassCode(int nodeId) {
    SourceLineNode node = nodes.get(nodeId - 1);
    String className = node.className();
    int colorCode = classCodes.entryCode(className);
    return colorCode;
  }

  public int getNodeMethodCode(int nodeId) {
    SourceLineNode node = nodes.get(nodeId - 1);
    String methodName = node.className() + "." + node.methodName();
    int methodCode = methodCodes.entryCode(methodName);
    return methodCode;
  }

  public void setClusterCount(int clusterCount) {
    this.clusterCount = clusterCount;
  }

  public int getClusterCount() {
    return this.clusterCount;
  }

  @Override
  public void spitDynamicFlowGraph(PrintStream out) {
    super.spitDynamicFlowGraph(out);

    int count = 0;
    out.println("\"traces\":[");
    for(FlowIdent i : flows) {
      spitJson(out, i);
      if(count >= this.flows.size() - 1) continue;
      out.println(",");
      count += 1;
    }
    out.println("]}");
  }

}
