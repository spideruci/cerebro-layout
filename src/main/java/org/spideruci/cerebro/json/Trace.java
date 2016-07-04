package org.spideruci.cerebro.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Trace {

	@JsonProperty("@type")
	private String type;
	
	private String name;
	private long size;
	
	public Trace() {
		// TODO Auto-generated constructor stub
	}

	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}

	public long getSize(){
		return size;
	}
	
	public void setSize(long size){
		this.size = size;
	}	
	
}
