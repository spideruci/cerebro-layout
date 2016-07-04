package org.spideruci.cerebro.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphInfo {
	@JsonProperty("nodes")
	private List<LineNode> nodes;
	
	@JsonProperty("links")
	private List<EdgeLink> links;
	
	@JsonProperty("traces")
	private List<Trace> traces;

	public GraphInfo() {
		// TODO Auto-generated constructor stub
	}
	
	public List<LineNode> getNodes(){
		return nodes;
	}
	
	public LineNode getNode(int index){
		return nodes.get(index);
	}
	
	public void addNode(LineNode node){
		nodes.add(node);
	}
	
	public EdgeLink getLink(int index){
		return links.get(index);
	}
	
	public List<EdgeLink> getLinks(){
		return links;
	}

	public void addLink(EdgeLink link){
		links.add(link);
	}
//	
//	public String toString(){
//		return nodes.toString() + ", " + links.toString();
//	}
}
