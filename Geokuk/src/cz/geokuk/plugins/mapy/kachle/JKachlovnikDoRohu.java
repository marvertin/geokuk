package cz.geokuk.plugins.mapy.kachle;


import java.util.EnumSet;

public class JKachlovnikDoRohu extends JKachlovnik {

  private static final long serialVersionUID = -7897332661428146095L;

  @Override
  public void setKachloTypes(KaSet aKachloSet) {
    super.setKachloTypes(new KaSet(EnumSet.of(EKaType.OPHOTO)));
  }

}
