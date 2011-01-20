package cz.geokuk.plugins.mapy.kachle;



import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloRoste;


public class JKachle extends JComponent implements ImageReceiver {

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
  private static final long serialVersionUID = -5445121736003161730L;

  // Jen kvůli zahazování. Možná to ani není potřeba.
  private List<WeakReference<KaAllReq>> kachLoadRequests = new ArrayList<WeakReference<KaAllReq>>();

  private EnumSet<EKaType> coMam = EnumSet.noneOf(EKaType.class);
  private Image img;

  private KaLoc iPoziceJenProVypsani;
  private KaAll plny;

  private boolean kachleJeZnicena;
  private int vzdalenostOdStredu;

  private final KachleModel kachleModel;

  protected boolean jeTamUzCelyObrazek;
  private final boolean vykreslovatOkamzite;
  private final JKachlovnik jKachlovnik;
  //private static int cictac;

  //Point mou = new Point();  // souřadnice roho

  //	private boolean jakoZeJeVykresleno;

  public JKachle(KaAll plny, KachleModel kachleModel, boolean vykreslovatOkamzite, JKachlovnik jKachlovnik) {
    this.plny = plny;
    this.kachleModel = kachleModel;
    this.vykreslovatOkamzite = vykreslovatOkamzite;
    this.jKachlovnik = jKachlovnik;
    assert kachleModel != null;
    setSize(KACHLE_WIDTH, KACHLE_HEIGHT);
  }


  void setPoziceJenProVypsani(KaLoc aPoziceJenProVypsani) {
    iPoziceJenProVypsani = aPoziceJenProVypsani;
  }


  @Override
  protected void paintComponent(Graphics aG) {
    super.paintComponent(aG);
    //    if (true) return;
    Graphics2D g = (Graphics2D) aG.create();
    if (vykreslovatOkamzite) {
      // Pokud rendruji do KMZ č souboru, a ne naobrazovku tak mám možná otočeno a nestojím o žádné uříznuití.
      g.setClip(null);
    }
    if (img == null) {
      g.setColor(Color.blue);
      drawPsanicko(g);

      g.setColor(Color.RED);
      vypisPozici(g);
    } else {
      g.drawImage(img, 0, 0, null);
      //       g.drawRect(0, 0, getWidth() -1, getHeight()-1);
      //       System.out.println(cictac ++ +  ". vykresleno " + iPoziceJenProVypsani);
      //       vypisPozici(g);
    }
    super.paintComponent(aG);
    //System.out.println("Pejntuji komponentu " + getLocation());
  }

  private void vypisPozici(Graphics2D g) {
    KaLoc p = iPoziceJenProVypsani;
    if (p != null) {
      String s = String.format("x=%h y=%h z=%d", p.getMou().xx, p.getMou().yy, p.getMoumer());
      g.setColor(Color.WHITE);
      g.drawString(s, 5, 240);
    }
  }

  private void drawPsanicko(Graphics2D g) {
    int kraj = 3;
    int x1 = kraj;
    int y1 = kraj;
    int x2 = getSize().width - 1 - kraj;
    int y2 = getSize().height - 1 - kraj;
    g.drawLine(x1, y1, x1, y2);
    g.drawLine(x1, y1, x2, y1);
    g.drawLine(x1, y2, x2, y2);
    g.drawLine(x2, y1, x2, y2);
    g.drawLine(x1, y1, x2, y2);
    g.drawLine(x1, y2, x2, y1);
  }


  @Override
  public synchronized void setImage(final EnumSet<EKaType> types, final Image img2, final boolean aJeToUzCelyObrazek) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        setImageImmadiately(types, img2, aJeToUzCelyObrazek);
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
   * @param aJeToUzCelyObrazek
   */
  protected synchronized void setImageImmadiately(final EnumSet<EKaType> types, final Image img2, boolean aJeToUzCelyObrazek) {
    //if (true) return;
    if (kachleJeZnicena) {
      pocitPlneniImageDoZnicenychKachli.inc();
      return;
    }
    if (! coMam.equals(types)) {
      img = img2;
      coMam = types;
      repaint();
    }
    if (aJeToUzCelyObrazek) {
      jeTamUzCelyObrazek = true;
      if (jKachlovnik != null) {
        jKachlovnik.kachleZpracovana(this);
      }
      notifyAll();
    }
    //System.out.println("  ... notifikovano=");
  }

  public void setVzdalenostOdStredu(int vzdalenostOdStredu) {
    this.vzdalenostOdStredu = vzdalenostOdStredu;
  }

  public void ziskejObsah(Priorita priorita) {
    //System.out.println("V kesi nalezeny image " + memoryCachedImages + " pro " + lokace);


    Image img = kachleModel.cache.memoryCachedImage(plny);
    if (img != null) {
      setImageImmadiately(plny.kaSet.getKts(), img, true);
      return; // to bylo jednoduché, je to celé zde
    }
    // můžeme požádat o plný
    KaAllReq klr = new KaAllReq(plny, this, priorita);
    klr.setVzdalenostOdStredu(vzdalenostOdStredu);
    kachLoadRequests.add(new WeakReference<KaAllReq>(klr)); // ať si request drží kachle
    kachleModel.rozrazovaciQueue.add(klr);
    kachleModel.pocitRozrazovaciQueue.set(kachleModel.rozrazovaciQueue.size());


    img = kachleModel.cache.memoryCachedImage(plny.getPodklad());
    if (img != null) {
      // tak aspoň uložíme ten nakešlý podklad
      setImageImmadiately(EnumSet.of(plny.getPodkladType()), img, false);
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



}
