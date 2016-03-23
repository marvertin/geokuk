package cz.geokuk.plugins.kesoid.importek;

import javax.swing.Timer;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

public class MultiNacitacLoaderManager {

	private final MultiNacitac multiNacitac;

	private MultiNacitacSwingWorker klsw;

	private Timer iTimer;

	private final KesoidModel kesoidModel;

	public MultiNacitacLoaderManager(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
		multiNacitac = new MultiNacitac(kesoidModel);
	}

	public void startLoad(final boolean prenacti, final Genom genom) {
		if (iTimer != null) {
			iTimer.stop();
		}
		multiNacitac.setRootDirs(prenacti, kesoidModel.getUmisteniSouboru().getKesDir().getEffectiveFileIfActive(), kesoidModel.getUmisteniSouboru().getGeogetDataDir().getEffectiveFileIfActive());
		if (klsw == null || klsw.isDone()) {
			klsw = new MultiNacitacSwingWorker(multiNacitac, genom, kesoidModel);
			klsw.execute();
		}
		startTimer(genom);
	}

	private void startTimer(final Genom genom) {
		iTimer = new Timer(10000, e -> startLoad(false, genom));
		iTimer.start();

	}

}
