/**
 * 
 */
package cz.geokuk.api.mapicon;

import java.awt.Graphics2D;


/**
 * @author veverka
 *
 */
public class RectDrawer extends ShapeDrawer {

	@Override
  protected void fill(Graphics2D g) {
	  g.fillRect(0, 0, getWidth(), getHeight());
  }

  
	@Override
  protected void draw(Graphics2D g) {
	  g.drawRect(0, 0,	getWidth() - 1, getHeight() - 1);
  }

  

  
}
