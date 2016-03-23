/**
 *
 */
package cz.geokuk.plugins.vylety;


import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.kesoid.KesBag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author veverka
 */
public class VyletLoadSwingWorker extends MySwingWorker0<Vylet, Void> {

	private static final Logger log = LogManager.getLogger(VyletLoadSwingWorker.class.getSimpleName());

	private final VyletovyZperzistentnovac vyletovyZperzistentnovac;
	private final KesBag vsechny;
	private final VyletModel vyletModel;


	public VyletLoadSwingWorker(VyletovyZperzistentnovac vyletovyZperzistentnovac2, KesBag vsechny, VyletModel vyletModel) {
		vyletovyZperzistentnovac = vyletovyZperzistentnovac2;
		this.vsechny = vsechny;
		this.vyletModel = vyletModel;
	}


	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	public Vylet doInBackground() throws Exception {
		return vyletovyZperzistentnovac.immediatlyNactiVylet(vsechny);
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void donex() throws InterruptedException, ExecutionException {
		Vylet result = get();
		if (result == null) return; // asi zkanclváno
		log.info("Nahran vylet, {} lovenych a {} ignorovanych.",
				result.get(EVylet.ANO).size(),
				result.get(EVylet.NE).size());
		vyletModel.setNewVylet(result);
	}


}

