/**
 *
 */
package cz.geokuk.util.gui;

import java.awt.*;

/**
 * @author veverka
 *
 */
public class MyOverlayManager implements LayoutManager2 {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
	 */
	public void addLayoutComponent(Component c, Object o) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
	 */
	public float getLayoutAlignmentX(Container aArg0) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
	 */
	public float getLayoutAlignmentY(Container aArg0) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
	 */
	public void invalidateLayout(Container aArg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
	 */
	public Dimension maximumLayoutSize(Container c) {
		Dimension dim = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		for (Component cc : c.getComponents()) {
			Dimension size = cc.getMinimumSize();
			if (size.width < dim.width)
				dim.width = size.width;
			if (size.height < dim.width)
				dim.height = size.height;
		}
		pridejInsets(dim, c);
		return dim;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
	 */
	public void addLayoutComponent(String s, Component c) {
		addLayoutComponent(c, s);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
	 */
	public void layoutContainer(Container c) {
		Insets in = c.getInsets();
		int width = c.getWidth() - in.right - in.left;
		int height = c.getHeight() - in.bottom - in.top;
		for (Component cc : c.getComponents()) {
			cc.setBounds(in.left, in.top, width, height);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
	 */
	public Dimension minimumLayoutSize(Container c) {
		Dimension dim = new Dimension();
		for (Component cc : c.getComponents()) {
			Dimension size = cc.getMinimumSize();
			if (size.width > dim.width)
				dim.width = size.width;
			if (size.height > dim.width)
				dim.height = size.height;
		}
		pridejInsets(dim, c);
		return dim;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
	 */
	public Dimension preferredLayoutSize(Container c) {
		Dimension dim = new Dimension();
		for (Component cc : c.getComponents()) {
			Dimension size = cc.getPreferredSize();
			if (size.width > dim.width)
				dim.width = size.width;
			if (size.height > dim.width)
				dim.height = size.height;
		}
		pridejInsets(dim, c);
		return dim;
	}

	private void pridejInsets(Dimension dim, Container c) {
		Insets in = c.getInsets();
		dim.height += in.top + in.bottom;
		dim.width += in.left + in.right;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
	 */
	public void removeLayoutComponent(Component c) {
	}

}
