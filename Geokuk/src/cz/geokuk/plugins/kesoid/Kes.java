/**
 * 
 */
package cz.geokuk.plugins.kesoid;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;


/**
 * @author veverka
 *
 */
public class Kes extends Kesoid {

  private int bestOf = Kes.NENI_HODNOCENI;
  private int hodnoceni = Kes.NENI_HODNOCENI;
  private int hodnoceniPocet = Kes.NENI_HODNOCENI;
  private int znamka = Kes.NENI_HODNOCENI;
  private int favorit = Kes.NENI_HODNOCENI;

  private String hint;
  private EKesSize size;
  private String difficulty;
  private String terrain;
  //private String guid;
  private Wpt finalWpt;
  private static String URL_PREFIX_PRINT = "http://www.geocaching.com/seek/cdpf.aspx?guid=";
  private static String URL_PREFIX_SHOW = "http://www.geocaching.com/seek/cache_details.aspx?guid=";
  public static final int NENI_HODNOCENI = -1;

  private String iFountTime;

  private Wpt mainWpt;
  /**
   * @return the bestOf
   */
  public int getBestOf() {
    return bestOf;
  }

  /**
   * @return the mainWpt
   */
  @Override
  public Wpt getMainWpt() {
    if (mainWpt != null) return mainWpt;
    return super.getMainWpt();
  }

  /**
   * @param aMainWpt the mainWpt to set
   */
  public void setMainWpt(Wpt aMainWpt) {
    mainWpt = aMainWpt;
  }

  /**
   * @param aBestOf the bestOf to set
   */
  public void setBestOf(int aBestOf) {
    bestOf = aBestOf;
  }

  /**
   * @return the hodnoceni
   */
  public int getHodnoceni() {
    return hodnoceni;
  }

  /**
   * @param aHodnoceni the hodnoceni to set
   */
  public void setHodnoceni(int aHodnoceni) {
    hodnoceni = aHodnoceni;
  }

  /**
   * @return the hodnoceniPocet
   */
  public int getHodnoceniPocet() {
    return hodnoceniPocet;
  }

  /**
   * @param aHodnoceniPocet the hodnoceniPocet to set
   */
  public void setHodnoceniPocet(int aHodnoceniPocet) {
    hodnoceniPocet = aHodnoceniPocet;
  }

  /**
   * @return the znamka
   */
  public int getZnamka() {
    return znamka;
  }

  /**
   * @param aZnamka the znamka to set
   */
  public void setZnamka(int aZnamka) {
    znamka = aZnamka;
  }

  /**
   * @return the hint
   */
  public String getHint() {
    return hint;
  }

  /**
   * @param aHint the hint to set
   */
  public void setHint(String aHint) {
    hint = aHint;
  }

  //  public Wpt getGc() {
  //    return gc;
  //  }

  public EKesSize getSize() {
    return size;
  }

  public void setSize(EKesSize size) {
    this.size = size;
  }

  public String getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(String difficulty) {
    this.difficulty = difficulty;
  }

  public String getTerrain() {
    return terrain;
  }

  public void setTerrain(String terrain) {
    this.terrain = terrain;
  }


  /**
   * @return
   */
  public Wpt getFinal() {
    return finalWpt;
  }




  @Override
  public void buildGenotyp(Genom genom, Genotyp g) {
    GenotypBuilderKes genotypBuilder = new GenotypBuilderKes(genom, g);
    genotypBuilder.build(this);
  }

  @Override
  public String[] getProhledavanci() {
    String[] prohledavanci = new String[] {getNazev(), getCode(), getAuthor()};
    return prohledavanci;
  }

  @Override
  public void addWpt(Wpt wpt) {
    super.addWpt(wpt);
    if (wpt == null) return;
    if (EKesWptType.FINAL_LOCATION == wpt.getType()) {
      finalWpt = wpt;
    }
  }

  /**
   * @param aFound
   */
  public void setFoundTime(String fountTime) {
    iFountTime = fountTime;
  }

  /**
   * @return the fountTime
   */
  public String getFountTime() {
    return iFountTime;
  }

  @Override
  public EKesoidKind getKesoidKind() {
    return EKesoidKind.KES;
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
    sb.append("  (" + getCode() + ")");
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
      sb.append("  (" + wpt.getName() + ")");
      sb.append("</small>");
    }
    //}
    //}

  }

  @Override
  public URL getUrlPrint() {
    try {
      String urls = getUrl();
      if (urls.startsWith(URL_PREFIX_SHOW)) {
        URL url = new URL(URL_PREFIX_PRINT + urls.substring(URL_PREFIX_SHOW.length()));
        return url;
      } else
        return null;
    } catch (MalformedURLException e) {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see cz.geokuk.kes.Kesoid#getUrlIcon()
   */
  @Override
  public Icon getUrlIcon() {
    return ImageLoader.seekResIcon("gccom.png");
  }

  /**
   * @return
   */
  public char getOneLetterType() {
    String sym = getMainWpt().getSym();
    if (sym == null || sym.length() == 0) return '?';
    return sym.charAt(0);
  }

  /**
   * @return
   */
  public char getOneLetterSize() {
    return getSize().getOneLetterSize();
  }

  /**
   * @return
   */
  public char getOneLetterDifficulty() {
    return naJednoPismeno(getDifficulty());
  }

  /**
   * @return
   */
  public char getOneLetterTerrain() {
    return naJednoPismeno(getTerrain());
  }

  private char naJednoPismeno(String s) {
    char c = s.charAt(0);
    if (s.length() == 1) return c;
    else return (char) (c - '1' + 'A');
  }

  /**
   * Vrátí takové to čtyřznakové info TR3A
   * @return
   */
  public String getInfo() {
    StringBuilder sb = new StringBuilder();
    sb.append(getOneLetterType());
    sb.append(getOneLetterSize());
    sb.append(getOneLetterDifficulty());
    sb.append(getOneLetterTerrain());
    return sb.toString();
  }

  public int getFavorit() {
    return favorit;
  }

  public void setFavorit(int favorit) {
    this.favorit = favorit;
  }


}
