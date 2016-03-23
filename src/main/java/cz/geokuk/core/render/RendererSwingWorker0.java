/**
 *
 */
package cz.geokuk.core.render;

import cz.geokuk.framework.*;

/**
 * @author veverka
 *
 */
public abstract class RendererSwingWorker0 extends MySwingWorker0<RenderResult, Void> implements AfterInjectInit {

	protected RenderModel	renderModel;
	private ProgressModel	progressModel;
	protected Factory		factory;
	protected Progressor	progressor;

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterInjectInit#initAfterInject()
	 */
	@Override
	public void initAfterInject() {
		progressor = progressModel.start(0, "xxx");
	}

	public void inject(final Factory factory) {
		this.factory = factory;

	}

	public void inject(final ProgressModel progressModel) {
		this.progressModel = progressModel;
	}

	public void inject(final RenderModel renderModel) {
		this.renderModel = renderModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.MySwingWorker0#donex()
	 */
	@Override
	protected final void donex() throws Exception {
		progressor.finish();
		RenderResult renderResult = null;
		if (!isCancelled()) {
			renderResult = get(); // aby vypadla výjimka
		}
		renderModel.rendrovaniSkoncilo(renderResult);
	}

	// protected Progressor createProgressor(int pocetKachli) {
	//// return progressModel.start(pocetKachli * Rendrovadlo.KOLIK_PROGRESUJEME_NA_KACHLICH, "Rendrování");
	// return null;
	// }
}
