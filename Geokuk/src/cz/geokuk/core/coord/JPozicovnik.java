package cz.geokuk.core.coord;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.Factory;

public class JPozicovnik extends JSingleSlide0 {


  /**
   * 
   */
  private static final int R_KRIZE = 50;

  /**
   * 
   */
  private static final int R_VNEJSI_KRUZNICE = 30;

  /**
   * 
   */
  private static final int R_VNITRNI_KRUZNICE = 20;

  private static final long serialVersionUID = -5858146658366237217L;



  private Pozice pozice = new Pozice();

  private boolean mysJePoblizKrize;

  private PoziceModel poziceModel;

  private VyrezModel vyrezModel;

  private Factory factory;

  private Point pointcur;


  public JPozicovnik() {
    setOpaque(false);
    setCursor(null);
    //Board.eveman.registerWeakly(this);
  }

  public void onEvent(PoziceChangedEvent aEvent) {
    if (pozice.equals(aEvent.pozice)) return;
    pozice = aEvent.pozice;
    prepocitatBlizkostKrize();
    repaint();
  }

  public void onEvent(ZmenaSouradnicMysiEvent aEvent) {
    pointcur = aEvent.pointcur;
    prepocitatBlizkostKrize();
  }

  private void prepocitatBlizkostKrize() {
    if (! pozice.isNoPosition()) {
      double dalka = getSoord().pixleDalka(getSoord().getMouCur(pointcur), pozice.getWgs().toMou());
      boolean pobliz = dalka < 20;
      if (pobliz != mysJePoblizKrize) {
        mysJePoblizKrize = pobliz;
        repaintKriz();
      }
    }
  }

  /**
   * 
   */
  private void repaintKriz() {
    if (pozice.isNoPosition()) return; // není co kreslit
    Mou mou = pozice.getWgs().toMou();
    Point p = getSoord().transform(mou);
    repaint(p.x - R_KRIZE, p.y - R_KRIZE, R_KRIZE * 2, R_KRIZE * 2);
  }

  @Override
  public void paintComponent(Graphics aG) {

    if (pozice.isNoPosition()) return; // není co kreslit
    Mou mou = pozice.getWgs().toMou();
    Point p = getSoord().transform(mou);
    int ra = R_VNITRNI_KRUZNICE;
    int rb = R_VNEJSI_KRUZNICE;
    int rk = R_KRIZE;

    Graphics2D g = (Graphics2D) aG;
    g.setStroke(new BasicStroke(mysJePoblizKrize ? 3 : 1));
    g.setColor(Color.RED);
    g.drawLine(p.x - rk, p.y, p.x + rk, p.y);
    g.drawLine(p.x, p.y - rk, p.x, p.y + rk);
    g.drawOval(p.x - ra, p.y - ra, 2 * ra, 2 * ra);
    g.drawOval(p.x - rb, p.y - rb, 2 * rb, 2 * rb);
  }

  /**
   * Vrátí pozici nmyši, pokud je v blízkosti kříže
   * @return
   */
  @Override
  public Mou getUpravenaMys() {
    if (mysJePoblizKrize) {
      return pozice.getWgs().toMou();
    } else {
      return chain().getUpravenaMys();
    }
  }

  /**
   * Invoked when the mouse has been clicked on a component.
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    //System.out.println("UDALOST " + e);
    //kesky.mouseClicked(e);
    //if (e.isConsumed()) return;
    if (SwingUtilities.isRightMouseButton(e)) {
      //Board.eveman.fire(new PoziceChangedEvent(new Pozice(), false));
      poziceModel.clearPozice();
    } else {
      boolean vystredovat = e.getClickCount() >= 2;
      poziceModel.setPozice(getSoord().getMouCur(e.getPoint()).toWgs());
      if (vystredovat) {
        vyrezModel.vystredovatNaPozici();
      }
    }
    chain().mouseClicked(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    maybeShowPopup(e);
    chain().mousePressed(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    maybeShowPopup(e);
    chain().mouseReleased(e);
  }

  /**
   * @param aE
   */
  private void maybeShowPopup(MouseEvent e) {
    if (e.isPopupTrigger() && mysJePoblizKrize) {
      JPopupMenu popup = factory.init(new JPozicovnikPopup(pozice.getWgs()));
      if (popup != null) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }

  }

  public void inject(PoziceModel poziceModel) {
    this.poziceModel = poziceModel;
  }
  public void inject(VyrezModel vyrezModel) {
    this.vyrezModel = vyrezModel;
  }
  @Override
  public void inject(Factory factory) {
    this.factory = factory;
  }

}
