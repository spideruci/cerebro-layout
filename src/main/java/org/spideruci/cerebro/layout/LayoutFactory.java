package org.spideruci.cerebro.layout;

import org.graphstream.ui.layout.springbox.BarnesHutLayout;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;

public class LayoutFactory {
  
  public static String layoutConfig = null;
  
  public static SpringBox getSpringBox() {
    SpringBox layout = new SpringBox();
    layoutConfig = "-springbox";
    return layout;
  }

  public static LinLog getLinLog(double a, double r, double force) {
    LinLog layout = new LinLog();
    layout.configure(a, r, true, force);
    layoutConfig += "-linlog";
    layoutConfig += "-a" + String.format("%.2f", a).replace(".", "");
    layoutConfig += "-r" + String.format("%.2f", a).replace(".", "").replace("-", "");
    layoutConfig += "-f" + String.format("%.3f", a).replace(".", "");
    return layout;
  }
  
  public static BarnesHutLayout configLayout(BarnesHutLayout layout) {
    return configLayout(layout, 4, 0.9);
  }
  
  public static BarnesHutLayout configLayout(BarnesHutLayout layout, 
      double qual, double stab) {
    layout.setQuality(qual);
    layout.setBarnesHutTheta(0.5);
    layout.setStabilizationLimit(stab);
    return layout;
  }
}
