/**
 *
 */
package cz.geokuk.plugins.kesoid.importek;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.index2d.BoundingRect;

/**
 * @author veverka
 */
public class MultiNacitacSwingWorker extends MySwingWorker0<KesBag, Void> {

	private static final Logger log = LogManager.getLogger(MultiNacitacSwingWorker.class.getSimpleName());

	private final MultiNacitac multiNacitac;
	private final Genom iGenom;
	private final KesoidModel kesoidModel;

	/**
	 * @param aBoard
	 */
	public MultiNacitacSwingWorker(final MultiNacitac multiNacitac, final Genom genom, final KesoidModel kesoidModel) {
		this.multiNacitac = multiNacitac;
		iGenom = genom;
		this.kesoidModel = kesoidModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected KesBag doInBackground() throws IOException {
		return multiNacitac.nacti(this, iGenom);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void donex() throws InterruptedException, ExecutionException {
		if (isCancelled()) {
			return;
		}
		final KesBag result = get();
		if (result == null) {
			return; // asi zkanclváno
		}
		log.info("Loaded {} caches, {}={} waypoints.", result.getKesoidy().size(), result.getWpts().size(), result.getIndexator().count(BoundingRect.ALL));
		final long cas = System.currentTimeMillis();
		kesoidModel.setVsechnyKesoidy(result);
		log.debug("Cas zpracování načtených kešíků CAS " + (System.currentTimeMillis() - cas));
	}

}
