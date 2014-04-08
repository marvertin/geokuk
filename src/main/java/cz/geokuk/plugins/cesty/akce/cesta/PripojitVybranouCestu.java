/**
 * 
 */
package cz.geokuk.plugins.cesty.akce.cesta;


import java.awt.Color;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.cesty.data.Cesta;

/**
 * Jde na vybranou pozici
 * @author veverka
 *
 */
public class PripojitVybranouCestu extends CestaAction0 {

  private static final long serialVersionUID = -2882817111560336824L;

  //  private Pozice pozice;
  /**
   * @param aBoard
   */
  public PripojitVybranouCestu(Cesta cesta) {
    super(cesta);
    putValue(NAME, "Připojit vybranou cestu");
    putValue(SHORT_DESCRIPTION, "Za tuto cestu připojí vybranou cestu spojením koncového bodu této cesty a počátečního bodu vybrané cesty.");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
  }

  @Override
  protected boolean mamPovolitProCestu(Cesta cesta) {
    return curta() != null && curta() != cesta;
  }

  @Override
  protected void nastavJmenoAkce(Cesta cesta, boolean aZKontextovehoMenu) {
    putValue(NAME, "<html>Připojit k" + cesta.getNazevADalkaHtml() + " vybranou cestu" +
        curta().getNazevADalkaHtml() + " usekem " + Cesta.dalkaHtml(Mou.dalka(cesta.getCil(), curta().getStart()), Color.BLACK));
  }

  @Override
  protected void provedProCestu(Cesta cesta) {
    cesta.kontrolaKonzistence();
    curta().kontrolaKonzistence();
    cestyModel.pripojitCestuZa(cesta, curta());
    cesta.kontrolaKonzistence();
    curta().kontrolaKonzistence();
  }

}
