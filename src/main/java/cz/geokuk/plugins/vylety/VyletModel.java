/**
 *
 */
package cz.geokuk.plugins.vylety;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author Martin Veverka
 *
 */
public class VyletModel extends Model0 {

	private class Worker extends SwingWorker<Void, Void> {

		private final List<Wpt> wpts;

		public Worker(final List<Wpt> wpts) {
			this.wpts = wpts;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Void doInBackground() throws Exception {
			final Clipboard scl = getSystemClipboard();
			for (final Wpt wpt : wpts) {
				if (isCancelled()) {
					break;
				}
				scl.setContents(new StringSelection(wpt.getUrlProPridaniDoSeznamuVGeogetu().toExternalForm()), null);
				Thread.sleep(100);
			}
			return null;
		}

	}

	private Vylet vylet;

	private VyletovyZperzistentnovac vyletovyZperzistentnovac;

	private Worker worker;

	public void add(final EVylet evyl, final Wpt wpt) {
		final EVylet evylPuvodni = vylet.add(evyl, wpt);
		if (evyl != evylPuvodni) {
			final VyletSaveSwingWorker worker = new VyletSaveSwingWorker(vyletovyZperzistentnovac, vylet);
			worker.execute();
			onChange(wpt, evylPuvodni, evyl);
		}

	}

	public Set<Wpt> get(final EVylet evyl) {
		return vylet.get(evyl);
	}

	public EVylet get(final Wpt wpt) {
		return vylet.get(wpt);
	}

	public void inject(final VyletovyZperzistentnovac vyletovyZperzistentnovac) {
		this.vyletovyZperzistentnovac = vyletovyZperzistentnovac;
	}

	public void nasypVyletDoGeogetu() {
		if (worker != null) {
			worker.cancel(true);
		}
		worker = new Worker(new ArrayList<>(get(EVylet.ANO)));
		worker.execute();
	}

	public void removeAll(final EVylet evyl) {
		vylet.removeAll(evyl);
		final VyletSaveSwingWorker worker = new VyletSaveSwingWorker(vyletovyZperzistentnovac, vylet);
		worker.execute();
		onChange(null, null, null);
	}

	public void setNewVylet(final Vylet newvylet) {
		vylet = newvylet;
		fire(new VyletChangeEvent(this, null, null, null));
	}

	/**
	 * @param vsechny
	 */
	public void startLoadingVylet(final KesBag vsechny) {
		final VyletLoadSwingWorker worker = new VyletLoadSwingWorker(vyletovyZperzistentnovac, vsechny, this);
		worker.execute();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
		setNewVylet(new Vylet());
	}

	private void onChange(final Wpt wpt, final EVylet evylPuvodni, final EVylet evyl) {
		if (!SwingUtilities.isEventDispatchThread()) {
			return;
		}
		fire(new VyletChangeEvent(this, wpt, evyl, evylPuvodni));
	}

}
