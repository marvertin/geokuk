package cz.geokuk.plugins.cesty.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.plugins.cesty.FBarvy;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.util.lang.FUtil;

public class Cesta implements Iterable<Bousek0> {

  /// prazdna cesta ma prazdny start i cil, ale snad by ani nemala byt
  private Bod start;
  private Bod cil;
  private Doc doc;
  private String nazev;

  public static Cesta create() {
    return new Cesta();
  }

  Cesta() {
  }

  Bod createBod(Mouable mouable) {
    return new Bod(this, mouable);
  }

  Usek createUsek() {
    return new Usek(this);
  }


  /**
   * Zahájí novou cestu, zruší vše, co zde bylo.
   */
  Bod zacni(Mouable mouable) {
    start = createBod(mouable);
    cil = start;
    return start;
  }

  Bod pridejNaKonec(Mouable mouable) {
    if (isEmpty())
      return zacni(mouable);
    else {
      Bod bod = createBod(mouable);
      if (jsouMocBlizko(bod, cil)) return cil;
      Usek usek = createUsek();
      usek.spoj(cil, bod);
      cil = bod;
      return bod;
    }
  }

  Bod pridejNaZacatek(Mouable mouable) {
    if (isEmpty())
      return zacni(mouable);
    else {
      Bod bod = createBod(mouable);
      if (jsouMocBlizko(bod, start)) return start;
      Usek usek = createUsek();
      usek.spoj(bod, start);
      start = bod;
      return bod;
    }
  }


  private boolean jsouMocBlizko(Bod b1, Bod b2) {
    long kvadratVzdalenosti = FUtil.soucetKvadratu (b1.getMou().xx - b2.getMou().xx, b1.getMou().yy - b2.getMou().yy);
    //    System.out.println("Kvadrat vzdalenosti moc blizkych: " + kvadratVzdalenosti);
    if (kvadratVzdalenosti < 1000)
      return true;
    return false;
  }

  Bod pridejNaMisto(Mouable mouable) {
    Bousek0 usbod = locateBousekKamNejlepeVlozit(mouable.getMou());
    if (usbod instanceof Usek) {
      Usek usek = (Usek) usbod;
      Bod bod = usek.rozdelAZanikni(mouable);
      return bod;
    } else if (usbod instanceof Bod) {
      Bod bod = (Bod) usbod;
      Bod b = null;
      if (bod == start) {
        b = pridejNaZacatek(mouable);
      } else {
        b = pridejNaKonec(mouable);
      }
      assert b != null;
      return b;
    } else
      return zacni(mouable);
  }


  public boolean isEmpty() {
    return start == null;
  }

  /**
   * Vrací nejbližší objekt, ale jen když je do zadané vzálenosti.
   * Dá se vždy přednost bodu před úsekem.
   * Vrací null, pokud nic není do zadané vzdálenosti
   * @param mou
   * @param maximalniVzdalenost
   * @return
   */
  public SearchResult locateNejblizsiDoKvadratuVzdalenosi(Mou mou, long kvadratMaximalniVzdalenosti, boolean aDatPrednostBoduPredUsekem) {
    long minKvadrat = Long.MAX_VALUE;
    Bousek0 neblizsiBousek = null;
    for (Bod bod : getBody()) {
      long kvadratVzdalenosti = bod.computeKvadratVzdalenosti(mou);
      if (kvadratVzdalenosti <= kvadratMaximalniVzdalenosti) {
        if (kvadratVzdalenosti <= minKvadrat) {
          minKvadrat = kvadratVzdalenosti;
          neblizsiBousek = bod;
        }
      }
    }
    if (aDatPrednostBoduPredUsekem && neblizsiBousek != null) {
      SearchResult sr = new SearchResult();
      sr.bousek = neblizsiBousek;
      sr.kvadradVzdalenosti = minKvadrat;
      return sr;
    }
    for (Usek usek : getUseky()) {
      long kvadratVzdalenosti = usek.computeKvadratVzdalenostiBoduKUsecce(mou);
      if (kvadratVzdalenosti <= kvadratMaximalniVzdalenosti) {
        if (kvadratVzdalenosti < minKvadrat) {
          minKvadrat = kvadratVzdalenosti;
          neblizsiBousek = usek;
        }
      }
    }
    SearchResult sr = new SearchResult();
    sr.bousek = neblizsiBousek;
    sr.kvadradVzdalenosti = minKvadrat;
    return sr;
  }

  public static class SearchResult {
    public Bousek0 bousek;
    public long kvadradVzdalenosti = Long.MAX_VALUE;
  }

  /**
   * Najde úsek, do kterého je nejlepší daný bod vložit.
   * Pokud vrátí bod, je to bod krajový a je nejlepší vložit za něj.
   * @param mou Null vrací jen pokud je cesta úplně prázdná.
   * @return
   */
  public Bousek0 locateBousekKamNejlepeVlozit(Mou mou) {
    Bousek0 bousek = locateNejblizsi(mou);
    if (bousek instanceof Usek) {
      Usek usek = (Usek) bousek;
      return usek;
    }
    if (bousek instanceof Bod) {
      Bod bod = (Bod) bousek;
      Usek usek = bod.getBlizsiUsek(mou);
      return usek == null ? bod : usek;
    }
    return null; // cesta je úplně prázdná
  }

  /**
   * Vrací nejbližší objekt.
   * Dá se vždy přednost bodu před úsekem.
   * Vrací null, pokud nic není do zadané vzdálenosti
   * @param mou
   * @param maximalniVzdalenost
   * @return
   */
  public Bousek0 locateNejblizsi(Mou mou) {
    long minKvadrat = Long.MAX_VALUE;
    Bousek0 neblizsiUsta = null;
    for (Bod bod : getBody()) {
      long kvadratVzdalenosti = bod.computeKvadratVzdalenosti(mou);
      if (kvadratVzdalenosti < minKvadrat) {
        minKvadrat = kvadratVzdalenosti;
        neblizsiUsta = bod;
      }
    }
    for (Usek usek : getUseky()) {
      long kvadratVzdalenosti = usek.computeKvadratVzdalenosti(mou);
      if (kvadratVzdalenosti < minKvadrat) {
        minKvadrat = kvadratVzdalenosti;
        neblizsiUsta = usek;
      }
    }
    return neblizsiUsta;
  }


  /**
   * Vrací seznam všechj úseků cesty.
   * @return
   */
  public Iterable<Usek> getUseky() {
    return new Iterable<Usek>() {

      @Override
      public Iterator<Usek> iterator() {
        return new Iterator<Usek>() {

          private Usek currUsek = start == null ? null : start.getUvpred();

          @Override
          public void remove() {
            throw new RuntimeException("Usek nelze odstranit, co se temi body");
          }

          @Override
          public Usek next() {
            Usek result = currUsek;
            currUsek = currUsek.getBvpred().getUvpred();
            return result;
          }

          @Override
          public boolean hasNext() {
            return currUsek != null;
          }
        };
      }
    };
  }

  /**
   * Vrací seznam všech bodů cesty.
   * @return
   */
  public Iterable<Bod> getBody() {
    return new Iterable<Bod>() {

      @Override
      public Iterator<Bod> iterator() {
        return new Iterator<Bod>() {

          private Bod nextBod = start;

          @Override
          public void remove() {

            throw new RuntimeException("Bod nelze odstranit, protože to není implementováno");
          }

          @Override
          public Bod next() {
            Bod result = nextBod;
            nextBod = nextBod.getUvpred() == null ? null : nextBod.getUvpred().getBvpred();
            return result;
          }

          @Override
          public boolean hasNext() {
            return nextBod != null;
          }
        };
      }
    };
  }

  /**
   * Vrací seznam všech bodů cesty.
   * @return
   */
  public Iterable<Bousek0> getBousky() {
    return new Iterable<Bousek0>() {

      @Override
      public Iterator<Bousek0> iterator() {
        return new Iterator<Bousek0>() {

          private Bousek0 nextBousek = start;

          @Override
          public void remove() {

            throw new RuntimeException("Bod nelze odstranit, protože to není implementováno");
          }

          @Override
          public Bousek0 next() {
            Bousek0 result = nextBousek;
            nextBousek = nextBousek.getBousekVpred() == null ? null : nextBousek.getBousekVpred();
            return result;
          }

          @Override
          public boolean hasNext() {
            return nextBousek != null;
          }
        };
      }
    };
  }


  /**
   * Vrátí množinu waypointů, které jsou v cestě.
   * @return
   */
  public Set<Wpt> getWpts() {
    Set<Wpt> wpts = new HashSet<Wpt>();
    for (Bod bod : getBody()) {
      if (bod.mouable instanceof Wpt) {
        wpts.add((Wpt)bod.mouable);
      }
    }
    return wpts;
  }

  void clear() {
    //System.out.println(System.identityHashCode(this) + ": clear()");
    //new Throwable().printStackTrace(System.out);
    start = null;
    cil = null;
  }

  Usek odeberBodNechceme(Mouable mouable) {
    if (mouable instanceof Bod) {
      Bod bod = (Bod) mouable;
      Usek usek = bod.remove();
      return usek;
    } else {
      for (Bod bod : getBody()) {
        if (bod.mouable == mouable) {
          Usek usek = bod.remove();
          return usek;
        }
      }
    }
    return null;
  }

  public boolean hasWpt(Wpt wpt) {
    //System.out.println("Volá se hasWpt");
    //FIXME-vylet Optimalizovat, volá se často.
    for (Bod bod : getBody()) {
      if (bod.mouable == wpt)
        return true;
    }
    return false;
  }

  public int getPocetWaypointu() {
    //System.out.println("Volá se getPocetWaypointu");
    //new Throwable().printStackTrace();
    //FIXME-vylet Optimalizovat, volá se často.
    int sum = 0;
    for (Bod bod : getBody()) {
      if (bod.mouable instanceof Wpt) {
        sum++;
      }
    }
    return sum;
  }

  public Bod getCil() {
    return cil;
  }

  public Bod getStart() {
    return start;
  }

  void setStart(Bod start) {
    if (start == null) {
      clear();
    } else {
      this.start = start;
      start.setUvzad(null);   // odříznout zbytek k začátku
    }
  }

  void setCil(Bod cil) {
    if (cil == null) {
      clear();
    } else {
      this.cil = cil;
      cil.setUvpred(null);  // odříznout zbytek ke konci
    }
  }

  Doc getDoc() {
    return doc;
  }

  public boolean isJednobodova() {
    return start == cil;
  }

  public boolean isKruh() {
    if (isEmpty()) return false; // prázdná není kruh
    if (isJednobodova()) return false;  // jednobodová není kruh
    boolean result = start.getMouable().equals(cil.getMouable());
    return result;
  }

  void setChanged() {
    if (doc != null) {
      doc.setChanged();
    }
  }

  void setDoc(Doc doc2) {
    doc = doc2;
  }

  public double dalka() {
    double suma = 0;
    for (Usek usek : getUseky()) {
      suma += usek.dalka();
    }
    return suma;
  }

  public String dalkaHtml() {
    return dalkaHtml(dalka(), FBarvy.CURTA_NORMALNE);
  }

  public double dalkaStartuACile() {
    //System.out.println(System.identityHashCode(this) + ": dalkaStartuACile "+ start + " " + cil);
    kontrolaKonzistence();
    return Mou.dalka(start, cil);
  }

  public String dalkaStartuACileHtml() {
    return dalkaHtml(dalkaStartuACile(), Color.BLACK);
  }


  void reverse() {
    ArrayList<Usek> useky = new ArrayList<Usek>();
    for (Usek usek : getUseky()) {
      useky.add(usek);
    }
    for (Usek usek : useky) {
      Bod bod = usek.getBvpred();
      usek.setBvpred(usek.getBvzad());
      usek.setBvzad(bod);
    }
    for (Usek usek : useky) {
      usek.getBvpred().setUvzad(usek);
      usek.getBvzad().setUvpred(usek);
    }
    Bod bod = start;
    start = cil;
    cil = bod;
    start.setUvzad(null);
    cil.setUvpred(null);
    setChanged();
  }

  /** Dálku v km v zadané barvě */
  public static final String dalkaHtml(double dalka, Color color) {
    return String.format("<font color=%s><i>%.1f km</i></font>", FUtil.getHtmlColor(color), dalka / 1000);
  }

  public boolean hasBod(Mou mou) {
    for (Bod bod : getBody()) {
      if (bod.getMou().equals(mou)) return true;
    }
    return false;
  }

  void remove() {
    if (doc == null) return;
    doc.removex(this);
    doc = null;
  }

  public void napojBouskyNaTutoCestu() {
    for (Bousek0 bousek : this) {
      bousek.cesta = this;
    }
  }

  @Override
  public Iterator<Bousek0> iterator() {
    return getBousky().iterator();
  }

  public String getNazevHtml() {
    if (nazev != null)
      // Ta mezera je tam kvůli bezejmenným cestám
      return " <i>\"" + nazev + "\"</i>";
    else
      return "";
  }

  public String getNazevADalkaHtml() {
    return getNazevHtml() + " " + dalkaHtml();
  }

  /**
   * Přijme body z jiné cesty a tuto cestu vyprázdní
   * @param cesta
   */
  void prijmyBodyZJine(Cesta cesta) {
    assert cesta != this;
    setStart(cesta.getStart());
    setCil(cesta.getCil());
    cesta.clear();
    System.out.println(System.identityHashCode(this) + ": prijmyBodyZJine() " + System.identityHashCode(cesta) + start + " " + cil);
    napojBouskyNaTutoCestu();
  }

  void pripojitZa(Cesta cesta2) {
    Cesta cesta = Doc.propojCestyDoJine(this, cesta2);
    prijmyBodyZJine(cesta);
    doc.removex(cesta2);
  }

  void pripojitPred(Cesta cesta1) {
    Cesta cesta = Doc.propojCestyDoJine(cesta1, this);
    prijmyBodyZJine(cesta);
    doc.removex(cesta1);
  }

  public int getPocetVzdusnychUseku() {
    int pocet = 0;
    for (Usek usek : getUseky()) {
      if (usek.isVzdusny()) {
        pocet ++;
      }
    }
    return pocet;
  }

  /**
   * Vrátí kolik podcest se souvislými úseky v cestě existuje.
   * 
   * @return Nemůže být větší než počet vzdušných úseků a nemůže být menší než 1, pokud nějaký vzdušný úsek existuje.
   */
  public int getPocetPodcestVzdusnychUseku() {
    int pocet = 0;
    boolean minuleBylVzdusny = false;
    for (Usek usek : getUseky()) {
      if (usek.isVzdusny() && ! minuleBylVzdusny) {
        pocet ++;
      }
      minuleBylVzdusny = usek.isVzdusny();
    }
    return pocet;
  }


  private void kon(boolean podm) {
    if (!podm)
      throw new RuntimeException("Selhala kontrola konzistence cesty");
  }

  public void kontrolaKonzistence() {
    boolean assertsEnabled = false;
    assert assertsEnabled = true;
    if (!assertsEnabled) return;

    kon (doc != null);
    doc.kontrolaZeJeTady(this);

    kon(start == null && cil == null || start != null && cil != null);
    if (start == null) return;
    kon (start.getUvzad() == null);
    kon (cil.getUvpred() == null);

    @SuppressWarnings("unused")
    int pocetBousku = 0;
    for (Bousek0 bousek = start; bousek != null; bousek = bousek.getBousekVpred()) {
      kon (bousek.getCesta() == this);
      bousek.kontrolaKonzistence();
      if (bousek.getBousekVpred() == null) {
        kon (bousek == cil);
      }
      pocetBousku ++;
    }

    for (Bousek0 bousek = cil; bousek != null; bousek = bousek.getBousekVzad()) {
      kon (bousek.getCesta() == this);
      bousek.kontrolaKonzistence();
      if (bousek.getBousekVzad() == null) {
        kon (bousek == start);
      }
    }

    //System.out.println(System.identityHashCode(this) + ": uspela kontrola existence: " + pocetBousku + " bousku " + ATimestamp.now());
  }

  public void pospojujVzdusneUseky() {
    List<Bod> keSmazani = new ArrayList<Bod>();
    for (Bod bod : getBody()) {
      if (bod.isMeziVzdusnymiUseky()) {
        keSmazani.add(bod);
      }
    }
    for (Bod bod : keSmazani) {
      bod.remove();
    }
  }

  public String getNazev() {
    return nazev;
  }

  void setNazev(String nazev) {
    this.nazev = nazev;
  }

}
