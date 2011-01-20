/**
 * 
 */
package cz.geokuk.plugins.kesoid.mvc;


import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coord.Pozice;
import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.framework.Action0;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Kesoid;



/**
 * @author veverka
 *
 */
public class UrlToListingForGeogetAction extends Action0 {

  private static final long serialVersionUID = -8054017274338240706L;
  private final Kesoid iKes;
  private Pozice pozice;
  private KesoidModel kesoidModel;

  /**
   * 
   */
  public UrlToListingForGeogetAction(Kesoid kes) {
    super("Listing do Geogetu", ImageLoader.seekResIcon("x16/geoget.png"));
    iKes = kes;
    putValue(SHORT_DESCRIPTION, "Do systémového clipboardu vloží URL tiskové podoby listingu keše, na nějž se chytne spuštěný GEOGET a otevře listing (a možná i GSAK).");
    //putValue(MNEMONIC_KEY, InputEvent.)
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
  }
  
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */

  @Override
  public void actionPerformed(ActionEvent e) {
    Kesoid kes = iKes;
    if (kes == null) {
      if (pozice.getWpt() == null) return;
      kes = pozice.getWpt().getKesoid();
    }
    kesoidModel.otevriListingVGeogetu(kes);
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }
  
  public void onEvent(PoziceChangedEvent event) {
    pozice = event.pozice;
    setEnabled(iKes != null || pozice.getWpt() != null);
  }
}



