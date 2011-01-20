package cz.geokuk.plugins.mrizky;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.BeanSubtypable;
import cz.geokuk.framework.BeanSubtype;



public abstract class JMrizka0 extends JSingleSlide0 implements BeanSubtypable {


  private static final long serialVersionUID = -5858146658366237217L;


  private final Stroke slabe =  new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
      new float[]{2.0f, 4.0f}  , 0.0f);
  private final Stroke silne =  new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);


  public JMrizka0() {
    setOpaque(false);
    setCursor(null);
  }

  public abstract void initPainting(Vykreslovac aVykreslovac);

  public void donePainting() {}

  public abstract Mou convertToMou(double x, double y);

  public abstract double convertToX(Mou mou);
  public abstract double convertToY(Mou mou);

  private Point convert(double x, double y) {
    Point result = getSoord().transform(convertToMou(x, y));
    return result;
  }


  public abstract String getTextX(double x);

  public abstract String getTextY(double x);


  /* (non-Javadoc)
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent(Graphics aG) {
    Vykreslovac vykreslovac = new Vykreslovac();
    initPainting(vykreslovac);
    vykreslovac.paint(aG);
    donePainting();
  }

  protected class Vykreslovac {

    private static final int PIXLMEZ = 50;

    private double x0;
    private double y0;
    private double xkrok;
    private double ykrok;

    private int tlustsiX;
    private int tlustsiY;

    private Color color = Color.BLACK;

    void paint(Graphics aG) {
      if (ykrok == 0 || ykrok == 0) return; // at se nezacykli
      Graphics2D g = (Graphics2D) aG;
      g.setColor(color);
      //      if (true) return;

      korigujZnamenkaKroku();
      posunX0Y0ZaKonec();
      // příprava prvního řádku
      List<Point> a = new ArrayList<Point>();
      //Point p = convert(x0, y0);
      {
        double x = x0;
        Point p;
        do {
          p = convert(x, y0);
          a.add(p);
          x -= xkrok;
        } while (p.x >= 0 - 100); // přidáme něco ať při otočení nezmizí
        a.add(convert(x, y0));  // a ještě jeden
      }

      double y = y0;
      do {
        y -= ykrok;
        List<Point> b = new ArrayList<Point>(a.size());
        b.add(convert(x0, y));
        double x = x0 - xkrok;
        for (int i=1; i<a.size(); i++) {
          Point p = a.get(i);
          Point p1 = a.get(i-1);
          Point p2 = convert(x, y);
          g.setStroke(naSilneY(y + ykrok) ? silne : slabe);
          g.drawLine(p.x, p.y , p1.x, p1.y);
          g.setStroke(naSilneX(x) ? silne : slabe);
          g.drawLine(p.x, p.y , p2.x, p2.y);
          if (p2.y < 0 && p.y >= 0) { // dobrý čas zobrazit text
            //g.transform(AffineTransform.getRotateInstance(Math.PI / 1,35));
            g.drawString(getTextX(x + xkrok), p.x + 3, 35);
          }
          if (p2.y < getHeight() && p.y >= getHeight()) { // dobrý čas zobrazit text
            g.drawString(getTextX(x + xkrok), p.x + 3, getHeight() - 10);
          }
          if (p.x < 0 && p1.x >= 0) { // dobrý čas zobrazit text
            g.drawString(getTextY(y + ykrok), 7, p.y - 5);
          }
          if (p.x < getWidth() && p1.x >= getWidth()) { // dobrý čas zobrazit text
            g.drawString(getTextY(y + ykrok), getWidth() - 60, p.y - 5);
          }
          b.add(p2);
          x -= xkrok;
        }
        a = b; // tam ta se stává referenční
        // vykreslení řádku
      } while (convert(x0, y).y >= 0 - 100);  // přidáme něco, ať nezmizí při otočení

      // a teď texty
    }

    /* (non-Javadoc)
     * @see mrizka.JMrizka0#initPainting(coordinates.Mou)
     */
    private int convertVzdalenostMensi(double vzdalenost) {
      Mou moustred = getSoord().getMoustred();
      double x1 = convertToX(moustred);
      double y1 = convertToY(moustred);
      double x2 = x1 + vzdalenost;
      double y2 = y1 + vzdalenost;
      Point p1 = convert(x1, y1);
      Point p2 = convert(x2, y2);
      int dx = Math.abs(p1.x - p2.x);
      int dy = Math.abs(p1.x - p2.x);
      int d = Math.min(dx, dy);

      //      lat = Math.round(lat / krok) * krok;
      //      lon = Math.round(lon / krok) * krok;
      return d;
    }

    protected final void setColor(Color color) {
      this.color = color;
    }

    protected final void rastr(double krok, int tlustsi) {
      if (xkrok > 0) return; // už bylo nastaveno
      int d = convertVzdalenostMensi(krok);
      if (d < PIXLMEZ) return; // to je ještě málo;
      this.xkrok = krok;
      this.ykrok = krok;
      this.tlustsiX = tlustsi;
      this.tlustsiY = tlustsi;
      Mou moustred = getSoord().getMoustred();
      x0 = Math.round(convertToX(moustred) / krok) * krok;
      y0 = Math.round(convertToY(moustred) / krok) * krok;
      //System.out.println("KROK A SILA:  " +  tlustsi + " | " + krok);
    }

    private boolean naSilneX(double x) {
      boolean result = Math.round(x / xkrok) % tlustsiX == 0;
      return result;
    }

    private boolean naSilneY(double y) {
      boolean result = Math.round(y / ykrok) % tlustsiY == 0;
      return result;
    }

    /**
     * 
     */
    private void posunX0Y0ZaKonec() {
      int width = getWidth();
      while(convert(x0, y0).x < width) {
        x0 += xkrok;
      }
      x0 += xkrok * 2; // a ještě jeden pro jistotu
      int height = getHeight();
      while(convert(x0, y0).y < height) {
        y0 += ykrok;
      }
      y0 += ykrok * 2; // a ještě jeden pro jistotu
    }

    /**
     * 
     */
    private void korigujZnamenkaKroku() {
      Point p = convert(x0, y0);
      Point r = convert(x0 + xkrok, y0 + ykrok);
      if (r.x < p.x) xkrok = - xkrok;
      if (r.y < p.y) ykrok = - ykrok;
    }

  }


  private String kterouMamMrizku() {
    String kn = getClass().getName();
    int poz = kn.indexOf("JMrizka");
    String result = kn.substring(poz + "JMrizka".length());
    return result;
  }

  @BeanSubtype
  public void onEvent(MrizkaEvent event) {
    setVisible(event.onoff);
  }

  @Override
  public String getSubType() {
    return kterouMamMrizku();
  }

  //  @Override
  //  public void finalize() {
  //    System.out.println("Mřížky finalizovány");
  //  }

}
