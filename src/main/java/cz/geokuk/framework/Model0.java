/**
 *
 */
package cz.geokuk.framework;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import javax.swing.JFrame;

import cz.geokuk.core.program.MainFrameHolder;

/**
 * @author veverka
 *
 */
public abstract class Model0 implements AfterInjectInit {

	private EventFirer		ef;
	private Prefe			prefe;
	private MainFrameHolder	mainFrameHolder;
	private Factory			factory;

	// TODO udělat zase protected, aže se nebude vyhazovat KeskyVyfiltrovanyEvent
	public void fire(final Event0<?> event) {
		event.setModel(this);
		ef.fire(event);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.AfterInjectInit#initAfterInject()
	 */
	@Override
	public final void initAfterInject() {
		initAndFire();
	}

	public void inject(final EventFirer ef) {
		this.ef = ef;
	}

	public void inject(final Factory factory) {
		this.factory = factory;
	}

	public void inject(final MainFrameHolder mainFrameHolder) {
		this.mainFrameHolder = mainFrameHolder;
	}

	public void inject(final Prefe prefe) {
		this.prefe = prefe;
	}

	public void onEvent(final PreferencesProfileChangedEvent event) {
		// staci jen reloadnout pri zmeně, protože teĎ už prefe.curr()
		// bude spravne preference nastavovat
		reloadPreferences();
	}

	protected MyPreferences currPrefe() {
		return prefe.curr();
	}

	protected <T> T factoryInit(final T obj) {
		return factory.init(obj);
	}

	protected Clipboard getSystemClipboard() {
		final Toolkit toolkit = getMainFrame().getToolkit();
		final Clipboard scl = toolkit.getSystemClipboard();
		return scl;
	}

	/**
	 *
	 */
	protected abstract void initAndFire();

	/**
	 */
	protected void reloadPreferences() {
	}

	private JFrame getMainFrame() {
		return mainFrameHolder.getMainFrame();
	}
}
