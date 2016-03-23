package cz.geokuk.plugins.kesoid.mapicon;

import java.util.Objects;

import javax.swing.Timer;

import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidUmisteniSouboru;
import cz.geokuk.util.file.Filex;

public class IkonNacitacManager {

	private final IkonNacitacLoader ikonNacitacLoader;
	private IkonNacitacSwingWorker sw;
	private final KesoidModel kesoidModel;

	private Filex lastThirdParty;
	private Filex lastMyOwn;

	public IkonNacitacManager(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
		ikonNacitacLoader = new IkonNacitacLoader();

		new Timer(10000, e -> startLoad(false)).start();
	}

	public void startLoad(final boolean prenacti) {
		final KesoidUmisteniSouboru umisteniSouboru = kesoidModel.getUmisteniSouboru();
		final Filex thirdParty = umisteniSouboru.getImage3rdPartyDir();
		final Filex myDir = umisteniSouboru.getImageMyDir();

		if (prenacti || !Objects.equals(lastThirdParty, thirdParty) || !Objects.equals(lastMyOwn, myDir)) {
			lastMyOwn = myDir;
			lastThirdParty = thirdParty;
			ikonNacitacLoader.setImage3rdPartyDir(thirdParty.isActive() ? thirdParty.getEffectiveFile() : null);
			ikonNacitacLoader.setImageMyDir(myDir.isActive() ? myDir.getEffectiveFile() : null);
			if (sw == null || sw.isDone()) {
				sw = new IkonNacitacSwingWorker(ikonNacitacLoader, prenacti, kesoidModel);
				sw.execute();
			}
		}
	}

}
