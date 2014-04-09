package cz.geokuk.plugins.mapy.kachle;



import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.napoveda.NapovedaModelChangedEvent;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;
import cz.geokuk.util.pocitadla.PocitadloRoste;




public abstract class JKachlovnik extends JSingleSlide0 implements AfterEventReceiverRegistrationInit {

  public static final int HRANA = 256;


  private static final long serialVersionUID = -6300199882447791157L;

  private static Pocitadlo pocitZustalychKachli = new PocitadloRoste("Počet zůstalých kachlí",
      "Počet kachlí jKachle, které jako kompoenty zůstaly a jen se změnila její lokace, protože po reinicializaci byly na svém místě (obvykle posun) a nebylo je tudíž nutné znovu vytvářet");

  private static Pocitadlo pocitReinicializaceKachlovniku = new PocitadloRoste("Kolikrát bylo nuceno reinicializovat celý kachlovník",
      "Říká, kolikrát byla zavolána metoda init pro reinicializaci celého kachlovníku v důsledku posunu, změnu velikosti, zůůmování atd.");

  private static Pocitadlo pocitVynuceneNepouzitiExistujiciKachle = new PocitadloRoste("Vynucené použití neexistující kachle",
      "Kolikrát do JKachlovnik.ini() přišlo z venku, že JKachle komponenty nesmím použít");

  private final Pocitadlo pocitKachliVKachlovniku1 = new PocitadloMalo("Počet kachlí v kachlovníku 1", "");
  private final Pocitadlo pocitKachliVKachlovniku2 = new PocitadloMalo("Počet kachlí v kachlovníku 2", "");
  @SuppressWarnings("unused") // pripraveno
  private static final int PRESAH_KACHLI = 3;


  private Map<KaLoc, JKachle> kachles = new HashMap<KaLoc, JKachle>(200);


  private KaSet kachloTypesSet = new KaSet(EnumSet.noneOf(EKaType.class));

  private KachleModel kachleModel;


  // FIXME musí to být private a musí být na tru nastaveno jen při rendrování
  protected boolean vykreslovatokamzite;


  // je to jen kvuli garamge collectoru, aby nezrusil, NERUSIT PROMENNU i kdyz zdanlive je to na nic
  public JKachlovnik() {
    setLayout(null);
    setPreferredSize(new Dimension(800, 600));
    setBackground(Color.GREEN);
    setOpaque(false);
  }


  @Override
  public void onVyrezChanged() {
    init(true, Priorita.KACHLE);
  }

  private void registerEvents() {
  }


  public void onEvent(ZmenaMapNastalaEvent event) {
    setKachloTypes(event.getKaSet());
  }

  protected void init(boolean smimZnovuPouzitKachle, Priorita priorita) {
    if (! isSoordInitialized()) return;
    if (getWidth() == 0 || getHeight() == 0) return; // nemá smysl rendrovat prázdný kachlovník
    Coord soord = getSoord();
    if (soord == null) return;
    assert kachleModel != null;
    if (kachloTypesSet == null) return;
    pocitReinicializaceKachlovniku.inc();
    if (!smimZnovuPouzitKachle) {
      pocitVynuceneNepouzitiExistujiciKachle.inc();
    }

    //System.out.println(System.identityHashCode(this) + " kachlovnik se inicializuje: " + getWidth() + " " + getHeight() );
    int xn = getWidth();

    // kraj krajove kachle vlevo dole, ktera se jeste musi zobrazit
    int moumaska = ~ (soord.getMoukrok()-1);
    Mou mou0 = new Mou(soord.getMoupoc().xx & moumaska, soord.getMoupoc().yy & moumaska);

    //System.out.println(xn + " --- " + yn);

    // a teď jedeme
    Map<KaLoc, JKachle> newKachles = new HashMap<KaLoc, JKachle>(200);

    Mou mou = new Mou();
    for (mou.xx = mou0.xx; soord.transform(mou).x < xn; mou.xx += soord.getMoukrok()) {
      for (mou.yy = mou0.yy; soord.transform(mou).y > 0; mou.yy += soord.getMoukrok()) { // y ukazuje na spodek kachle
        KaLoc lokace = new KaLoc(new Mou(mou), soord.getMoumer());
        JKachle kachle = kachles.remove(lokace);
        boolean kachleSePouzije = smimZnovuPouzitKachle && kachle != null;
        if (! kachleSePouzije) {
          if (kachle != null)
          {
            remove(kachle); // odstranit, když už je
          }
          kachle = createKachle(new KaAll(lokace, kachloTypesSet), kachleModel, vykreslovatokamzite, this); // když se nepoužije, musí se stvořit nová
          kachle.setVzdalenostOdStredu(soord.getVzdalenostKachleOdStredu(lokace.getMou()));
          add(kachle);          // a přidat jako komponentu
          kachle.ziskejObsah(priorita);
        } else {  // použije se původní kachle
          pocitZustalychKachli.inc();
        }
        { // napozicujeme každou kachli do správné podoby
          Point p = soord.transform(lokace.getMou());
          int x = p.x;
          int y = p.y - HRANA;
          //System.out.println("KACHLE xx: " + new Point(x,y) + " ********* " + getSize());
          if (x < -255 || x >= getWidth() || y < -255 || y >= getHeight()) {
            System.out.println("KACHLE JE úplně mimo: " + new Point(x,y) + " ********* " + getSize());
          }
          kachle.setLocation(x, y);
          kachle.setPoziceJenProVypsani(lokace);
          newKachles.put(lokace, kachle);  // děláme vždy novou mapu kachlí i když je znovu používáme
        }
      } // konec iterace dle Y
    } // konec iterace dle X
    //System.out.println("mame komponent: " + super.getComponentCount());
    //System.out.println("Nahrazenych kachli: " + kachles.size());
    for (JKachle kachle : kachles.values()) {
      remove(kachle);  // to co zbylo vyhodíme
      kachle.uzTeNepotrebuju();
    }
    kachles = newKachles; // a nově získané kachle sem schovat
    pocitKachliVKachlovniku1.set(kachles.size());
    pocitKachliVKachlovniku2.set(getComponentCount());

  }


  protected JKachle createKachle(KaAll plny, KachleModel kachleModel, boolean vykreslovatOkamzite, JKachlovnik jKachlovnik) {
    return new JKachle(plny, kachleModel, vykreslovatOkamzite, jKachlovnik);
  }


  /**
   * @return the kachloTypes
   */
  public KaSet getKachloTypes() {
    return kachloTypesSet;
  }

  public void onEvent(NapovedaModelChangedEvent event) {
    if (event.getModel().isOnlineMode()) {
      init(false, Priorita.KACHLE);
    }
  }

  /**
   * @param aKachloTypes the kachloTypes to set
   */
  public void setKachloTypes(KaSet aKachloSet) {
    if (kachloTypesSet.equals(aKachloSet)) return;
    kachloTypesSet = aKachloSet;
    init(false, Priorita.KACHLE);
  }


  public void inject(KachleModel kachleModel) {
    this.kachleModel = kachleModel;
  }


  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    registerEvents();
  }


  void kachleZpracovana(JKachle jKachle) {
  }


  //  @Override
  //  public void finalize() {
  //    System.out.println("Kachlovník finalizován");
  //  }
}
