package cz.geokuk.util.gui.fontchoser;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.event.*;

/**
 * <code>JFontChooser</code> provides a pane of controls designed to allow a user to manipulate and select a font.
 *
 * This class provides three levels of API:
 * <ol>
 * <li>A static convenience method which shows a modal font-chooser dialog and returns the font selected by the user.
 * <li>A static convenience method for creating a font-chooser dialog where <code>ActionListeners</code> can be specified to be invoked when the user presses one of the dialog buttons.
 * <li>The ability to create instances of <code>JFontChooser</code> panes directly (within any container). <code>PropertyChange</code> listeners can be added to detect when the current "font" property changes.
 * </ol>
 * <p>
 *
 * @author Adrian BER
 */
public class JFontChooser extends JComponent {

	/**
	 *
	 */
	private static final long		serialVersionUID	= -3412974969117820342L;

	/** The list of possible font sizes. */
	private static final Integer[]	SIZES				= { 8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 24, 26, 28, 32, 36, 40, 48, 56, 64, 72 };

	/** The list of possible fonts. */
	private static final String[]	FONTS				= GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

	private FontSelectionModel		selectionModel;

	private JList<String>			fontList;

	private JList<Integer>			sizeList;

	private JCheckBox				boldCheckBox;

	private JCheckBox				italicCheckBox;

	private JLabel					previewLabel;

	/** The preview text, if null the font name will be the preview text. */
	private String					previewText;

	/** Listener used to update the font of the selection model. */
	private final SelectionUpdater	selectionUpdater	= new SelectionUpdater();

	/**
	 * Listener used to update the font in the components. This should be registered with the selection model.
	 */
	private final LabelUpdater		labelUpdater		= new LabelUpdater();

	/** True if the components are being updated and no event should be generated. */
	private boolean					updatingComponents	= false;

	/**
	 * Listener class used to update the font in the components. This should be registered with the selection model.
	 */
	private class LabelUpdater implements ChangeListener {

		public void stateChanged(final ChangeEvent e) {
			updateComponents();
		}

	}

	/** Listener class used to update the font of the preview label. */
	private class SelectionUpdater implements ChangeListener, ListSelectionListener {

		public void stateChanged(final ChangeEvent e) {
			if (!updatingComponents) {
				setFont(buildFont());
			}
		}

		public void valueChanged(final ListSelectionEvent e) {
			if (!updatingComponents) {
				setFont(buildFont());
			}
		}
	}

	/**
	 * Shows a modal font-chooser dialog and blocks until the dialog is hidden. If the user presses the "OK" button, then this method hides/disposes the dialog and returns the selected color. If the user presses the "Cancel" button or closes the dialog without pressing "OK", then this method
	 * hides/disposes the dialog and returns <code>null</code>.
	 *
	 * @param component
	 *            the parent <code>Component</code> for the dialog
	 * @param title
	 *            the String containing the dialog's title
	 * @return the selected font or <code>null</code> if the user opted out
	 * @exception HeadlessException
	 *                if GraphicsEnvironment.isHeadless() returns true.
	 * @see java.awt.GraphicsEnvironment#isHeadless
	 */
	public Font showDialog(final Component component, final String title) {

		final FontTracker ok = new FontTracker(this);
		final JDialog dialog = createDialog(component, title, true, ok, null);
		dialog.addWindowListener(new FontChooserDialog.Closer());
		dialog.addComponentListener(new FontChooserDialog.DisposeOnClose());

		dialog.setVisible(true); // blocks until user brings dialog down...

		return ok.getFont();
	}

	/**
	 * Creates and returns a new dialog containing the specified <code>ColorChooser</code> pane along with "OK", "Cancel", and "Reset" buttons. If the "OK" or "Cancel" buttons are pressed, the dialog is automatically hidden (but not disposed). If the "Reset" button is pressed, the color-chooser's
	 * color will be reset to the font which was set the last time <code>show</code> was invoked on the dialog and the dialog will remain showing.
	 *
	 * @param c
	 *            the parent component for the dialog
	 * @param title
	 *            the title for the dialog
	 * @param modal
	 *            a boolean. When true, the remainder of the program is inactive until the dialog is closed.
	 * @param okListener
	 *            the ActionListener invoked when "OK" is pressed
	 * @param cancelListener
	 *            the ActionListener invoked when "Cancel" is pressed
	 * @return a new dialog containing the font-chooser pane
	 * @exception HeadlessException
	 *                if GraphicsEnvironment.isHeadless() returns true.
	 * @see java.awt.GraphicsEnvironment#isHeadless
	 */
	public JDialog createDialog(final Component c, final String title, final boolean modal, final ActionListener okListener, final ActionListener cancelListener) {

		return new FontChooserDialog(c, title, modal, this, okListener, cancelListener);
	}

	/**
	 * Creates a color chooser pane with an initial font which is the same font as the default font for labels.
	 */
	public JFontChooser() {
		this(new DefaultFontSelectionModel());
	}

	/**
	 * Creates a font chooser pane with the specified initial font.
	 *
	 * @param initialFont
	 *            the initial font set in the chooser
	 */
	public JFontChooser(final Font initialFont) {
		this(new DefaultFontSelectionModel(initialFont));
	}

	/**
	 * Creates a font chooser pane with the specified <code>FontSelectionModel</code>.
	 *
	 * @param model
	 *            the font selection model used by this component
	 */
	public JFontChooser(final FontSelectionModel model) {
		selectionModel = model;
		init(model.getSelectedFont());
		selectionModel.addChangeListener(labelUpdater);
	}

	private void init(final Font font) {
		setLayout(new GridBagLayout());

		final Insets ins = new Insets(2, 2, 2, 2);

		fontList = new JList<>(FONTS);
		fontList.setVisibleRowCount(10);
		fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(fontList), new GridBagConstraints(0, 0, 1, 1, 2, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, ins, 0, 0));

		sizeList = new JList<>(SIZES);
		((JLabel) sizeList.getCellRenderer()).setHorizontalAlignment(JLabel.RIGHT);
		sizeList.setVisibleRowCount(10);
		sizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(new JScrollPane(sizeList), new GridBagConstraints(1, 0, 1, 1, 1, 2, GridBagConstraints.CENTER, GridBagConstraints.BOTH, ins, 0, 0));

		boldCheckBox = new JCheckBox("Bold");
		add(boldCheckBox, new GridBagConstraints(0, 1, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, ins, 0, 0));

		italicCheckBox = new JCheckBox("Italic");
		add(italicCheckBox, new GridBagConstraints(0, 2, 2, 1, 1, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, ins, 0, 0));

		previewLabel = new JLabel("");
		previewLabel.setHorizontalAlignment(JLabel.CENTER);
		previewLabel.setVerticalAlignment(JLabel.CENTER);
		add(new JScrollPane(previewLabel), new GridBagConstraints(0, 3, 2, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, ins, 0, 0));

		setFont(font == null ? previewLabel.getFont() : font);

		fontList.addListSelectionListener(selectionUpdater);
		sizeList.addListSelectionListener(selectionUpdater);
		boldCheckBox.addChangeListener(selectionUpdater);
		italicCheckBox.addChangeListener(selectionUpdater);
	}

	private Font buildFont() {
		// Font labelFont = previewLabel.getFont();

		final String fontName = (String) fontList.getSelectedValue();
		if (fontName == null) {
			return null;
			// fontName = labelFont.getName();
		}
		final Integer sizeInt = (Integer) sizeList.getSelectedValue();
		if (sizeInt == null) {
			// size = labelFont.getSize();
			return null;
		}

		// create the font
		// // first create the font attributes
		// HashMap map = new HashMap();
		// map.put(TextAttribute.BACKGROUND, Color.white);
		// map.put(TextAttribute.FAMILY, fontName);
		// map.put(TextAttribute.FOREGROUND, Color.black);
		// map.put(TextAttribute.SIZE , new Float(size));
		// map.put(TextAttribute.UNDERLINE, italicCheckBox.isSelected() ? TextAttribute.UNDERLINE_LOW_ONE_PIXEL : TextAttribute.UNDERLINE_LOW_TWO_PIXEL);
		// map.put(TextAttribute.STRIKETHROUGH, italicCheckBox.isSelected() ? TextAttribute.STRIKETHROUGH_ON : Boolean.FALSE);
		// map.put(TextAttribute.WEIGHT, boldCheckBox.isSelected() ? TextAttribute.WEIGHT_BOLD : TextAttribute.WEIGHT_REGULAR);
		// map.put(TextAttribute.POSTURE,
		// italicCheckBox.isSelected() ? TextAttribute.POSTURE_OBLIQUE : TextAttribute.POSTURE_REGULAR);
		//
		// return new Font(map);

		return new Font(fontName, (italicCheckBox.isSelected() ? Font.ITALIC : Font.PLAIN) | (boldCheckBox.isSelected() ? Font.BOLD : Font.PLAIN), sizeInt);
	}

	/** Updates the font in the preview component according to the selected values. */
	private void updateComponents() {
		updatingComponents = true;

		final Font font = getFont();

		fontList.setSelectedValue(font.getName(), true);
		sizeList.setSelectedValue(font.getSize(), true);
		boldCheckBox.setSelected(font.isBold());
		italicCheckBox.setSelected(font.isItalic());

		if (previewText == null) {
			previewLabel.setText(font.getName());
		}

		// set the font and fire a property change
		final Font oldValue = previewLabel.getFont();
		previewLabel.setFont(font);
		firePropertyChange("font", oldValue, font);

		updatingComponents = false;
	}

	/**
	 * Returns the data model that handles font selections.
	 *
	 * @return a FontSelectionModel object
	 */
	public FontSelectionModel getSelectionModel() {
		return selectionModel;
	}

	/**
	 * Set the model containing the selected font.
	 *
	 * @param newModel
	 *            the new FontSelectionModel object
	 */
	public void setSelectionModel(final FontSelectionModel newModel) {
		final FontSelectionModel oldModel = selectionModel;
		selectionModel = newModel;
		oldModel.removeChangeListener(labelUpdater);
		newModel.addChangeListener(labelUpdater);
		firePropertyChange("selectionModel", oldModel, newModel);
	}

	/**
	 * Gets the current font value from the font chooser.
	 *
	 * @return the current font value of the font chooser
	 */
	public Font getFont() {
		return selectionModel.getSelectedFont();
	}

	/**
	 * Sets the current font of the font chooser to the specified font. The <code>ColorSelectionModel</code> will fire a <code>ChangeEvent</code>
	 *
	 * @param font
	 *            the font to be set in the font chooser
	 * @see JComponent#addPropertyChangeListener
	 */
	public void setFont(final Font font) {
		selectionModel.setSelectedFont(font);
	}

	/**
	 * Returns the preview text displayed in the preview component.
	 *
	 * @return the preview text, if null the font name will be displayed
	 */
	public String getPreviewText() {
		return previewText;
	}

	/**
	 * Sets the preview text displayed in the preview component.
	 *
	 * @param previewText
	 *            the preview text, if null the font name will be displayed
	 */
	public void setPreviewText(final String previewText) {
		this.previewText = previewText;
		previewLabel.setText("");
		updateComponents();
	}

}

/*
 * Class which builds a font chooser dialog consisting of a JFontChooser with "Ok", "Cancel", and "Reset" buttons.
 *
 * Note: This needs to be fixed to deal with localization!
 */
class FontChooserDialog extends JDialog {
	/**
	 *
	 */
	private static final long	serialVersionUID	= -7376253355818496076L;
	private Font				initialFont;
	private final JFontChooser	chooserPane;

	public FontChooserDialog(final Component c, final String title, final boolean modal, final JFontChooser chooserPane, final ActionListener okListener, final ActionListener cancelListener) {
		super(JOptionPane.getFrameForComponent(c), title, modal);
		// setResizable(false);

		final String okString = UIManager.getString("ColorChooser.okText");
		final String cancelString = UIManager.getString("ColorChooser.cancelText");
		final String resetString = UIManager.getString("ColorChooser.resetText");

		/*
		 * Create Lower button panel
		 */
		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		final JButton okButton = new JButton(okString);
		getRootPane().setDefaultButton(okButton);
		okButton.setActionCommand("OK");
		if (okListener != null) {
			okButton.addActionListener(okListener);
		}
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
			}
		});
		buttonPane.add(okButton);

		final JButton cancelButton = new JButton(cancelString);

		// The following few lines are used to register esc to close the dialog
		final Action cancelKeyAction = new AbstractAction() {
			private static final long serialVersionUID = -2840323139867668049L;

			public void actionPerformed(final ActionEvent e) {
				// todo make it in 1.3
				// ActionListener[] listeners
				// = ((AbstractButton) e.getSource()).getActionListeners();
				// for (int i = 0; i < listeners.length; i++) {
				// listeners[i].actionPerformed(e);
				// }
			}
		};
		final KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke((char) KeyEvent.VK_ESCAPE);
		final InputMap inputMap = cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap actionMap = cancelButton.getActionMap();
		if (inputMap != null && actionMap != null) {
			inputMap.put(cancelKeyStroke, "cancel");
			actionMap.put("cancel", cancelKeyAction);
		}
		// end esc handling

		cancelButton.setActionCommand("cancel");
		if (cancelListener != null) {
			cancelButton.addActionListener(cancelListener);
		}
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setVisible(false);
			}
		});
		buttonPane.add(cancelButton);

		final JButton resetButton = new JButton(resetString);
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				reset();
			}
		});
		final int mnemonic = UIManager.getInt("ColorChooser.resetMnemonic");
		if (mnemonic != -1) {
			resetButton.setMnemonic(mnemonic);
		}
		buttonPane.add(resetButton);

		// initialiase the content pane
		this.chooserPane = chooserPane;

		final Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(chooserPane, BorderLayout.CENTER);

		contentPane.add(buttonPane, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(c);
	}

	public void setVisible(final boolean visible) {
		if (visible)
			initialFont = chooserPane.getFont();
		super.setVisible(visible);
	}

	public void reset() {
		chooserPane.setFont(initialFont);
	}

	static class Closer extends WindowAdapter implements Serializable {
		private static final long serialVersionUID = 5235126599151412593L;

		public void windowClosing(final WindowEvent e) {
			final Window w = e.getWindow();
			w.setVisible(false);
		}
	}

	static class DisposeOnClose extends ComponentAdapter implements Serializable {
		private static final long serialVersionUID = 7570289535006656935L;

		public void componentHidden(final ComponentEvent e) {
			final Window w = (Window) e.getComponent();
			w.dispose();
		}
	}

}

class FontTracker implements ActionListener, Serializable {
	private static final long	serialVersionUID	= -969244282397272192L;
	JFontChooser				chooser;
	Font						color;

	public FontTracker(final JFontChooser c) {
		chooser = c;
	}

	public void actionPerformed(final ActionEvent e) {
		color = chooser.getFont();
	}

	public Font getFont() {
		return color;
	}
}

/**
 * A generic implementation of <code>{@link FontSelectionModel}</code>.
 *
 * @author Adrian BER
 */
class DefaultFontSelectionModel implements FontSelectionModel {

	/** The default selected font. */
	private static final Font		DEFAULT_INITIAL_FONT	= new Font("Dialog", Font.PLAIN, 12);

	/** The selected font. */
	private Font					selectedFont;

	/** The change listeners notified by a change in this model. */
	private final EventListenerList	listeners				= new EventListenerList();

	/**
	 * Creates a <code>DefaultFontSelectionModel</code> with the current font set to <code>Dialog, 12</code>. This is the default constructor.
	 */
	public DefaultFontSelectionModel() {
		this(DEFAULT_INITIAL_FONT);
	}

	/**
	 * Creates a <code>DefaultFontSelectionModel</code> with the current font set to <code>font</code>, which should be non-<code>null</code>. Note that setting the font to <code>null</code> is undefined and may have unpredictable results.
	 *
	 * @param selectedFont
	 *            the new <code>Font</code>
	 */
	public DefaultFontSelectionModel(Font selectedFont) {
		if (selectedFont == null) {
			selectedFont = DEFAULT_INITIAL_FONT;
		}
		this.selectedFont = selectedFont;
	}

	public Font getSelectedFont() {
		return selectedFont;
	}

	public void setSelectedFont(final Font selectedFont) {
		if (selectedFont != null) {
			this.selectedFont = selectedFont;
			fireChangeListeners();
		}
	}

	public void addChangeListener(final ChangeListener listener) {
		listeners.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(final ChangeListener listener) {
		listeners.remove(ChangeListener.class, listener);
	}

	/** Fires the listeners registered with this model. */
	protected void fireChangeListeners() {
		final ChangeEvent ev = new ChangeEvent(this);
		final Object[] l = listeners.getListeners(ChangeListener.class);
		for (final Object listener : l) {
			((ChangeListener) listener).stateChanged(ev);
		}
	}
}
