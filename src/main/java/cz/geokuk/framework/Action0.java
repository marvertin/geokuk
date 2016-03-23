/**
 *
 */
package cz.geokuk.framework;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.*;

import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.program.MainFrameHolder;

/**
 * @author veverka
 *
 */
public abstract class Action0 extends AbstractAction {
	private static final long	serialVersionUID	= -8430830975286039793L;

	protected Factory			factory;
	protected PoziceModel		poziceModel;
	protected VyrezModel		vyrezModel;

	private MainFrameHolder		mainFrameHolder;

	/**
	 *
	 */
	public Action0() {}

	/**
	 * @param string
	 */
	public Action0(final String string) {
		super(string);
	}

	/**
	 * @param aString
	 * @param aIcon
	 */
	public Action0(final String aString, final Icon aIcon) {
		super(aString, aIcon);
	}

	/**
	 * Spustí akci
	 */
	public void fire() {
		if (!isEnabled()) {
			return; // žádná akce, pokud je zakázána
		}
		int modifiers = 0;
		final AWTEvent currentEvent = EventQueue.getCurrentEvent();
		if (currentEvent instanceof InputEvent) {
			modifiers = ((InputEvent) currentEvent).getModifiers();
		} else if (currentEvent instanceof ActionEvent) {
			modifiers = ((ActionEvent) currentEvent).getModifiers();
		}
		actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(), EventQueue.getMostRecentEventTime(), modifiers));
	}

	public Icon getIcon() {
		return (Icon) getValue(Action.SMALL_ICON);
	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void inject(final MainFrameHolder mainFrameHolder) {
		this.mainFrameHolder = mainFrameHolder;
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	/**
	 * @return
	 */
	protected String getActionCommand() {
		return null;
	}

	/**
	 * Výhradně za účelem definování parenta dialogům.
	 *
	 * @return
	 */
	protected JFrame getMainFrame() {
		return mainFrameHolder.getMainFrame();
	}

}
