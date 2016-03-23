package cz.geokuk.core.render;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class JKalibrBoduSpinner extends JSpinner {

	private static final long serialVersionUID = 2463788585820522859L;

	public JKalibrBoduSpinner() {
		super(new SpinnerNumberModel(2, 2, 25, 1));
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
	}

}
