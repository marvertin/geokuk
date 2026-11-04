/**
 *
 */
package cz.geokuk.util.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * Dva svislé tenké posuvníky nad mapou, barva (stupeň šedi) a průhlednost. Nadpisy, popisky os a bublinka s hodnotou se kreslí jen při interakci (najetí myší, tažení, kolečko) a
 * to přes {@link #kresliNadMapou}, aby je vrstva nad mapou mohla vykreslit nad ostatními vrstvami.
 *
 * @author Martin Veverka
 *
 */
public class JBarvovyDvojSlider extends JPanel {

	private static final long serialVersionUID = 5956221175094609896L;

	private static final int MAX = 255;
	/** Vzdálenost popisků os od osy posuvníku. */
	private static final int MEZERA_OSY = 12;
	/** Mezera mezi popisky a bublinkou. */
	private static final int MEZERA_BUBLINKY = 14;
	/** Výška místa nad posuvníkem pro nadpis. */
	private static final int VYSKA_NADPISU = 30;
	private static final String[] TEXTY_OSY_BARVY = { "Černá", "Šedá", "Bílá" };
	private static final int[] HODNOTY_OSY_BARVY = { 0, 128, MAX };

	private JSlider iBarvovnik;
	private JSlider iPruhlednik;
	private final BublinkaHodnoty bublinkaBarvy;
	private final BublinkaHodnoty bublinkaPruhlednosti;
	private final ViditelnostLegendy legenda;
	private final List<Sledovani> sledovani = new ArrayList<>();
	/** Hodnoty se právě nastavují z modelu, bublinka se neukazuje. */
	private boolean nastavuji;
	/** Kde se legenda naposledy kreslila, ve značkách vrstvy; překreslí se při dalším kroku animace. */
	private Rectangle naposledy = new Rectangle();
	/** Vrstva, do které se legenda kreslí a kam se posílají překreslení. */
	private Component vrstva;

	public JBarvovyDvojSlider() {
		this(BublinkaHodnoty::new, null);
	}

	/** V testech se bublinky a legenda vyrábějí s vlastními hodinami a časovačem. */
	JBarvovyDvojSlider(final Function<Runnable, BublinkaHodnoty> vyrobaBublinky, final Function<Runnable, ViditelnostLegendy> vyrobaLegendy) {
		bublinkaBarvy = vyrobaBublinky.apply(this::prekresliLegendu);
		bublinkaPruhlednosti = vyrobaBublinky.apply(this::prekresliLegendu);
		legenda = vyrobaLegendy != null ? vyrobaLegendy.apply(this::prekresliLegendu) : new ViditelnostLegendy(this::prekresliLegendu);
		initComponents();
		addHierarchyListener(e -> {
			if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing()) {
				zrusBublinky();
			}
		});
	}

	/** Vrstva nad mapou, která legendu kreslí; kvůli překreslování jen oblasti legendy. */
	public void setVrstvaLegendy(final Component vrstva) {
		this.vrstva = vrstva;
	}

	@Override
	public void removeNotify() {
		zrusBublinky();
		super.removeNotify();
	}

	/** Vrstva se skryla nebo zanikla uprostřed tažení, puštění už nepřijde. */
	void zrusBublinky() {
		for (final Sledovani s : sledovani) {
			s.tahne = false;
			s.bublinka.zrus();
		}
		legenda.zrus();
	}

	private Font fontBublinky() {
		final Font f = getFont() != null ? getFont() : new JLabel().getFont();
		return f.deriveFont(Font.BOLD, 12f);
	}

	private Font fontLegendy() {
		final Font f = getFont() != null ? getFont() : new JLabel().getFont();
		return f.deriveFont(Font.PLAIN, 11f);
	}

	/** Stav tažení jednoho posuvníku. */
	private static final class Sledovani {
		final JSlider slider;
		final BublinkaHodnoty bublinka;
		boolean tahne;
		int posledni;

		Sledovani(final JSlider slider, final BublinkaHodnoty bublinka) {
			this.slider = slider;
			this.bublinka = bublinka;
			this.posledni = slider.getValue();
		}
	}

	/** Popisek na ose: hodnota posuvníku a text. */
	static final class Popisek {
		final int hodnota;
		final String text;

		Popisek(final int hodnota, final String text) {
			this.hodnota = hodnota;
			this.text = text;
		}
	}

	/** Popisky osy barvy: černá dole, šedá uprostřed, bílá nahoře. */
	static List<Popisek> osaBarvy() {
		final List<Popisek> l = new ArrayList<>();
		for (int i = 0; i < HODNOTY_OSY_BARVY.length; i++) {
			l.add(new Popisek(HODNOTY_OSY_BARVY[i], TEXTY_OSY_BARVY[i]));
		}
		return l;
	}

	/** Popisky osy průhlednosti po 25 %; 0 % (plně krycí) nahoře. */
	static List<Popisek> osaPruhlednosti() {
		final List<Popisek> l = new ArrayList<>();
		for (int procent = 0; procent <= 100; procent += 25) {
			l.add(new Popisek(krytiProPruhlednost(procent), procent + " %"));
		}
		return l;
	}

	/**
	 * @param aChangeListener
	 */
	public void addChangeListener(final ChangeListener aChangeListener) {
		iBarvovnik.addChangeListener(aChangeListener);
		iPruhlednik.addChangeListener(aChangeListener);
	}

	/**
	 * @return the barvovnik
	 */
	public JSlider getBarvovnik() {
		return iBarvovnik;
	}

	public Color getColor() {
		final int barva = iBarvovnik.getModel().getValue();
		final int pruhlednost = iPruhlednik.getModel().getValue();
		return new Color(barva, barva, barva, pruhlednost);
	}

	/**
	 * @return the pruhlednik
	 */
	public JSlider getPruhlednik() {
		return iPruhlednik;
	}

	public void setColor(final Color color) {
		final int barva = (color.getBlue() + color.getRed() + color.getGreen()) / 3;
		final int pruhlednost = color.getAlpha();
		nastavuji = true;
		try {
			iBarvovnik.getModel().setValue(barva);
			iPruhlednik.getModel().setValue(pruhlednost);
		} finally {
			nastavuji = false;
		}
	}

	/** Barva v procentech ve směru osy: nahoře je bílá (0 %), dole černá (100 %). */
	public static String textBarvy(final int hodnota) {
		return barvaProcent(hodnota) + " %";
	}

	static int barvaProcent(final int hodnota) {
		return Math.round((MAX - hodnota) * 100f / MAX);
	}

	/** Průhlednost v procentech z krytí 0 až 255 ve směru osy: nahoře plně krycí (0 %), dole neviditelná (100 %). */
	public static String textPruhlednosti(final int krytiAlfa) {
		return pruhlednostProcent(krytiAlfa) + " %";
	}

	static int pruhlednostProcent(final int krytiAlfa) {
		return Math.round((MAX - krytiAlfa) * 100f / MAX);
	}

	/** Hodnota posuvníku (krytí) pro průhlednost v procentech, pro popisky os. */
	static int krytiProPruhlednost(final int procent) {
		return Math.round(MAX - procent * MAX / 100f);
	}

	/**
	 * Svislá poloha jezdce svislého posuvníku od jeho horního okraje: nejvyšší hodnota je nahoře, jezdec zabírá z každého konce aspoň {@code okraj} pixelů.
	 */
	static int polohaJezdce(final int vyska, final int okraj, final int hodnota, final int min, final int max) {
		final double podil = max == min ? 1 : (hodnota - min) / (double) (max - min);
		return okraj + (int) Math.round((1 - podil) * (vyska - 2 * okraj));
	}

	/** Střed osy posuvníku ve značkách vrstvy. */
	private Point stredOsy(final JSlider s, final Component kam) {
		final Point p = SwingUtilities.convertPoint(s, s.getWidth() / 2, 0, kam);
		return p;
	}

	private int yHodnoty(final JSlider s, final Component kam, final int hodnota) {
		final Point p = SwingUtilities.convertPoint(s, 0, 0, kam);
		final Insets in = s.getInsets();
		return p.y + in.top + polohaJezdce(s.getHeight() - in.top - in.bottom, okraj(), hodnota, s.getMinimum(), s.getMaximum());
	}

	private BublinkaHodnoty bublinka(final JSlider s) {
		return s == iBarvovnik ? bublinkaBarvy : bublinkaPruhlednosti;
	}

	/** Šířka nejdelšího popisku os, podle ní se odsadí bublinky. */
	private int sirkaOsy(final FontMetrics fm) {
		int w = 0;
		for (final Popisek p : osaBarvy()) {
			w = Math.max(w, PopisekNaMape.hranice(fm, p.text, 0, 0).width);
		}
		for (final Popisek p : osaPruhlednosti()) {
			w = Math.max(w, PopisekNaMape.hranice(fm, p.text, 0, 0).width);
		}
		return w;
	}

	/**
	 * Nakreslí legendu (nadpisy, popisky os) a bublinky s hodnotami; volá vrstva nad mapou, které posuvníky patří. Vrací souhrnný obdélník ve značkách vrstvy, nebo null, když se
	 * nic nekreslilo.
	 */
	public Rectangle kresliNadMapou(final Graphics2D g, final Component kam) {
		Rectangle souhrn = null;
		final float v = legenda.viditelnost();
		final Font fontL = fontLegendy();
		final FontMetrics fmL = g.getFontMetrics(fontL);
		final int sirkaOsy = sirkaOsy(fmL);
		final int pravyOkrajPanelu = SwingUtilities.convertPoint(this, getWidth(), 0, kam).x;
		for (final JSlider s : new JSlider[] { iBarvovnik, iPruhlednik }) {
			final int osaX = stredOsy(s, kam).x;
			final int pravyOkrajPopisku = osaX - MEZERA_OSY;
			if (v > 0f) {
				final boolean barva = s == iBarvovnik;
				for (final Popisek p : barva ? osaBarvy() : osaPruhlednosti()) {
					final int y = yHodnoty(s, kam, p.hodnota);
					PopisekNaMape.znacka(g, osaX - MEZERA_OSY + 2, osaX - 3, y, v);
					souhrn = sjednot(souhrn, PopisekNaMape.nakresli(g, fontL, p.text, pravyOkrajPopisku, y, v));
				}
				final Point nahore = SwingUtilities.convertPoint(s, 0, 0, kam);
				souhrn = sjednot(souhrn, PopisekNaMape.nakresli(g, fontL, barva ? "Barva" : "Průhlednost", pravyOkrajPanelu, nahore.y - VYSKA_NADPISU / 2, v));
			}
			final BublinkaHodnoty b = bublinka(s);
			final float vb = b.viditelnost();
			if (vb > 0f) {
				final Rectangle r = PopisekNaMape.nakresli(g, fontBublinky(), b.getText(), pravyOkrajPopisku - sirkaOsy - MEZERA_BUBLINKY, yHodnoty(s, kam, s.getValue()), vb);
				souhrn = sjednot(souhrn, r);
			}
		}
		naposledy = souhrn == null ? new Rectangle() : souhrn;
		return souhrn;
	}

	private static Rectangle sjednot(final Rectangle a, final Rectangle b) {
		if (b == null) {
			return a;
		}
		return a == null ? b : a.union(b);
	}

	/** Kolik pixelů z každého konce zabírá jezdec. */
	private static int okraj() {
		int vyska = 16;
		final Icon jezdec = UIManager.getIcon("Slider.verticalThumbIcon");
		if (jezdec != null) {
			vyska = Math.max(vyska, jezdec.getIconHeight());
		}
		return vyska / 2;
	}

	/** Překreslí jen oblast legendy a bublinek, ne celou mapu. */
	private void prekresliLegendu() {
		final Component kam = vrstva != null ? vrstva : getParent();
		if (kam == null) {
			repaint();
			return;
		}
		final Rectangle oblast = new Rectangle(naposledy);
		final Point vlevoNahore = SwingUtilities.convertPoint(this, 0, 0, kam);
		final FontMetrics fm = getFontMetrics(fontLegendy());
		final int odOkraje = sirkaOsy(fm) + MEZERA_OSY + MEZERA_BUBLINKY + 190;
		oblast.add(new Rectangle(vlevoNahore.x - odOkraje, vlevoNahore.y - VYSKA_NADPISU - 10, odOkraje + getWidth() + 4, getHeight() + 2 * VYSKA_NADPISU));
		oblast.grow(2, 2);
		kam.repaint(oblast.x, oblast.y, oblast.width, oblast.height);
	}

	/**
	 *
	 */
	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setOpaque(false);

		iBarvovnik = new JSlider(SwingConstants.VERTICAL);
		iBarvovnik.setOpaque(false);
		iBarvovnik.setToolTipText("Nastavení stupně šedi kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
		iBarvovnik.setFocusable(false);
		final DefaultBoundedRangeModel barvovnikModel = new DefaultBoundedRangeModel(128, 0, 0, MAX);
		iBarvovnik.setModel(barvovnikModel);

		iPruhlednik = new JSlider(SwingConstants.VERTICAL);
		iPruhlednik.setOpaque(false);
		iPruhlednik.setToolTipText("Nastavení průhlednosti kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
		iPruhlednik.setFocusable(false);
		final DefaultBoundedRangeModel pruhlednikModel = new DefaultBoundedRangeModel(128, 0, 0, MAX);
		iPruhlednik.setModel(pruhlednikModel);

		sledujBublinku(iBarvovnik, bublinkaBarvy, JBarvovyDvojSlider::textBarvy);
		sledujBublinku(iPruhlednik, bublinkaPruhlednosti, JBarvovyDvojSlider::textPruhlednosti);
		sledujMys();

		add(Box.createRigidArea(new Dimension(0, VYSKA_NADPISU)));
		add(iBarvovnik);
		add(Box.createRigidArea(new Dimension(0, VYSKA_NADPISU + 10)));
		add(iPruhlednik);
		add(Box.createRigidArea(new Dimension(0, 20)));
	}

	/** Najetí myší na pruh posuvníků a fokus na posuvníku ukazují legendu. */
	private void sledujMys() {
		final MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				aktualizujMys(e);
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				aktualizujMys(e);
			}

			@Override
			public void mouseMoved(final MouseEvent e) {
				aktualizujMys(e);
			}
		};
		for (final Component c : new Component[] { this, iBarvovnik, iPruhlednik }) {
			c.addMouseListener(ma);
			c.addMouseMotionListener(ma);
		}
		final FocusListener fl = new FocusAdapter() {
			@Override
			public void focusGained(final FocusEvent e) {
				legenda.fokus(true);
			}

			@Override
			public void focusLost(final FocusEvent e) {
				legenda.fokus(false);
			}
		};
		iBarvovnik.addFocusListener(fl);
		iPruhlednik.addFocusListener(fl);
	}

	/** Myš je nad pruhem, když je v mezích panelu, i když přešla z jednoho posuvníku na druhý. */
	private void aktualizujMys(final MouseEvent e) {
		final Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), this);
		legenda.mys(contains(p));
	}

	/** Bublinka a legenda se ukazují při tažení i při změně kolečkem nebo klávesou, ne při nastavení z modelu ani při pouhé změně příznaku tažení. */
	private void sledujBublinku(final JSlider s, final BublinkaHodnoty bublinka, final IntFunction<String> text) {
		final Sledovani sl = new Sledovani(s, bublinka);
		sledovani.add(sl);
		s.addChangeListener(e -> {
			final int hodnota = s.getValue();
			if (nastavuji) {
				sl.posledni = hodnota;
				return;
			}
			final String t = text.apply(hodnota);
			if (s.getValueIsAdjusting()) {
				sl.tahne = true;
				sl.posledni = hodnota;
				bublinka.tahne(t);
				legenda.tahne(true);
			} else if (sl.tahne) {
				sl.tahne = false;
				sl.posledni = hodnota;
				bublinka.tahne(t);
				bublinka.pust();
				legenda.tahne(jeNekdoTahne());
			} else if (hodnota != sl.posledni) {
				sl.posledni = hodnota;
				bublinka.dotkni(t);
				legenda.dotyk();
			}
		});
	}

	private boolean jeNekdoTahne() {
		for (final Sledovani s : sledovani) {
			if (s.tahne) {
				return true;
			}
		}
		return false;
	}
}
