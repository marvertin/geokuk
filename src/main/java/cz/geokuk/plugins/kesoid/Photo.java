package cz.geokuk.plugins.kesoid;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

import javax.swing.*;
import java.io.File;

/**
 * Kesoid representing coordinates loaded from EXIF of a picture.
 */
public class Photo extends Kesoid {

  @Override
  public void buildGenotyp(Genom genom, Genotyp genotyp) {
    GenotypBuilderPhoto genotypBuilder = new GenotypBuilderPhoto(genom, genotyp);
    genotypBuilder.build(this);
  }

  @Override
  public EKesoidKind getKesoidKind() {
    return EKesoidKind.PHOTO;
  }

  @Override
  public File getSourceFile() {
    File file = new File(getIdentifier());
    if (file.exists()) {
      return file;
    } else {
      return null;
    }
  }

  @Override
  public void prispejDoTooltipu(StringBuilder sb, Wpt wpt) {
    sb.append("<b>").append(wpt.getName()).append("<b>");
  }

  @Override
  public Icon getUrlIcon() {
    return ImageLoader.seekResIcon("gccom.png");
  }
}
