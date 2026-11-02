package cz.geokuk.plugins.kesoid.mvc;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import cz.geokuk.plugins.kesoid.importek.*;
import cz.geokuk.plugins.kesoid.importek.StavZdroju.StavVyberu;
import cz.geokuk.util.lang.FString;

/**
 * Tabulka zdrojů Načíst | Zdroj | Velikost | WP | Stav: řádek typu zdroje a pod ním jeho soubory či databáze. Bez typu ukazuje všechny typy, s typem jen jeden.
 * Táž komponenta je v popupech stavového řádku i v okně Přehled zdrojů.
 */
public class JTabulkaZdroju extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Locale CS = new Locale("cs");

	static final int SL_NACIST = 0;
	static final int SL_ZDROJ = 1;
	static final int SL_VELIKOST = 2;
	static final int SL_WP = 3;
	static final int SL_STAV = 4;
	private static final String[] SLOUPCE = { "Načíst", "Zdroj", "Velikost", "WP", "Stav" };
	private static final int[] SIRKY = { 56, 220, 100, 150, 220 };

	static final int PLNA_SIRKA = 776;
	/** Pod touto šířkou okna odpadne sloupec Velikost. */
	static final int PRAH_UZKE = 900;
	static final int VYSKA_RADKU = 22;

	static final Color BARVA_DUPLICIT = new Color(0x9A5B00);

	/** Řádek je buď typ zdroje (polozka == null), nebo jeho položka. */
	static final class Radek {
		final TypZdroje typ;
		final StavPolozky polozka;
		/** Pořadí položky ve skupině typu, pro střídavé podbarvení. */
		final int poradi;

		Radek(final TypZdroje typ, final StavPolozky polozka, final int poradi) {
			this.typ = typ;
			this.polozka = polozka;
			this.poradi = poradi;
		}
	}

	/** Typ, jehož položky se ukazují; null jsou všechny typy. */
	private final TypZdroje zobrazenyTyp;
	private final List<Radek> radky = new ArrayList<>();
	private final JTable tabulka;
	private final JScrollPane scroll;
	private final TableColumn sloupecVelikost;
	private final JLabel casDat = new JLabel();
	private StavZdroju stav = StavZdroju.PRAZDNY;
	private final Tocitko tocitko = new Tocitko(this, () -> getTabulka().repaint());
	private Set<TypZdroje> povolene = EnumSet.allOf(TypZdroje.class);
	private OvladaniZdroju ovladani;
	private boolean uzka;
	private int sirka = PLNA_SIRKA;
	/** Nejvyšší výška celé tabulky; nad ní se řádky posouvají. */
	private int maxVyska = Integer.MAX_VALUE;

	private final AbstractTableModel tm = new AbstractTableModel() {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return radky.size();
		}

		@Override
		public int getColumnCount() {
			return SLOUPCE.length;
		}

		@Override
		public String getColumnName(final int c) {
			return SLOUPCE[c];
		}

		@Override
		public Object getValueAt(final int r, final int c) {
			return text(radky.get(r), c);
		}
	};

	public JTabulkaZdroju(final TypZdroje zobrazenyTyp) {
		super(new BorderLayout(0, 6));
		this.zobrazenyTyp = zobrazenyTyp;
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		final JPanel odkazy = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		odkazy.add(odkaz("Vše zapnout", true));
		odkazy.add(new JLabel("|"));
		odkazy.add(odkaz("Vše vypnout", false));
		add(odkazy, BorderLayout.NORTH);

		tabulka = new JTable(tm) {
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(final MouseEvent e) {
				final int r = rowAtPoint(e.getPoint());
				final int c = columnAtPoint(e.getPoint());
				return r < 0 || c < 0 ? null : tooltip(radky.get(r), convertColumnIndexToModel(c));
			}
		};
		tabulka.setRowHeight(VYSKA_RADKU);
		tabulka.setShowGrid(false);
		tabulka.setIntercellSpacing(new Dimension(0, 0));
		tabulka.setFocusable(false);
		tabulka.setRowSelectionAllowed(false);
		tabulka.getTableHeader().setReorderingAllowed(false);
		tabulka.setDefaultRenderer(Object.class, new Vykreslovac());
		for (int i = 0; i < SIRKY.length; i++) {
			tabulka.getColumnModel().getColumn(i).setPreferredWidth(SIRKY[i]);
		}
		tabulka.getColumnModel().getColumn(SL_NACIST).setMaxWidth(SIRKY[SL_NACIST]);
		tabulka.addMouseListener(new MouseAdapter() {
			private boolean menuPriStisku;

			@Override
			public void mousePressed(final MouseEvent e) {
				menuPriStisku = e.isPopupTrigger();
				ukazMenu(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				menuPriStisku |= e.isPopupTrigger();
				ukazMenu(e);
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (menuPriStisku || SwingUtilities.isRightMouseButton(e)) {
					return;
				}
				final int r = tabulka.rowAtPoint(e.getPoint());
				final int c = tabulka.columnAtPoint(e.getPoint());
				if (r >= 0 && c >= 0 && tabulka.convertColumnIndexToModel(c) == SL_NACIST) {
					klik(radky.get(r), jenTato(e));
				}
			}
		});
		sloupecVelikost = tabulka.getColumnModel().getColumn(SL_VELIKOST);
		scroll = new JScrollPane(tabulka);
		add(scroll, BorderLayout.CENTER);

		casDat.setForeground(Color.GRAY);
		casDat.setVisible(false);
		add(casDat, BorderLayout.SOUTH);
		obnov(StavZdroju.PRAZDNY);
	}

	public void setOvladani(final OvladaniZdroju ovladani) {
		this.ovladani = ovladani;
	}

	public void obnov(final StavZdroju novy) {
		stav = novy;
		radky.clear();
		for (final TypZdroje typ : TypZdroje.values()) {
			if (zobrazenyTyp != null ? zobrazenyTyp != typ : !povolene.contains(typ)) {
				continue;
			}
			radky.add(new Radek(typ, null, 0));
			int poradi = 0;
			for (final StavPolozky p : stav.getPolozky(typ)) {
				radky.add(new Radek(typ, p, poradi++));
			}
		}
		tm.fireTableDataChanged();
		prepocitejVelikost();
		tocitko.nastav(radky.stream().anyMatch(r -> r.polozka != null ? r.polozka.isNacitat() && r.polozka.getStav() == StavZdroje.NACITA_SE
				: stav.getStavTypu(r.typ) == StavZdroje.NACITA_SE));
	}

	boolean animuje() {
		return tocitko.bezi();
	}

	/** Čas nejmladšího načteného souboru k zobrazení; bez načtených zdrojů prázdný. */
	public static String casDat(final long nejmladsi) {
		return nejmladsi <= 0 ? "" : String.format("%tF %<tR", nejmladsi);
	}

	/** Typy zdrojů zapnuté v Nastavení; jen ty má tabulka všech typů. */
	public void setPovoleneTypy(final Set<TypZdroje> nove) {
		povolene = EnumSet.noneOf(TypZdroje.class);
		povolene.addAll(nove);
		obnov(stav);
	}

	/** Čas nejmladšího načteného souboru; jen v tabulce všech typů. */
	public void setCasDat(final String cas) {
		casDat.setText(cas == null || cas.isEmpty() ? "" : "Nejnovější data: " + cas);
		casDat.setVisible(zobrazenyTyp == null && cas != null && !cas.isEmpty());
	}

	/** V úzkém okně se tabulka zúží na šířku okna a odpadne sloupec Velikost. */
	public void nastavDostupnouSirku(final int sirkaOkna) {
		final boolean nove = sirkaOkna < PRAH_UZKE;
		if (nove != uzka) {
			uzka = nove;
			if (nove) {
				tabulka.removeColumn(sloupecVelikost);
			} else {
				tabulka.addColumn(sloupecVelikost);
				tabulka.moveColumn(tabulka.getColumnCount() - 1, SL_VELIKOST);
			}
		}
		sirka = Math.min(PLNA_SIRKA, sirkaOkna);
		prepocitejVelikost();
	}

	boolean isUzka() {
		return uzka;
	}

	/** Výška, kterou má tabulka i s tlačítky k dispozici; víc řádků se posouvá. */
	public void nastavDostupnouVysku(final int vyska) {
		maxVyska = vyska;
		prepocitejVelikost();
	}

	private void prepocitejVelikost() {
		final int hlavicka = tabulka.getTableHeader().getPreferredSize().height + 4;
		final int vsechnyRadky = VYSKA_RADKU * Math.max(1, radky.size()) + hlavicka;
		scroll.setPreferredSize(new Dimension(sirka - 16, vsechnyRadky));
		final int navic = getPreferredSize().height - vsechnyRadky;
		scroll.setPreferredSize(new Dimension(sirka - 16, Math.max(VYSKA_RADKU + hlavicka, Math.min(vsechnyRadky, maxVyska - navic))));
		revalidate();
	}

	private JButton odkaz(final String text, final boolean zapnout) {
		final JButton b = new JButton("<html><a href=\"#\">" + text + "</a></html>");
		b.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		b.setContentAreaFilled(false);
		b.setFocusable(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.addActionListener(e -> {
			if (ovladani == null) {
				return;
			}
			if (zobrazenyTyp == null) {
				ovladani.setNacitatVse(zapnout);
			} else {
				ovladani.setNacitatVseVTypu(zobrazenyTyp, zapnout);
			}
		});
		return b;
	}

	/** Ctrl (na macOS Cmd) s klikem vybere jen tuto položku nebo jen tento typ. */
	static final int MASKA_JEN = System.getProperty("os.name", "").startsWith("Mac") ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
	static final String KLAVESA_JEN = MASKA_JEN == InputEvent.META_DOWN_MASK ? "⌘" : "Ctrl";

	static boolean jenTato(final InputEvent e) {
		return (e.getModifiersEx() & MASKA_JEN) != 0;
	}

	static boolean jenTato(final ActionEvent e) {
		return (e.getModifiers() & (MASKA_JEN == InputEvent.META_DOWN_MASK ? ActionEvent.META_MASK : ActionEvent.CTRL_MASK)) != 0;
	}

	private void ukazMenu(final MouseEvent e) {
		if (!e.isPopupTrigger() || ovladani == null) {
			return;
		}
		final int r = tabulka.rowAtPoint(e.getPoint());
		if (r >= 0) {
			menu(radky.get(r)).show(tabulka, e.getX(), e.getY());
		}
	}

	JPopupMenu menu(final Radek radek) {
		final JPopupMenu menu = new JPopupMenu();
		final TypZdroje typ = radek.typ;
		final StavPolozky p = radek.polozka;
		if (p == null) {
			pridej(menu, "Načítat jen " + typ.getNazev(), () -> ovladani.setNacitatJenTyp(typ));
			pridej(menu, "Načítat všechny položky " + typ.getNazev(), () -> ovladani.setNacitatVseVTypu(typ, true));
			pridej(menu, "Nenačítat " + typ.getNazev(), () -> ovladani.setNacitatTyp(typ, false)).setEnabled(stav.getStavVyberuTypu(typ) != StavVyberu.VYPNUTO);
		} else {
			pridej(menu, "Načítat jen tuto", () -> ovladani.setNacitatJenPolozku(typ, p.getSoubor()));
			pridej(menu, "Načítat všechny v typu " + typ.getNazev(), () -> ovladani.setNacitatVseVTypu(typ, true));
			pridej(menu, "Nenačítat tuto", () -> ovladani.setNacitatPolozku(p.getSoubor(), false)).setEnabled(p.isZapnuto());
		}
		return menu;
	}

	private static JMenuItem pridej(final JPopupMenu menu, final String text, final Runnable akce) {
		final JMenuItem polozka = new JMenuItem(text);
		polozka.addActionListener(e -> akce.run());
		menu.add(polozka);
		return polozka;
	}

	private void klik(final Radek radek, final boolean jen) {
		if (ovladani == null) {
			return;
		}
		if (radek.polozka == null) {
			if (jen) {
				ovladani.setNacitatJenTyp(radek.typ);
			} else {
				klikTyp(stav, radek.typ, ovladani);
			}
		} else if (jen || radek.polozka.isTypVypnut()) {
			ovladani.setNacitatJenPolozku(radek.typ, radek.polozka.getSoubor());
		} else {
			ovladani.setNacitatPolozku(radek.polozka.getSoubor(), !radek.polozka.isZapnuto());
		}
	}

	/**
	 * Klik na zaškrtávátko typu: zapnutý i částečně zapnutý typ se vypne (výběr položek zůstane), vypnutý se zapne s dřívějším výběrem; když žádná položka vybraná není,
	 * zapnou se všechny.
	 */
	static void klikTyp(final StavZdroju stav, final TypZdroje typ, final OvladaniZdroju ovladani) {
		if (stav.getStavVyberuTypu(typ) != StavVyberu.VYPNUTO) {
			ovladani.setNacitatTyp(typ, false);
		} else if (stav.getPolozky(typ).stream().anyMatch(StavPolozky::isZapnuto)) {
			ovladani.setNacitatTyp(typ, true);
		} else {
			ovladani.setNacitatVseVTypu(typ, true);
		}
	}

	/** Název typu a v závorce jeho zkratka, třeba „GSAK (Ctrl+3)“. */
	static String nazevTypuSeZkratkou(final TypZdroje typ) {
		final String zkratka = ZkratkyZdroju.text(typ);
		return zkratka == null ? typ.getNazev() : typ.getNazev() + " (" + zkratka + ")";
	}

	String text(final Radek radek, final int sloupec) {
		final StavPolozky p = radek.polozka;
		switch (sloupec) {
		case SL_NACIST:
			return "";
		case SL_ZDROJ:
			return p != null ? p.getNazev() : nazevTypuSeZkratkou(radek.typ);
		case SL_VELIKOST:
			return velikost(p != null ? p.getVelikostNaDisku() : velikostTypu(radek.typ));
		case SL_WP:
			return p != null ? (p.isNacitat() ? wp(p.getWpBrano(), p.getWpCelkem()) : "–") : wpTypu(radek.typ);
		default:
			if (p == null && stav.getProblemSlozky(radek.typ) != null) {
				return stav.getProblemSlozky(radek.typ);
			}
			return p != null ? (p.isNacitat() ? textStavu(p.getStav(), p.getPostup()) : StavZdroje.VYPNUTO.getText()) : textStavu(stav.getStavTypu(radek.typ), postupTypu(radek.typ));
		}
	}

	String tooltip(final Radek radek, final int sloupec) {
		final StavPolozky p = radek.polozka;
		if (sloupec == SL_ZDROJ && p != null) {
			return FString.text(p.getCesta());
		}
		if (sloupec == SL_NACIST && p != null && p.isTypVypnut()) {
			return "Zapnout " + radek.typ.getNazev() + " jen s tímto souborem";
		}
		if (sloupec == SL_NACIST) {
			return KLAVESA_JEN + "+klik: načítat jen " + (p != null ? "tento soubor" : radek.typ.getNazev());
		}
		if (sloupec == SL_STAV && p == null && stav.getProblemSlozky(radek.typ) != null) {
			return RADA_SLOZKA;
		}
		if (sloupec == SL_WP) {
			final int dup = p != null ? (p.isNacitat() ? p.getPocetDuplicit() : 0) : duplicityTypu(radek.typ);
			return dup > 0 ? textDuplicit(dup) : null;
		}
		if (sloupec == SL_STAV && p != null && p.isNacitat()) {
			if (p.getStav() == StavZdroje.CHYBA && p.getChyba() != null) {
				return FString.text(p.getChyba());
			}
			if (p.getStav() == StavZdroje.CEKA_NA_ZAPIS) {
				return RADA_ZAMCENO;
			}
		}
		return null;
	}

	static final String ZAMCENO = "Zamčeno jiným programem";
	static final String RADA_SLOZKA = "Zkontrolujte složku v Soubor > Umístění souborů.";
	static final String RADA_ZAMCENO = "Zavřete program, který databázi používá; načte se sama.";

	static String textStavu(final StavZdroje s, final int postup) {
		switch (s) {
		case NACITA_SE:
			return "Načítá se " + postup + " %";
		case CEKA_NA_ZAPIS:
			return ZAMCENO;
		default:
			return s.getText();
		}
	}

	private int postupTypu(final TypZdroje typ) {
		int soucet = 0;
		int pocet = 0;
		for (final StavPolozky p : stav.getPolozky(typ)) {
			if (p.isNacitat()) {
				soucet += p.getStav() == StavZdroje.NACITA_SE ? p.getPostup() : p.getStav() == StavZdroje.CEKA_NA_RADU ? 0 : 100;
				pocet++;
			}
		}
		return pocet == 0 ? 0 : soucet / pocet;
	}

	private long velikostTypu(final TypZdroje typ) {
		long s = 0;
		for (final StavPolozky p : stav.getPolozky(typ)) {
			s += p.getVelikostNaDisku();
		}
		return s;
	}

	private String wpTypu(final TypZdroje typ) {
		long brano = 0;
		long celkem = 0;
		boolean znamo = false;
		for (final StavPolozky p : stav.getPolozky(typ)) {
			if (p.isNacitat() && p.getWpCelkem() != StavPolozky.NEZNAMO) {
				brano += p.getWpBrano();
				celkem += p.getWpCelkem();
				znamo = true;
			}
		}
		return znamo ? wp(brano, celkem) : "–";
	}

	private int duplicityTypu(final TypZdroje typ) {
		int s = 0;
		for (final StavPolozky p : stav.getPolozky(typ)) {
			s += p.isNacitat() ? p.getPocetDuplicit() : 0;
		}
		return s;
	}

	private boolean maDuplicity(final Radek radek) {
		return radek.polozka != null ? radek.polozka.isNacitat() && radek.polozka.getPocetDuplicit() > 0 : duplicityTypu(radek.typ) > 0;
	}

	static String textDuplicit(final int pocet) {
		return String.format(CS, "%,d keší se neukazuje, protože jsou ve více zdrojích.", pocet);
	}

	static String wp(final long brano, final long celkem) {
		if (celkem == StavPolozky.NEZNAMO) {
			return "–";
		}
		return brano == celkem ? String.format(CS, "%,d", brano) : String.format(CS, "%,d z %,d  ≠", brano, celkem);
	}

	static String velikost(final long bajty) {
		if (bajty < 1_000_000L) {
			return String.format(CS, "%,d kB", Math.max(bajty > 0 ? 1 : 0, bajty / 1000));
		}
		return bajty < 1_000_000_000L ? String.format(CS, "%.1f MB", bajty / 1e6) : String.format(CS, "%.2f GB", bajty / 1e9);
	}

	JTable getTabulka() {
		return tabulka;
	}

	List<Radek> getRadky() {
		return radky;
	}

	/** Šedé podbarvení odvozené z barev vzhledu: skupina typu výrazněji, položky střídavě, aby oko udrželo řádek. */
	static Color pozadi(final JTable t, final Radek radek) {
		if (radek.polozka == null) {
			return smichej(t.getBackground(), t.getForeground(), 0.15);
		}
		return radek.poradi % 2 == 0 ? t.getBackground() : smichej(t.getBackground(), t.getForeground(), 0.08);
	}

	private static Color smichej(final Color a, final Color b, final double podilB) {
		return new Color((int) Math.round(a.getRed() * (1 - podilB) + b.getRed() * podilB), (int) Math.round(a.getGreen() * (1 - podilB) + b.getGreen() * podilB),
				(int) Math.round(a.getBlue() * (1 - podilB) + b.getBlue() * podilB));
	}

	private class Vykreslovac extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		Vykreslovac() {
			putClientProperty("html.disable", Boolean.TRUE);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable t, final Object v, final boolean sel, final boolean fokus, final int r, final int sloupec) {
			super.getTableCellRendererComponent(t, v, false, false, r, sloupec);
			final int c = t.convertColumnIndexToModel(sloupec);
			final Radek radek = radky.get(r);
			final StavPolozky p = radek.polozka;
			final boolean aktivni = p != null ? p.isNacitat() : stav.getStavVyberuTypu(radek.typ) != StavVyberu.VYPNUTO;
			setIcon(null);
			setHorizontalAlignment(c == SL_NACIST ? CENTER : c == SL_VELIKOST || c == SL_WP ? RIGHT : LEFT);
			setFont(t.getFont().deriveFont(p == null ? Font.BOLD : Font.PLAIN));
			final Border odsazeni = BorderFactory.createEmptyBorder(0, c == SL_ZDROJ && p != null ? 22 : 6, 0, 6);
			setBorder(p == null ? BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, smichej(t.getBackground(), t.getForeground(), 0.25)), odsazeni) : odsazeni);
			setBackground(pozadi(t, radek));
			setForeground(aktivni ? t.getForeground() : Color.GRAY);
			if (c == SL_NACIST) {
				final StavVyberu volba = p == null ? stav.getStavVyberuTypu(radek.typ) : p.isZapnuto() ? StavVyberu.ZAPNUTO : StavVyberu.VYPNUTO;
				setIcon(p != null && p.isTypVypnut() ? IkonyZdroju.zaskrtavatkoSede(volba) : IkonyZdroju.zaskrtavatko(volba));
			} else if (c == SL_STAV && p == null && stav.getProblemSlozky(radek.typ) != null) {
				setIcon(IkonyZdroju.pro(StavZdroje.CHYBA));
				setForeground(t.getForeground());
			} else if (c == SL_STAV && aktivni) {
				setIcon(IkonyZdroju.pro(p != null ? p.getStav() : stav.getStavTypu(radek.typ)));
			} else if (c == SL_WP && maDuplicity(radek)) {
				setForeground(BARVA_DUPLICIT);
			}
			setOpaque(true);
			return this;
		}
	}
}
