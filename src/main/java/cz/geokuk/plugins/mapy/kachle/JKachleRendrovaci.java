package cz.geokuk.plugins.mapy.kachle;

import java.awt.Graphics;

public class JKachleRendrovaci extends JKachle {

  /**
   * Celá tato třída je jen kvůli zmenšení paměti při rendrování na polovinu,
   * jinak by nebyla potřeba.
   */
  private static final long serialVersionUID = -4855904714968272822L;

  public JKachleRendrovaci(JKachlovnik jKachlovnik) {
    super(jKachlovnik);
    
    
  }

  
  @Override
  protected synchronized void paintComponent(Graphics aG) {
    if (getKachle().jeTamUzCelyObrazek) {
      //System.out.println("Paintuji normálně");
      super.paintComponent(aG);
      getKachle().uzTeNepotrebuju();
      ((KachleRendrovaci)getKachle()).grf = null;
    } else {
      //System.out.println("Odkládám paintování");
      ((KachleRendrovaci)getKachle()).grf = aG.create();
    }
  }
}
