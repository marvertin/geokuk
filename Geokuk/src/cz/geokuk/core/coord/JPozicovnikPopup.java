/**
 * 
 */
package cz.geokuk.core.coord;


import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.Factory;
import cz.geokuk.plugins.geocoding.GeocodingBorowserXmlAction;

/**
 * @author veverka
 *
 */
public class JPozicovnikPopup extends JPopupMenu implements AfterInjectInit {

  private static final long serialVersionUID = 1586052649958330581L;
  private Factory factory;
  private final Wgs wgs;


  /**
   * @param aMysNadWpt
   * 
   */
  public JPozicovnikPopup(Wgs wgs) {
    this.wgs = wgs;
  }

  public void inject(Factory factory) {
    this.factory = factory;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.program.AfterInjectInit#initAfterInject()
   */
  @Override
  public void initAfterInject() {
    //  add(new ZoomKesAction(kesoid));
    if (wgs != null) {
      JMenuItem item = new JMenuItem(factory.init(new CenterPoziceAction()));
      //item.setText("Centruj");
      //TODO Dát ikonu středování
      //item.setIcon(null);

      add(item);
    }

    add(new JMenuItem(factory.init(new ZoomPoziceAction(wgs))));
    add(new JMenuItem(factory.init(new OdstranKrizAction())));
    add(new JMenuItem(factory.init(new GeocodingBorowserXmlAction(wgs))));
    //    addSeparator();

  }


}
