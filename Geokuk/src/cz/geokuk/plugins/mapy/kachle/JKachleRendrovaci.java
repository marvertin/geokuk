package cz.geokuk.plugins.mapy.kachle;

import java.awt.Graphics;
import java.awt.Image;
import java.util.EnumSet;

public class JKachleRendrovaci extends JKachle {

  /**
   * Celá tato třída je jen kvůli zmenšení paměti při rendrování na polovinu,
   * jinak by nebyla potřeba.
   */
  private static final long serialVersionUID = -4855904714968272822L;
  private Graphics grf;

  public JKachleRendrovaci(KaAll plny, KachleModel kachleModel,
      boolean vykreslovatOkamzite, JKachlovnik jKachlovnik) {
    super(plny, kachleModel, vykreslovatOkamzite, jKachlovnik);
  }


  @Override
  protected synchronized void setImageImmadiately(EnumSet<EKaType> types, Image img2, boolean aJeToUzCelyObrazek) {
    if (aJeToUzCelyObrazek) {
      super.setImageImmadiately(types, img2, aJeToUzCelyObrazek);
      if (grf != null) {
        //System.out.println("Paintuji odloženě");
        super.paintComponent(grf);
        uzTeNepotrebuju();
      }
    }
  }
  
  @Override
  protected synchronized void paintComponent(Graphics aG) {
    if (jeTamUzCelyObrazek) {
      //System.out.println("Paintuji normálně");
      super.paintComponent(aG);
      uzTeNepotrebuju();
      grf = null;
    } else {
      //System.out.println("Odkládám paintování");
      grf = aG.create();
    }
  }
}
