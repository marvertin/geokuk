/**
 *
 */
package cz.geokuk.core.render;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.framework.Progressor;


/**
 * @author veverka
 *
 */
public abstract class RendererSwingWorker0 extends MySwingWorker0<RenderResult, Void> implements AfterInjectInit {

	protected RenderModel renderModel;
	private ProgressModel progressModel;
	protected Factory factory;
	protected Progressor progressor;


	public void inject(RenderModel renderModel) {
		this.renderModel = renderModel;
	}

	public void inject(ProgressModel progressModel) {
		this.progressModel = progressModel;
	}

	public void inject(Factory factory) {
		this.factory = factory;

	}

	/* (non-Javadoc)
	 * @see cz.geokuk.framework.AfterInjectInit#initAfterInject()
	 */
	@Override
	public void initAfterInject() {
		progressor = progressModel.start(0, "xxx");
	}

	/* (non-Javadoc)
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

	//  protected Progressor createProgressor(int pocetKachli) {
	////    return progressModel.start(pocetKachli * Rendrovadlo.KOLIK_PROGRESUJEME_NA_KACHLICH, "Rendrování");
	//    return null;
	//  }
}
