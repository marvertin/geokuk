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
public final class FComponent {
	private FComponent() {
	}

	public static void enableMouseSroll(final JComponent component) {
		component.addMouseMotionListener(new ScrollRectToVisibleListener());
		component.setAutoscrolls(true);
	}

	public static void setEnabled(final Component component, final boolean enabled) {
		component.setEnabled(enabled);
		if (component instanceof Container) {
			final Container container = (Container) component;
			for (final Component c : container.getComponents()) {
				setEnabled(c, enabled);
			}
		}
	}

	public static void setEnabledChildren(final Component component, final boolean enabled) {
		if (component instanceof Container) {
			final Container container = (Container) component;
			for (final Component c : container.getComponents()) {
				setEnabled(c, enabled);
			}
		}
	}

}
