/**
 * 
 */
package cz.geokuk.util.gui;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;


/**
 * @author veverka
 *
 */
public final class FComponent {  private FComponent() {}

public static void enableMouseSroll(JComponent component) {
  component.addMouseMotionListener(new ScrollRectToVisibleListener());
  component.setAutoscrolls(true);
}

public static void setEnabled(Component component, boolean enabled) {
  component.setEnabled(enabled);
  if (component instanceof Container) {
    Container container = (Container) component;
    for (Component c : container.getComponents())
      setEnabled(c, enabled);
  }
}

public static void setEnabledChildren(Component component, boolean enabled) {
  if (component instanceof Container) {
    Container container = (Container) component;
    for (Component c : container.getComponents())
      setEnabled(c, enabled);
  }
}


}
