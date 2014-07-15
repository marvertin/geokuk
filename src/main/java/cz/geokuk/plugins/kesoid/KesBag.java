package cz.geokuk.plugins.kesoid;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.importek.InformaceOZdrojich;
import cz.geokuk.plugins.kesoid.mapicon.Alela;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genom.CitacAlel;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;
import cz.geokuk.util.lang.CounterMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class KesBag {
    private static final Logger log =
            LogManager.getLogger(KesBag.class.getSimpleName());

  private final List<Wpt> wpts = new ArrayList<>();
  private Set<Kesoid> kesoidyset;
  private List<Kesoid> kesoidy;

  private CounterMap<Alela> poctyAlel;


  private final Indexator<Wpt> indexator;

  private int maximalniBestOf = 0;
  private int maximalniHodnoceni;
  private int maximalniFavorit;
  private final Genom genom;

  private final CitacAlel citacAlel;

  private InformaceOZdrojich iInformaceOZdrojich;

  private boolean locked;

  ////////////////////////////////////////
  public KesBag(final Genom genom) {
    this.genom = genom;
    indexator =new Indexator<Wpt>(BoundingRect.ALL);
    kesoidyset = new HashSet<Kesoid>();
    citacAlel = genom.createCitacAlel();
  }

  public void add(Wpt wpt, Genotyp genotyp) {
    if (locked)
      throw new RuntimeException("Nelze uz vkladat waypointy");
    if (genotyp == null) { // to je zde jen z důvodu optimalizace
      genotyp = wpt.getGenotyp(genom);
    }
    Mou mou = wpt.getMou();
    if (mou.xx < 0 || mou.yy < 0) {
      //// TODO [veverka] Řešit rozsah [25.11.2009 9:45:59; veverka]
      log.error("Nelze přidat takový waypoint: " + mou + " / " + mou.xx + ":" + mou.yy + " / " + wpt + " --- " +
              wpt.getKesoid());
      return;
    }
    indexator.vloz(mou.xx, mou.yy, wpt);
    Kesoid kesoid = wpt.getKesoid();
    kesoidyset.add(kesoid);
    wpts.add(wpt);
    if (kesoid instanceof Kes) {
      Kes kes = (Kes) kesoid;
      maximalniBestOf = Math.max(maximalniBestOf, kes.getBestOf());
      maximalniHodnoceni = Math.max(maximalniHodnoceni, kes.getHodnoceni());
      maximalniFavorit = Math.max(maximalniFavorit, kes.getFavorit());
    }
    for (Alela alela : genotyp.getAlely()) {
      assert alela != null;
      citacAlel.add(alela);
    }
  }

  public void done() {
    kesoidy = new ArrayList<Kesoid>(kesoidyset.size());
    kesoidy.addAll(kesoidyset);
    kesoidyset = null;
    poctyAlel = citacAlel.getCounterMap();
    //System.out.println(poctyAlel);
  }


  /**
   * @return the genom
   */
  public Genom getGenom() {
    return genom;
  }

  public Indexator<Wpt> getIndexator() {
    return indexator;
  }

  /**
   * @return the maximalniBestOf
   */
  public int getMaximalniBestOf() {
    return maximalniBestOf;
  }

  public int getMaximalniHodnoceni() {
    return maximalniHodnoceni;
  }

  public int getMaximalniFavorit() {
    return maximalniFavorit;
  }



  public List<Wpt> getWpts() {
    if (kesoidy == null)
      throw new RuntimeException("Jeste neni kesBag vytvoren");
    return wpts;
  }

  public List<Kesoid> getKesoidy() {
    if (kesoidy == null)
      throw new RuntimeException("Jeste neni kesBag vytvoren");
    return kesoidy;
  }

  /**
   * @return the poctyAlel
   */
  public CounterMap<Alela> getPoctyAlel() {
    return poctyAlel;
  }

  public Set<Alela> getPouziteAlely() {
    // TODO optimalizovat
      return poctyAlel.getMap().keySet();
  }

  /**
   * @param informaceOZdrojich the informaceOZdrojich to set
   */
  public void setInformaceOZdrojich(InformaceOZdrojich informaceOZdrojich) {
    iInformaceOZdrojich = informaceOZdrojich;
  }

  /**
   * @return the informaceOZdrojich
   */
  public InformaceOZdrojich getInformaceOZdrojich() {
    return iInformaceOZdrojich;
  }


}
