package org.spideruci.cerebro.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LineNode {

	@JsonProperty("@type")
	private String type;
	
	private String className;
	private String methodName;
	private int lineNum;
	private int id;
	private int colorGroup;
	private double x;
	private double y;
	private String community;
	
	public LineNode() {
		// TODO Auto-generated constructor stub
	}
	
	public LineNode get(){
		return this;
	}
	
	public void set(LineNode lineNode){
		this.type = lineNode.type;
		this.className = lineNode.className;
		this.methodName = lineNode.methodName;
		this.lineNum = lineNode.lineNum;
		this.id = lineNode.id;
		this.colorGroup = lineNode.colorGroup;
		this.x = lineNode.x;
		this.y = lineNode.y;
		this.community = lineNode.community;
	}
	
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public String getClassName(){
		return className;
	}
	
	public void setClassName(String className){
		this.className = className;
	}
	
	public String getMethodName(){
		return methodName;
	}
	
	public void setMethodName(String methodName){
		this.methodName = methodName;
	}
	
	public int getLineNum(){
		return lineNum;
	}
	
	public void setLineNum(int lineNum){
		this.lineNum = lineNum;
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getColorGroup(){
		return colorGroup;
	}
	
	public void setColorGroup(int colorGroup){
		this.colorGroup = colorGroup;
	}
	
	public double getX(){
		return x;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public double getY(){
		return y;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public String getCommunity(){
		return community;
	}
	
	public void setCommunity(String community){
		this.community = community;
	}

	@Override
	public String toString(){
		String s = "Node [ "
						+ "type: " + type + ", "
						+ "className: " + className + ", "
						+ "methodName: " + methodName + ", "
						+ "lineNum: " + lineNum + ", "
						+ "id: " + id + ", "
						+ "colorGroup: " + colorGroup + ", "
						+ "x: " + x + ", "
						+ "y: " + y + ", "
						+ "community: " + community + " ]";
								
		return s;
	}
}
