/**
 * 
 */
package cz.geokuk.core.coord;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.MouseGestureContext;


/**
 * @author veverka
 *
 */
public class JZoomovaciObdelnik extends JSingleSlide0  {

  private static final long serialVersionUID = -4801191981059574701L;

  private Point pocatek;

  private Point konec;

  private VyrezModel vyrezModel;

  public JZoomovaciObdelnik() {
    setPreferredSize(new Dimension(1600, 40));
    setOpaque(false);
  }

  @Override
  protected void paintComponent(Graphics aG) {
    Graphics2D g = (Graphics2D) aG;
    Rectangle rect = getRectangle();
    if (rect != null) {

      Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
          new float[] { 3, 3 }, 0);
      g.setStroke(stroke);
      g.drawRect(rect.x, rect.y, rect.width, rect.height);
      g.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
      g.drawLine(rect.x + rect.width, rect.y, rect.x, rect.y + rect.height);


      g.setColor(Color.YELLOW);
      stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
          new float[] { 3, 3 }, 3);
      g.setStroke(stroke);
      g.drawRect(rect.x, rect.y, rect.width, rect.height);
      g.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);
      g.drawLine(rect.x + rect.width, rect.y, rect.x, rect.y + rect.height);

      Mou stred = getSoord().transform(getRectangle()).getStred();
      Point p = getSoord().transform(stred);
      g.fillOval(p.x-5, p.y-5, 10, 10);
      //  		Rectangle r = getRectangle();
      //
      //  		p = new Point(r.x + r.width /2, r.y + r.height /2);
      //  		g.setColor(Color.BLUE);
      //  		g.fillOval(p.x-5, p.y-5, 10, 10);
      //
      //  		p = coord.transform(coord.transform(p));
      //  		g.setColor(Color.MAGENTA);
      //  		g.fillOval(p.x-5, p.y-5, 10, 10);
      //
      //  		p = coord.transform(coord.transform(getRectangle()).sstre);
      //  		g.setColor(Color.GREEN);
      //  		g.fillOval(p.x-5, p.y-5, 10, 10);

    }
  }

  private Rectangle getRectangle() {
    if (pocatek == null || konec == null) return null;
    return new Rectangle(
        Math.min(pocatek.x, konec.x),
        Math.min(pocatek.y, konec.y),
        Math.abs(pocatek.x - konec.x),
        Math.abs(pocatek.y - konec.y));
  }


  @Override
  public void mousePressed(MouseEvent e, MouseGestureContext ctx) {
    if (e.isShiftDown()) {
      pocatek = e.getPoint();
    }
    chain().mousePressed(e, ctx);
    //	System.out.println("MYS stisknuta: " + e);
  }

  @Override
  public void mouseReleased(MouseEvent e, MouseGestureContext ctx) {
    if (e.isShiftDown() && getRectangle() != null) {
      //	System.out.println("Budeme zoomovat na " + getRectangle());
      vyrezModel.zoomTo(getSoord().transform(getRectangle()));
    }
    changeEndPoint(null);
    chain().mouseReleased(e, ctx);
  }

  @Override
  public void mouseDragged(MouseEvent e, MouseGestureContext ctx) {
    Point point = e.getPoint();
    if (! e.isShiftDown()) {
      point = null;
    }
    changeEndPoint(point);
    chain().mouseDragged(e, ctx);
  }

  private void changeEndPoint(Point point) {
    Rectangle rect = getRectangle();
    if (rect != null) {
      rect.width ++;
      rect.height ++;
      repaint(rect); // na starém už nebude
    }
    konec = point;
    if (konec == null) {
      pocatek = null;
    }
    rect = getRectangle();
    if (rect != null) {
      rect.width ++;
      rect.height ++;
      repaint(rect); // na novém bude
    }
  }

  public void inject(VyrezModel vyrezModel) {
    this.vyrezModel = vyrezModel;
  }

}

