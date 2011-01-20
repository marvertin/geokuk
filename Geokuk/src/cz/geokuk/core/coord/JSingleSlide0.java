/**
 * 
 */
package cz.geokuk.core.coord;

import java.awt.Cursor;
import java.awt.Graphics;

import javax.swing.JFrame;

import cz.geokuk.core.program.MainFrameHolder;
import cz.geokuk.framework.Factory;
import cz.geokuk.framework.JSlide0;

/**
 *  Předek všech jednoduchých, tedy nesložených slidů
 */
public abstract class JSingleSlide0 extends JSlide0 {

  private static final long serialVersionUID = 8758817189971703053L;

  private Coord soord;

  private MainFrameHolder mainFrameHolder;

  protected Factory factory;

  protected boolean isSoordInitialized() {
    return soord != null;
  }

  /**
   * @return the coord
   */
  public Coord getSoord() {
    assert soord != null;
    return soord;
  }

  /**
   * @param coord the coord to set
   */
  public void setSoord(Coord soord) {
    assert soord != null;
    this.soord = soord;
  }


  protected void onVyrezChanged() {
  }

  //  protected final void onEvent(VyrezChangedEvent0 event ) {
  //
  //  }

  protected void setMouseCursor(Cursor cursor) {
    getMainFrame().setCursor(cursor);
  }

  private JFrame getMainFrame() {
    return mainFrameHolder.getMainFrame();
  }

  public void inject(MainFrameHolder mainFrameHolder) {
    this.mainFrameHolder = mainFrameHolder;
  }

  public void inject(Factory factory) {
    this.factory = factory;
  }


  public void render(Graphics g) throws InterruptedException {
    paintComponent(g);
  }

  /**
   * Potomek musí vytvořit novou instanci slidu, který bude rendrovatelný.
   * Obvykle postačí, když vytvoří prázdnou instanci své vlastní třídy.
   * Pokud vrátí null, nic se rendrovat nebude
   * @return
   */
  public JSingleSlide0 createRenderableSlide() {
    return null;
  }

  public EJakOtacetPriRendrovani jakOtacetProRendrovani() {
    return EJakOtacetPriRendrovani.GRAPH2D;
  }

}
