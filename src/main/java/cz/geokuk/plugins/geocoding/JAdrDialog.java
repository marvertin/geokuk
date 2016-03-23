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

package cz.geokuk.plugins.geocoding;

/*
 * TextFieldDemo.java requires one additional file:
 * content.txt
 */

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.RefreshorVysledkuHledani;
import cz.geokuk.core.hledani.VysledekHledani;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.refbody.ReferencniBodSeZmenilEvent;

public class JAdrDialog extends JMyDialog0 implements RefreshorVysledkuHledani<Nalezenec>, DocumentListener {

	private static final long	serialVersionUID	= 7087453419069194768L;

	private JTextField			entry;
	private JLabel				jLabel1;
	private JButton				jButtonCentruj;
	private JAdrTable			jAdrTabulka;
	private JLabel				status;

	final static Color			HILIT_COLOR			= Color.LIGHT_GRAY;
	final static Color			ERROR_COLOR			= Color.PINK;
	final static String			CANCEL_ACTION		= "cancel-adr-hled";

	final Color					entryBg;
	// final Highlighter hilit;
	// final Highlighter.HighlightPainter painter;

	private Wgs					referencniBod;

	private PoziceModel			poziceModel;

	private VyrezModel			vyrezModel;

	private GeocodingModel		geocodingModel;

	public JAdrDialog() {
		init();
		entryBg = entry.getBackground();
		entry.getDocument().addDocumentListener(this);
		registerEvents();
	}

	private void registerEvents() {
		jButtonCentruj.addActionListener(e -> {

			final Nalezenec nalezenec = jAdrTabulka.getCurrent();
			if (nalezenec != null) {
				poziceModel.setPozice(nalezenec.wgs);
				vyrezModel.vystredovatNaPozici();
				// Board.eveman.fire(new PoziceChangedEvent(nalezenec.wgs, true) );
			}
		});

		// Board.eveman.registerWeakly(this);

		// jKeskovaciTabulka.getMod
		jAdrTabulka.addListSelectionListener(aE -> jButtonCentruj.setEnabled(jAdrTabulka.getCurrent() != null));
	}

	public void onEvent(final ReferencniBodSeZmenilEvent aEvent) {
		if (naVstupuJsouSouradkyNeboNic()) {
			entry.setText(aEvent.wgs.lat + "," + aEvent.wgs.lon);
			entry.selectAll();
		}
		setReferencniBod(aEvent.wgs);
	}

	private boolean naVstupuJsouSouradkyNeboNic() {
		final String text = entry.getText();
		if (text.trim().length() == 0) {
			return true;
		}
		if (text.matches("-?\\d+(\\.\\d+),-?\\d+(\\.\\d+)")) {
			return true;
		}
		return false;
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 */

	@Override
	protected void initComponents() {
		entry = new JTextField();
		status = new JLabel();
		jLabel1 = new JLabel();
		jButtonCentruj = new JButton("Centruj");
		getRootPane().setDefaultButton(jButtonCentruj);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Najdi místo dle adresy");

		final JAdrTable kesTable = new JAdrTable();
		// jKeskovaciTabulka = new JScrollPane(textArea);
		jAdrTabulka = kesTable;

		jLabel1.setText("Hledat: ");

		final JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		add(panel);

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING) // hroup
				.addGroup(GroupLayout.Alignment.TRAILING,
						layout.createSequentialGroup() // h1
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING) // h2
										.addComponent(jAdrTabulka, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
										.addComponent(status, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE).addGroup(layout.createSequentialGroup() // h3
												.addComponent(jLabel1).addComponent(entry, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE).addComponent(jButtonCentruj)))));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING) // vGrou
				.addGroup(layout.createSequentialGroup() // v1
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE) // v2
								.addComponent(jLabel1).addComponent(entry, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(jButtonCentruj))
						.addComponent(status).addComponent(jAdrTabulka, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)));

		pack();
	}

	protected void setReferencniBod(final Wgs wgs) {
		if (wgs.equals(referencniBod)) {
			return;
		}
		referencniBod = wgs;
		search();
	}

	public void search() {
		if (referencniBod == null) {
			return;
		}
		message("Hleda se ...");
		final String s = entry.getText();
		geocodingModel.spustHledani(s, this);
	}

	void message(final String msg) {
		status.setText(msg);
	}

	// DocumentListener methods

	@Override
	public void insertUpdate(final DocumentEvent ev) {
		search();
	}

	@Override
	public void removeUpdate(final DocumentEvent ev) {
		search();
	}

	@Override
	public void changedUpdate(final DocumentEvent ev) {
	}

	class CancelAction extends AbstractAction {
		private static final long serialVersionUID = -480129891208539096L;

		@Override
		public void actionPerformed(final ActionEvent ev) {
			// hilit.removeAllHighlights();
			entry.setText("");
			entry.setBackground(entryBg);
		}
	}

	@Override
	public void refreshVysledekHledani(final VysledekHledani<Nalezenec> vysledekHledani) {
		// if (nalezenci.size() == 0) {
		// jButtonCentruj.setEnabled(false);
		// } else {
		// jButtonCentruj.setEnabled(true);
		// }
		if (vysledekHledani.nalezenci != null) {
			jAdrTabulka.setNalezenci(vysledekHledani.nalezenci);
			if (vysledekHledani.nalezenci.size() > 0) { // match found
				entry.setBackground(entryBg);
				message("Nalezeno " + vysledekHledani.nalezenci.size() + " možných adres.");
			} else {
				entry.setBackground(ERROR_COLOR);
				message("Žádná shoda, stiskni ESC k výmazu hledacího pole.");
			}
		}
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "HledatAdresu";
	}

	public void inject(final GeocodingModel geocodingModel) {
		this.geocodingModel = geocodingModel;
	}
}
