package cz.geokuk.plugins.mapy.kachle;

import java.awt.Image;
import java.util.EnumSet;

public class KachloStav {
  public final EnumSet<EKaType> types;
  public final Image img2;
  public final boolean jeToUzCelyObrazek;

  public KachloStav(EnumSet<EKaType> types, Image img2, boolean aJeToUzCelyObrazek) {
    this.types = types;
    this.img2 = img2;
    this.jeToUzCelyObrazek = aJeToUzCelyObrazek;
  }
}