/**
 * 
 */
package index2dguitest;


import java.awt.Color;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Ctverecnik;
import cz.geokuk.util.index2d.Indexator;
import cz.geokuk.util.index2d.Sheet;
import cz.geokuk.util.index2d.Visitor;

/**
 * @author veverka
 *
 */
public class JBody extends JPanel {

  private static final long serialVersionUID = 4495019710958865432L;

  private static final boolean ZOBRAZIT_JEDNOTLIVE = true;

//  private static int POCET = 200000;
//  private static int VIRTUALNIVELIKOST = 3000;
//  private int OFFSETX = VIRTUALNIVELIKOST / 3;
//  private int OFFSETY = VIRTUALNIVELIKOST / 4;

  private static int POCET = 200000;
  private static int VIRTUALNIVELIKOST = 30000;
  private int OFFSETX = VIRTUALNIVELIKOST / 3;
  private int OFFSETY = VIRTUALNIVELIKOST / 4;

  private final Set<Point> body = new HashSet<Point>();
  private final Indexator<Point> indexator;
  
  private Rectangle vyber;
  
  private int posunovaciX;
  private int posunovaciY;
  
  public JBody() {
    indexator = new Indexator<Point>(new BoundingRect(0, 0, VIRTUALNIVELIKOST, VIRTUALNIVELIKOST));
    body.clear();
    for (int i=0; i<=POCET; i++) {
      //        Dimension size = getSize();
      //        Insets insets = getInsets();
      //
      //        int w =  size.width - insets.left - insets.right;
      //        int h =  size.height - insets.top - insets.bottom;

      int x = (int) (Math.random() * VIRTUALNIVELIKOST);
      int y = (int) (Math.random() * VIRTUALNIVELIKOST);
      Point point = new Point(x, y);
      body.add(point);
      indexator.vloz(x, y, point);
    }
    
    addComponentListener(new ComponentAdapter() {
      /* (non-Javadoc)
       * @see java.awt.event.ComponentAdapter#componentResized(java.awt.event.ComponentEvent)
       */
      @Override
      public void componentResized(ComponentEvent e) {
        repaint();
      }
    });
    addMouseListener(new Mysovani());
    addMouseMotionListener(new Mysovani());
  }
  
  /* (non-Javadoc)
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent(Graphics aG) {
    super.paintComponent(aG);
    final Graphics2D g = (Graphics2D) aG;

    BoundingRect boundingAll = new BoundingRect(OFFSETX, OFFSETY,
        getWidth() + OFFSETX, getHeight() + OFFSETY);

//    for (Point bod : body) {
//      g.drawLine(bod.x, bod.y, bod.x, bod.y);
//    }
    if (ZOBRAZIT_JEDNOTLIVE) {
      for (Sheet<Point> sheet : indexator.deepList(boundingAll)) {
        Point bod = sheet.get();
        drawCircle(g, bod.x - OFFSETX, bod.y - OFFSETY, 7);
      }
    }
    if (vyber != null) {
      g.drawRect(vyber.x, vyber.y, vyber.width, vyber.height);
      BoundingRect bounding = new BoundingRect(vyber.x + OFFSETX, vyber.y + OFFSETY,
                                   vyber.x + vyber.width  + OFFSETX, vyber.y + vyber.height  + OFFSETY);
      
      indexator.visit(bounding, new Visitor<Point>() {
        
        @Override
        public void visit(Ctverecnik<Point> c) {
          g.setColor(Color.BLUE);
          int x = c.getXx1() - OFFSETX;
          int y = c.getYy1() - OFFSETY;
          g.drawRect(x, y, c.getXx2() - c.getXx1(), c.getYy2() - c.getYy1());
          g.drawString(c.getCount() + "", x+3, y+12);
        }
        
        @Override
        public void visit(Sheet<Point> aSheet) {
          g.setColor(Color.BLUE);
          fillCircle(g, aSheet.getXx() - OFFSETX, aSheet.getYy() - OFFSETY, 5);
        }
      });

      if (ZOBRAZIT_JEDNOTLIVE) {
        for (Sheet<Point> sheet : indexator.deepList(bounding)) {
          Point bod = sheet.get();
          g.setColor(Color.GREEN);
          fillCircle(g, bod.x - OFFSETX, bod.y - OFFSETY, 3);
        }
      }
      
    }
  }

  /**
   * @param aG
   * @param aX
   * @param aY
   * @param aI
   */
  private void drawCircle(Graphics2D g, int x, int y, int r) {
    g.drawOval(x - r, y - r, 2*r, 2*r);
  }

  /**
   * @param aG
   * @param aX
   * @param aY
   * @param aI
   */
  private void fillCircle(Graphics2D g, int x, int y, int r) {
    g.fillOval(x - r, y - r, 2*r, 2*r);
  }
  
  private final class Mysovani implements MouseInputListener {


    @Override
    public void mouseReleased(MouseEvent e) {
      //System.out.println("UDALOST " + e);
      //        if (e.getButton() == MouseEvent.BUTTON3) {
      //            iMenu.show(e.getComponent(), e.getX(), e.getY());
      //        }
      //vyber = null;
      repaint();

    }

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
      //System.out.println("UDALOST " + e);
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    @Override
    public void mousePressed(MouseEvent e) {
//      bod = e.getPoint();
      if ((e.getModifiers() & Event.CTRL_MASK) == 0) {
        vyber = new Rectangle(e.getX(), e.getY(), 0, 0);
        System.out.println("PO STISKU: " + vyber + " " + e.getX() + " " + e.getY());
        repaint();
        System.out.println("UDALOST " + e);
      }
      posunovaciX = e.getX();
      posunovaciY = e.getY();
    }

    /**
     * Invoked when the mouse enters a component.
     */
    @Override
    public void mouseEntered(MouseEvent e) {
      System.out.println("UDALOST " + e);
    }

    /**
     * Invoked when the mouse exits a component.
     */
    @Override
    public void mouseExited(MouseEvent e) {
      System.out.println("UDALOST " + e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      if ((e.getModifiers() & Event.CTRL_MASK) != 0) {
        System.out.println("posun" + e);
        OFFSETX -= e.getX() - posunovaciX;
        OFFSETY -= e.getY() - posunovaciY;
        posunovaciX = e.getX();
        posunovaciY = e.getY();
      } else {
        System.out.println(vyber);
        vyber.setSize(e.getX() - vyber.x, e.getY() - vyber.y);
      }
      repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      //System.out.println("POSUNOVACKA TO ASI BUDE: " + e);
    }
  }
  
}
