/**
 *
 */
package cz.geokuk.framework;

import javax.swing.SwingWorker;

/**
 * @author veverka
 *
 */
public abstract class MySwingWorker0<T, V> extends SwingWorker<T, V> {

	protected void donex() throws Exception {
	}


	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected final void done() {
		try {
			donex();
		} catch (Exception e) {
			throw new RuntimeException("Vyjimka pri zpracovani na pozadi.", e);
		}
	}
}
