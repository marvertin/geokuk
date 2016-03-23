package cz.geokuk.util.gui;

import javax.swing.*;

import cz.geokuk.core.program.JGeokukToolbar;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.ToggleAction0;

public abstract class MenuStrujce {

	protected final JMenuBar		menuBar;
	protected JMenu					menu;
	protected JMenuItem				item;

	protected final JGeokukToolbar	tb;

	protected Factory				factory;

	public MenuStrujce(JMenuBar menuBar, JGeokukToolbar toolBar) {
		this.menuBar = menuBar;
		tb = toolBar;
	}

	public JMenuBar getMenuBar() {
		return menuBar;
	}

	protected void menu(String name, String aTooltip) {
		menu = new JMenu(name);
		menu.setToolTipText(aTooltip);
		menuBar.add(menu);
	}

	protected void item(Action action) {
		if (action instanceof ToggleAction0) {
			ToggleAction0 moa = (ToggleAction0) action;
			item = new JCheckBoxMenuItem();
			moa.join(item);
			menu.add(item);
		} else {
			item = new JMenuItem(action);
			menu.add(item);
		}
	}

	protected void item(ToggleAction0 action, ButtonGroup bg) {
		item = new JRadioButtonMenuItem(action);
		action.join(item);
		menu.add(item);
		bg.add(item);
	}

	protected void separator() {
		menu.addSeparator();
	}

	public void inject(Factory factory) {
		this.factory = factory;
	}

	protected abstract void makeMenu();
}
