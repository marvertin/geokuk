/**
 *
 */
package cz.geokuk.framework;

import javax.swing.SwingWorker;

/**
 * @author Martin Veverka
 *
 */
public abstract class MySwingWorker0<T, V> extends SwingWorker<T, V> {

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected final void done() {
		try {
			donex();
		} catch (final Exception e) {
			throw new RuntimeException("Vyjimka pri zpracovani na pozadi.", e);
		}
	}

	protected void donex() throws Exception {}
}
