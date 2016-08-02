package org.spideruci.cerebro.layout.model;

import org.apache.commons.math3.ml.clustering.Clusterable;

import com.google.common.base.Preconditions;

import javafx.scene.paint.Color;

public class SourceLineNode implements Clusterable {
  private final String className;
  private final String methodName;
  private final int lineNum;
  private int id;
  public int colorGroup;
  public float x;
  public float y;
  private String community;
  private double suspiciousness;
  private double confidence;
//  private Color color;
  private String colorString;
  private String author;
  
  public SourceLineNode(String ownerClass, String ownerMethod, int line) {
    this.className = ownerClass;
    this.methodName = ownerMethod;
    this.lineNum = line;
    this.id = -1;
    this.colorGroup = -1;
    this.x = this.y = Float.NaN;
  }
  
  public static SourceLineNode clone(SourceLineNode node) {
    SourceLineNode clone = new SourceLineNode(node.className(), 
        node.methodName(), node.lineNum());
    return clone;
  }

  public String className() {
    return className;
  }

  public String methodName() {
    return methodName;
  }

  public int lineNum() {
    return lineNum;
  }

  public int id() {
    return id;
  }

  public void initId(int id) {
    if(id == -1) {
      throw new RuntimeException("Badly initialized id-value: -1.");
    }

    if(this.id != -1) {
      throw new RuntimeException("Already initialized id for this node:" + this); 
    }

    this.id = id;
  }

  public void initColorGroup(int group) {
    if(group == -1) {
      throw new RuntimeException("Badly initialized id-value: -1.");
    }

    if(this.colorGroup != -1) {
      throw new RuntimeException("Already initialized id for this node:" + this); 
    }

    this.colorGroup = group;
  }


  public void initXY(double x, double y) {
    initXY((float)x, (float)y);
  }

  public void initXY(float x, float y) {
    if(this.x == Float.NaN && this.y == Float.NaN) {
      return;
    }

    this.x = x;
    this.y = y;
  }

  public void resetXY() {
    this.x = Float.NaN;
    this.y = Float.NaN;
  }
  
  public boolean initCommunity(String community) {
    if(this.community != null) {
      return false;
    }
    
    this.community = Preconditions.checkNotNull(community);
    return true;
  }
  
  public void resetCommunity() {
    this.community = null;
  }

  @Override
  public String toString() {
    return "id: " + id + "   className:" + className + "   methodName:" + methodName + "   lineNum:" + lineNum;
  }

  public String toString(String sep) {
    return className + sep + methodName + sep + lineNum;
  }

  @Override
  public int hashCode() {
    int result = 17;

    result = 37 * result + lineNum;
    result = 37 * result + className.hashCode();
    result = 37 * result + methodName.hashCode();

    return result;
  }


  @Override
  public boolean equals(Object object) {
    if(object == null) {
      return false;
    }

    if(!(object instanceof SourceLineNode)) {
      return false;
    }

    SourceLineNode linenode = (SourceLineNode) object;

    if(linenode.lineNum == this.lineNum
        && linenode.className.equals(this.className)
        && linenode.methodName.equals(this.methodName)) {
      return true;
    }

    return false;
  }

  @Override
  public double[] getPoint() {
    return new double[] { x, y };
  }
  
  public double getSuspiciousness(){
	  return suspiciousness;
  }
  
  public void setSuspiciousness(double suspiciousness){
	  this.suspiciousness = suspiciousness;
  }
  
  public double getConfidence(){
	  return confidence;
  }
  
  public void setConfidence(double confidence){
	  this.confidence = confidence;
  }
  
  public String getAuthor(){
	  return author;
  }
  
  public void setAuthor(String author){
	  this.author = author;
  }
//  public Color getColor(){
//	  return color;
//  }
//  
//  public String getColorToString(){
//	  return color.toString();
//  }
//  
//  public void setColor(Color color){
//	  this.color = color;
//  }
//  
  public String getColorString(){
	  return colorString;
  }
  
  public void setColorString(String colorString){
	  this.colorString = colorString;
  }
  
}