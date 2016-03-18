package cz.geokuk.plugins.mapy.kachle;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;

public class JKachle extends JComponent {

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

  private static final long serialVersionUID = -5445121736003161730L;

  private final JKachlovnik jKachlovnik;
  private Kachle kachle;
  //private static int cictac;
  private final KaLoc kaloc;

  //Point mou = new Point();  // souřadnice roho

  //	private boolean jakoZeJeVykresleno;

  public JKachle(final JKachlovnik jKachlovnik, final KaLoc kaloc) {
    this.jKachlovnik = jKachlovnik;
    this.kaloc = kaloc;
    setSize(KACHLE_WIDTH, KACHLE_HEIGHT);
  }

  public void init() {

  }

  @Override
  protected void paintComponent(final Graphics aG) {
    super.paintComponent(aG);
    final boolean zobrazovatNaKachlichPozice = false;
    //    if (true) return;
    final Graphics2D g = (Graphics2D) aG.create();
    if (kachle.isVykreslovatOkamzite()) {
      // Pokud rendruji do KMZ č souboru, a ne naobrazovku tak mám možná otočeno a nestojím o žádné uříznuití.
      g.setClip(null);
    }
    if (kachle.getImg() != null) {
      g.drawImage(kachle.getImg(), 0, 0, null);
    }
    if (kachle.getImg() == null || zobrazovatNaKachlichPozice) {
      g.setColor(Color.blue);
      drawPsanicko(g);
      g.setColor(Color.RED);
      vypisPozici(g);

      if (kachle.kachloStav != null && kachle.kachloStav.faze != null) {
        //System.out.println("FAZE: " + kachle.faze);
        switch (kachle.kachloStav.faze) {
        case ZACINAM_STAHOVAT:
          g.setColor(Color.YELLOW);
          g.drawString("STAHUJI ", 5,  170);
          break;
        case ZACINAM_NACITAT_Z_DISKU:
          g.setColor(Color.MAGENTA);
          g.drawString("NAČÍTÁM Z DISKU ", 5,  170);
          break;
        case STAZENA_POSLEDNI_KACHLE:
          break;
        case STAZENA_PRUBEZNA_KACHLE:
          break;
        case CHYBA:
          g.setColor(Color.RED);
          g.drawString("CHYBA ", 5,  170);
          g.drawString(kachle.kachloStav.thr + "", 5,  185);
          break;
        case OFFLINE_MODE:
          g.setColor(Color.YELLOW);
          g.drawString("OFF-LINE-MODE ", 5,  170);
          break;
        }
      }
      if (kachle.kachloStav != null && kachle.kachloStav.url != null) {
        final String s = kachle.kachloStav.url.toString();
        final int index = ordinalIndexOf(s, '/', 2) + 1;
        g.setColor(Color.YELLOW);
        g.drawString(s.substring(0, index), 5,  190);
        g.drawString(s.substring(index), 10,  205);
      }

    }
    super.paintComponent(aG);
  }

  private void vypisPozici(final Graphics2D g) {
    final KaLoc p = kachle.getiPoziceJenProVypsani();
    if (p != null) {
      final Mou mou = p.getMouSZ(); // souřadnice SZ kachle
      final int xx = mou.xx;
      final int yy = mou.yy;
      g.setColor(Color.WHITE);
      g.drawString("x = " + toHex(xx), 5,  15);
      g.drawString("y = " + toHex(yy), 5,  30);
      g.drawString("z = " + p.getMoumer(), 5, 45);

      final Wgs wgs = mou.toWgs();
      g.drawString("lat = " + wgs.lat, 5, 70);
      g.drawString("lon = " + wgs.lon, 5, 89);

      g.drawString("[" + p.getSignedX() + "," + p.getSignedY() + "]" , 120,  133);
      g.drawString("[" + p.getFromSzUnsignedX() + "," + p.getFromSzUnsignedY() + "]" , 120,  148);


    }
  }


  private void drawPsanicko(final Graphics2D g) {
    final int kraj = 3;
    final int x1 = kraj;
    final int y1 = kraj;
    final int x2 = getSize().width - 1 - kraj;
    final int y2 = getSize().height - 1 - kraj;
    g.drawLine(x1, y1, x1, y2);
    g.drawLine(x1, y1, x2, y1);
    g.drawLine(x1, y2, x2, y2);
    g.drawLine(x2, y1, x2, y2);
    g.drawLine(x1, y1, x2, y2);
    g.drawLine(x1, y2, x2, y1);
  }


  private static String toHex(final int cc) {
    String s = Integer.toHexString(cc);
    s = "00000000".substring(0, 8-s.length()) + s;
    s = s.substring(0,4) + " " + s.substring(4);
    return s;
  }

  public JKachlovnik getjKachlovnik() {
    return jKachlovnik;
  }

  public Kachle getKachle() {
    return kachle;
  }

  public void setKachle(final Kachle aKachle) {
    kachle = aKachle;
  }

  private static int ordinalIndexOf(final String str, final char c, int n) {
    int pos = str.indexOf(c, 0);
    while (n-- > 0 && pos != -1) {
      pos = str.indexOf(c, pos+1);
    }
    return pos;
  }


  public KaLoc getKaLoc() {
    return kaloc;
  }
}
