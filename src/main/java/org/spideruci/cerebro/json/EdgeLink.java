package org.spideruci.cerebro.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EdgeLink {

	@JsonProperty("@type")
	private String type;
	
	private int startId;
	private int endId;
	private double x1;
	private double y1;
	private double x2;
	private double y2;
	private double value;
	
	public EdgeLink() {
		// TODO Auto-generated constructor stub
	}

	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public int getStartId(){
		return startId;
	}
	
	public void setStartId(int startId){
		this.startId = startId;
	}
	
	public int getEndId(){
		return endId;
	}
	
	public void setEndId(int endId){
		this.endId = endId;
	}
	
	public double getX1(){
		return x1;
	}
	
	public void setX1(double x1){
		this.x1 = x1;
	}
	
	public double getY1(){
		return y1;
	}
	
	public void setY1(double y1){
		this.y1 = y1;
	}
	
	public double getX2(){
		return x2;
	}
	
	public void setX2(double x2){
		this.x2 = x2;
	}
	
	public double getY2(){
		return y2;
	}
	
	public void setY(double y2){
		this.y2 = y2;
	}
	
	public double getValue(){
		return value;
	}
	
	public void setValue(double value){
		this.value = value;
	}
	
	
	@Override
	public String toString(){
		
		String s = "Node [ "
				+ "type: " + type + ", "
				+ "startId: " + startId + ", "
				+ "endId: " + endId + ", "
				+ "x1: " + x1 + ", "
				+ "y1: " + y1 + ", "
				+ "x2: " + x2 + ", "
				+ "y2: " + y2 + ", "
				+ "value: " + value + " ]";
						
		return s;
	}
}
