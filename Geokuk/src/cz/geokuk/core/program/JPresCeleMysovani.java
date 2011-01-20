/**
 * 
 */
package cz.geokuk.core.program;


import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.coordinates.Mou;

/**
 * Musí být úplně nahoře, je průhledná
 * a chytá události myši a případně je distribuuje.
 * Také zobrazuje myší kourzor.
 */
public final class JPresCeleMysovani extends JSingleSlide0 {

  private static final long serialVersionUID = 4979888007463850390L;

  private boolean posouvameMapu;


  /**
   * @param jKachlovnik
   */
  JPresCeleMysovani() {
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);

  }


  private Point bod;
  private VyrezModel vyrezModel;
  private Point cur;

  private PoziceModel poziceModel;


  /**
   * Invoked when a mouse button has been pressed on a component.
   */
  @Override
  public void mousePressed(MouseEvent e) {
    bod = e.getPoint();
    if (!e.isShiftDown()) {
      posouvameMapu = true;
      setMouseCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    //System.out.println("UDALOST " + e);
    //System.out.println("Kliknuti mysi v pozici: " + getCoord().getMouCur());
    chain().mousePressed(e);
    requestFocus();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    posouvameMapu = false;
    setMouseCursor(null);
    chain().mouseReleased(e);
    //zoomovaciObdelnik.mouseReleased(e);
  }


  @Override
  public void mouseDragged(MouseEvent e) {
    if (posouvameMapu) {
      cur = e.getPoint();
      int dx = cur.x - bod.x;
      int dy = cur.y - bod.y;

      Mou mou = getSoord().getMoustred();
      Mou moustred = new Mou(mou.xx + -dx * getSoord().getPomer(), mou.yy + dy * getSoord().getPomer());
      //getCoord().setMoustredNezadouci(moustred);
      vyrezModel.setMoustred(moustred);
      bod = cur;
    }
    chain().mouseDragged(e);
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    cur = e.getPoint();
    Mou mouCur = getSoord().getMouCur(cur);
    poziceModel.setMys(cur, mouCur);
    //System.out.println("Souradnice: " + wgs);
    chain().mouseMoved(e);
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    int rotation  = e.getWheelRotation();
    //			System.out.println("Rotace: " + rotation);
    int moumer = getSoord().getMoumer() - rotation;
    Mou mou = getUpravenaMys();
    vyrezModel.zoomByGivenPoint(moumer, mou);
    requestFocus();

  }


  public void inject(VyrezModel vyrezModel) {
    this.vyrezModel = vyrezModel;
  }


  public void inject(PoziceModel poziceModel) {
    this.poziceModel = poziceModel;
  }


}