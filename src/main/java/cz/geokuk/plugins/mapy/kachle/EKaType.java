package cz.geokuk.plugins.mapy.kachle;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public enum EKaType {

  
  BASE_M(true, false, 0, 16, 16, "base-m", "Základní", "Základní mapa se silnicemi.", KeyEvent.VK_Z,  KeyStroke.getKeyStroke('z')),
  TURIST_M(true, false, 0, 16, 16, "turist-m", "Turistická", "Turistická mapa.", KeyEvent.VK_T, KeyStroke.getKeyStroke('t')),
  OPHOTO_M(true, true, 0, 18, 16, "ophoto-m", "Letecká", "Letecká ortho foto mapa", KeyEvent.VK_L, KeyStroke.getKeyStroke('f')),
  WTURIST_WINTER_M(true, false, 0, 16, 16, "wturist_winter-m", "Turistická zimní", "Zimní turistická mapa.", KeyEvent.VK_M, KeyStroke.getKeyStroke('w')),
  TURIST_AQUATIC_M(true, false, 0, 16, 16, "wturist_aquatic-m", "Letní", "Letní turistická mapa s koupalištěmi", KeyEvent.VK_F, KeyStroke.getKeyStroke('f') ),
  OPHOTO1012_M(true, true, 0, 18, 16, "ophoto1012-m", "Letecká 2012", "Starší fotomapa", 0, null),
  OPHOTO0406_M(true, true, 0, 18, 16, "ophoto0406-m", "Letecká 2006", "Starší fotomapa", KeyEvent.VK_6, null),
  OPHOTO0203_M(true, true, 0, 16, 16, "ophoto0203-m", "Letecká 2003", "Starší fotomapa", KeyEvent.VK_3, null),
  ZEMEPIS_M(true, false, 0, 16, 16, "zemepis-m", "Zeměpisná", "Zeměpisná mapa", KeyEvent.VK_G, KeyStroke.getKeyStroke('g')),
  ARMY2_M(true, true, 0, 13, 13, "army2-m", "Historická", "Historická mapa z let 1836-52", KeyEvent.VK_H, KeyStroke.getKeyStroke('h')),
  _BEZ_PODKLADU(true, true, 0, 18, 18, null, "žádná",  "Mapy bez podkladu.", 0, null),
 
  
  TTUR_M(false, "hybrid-tz-m", "Turistické trasy", "Turistické trasy, červená, modrá, zelená, žlutá.", KeyEvent.VK_U, KeyStroke.getKeyStroke('u')),
  TCYKLO_M(false, "hybrid-cyklo-m", "Cyklotrasy", "Cyklistické trasy fialově.", KeyEvent.VK_C, KeyStroke.getKeyStroke('c')),
  HYBRID_M(false, "hybrid-m", "Popisy", "Cesty a názvy měst na fotomapách.", KeyEvent.VK_P, KeyStroke.getKeyStroke('p')),
  RELIEF_M(false, "hybrid-border-m", "Reliéf", "Prostorové zobrazení mapy.", KeyEvent.VK_R, KeyStroke.getKeyStroke('r')),
  
  ;


  
//  super("Turistické trasy");
//  putValue(SHORT_DESCRIPTION, "Turistické trasy, červená, modrá, zelená, žlutá.");
//  putValue(MNEMONIC_KEY, KeyEvent.VK_U);
//  putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('u'));
  
  private final boolean podklad;
  private final int minMoumer;
  private final int maxMoumer;
  private final int maxAutoMoumer;
  private final String url_type;
  private final String nazev;
  private final String popis;
  private final int klavesa;
  private final KeyStroke keyStroke;

  public boolean isPodklad() {
    return podklad;
  }

  private EKaType(final boolean podklad, final String url_type,
      String nazev, String popis, int klavesa, KeyStroke keyStroke) {
    this.podklad = podklad;
    minMoumer = 3;
    maxMoumer = 18;
    maxAutoMoumer = 17;
    this.url_type = url_type;
    this.nazev = nazev;
    this.popis = popis;
    this.klavesa = klavesa;
    this.keyStroke = keyStroke;
  }

  private EKaType(final boolean podklad, final boolean jeMozneNavrsitTexty,
      final int minMoumer, final int maxMoumer, final int maxAutoMoumer, final String url_type,
      String nazev, String popis, int klavesa, KeyStroke keyStroke) {
    this.podklad = podklad;
    this.minMoumer = minMoumer;
    this.maxMoumer = maxMoumer;
    this.maxAutoMoumer = maxAutoMoumer;
    this.url_type = url_type;
    this.nazev = nazev;
    this.popis = popis;
    this.klavesa = klavesa;
    this.keyStroke = keyStroke;
    
  }

  public void addToUrl(final StringBuilder sb) {
    if (this == _BEZ_PODKLADU) {
      throw new RuntimeException("není možné downloadovat žádný podklad");
    }
    sb.append(url_type);
  }

  public int getMinMoumer() {
    return minMoumer;
  }

  public int getMaxMoumer() {
    return maxMoumer;
  }

  public int fitMoumer(int moumer) {
    if (moumer < minMoumer) {
      moumer = minMoumer;
    }
    if (moumer > maxMoumer) {
      moumer = maxMoumer;
    }
    return moumer;
  }

  public int getMaxAutoMoumer() {
    return maxAutoMoumer;
  }

  public String getNazev() {
    return nazev;
  }

  public String getPopis() {
    return popis;
  }

  public int getKlavesa() {
    return klavesa;
  }

  public KeyStroke getKeyStroke() {
    return keyStroke;
  }
}
