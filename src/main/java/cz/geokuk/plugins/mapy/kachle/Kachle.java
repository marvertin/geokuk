package cz.geokuk.plugins.mapy.kachle;

import java.awt.Image;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.SwingUtilities;

import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloRoste;

public class Kachle implements ImageReceiver {

  //	(2) ad mapy.cz: jejich vnitřní XY souřadnice jsou odvozeninou UTM souřadnic.
  //	čtverec s nejzápadnějčím bodem ČR je na http://m4.mapserver.mapy.cz/turist/13_79D8000_8250000
  //	sX = dec(79D8000) = 127762432
  //	sY = dec(8250000) = 136642560
  //	13 je měřítko mapy
  //
  //	X_UTM=(sX*0,03125)-3700000 = 292576
  //	Y_UTM=(sY*0,03125)+1300000 = 5570080
  //
  //	a pak už jen stačí převést UTM na WGS :-) a máš souřadnice levého dolního rohu každé dlaždice těch jejich bitmap.

  //	http://www.mapy.cz/#x=127762400@y=136642528@z=13@mm=TP@sa=s@st=s@ssq=50.246422,12.090604@sss=1@ssp=1
  public static int KACHLE_WIDTH =256;
  public static int KACHLE_HEIGHT =256;

  private static Pocitadlo pocitPlneniImageDoZnicenychKachli = new PocitadloRoste("Počet plnění obrázku do zničených kachlí",
  "Počítá, kolikrát se stahovaný obrázek plnil do kachle, která už nebyla potřeba a nikdy potřeba nebude, ale download už běžel.");

  // Jen kvůli zahazování. Možná to ani není potřeba.
  private List<WeakReference<KaAllReq>> kachLoadRequests = new ArrayList<>();

  private EnumSet<EKaType> coMam = EnumSet.noneOf(EKaType.class);
  private Image img;

  private KaLoc iPoziceJenProVypsani;
  private KaAll plny;

  private boolean kachleJeZnicena;
  private int vzdalenostOdStredu;

  private final KachleModel kachleModel;

  protected boolean jeTamUzCelyObrazek;
  private final boolean vykreslovatOkamzite;
  //private static int cictac;
  private final JKachle jkachle;

  //Point mou = new Point();  // souřadnice roho

  //	private boolean jakoZeJeVykresleno;

  public Kachle(KaAll plny, KachleModel kachleModel, boolean vykreslovatOkamzite, JKachle jkachle) {
    this.plny = plny;
    this.kachleModel = kachleModel;
    this.vykreslovatOkamzite = vykreslovatOkamzite;
    this.jkachle = jkachle;
    assert kachleModel != null;
  }

  public void init() {
    
  }

  void setPoziceJenProVypsani(KaLoc aPoziceJenProVypsani) {
    iPoziceJenProVypsani = aPoziceJenProVypsani;
  }


  @Override
  public synchronized void setImage(KachloStav kachloStav) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        setImageImmadiately(kachloStav);
      }
    };
    if (vykreslovatOkamzite) {
      runnable.run();
    } else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  /**
   * Toto musí běžen na hlavním vlákně.
   * @param types
   * @param img2
   * @param jeToUzCelyObrazek
   */
  protected synchronized void setImageImmadiately(KachloStav kachloStav) {
    if (kachleJeZnicena) {
      pocitPlneniImageDoZnicenychKachli.inc();
      return;
    }
    if (! coMam.equals(kachloStav.types)) {
      img = kachloStav.img2;
      coMam = kachloStav.types;
      if (jkachle != null) jkachle.repaint();
    }
    if (kachloStav.jeToUzCelyObrazek) {
      jeTamUzCelyObrazek = true;
      if (jkachle != null && jkachle.getjKachlovnik() != null) {
        jkachle.getjKachlovnik().kachleZpracovana(jkachle);
      }
      notifyAll();
    }
    //System.out.println("  ... notifikovano=");
  }

  public void setVzdalenostOdStredu(int vzdalenostOdStredu) {
    this.vzdalenostOdStredu = vzdalenostOdStredu;
  }

  public void ziskejObsah(Priority priorita) {
    //if(true) return;
    //System.out.println("V kesi nalezeny image " + memoryCachedImages + " pro " + lokace);


    Image img = kachleModel.cache.memoryCachedImage(plny);
    if (img != null) {
      setImageImmadiately(new KachloStav(plny.kaSet.getKts(), img, true));
      return; // to bylo jednoduché, je to celé zde
    }
    // můžeme požádat o plný
    KaAllReq klr = new KaAllReq(plny, this, priorita);
    klr.setVzdalenostOdStredu(vzdalenostOdStredu);
    kachLoadRequests.add(new WeakReference<>(klr)); // ať si request drží kachle
    kachleModel.rozrazovaciQueue.add(klr);
    kachleModel.pocitRozrazovaciQueue.set(kachleModel.rozrazovaciQueue.size());


    img = kachleModel.cache.memoryCachedImage(plny.getPodklad());
    if (img != null) {
      // tak aspoň uložíme ten nakešlý podklad
      setImageImmadiately(new KachloStav(EnumSet.of(plny.getPodkladType()), img, false));
    }
  }

  public synchronized void uzTeNepotrebuju() {
    if (kachleJeZnicena) return;
    for (WeakReference<KaAllReq> wrklr : kachLoadRequests) {
      KaAllReq kaAllReq = wrklr.get();
      if (kaAllReq != null) kaAllReq.uzToZahod();
    }
    // nachystáme na zničení, to může trvat, ale ty objekty zde být nemusí
    kachLoadRequests = null;
    img = null;
    coMam = null;
    plny = null;
    kachleJeZnicena = true;
  }



  // Řešení čekání na dokončení renderu

  public synchronized void waitNaDotazeniDlazdice() throws InterruptedException {
    while (! jeTamUzCelyObrazek) {
      wait();
    }
  }

  protected Image getImg() {
    return img;
  }

  public boolean isVykreslovatOkamzite() {
    return vykreslovatOkamzite;
  }

  public KaLoc getiPoziceJenProVypsani() {
    return iPoziceJenProVypsani;
  }

  public JKachle getJkachle() {
    return jkachle;
  }


}
