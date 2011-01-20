package cz.geokuk.plugins.mapy;

import java.util.EnumSet;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.kachle.EKaType;
import cz.geokuk.plugins.mapy.kachle.KaSet;

public class MapyModel extends Model0 {

  
  private EMapPodklad podklad;
  
  public EMapPodklad getPodklad() {
    return podklad;
  }

  public EnumSet<EMapDekorace> getDekorace() {
    return dekorace.clone();
  }

  private EnumSet<EMapDekorace> dekorace;

  public void setPodklad(EMapPodklad podklad) {
    if (podklad == this.podklad) return;
    this.podklad = podklad;
    currPrefe().node(FPref.NODE_KTERE_MAPY_node).putEnum(FPref.VALUE_MAPOVE_PODKLADY_value, podklad);
    fajruj();
  }
  
  public void setDekorace(EnumSet<EMapDekorace> dekorace) {
    if (dekorace.equals(this.dekorace)) return;
    this.dekorace = dekorace.clone(); 
    currPrefe().node(FPref.NODE_KTERE_MAPY_node).putEnumSet(FPref.VALUE_MAPOVE_DEKORACE_value, dekorace);
    fajruj();
  }

  public KaSet getKaSet() {
    EnumSet<EKaType> kts = EKaType.compute(podklad, dekorace);
    assert kts != null;
    KaSet kaSet = new KaSet(kts);
    return kaSet;
  }
  
  private void fajruj() {
    if (podklad != null && dekorace != null) {
      fire(new ZmenaMapNastalaEvent(getKaSet()));
    }
  }

  @Override
  protected void initAndFire() {
    EMapPodklad podklad = currPrefe().node(FPref.NODE_KTERE_MAPY_node).getEnum(FPref.VALUE_MAPOVE_PODKLADY_value, EMapPodklad.TURIST, EMapPodklad.class);
    EnumSet<EMapDekorace> dekorace = currPrefe().node(FPref.NODE_KTERE_MAPY_node).getEnumSet(FPref.VALUE_MAPOVE_DEKORACE_value, EnumSet.of(EMapDekorace.TTUR), EMapDekorace.class);
    setPodklad(podklad);
    setDekorace(dekorace);
  }
  
  
  
  
  
}
