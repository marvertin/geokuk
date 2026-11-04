package cz.geokuk.plugins.kesoidobsazenost;

import java.awt.Graphics;
import java.awt.Graphics2D;

import cz.geokuk.framework.JSlide0;
import cz.geokuk.util.gui.JBarvovyDvojSlider;

/** Vrstva nad kešoidy, která kreslí legendu a bublinky posuvníků kruhů Obsazenost, aby je ikony nepřekrývaly. Myš propouští ke spodním vrstvám, v řetězci slidů jen předává dál. */
public class JLegendaObsazenosti extends JSlide0 {

	private static final long serialVersionUID = 1L;

	private final transient JBarvovyDvojSlider slidovnik;

	public JLegendaObsazenosti(final JObsazenost obsazenost) {
		slidovnik = obsazenost.getSlidovnik();
		slidovnik.setVrstvaLegendy(this);
		setOpaque(false);
	}

	@Override
	public boolean contains(final int x, final int y) {
		return false;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		if (slidovnik.isShowing()) {
			slidovnik.kresliNadMapou((Graphics2D) g, this);
		}
	}
}
