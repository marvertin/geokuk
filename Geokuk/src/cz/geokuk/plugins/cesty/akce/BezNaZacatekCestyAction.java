/**
 * 
 */
package cz.geokuk.plugins.cesty.akce;


import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;


/**
 * Jde na vybranou pozici
 * @author veverka
 *
 */
public class BezNaZacatekCestyAction extends VyletAction0 {

  private static final long serialVersionUID = -2882817111560336824L;

  //  private Pozice pozice;
  /**
   * @param aBoard
   */
  public BezNaZacatekCestyAction() {
    putValue(NAME, "Na začátek cesty");
    putValue(SHORT_DESCRIPTION, "Přesune mapu na začátek vybrané cesty. Pokud žádná cesta není vybraná, vybere nejbližší cestu.");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("HOME"));
  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent aE) {
    //poziceModel.setPozice(pozice);
    cestyModel.bezNaZacatekCesty(curta());
    vyrezModel.vystredovatNaPozici();

    //  	Board.eveman.fire(new PoziceChangedEvent(pozice, true));
  }

  @Override
  protected void vyletChanged() {
    super.vyletChanged();
    setEnabled(! curdoc().isEmpty());
  }

  //  public void onEvent(PoziceChangedEvent event) {
  //    setEnabled(! event.poziceq.isNoPosition());
  //  }


}
