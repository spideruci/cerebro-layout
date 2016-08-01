package org.spideruci.cerebro.layout.model;

import static org.spideruci.cerebro.layout.DynamicDisplay.DEFAULT_TITLE;
import static org.spideruci.cerebro.layout.DynamicDisplay.init;

import java.io.File;
import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.spideruci.cerebro.json.EdgeLink;
import org.spideruci.cerebro.json.GraphInfo;
import org.spideruci.cerebro.json.LineNode;
import org.spideruci.cerebro.layout.DynamicDisplay;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonReader {

	DynamicFlowGraph dynamicFlowGraph;
	GraphInfo gi;
	
	public JsonReader() {
		// TODO Auto-generated constructor stub
	}

	public DynamicFlowGraph read(File file) {
		ObjectMapper objectMapper = new ObjectMapper();
		//why???
		objectMapper.enable(Feature.ALLOW_NON_NUMERIC_NUMBERS);
		
		dynamicFlowGraph = new DynamicFlowGraph();

		try {
			gi = objectMapper.readValue(file, GraphInfo.class);

			for(LineNode ln: gi.getNodes()){
				SourceLineNode node = new SourceLineNode(ln.getClassName(), ln.getMethodName(), ln.getLineNum());
				node.initXY(ln.getX(), ln.getY());
				node = dynamicFlowGraph.addNode(node);		
			}

			for(EdgeLink el: gi.getLinks()){
				SourceLineNode from = dynamicFlowGraph.getNode(el.getStartId());
				SourceLineNode to = dynamicFlowGraph.getNode(el.getEndId());
				
				dynamicFlowGraph.addEdge(from, to, (int) el.getValue());
			}

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return dynamicFlowGraph;
	}
	


}
