package cz.geokuk.plugins.mapy.kachle;

import java.awt.Image;
import java.awt.image.BufferedImage;

class DlazebniPosilac {

  private DlazebniKombiner kombiner;
  private final ImageReceiver imgrcv;


  public DlazebniPosilac(DlazebniKombiner kombiner, ImageReceiver imgrcv) {
    super();
    this.kombiner = kombiner;
    this.imgrcv = imgrcv;
  }


  public void add(EKaType type, Image img) {
    if (img == null || kombiner == null) {
        return;
    }
    kombiner.add(type, img);
    BufferedImage kombinedImage = kombiner.getKombinedImage();
    boolean hotovo = kombiner.isHotovo();
    if (kombinedImage != null) {
      // Jen pokud už se něco podařilo nakombinovat budu někam posílat.
      // NULL je zde tehdy, když přišly nejdříve dekorace jako turistické trasy či stíny, ale ještě není podklad
      imgrcv.setImage(kombiner.getCoMam(), kombinedImage, hotovo);
    }
    if (hotovo) {
      this.kombiner.uzTeNepotrebuju();
      this.kombiner = null; // uvolnit paměť
    }
  }

}
