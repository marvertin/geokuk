package cz.geokuk.plugins.mapy.kachle;

import java.awt.Graphics;
import java.awt.Image;

public class JKachleRendrovaci extends JKachle {

  /**
   * Celá tato třída je jen kvůli zmenšení paměti při rendrování na polovinu,
   * jinak by nebyla potřeba.
   */
  private static final long serialVersionUID = -4855904714968272822L;
  private Graphics grf;

  public JKachleRendrovaci(final JKachlovnik jKachlovnik, final KaAll kaall) {
    super(jKachlovnik, kaall);


  }


  @Override
  protected void ziskanPlnyObrazek(final Image img) {
    if (grf != null) {
      super.paintComponent(grf);
      uzTeNepotrebuju();
    }
  }

  @Override
  protected synchronized void paintComponent(final Graphics aG) {
    super.paintComponent(aG);

    if (jeTamUzCelyObrazek()) {
      super.paintComponent(aG);
      uzTeNepotrebuju();
      grf = null;
    } else {
      grf = aG.create();
    }
  }
}
