package cz.geokuk.plugins.kesoid.importek;

import java.util.List;

import javax.swing.Timer;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.file.Filex;
import cz.geokuk.util.file.KeFile;

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
		multiNacitac.setRootDirs(prenacti, kesoidModel.getUmisteniSouboru().getKesDir().getEffectiveFileIfActive(), kesoidModel.getUmisteniSouboru().getGeogetDataDir().getEffectiveFileIfActive(),
		        kesoidModel.getUmisteniSouboru().getGsakDataDir().getEffectiveFileIfActive());
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

	public List<KeFile> gsakSoubory(final Filex aDataDir) {
		return multiNacitac.gsakSoubory(aDataDir);
	}

}
