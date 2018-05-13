package org.spideruci.cerebro.layout.model;

public class SourceLineNode extends SimpleNode {
  public final String className;
  public final String methodName;
  public final int lineNum;

  public SourceLineNode(String ownerClass, String ownerMethod, int line) {
    this.className = ownerClass;
    this.methodName = ownerMethod;
    this.lineNum = line;
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

  @Override
  public String toString() {
    return id + ":" + className + ":" + methodName + ":" + lineNum;
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

}