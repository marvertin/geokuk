/**
 *
 */
package cz.geokuk.plugins.kesoid;

import java.util.concurrent.*;

import cz.geokuk.framework.*;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.index2d.BoundingRect;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Martin Veverka
 */
@Slf4j
public class KesFilteringSwingWorker extends MySwingWorker0<KesBag, Void> {




	private static int citac;
	private final KesBag vsechny;
	private final int cisloFiltrovani;
	private long startTime;
	private final KesoidFilterModel kesoidFilterModel;
	private final ProgressModel progresModel;

	private final KesoidModel kesoidModel;

	/**
	 * @param aBoard
	 * @param aKesList
	 */
	public KesFilteringSwingWorker(final KesBag vsechny, final KesoidFilterModel kesoidFilterModel, final KesoidModel kesoidModel, final ProgressModel progresModel) {
		this.vsechny = vsechny;
		this.kesoidFilterModel = kesoidFilterModel;
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
			final KesBag kesbag = new KesBag(vsechny2.getGenom());
			final BlockingQueue<Wpt> queue = new LinkedBlockingDeque<>();
			log.debug("FILTERING {} - start, source: {}={} waypoints.", cisloFiltrovani, pocetvsech, vsechny2.getIndexator().count(BoundingRect.ALL));
			startTime = System.currentTimeMillis();
			final KesoidFilter filter = kesoidFilterModel.createKesoidFilter();
			new Thread((Runnable) () -> {
				try {
					int citac = 0;
					// System.out.println("VSECHNY: " + vsechny2);
					for (final Wpt wpt : vsechny2.getWpts()) {
						if (isCancelled()) {
							return;
						}
						if (filter.isFiltered(wpt)) {
							queue.put(wpt);
						}
						citac++;
						if (citac % 1000 == 0) {
							progressor.setProgress(citac);
						}

					}
					queue.put(Wpt.ZARAZKA);
				} catch (final InterruptedException ignored) {
				}
			}, "Filtrovani kesoidu").start();
			for (;;) {
				final Wpt wpt = queue.take();
				if (wpt == Wpt.ZARAZKA) {
					break;
				}
				kesbag.expose(wpt);
			}
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
		log.debug("FILTERING {} - finished, filtered {}={} waypoints, {} ms.\n", cisloFiltrovani, result.getWpts().size(),
				result.getIndexator().count(BoundingRect.ALL), System.currentTimeMillis() - startTime);
	}
}
