/**
 *
 */
package cz.geokuk.plugins.vylety;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.framework.MySwingWorker0;

/**
 * @author veverka
 */
public class VyletSaveSwingWorker extends MySwingWorker0<Vylet, Void> {

	private static final Logger				log	= LogManager.getLogger(VyletSaveSwingWorker.class.getSimpleName());

	private final VyletovyZperzistentnovac	vyletovyZperzistentnovac;
	private final Vylet						vylet;

	public VyletSaveSwingWorker(final VyletovyZperzistentnovac vyletovyZperzistentnovac, final Vylet vylet) {
		this.vyletovyZperzistentnovac = vyletovyZperzistentnovac;
		this.vylet = vylet;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Vylet doInBackground() throws Exception {
		vyletovyZperzistentnovac.immediatlyZapisVylet(vylet);
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void donex() throws InterruptedException, ExecutionException {
		if (isCancelled())
			return;
		final Vylet result = get();
		if (result == null)
			return; // asi zkanclváno
		log.info("Nahran vylet, %d lovenych a %d ignorovanych: \n", result.get(EVylet.ANO).size(), result.get(EVylet.NE).size());
		// Board.eveman.fire(new VyletChangeEvent(result, null, null, null));
	}

}
