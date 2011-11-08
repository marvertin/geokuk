package cz.geokuk.plugins.kesoid;


import java.util.HashSet;
import java.util.Set;

import cz.geokuk.plugins.kesoid.mapicon.Alela;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.vylety.EVylet;
import cz.geokuk.plugins.vylety.IgnoreListChangedEvent;
import cz.geokuk.plugins.vylety.VyletChangedEvent;
import cz.geokuk.plugins.vylety.VyletModel;
import cz.geokuk.util.lang.FUtil;



public class KesFilter  {

  private FilterDefinition filterDefinition;
  private Set<Alela> nechteneAlely;
  private Set<String> jmenaNechtenychAlel;

  private VyletModel vyletModel;
  private KesoidModel kesoidModel;

  private Set<Wpt> jenTytoVyletoveWaypointyZobrazit;

  public KesFilter() {
  }

  public void onEvent(IgnoreListChangedEvent event) {
    if (filterDefinition.getPrahVyletu() != EVylet.VSECHNY) {
      kesoidModel.spustFiltrovani();
    }

  }

  public void onEvent(VyletChangedEvent aEvent) {
    EVylet evylPrah = filterDefinition.getPrahVyletu();

    if (evylPrah == EVylet.JEN_V_CESTE) {
      Set<Wpt> wpts = new HashSet<Wpt>();
      FUtil.addAll(wpts, aEvent.getDoc().getWpts());
      if (! wpts.equals(jenTytoVyletoveWaypointyZobrazit)) {
        jenTytoVyletoveWaypointyZobrazit = wpts;
        kesoidModel.spustFiltrovani();
      }

    }
    else {
      jenTytoVyletoveWaypointyZobrazit = null;
    }
  }



  /**
   * @return the jmenaNechtenychAlel
   */
  public Set<String> getJmenaNechtenychAlel() {
    return jmenaNechtenychAlel;
  }

  /**
   * @param jmenaNechtenychAlel the jmenaNechtenychAlel to set
   */
  public void setJmenaNechtenychAlel(Set<String> jmenaNechtenychAlel) {
    this.jmenaNechtenychAlel = jmenaNechtenychAlel;
    nechteneAlely = null;
  }

  public boolean isFiltered(Wpt aWpt, Genom genom, Genotyp genotyp) {
    try {
      if (genotyp == null) { // to je zde jen z důvodu optimalizace
        genotyp = aWpt.getGenotyp(genom);
      }
      Set<Alela> alelygenotypu = genotyp.getAlely();
      if (jmenaNechtenychAlel != null) {
        if (nechteneAlely == null) {
          nechteneAlely = genom.namesToAlelyIgnorujNeexistujici(jmenaNechtenychAlel);
        }
        Set<Alela> alely = new HashSet<Alela>(nechteneAlely);
        alely.retainAll(alelygenotypu);
        if (alely.size() > 0) return false;
      }

      //    if (aWpt.getType() != AWptType.CACHE && ! wptTypes.contains(aWpt.getType())) return false;

      Kesoid kesoid = aWpt.getKesoid();

      if (filterDefinition.isJenFinalUNalezenych()) {
        if (kesoid.getVztah() == EKesVztah.FOUND || kesoid.getVztah() == EKesVztah.OWN) {
          if (aWpt != kesoid.getMainWpt()) return false;
        }
      }

      if (kesoid instanceof Kes) {
        Kes kes = (Kes) kesoid;

        if (kes.getVztah() == EKesVztah.NORMAL) {  // jen u nenalezených
          if (kes.getFinal() != null && filterDefinition.isJenDoTerenuUNenalezenych() &&
              (!aWpt.nutnyKLusteni()) && ! Wpt.TRADITIONAL_CACHE.equals(kes.getFirstWpt().getSym())) return false;
        }

        if (kes.getHodnoceni() != Kes.NENI_HODNOCENI) {
          if (kes.getHodnoceni() <  filterDefinition.getPrahHodnoceni()) return false;
        }
        if (kes.getBestOf() != Kes.NENI_HODNOCENI) {
          if (kes.getBestOf() < filterDefinition.getPrahBestOf()) return false;
        }
        if (kes.getFavorit() != Kes.NENI_HODNOCENI) {
          if (kes.getFavorit() < filterDefinition.getPrahFavorit()) return false;
        }
      }
      if (! zaraditDlePrahuVyletu(aWpt)) return false;
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Filtrovani waypointu: " + aWpt, e);

    }

  }
  //
  //  Board.kesfilter.jenFinal.setSelected(true);
  //  Board.kesfilter.jenJedenUNalezenych.setSelected(true);
  //  Board.kesfilter.smailikNaFinalce.setSelected(true);

  private boolean zaraditDlePrahuVyletu(Wpt aWpt) {
    if (vyletModel != null) {
      EVylet evylPrah = filterDefinition.getPrahVyletu();
      switch (evylPrah) {
      case VSECHNY: return true;
      case BEZ_IGNOROVANYCH: return ! vyletModel.isOnIgnoreList(aWpt);
      case JEN_V_CESTE: return vyletModel.isOnVylet(aWpt);
      default: return true;
      }
    } else
      return true;
  }


  public void setDefaults() {
    //Atom.of(AWptType.FINAL_LOCATION);
    filterDefinition = new FilterDefinition();
  }

  /**
   * 
   */
  public void init() {
    nechteneAlely = null;
  }

  /**
   * 
   */
  public void done() {
    nechteneAlely = null;
  }

  /**
   * @return the filterDefinition
   */
  public FilterDefinition getFilterDefinition() {
    return filterDefinition;
  }

  /**
   * @param filterDefinition the filterDefinition to set
   */
  public void setFilterDefinition(FilterDefinition filterDefinition) {
    this.filterDefinition = filterDefinition;
  }



  public void inject(VyletModel vyletModel) {
    this.vyletModel = vyletModel;
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }


}
