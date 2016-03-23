package cz.geokuk.plugins.kesoidpopisky;

import java.awt.Color;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import cz.geokuk.util.gui.fontchoser.JFontChooser;

public class JVlastnostiPisma extends JPanel {

	private static final long			serialVersionUID				= 6845731953052027169L;

	public static final String			VLASTNOSI_PISMA_MODEL_PROPERTY	= "vlastnostiPismaModel";

	private final VlastnostiPismaModel	vlastnostiPismaModel;

	private JSpinner					xspinner;
	private JSpinner					yspinner;
	private JColorChooser				foregroundChooser;
	private JFontChooser				fontChooser;
	private JColorChooser				backgroudChooser;

	public JVlastnostiPisma() {
		this(new VlastnostiPismaModel());
	}

	public JVlastnostiPisma(final VlastnostiPismaModel model) {
		assert model != null;
		vlastnostiPismaModel = model;
		initComponents();
		registerEvents();
	}

	protected void initComponents() {
		xspinner = new JSpinner();
		yspinner = new JSpinner();
		foregroundChooser = new JColorChooser(Color.BLACK);
		backgroudChooser = new JColorChooser(Color.WHITE);
		fontChooser = new JFontChooser();

		xspinner.setToolTipText("Posun popisku vůči ikoně v horizontálním směru");
		yspinner.setToolTipText("Posun popisku vůči ikoně ve vertkálním směru");
		foregroundChooser.setToolTipText("Barva písma včetně průhlednosti");
		backgroudChooser.setToolTipText("Barva podkladu včetně průhlednosti");
		fontChooser.setToolTipText("Font popisků na mapě");

		foregroundChooser.setBorder(BorderFactory.createTitledBorder("Písmo"));
		backgroudChooser.setBorder(BorderFactory.createTitledBorder("Podklad"));

		final Box box = Box.createHorizontalBox();

		box.add(foregroundChooser);
		box.add(xspinner);
		box.add(yspinner);
		// box.add(Box.createVerticalStrut(10));
		box.add(fontChooser);
		box.add(backgroudChooser);
		add(box);
	}

	/**
	 *
	 */
	private void registerEvents() {
		final ChangeListener chlistenerGui2Model = e -> {
			final VlastnostiPismaModel m = getVlastnostiPismaModel();
			m.setForeground(foregroundChooser.getSelectionModel().getSelectedColor());
			m.setPosuX((Integer) xspinner.getValue());
			m.setPosuY((Integer) yspinner.getValue());
			m.setBackground(backgroudChooser.getSelectionModel().getSelectedColor());
			m.setFont(fontChooser.getFont());
		};

		final ChangeListener chlistenerModel2Gui = e -> {
			final VlastnostiPismaModel m = getVlastnostiPismaModel();
			foregroundChooser.getSelectionModel().setSelectedColor(m.getForeground());
			backgroudChooser.getSelectionModel().setSelectedColor(m.getBackground());
			fontChooser.getSelectionModel().setSelectedFont(m.getFont());
			xspinner.setValue(m.getPosuX());
			yspinner.setValue(m.getPosuY());
		};

		foregroundChooser.getSelectionModel().addChangeListener(chlistenerGui2Model);
		xspinner.addChangeListener(chlistenerGui2Model);
		yspinner.addChangeListener(chlistenerGui2Model);
		backgroudChooser.getSelectionModel().addChangeListener(chlistenerGui2Model);
		fontChooser.getSelectionModel().addChangeListener(chlistenerGui2Model);

		getVlastnostiPismaModel().addChangeListener(chlistenerModel2Gui);

	}

	/**
	 * Sets the model containing the selected color.
	 *
	 * @param newModel
	 *            the new <code>ColorSelectionModel</code> object
	 *
	 * @beaninfo bound: true hidden: true description: The model which contains the currently selected color.
	 */
	// Nedáváme, museli bychom pak testovat, zda se správně mění model asprávně reagovat na lsitenery.

	// private void setVlastnostiPismaModel(final VlastnostiPismaModel newModel ) {
	// final VlastnostiPismaModel oldModel = vlastnostiPismaModel;
	// vlastnostiPismaModel = newModel;
	// firePropertyChange(VLASTNOSI_PISMA_MODEL_PROPERTY, oldModel, newModel);
	// }

	public VlastnostiPismaModel getVlastnostiPismaModel() {
		return vlastnostiPismaModel;
	}

}
