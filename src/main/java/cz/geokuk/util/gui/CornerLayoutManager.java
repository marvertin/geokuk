/**
 * 
 */
package cz.geokuk.util.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;

/**
 * @author veverka
 *
 */
public class CornerLayoutManager implements LayoutManager2 {

  private static final float ratiox = 0.25f;
  private static final float ratioy = 0.33f;
  
  private Component sz;
  private Component sv;
  private Component jz;
  private Component jv;

  private Component podklad;

  /* (non-Javadoc)
   * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
   */
  @Override
  public void addLayoutComponent(Component c, Object o) {
    ERoh roh = (ERoh) o;
    if (roh == null) {
      podklad = c;
    } else {
      switch (roh) {
      case SZ: sz = c; break;
      case SV: sv = c; break;
      case JZ: jz = c; break;
      case JV: jv = c; break;
      default: podklad = c;
      }
    }
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
   */
  @Override
  public float getLayoutAlignmentX(Container aArg0) {
    return 0;
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
   */
  @Override
  public float getLayoutAlignmentY(Container aArg0) {
    return 0;
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
   */
  @Override
  public void invalidateLayout(Container aArg0) {
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
   */
  @Override
  public Dimension maximumLayoutSize(Container c) {
    return podklad.getMinimumSize();
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
   */
  @Override
  public void addLayoutComponent(String s, Component c) {
    addLayoutComponent(c, s);
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
   */
  @Override
  public void layoutContainer(Container c) {
    Insets in = c.getInsets();
    int x1 = in.left;
    int x2 = c.getWidth() - in.right;
    int y1 = in.top;
    int y2 = c.getHeight() - in.bottom;
    Dimension dim = new Dimension(x2-x1, y2-y1);

    //System.out.println(dim + " " + c.getWidth() + " " + c.getHeight());
    doRohu(sz, dim, x1, y1);
    doRohu(jz, dim, x1, -y2);
    doRohu(sv, dim, -x2, y1);
    doRohu(jv, dim, -x2, -y2);
    
    podklad.setBounds(x1, y1, dim.width, dim.height);
    
  }

  private void doRohu(Component cc, Dimension dim, int x, int y) {
    if (cc != null) {
      Dimension v = rozmer(dim, cc);
      int x0 = x < 0 ? -v.width - x : x;
      int y0 = y < 0 ? -v.height - y : y;
      cc.setBounds(x0, y0, v.width, v.height);
      //System.out.println(x0 + " " + y0 + v);
    }
    
  }
  /**
   * @param dim
   * @param cc
   */
  private Dimension rozmer(Dimension dim, Component cc) {
    Dimension prs = cc.getPreferredSize();
    Dimension mis = cc.getMinimumSize();
    Dimension mas = cc.getMaximumSize();
    
    int dx = rozmer(ratiox, dim.width, prs.width, mis.width, mas.width);
    int dy = rozmer(ratioy, dim.height, prs.height, mis.height, mas.height);
    Dimension v = new Dimension(dx, dy);
    return v;
  }
  
  private int rozmer(float ratio, int size, int pr, int mi, int ma) {
    int mypr = (int) (size * ratio);
    int v = Math.min(mypr, pr);
    v = Math.max(v, mi);
    v = Math.min(v, ma);
    v = Math.min(v, size); // a omezit prostorem, ktery mame
    return v;
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
   */
  @Override
  public Dimension minimumLayoutSize(Container aArg0) {
    return podklad.getMaximumSize();
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
   */
  @Override
  public Dimension preferredLayoutSize(Container aArg0) {
    return podklad.getPreferredSize();
  }

  /* (non-Javadoc)
   * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
   */
  @Override
  public void removeLayoutComponent(Component c) {
    if (sz == c) sz = null;
    if (sv == c) sv = null;
    if (jz == c) jz = null;
    if (jv == c) jv = null;
    if (podklad == c) podklad = null;
  }

}
