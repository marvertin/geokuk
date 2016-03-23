package cz.geokuk.plugins.kesoid.mapicon;

import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

/**
 * @author Martin Veverka
 *
 */
public class IkonNacitacSwingWorker extends MySwingWorker0<IkonBag, Void> {

	private final IkonNacitacLoader ikonNacitac;
	private final boolean iPrenacti;
	private final KesoidModel kesoidModel;

	/**
	 * @param aBoard
	 */
	public IkonNacitacSwingWorker(final IkonNacitacLoader multiNacitac, final boolean prenacti, final KesoidModel kesoidModel) {
		ikonNacitac = multiNacitac;
		iPrenacti = prenacti;
		this.kesoidModel = kesoidModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected IkonBag doInBackground() throws Exception {
		assert kesoidModel.getJmenoAktualniSadyIkon() != null;
		return ikonNacitac.nacti(this, iPrenacti, kesoidModel.getJmenoAktualniSadyIkon());
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
		final IkonBag result = get();
		if (result == null) {
			return; // asi zkanclváno
		}
		// System.out.printf("Loaded %d caches, %d=%d waypoints: \n",
		// result.getKesky().size(),
		// result.getWpts().size(),
		// result.getIndexator().count(BoundingRect.ALL));
		kesoidModel.setIkonBag(result);
	}

}
