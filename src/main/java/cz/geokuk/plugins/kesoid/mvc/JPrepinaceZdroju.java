package cz.geokuk.plugins.kesoid.mvc;

import java.awt.*;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;

import cz.geokuk.plugins.kesoid.importek.*;
import cz.geokuk.plugins.kesoid.importek.StavZdroju.StavVyberu;
import cz.geokuk.util.file.Filex;
import cz.geokuk.util.gui.PopupNadBlokem;
import cz.geokuk.util.lang.FString;

/**
 * Blok stavového řádku „Zdroje:“ a u každého typu zdroje zaškrtávátko, název a ikona stavu. Zaškrtávátko zapne či vypne typ i během načítání. Najetí na popisek vysune
 * tabulku všech zdrojů, najetí na název (v úzkém okně na ikonu) jen zdroje typu.
 */
public class JPrepinaceZdroju extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Pod touto šířkou okna se skryjí názvy zdrojů, zůstane zaškrtávátko a ikona. */
	static final int PRAH_KOMPAKTNI = JTabulkaZdroju.PRAH_UZKE;

	/** Okraj aktivní plochy popisku a názvu; vedle zaškrtávátka a ikony zůstává místo, které popup nevyvolá. */
	private static final int PADDING = 3;

	static final int PRODLEVA_OTEVRENI_MS = PopupNadBlokem.PRODLEVA_OTEVRENI_MS;
	static final int PRODLEVA_PREPNUTI_MS = PopupNadBlokem.PRODLEVA_PREPNUTI_MS;
	static final int PRODLEVA_ZAVRENI_MS = PopupNadBlokem.PRODLEVA_ZAVRENI_MS;

	static final int VYSKA = 20;

	private final JLabel popisek = new JNatazenyPopisek("Zdroje:");
	/** Tabulky popupů se skládají až při prvním zobrazení, při startu stačí řádek bloku. */
	private JTabulkaZdroju uplna;
	private JComponent souhrn;
	private final Map<TypZdroje, JComponent> popupyTypu = new EnumMap<>(TypZdroje.class);
	private final Map<TypZdroje, JTabulkaZdroju> detaily = new EnumMap<>(TypZdroje.class);
	private final Map<TypZdroje, JButton> zaskrtavatka = new EnumMap<>(TypZdroje.class);
	private final Map<TypZdroje, JLabel> ikony = new EnumMap<>(TypZdroje.class);
	private final Map<TypZdroje, JLabel> nazvy = new EnumMap<>(TypZdroje.class);
	private final Map<TypZdroje, JPanel> bunky = new EnumMap<>(TypZdroje.class);
	private final PopupNadBlokem popup = new PopupNadBlokem(this, this::prizpusob);

	private static final Border RAMECEK_POPUPU = BorderFactory.createLineBorder(new Color(0x8AA0C0));

	private StavZdroju stav = StavZdroju.PRAZDNY;
	private Set<TypZdroje> povolene;
	private boolean usporny;
	private String casDat;
	private int dostupnaSirka = Integer.MAX_VALUE;
	private int dostupnaVyska = Integer.MAX_VALUE;
	private final Tocitko tocitko = new Tocitko(this, this::prekresliTocitka);
	private OvladaniZdroju ovladani;

	public JPrepinaceZdroju() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEtchedBorder());

		popisek.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, PADDING));
		popisek.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				naCil(souhrn(), JPrepinaceZdroju.this::ukazSouhrn, System.currentTimeMillis());
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				opustenCil(souhrn);
			}
		});
		natahni(popisek);
		add(Box.createHorizontalStrut(5 - PADDING));
		add(popisek);

		for (final TypZdroje typ : TypZdroje.values()) {
			// Tlačítko, ne JCheckBox: jeho vlastní stav „vybráno“ by neodpovídal stavu typu (záznam kliknutí v Diagnostice).
			final JButton zaskrtavatko = new JButton(IkonyZdroju.zaskrtavatko(StavVyberu.VYPNUTO)) {
				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getMaximumSize() {
					return natazena(this);
				}

				@Override
				public Point getToolTipLocation(final MouseEvent e) {
					return bublinaNadBlokem(this, e);
				}
			};
			zaskrtavatko.setFocusable(false);
			zaskrtavatko.setOpaque(false);
			zaskrtavatko.setContentAreaFilled(false);
			zaskrtavatko.setMargin(new Insets(0, 0, 0, 0));
			zaskrtavatko.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
			zaskrtavatko.addActionListener(e -> {
				if (ovladani == null) {
					return;
				}
				if (JTabulkaZdroju.jenTato(e)) {
					ovladani.setNacitatJenTyp(typ);
				} else {
					JTabulkaZdroju.klikTyp(stav, typ, ovladani);
				}
			});
			final JLabel ikona = new JLabel(IkonyZdroju.prazdna()) {
				private static final long serialVersionUID = 1L;

				@Override
				public Dimension getMaximumSize() {
					return natazena(this);
				}

				@Override
				public Point getToolTipLocation(final MouseEvent e) {
					return bublinaNadBlokem(this, e);
				}
			};
			final JLabel nazev = new JNatazenyPopisek(typ.getNazev());
			nazev.setBorder(BorderFactory.createEmptyBorder(0, PADDING, 0, PADDING));
			nazev.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(final MouseEvent e) {
					naCil(popupTypu(typ), () -> ukazTyp(typ), System.currentTimeMillis());
				}

				@Override
				public void mouseExited(final MouseEvent e) {
					opustenCil(popupyTypu.get(typ));
				}
			});
			ikona.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(final MouseEvent e) {
					if (!nazev.isVisible()) {
						naCil(popupTypu(typ), () -> ukazTyp(typ), System.currentTimeMillis());
					}
				}

				@Override
				public void mouseExited(final MouseEvent e) {
					opustenCil(popupyTypu.get(typ));
				}
			});
			natahni(zaskrtavatko);
			natahni(ikona);
			natahni(nazev);
			final JPanel bunka = new JPanel();
			bunka.setLayout(new BoxLayout(bunka, BoxLayout.X_AXIS));
			bunka.setOpaque(false);
			bunka.add(Box.createHorizontalStrut(8));
			bunka.add(zaskrtavatko);
			bunka.add(Box.createHorizontalStrut(2));
			bunka.add(nazev);
			bunka.add(ikona);
			add(bunka);
			bunky.put(typ, bunka);
			zaskrtavatka.put(typ, zaskrtavatko);
			ikony.put(typ, ikona);
			nazvy.put(typ, nazev);
		}
		add(Box.createHorizontalStrut(2));

		addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
			@Override
			public void ancestorResized(final HierarchyEvent e) {
				prizpusob();
			}
		});
		obnov(StavZdroju.PRAZDNY);
	}

	public void setOvladani(final OvladaniZdroju ovladani) {
		this.ovladani = ovladani;
		if (uplna != null) {
			uplna.setOvladani(ovladani);
		}
		for (final JTabulkaZdroju t : detaily.values()) {
			t.setOvladani(ovladani);
		}
	}

	/** Tabulka všech zdrojů v popupu; při prvním použití ji složí a předá jí stav bloku. */
	private JComponent souhrn() {
		if (souhrn == null) {
			uplna = new JTabulkaZdroju(null);
			nastav(uplna);
			souhrn = obal(uplna);
		}
		return souhrn;
	}

	private JComponent popupTypu(final TypZdroje typ) {
		JComponent popup = popupyTypu.get(typ);
		if (popup == null) {
			final JTabulkaZdroju detail = new JTabulkaZdroju(typ);
			nastav(detail);
			detaily.put(typ, detail);
			popup = obal(detail);
			popupyTypu.put(typ, popup);
		}
		return popup;
	}

	/** Předá nové tabulce to, co blok dosud nastavil ostatním. */
	private void nastav(final JTabulkaZdroju t) {
		t.setOvladani(ovladani);
		if (povolene != null && t == uplna) {
			t.setPovoleneTypy(povolene);
		}
		if (casDat != null && t == uplna) {
			t.setCasDat(casDat);
		}
		t.nastavDostupnouSirku(dostupnaSirka);
		t.nastavDostupnouVysku(dostupnaVyska);
		t.obnov(stav);
	}

	/**
	 * Typy zdrojů, které má uživatel v Nastavení (Umístění souborů); ostatní buňky ani řádky tabulek nejsou. Blok tak mění šířku jen se změnou Nastavení.
	 */
	public void setPovoleneTypy(final Set<TypZdroje> povolene) {
		boolean zmena = false;
		for (final Map.Entry<TypZdroje, JPanel> e : bunky.entrySet()) {
			final boolean videt = povolene.contains(e.getKey());
			zmena |= e.getValue().isVisible() != videt;
			e.getValue().setVisible(videt);
		}
		this.povolene = EnumSet.noneOf(TypZdroje.class);
		this.povolene.addAll(povolene);
		if (uplna != null) {
			uplna.setPovoleneTypy(povolene);
		}
		if (zmena) {
			zavriSeznam();
			revalidate();
			repaint();
		}
	}

	/** Typy, jejichž datová složka je v Nastavení zapnutá. */
	public static Set<TypZdroje> povoleneTypy(final KesoidUmisteniSouboru u) {
		final Set<TypZdroje> povolene = EnumSet.noneOf(TypZdroje.class);
		if (u == null) {
			return EnumSet.allOf(TypZdroje.class);
		}
		pridejKdyzAktivni(povolene, TypZdroje.GPX, u.getKesDir());
		pridejKdyzAktivni(povolene, TypZdroje.GEOGET, u.getGeogetDataDir());
		pridejKdyzAktivni(povolene, TypZdroje.GSAK, u.getGsakDataDir());
		pridejKdyzAktivni(povolene, TypZdroje.OPENSAK, u.getOpensakDataDir());
		return povolene;
	}

	private static void pridejKdyzAktivni(final Set<TypZdroje> povolene, final TypZdroje typ, final Filex slozka) {
		if (slozka != null && slozka.isActive()) {
			povolene.add(typ);
		}
	}

	public void setCasDat(final String cas) {
		casDat = cas;
		if (uplna != null) {
			uplna.setCasDat(cas);
		}
	}

	@Override
	public void addNotify() {
		super.addNotify();
		SwingUtilities.invokeLater(this::prizpusob);
	}

	@Override
	public void removeNotify() {
		popup.zastav();
		super.removeNotify();
	}

	/** Bublina celá nad blokem: u spodního okraje obrazovky by jinak skončila pod kurzorem a zachytila klik. */
	Point bublinaNadBlokem(final JComponent c, final MouseEvent e) {
		final String text = c.getToolTipText(e);
		if (text == null) {
			return null;
		}
		final JToolTip tip = c.createToolTip();
		tip.setTipText(text);
		final int vBloku = SwingUtilities.convertPoint(c, 0, 0, this).y;
		return new Point(e.getX(), -vBloku - tip.getPreferredSize().height - 2);
	}

	private static void natahni(final JComponent c) {
		c.setAlignmentY(CENTER_ALIGNMENT);
	}

	/**
	 * Komponenta zabere celou výšku bloku, aby plocha pro najetí a klik byla dost velká. Šířka se počítá pokaždé znovu: písmo vzhledu i měřítko obrazovky ji
	 * mění až po vytvoření bloku.
	 */
	private static Dimension natazena(final JComponent c) {
		return new Dimension(c.getPreferredSize().width, Integer.MAX_VALUE);
	}

	private static final class JNatazenyPopisek extends JLabel {
		private static final long serialVersionUID = 1L;

		JNatazenyPopisek(final String text) {
			super(text);
		}

		@Override
		public Dimension getMaximumSize() {
			return natazena(this);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		final Dimension d = super.getPreferredSize();
		final Insets ins = getInsets();
		return new Dimension(d.width, Math.max(d.height, VYSKA + ins.top + ins.bottom));
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	private int sirkaOkna() {
		final Window okno = SwingUtilities.getWindowAncestor(this);
		return okno != null ? okno.getWidth() : Integer.MAX_VALUE;
	}

	/** Když se stavový řádek nevejde do jednoho řádku, zůstane u zdrojů jen zaškrtávátko a ikona, bez názvů i bez popisku Zdroje. */
	public void setUsporny(final boolean usporny) {
		if (this.usporny != usporny) {
			this.usporny = usporny;
			prizpusob();
		}
	}

	public boolean isUsporny() {
		return usporny;
	}

	/** Šířka, kterou zabírají popisek a názvy zdrojů, když jsou vidět. */
	public int sirkaNazvu() {
		int sirka = popisek.getPreferredSize().width;
		for (final Map.Entry<TypZdroje, JLabel> e : nazvy.entrySet()) {
			if (bunky.get(e.getKey()).isVisible()) {
				sirka += e.getValue().getPreferredSize().width;
			}
		}
		return sirka;
	}

	/** V úzkém okně jen zaškrtávátko a ikona, popupy se zúží. */
	void prizpusob() {
		final int sirka = sirkaOkna();
		final boolean kompaktni = sirka < PRAH_KOMPAKTNI || usporny;
		boolean zmena = popisek.isVisible() == usporny;
		popisek.setVisible(!usporny);
		for (final JLabel n : nazvy.values()) {
			zmena |= n.isVisible() == kompaktni;
			n.setVisible(!kompaktni);
		}
		final Insets ramecek = RAMECEK_POPUPU.getBorderInsets(this);
		final int vyska = isShowing() ? getLocationOnScreen().y - popup.horniHranice() - ramecek.top - ramecek.bottom : Integer.MAX_VALUE;
		dostupnaSirka = sirka;
		dostupnaVyska = vyska;
		if (uplna != null) {
			uplna.nastavDostupnouSirku(sirka);
			uplna.nastavDostupnouVysku(vyska);
		}
		for (final JTabulkaZdroju t : detaily.values()) {
			t.nastavDostupnouSirku(sirka);
			t.nastavDostupnouVysku(vyska);
		}
		if (zmena) {
			revalidate();
			repaint();
		}
	}

	boolean isKompaktni() {
		return !nazvy.get(TypZdroje.GPX).isVisible();
	}

	private static JComponent obal(final JComponent obsah) {
		final JPanel obal = new JPanel(new BorderLayout());
		obal.setBorder(RAMECEK_POPUPU);
		obal.add(obsah);
		return obal;
	}

	/** Mění se jen ikony, texty bublin a barva názvu; velikost buněk zůstává, aby se nic v řádku neposouvalo. */
	public void obnov(final StavZdroju novy) {
		stav = novy;
		for (final TypZdroje typ : TypZdroje.values()) {
			final StavZdroje stavTypu = stav.getStavTypu(typ);
			final StavVyberu volba = stav.getStavVyberuTypu(typ);
			final boolean zapnuto = stavTypu != StavZdroje.VYPNUTO;
			final JButton z = zaskrtavatka.get(typ);
			z.setIcon(IkonyZdroju.zaskrtavatko(volba));
			z.setToolTipText(tooltipZaskrtavatka(typ, volba));
			final JLabel ikona = ikony.get(typ);
			ikona.setIcon(stav.getProblemSlozky(typ) != null ? IkonyZdroju.pro(StavZdroje.CHYBA) : zapnuto ? IkonyZdroju.pro(stavTypu) : IkonyZdroju.prazdna());
			ikona.setToolTipText(tooltipIkony(stav, typ));
			nazvy.get(typ).setForeground(zapnuto ? UIManager.getColor("Label.foreground") : sedaNazvu());
		}
		tocitko.nastav(ikony.values().stream().anyMatch(i -> i.getIcon() == IkonyZdroju.pro(StavZdroje.NACITA_SE)));
		if (uplna != null) {
			uplna.obnov(stav);
		}
		for (final JTabulkaZdroju t : detaily.values()) {
			t.obnov(stav);
		}
		final JComponent viditelny = viditelny();
		if (viditelny != null && !viditelny.getSize().equals(viditelny.getPreferredSize())) {
			ukaz(viditelny, viditelny == souhrn ? null : kotvaTypu(viditelny), true);
		}
	}

	private static Color sedaNazvu() {
		final Color seda = UIManager.getColor("Label.disabledForeground");
		return seda != null ? seda : Color.GRAY;
	}

	private void prekresliTocitka() {
		for (final JLabel ikona : ikony.values()) {
			if (ikona.getIcon() == IkonyZdroju.pro(StavZdroje.NACITA_SE)) {
				ikona.repaint();
			}
		}
	}

	boolean animuje() {
		return tocitko.bezi();
	}

	static String tooltipZaskrtavatka(final TypZdroje typ, final StavVyberu volba) {
		final String zkratka = ZkratkyZdroju.text(typ);
		final String ve = zkratka == null ? "" : " (" + zkratka + ")";
		switch (volba) {
		case VYPNUTO:
			return typ.getNazev() + " zapnout" + ve + jenTypem(typ);
		case CASTECNE:
			return typ.getNazev() + " vypnout" + ve + " (načítá se jen část položek)" + jenTypem(typ);
		default:
			return typ.getNazev() + " vypnout" + ve + jenTypem(typ);
		}
	}

	private static String jenTypem(final TypZdroje typ) {
		return "; " + JTabulkaZdroju.KLAVESA_JEN + "+klik: jen " + typ.getNazev();
	}

	/** Stav typu; u zámku a chyby i dotčené položky. */
	static String tooltipIkony(final StavZdroju stav, final TypZdroje typ) {
		final String problem = stav.getProblemSlozky(typ);
		if (problem != null) {
			return "<html>" + FString.html(typ.getNazev() + ": " + problem) + "<br>" + FString.html(JTabulkaZdroju.RADA_SLOZKA);
		}
		final StavZdroje s = stav.getStavTypu(typ);
		if (s == StavZdroje.VYPNUTO) {
			return null;
		}
		final StringBuilder sb = new StringBuilder("<html>").append(FString.html(typ.getNazev() + ": " + JTabulkaZdroju.textStavu(s, postup(stav, typ))));
		if (s == StavZdroje.CEKA_NA_ZAPIS || s == StavZdroje.CHYBA) {
			for (final StavPolozky p : stav.getPolozky(typ)) {
				if (p.isNacitat() && p.getStav() == s) {
					sb.append("<br>").append(FString.html(p.getNazev() + (p.getChyba() != null ? ": " + p.getChyba() : "")));
				}
			}
			if (s == StavZdroje.CEKA_NA_ZAPIS) {
				sb.append("<br>").append(FString.html(JTabulkaZdroju.RADA_ZAMCENO));
			}
		}
		return sb.toString();
	}

	private static int postup(final StavZdroju stav, final TypZdroje typ) {
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

	private Component kotvaTypu(final JComponent obsah) {
		for (final Map.Entry<TypZdroje, JComponent> e : popupyTypu.entrySet()) {
			if (e.getValue() == obsah) {
				return zaskrtavatka.get(e.getKey());
			}
		}
		return null;
	}

	/** Zobrazí tabulku všech zdrojů. */
	public void ukazSouhrn() {
		ukaz(souhrn(), null, false);
	}

	/** Zobrazí jen zdroje jednoho typu. */
	public void ukazTyp(final TypZdroje typ) {
		ukaz(popupTypu(typ), zaskrtavatka.get(typ), false);
	}

	/** Úplná tabulka je zarovnaná k pravému okraji bloku, detail k buňce typu; vždy těsně nad blokem a uvnitř obrazovky. */
	private void ukaz(final JComponent obsah, final Component kotva, final boolean znovu) {
		popup.ukaz(obsah, kotva, znovu);
	}

	Point polohaPopupu(final Dimension d, final Component kotva) {
		return popup.poloha(d, kotva);
	}

	public void zavriSeznam() {
		popup.zavri();
	}

	/** Obsah zobrazeného popupu, nebo null. */
	JComponent viditelny() {
		return popup.viditelny();
	}

	JComponent getObsahSouhrnu() {
		return souhrn();
	}

	JComponent getObsahDetailu(final TypZdroje typ) {
		return popupTypu(typ);
	}

	/** Myš vjela na popisek či název: popup se otevře nebo přepne, až na něm vydrží. */
	void naCil(final JComponent obsah, final Runnable ukazani, final long ted) {
		popup.naCil(obsah, ukazani, ted);
	}

	void opustenCil(final JComponent obsah) {
		popup.opustenCil(obsah);
	}

	void tik(final Point mys, final long ted) {
		popup.tik(mys, ted);
	}

	/** Co plocha bloku dělá. */
	enum Druh {
		UPLNA_TABULKA, PREPINA, DETAIL, TOOLTIP
	}

	/** Plocha bloku, která reaguje na najetí nebo klik; souřadnice v bloku. */
	static final class Oblast {
		final Rectangle plocha;
		final Druh druh;
		final TypZdroje typ;

		Oblast(final Rectangle plocha, final Druh druh, final TypZdroje typ) {
			this.plocha = plocha;
			this.druh = druh;
			this.typ = typ;
		}
	}

	/** Plochy, ve kterých blok reaguje; mezery mezi nimi nereagují. */
	List<Oblast> oblasti() {
		final List<Oblast> oblasti = new ArrayList<>();
		oblasti.add(new Oblast(plocha(popisek), Druh.UPLNA_TABULKA, null));
		for (final TypZdroje typ : TypZdroje.values()) {
			if (!bunky.get(typ).isVisible()) {
				continue;
			}
			oblasti.add(new Oblast(plocha(zaskrtavatka.get(typ)), Druh.PREPINA, typ));
			final boolean bezNazvu = !nazvy.get(typ).isVisible();
			oblasti.add(new Oblast(plocha(ikony.get(typ)), bezNazvu ? Druh.DETAIL : Druh.TOOLTIP, typ));
			if (!bezNazvu) {
				oblasti.add(new Oblast(plocha(nazvy.get(typ)), Druh.DETAIL, typ));
			}
		}
		return oblasti;
	}

	private Rectangle plocha(final Component c) {
		return SwingUtilities.convertRectangle(c.getParent(), c.getBounds(), this);
	}

	JPanel getBunka(final TypZdroje typ) {
		return bunky.get(typ);
	}

	JButton getZaskrtavatko(final TypZdroje typ) {
		return zaskrtavatka.get(typ);
	}

	JLabel getIkona(final TypZdroje typ) {
		return ikony.get(typ);
	}

	JLabel getNazev(final TypZdroje typ) {
		return nazvy.get(typ);
	}

	JLabel getPopisek() {
		return popisek;
	}

	boolean jsouTabulkyVytvoreny() {
		return uplna != null || !detaily.isEmpty();
	}

	JTabulkaZdroju getUplna() {
		souhrn();
		return uplna;
	}

	JTabulkaZdroju getDetail(final TypZdroje typ) {
		popupTypu(typ);
		return detaily.get(typ);
	}
}
