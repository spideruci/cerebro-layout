package org.spideruci.analysis.trace;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spideruci.analysis.trace.io.TraceReader;
import org.spideruci.cerebro.layout.model.DynamicFlowGraph;
import org.spideruci.cerebro.layout.model.FlowIdent;
import org.spideruci.cerebro.layout.model.SourceLineNode;

public class MethodTraceDirReader extends TraceDirReader {
	private static HashMap<String, Map<String, SourceLineNode>> methodMap = new HashMap<>();

	public MethodTraceDirReader() {
		// TODO Auto-generated constructor stub
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
			String prevMethod = "";
			String prevClass = "";

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

				SourceLineNode node = new SourceLineNode(ownerClass, ownerMethod, lineNumber);

				if(!ownerMethod.equals(prevMethod) && !ownerClass.equals(prevClass)){
					if(!methodMap.containsKey(node.methodName())){
						node = dynamicFlowGraph.addNode(node);
						Map<String, SourceLineNode> temp = new HashMap<>();
						temp.put(node.className(), node);
						methodMap.put(node.methodName(), temp);
					}
					else{
						if(methodMap.get(node.methodName()).get(node.className()) != null)
							node = methodMap.get(node.methodName()).get(node.className());
						else{
							node = dynamicFlowGraph.addNode(node);
							methodMap.get(node.methodName()).put(node.className(), node);
						}
					}


					spitJsonTrace(jsonTraceStream, node.id());
					count += 1;
					SourceLineNode previous = threadedPrevious.get(threadId); 
					if(previous != null) {
						dynamicFlowGraph.addEdge(previous, node);
					}

					threadedPrevious.put(threadId, node);
				}
				prevMethod = ownerMethod;
				prevClass = ownerClass;
			}

			jsonTraceStream.println("0]");
			FlowIdent ident = new FlowIdent(name, count);
			dynamicFlowGraph.addFlows(ident);
		}

		return dynamicFlowGraph;
	}

}
