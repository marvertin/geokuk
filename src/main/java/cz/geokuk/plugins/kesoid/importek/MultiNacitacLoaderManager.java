package cz.geokuk.plugins.kesoid.importek;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

public class MultiNacitacLoaderManager {

	private final MultiNacitac		multiNacitac;

	private MultiNacitacSwingWorker	klsw;

	private Timer					iTimer;

	private final KesoidModel		kesoidModel;

	public void startLoad(boolean prenacti, Genom genom) {
		if (iTimer != null)
			iTimer.stop();
		multiNacitac.setRootDirs(prenacti, kesoidModel.getUmisteniSouboru().getKesDir().getEffectiveFileIfActive(), kesoidModel.getUmisteniSouboru().getGeogetDataDir().getEffectiveFileIfActive());
		if (klsw == null || klsw.isDone()) {
			klsw = new MultiNacitacSwingWorker(multiNacitac, genom, kesoidModel);
			klsw.execute();
		}
		startTimer(genom);
	}

	public MultiNacitacLoaderManager(KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
		multiNacitac = new MultiNacitac(kesoidModel);
	}

	private void startTimer(final Genom genom) {
		iTimer = new Timer(10000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startLoad(false, genom);
			}
		});
		iTimer.start();

	}

}
