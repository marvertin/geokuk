package cz.geokuk.framework;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

public class FKurzory {

  public static final Cursor TEXTOVY_KURZOR = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);

  public static final Cursor KAM_SE_DA_KLIKNOUT = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);



  public static final Cursor POSOUVANI_MAPY = new PohybMapou().createCursor();

  public static final Cursor PRIDAVANI_BODU = new PridavaniBodu(Color.WHITE, true).createCursor();

  /////// Další kurzory jsou nad mapou

  /// Kurzory bez nějaké akce, jen jsem nad něčím

  public static final Cursor STANDARDNI_MAPOVY_KRIZ = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);

  /** Stojím nad waypointem jen tak s myší */
  public static final Cursor NAD_WAYPOINTEM = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  /** Když draguje bod Bod nějaké cesty tak jsem vždy blízko */
  public static final Cursor BLIZKO_BOUSKU_NORMAL = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  /// Dragovací či jinak akční kurzory
  /** Jsem blízko bousku a nemám stisknuto */
  public static final Cursor BLIZKO_BOUSKU_DRAGOVANI = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

  /** Táhnu bod a jsem nad waypointem, takže když ho pustím, spadne na něj */
  public static final Cursor NAD_WAYPOINTEM_DRAGOVANI_BODU =  new CtyrSipka().createCursor();

  /** Přidávám bod (držím CTRL, takže když sem spadne bude bod */
  public static final Cursor NAD_WAYPOINTEM_PRIDAVANI_BODU = new PridavaniBodu(Color.GREEN, false).createCursor();



  private static class CtyrSipka extends KurzorovyMatlak0 {

    public CtyrSipka() {
    }

    @Override
    void nakresli() {
      vystreduj();

      //      Area a1 = new Area([triangle 0,0 => 8,0 => 0,8]);
      //      Area a2 = new Area([triangle 0,0 => 8,0 => 8,8]);
      //      a1.add(a2);
      Area poly = new Area (new Polygon(
          new int[] { 0, 1, 1, 5, 0},
          new int[] { 0, 1, 7, 7, 12}, 5));
      Area a = new Area();

      AffineTransform rotace = new AffineTransform();
      rotace.rotate(Math.PI / 2);
      AffineTransform zrcadlo = new AffineTransform();
      zrcadlo.scale(1, -1);
      for (int i=0; i<4; i++) {
        a.transform(zrcadlo);
        a.add(poly);
        a.transform(zrcadlo);
        a.add(poly);
        a.transform(rotace);
      }
      g.setColor(Color.GREEN);
      g.fill(a);
      g.setColor(Color.BLACK);
      g.draw(a);
    }
  }

  private static class PohybMapou extends KurzorovyMatlak0 {

    public PohybMapou() {
    }

    @Override
    void nakresli() {
      vystreduj();

      Polygon poly = new Polygon(new int[] { 1, 3,  3, 8, 1},
          new int[] { 5, 5, 10,10,15}, 5);
      for (int i=0; i<4; i++) {
        g.scale(1, -1);
        g.setColor(Color.WHITE);
        g.fillPolygon(poly);
        g.setColor(Color.BLACK);
        g.drawPolygon(poly);
        g.scale(1, -1);
        g.setColor(Color.WHITE);
        g.fillPolygon(poly);
        g.setColor(Color.BLACK);
        g.drawPolygon(poly);
        g.rotate(Math.PI / 2);
      }
    }
  }

  private static class PridavaniBodu extends KurzorovyMatlak0 {

    private final Color vnitrek;
    private final boolean zamernyKriz;

    public PridavaniBodu(Color vnitrek, boolean zamernyKriz) {
      this.vnitrek = vnitrek;
      this.zamernyKriz = zamernyKriz;
    }

    @Override
    void nakresli() {
      vystreduj();
      int kr = 3;
      int in = 11;
      int ex = 14;

      Polygon poly = new Polygon(new int[] { kr, ex, ex, in, in, kr},
          new int[] { ex, ex, kr, kr, in, in}, 6);
      for (int i=0; i<4; i++) {
        g.setColor(vnitrek);
        g.fillPolygon(poly);
        g.setColor(Color.BLACK);
        g.drawPolygon(poly);
        g.rotate(Math.PI / 2);
      }

      //g.setXORMode(Color.WHITE);
      if (zamernyKriz) {
        g.setColor(vnitrek);
        int kriz = 6;
        g.drawLine(-kriz, -kriz, kriz, kriz);
        g.drawLine(-kriz, kriz, kriz, -kriz);
      }

    }
  }


  private abstract static class KurzorovyMatlak0 {
    BufferedImage img;
    Graphics2D g;
    int w;
    int h;
    int ws; // stře
    int hs; // střed

    abstract void nakresli();

    protected final void vystreduj() {
      g.translate(w / 2, h / 2);
      ws = w / 2;
      hs = h / 2;
    }

    final Cursor createCursor() {
      novy();
      nakresli();
      return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(ws, hs), "Kurzor");
    }

    private void novy() {
      Dimension bestCursorSize = Toolkit.getDefaultToolkit().getBestCursorSize(300, 300);
      //      System.out.println(bestCursorSize);
      img = new BufferedImage(bestCursorSize.width, bestCursorSize.height, BufferedImage.TYPE_4BYTE_ABGR);
      w = img.getWidth();
      h = img.getHeight();
      g = img.createGraphics();
    }
  }

}
