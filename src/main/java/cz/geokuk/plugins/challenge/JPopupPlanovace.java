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
 * Popup plánovače challenge pod odznakem pruhu: typ a pravidla, shrnutí, chybějící sektory po kružnici (klik na řádek vybere sektor), další sektor a filtr „jen kandidáti“. Leží ve vrstvě mapy a
 * myš zachytává jen ve svém obdélníku; zavře se klávesou Esc, kliknutím mimo (klik se přitom neztratí) nebo znovu kliknutím na odznak.
 */
public class JPopupPlanovace extends JSlide0 implements AfterInjectInit {

	private static final long serialVersionUID = 1L;

	static final int RADKU_SEZNAMU = 7;

	final transient Obsah obsah = new Obsah();
	private transient ChallengeModel model;
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
		obsah.dalsi.addActionListener(e -> ChallengeAkce.posun(model, true));
		obsah.setNaKlikRadku(this::klikNaRadek);
		obsah.matice.setNaVyber(this::vyberPolicko);
		obsah.vsechnaPolicka.addActionListener(e -> vyberPolicko(null));
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
		if (JPruhPlanovace.oblastOdznaku(getWidth(), prekazky.get()).contains(p)) {
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
		return RozmisteniPrekryvu.popupPlanovace(mapa, JPruhPlanovace.oblastOdznaku(mapa.width, prekazky), pozadovana, vse);
	}

	/** Stejná challenge jako v pruhu. */
	private Challenge challenge() {
		return model == null ? null : model.zobrazena();
	}

	void obnov() {
		final Challenge c = challenge();
		obsah.omezeniStitku.setText(model == null ? " " : TextyPopupuPlanovace.omezeniStitku(model.kandidatuVeVyrezu()));
		// Prázdný řádek by v popupu nechal mezeru nad tlačítky.
		obsah.omezeniStitku.setVisible(!obsah.omezeniStitku.getText().trim().isEmpty());
		if (c != null) {
			final boolean jenKandidati = model.stavPruhu().jenKandidati();
			if (c.typ() == Challenge.Typ.SEKTORY_360) {
				obsah.nastav(c, model.vysledek(c), model.stavPredpoctu(c), jenKandidati, model.vybranySektor(c));
			} else {
				obsah.nastavPolicka(c, model.vysledekPolicek(c), model.stavPredpoctu(c), jenKandidati, model.vybranePolicko(c));
			}
		}
		revalidate();
		repaint();
	}

	/** Klik na políčko matice D/T: vybere ho a zapne „jen kandidáti“; null výběr zruší a filtr nechá. */
	void vyberPolicko(final String stitek) {
		final Challenge c = challenge();
		if (c == null || c.typ() != Challenge.Typ.DT_MATICE) {
			return;
		}
		model.setVybranePolicko(c, stitek);
		if (stitek != null) {
			try {
				model.setJenKandidatiPro(c);
			} catch (final ChallengeChyba e) {
				// Neaktivní challenge filtr nedostane; výběr políčka platí.
			}
		}
	}

	/** Klik na řádek s kandidáty vybere jeho sektor nebo políčko; klik na vybraný výběr zruší. Keš se nevybírá a mapa se nevystřeďuje. */
	void klikNaRadek(final Object polozka) {
		final Challenge c = challenge();
		if (c == null || polozka == null) {
			return;
		}
		if (polozka instanceof Integer) {
			final int s = (Integer) polozka;
			model.setVybranySektor(c, model.vybranySektor(c) == s ? PosunVyberu.ZADNY_SEKTOR : s);
		} else if (c.typ() == Challenge.Typ.DT_MATICE) {
			model.setVybranePolicko(c, polozka.equals(model.vybranePolicko(c)) ? null : (String) polozka);
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
		final JButton nastaveni = new JButton("Nastavení plánovače…");
		final JLabel typ = new JLabel();
		final JLabel stred = new JLabel();
		final JLabel souhrn = new JLabel();
		final JLabel nadpisSeznamu = new JLabel(TextyPopupuPlanovace.nadpisSeznamu(Challenge.Typ.SEKTORY_360));
		final JProgressBar postup = new JProgressBar(0, 360);
		final DefaultListModel<String> radky = new DefaultListModel<>();
		final JList<String> seznam = new JList<>(radky);
		final JLabel bezKandidatu = new JLabel(" ");
		final JMaticeDT matice = new JMaticeDT();
		final JLabel legenda = new JLabel(TextyPopupuPlanovace.LEGENDA_MATICE);
		final JButton vsechnaPolicka = new JButton("✕ Všechna políčka");
		private final JScrollPane posuvnik = new JScrollPane(seznam);
		final JLabel napoveda = new JLabel(TextyPopupuPlanovace.napovedaSeznamu(Challenge.Typ.SEKTORY_360));
		final JLabel nejiste = new JLabel();
		final JLabel omezeniStitku = new JLabel(" ");
		final JButton dalsi = new JButton("▶ " + ChallengeAkce.nazevPosunu(Challenge.Typ.SEKTORY_360, true));
		/** Sektor (Integer) nebo štítek políčka (String) každého řádku seznamu, null u řádku, který nejde vybrat (den kalendáře). */
		final List<Object> polozky = new ArrayList<>();
		private Challenge.Typ typSeznamu = Challenge.Typ.SEKTORY_360;
		private int podMysi = -1;
		private boolean nastavujeSe;
		private transient java.util.function.Consumer<Object> naKlikRadku = p -> {};
		final JToggleButton kandidati = new JToggleButton("Zobrazit jen kandidáty  F4");

		Obsah() {
			super(new GridBagLayout());
			setOpaque(true);
			nadpis.setFont(nadpis.getFont().deriveFont(Font.BOLD));
			// Procenta jsou ve shrnutí: text přes výplň má v některých vzhledech malý kontrast.
			postup.setStringPainted(false);
			postup.setBorderPainted(true);
			seznam.setVisibleRowCount(RADKU_SEZNAMU);
			seznam.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			seznam.setPrototypeCellValue("359°  99 kandidátů  nejbližší 99,9 km od středu");
			seznam.setCellRenderer(new RadekRenderer());
			final java.awt.event.MouseAdapter mys = new java.awt.event.MouseAdapter() {
				@Override
				public void mouseMoved(final MouseEvent e) {
					najeti(radekNa(e.getPoint()));
				}

				@Override
				public void mouseExited(final MouseEvent e) {
					najeti(-1);
				}

				@Override
				public void mouseClicked(final MouseEvent e) {
					final int i = radekNa(e.getPoint());
					if (SwingUtilities.isLeftMouseButton(e) && i >= 0 && polozky.get(i) != null) {
						naKlikRadku.accept(polozky.get(i));
					}
				}
			};
			seznam.addMouseListener(mys);
			seznam.addMouseMotionListener(mys);
			// Výběr v seznamu ukazuje výběr v modelu; mění se jen klikem na řádek.
			seznam.setSelectionModel(new DefaultListSelectionModel() {
				private static final long serialVersionUID = 1L;

				@Override
				public void setSelectionInterval(final int a, final int b) {
					if (nastavujeSe) {
						super.setSelectionInterval(a, b);
					}
				}

				@Override
				public void addSelectionInterval(final int a, final int b) {
					setSelectionInterval(a, b);
				}
			});
			seznam.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			napoveda.setFont(napoveda.getFont().deriveFont(Font.PLAIN, napoveda.getFont().getSize2D() - 1));
			legenda.setFont(legenda.getFont().deriveFont(Font.PLAIN, legenda.getFont().getSize2D() - 1));
			vsechnaPolicka.setToolTipText("Zrušit výběr políčka – na mapě kandidáti všech chybějících políček");
			for (final AbstractButton b : new AbstractButton[] { nastaveni, dalsi, kandidati, vsechnaPolicka }) {
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
			add(posuvnik, g);
			g.fill = GridBagConstraints.NONE;
			g.anchor = GridBagConstraints.WEST;
			add(matice, g);
			g.weighty = 0;
			g.gridy++;
			add(legenda, g);
			g.fill = GridBagConstraints.HORIZONTAL;
			g.gridy++;
			add(bezKandidatu, g);
			g.gridy++;
			add(napoveda, g);
			g.gridy++;
			add(nejiste, g);
			g.gridy++;
			add(omezeniStitku, g);
			final JPanel tlacitka = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			tlacitka.setOpaque(false);
			tlacitka.add(dalsi);
			tlacitka.add(Box.createHorizontalStrut(8));
			tlacitka.add(kandidati);
			tlacitka.add(Box.createHorizontalStrut(8));
			tlacitka.add(vsechnaPolicka);
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
			typ.setText("Typ: " + StitkyPruhu.typ(c.typ()) + (c.typ() == Challenge.Typ.SEKTORY_360 ? "    Pravidla: " + TextyPopupuPlanovace.pravidla(c.zdroj()) : ""));
			stred.setText("Střed: " + TextyPopupuPlanovace.stred(c.stred()));
			stred.setVisible(c.typ() == Challenge.Typ.SEKTORY_360);
			nadpisSeznamu.setText(TextyPopupuPlanovace.nadpisSeznamu(c.typ()));
			napoveda.setText(TextyPopupuPlanovace.napovedaSeznamu(c.typ()));
			napoveda.setVisible(c.typ() != Challenge.Typ.KALENDAR);
			dalsi.setText("▶ " + ChallengeAkce.nazevPosunu(c.typ(), true));
			dalsi.setVisible(c.typ() != Challenge.Typ.KALENDAR);
			typSeznamu = c.typ();
			final boolean matice = c.typ() == Challenge.Typ.DT_MATICE;
			posuvnik.setVisible(!matice);
			this.matice.setVisible(matice);
			legenda.setVisible(matice);
			bezKandidatu.setVisible(!matice);
			vsechnaPolicka.setVisible(false);
			if (matice) {
				this.matice.nastav(null);
			}
			kandidati.setSelected(jenKandidati);
			radky.clear();
			polozky.clear();
			podMysi = -1;
			bezKandidatu.setText(" ");
			bezKandidatu.setToolTipText(TextyPopupuPlanovace.tipBezKandidatu(c.typ()));
			nejiste.setText(" ");
			nejiste.setVisible(false);
			if (maVysledek) {
				return true;
			}
			// Bez výsledku by matice ukázala všechna políčka jako splněná.
			this.matice.setVisible(false);
			legenda.setVisible(false);
			souhrn.setText(stav.faze() == StavPredpoctu.Faze.POCITA && stav.celkem() > 0 ? "Počítá se… " + stav.hotovo() + " z " + stav.celkem() + " keší" : "Počítá se…");
			postup.setValue(0);
			postup.setString("…");
			dalsi.setEnabled(false);
			return false;
		}

		void nastav(final Challenge c, final VysledekSektoru v, final StavPredpoctu stav, final boolean jenKandidati, final int vybranySektor) {
			if (!zahlavi(c, v != null, stav, jenKandidati)) {
				return;
			}
			souhrn.setText(TextyPopupuPlanovace.souhrn(v));
			postup.setMaximum(360);
			postup.setValue(v.pocetObsazenych());
			postup.setString(TextyPopupuPlanovace.procenta(v));
			int vybrany = -1;
			int bez = 0;
			for (final int s : TextyPopupuPlanovace.chybejiciPoKruznici(v)) {
				final VysledekSektoru.Kandidati k = v.kandidati(s);
				if (k == null || k.pocet() == 0) {
					bez++;
					continue;
				}
				if (s == vybranySektor) {
					vybrany = radky.size();
				}
				radky.addElement(TextyPopupuPlanovace.radekSektoru(s, k));
				polozky.add(s);
			}
			vyberRadek(vybrany);
			bezKandidatu.setText(bez == 0 ? " " : TextyPopupuPlanovace.bezKandidatu(c.typ(), bez));
			final String n = TextyPopupuPlanovace.nejiste(v);
			nejiste.setText(n.isEmpty() ? " " : "<html><body style='width: " + NEJVETSI_SIRKA_NADPISU + "px'>" + n + "</body></html>");
			nejiste.setVisible(!n.isEmpty());
			dalsi.setEnabled(polozky.stream().anyMatch(java.util.Objects::nonNull));
		}

		/** D/T matice a kalendář: shrnutí a chybějící políčka nebo dny v pořadí z výpočtu. */
		void nastavPolicka(final Challenge c, final VysledekPolicek p, final StavPredpoctu stav, final boolean jenKandidati, final String vybranePolicko) {
			if (!zahlavi(c, p != null, stav, jenKandidati)) {
				return;
			}
			souhrn.setText(TextyPopupuPlanovace.souhrn(p));
			postup.setMaximum(Math.max(1, p.celkem()));
			postup.setValue(p.celkem() - p.pocetChybejicich());
			postup.setString(TextyPopupuPlanovace.procenta(p));
			if (c.typ() == Challenge.Typ.DT_MATICE) {
				matice.nastav(p);
				matice.setVybrane(vybranePolicko);
				nadpisSeznamu.setText(TextyPopupuPlanovace.vybranePolicko(vybranePolicko, vybranePolicko == null ? 0 : p.pocetKandidatu(vybranePolicko),
						vybranePolicko != null && p.jePlne(vybranePolicko), jenKandidati));
				vsechnaPolicka.setVisible(vybranePolicko != null);
				dalsi.setEnabled(p.pocetKandidatuPoPolickach().values().stream().anyMatch(n -> n > 0));
				return;
			}
			for (final String s : p.stitkyChybejicich()) {
				radky.addElement(s);
				polozky.add(null);
			}
			dalsi.setEnabled(false);
		}

		void setNaKlikRadku(final java.util.function.Consumer<Object> naKlikRadku) {
			this.naKlikRadku = naKlikRadku;
		}

		private void vyberRadek(final int i) {
			nastavujeSe = true;
			try {
				if (i < 0) {
					seznam.clearSelection();
				} else {
					seznam.setSelectedIndex(i);
					seznam.ensureIndexIsVisible(i);
				}
			} finally {
				nastavujeSe = false;
			}
		}

		private int radekNa(final Point p) {
			final int i = seznam.locationToIndex(p);
			return i >= 0 && seznam.getCellBounds(i, i).contains(p) ? i : -1;
		}

		/** Řádek pod myší, který jde vybrat: ruka, podtržení a tooltip. */
		private void najeti(final int i) {
			if (i == podMysi) {
				return;
			}
			podMysi = i;
			final boolean klikaci = i >= 0 && polozky.get(i) != null;
			seznam.setCursor(klikaci ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
			seznam.setToolTipText(klikaci ? TextyPopupuPlanovace.tipRadku(typSeznamu, seznam.isSelectedIndex(i)) : null);
			seznam.repaint();
		}

		/** Řádek pod myší, který jde vybrat, je podtržený a podbarvený. */
		private final class RadekRenderer extends DefaultListCellRenderer {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(final JList<?> l, final Object hodnota, final int i, final boolean vybrany, final boolean fokus) {
				final JLabel r = (JLabel) super.getListCellRendererComponent(l, hodnota, i, vybrany, false);
				final boolean klikaci = i >= 0 && i < polozky.size() && polozky.get(i) != null;
				if (klikaci && i == podMysi) {
					r.setText("<html><u>" + hodnota + "</u></html>");
					if (!vybrany) {
						final Color z = UIManager.getColor("List.selectionBackground");
						final Color b = l.getBackground();
						if (z != null && b != null) {
							r.setBackground(new Color((z.getRed() + 3 * b.getRed()) / 4, (z.getGreen() + 3 * b.getGreen()) / 4, (z.getBlue() + 3 * b.getBlue()) / 4));
						}
					}
				}
				return r;
			}
		}
	}
}
