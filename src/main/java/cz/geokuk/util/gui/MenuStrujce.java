package cz.geokuk.util.gui;

import javax.swing.*;

import cz.geokuk.core.program.JGeokukToolbar;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.ToggleAction0;

public abstract class MenuStrujce {

	protected final JMenuBar menuBar;
	protected JMenu menu;
	protected JMenuItem item;

	protected final JGeokukToolbar tb;

	protected Factory factory;

	public MenuStrujce(final JMenuBar menuBar, final JGeokukToolbar toolBar) {
		this.menuBar = menuBar;
		tb = toolBar;
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	protected void item(final Action action) {
		if (action instanceof ToggleAction0) {
			final ToggleAction0 moa = (ToggleAction0) action;
			item = new JCheckBoxMenuItem();
			moa.join(item);
			menu.add(item);
		} else {
			item = new JMenuItem(action);
			menu.add(item);
		}
	}

	protected void item(final ToggleAction0 action, final ButtonGroup bg, boolean selected) {
		item = new JRadioButtonMenuItem(action);
		action.join(item);

		menu.add(item);
		bg.add(item);

		item.setSelected(selected);
		bg.setSelected(item.getModel(), selected);
	}

	protected abstract void makeMenu();

	protected void menu(final String name, final String aTooltip) {
		menu = new JMenu(name);
		menu.setToolTipText(aTooltip);
		menuBar.add(menu);
	}

	protected void separator() {
		menu.addSeparator();
	}
}
