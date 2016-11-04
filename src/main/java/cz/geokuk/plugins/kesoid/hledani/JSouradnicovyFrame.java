/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.geokuk.plugins.kesoid.hledani;

/*
 * TextFieldDemo.java requires one additional file:
 * content.txt
 */
import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.coordinates.WgsParser;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.refbody.ReferencniBodSeZmenilEvent;

public class JSouradnicovyFrame extends JMyDialog0 implements AfterEventReceiverRegistrationInit, DocumentListener {

	class CancelAction extends AbstractAction {
		private static final long serialVersionUID = -480129891208539096L;

		@Override
		public void actionPerformed(final ActionEvent ev) {
			// hilit.removeAllHighlights();
			jSouEdit.setText("");
			jSouEdit.setBackground(entryBg);
		}
	}
	private static final String NS = "NS";
	private static final String EW = "EW";

	private static final Double SPATNY_FORMAT = Double.NEGATIVE_INFINITY;

	private static final long serialVersionUID = 7087453419069194768L;
	// TODO skutečnou hodnotu maximální šířky sem dát
	private static final double SIRKA_MAX = 80;

	private static final double SIRKA_MIN = -80;
	private static final double DELKA_MIN = -180;

	private static final double DELKA_MAX = 180;
	final static Color HILIT_COLOR = Color.LIGHT_GRAY;
	final static Color ERROR_COLOR = Color.PINK;
	final static String CANCEL_ACTION = "cancel-search";
	private JTextField jSouEdit;
	private JLabel jSouEditLabel;

	private JButton jButtonCentruj;
	private JLabel jHotovaSirka;
	private JLabel jHotovaDelka;

	private JLabel jUtm;

	private Color entryBg;
	private Wgs souradniceEditovane;

	private Wgs souradniceReferencni;

	private PoziceModel poziceModel;

	private VyrezModel vyrezModel;

	private boolean souradniceNastavenyRukama;

	public JSouradnicovyFrame() {
		setTitle("Zadání souřadnic");
		init();
		jSouEdit.getDocument().addDocumentListener(this);

		registerEvents();

	}

	@Override
	public void changedUpdate(final DocumentEvent ev) {
		souradniceNastavenyRukama = true;
		edituj();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
	 */
	@Override
	public void initAfterEventReceiverRegistration() {
		// super.ini
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	@Override
	public void insertUpdate(final DocumentEvent ev) {
		souradniceNastavenyRukama = true;
		edituj();
	}

	public void onEvent(final ReferencniBodSeZmenilEvent aEvent) {
		setSouradniceReferencni(aEvent.wgs);
		vyhodnotEnableCentrovacihoTlacitka();
	}

	public void onEvent(final VyrezChangedEvent aEvent) {
		vyhodnotEnableCentrovacihoTlacitka();
	}

	@Override
	public void removeUpdate(final DocumentEvent ev) {
		souradniceNastavenyRukama = true;
		edituj();
	}

	// DocumentListener methods

	@Override
	protected String getTemaNapovedyDialogu() {
		return "JintNaSouradnice";
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */

	@Override
	protected void initComponents() {
		final String tooltip = "<html>Šířku i délku zadáváte jak jedno až tři celá nebo desetinná čísla (stupně, minuty, vteřiny),"
		        + "<br/>jako oddělovač použijte mezeru nebo odpovídající značky °'\".<br/>Jako oddělovač desetin můžete použít tečku nebo čárku."
		        + "<br/>Písmena N nebo E můžete uvést na začátku, na konci nebo je vynechat.<br/>Pro jižní šířku zadejte S, pro západní délku zadejte W.</html>";
		jSouEdit = new JTextField();
		jSouEdit.setToolTipText(tooltip);

		jSouEditLabel = new JLabel("Souřadnice: ");
		jSouEditLabel.setLabelFor(jSouEdit);

		jButtonCentruj = new JButton("Centruj");
		jButtonCentruj.setToolTipText("Centruje mapu na zadaných souřadnicích.");
		getRootPane().setDefaultButton(jButtonCentruj);

		final Font hotovoFont = new Font("Monospaced", Font.BOLD, 20);
		jHotovaSirka = new JLabel();
		jHotovaSirka.setFont(hotovoFont);
		jHotovaSirka.setOpaque(true);
		jHotovaDelka = new JLabel();
		jHotovaDelka.setFont(hotovoFont);
		jHotovaDelka.setOpaque(true);
		jUtm = new JLabel();

		setTitle("Zadání souřadnic");

		final JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		add(panel);

		jButtonCentruj.setAlignmentY(CENTER_ALIGNMENT);
		jUtm.setText("?");
		// panel.add(jSirka);
		// panel.add(jDelka);
		// panel.add(jButtonCentruj);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
		        .addGroup(GroupLayout.Alignment.TRAILING,
		                layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSouEditLabel))
		                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jSouEdit)))
		        .addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(jHotovaSirka).addComponent(jHotovaDelka))
		                .addComponent(jButtonCentruj))
		        .addComponent(jUtm));

		layout.setVerticalGroup(layout.createSequentialGroup()
		        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		                .addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(jSouEditLabel).addComponent(jSouEdit))))
		        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addGroup(layout.createSequentialGroup().addComponent(jHotovaSirka).addComponent(jHotovaDelka))
		                .addComponent(jButtonCentruj))
		        .addComponent(jUtm));
		jSouEdit.setPreferredSize(new Dimension(150, jSouEdit.getPreferredSize().height));
		jSouEdit.setText("");
		entryBg = jSouEdit.getBackground();
		edituj();

	}

	private boolean aplikuj(final JLabel jHotova, final JTextField editacni, final double val, final double min, final double max, final String pismena) {
		boolean ok;
		if (val == SPATNY_FORMAT) {
			jHotova.setText("Grrrr!");
			jHotova.setBackground(Color.RED);
			editacni.setBackground(ERROR_COLOR);
			ok = false;
		} else {
			jHotova.setText(pismena.charAt(val >= 0 ? 0 : 1) + Wgs.toGeoFormat(Math.abs(val)));
			if (val >= min && val <= max) {
				jHotova.setBackground(Color.GREEN);
				editacni.setBackground(Color.WHITE);
				ok = true;
			} else {
				if (val < 10 && val == (long) val) {
					jHotova.setBackground(Color.GRAY);
					jHotova.setText(jHotova.getText().replaceAll("\\d", "?"));
					editacni.setBackground(Color.WHITE);
					ok = false;
				} else {
					jHotova.setBackground(Color.RED);
					editacni.setBackground(ERROR_COLOR);
					ok = false;
				}
			}
		}
		return ok;
	}

	// private final class SpousteniVyhledavace implements ChangeListener {
	// @Override
	// public void stateChanged(ChangeEvent e) {
	// search();
	// }
	// }

	private void edituj() {
		// if (souradnice == null) return;
		boolean ok;
		final Wgs wgs = new WgsParser().parsruj(jSouEdit.getText());
		if (wgs == null) { // prizpusobeni puvodni verzi
			// wgs = new Wgs(SPATNY_FORMAT, SPATNY_FORMAT);
		}
		final double lat = wgs == null ? SPATNY_FORMAT : wgs.lat;
		final double lon = wgs == null ? SPATNY_FORMAT : wgs.lon;
		final boolean okSirka = aplikuj(jHotovaSirka, jSouEdit, lat, SIRKA_MIN, SIRKA_MAX, NS);
		final boolean okDelka = aplikuj(jHotovaDelka, jSouEdit, lon, DELKA_MIN, DELKA_MAX, EW);
		ok = okSirka && okDelka;
		if (ok) {
			souradniceEditovane = wgs;
			jUtm.setText(wgs.toUtm().toString());
		} else {
			jUtm.setText("UTM = ?");
		}
		vyhodnotEnableCentrovacihoTlacitka();
	}

	// public static void main(String args[]) {
	// //Schedule a job for the event dispatch thread:
	// //creating and showing this application's GUI.
	//
	// SwingUtilities.invokeLater(new Runnable() {
	// public void run() {
	// //Turn off metal's use of bold fonts
	//// UIManager.put("swing.boldMetal", Boolean.FALSE);
	// new JSouradnicovyFrame(null).setVisible(true);
	// }
	// });
	// }

	/**
	 * @return
	 */
	private boolean jsmeVycentrovaniSeZadanouPozici() {
		final boolean jsmeNaMiste = souradniceEditovane != null && souradniceEditovane.equals(souradniceReferencni) && vyrezModel.isPoziceUprostred();
		return jsmeNaMiste;
	}

	private void registerEvents() {

		jButtonCentruj.addActionListener(e -> {
			poziceModel.setPozice(souradniceEditovane);
			vyrezModel.vystredovatNaPozici();
		});
	}

	private void setSouradniceReferencni(final Wgs wgs) {
		if (wgs.equals(souradniceReferencni)) {
			return;
		}
		souradniceReferencni = wgs;
		if (!souradniceNastavenyRukama) {
			jSouEdit.setText(wgs.toString());
//			jSouEdit.setText(Wgs.toGeoFormat(wgs.lat) + " ; " + Wgs.toGeoFormat(wgs.lon));
			jSouEdit.selectAll();
			souradniceNastavenyRukama = false; // toto se může zdát zbytečné, ale řádky před tím to změní
		}
		edituj();
	}

	/**
	 *
	 */
	private void vyhodnotEnableCentrovacihoTlacitka() {
		final boolean jsmeNaMiste = jsmeVycentrovaniSeZadanouPozici();

		// jsmeNaMiste = vyrezModel.isPoziceUprostred();
		jButtonCentruj.setEnabled(!jsmeNaMiste);
	}
}
