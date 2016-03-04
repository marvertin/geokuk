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

  //Point mou = new Point();  // souřadnice roho

  //	private boolean jakoZeJeVykresleno;

  public JKachle(JKachlovnik jKachlovnik) {
    this.jKachlovnik = jKachlovnik;
    setSize(KACHLE_WIDTH, KACHLE_HEIGHT);
  }

  public void init() {
    
  }

  @Override
  protected void paintComponent(Graphics aG) {
    super.paintComponent(aG);
    //    if (true) return;
    Graphics2D g = (Graphics2D) aG.create();
    if (kachle.isVykreslovatOkamzite()) {
      // Pokud rendruji do KMZ č souboru, a ne naobrazovku tak mám možná otočeno a nestojím o žádné uříznuití.
      g.setClip(null);
    }
    if (kachle.getImg() == null) {
      g.setColor(Color.blue);
      drawPsanicko(g);

      g.setColor(Color.RED);
      vypisPozici(g);
    } else {
      g.drawImage(kachle.getImg(), 0, 0, null);
      //       g.drawRect(0, 0, getWidth() -1, getHeight()-1);
      //       System.out.println(cictac ++ +  ". vykresleno " + iPoziceJenProVypsani);
      //       vypisPozici(g);
      g.setColor(Color.blue);
      drawPsanicko(g);

      g.setColor(Color.RED);
      vypisPozici(g);      
    }
    super.paintComponent(aG);
    //System.out.println("Pejntuji komponentu " + getLocation());
  }

  private void vypisPozici(Graphics2D g) {
    KaLoc p = kachle.getiPoziceJenProVypsani();
    if (p != null) {
      Mou mou = p.getMouSZ(); // souřadnice SZ kachle
      int xx = mou.xx;
      int yy = mou.yy;
      g.setColor(Color.WHITE);
      g.drawString("x = " + toHex(xx), 5,  15);
      g.drawString("y = " + toHex(yy), 5,  30);
      g.drawString("z = " + p.getMoumer(), 5, 45);
      
      Wgs wgs = mou.toWgs();
      g.drawString("lat = " + wgs.lat, 5, 70);
      g.drawString("lon = " + wgs.lon, 5, 89);
      
      g.drawString("[" + p.getSignedX() + "," + p.getSignedY() + "]" , 120,  133);
      g.drawString("[" + p.getFromSzUnsignedX() + "," + p.getFromSzUnsignedY() + "]" , 120,  148);
      

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


  private static String toHex(int cc) {
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

  public void setKachle(Kachle aKachle) {
    kachle = aKachle;
  }

}
