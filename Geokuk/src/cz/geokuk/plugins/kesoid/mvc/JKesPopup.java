/**
 * 
 */
package cz.geokuk.plugins.kesoid.mvc;


import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.Factory;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mapicon.Alela;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;
import cz.geokuk.plugins.vylety.VyletAnoAction;
import cz.geokuk.plugins.vylety.VyletNeAction;
import cz.geokuk.plugins.vylety.VyletNevimAction;

/**
 * @author veverka
 *
 */
public class JKesPopup extends JPopupMenu implements AfterEventReceiverRegistrationInit  {

  private static final long serialVersionUID = 1586052649958330581L;
  private final Wpt iMysNadWpt;
  private Factory factory;
  private KesBag vsechny;

  /**
   * @param aMysNadWpt 
   * 
   */
  public JKesPopup(Wpt aMysNadWpt) {
    iMysNadWpt = aMysNadWpt;
  }

  public void inject(Factory factory) {
    this.factory = factory;
  }

  public void onEvent(KeskyNactenyEvent event) {
    vsechny = event.getVsechny();
  }

  /* (non-Javadoc)
   * @see cz.geokuk.program.AfterInjectInit#initAfterInject()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    // Přidat zhasínače 
    JMenu zhasinace = new JMenu("Zhasni");
    add(zhasinace);
    Genotyp genotyp = iMysNadWpt.getGenotyp(vsechny.getGenom());
    for (Alela alela : genotyp.getAlely()) {
      if (alela.getGen().isVypsatelnyVeZhasinaci() &&  !alela.isVychozi()) {
        zhasinace.add(factory.init(new ZhasniKeseUrciteAlelyAction(alela)));
      }
    }
    
    ///
    Kesoid kesoid = iMysNadWpt.getKesoid();
    add(factory.init(new ZoomKesAction(kesoid)));
    JMenuItem item = new JMenuItem(factory.init(new CenterWaypointAction(iMysNadWpt)));
    item.setText("Centruj");
    //TODO Dát ikonu středování
    item.setIcon(null);
    add(item);
    if (kesoid.getUrlShow() != null) {
      add(factory.init(new ZobrazNaGcComAction(kesoid)));
    }
    if (kesoid.getUrlPrint() != null) {
      add(factory.init(new TiskniNaGcComAction(kesoid)));
    }
    if (kesoid.getUrlShow() != null) {
      add(factory.init(new UrlToClipboardForGeogetAction(kesoid)));
    }
    if (kesoid.getUrlPrint() != null) {
      add(factory.init(new UrlToListingForGeogetAction(kesoid)));
    }
    addSeparator();
    add(factory.init(new VyletAnoAction(kesoid)));
    add(factory.init(new VyletNevimAction(kesoid)));
    add(factory.init(new VyletNeAction(kesoid)));
    addSeparator();
    for (Wpt wpt : kesoid.getWpts()) {
      add(factory.init(new CenterWaypointAction(wpt)));
    }
  }

  
}
