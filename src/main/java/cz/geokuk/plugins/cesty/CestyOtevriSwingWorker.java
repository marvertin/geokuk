/**
 *
 */
package cz.geokuk.plugins.cesty;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.cesty.data.Doc;
import cz.geokuk.plugins.kesoid.KesBag;

/**
 * @author veverka
 *
 */
public class CestyOtevriSwingWorker extends MySwingWorker0<Doc, Void> {

	private static final Logger			log	= LogManager.getLogger(CestyOtevriSwingWorker.class.getSimpleName());

	private final CestyZperzistentnovac	cestyZperzistentnovac;
	private final KesBag				kesBag;
	private final CestyModel			cestyModel;
	private final File					file;

	public CestyOtevriSwingWorker(final CestyZperzistentnovac cestyZperzistentnovac, final KesBag vsechny, final CestyModel cestyModel, final File file) {
		this.cestyZperzistentnovac = cestyZperzistentnovac;
		kesBag = vsechny;
		this.cestyModel = cestyModel;
		this.file = file;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	public Doc doInBackground() throws Exception {
		final Doc doc = new Doc();
		doc.setFile(file);
		final List<Cesta> cesty = cestyZperzistentnovac.nacti(Collections.singletonList(file), kesBag);
		for (final Cesta cesta : cesty) {
			doc.xadd(cesta);
		}
		return doc;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void donex() throws InterruptedException, ExecutionException {
		final Doc doc = get();
		if (doc == null) {
			return; // asi zkanclváno
		}
		log.info("Načten dokument {}.", doc.getFile());
		cestyModel.prevezmiNoveOtevrenyDokument(doc);
	}

}
