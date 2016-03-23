/**
 *
 */
package cz.geokuk.util.gui;

import java.awt.*;

/**
 * @author veverka
 *
 */
public class CornerLayoutManager implements LayoutManager2 {

	private static final float	ratiox	= 0.25f;
	private static final float	ratioy	= 0.33f;

	private Component			sz;
	private Component			sv;
	private Component			jz;
	private Component			jv;

	private Component			podklad;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
	 */
	@Override
	public void addLayoutComponent(final Component c, final Object o) {
		final ERoh roh = (ERoh) o;
		if (roh == null) {
			podklad = c;
		} else {
			switch (roh) {
			case SZ:
				sz = c;
				break;
			case SV:
				sv = c;
				break;
			case JZ:
				jz = c;
				break;
			case JV:
				jv = c;
				break;
			default:
				podklad = c;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
	 */
	@Override
	public float getLayoutAlignmentX(final Container aArg0) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
	 */
	@Override
	public float getLayoutAlignmentY(final Container aArg0) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
	 */
	@Override
	public void invalidateLayout(final Container aArg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension maximumLayoutSize(final Container c) {
		return podklad.getMinimumSize();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 */
	@Override
	public void addLayoutComponent(final String s, final Component c) {
		addLayoutComponent(c, s);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	@Override
	public void layoutContainer(final Container c) {
		final Insets in = c.getInsets();
		final int x1 = in.left;
		final int x2 = c.getWidth() - in.right;
		final int y1 = in.top;
		final int y2 = c.getHeight() - in.bottom;
		final Dimension dim = new Dimension(x2 - x1, y2 - y1);

		// System.out.println(dim + " " + c.getWidth() + " " + c.getHeight());
		doRohu(sz, dim, x1, y1);
		doRohu(jz, dim, x1, -y2);
		doRohu(sv, dim, -x2, y1);
		doRohu(jv, dim, -x2, -y2);

		podklad.setBounds(x1, y1, dim.width, dim.height);

	}

	private void doRohu(final Component cc, final Dimension dim, final int x, final int y) {
		if (cc != null) {
			final Dimension v = rozmer(dim, cc);
			final int x0 = x < 0 ? -v.width - x : x;
			final int y0 = y < 0 ? -v.height - y : y;
			cc.setBounds(x0, y0, v.width, v.height);
			// System.out.println(x0 + " " + y0 + v);
		}

	}

	/**
	 * @param dim
	 * @param cc
	 */
	private Dimension rozmer(final Dimension dim, final Component cc) {
		final Dimension prs = cc.getPreferredSize();
		final Dimension mis = cc.getMinimumSize();
		final Dimension mas = cc.getMaximumSize();

		final int dx = rozmer(ratiox, dim.width, prs.width, mis.width, mas.width);
		final int dy = rozmer(ratioy, dim.height, prs.height, mis.height, mas.height);
		final Dimension v = new Dimension(dx, dy);
		return v;
	}

	private int rozmer(final float ratio, final int size, final int pr, final int mi, final int ma) {
		final int mypr = (int) (size * ratio);
		int v = Math.min(mypr, pr);
		v = Math.max(v, mi);
		v = Math.min(v, ma);
		v = Math.min(v, size); // a omezit prostorem, ktery mame
		return v;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension minimumLayoutSize(final Container aArg0) {
		return podklad.getMaximumSize();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension preferredLayoutSize(final Container aArg0) {
		return podklad.getPreferredSize();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	@Override
	public void removeLayoutComponent(final Component c) {
		if (sz == c)
			sz = null;
		if (sv == c)
			sv = null;
		if (jz == c)
			jz = null;
		if (jv == c)
			jv = null;
		if (podklad == c)
			podklad = null;
	}

}
