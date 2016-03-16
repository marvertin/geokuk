package cz.geokuk.plugins.mapy.kachle;

import java.awt.Graphics;

public class KachleRendrovaci extends Kachle {

  /**
   * Celá tato třída je jen kvůli zmenšení paměti při rendrování na polovinu,
   * jinak by nebyla potřeba.
   */
  Graphics grf;

  @Override
  protected synchronized void setImageImmadiately(KachloStav kachloStav) {
    if (kachloStav.jeToUzCelyObrazek) {
      super.setImageImmadiately(kachloStav);
      if (grf != null) {
        //System.out.println("Paintuji odloženě");
        super.getJkachle().paintComponent(grf);
        uzTeNepotrebuju();
      }
    }
  }



  KachleRendrovaci(KaAll aPlny, KachleModel aKachleModel, boolean aVykreslovatOkamzite, JKachle aJkachle) {
    super(aPlny, aKachleModel, aVykreslovatOkamzite, aJkachle);
  }
  
}
