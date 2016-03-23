/**
 *
 */
package cz.geokuk.plugins.kesoid;

import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.framework.*;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.index2d.BoundingRect;

/**
 * @author veverka
 */
public class KesFilteringSwingWorker extends MySwingWorker0<KesBag, Void> {

	private static final Logger	log		= LogManager.getLogger(KesFilteringSwingWorker.class.getSimpleName());

	private static final Dvojka	ZARAZKA	= new Dvojka();
	private static int			citac;

	private final KesBag		vsechny;
	private final Genom			iGenom;
	private final int			cisloFiltrovani;
	private long				startTime;
	private final KesFilter		kesfilter;
	private final ProgressModel	progresModel;
	private final KesoidModel	kesoidModel;

	/**
	 * @param aBoard
	 * @param aKesList
	 */
	public KesFilteringSwingWorker(final KesBag vsechny, final Genom genom, final KesFilter kesFilter, final KesoidModel kesoidModel, final ProgressModel progresModel) {
		this.vsechny = vsechny;
		iGenom = genom;
		this.kesfilter = kesFilter;
		this.kesoidModel = kesoidModel;
		this.progresModel = progresModel;
		cisloFiltrovani = ++citac;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected KesBag doInBackground() throws Exception {

		final KesBag vsechny2 = vsechny;
		final int pocetvsech = vsechny2.getWpts().size();
		final Progressor progressor = progresModel.start(pocetvsech, "Filtruji");
		try {
			final KesBag kesbag = new KesBag(iGenom);
			final BlockingQueue<Dvojka> queue = new LinkedBlockingDeque<>();
			log.debug("FILTERING {} - start, source: {} caches, {}={} waypoints.", cisloFiltrovani, vsechny2.getKesoidy().size(), pocetvsech, vsechny2.getIndexator().count(BoundingRect.ALL));
			startTime = System.currentTimeMillis();
			kesfilter.init();
			new Thread((Runnable) () -> {
				try {
					int citac = 0;
					// System.out.println("VSECHNY: " + vsechny2);
					for (final Wpt wpt : vsechny2.getWpts()) {
						if (isCancelled()) {
							return;
						}
						final Genotyp genotyp = wpt.getGenotyp(iGenom);
						// TEn genotyp se předává jen z důvodu optimalizace
						if (kesfilter.isFiltered(wpt, iGenom, genotyp)) {
							final Dvojka dvojka = new Dvojka();
							dvojka.wpt = wpt;
							dvojka.genotyp = genotyp;
							queue.put(dvojka);
						}
						citac++;
						if (citac % 1000 == 0) {
							progressor.setProgress(citac);
						}

					}
					queue.put(ZARAZKA);
				} catch (final InterruptedException ignored) {
				}
			} , "Filtrovani kesoidu").start();
			for (;;) {
				final Dvojka dvojka = queue.take();
				if (dvojka == ZARAZKA) {
					break;
				}
				kesbag.add(dvojka.wpt, dvojka.genotyp);
			}
			kesfilter.done();
			log.debug("FILTERING {} - prepared result, {} ms.", cisloFiltrovani, System.currentTimeMillis() - startTime);
			kesbag.done();
			progressor.finish();
			return kesbag;
		} finally {
			progressor.finish();
			log.debug("FILTERING {} - thread finishing.", cisloFiltrovani);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void donex() throws InterruptedException, ExecutionException {
		if (isCancelled()) {
			log.debug("FILTERING {} - canceled.", cisloFiltrovani);
			return;
		}
		final KesBag result = get();
		if (result == null) {
			return; // asi zkanclváno
		}
		// TODO řešit progresy nějak ssematicky
		kesoidModel.fire(new KeskyVyfiltrovanyEvent(result, vsechny));
		log.debug("FILTERING {} - finished, filtered {} caches, {}={} waypoints, {} ms.\n", cisloFiltrovani, result.getKesoidy().size(), result.getWpts().size(),
				result.getIndexator().count(BoundingRect.ALL), System.currentTimeMillis() - startTime);
	}

	private static class Dvojka {
		Wpt		wpt;
		Genotyp	genotyp;
	}
}
