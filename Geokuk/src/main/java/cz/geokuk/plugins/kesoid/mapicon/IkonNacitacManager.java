package cz.geokuk.plugins.kesoid.mapicon;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidUmisteniSouboru;

public class IkonNacitacManager {


  private final IkonNacitacLoader ikonNacitacLoader;

  private IkonNacitacSwingWorker sw;

  private final KesoidModel kesoidModel;

  public void startLoad( boolean prenacti) {
    KesoidUmisteniSouboru umisteniSouboru = kesoidModel.getUmisteniSouboru();
    ikonNacitacLoader.setImage3rdPartyDir(umisteniSouboru.getImage3rdPartyDir().isActive() ?
        umisteniSouboru.getImage3rdPartyDir().getEffectiveFile() : null);
    ikonNacitacLoader.setImageMyDir(umisteniSouboru.getImageMyDir().isActive() ?
        umisteniSouboru.getImageMyDir().getEffectiveFile() : null);
    if (sw == null || sw.isDone()) {
      sw = new IkonNacitacSwingWorker(ikonNacitacLoader, prenacti, kesoidModel);
      sw.execute();
    }
  }

  public IkonNacitacManager(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
    ikonNacitacLoader = new IkonNacitacLoader();

    new Timer(10000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        startLoad(false);
      }
    }).start();
  }

}
