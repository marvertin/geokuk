/**
 * 
 */
package cz.geokuk.plugins.vylety.akce;


import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

/**
 * Jde na vybranou pozici
 * @author veverka
 *
 */
public class BezNaBodVzadAction extends VyletAction0 {

  private static final long serialVersionUID = -2882817111560336824L;

  //  private Pozice pozice;
  /**
   * @param aBoard
   */
  public BezNaBodVzadAction() {
    putValue(NAME, "Na další bod vzad");
    putValue(SHORT_DESCRIPTION, "Přesune mapu o jeden bod vzad na vybrané cestě. Pokud není vybraný žádný bod, přesune na začátek cesty, pokud neí vybraná žádná cesta, vybere nejbližší cestu a přesunena její konec.");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt LEFT"));
  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent aE) {
    //poziceModel.setPozice(pozice);
    vyletModel.bezNaBodVzad();
    vyrezModel.vystredovatNaPozici();

    //  	Board.eveman.fire(new PoziceChangedEvent(pozice, true));
  }

  @Override
  protected void vyletChanged() {
    setEnabled(! curdoc().isEmpty());
    super.vyletChanged();
  }

  //  public void onEvent(PoziceChangedEvent event) {
  //    setEnabled(! event.poziceq.isNoPosition());
  //  }


}
