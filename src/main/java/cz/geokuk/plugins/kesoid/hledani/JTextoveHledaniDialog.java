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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.regex.PatternSyntaxException;

import javax.swing.*;
import javax.swing.event.*;

import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.*;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.refbody.ReferencniBodSeZmenilEvent;

public class JTextoveHledaniDialog extends JMyDialog0 implements AfterInjectInit, RefreshorVysledkuHledani<Nalezenec>, DocumentListener {

	class CancelAction extends AbstractAction {
		private static final long serialVersionUID = -480129891208539096L;

		@Override
		public void actionPerformed(final ActionEvent ev) {
			// hilit.removeAllHighlights();
			entry.setText("");
			entry.setBackground(entryBg);
		}
	}

	private final class SpousteniVyhledavace implements ChangeListener {

		private boolean bylVybran;

		@Override
		public void stateChanged(final ChangeEvent e) {
			final JCheckBox chb = (JCheckBox) e.getSource();
			if (bylVybran != chb.isSelected()) {
				bylVybran = chb.isSelected();
				search();
			}
		}
	}

	private static final long serialVersionUID = 7087453419069194768L;
	final static Color HILIT_COLOR = Color.LIGHT_GRAY;
	final static Color ERROR_COLOR = Color.PINK;
	final static String CANCEL_ACTION = "cancel-search";
	private JTextField entry;
	private JLabel jLabel1;

	private JButton jButtonCentruj;
	private JKesTable jKeskovaciTabulka;
	private JLabel status;

	private JCheckBox jRegularniVyrazy;
	private JCheckBox jJenVZobrazenych;
	final Color entryBg;

	// final Highlighter hilit;
	// final Highlighter.HighlightPainter painter;
	private KesBag vsechny;

	private KesBag filtrovane;

	private Wgs referencniBod;

	private PoziceModel poziceModel;

	private VyrezModel vyrezModel;

	private HledaciSluzba hledaciSluzba;

	public JTextoveHledaniDialog() {
		init();

		entryBg = entry.getBackground();
		entry.getDocument().addDocumentListener(this);

		registerEvents();

	}

	@Override
	public void changedUpdate(final DocumentEvent ev) {}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterInjectInit#initAfterInject()
	 */
	@Override
	public void initAfterInject() {
		factory.init(jKeskovaciTabulka);
	}

	public void inject(final HledaciSluzba hledaciSluzba) {
		this.hledaciSluzba = hledaciSluzba;
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	@Override
	public void insertUpdate(final DocumentEvent ev) {
		search();
	}

	public void onEvent(final KeskyNactenyEvent aEvent) {
		setVsechny(aEvent.getVsechny());
	}

	public void onEvent(final KeskyVyfiltrovanyEvent aEvent) {
		setFiltrovane(aEvent.getFiltrovane());
	}

	// DocumentListener methods

	public void onEvent(final ReferencniBodSeZmenilEvent aEvent) {
		setReferencniBod(aEvent.wgs);
	}

	@Override
	public void refreshVysledekHledani(final VysledekHledani<Nalezenec> vysledekHledani) {
		// if (nalezenci.size() == 0) {
		// jButtonCentruj.setEnabled(false);
		// } else {
		// jButtonCentruj.setEnabled(true);
		// }
		if (vysledekHledani.nalezenci != null) {
			jKeskovaciTabulka.setKeslist(vysledekHledani.nalezenci);
			if (vysledekHledani.nalezenci.size() > 0) { // match found
				entry.setBackground(entryBg);
				message("Nalezeno " + vysledekHledani.nalezenci.size() + " keší.");
			} else {
				entry.setBackground(ERROR_COLOR);
				message("Žádná shoda, stiskni ESC k výmazu hledacího pole.");
			}
		}
		final Exception xexception = vysledekHledani.exception;
		if (xexception instanceof PatternSyntaxException) {
			final PatternSyntaxException exception = (PatternSyntaxException) xexception;
			entry.setBackground(entryBg);
			message(exception.getDescription() + " na pozici " + exception.getIndex());
		}
	}

	@Override
	public void removeUpdate(final DocumentEvent ev) {
		search();
	}

	public void search() {
		if (vsechny == null || filtrovane == null || referencniBod == null) {
			return;
		}
		message("Hleda se ...");
		final String s = entry.getText();
		final HledaciPodminka podm = new HledaciPodminka();
		podm.setStredHledani(referencniBod);
		podm.setVzorek(s);
		podm.setRegularniVyraz(jRegularniVyrazy.isSelected());
		podm.setJenVZobrazenych(jJenVZobrazenych.isSelected());
		final KesBag kde = jJenVZobrazenych.isSelected() ? filtrovane : vsechny;
		hledaciSluzba.spustHledani(new Hledac(kde), podm, this);
	}

	public void setFiltrovane(final KesBag filtrovane) {
		this.filtrovane = filtrovane;
		search();
	}

	public void setVsechny(final KesBag vsechny) {
		this.vsechny = vsechny;
		search();
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "HledatVKesoidech";
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
		setTitle("Najdi keš");

		final JKesTable kesTable = new JKesTable();
		// jKeskovaciTabulka = new JScrollPane(textArea);
		jKeskovaciTabulka = kesTable;

		jLabel1.setText("Hledat: ");

		jJenVZobrazenych = new JCheckBox("Jen v zobrazených keších");
		jRegularniVyrazy = new JCheckBox("Regulární výraz");

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
		                                .addComponent(jKeskovaciTabulka, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
		                                .addComponent(status, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
		                                .addGroup(layout.createSequentialGroup() // h3
		                                        .addComponent(jLabel1).addComponent(entry, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE).addComponent(jButtonCentruj))
		                        .addGroup(layout.createSequentialGroup().addComponent(jJenVZobrazenych).addComponent(jRegularniVyrazy)))));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING) // vGrou
		        .addGroup(layout.createSequentialGroup() // v1
		                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE) // v2
		                        .addComponent(jLabel1).addComponent(entry, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(jButtonCentruj))
		                .addGroup(layout.createParallelGroup().addComponent(jJenVZobrazenych).addComponent(jRegularniVyrazy)).addComponent(status)
		                .addComponent(jKeskovaciTabulka, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)));

		pack();
	}

	protected void setReferencniBod(final Wgs wgs) {
		if (wgs.equals(referencniBod)) {
			return;
		}
		referencniBod = wgs;
		search();
	}

	void message(final String msg) {
		status.setText(msg);
	}

	private void registerEvents() {
		jRegularniVyrazy.addChangeListener(new SpousteniVyhledavace());
		jJenVZobrazenych.addChangeListener(new SpousteniVyhledavace());

		jButtonCentruj.addActionListener(e -> {

			final Nalezenec nalezenec = jKeskovaciTabulka.getCurrent();
			if (nalezenec != null) {
				final Kesoid kes = nalezenec.getKes();
				poziceModel.setPozice(kes.getMainWpt());
				vyrezModel.vystredovatNaPozici();
				// Board.eveman.fire(new PoziceChangedEvent(kes.getMainWpt(), true) );
			}
			// System.out.println("NEJBLIZSI KES: " + nejblizsiKes);
		});

		// Board.eveman.registerWeakly(this);

		// jKeskovaciTabulka.getMod
		jKeskovaciTabulka.addListSelectionListener(aE -> jButtonCentruj.setEnabled(jKeskovaciTabulka.getCurrent() != null));
	}

}
