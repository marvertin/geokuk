/**
 *
 */
package cz.geokuk.util.gui;

import java.awt.*;

/**
 * @author Martin Veverka
 *
 */
public class MyOverlayManager implements LayoutManager2 {

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
	 */
	@Override
	public void addLayoutComponent(final Component c, final Object o) {}

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
	public void invalidateLayout(final Container aArg0) {}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	@Override
	public void layoutContainer(final Container c) {
		final Insets in = c.getInsets();
		final int width = c.getWidth() - in.right - in.left;
		final int height = c.getHeight() - in.bottom - in.top;
		for (final Component cc : c.getComponents()) {
			cc.setBounds(in.left, in.top, width, height);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension maximumLayoutSize(final Container c) {
		final Dimension dim = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		for (final Component cc : c.getComponents()) {
			final Dimension size = cc.getMinimumSize();
			if (size.width < dim.width) {
				dim.width = size.width;
			}
			if (size.height < dim.width) {
				dim.height = size.height;
			}
		}
		pridejInsets(dim, c);
		return dim;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension minimumLayoutSize(final Container c) {
		final Dimension dim = new Dimension();
		for (final Component cc : c.getComponents()) {
			final Dimension size = cc.getMinimumSize();
			if (size.width > dim.width) {
				dim.width = size.width;
			}
			if (size.height > dim.width) {
				dim.height = size.height;
			}
		}
		pridejInsets(dim, c);
		return dim;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	@Override
	public Dimension preferredLayoutSize(final Container c) {
		final Dimension dim = new Dimension();
		for (final Component cc : c.getComponents()) {
			final Dimension size = cc.getPreferredSize();
			if (size.width > dim.width) {
				dim.width = size.width;
			}
			if (size.height > dim.width) {
				dim.height = size.height;
			}
		}
		pridejInsets(dim, c);
		return dim;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	@Override
	public void removeLayoutComponent(final Component c) {}

	private void pridejInsets(final Dimension dim, final Container c) {
		final Insets in = c.getInsets();
		dim.height += in.top + in.bottom;
		dim.width += in.left + in.right;
	}

}
