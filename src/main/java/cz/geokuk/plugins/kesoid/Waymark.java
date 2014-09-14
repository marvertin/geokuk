package cz.geokuk.plugins.kesoid;

import javax.swing.Icon;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

public class Waymark extends Kesoid {

  @Override
  public void buildGenotyp(Genom genom, Genotyp g) {
    GenotypBuilderWaymark genotypBuilder = new GenotypBuilderWaymark(genom, g);
    genotypBuilder.build(this);
  }

  @Override
  public EKesoidKind getKesoidKind() {
    return EKesoidKind.WAYMARK;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.kes.Kesoid#prispejDoTooltipu(java.lang.StringBuilder)
   */
  @Override
  public void prispejDoTooltipu(StringBuilder sb, Wpt wpt) {
    sb.append("<b>");
    sb.append(getNazev());
    sb.append("</b>");
    sb.append("<small>");
    sb.append(" - ");
    sb.append(getFirstWpt().getSym());
    sb.append("  (").append(getCode()).append(")");
    sb.append("</small>");
    sb.append("<br>");
    if (wpt != getFirstWpt()) {
      if (! getNazev().contains(wpt.getNazev())) {
        sb.append(wpt.isRucnePridany() ? "+ " : "");
        sb.append("<i>");
        sb.append(wpt.getName().substring(0, 2));
        sb.append(": ");
        sb.append(wpt.getNazev());
        sb.append("</i>");
      }
      //if (! getSym().equals(wpt.getSym())) {
      sb.append("<small>");
      sb.append(" - ");
      sb.append(wpt.getSym());
      sb.append("  (").append(wpt.getName()).append(")");
      sb.append("</small>");
    }
  }

  @Override
  public Icon getUrlIcon() {
    return ImageLoader.seekResIcon("waymarking.png");
  }

}
