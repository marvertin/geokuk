/**
 * 
 */
package cz.geokuk.api.mapicon;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;


/**
 * @author veverka
 *
 */
public abstract class ShapeDrawer extends PaintingDrawer0 {


  /* (non-Javadoc)
   * @see geokuk.mapicon.Vykreslovac#draw(geokuk.mapicon.VykreslovaciKontext, java.util.List)
   */
  @Override
	protected void drawImage(Graphics2D g) {
  	
	  g.setColor(getFillColor());
	  fill(g);
	  if (getLineWidth() > 0) {
		  g.setColor(getDrawColor());
		  g.setStroke(new BasicStroke(getLineWidth()));
		  draw(g);
	  }
  }

	private Color getDrawColor() {
	  return getColor("draw", Color.BLACK);
  }

	protected final Color getFillColor() {
	  return getColor("fill", Color.GRAY);
  }

	protected final int getLineWidth() {
	  return getInt("lineWidth", 1);
  }
	
  protected abstract void fill(Graphics2D g);
  
  protected abstract void draw(Graphics2D g);
  
}
