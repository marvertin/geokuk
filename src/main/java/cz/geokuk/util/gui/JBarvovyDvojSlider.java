/**
 *
 */
package cz.geokuk.util.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * @author veverka
 *
 */
public class JBarvovyDvojSlider extends JPanel {

	private static final long	serialVersionUID	= 5956221175094609896L;

	private JSlider				iBarvovnik;
	private JSlider				iPruhlednik;

	/**
	 * @return the barvovnik
	 */
	public JSlider getBarvovnik() {
		return iBarvovnik;
	}

	/**
	 * @return the pruhlednik
	 */
	public JSlider getPruhlednik() {
		return iPruhlednik;
	}

	public JBarvovyDvojSlider() {
		initComponents();
	}

	/**
	 *
	 */
	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setOpaque(false);

		// iBarvovnik = new JSlider(JSlider.VERTICAL, 0, 255, 128);
		// iBarvovnik = new JSlider(Board.hodnotyObsazenosti.getBarvovnikModel());
		iBarvovnik = new JSlider(JSlider.VERTICAL);
		iBarvovnik.setOpaque(false);
		iBarvovnik.setToolTipText("Nastavení stupně šedi kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
		iBarvovnik.setFocusable(false);
		final DefaultBoundedRangeModel barvovnikModel = new DefaultBoundedRangeModel(128, 0, 0, 255);
		iBarvovnik.setModel(barvovnikModel);

		iPruhlednik = new JSlider(JSlider.VERTICAL);
		// iPruhlednik = new JSlider(Board.hodnotyObsazenosti.getPruhlednikModel());
		iPruhlednik.setOpaque(false);
		iPruhlednik.setToolTipText("Nastavení průhlednosti kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
		iPruhlednik.setFocusable(false);
		final DefaultBoundedRangeModel pruhlednikModel = new DefaultBoundedRangeModel(128, 0, 0, 255);
		iPruhlednik.setModel(pruhlednikModel);

		add(Box.createRigidArea(new Dimension(0, 20)));
		add(iBarvovnik);
		add(Box.createRigidArea(new Dimension(0, 20)));
		add(iPruhlednik);
		add(Box.createRigidArea(new Dimension(0, 20)));
	}

	public Color getColor() {
		final int barva = iBarvovnik.getModel().getValue();
		final int pruhlednost = iPruhlednik.getModel().getValue();
		return new Color(barva, barva, barva, pruhlednost);
	}

	public void setColor(final Color color) {
		final int barva = (color.getBlue() + color.getRed() + color.getGreen()) / 3;
		final int pruhlednost = color.getAlpha();
		iBarvovnik.getModel().setValue(barva);
		iPruhlednik.getModel().setValue(pruhlednost);
	}

	/**
	 * @param aChangeListener
	 */
	public void addChangeListener(final ChangeListener aChangeListener) {
		iBarvovnik.addChangeListener(aChangeListener);
		iPruhlednik.addChangeListener(aChangeListener);
	}

	// public void setModel(BarvovyDvojSliderModel model) {
	// iModel = model;
	// iBarvovnik.setModel(model.getBarvovnikModel());
	// iPruhlednik.setModel(model.getPruhlednikModel());
	// }

	// /**
	// * @return
	// */
	// public BarvovyDvojSliderModel getModel() {
	// return iModel;
	// }

}
