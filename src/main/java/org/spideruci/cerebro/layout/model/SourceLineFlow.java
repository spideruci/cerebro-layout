package org.spideruci.cerebro.layout.model;

public class SourceLineFlow {
  public int startId;
  public int endId;
  public float x1, y1;
  public float x2, y2;
  public double value;

  public static <T extends SimpleNode> SourceLineFlow flow(T start, T end, double weight) {
    SourceLineFlow flow = new SourceLineFlow(start.id, end.id, 
                                              start.x, start.y, 
                                              end.x, end.y, 
                                              weight);
    return flow;
  }

  private SourceLineFlow(int start, int end, 
      float x1, float y1, float x2, float y2, double value) {
    this.startId = start;
    this.endId = end;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.value = value;
  }
}