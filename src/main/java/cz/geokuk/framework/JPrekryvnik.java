package cz.geokuk.framework;

import java.awt.Component;

import cz.geokuk.util.gui.MyOverlayManager;

public abstract class JPrekryvnik extends JSlide0 {

	private static final long serialVersionUID = 6666523437492021231L;

	private JSlide0 lastAddedSlide;

	/**
	 *
	 */
	public JPrekryvnik() {
		setLayout(new MyOverlayManager());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.Container#add(java.awt.Component)
	 */
	@Override
	public Component add(final Component aComp) {
		if (aComp instanceof JSlide0) {
			final JSlide0 slide = (JSlide0) aComp;
			if (lastAddedSlide != null) {
				lastAddedSlide.addChained(slide);
			}
			lastAddedSlide = slide;
			return super.add(slide);
		} else {
			throw new RuntimeException("Kopmonenta " + aComp.getClass().getName() + " musí implementovat JSlide0");
		}
	}

	@Override
	public boolean isOptimizedDrawingEnabled() {
		return false;
	}
}
