package cz.geokuk.plugins.challenge;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;

import cz.geokuk.core.napoveda.UkazatelVykonu;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JSlide0;
import cz.geokuk.framework.RozmisteniPrekryvu;

/**
 * Lišta plánovače challenge vlevo nahoře na mapě. Při vypnutém plánovači z ní zůstane jen úchyt, kterým se zapne. Zapnutá nese výběr challenge (typ a ▾, název v tooltipu), počet
 * chybějících, přechod na předchozí a další chybějící, filtr „jen kandidáti“, přepínače kreslení a nastavení. Při malé šířce mapy se zužuje ve třech stupních podle
 * {@link RozmisteniPrekryvu#sirkaPruhu}; mimo svůj obdélník nezachytává myš.
 */
public class JPruhPlanovace extends JSlide0 implements AfterInjectInit {

	private static final long serialVersionUID = 1L;

	private static final int PODSTAVEC = 4;
	private static final int MEZERA = 4;
	/** Větší mezera mezi skupinami (výběr a pohyb, filtry, nastavení). */
	private static final int MEZERA_SKUPIN = 8;
	private static final int VYSKA_PRVKU = 20;
	private static final int SIRKA_UCHYTU = 26;
	private static final int SIRKA_VYBERU = 62;
	private static final int SIRKA_ODZNAKU = 80;
	private static final int SIRKA_SIPKY = 24;
	private static final int SIRKA_IKONY = 26;
	private static final int SIRKA_KANDIDATU = 124, SIRKA_SEKTORU = 88, SIRKA_STITKU = 72, SIRKA_NASTAVENI = 116;
	/** Tlačítko vybraného políčka D/T; vejde se i „D4,5/T4,5 ✕“. */
	static final int SIRKA_POLICKA = 92;
	/** O kolik je lišta s tlačítkem vybraného políčka širší (tlačítko a mezera skupiny před ním). */
	static final int PRIDAVEK_POLICKA = SIRKA_POLICKA + MEZERA_SKUPIN;
	private static final Color CERVENA = new Color(200, 40, 40);
	private static final javax.swing.border.Border RAMECEK = BorderFactory.createEtchedBorder();
	/** Prodleva, po které se výběr challenge otevře při najetí myší na jeho tlačítko. */
	static final int PRODLEVA_VYBERU_MS = 500;

	/** Pořadí prvků v poli z {@link #rozloz}. */
	static final int UCHYT = 0, VYBER = 1, ODZNAK = 2, ZPET = 3, DAL = 4, KANDIDATI = 5, SEKTORY = 6, STITKY = 7, NASTAVENI = 8, POLICKO = 9;
	private static final int POCET = 10;
	/** Pořadí prvků zleva: vybrané políčko je poslední, ať ostatní prvky při výběru a zrušení nepoposkočí. */
	private static final int[] ZLEVA = { UCHYT, VYBER, ODZNAK, ZPET, DAL, KANDIDATI, SEKTORY, STITKY, NASTAVENI, POLICKO };

	/** Stupeň lišty podle její šířky. */
	enum Stupen {
		PLNY, IKONY, UZKY;

		static Stupen pro(final int sirkaPruhu) {
			if (sirkaPruhu >= RozmisteniPrekryvu.PRUH_PLNY) {
				return PLNY;
			}
			return sirkaPruhu >= RozmisteniPrekryvu.PRUH_IKONY ? IKONY : UZKY;
		}
	}

	private final transient JButton uchyt = new JButton(new Terc());
	private final transient JButton chip = new JButton("▾");
	private final transient Odznak odznak = new Odznak();
	private final transient JButton zpet = new JButton(new Sipka(true));
	private final transient JButton dal = new JButton(new Sipka(false));
	private final transient Prepinac kandidati = new Prepinac(new Trychtyr());
	private final transient Prepinac sektory = new Prepinac(new Klin());
	private final transient Prepinac stitky = new Prepinac(new Stitek());
	private final transient JButton nastaveni = new JButton(new Ozubene());
	private final transient JButton policko = new JButton();
	/** Políčko D/T vybrané v modelu, null když žádné nebo challenge není D/T. */
	private String vybranePolicko;
	private transient java.util.function.BooleanSupplier jinyPopupOtevreny = () -> false;
	private final transient Timer prodlevaVyberu = new Timer(PRODLEVA_VYBERU_MS, e -> otevriVyber());
	private transient ChallengeModel model;
	private transient Runnable naKlikOdznaku;
	private transient Runnable naNastaveni;
	private transient JPopupMenu vyber;
	private transient java.util.function.Supplier<? extends java.util.Collection<Rectangle>> prekazky = java.util.Collections::emptyList;
	private Stupen stupen = Stupen.PLNY;
	private boolean zapnuto;

	public JPruhPlanovace() {
		setOpaque(false);
		setLayout(null);
		uchyt.setToolTipText(TIP_UCHYTU_ZAPNOUT);
		zpet.setToolTipText(ChallengeAkce.tipPosunu(Challenge.Typ.SEKTORY_360, false));
		dal.setToolTipText(ChallengeAkce.tipPosunu(Challenge.Typ.SEKTORY_360, true));
		odznak.setToolTipText(popisChybejicich(Challenge.Typ.SEKTORY_360));
		kandidati.setToolTipText(popisKandidatu(Challenge.Typ.SEKTORY_360));
		sektory.setToolTipText("Sektory na mapě: prstenec, klín chybějícího sektoru a čára ke kandidátovi");
		stitky.setToolTipText("Štítky u kandidátů: číslo sektoru nebo políčko D/T");
		nastaveni.setToolTipText(TIP_NASTAVENI);
		chip.setToolTipText("Vybrat challenge");
		sektory.setSelected(true);
		stitky.setSelected(true);
		for (final AbstractButton b : new AbstractButton[] { uchyt, chip, odznak, zpet, dal, kandidati, sektory, stitky, nastaveni, policko }) {
			b.setFocusable(false);
			b.setMargin(new Insets(0, 2, 0, 2));
			add(b);
		}
		uchyt.addActionListener(e -> prepniZapnuti());
		chip.addActionListener(e -> {
			prodlevaVyberu.stop();
			otevriVyber();
		});
		prodlevaVyberu.setRepeats(false);
		chip.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				if (chip.isEnabled() && !jeVyberOtevreny()) {
					prodlevaVyberu.restart();
				}
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				prodlevaVyberu.stop();
			}
		});
		kandidati.addActionListener(e -> prepniKandidaty());
		sektory.addActionListener(e -> {
			if (model != null) {
				model.setKresliSektory(sektory.isSelected());
			}
		});
		stitky.addActionListener(e -> {
			if (model != null) {
				model.setKresliStitky(stitky.isSelected());
			}
		});
		policko.addActionListener(e -> zrusPolicko());
		pridejEsc();
		zpet.addActionListener(e -> prejdi(false));
		dal.addActionListener(e -> prejdi(true));
		odznak.addActionListener(e -> {
			if (naKlikOdznaku != null) {
				naKlikOdznaku.run();
			}
		});
		nastaveni.addActionListener(e -> otevriNastaveni());
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	/** Nastavení otevírá dialog plánovače přes jeho akci. */
	public void inject(final cz.geokuk.core.program.Akce akce) {
		setNaNastaveni(() -> akce.challengeNastaveniAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "nastaveni")));
	}

	/** Co se stane po kliknutí na odznak (popup s podrobnostmi). */
	public void setNaKlikOdznaku(final Runnable naKlikOdznaku) {
		this.naKlikOdznaku = naKlikOdznaku;
	}

	/** Panely a legenda na mapě, před kterými lišta končí. */
	public void setPrekazky(final java.util.function.Supplier<? extends java.util.Collection<Rectangle>> prekazky) {
		this.prekazky = prekazky;
	}

	/** Co otevře Nastavení… a Plánovač challenge… (dialog plánovače). */
	public void setNaNastaveni(final Runnable naNastaveni) {
		this.naNastaveni = naNastaveni;
	}

	@Override
	public void initAfterInject() {
		obnov();
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		obnov();
	}

	/** Sektory (prstenec, klín, čára) má jen challenge 360°. */
	static boolean maSektory(final Challenge.Typ typ) {
		return typ == Challenge.Typ.SEKTORY_360;
	}

	static final String TIP_UCHYTU_ZAPNOUT = "<html><b>Plánovač challenge</b> – zapnout<br>Ukáže, které sektory nebo políčka challenge vám chybí,<br>a na mapě keše, které je doplní. Menu Kešoidy › Plánovač challenge.</html>";
	static final String TIP_UCHYTU_VYPNOUT = "<html><b>Plánovač challenge</b> – vypnout</html>";
	static final String TIP_NASTAVENI = "Nastavení plánovače… – seznam challenge, výběr aktivní, kopie šablon a úprava vlastních";

	/** Kolik políček challenge má: 360 sektorů, u políček podle výsledku. */
	private int celkem(final Challenge c) {
		if (c.typ() == Challenge.Typ.SEKTORY_360) {
			return 360;
		}
		final VysledekPolicek v = model == null ? null : model.vysledekPolicek(c);
		return v == null ? 0 : v.celkem();
	}

	/** Tooltip odznaku: „Chybí 193 sektorů z 360 – klik ukáže podrobnosti“. */
	static String tipOdznaku(final Challenge.Typ typ, final int chybi, final int celkem) {
		final String co;
		switch (typ) {
		case DT_MATICE:
			co = tvar(chybi, "políčko", "políčka", "políček");
			break;
		case KALENDAR:
			co = tvar(chybi, "den", "dny", "dní");
			break;
		default:
			co = tvar(chybi, "sektor", "sektory", "sektorů");
		}
		return "Chybí " + chybi + " " + co + (celkem > 0 ? " z " + celkem : "") + " – klik ukáže podrobnosti";
	}

	private static String tvar(final int n, final String jeden, final String dvaAzCtyri, final String jinak) {
		if (n == 1) {
			return jeden;
		}
		return n >= 2 && n <= 4 ? dvaAzCtyri : jinak;
	}

	/** Co počítá odznak: chybějící sektory, políčka nebo dny podle typu challenge. */
	static String popisChybejicich(final Challenge.Typ typ) {
		switch (typ) {
		case DT_MATICE:
			return "Chybějící políčka D/T";
		case KALENDAR:
			return "Chybějící dny";
		default:
			return "Chybějící sektory";
		}
	}

	/** Co zůstane na mapě po zapnutí filtru „jen kandidáti“. */
	static String popisKandidatu(final Challenge.Typ typ) {
		switch (typ) {
		case DT_MATICE:
			return "Na mapě zůstanou jen kandidáti, kteří doplní chybějící políčko D/T";
		case KALENDAR:
			return "Na mapě zůstanou jen kandidáti, kteří doplní chybějící den";
		default:
			return "Na mapě zůstanou jen kandidáti, kteří zaplní chybějící sektor";
		}
	}

	/**
	 * Obdélníky prvků lišty v souřadnicích mapy pro danou šířku zapnuté lišty, v pořadí {@link #UCHYT} až {@link #POLICKO}. Prvek, který se na tomto stupni neukazuje, má
	 * prázdný obdélník.
	 */
	static Rectangle[] rozloz(final int sirkaPruhu) {
		return rozloz(sirkaPruhu, false);
	}

	/** Totéž s tlačítkem vybraného políčka na konci lišty; {@code sirkaPruhu} je šířka lišty bez něj. */
	static Rectangle[] rozloz(final int sirkaPruhu, final boolean sPolickem) {
		final Stupen s = Stupen.pro(sirkaPruhu);
		final int[] sirky = new int[POCET];
		sirky[UCHYT] = SIRKA_UCHYTU;
		sirky[VYBER] = SIRKA_VYBERU;
		sirky[ODZNAK] = SIRKA_ODZNAKU;
		sirky[ZPET] = SIRKA_SIPKY;
		sirky[DAL] = SIRKA_SIPKY;
		sirky[KANDIDATI] = s == Stupen.PLNY ? SIRKA_KANDIDATU : s == Stupen.IKONY ? SIRKA_IKONY : 0;
		sirky[SEKTORY] = s == Stupen.PLNY ? SIRKA_SEKTORU : s == Stupen.IKONY ? SIRKA_IKONY : 0;
		sirky[STITKY] = s == Stupen.PLNY ? SIRKA_STITKU : s == Stupen.IKONY ? SIRKA_IKONY : 0;
		sirky[NASTAVENI] = s == Stupen.PLNY ? SIRKA_NASTAVENI : SIRKA_IKONY;
		sirky[POLICKO] = sPolickem && s != Stupen.UZKY ? SIRKA_POLICKA : 0;
		final Rectangle[] r = new Rectangle[POCET];
		final int y = RozmisteniPrekryvu.OKRAJ + (RozmisteniPrekryvu.VYSKA_PRUHU - VYSKA_PRVKU) / 2;
		int x = RozmisteniPrekryvu.OKRAJ + PODSTAVEC;
		for (final int i : ZLEVA) {
			if (sirky[i] == 0) {
				r[i] = new Rectangle(x, y, 0, 0);
				continue;
			}
			if (i == KANDIDATI || i == NASTAVENI || i == POLICKO) {
				x += MEZERA_SKUPIN - MEZERA;
			}
			r[i] = new Rectangle(x, y, sirky[i], VYSKA_PRVKU);
			x += sirky[i] + MEZERA;
		}
		return r;
	}

	/** Odznak v souřadnicích mapy dané šířky s danými překážkami vpravo (pod ním visí popup). */
	static Rectangle oblastOdznaku(final int sirkaMapy, final java.util.Collection<Rectangle> prekazky) {
		return rozloz(RozmisteniPrekryvu.pruhPlanovace(RozmisteniPrekryvu.volnaSirkaPruhu(sirkaMapy, prekazky)).width)[ODZNAK];
	}

	/** Odznak v souřadnicích mapy dané šířky bez překážek. */
	static Rectangle oblastOdznaku(final int sirkaMapy) {
		return oblastOdznaku(sirkaMapy, java.util.Collections.emptyList());
	}

	/** Odznak v souřadnicích mapy. */
	public Rectangle oblastOdznaku() {
		return oblastOdznaku(getWidth(), prekazky.get());
	}

	/** Šířka zapnuté lišty v souřadnicích mapy bez tlačítka vybraného políčka. */
	private int sirkaListy() {
		return RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(getWidth(), prekazky.get()));
	}

	/** Tlačítko vybraného políčka je vidět: políčko je vybrané, lišta není nejužší a s tlačítkem se vejde do volné šířky. */
	private boolean sPolickem() {
		if (vybranePolicko == null) {
			return false;
		}
		final int sirka = sirkaListy();
		return Stupen.pro(sirka) != Stupen.UZKY && sirka + PRIDAVEK_POLICKA <= RozmisteniPrekryvu.volnaSirkaPruhu(getWidth(), prekazky.get());
	}

	/** Šířka zapnuté lišty včetně tlačítka vybraného políčka, je-li vidět. */
	private int sirkaListySPolickem() {
		return sirkaListy() + (sPolickem() ? PRIDAVEK_POLICKA : 0);
	}

	/** Obdélník lišty (zapnuté) nebo úchytu (vypnuté) v souřadnicích mapy. */
	Rectangle oblastPruhu() {
		final Rectangle r = RozmisteniPrekryvu.pruhPlanovace(0, zapnuto);
		if (zapnuto) {
			r.width = sirkaListySPolickem();
		}
		return r;
	}

	@Override
	public boolean contains(final int x, final int y) {
		return isVisible() && oblastPruhu().contains(x, y);
	}

	@Override
	public void doLayout() {
		final int sirka = sirkaListy();
		final Rectangle[] r = rozloz(sirka, sPolickem());
		final Component[] c = prvky();
		stupen = Stupen.pro(sirka);
		for (int i = 0; i < c.length; i++) {
			c[i].setBounds(r[i]);
			c[i].setVisible(i == UCHYT || (zapnuto && !r[i].isEmpty()));
		}
		final boolean texty = stupen == Stupen.PLNY;
		nastavText(kandidati, texty ? "jen kandidáti" : "");
		nastavText(sektory, texty ? "Sektory" : "");
		nastavText(stitky, texty ? "Štítky" : "");
		nastavText(nastaveni, texty ? "Nastavení…" : "");
		chip.setFont(pismoNaSirku(UIManager.getFont("Button.font"), chip.getText(), r[VYBER].width - 8, chip::getFontMetrics));
	}

	private Component[] prvky() {
		return new Component[] { uchyt, chip, odznak, zpet, dal, kandidati, sektory, stitky, nastaveni, policko };
	}

	private static void nastavText(final AbstractButton b, final String text) {
		if (!text.equals(b.getText())) {
			b.setText(text);
		}
	}

	/** Písmo tlačítka výběru s typem: výchozí, a když se text nevejde, menší až do 9 bodů, ať se nezkrátí na „…“. */
	static Font pismoNaSirku(final Font zaklad, final String text, final int sirka, final java.util.function.Function<Font, FontMetrics> metriky) {
		if (zaklad == null || text == null || sirka <= 0) {
			return zaklad;
		}
		Font f = zaklad;
		while (f.getSize2D() > 9f && metriky.apply(f).stringWidth(text) > sirka) {
			f = f.deriveFont(f.getSize2D() - 0.5f);
		}
		return f;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Rectangle p = oblastPruhu();
		final Graphics2D g2 = (Graphics2D) g.create();
		try {
			// Stejný blok jako „Zdroje:“ a „Výlet:“ ve stavovém řádku: pozadí a vyrytý rámeček podle skinu.
			g2.setColor(UIManager.getColor("Panel.background"));
			g2.fillRect(p.x, p.y, p.width, p.height);
			RAMECEK.paintBorder(this, g2, p.x, p.y, p.width, p.height);
		} finally {
			g2.dispose();
		}
	}

	private Challenge challenge() {
		return model == null ? null : model.zobrazena();
	}

	/** Barva zobrazené challenge, bez ní šedá. */
	/** Akcent skinu: proužek zapnutého přepínače. */
	/** Barva skinu, která se od daného pozadí liší nejvíc jasem: zapnutý přepínač se tak nesplete s nedostupným. */
	static Color akcentNa(final Color pozadi) {
		final Color[] kandidati = { javax.swing.plaf.metal.MetalLookAndFeel.getPrimaryControlDarkShadow(), javax.swing.plaf.metal.MetalLookAndFeel.getPrimaryControl(),
				javax.swing.plaf.metal.MetalLookAndFeel.getPrimaryControlHighlight(), javax.swing.plaf.metal.MetalLookAndFeel.getFocusColor() };
		Color nej = Color.GRAY;
		double rozdil = -1;
		for (final Color c : kandidati) {
			if (c != null && (pozadi == null || Math.abs(jas(c) - jas(pozadi)) > rozdil)) {
				rozdil = pozadi == null ? 0 : Math.abs(jas(c) - jas(pozadi));
				nej = c;
			}
		}
		return nej;
	}

	private void obnov() {
		zapnuto = model != null && model.jeZapnuto();
		final StavPruhu stav = model == null ? StavPruhu.SKRYTY : model.stavPruhu();
		final boolean maChallenge = stav.jeViditelny();
		uchyt.setToolTipText(zapnuto ? TIP_UCHYTU_VYPNOUT : TIP_UCHYTU_ZAPNOUT);
		nastavText(chip, (maChallenge ? stav.stitekTypu() + " " : "") + "▾");
		chip.setToolTipText(maChallenge ? "Challenge: " + stav.challenge().nazev() + " – vybrat jinou" : "Vybrat challenge");
		if (maChallenge) {
			odznak.nastav("chybí " + (stav.badge() < 0 ? "…" : Integer.toString(stav.badge())));
			odznak.setToolTipText(stav.badge() < 0 ? popisChybejicich(stav.typ()) + " – počítá se" : tipOdznaku(stav.typ(), stav.badge(), celkem(stav.challenge())));
			kandidati.setToolTipText(popisKandidatu(stav.typ()) + " (F4)");
			zpet.setToolTipText(ChallengeAkce.tipPosunu(stav.typ(), false));
			dal.setToolTipText(ChallengeAkce.tipPosunu(stav.typ(), true));
		} else {
			odznak.nastav("–");
		}
		kandidati.setSelected(maChallenge && stav.jenKandidati());
		sektory.setSelected((model == null || model.isKresliSektory()) && maChallenge && maSektory(stav.typ()));
		stitky.setSelected(model == null || model.isKresliStitky());
		for (final AbstractButton b : new AbstractButton[] { odznak, zpet, dal, kandidati, stitky }) {
			b.setEnabled(maChallenge);
		}
		sektory.setEnabled(maChallenge && maSektory(stav.typ()));
		obnovPolicko(maChallenge ? stav.challenge() : null, maChallenge && stav.jenKandidati());
		if (!isVisible()) {
			setVisible(true);
		}
		UkazatelVykonu.setPodPruhem(true);
		revalidate();
		repaint();
	}

	/** Tlačítko „D3/T4 ✕“: jen u D/T s vybraným políčkem; při vypnutém zobrazení jen kandidátů (F4) zašedlé, ale klikací. */
	private void obnovPolicko(final Challenge c, final boolean jenKandidati) {
		vybranePolicko = c != null && c.typ() == Challenge.Typ.DT_MATICE && model != null ? model.vybranePolicko(c) : null;
		if (vybranePolicko == null) {
			return;
		}
		final VysledekPolicek v = model.vysledekPolicek(c);
		final int n = v == null ? 0 : v.pocetKandidatu(vybranePolicko);
		nastavText(policko, vybranePolicko + " ✕");
		policko.setToolTipText(tipPolicka(vybranePolicko, n, v != null && v.jePlne(vybranePolicko), jenKandidati));
		chip.setToolTipText(chip.getToolTipText() + "; vybrané políčko " + vybranePolicko);
		policko.setForeground(jenKandidati ? UIManager.getColor("Button.foreground") : zasedly());
	}

	/** Tooltip tlačítka vybraného políčka. */
	static String tipPolicka(final String stitek, final int kandidatu, final boolean plne, final boolean jenKandidati) {
		final String pocet = plne ? kandidatu + "+ kandidátů" : TextyPopupuPlanovace.kandidatu(kandidatu);
		final String stav = jenKandidati ? "na mapě jen jeho kandidáti (" + pocet + ")" : "na mapě všechny keše, F4 ukáže jen jeho kandidáty (" + pocet + ")";
		return "<html>Zrušit výběr políčka " + stitek + " (Esc)<br>" + stav + "</html>";
	}

	/** Esc zruší výběr políčka, jen když je políčko vybrané a žádný popup nečeká na Esc sám. */
	private void pridejEsc() {
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "zrusPolickoPlanovace");
		getActionMap().put("zrusPolickoPlanovace", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return vybranePolicko != null && !jinyPopupOtevreny.getAsBoolean();
			}

			@Override
			public void actionPerformed(final java.awt.event.ActionEvent e) {
				zrusPolicko();
			}
		});
	}

	/** Je otevřený popup, který Esc zavírá (popup plánovače, popupy stavového řádku); Esc pak výběr políčka nezruší. */
	public void setJinyPopupOtevreny(final java.util.function.BooleanSupplier jinyPopupOtevreny) {
		this.jinyPopupOtevreny = jinyPopupOtevreny;
	}

	/** Barva textu mezi textem a pozadím tlačítka: výběr platí, ale filtr je vypnutý. */
	private static Color zasedly() {
		final Color t = UIManager.getColor("Button.foreground");
		final Color p = UIManager.getColor("Button.background");
		if (t == null || p == null) {
			return Color.GRAY;
		}
		return new Color((t.getRed() + p.getRed()) / 2, (t.getGreen() + p.getGreen()) / 2, (t.getBlue() + p.getBlue()) / 2);
	}

	private void zrusPolicko() {
		final Challenge c = challenge();
		if (c != null && model != null) {
			model.setVybranePolicko(c, null);
		}
	}

	private void prepniZapnuti() {
		if (model == null) {
			return;
		}
		final boolean zapnout = !model.jeZapnuto();
		model.setZapnuto(zapnout);
		if (zapnout && model.zobrazena() == null) {
			SwingUtilities.invokeLater(this::otevriVyber);
		}
	}

	private void prepniKandidaty() {
		final Challenge c = challenge();
		if (c == null) {
			return;
		}
		try {
			model.setJenKandidatiPro(kandidati.isSelected() ? c : null);
		} catch (final ChallengeChyba e) {
			kandidati.setSelected(false);
		}
	}

	private void prejdi(final boolean dopredu) {
		ChallengeAkce.posun(model, dopredu);
	}

	private void otevriNastaveni() {
		if (vyber != null) {
			vyber.setVisible(false);
		}
		if (naNastaveni != null) {
			naNastaveni.run();
		}
	}

	boolean jeVyberOtevreny() {
		return vyber != null && vyber.isVisible();
	}

	/** Výběr challenge pod chipem: jediná aktivní (přepínač), v nejužším stupni i přepínače kreslení, kopie a dialog plánovače. */
	JPopupMenu sestavVyber() {
		final JPopupMenu m = new JPopupMenu();
		if (model == null) {
			return m;
		}
		final JLabel nadpis = new JLabel("Challenge:");
		nadpis.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
		nadpis.setFont(nadpis.getFont().deriveFont(Font.BOLD));
		m.add(nadpis);
		final ButtonGroup skupina = new ButtonGroup();
		final Challenge zobrazena = model.zobrazena();
		for (final Challenge c : model.vsechny()) {
			if (!model.jeVUi(c)) {
				continue;
			}
			final JRadioButtonMenuItem polozka = new JRadioButtonMenuItem(c.nazev() + (model.jeSablona(c) ? "   (šablona)" : ""));
			polozka.setSelected(c.equals(zobrazena));
			polozka.addActionListener(e -> vyber(c));
			skupina.add(polozka);
			m.add(polozka);
		}
		if (stupen == Stupen.UZKY) {
			m.addSeparator();
			final JCheckBoxMenuItem k = new JCheckBoxMenuItem("Jen kandidáti", kandidati.isSelected());
			k.setEnabled(kandidati.isEnabled());
			k.addActionListener(e -> {
				kandidati.setSelected(k.isSelected());
				prepniKandidaty();
			});
			m.add(k);
			final JCheckBoxMenuItem sekt = prepinacVMenu("Sektory na mapě", model.isKresliSektory(), model::setKresliSektory);
			sekt.setEnabled(sektory.isEnabled());
			m.add(sekt);
			m.add(prepinacVMenu("Štítky u kandidátů", model.isKresliStitky(), model::setKresliStitky));
		}
		m.addSeparator();
		final JMenuItem kopie = new JMenuItem("Kopírovat vybranou…");
		kopie.setEnabled(zobrazena != null && model.jeSablona(zobrazena));
		kopie.setToolTipText("Vlastní kopie vybrané šablony k úpravě v dialogu plánovače");
		kopie.addActionListener(e -> kopiruj());
		m.add(kopie);
		final JMenuItem planovac = new JMenuItem("Nastavení plánovače…", new Ozubene());
		planovac.addActionListener(e -> otevriNastaveni());
		m.add(planovac);
		return m;
	}

	private static JCheckBoxMenuItem prepinacVMenu(final String text, final boolean stav, final java.util.function.Consumer<Boolean> nastav) {
		final JCheckBoxMenuItem p = new JCheckBoxMenuItem(text, stav);
		p.addActionListener(e -> nastav.accept(p.isSelected()));
		return p;
	}

	private void otevriVyber() {
		if (!zapnuto || !isShowing() || jeVyberOtevreny()) {
			return;
		}
		vyber = sestavVyber();
		vyber.show(this, chip.getX(), chip.getY() + chip.getHeight() + 2);
	}

	/** Udělá z challenge jedinou aktivní a zobrazí ji v liště. */
	void vyber(final Challenge c) {
		try {
			for (final Challenge a : model.aktivni()) {
				if (!a.equals(c)) {
					model.deaktivuj(a);
				}
			}
			if (!model.jeAktivni(c)) {
				model.aktivuj(c);
			}
			model.setZobrazena(c);
		} catch (final ChallengeChyba e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Plánovač challenge", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void kopiruj() {
		final Challenge z = model.zobrazena();
		if (z == null || !model.jeSablona(z)) {
			return;
		}
		try {
			vyber(model.kopieSablony(z));
			otevriNastaveni();
		} catch (final ChallengeChyba e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Plánovač challenge", JOptionPane.WARNING_MESSAGE);
		}
	}

	/** Jas barvy 0 až 1. */
	static double jas(final Color c) {
		return (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 255000.0;
	}

	/** Barva textu a ikony na daném pozadí: tmavá na světlém, světlá na tmavém. */
	static Color popredi(final Color pozadi) {
		return pozadi == null || jas(pozadi) > 0.5 ? new Color(0x202020) : new Color(0xF0F0F0);
	}

	/** Přepínač lišty: text a ikona v barvě podle skutečného pozadí, zapnutý má pod obsahem proužek v akcentu skinu. */
	static final class Prepinac extends JToggleButton {
		private static final long serialVersionUID = 1L;

		Prepinac(final Icon ikona) {
			super(ikona);
			addItemListener(e -> obnovPopredi());
			addPropertyChangeListener("enabled", e -> obnovPopredi());
		}

		@Override
		public void updateUI() {
			super.updateUI();
			obnovPopredi();
		}

		/** Pozadí zapnutého přepínače: hlavní barva skinu, aby se zapnutý nespletl s nedostupným. */
		static Color pozadiZapnuteho() {
			final Color c = javax.swing.plaf.metal.MetalLookAndFeel.getPrimaryControl();
			return c != null ? c : UIManager.getColor("ToggleButton.select");
		}

		boolean jeZvyrazneny() {
			return isSelected() && isEnabled();
		}

		void obnovPopredi() {
			final Color pozadi = jeZvyrazneny() ? pozadiZapnuteho() : UIManager.getColor(isSelected() ? "ToggleButton.select" : "ToggleButton.background");
			setForeground(popredi(pozadi != null ? pozadi : UIManager.getColor("Panel.background")));
			setContentAreaFilled(!jeZvyrazneny());
		}

		@Override
		protected void paintComponent(final Graphics g) {
			if (jeZvyrazneny()) {
				g.setColor(pozadiZapnuteho());
				g.fillRect(0, 0, getWidth(), getHeight());
			}
			super.paintComponent(g);
			if (jeZvyrazneny()) {
				g.setColor(akcentNa(pozadiZapnuteho()));
				g.fillRect(3, getHeight() - 5, getWidth() - 6, 3);
			}
		}
	}

	/** Červený odznak s počtem chybějících. */
	private static final class Odznak extends JButton {
		private static final long serialVersionUID = 1L;

		Odznak() {
			super("…");
			setContentAreaFilled(false);
			setBorderPainted(false);
			setForeground(Color.WHITE);
			setFont(getFont().deriveFont(Font.BOLD));
		}

		void nastav(final String text) {
			setText(text);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			final Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setColor(isEnabled() ? CERVENA : Color.GRAY);
				g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
			} finally {
				g2.dispose();
			}
			super.paintComponent(g);
		}
	}

	/** Základ ikon lišty: vyhlazené kreslení barvou popředí tlačítka (šedou, je-li nedostupné). */
	private abstract static class Ikona implements Icon {
		private final int w, h;

		Ikona(final int w, final int h) {
			this.w = w;
			this.h = h;
		}

		@Override
		public final void paintIcon(final Component c, final Graphics g, final int x, final int y) {
			final Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.translate(x, y);
				g2.setColor(c.isEnabled() ? c.getForeground() : Color.GRAY);
				kresli(g2);
			} finally {
				g2.dispose();
			}
		}

		abstract void kresli(Graphics2D g);

		@Override
		public int getIconWidth() {
			return w;
		}

		@Override
		public int getIconHeight() {
			return h;
		}
	}

	/** Terč úchytu v barvě popředí tlačítka. */
	private static final class Terc extends Ikona {
		Terc() {
			super(16, 16);
		}

		@Override
		void kresli(final Graphics2D g) {
			g.setStroke(new BasicStroke(1.6f));
			g.drawOval(1, 1, 13, 13);
			g.drawOval(4, 4, 7, 7);
			g.fillOval(7, 7, 2, 2);
		}
	}

	/** Trojúhelníková šipka doleva nebo doprava. */
	private static final class Sipka extends Ikona {
		private final boolean doleva;

		Sipka(final boolean doleva) {
			super(8, 10);
			this.doleva = doleva;
		}

		@Override
		void kresli(final Graphics2D g) {
			final int[] xs = doleva ? new int[] { 8, 0, 8 } : new int[] { 0, 8, 0 };
			g.fillPolygon(xs, new int[] { 0, 5, 10 }, 3);
		}
	}

	/** Ikona trychtýře filtru. */
	private static final class Trychtyr extends Ikona {
		Trychtyr() {
			super(12, 12);
		}

		@Override
		void kresli(final Graphics2D g) {
			g.fillPolygon(new int[] { 0, 12, 7, 7, 5, 5 }, new int[] { 0, 0, 6, 11, 11, 6 }, 6);
		}
	}

	/** Ikona klínu sektoru. */
	private static final class Klin extends Ikona {
		Klin() {
			super(14, 14);
		}

		@Override
		void kresli(final Graphics2D g) {
			g.setStroke(new BasicStroke(1.3f));
			g.draw(new java.awt.geom.Arc2D.Double(-12, 1, 26, 26, 20, 50, java.awt.geom.Arc2D.PIE));
		}
	}

	/** Ikona štítku. */
	private static final class Stitek extends Ikona {
		Stitek() {
			super(14, 14);
		}

		@Override
		void kresli(final Graphics2D g) {
			g.setStroke(new BasicStroke(1.3f));
			g.drawPolygon(new int[] { 1, 9, 13, 9, 1 }, new int[] { 3, 3, 7, 11, 11 }, 5);
			g.fillOval(3, 6, 2, 2);
		}
	}

	/** Ikona ozubeného kola nastavení. */
	static final class Ozubene extends Ikona {
		Ozubene() {
			super(14, 14);
		}

		@Override
		void kresli(final Graphics2D g) {
			g.translate(7, 7);
			for (int i = 0; i < 8; i++) {
				g.fillRect(-1, -7, 2, 3);
				g.rotate(Math.PI / 4);
			}
			g.setStroke(new BasicStroke(2f));
			g.drawOval(-4, -4, 8, 8);
		}
	}
}
