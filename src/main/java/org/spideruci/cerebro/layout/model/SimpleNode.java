package org.spideruci.cerebro.layout.model;

import org.apache.commons.math3.ml.clustering.Clusterable;

import com.google.common.base.Preconditions;

public class SimpleNode implements Clusterable {
  public int id;
  public int colorGroup;
  public float x;
  public float y;
  public float z;
  public String community;
  
  public SimpleNode() {
    this.id = -1;
    this.colorGroup = -1;
    this.x = this.y = this.z = Float.NaN;
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
    initXYZ((float)x, (float)y, 0.0f);
  }
  
  public void initXYZ(double x, double y, double z) {
    initXYZ((float)x, (float)y, (float)z);
  }

  public void initXY(float x, float y) {
    initXYZ(x, y, 0.0f);
  }
  
  public void initXYZ(float x, float y, float z) {
    if(this.x == Float.NaN && this.y == Float.NaN && this.z == Float.NaN) {
      return;
    }

    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void resetXY() {
    resetXYZ();
  }
  
  public void resetXYZ() {
    this.x = Float.NaN;
    this.y = Float.NaN;
    this.z = Float.NaN;
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
  public double[] getPoint() {
    return new double[] { x, y };
  }
  
  public double[] getPoint3D() {
    return new double[] { x, y, z };
  }

}
