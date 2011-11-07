package cz.geokuk.core.coord;



import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.MouRect;
import cz.geokuk.core.coordinates.Moud;
import cz.geokuk.core.coordinates.Utm;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.util.index2d.BoundingRect;




/**
 * Objekt je immuitable.
 * @author veverka
 *
 */
public class Coord  {

  private static int MIN_MOU_X = 0x04000000;
  private static int MIN_MOU_Y = 0x04000000;
  private static int MAX_MOU_X = 0x0c000000;
  private static int MAX_MOU_Y = 0x0c000000;



  //	X_UTM=(sX*0,03125)-3700000 = 292576
  //	Y_UTM=(sY*0,03125)+1300000 = 5570080

  // Základní parametry
  private final int moumer;  // měřítko
  private final Mou moustred; // střed
  private final Dimension dim; // velikost
  private final double natoceni; // natočení v radiánech

  // odvozené
  private final int moukrok; // krok, který přechází od kachle ke kachli
  private final int pomer;  // poměr mezi mou a pixlama
  private AffineTransform tam;
  private AffineTransform zpet;

  /**
   * 
   */
  public Coord(int moumer, Mou moustred, Dimension dim, double natoceni) {
    //moumer
    this.moumer = moumer;
    moukrok = 1 << (28-moumer);
    pomer = 1 << (20-moumer);

    //moustred
    int xx = moustred.xx;
    int yy = moustred.yy;
    int pulSirkyXX = dim.width * pomer / 2;
    int pulVyskyYY = dim.height * pomer / 2;
    int sever = yy + pulVyskyYY;
    int jih =   yy - pulVyskyYY;
    int vychod= xx + pulSirkyXX;
    int zapad = xx - pulSirkyXX;
    if (zapad  < MIN_MOU_X) {
      xx += MIN_MOU_X - zapad;
    }
    if (jih    < MIN_MOU_Y) {
      yy += MIN_MOU_Y - jih;
    }
    if (vychod > MAX_MOU_X) {
      xx -= vychod - MAX_MOU_X;
    }
    if (sever  > MAX_MOU_Y) {
      yy -= sever  - MAX_MOU_Y;
    }
    this.moustred = new Mou(xx, yy);

    // dim
    this.dim = dim;

    // natoceni
    this.natoceni = natoceni;
    computeAffineTransforms();
  }



  private void computeAffineTransforms() {
    if (natoceni == 0) {
      tam = null;
      zpet = null;
      return;
    }
    AffineTransform tra = new AffineTransform();
    double widthPul = (double)dim.width / 2;
    double heightPul = (double)dim.height / 2;
    tra.translate(widthPul, heightPul);
    tra.rotate(natoceni);
    tra.translate(- widthPul, -heightPul);
    tam = tra;
    try {
      zpet = tam.createInverse();
    } catch (NoninvertibleTransformException e) {
      throw new RuntimeException(e);
    }
  }

  public Mou transform(Point p) {
    if (zpet != null) {
      zpet.transform(p, p);
    }
    Mou mou = new Mou();
    mou.xx = getMoupoc().xx + p.x * pomer;
    mou.yy = getMoupoc().yy + (dim.height - p.y) * pomer;
    return mou;
  }

  public MouRect transform(Rectangle rect) {
    return new MouRect(transform(new Point(rect.x, rect.y)),
        transform(new Point(rect.x + rect.width, rect.y + rect.height) ));

  }

  public BoundingRect transforToBounding(Rectangle rect) {
    Point p1 = new Point(rect.x, rect.y);
    Point p2 = new Point(rect.x + rect.width, rect.y + rect.height);
    Mou mou1 = transform(p1);
    Mou mou2 = transform(p2);
    // To je spravne, protoze souradnice jdou opacne
    BoundingRect br = new BoundingRect(mou1.xx, mou2.yy, mou2.xx, mou1.yy);
    return br;
  }



  public Rectangle transform(MouRect mourect) {
    Point p1 = transform(mourect.getSz());
    Point p2 = transform(mourect.getJv());
    Rectangle rect = new Rectangle(p1, new Dimension(p2.x - p1.x, p2.y - p1.y));
    //System.err.println("RORERO: " + rect + " --- " + mourect + " / " + p1 + "+" + p2);
    return rect;
  }

  public double pixleDalka(Mou mou1, Mou mou2) {
    Point p1 = transform(mou1);
    Point p2 = transform(mou2);
    double d = Math.hypot(p1.x - p2.x, p1.y - p2.y);
    return d;
  }

  public Point transform(Mou mou) {
    Point p = new Point();
    p.x = (mou.xx - getMoupoc().xx) / pomer ;
    p.y = dim.height - (mou.yy - getMoupoc().yy) / pomer;
    if (tam != null) {
      tam.transform(p, p);
    }
    return p;
  }



  /**
   * Spočítá nový moustred, který musí být pokud se zazůmuje na danou veliksot,
   * ab zůstal zadaný střed na svém místě obrazovky.
   */
  public Mou computeZoom(int newMoumer, Mou zoomMouStred) {
    if (newMoumer == moumer) return getMoustred();
    //    Point zommstred = zoomMouStred == null ? new Point(cur) : transform(zoomMouStred);
    // získat střed zoomování na obrazovce, podle tohoto zoomujeme
    Point pointZoomStred = zoomMouStred == null ? transform(getMoustred()) : transform(zoomMouStred); // střed zůmování v bodech
    Mou mouZoomStred = transform(pointZoomStred); // a střed v mou
    Coord c = derive(newMoumer); // pokusne tam nastavit nové měřítko
    Moud rozdil = c.transform(pointZoomStred).sub(mouZoomStred); // korigovat střed map, aby zůstal střed zůmování
    //System.out.println("CCCCCCCCCCCCCCCCCCC: " + rozdil + " " + pointZoomStred + " " + zoomMouStred);
    Mou newMoustred = getMoustred().sub(rozdil);
    return newMoustred;
  }



  public Mou getMoustred() {
    //Mou moustred = new Mou(moupoc.xx + width * pomer / 2, moupoc.yy + height * pomer / 2);
    return moustred;
  }

  public Mou getMoupoc() {
    Mou moupoc = new Mou(moustred.xx - dim.width * pomer / 2, moustred.yy - dim.height * pomer / 2);
    return moupoc;
  }

  public Mou getMouCur(Point cur) {
    return transform(cur);
  }

  public Moud getMouSize() {
    return new Moud(dim.width * pomer, dim.height * pomer);
  }

  public Mou getMouS() {
    return getMoustred().add(0, getMouSize().dyy / 2);
  }

  public Mou getMouSV() {
    return getMoupoc().add(getMouSize());
  }

  public Mou getMouV() {
    return getMoustred().add(getMouSize().dxx / 2, 0);
  }

  public Mou getMouJV() {
    return getMoupoc().add(getMouSize().dxx, 0);
  }

  public Mou getMouJ() {
    return getMoustred().add(0, - getMouSize().dyy / 2);
  }


  public Mou getMouJZ() {
    return getMoupoc();
  }

  public Mou getMouZ() {
    return getMoustred().add(- getMouSize().dxx / 2, 0);
  }

  public Mou getMouSZ() {
    return getMoupoc().add(0, getMouSize().dyy);
  }



  public int getWidth() {
    return dim.width;
  }

  public int getHeight() {
    return dim.height;
  }

  public int getPomer() {
    return pomer;
  }

  public int getMoumer() {
    return moumer;
  }

  public int getMoukrok() {
    //TODO To tady nemá být, nemá se zde vědět, jak jsou kchle velké
    return moukrok;
  }

  public BoundingRect getBoundingRect() {
    Mou jz = getMouJZ();
    Mou sv = getMouSV();
    BoundingRect boundingRect = new BoundingRect(jz.xx, jz.yy, sv.xx, sv.yy);
    return boundingRect;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Coord [moumer=" + moumer + ", moustred=" + moustred + ", dim=" + dim + ", natoceni=" + natoceni + "]";
  }


  /**
   * 
   */
  public double getMouboduNaMetr() {
    return getPixluNaMetr() * pomer;
  }

  /**
   * 
   */
  public double getPixluNaMetr() {
    Utm ujz = getMouJZ().toUtm();
    Utm usv = getMouSV().toUtm();
    double odKrajeKeKraji = Math.abs(ujz.ux - usv.ux);
    double pixluNaMetr = dim.width / odKrajeKeKraji;
    return pixluNaMetr;
  }

  public double getWidthMetru() {
    return dim.width / getPixluNaMetr();
  }

  public double getHeightMetru() {
    return dim.height / getPixluNaMetr();
  }


  public int getVzdalenostKachleOdStredu(Mou moupoc) {
    int mouHranaPul = getMoukrok() / 2;
    Mou moustredx = getMoustred();
    double hypot = Math.hypot(moupoc.xx + mouHranaPul - moustredx.xx,
        moupoc.yy + mouHranaPul - moustredx.yy);
    return (int) hypot;
  }

  /**
   * Vypočítá obdélník vyhovující souřadnicovému obdélníku a přidá nějaké pixly
   * na stranách podle incestu.
   * @param mourect
   * @param insets
   * @return
   */
  public Rectangle transform(MouRect mourect, Insets insets) {
    if (mourect == null) return null;

    Rectangle r = transform(mourect);
    Rectangle rect = new Rectangle(
        r.x - insets.left,
        r.y - insets.top,
        r.width + insets.left + insets.right,
        r.height + insets.top + insets.bottom);
    //System.out.println("Kompjuted rektangle: " + rect + " | " + r + " -- " + mourect);
    return rect;
  }

  /**
   * Úhel, o který je mapa v daném bodě pootočena oproti normálnímu směru na sever.
   * @return
   */
  public double computNataceciUhel() {
    if (getMouSize().isAnyRozmerEmpty()) return 0;
    final Mou mouS = getMouS();
    final Mou mouJ = getMouJ();
    final Wgs wgsS = mouS.toWgs();
    final Wgs wgsJ = mouJ.toWgs();
    final double lon = (wgsS.lon + wgsJ.lon) / 2; // stredova délka
    final Mou mouSq = new Wgs(wgsS.lat, lon).toMou();
    final Mou mouJq = new Wgs(wgsJ.lat, lon).toMou();
    double uhel = Math.atan( (double)(mouSq.xx - mouJq.xx) / (double)(mouSq.yy - mouJq.yy));
    return uhel;
  }

  /**
   * @return the dim
   */
  public Dimension getDim() {
    return dim;
  }



  /**
   * @return the natoceni
   */
  public double getNatoceni() {
    return natoceni;
  }

  private Coord create(int moumer, Mou moustred, Dimension dim, double natoceni) {
    if (moumer == moukrok
        && moustred.equals(this.moustred)
        && dim.equals(this.dim)
        && natoceni == this.natoceni
    ) return this; // vrátíme stejnou instanci, podle čehož se pozná, že nedošlo ke změně
    return new Coord(moumer, moustred, dim, natoceni);
  }



  public Coord derive(int moumer, Mou moustred, Dimension dim, double natoceni) {
    return create(moumer, moustred, dim, natoceni);
  }


  public Coord derive(int moumer, Mou moustred, Dimension dim) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(int moumer, Mou moustred, double natoceni) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(int moumer, Dimension dim, double natoceni) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(Mou moustred, Dimension dim, double natoceni) {
    return create(moumer, moustred, dim, natoceni);
  }

  public Coord derive(int moumer, Mou moustred) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(int moumer, Dimension dim) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(int moumer, double natoceni) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(Mou moustred, Dimension dim) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(Mou moustred, double natoceni) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(Dimension dim, double natoceni) {
    return create(moumer, moustred, dim, natoceni);
  }

  public Coord derive(int moumer) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(Mou moustred) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(Dimension dim) {
    return create(moumer, moustred, dim, natoceni);
  }
  public Coord derive(double natoceni) {
    return create(moumer, moustred, dim, natoceni);
  }



  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dim == null) ? 0 : dim.hashCode());
    result = prime * result + moumer;
    result = prime * result + ((moustred == null) ? 0 : moustred.hashCode());
    long temp;
    temp = Double.doubleToLongBits(natoceni);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }



  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Coord other = (Coord) obj;
    if (dim == null) {
      if (other.dim != null)
        return false;
    } else if (!dim.equals(other.dim))
      return false;
    if (moumer != other.moumer)
      return false;
    if (moustred == null) {
      if (other.moustred != null)
        return false;
    } else if (!moustred.equals(other.moustred))
      return false;
    if (Double.doubleToLongBits(natoceni) != Double.doubleToLongBits(other.natoceni))
      return false;
    return true;
  }



  public static Coord prozatimniInicializacni() {
    return new Coord(0, new Mou((MIN_MOU_X + MAX_MOU_X) / 2, (MIN_MOU_Y + MAX_MOU_Y) / 2),
        new Dimension(100,100), 0.0);
  }

}
