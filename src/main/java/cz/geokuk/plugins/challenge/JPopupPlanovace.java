package cz.geokuk.plugins.challenge;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JSlide0;
import cz.geokuk.framework.RozmisteniPrekryvu;

/**
 * Popup plánovače challenge pod odznakem pruhu: typ a pravidla, shrnutí, chybějící sektory od nejbližšího kandidáta, další chybějící a filtr „jen kandidáti“. Leží ve vrstvě mapy a
 * myš zachytává jen ve svém obdélníku; zavře se klávesou Esc, kliknutím mimo (klik se přitom neztratí) nebo znovu kliknutím na odznak.
 */
public class JPopupPlanovace extends JSlide0 implements AfterInjectInit {

	private static final long serialVersionUID = 1L;

	static final int RADKU_SEZNAMU = 7;

	final transient Obsah obsah = new Obsah();
	private transient ChallengeModel model;
	private transient PlanovacChallengeSluzba sluzba;
	private transient Runnable naNastaveni;
	private boolean otevreno;
	private transient AWTEventListener mimo;
	private transient Supplier<? extends Collection<Rectangle>> prekazky = Collections::emptyList;

	public JPopupPlanovace() {
		setOpaque(false);
		setLayout(null);
		add(obsah);
		setVisible(false);
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "zavriPopupPlanovace");
		getActionMap().put("zavriPopupPlanovace", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return otevreno;
			}

			@Override
			public void actionPerformed(final ActionEvent e) {
				zavri();
			}
		});
		obsah.dalsi.addActionListener(e -> dalsiChybejici());
		obsah.kandidati.addActionListener(e -> prepniKandidaty());
		obsah.nastaveni.addActionListener(e -> {
			if (naNastaveni != null) {
				zavri();
				naNastaveni.run();
			}
		});
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void inject(final PlanovacChallengeSluzba sluzba) {
		this.sluzba = sluzba;
	}

	/** Nastavení… otevře dialog plánovače z menu. */
	public void inject(final cz.geokuk.core.program.Akce akce) {
		setNaNastaveni(() -> akce.challengeNastaveniAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "nastaveni")));
	}

	/** Panely v rozích mapy a legenda, do kterých popup nesmí zasahovat (souřadnice mapy). */
	public void setPrekazky(final Supplier<? extends Collection<Rectangle>> prekazky) {
		this.prekazky = prekazky;
	}

	/** Co udělá tlačítko Nastavení…; bez něj je tlačítko nedostupné. */
	public void setNaNastaveni(final Runnable naNastaveni) {
		this.naNastaveni = naNastaveni;
		obsah.nastaveni.setEnabled(naNastaveni != null);
	}

	@Override
	public void initAfterInject() {
		obsah.nastaveni.setEnabled(naNastaveni != null);
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		if (!otevreno) {
			return;
		}
		if (model == null || !model.jePruhViditelny()) {
			zavri();
		} else {
			obnov();
		}
	}

	public boolean jeOtevreno() {
		return otevreno;
	}

	/** Otevře popup, nebo ho zavře, když je otevřený (klik na odznak). */
	public void prepni() {
		if (otevreno) {
			zavri();
		} else {
			otevri();
		}
	}

	void otevri() {
		if (model == null || !model.jePruhViditelny()) {
			return;
		}
		otevreno = true;
		obnov();
		setVisible(true);
		if (mimo == null) {
			mimo = this::mysKdekoli;
			try {
				Toolkit.getDefaultToolkit().addAWTEventListener(mimo, AWTEvent.MOUSE_EVENT_MASK);
			} catch (final SecurityException e) {
				mimo = null;
			}
		}
		revalidate();
		repaint();
	}

	void zavri() {
		otevreno = false;
		if (mimo != null) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(mimo);
			mimo = null;
		}
		setVisible(false);
		repaint();
	}

	/** Při odebrání z okna se popup zavře, ať po něm nezůstane globální posluchač myši. */
	@Override
	public void removeNotify() {
		if (otevreno) {
			zavri();
		}
		super.removeNotify();
	}

	/** Stisk myši mimo popup ho zavře; událost dál normálně pokračuje. Stisk na odznaku nechá na odznaku, ten popup přepíná sám. */
	void mysKdekoli(final AWTEvent e) {
		if (e.getID() != MouseEvent.MOUSE_PRESSED || !otevreno || !(e instanceof MouseEvent)) {
			return;
		}
		final MouseEvent me = (MouseEvent) e;
		final Component zdroj = me.getComponent();
		if (zdroj == null || SwingUtilities.isDescendingFrom(zdroj, obsah)) {
			return;
		}
		final Point p = SwingUtilities.convertPoint(zdroj, me.getPoint(), this);
		if (JPruhPlanovace.oblastOdznaku(getWidth()).contains(p)) {
			return;
		}
		zavri();
	}

	@Override
	public boolean contains(final int x, final int y) {
		return otevreno && obsah.getBounds().contains(x, y);
	}

	@Override
	public void doLayout() {
		obsah.setBounds(poloha(getSize(), obsah.getPreferredSize(), prekazky.get()));
	}

	/** Popup v souřadnicích mapy, viz {@link RozmisteniPrekryvu#popupPlanovace}; měřítko dole je také překážka. */
	static Rectangle poloha(final Dimension mapa, final Dimension pozadovana, final Collection<Rectangle> prekazky) {
		final List<Rectangle> vse = new ArrayList<>(prekazky);
		vse.add(RozmisteniPrekryvu.meritko(mapa));
		return RozmisteniPrekryvu.popupPlanovace(mapa, JPruhPlanovace.oblastOdznaku(mapa.width), pozadovana, vse);
	}

	/** Stejná challenge jako v pruhu. */
	private Challenge challenge() {
		return model == null ? null : model.zobrazena();
	}

	void obnov() {
		final Challenge c = challenge();
		if (c != null) {
			final boolean jenKandidati = model.stavPruhu().jenKandidati();
			if (c.typ() == Challenge.Typ.SEKTORY_360) {
				obsah.nastav(c, model.vysledek(c), model.stavPredpoctu(c), jenKandidati);
			} else {
				obsah.nastavPolicka(c, model.vysledekPolicek(c), model.stavPredpoctu(c), jenKandidati);
			}
		}
		revalidate();
		repaint();
	}

	private void dalsiChybejici() {
		final Challenge c = challenge();
		if (c != null && sluzba != null && sluzba.planovac() != null) {
			sluzba.planovac().dalsiChybejici(c);
		}
	}

	private void prepniKandidaty() {
		final Challenge c = challenge();
		if (c == null) {
			return;
		}
		try {
			model.setJenKandidatiPro(obsah.kandidati.isSelected() ? c : null);
		} catch (final ChallengeChyba e) {
			obsah.kandidati.setSelected(false);
		}
	}

	/** Vlastní obsah popupu; barvy a písmo z aktuálního vzhledu. */
	static final int NEJVETSI_SIRKA_NADPISU = 380;

	static final class Obsah extends JPanel {
		private static final long serialVersionUID = 1L;

		/** Název challenge může být dlouhý (šablony mají GC kód a anglický název): zkrátí se „…“ a plný je v tooltipu. */
		final cz.geokuk.util.gui.JZkracenyLabel nadpis = new cz.geokuk.util.gui.JZkracenyLabel(NEJVETSI_SIRKA_NADPISU);
		final JButton nastaveni = new JButton("Nastavení…");
		final JLabel typ = new JLabel();
		final JLabel stred = new JLabel();
		final JLabel souhrn = new JLabel();
		final JLabel nadpisSeznamu = new JLabel(TextyPopupuPlanovace.nadpisSeznamu(Challenge.Typ.SEKTORY_360));
		final JProgressBar postup = new JProgressBar(0, 360);
		final DefaultListModel<String> radky = new DefaultListModel<>();
		final JList<String> seznam = new JList<>(radky);
		final JLabel nejiste = new JLabel();
		final JButton dalsi = new JButton("▶ Další chybějící  F4");
		final JToggleButton kandidati = new JToggleButton("Zobrazit jen kandidáty");

		Obsah() {
			super(new GridBagLayout());
			setOpaque(true);
			nadpis.setFont(nadpis.getFont().deriveFont(Font.BOLD));
			// Procenta jsou ve shrnutí: text přes výplň má v některých vzhledech malý kontrast.
			postup.setStringPainted(false);
			postup.setBorderPainted(true);
			seznam.setVisibleRowCount(RADKU_SEZNAMU);
			seznam.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			seznam.setPrototypeCellValue("359°  99 kandidátů  nejbližší 99,9 km");
			for (final AbstractButton b : new AbstractButton[] { nastaveni, dalsi, kandidati }) {
				b.setFocusable(false);
			}
			final GridBagConstraints g = new GridBagConstraints();
			g.gridx = 0;
			g.gridy = 0;
			g.anchor = GridBagConstraints.WEST;
			g.insets = new Insets(0, 0, 4, 8);
			g.weightx = 1;
			add(nadpis, g);
			g.gridx = 1;
			g.weightx = 0;
			g.insets = new Insets(0, 0, 4, 0);
			add(nastaveni, g);
			g.gridx = 0;
			g.gridwidth = 2;
			g.fill = GridBagConstraints.HORIZONTAL;
			g.insets = new Insets(0, 0, 4, 0);
			for (final JComponent c : new JComponent[] { typ, stred, souhrn, postup }) {
				g.gridy++;
				add(c, g);
			}
			g.gridy++;
			add(nadpisSeznamu, g);
			g.gridy++;
			g.fill = GridBagConstraints.BOTH;
			g.weighty = 1;
			add(new JScrollPane(seznam), g);
			g.weighty = 0;
			g.fill = GridBagConstraints.HORIZONTAL;
			g.gridy++;
			add(nejiste, g);
			final JPanel tlacitka = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			tlacitka.setOpaque(false);
			tlacitka.add(dalsi);
			tlacitka.add(Box.createHorizontalStrut(8));
			tlacitka.add(kandidati);
			g.gridy++;
			g.insets = new Insets(4, 0, 0, 0);
			add(tlacitka, g);
			obnovVzhled();
		}

		@Override
		public void updateUI() {
			super.updateUI();
			obnovVzhled();
		}

		/** Rámeček a barva upozornění podle pozadí vzhledu (světlý i tmavý skin). */
		private void obnovVzhled() {
			final Color pozadi = UIManager.getColor("Panel.background");
			final Color hrana = UIManager.getColor("Separator.foreground");
			setBorder(new CompoundBorder(new LineBorder(hrana == null ? Color.GRAY : hrana), new EmptyBorder(8, 10, 8, 10)));
			if (nejiste != null) {
				nejiste.setForeground(jeTmave(pozadi) ? new Color(0xFFD080) : new Color(0x9A5A00));
			}
			if (postup != null) {
				postup.setForeground(jeTmave(pozadi) ? VYPLN_TMAVA : VYPLN_SVETLA);
				postup.setBackground(jeTmave(pozadi) ? DRAHA_TMAVA : DRAHA_SVETLA);
			}
		}

		/** Výplň a dráha ukazatele postupu; barvy vzhledu mají někde výplň skoro stejnou jako dráhu. */
		static final Color VYPLN_SVETLA = new Color(0x2F6FC0);
		static final Color DRAHA_SVETLA = new Color(0xE6E6E6);
		static final Color VYPLN_TMAVA = new Color(0x7FB2F0);
		static final Color DRAHA_TMAVA = new Color(0x2E2E2E);

		static boolean jeTmave(final Color c) {
			return c != null && c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114 < 128_000;
		}

		/** Společná hlavička; vrátí false, když výsledek ještě není a ukáže se jen průběh. */
		private boolean zahlavi(final Challenge c, final boolean maVysledek, final StavPredpoctu stav, final boolean jenKandidati) {
			nadpis.setPlnyText(TextyPopupuPlanovace.nadpis(c));
			typ.setText("Typ: " + StitkyPruhu.typ(c.typ()) + "    Pravidla: " + TextyPopupuPlanovace.pravidla(c.zdroj()));
			stred.setText("Střed: " + TextyPopupuPlanovace.stred(c.stred()));
			stred.setVisible(c.typ() == Challenge.Typ.SEKTORY_360);
			nadpisSeznamu.setText(TextyPopupuPlanovace.nadpisSeznamu(c.typ()));
			kandidati.setSelected(jenKandidati);
			radky.clear();
			nejiste.setText(" ");
			if (maVysledek) {
				return true;
			}
			souhrn.setText(stav.faze() == StavPredpoctu.Faze.POCITA && stav.celkem() > 0 ? "Počítá se… " + stav.hotovo() + " z " + stav.celkem() + " keší" : "Počítá se…");
			postup.setValue(0);
			postup.setString("…");
			dalsi.setEnabled(false);
			return false;
		}

		void nastav(final Challenge c, final VysledekSektoru v, final StavPredpoctu stav, final boolean jenKandidati) {
			if (!zahlavi(c, v != null, stav, jenKandidati)) {
				return;
			}
			souhrn.setText(TextyPopupuPlanovace.souhrn(v));
			postup.setMaximum(360);
			postup.setValue(v.pocetObsazenych());
			postup.setString(TextyPopupuPlanovace.procenta(v));
			for (final int s : TextyPopupuPlanovace.serazeneChybejici(v)) {
				radky.addElement(TextyPopupuPlanovace.radekSektoru(s, v.kandidati(s)));
			}
			final String n = TextyPopupuPlanovace.nejiste(v);
			nejiste.setText(n.isEmpty() ? " " : n);
			dalsi.setEnabled(!v.chybejici().isEmpty());
		}

		/** D/T matice a kalendář: shrnutí a chybějící políčka nebo dny v pořadí z výpočtu. */
		void nastavPolicka(final Challenge c, final VysledekPolicek p, final StavPredpoctu stav, final boolean jenKandidati) {
			if (!zahlavi(c, p != null, stav, jenKandidati)) {
				return;
			}
			souhrn.setText(TextyPopupuPlanovace.souhrn(p));
			postup.setMaximum(Math.max(1, p.celkem()));
			postup.setValue(p.celkem() - p.pocetChybejicich());
			postup.setString(TextyPopupuPlanovace.procenta(p));
			for (final String s : p.stitkyChybejicich()) {
				radky.addElement(s);
			}
			dalsi.setEnabled(p.pocetChybejicich() > 0);
		}
	}
}
