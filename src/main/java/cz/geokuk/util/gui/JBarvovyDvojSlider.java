/**
 *
 */
package cz.geokuk.util.gui;

import java.awt.*;
import java.util.Hashtable;
import java.util.function.Function;
import java.util.function.IntFunction;

import javax.swing.*;
import javax.swing.event.ChangeListener;

/**
 * Dva svislé posuvníky nad mapou, barva (stupeň šedi) a průhlednost, s nadpisy, popisky os a bublinkou s hodnotou při tažení.
 *
 * @author Martin Veverka
 *
 */
public class JBarvovyDvojSlider extends JPanel {

	private static final long serialVersionUID = 5956221175094609896L;

	private static final int MAX = 255;
	/** Mezera mezi bublinkou a posuvníkem. */
	private static final int MEZERA_BUBLINKY = 8;

	private JSlider iBarvovnik;
	private JSlider iPruhlednik;
	private final BublinkaHodnoty bublinkaBarvy;
	private final BublinkaHodnoty bublinkaPruhlednosti;
	private final java.util.List<Sledovani> sledovani = new java.util.ArrayList<>();
	/** Hodnoty se právě nastavují z modelu, bublinka se neukazuje. */
	private boolean nastavuji;
	/** Kde se bublinky naposledy kreslily, ve značkách rodiče; překreslí se při dalším kroku animace. */
	private Rectangle naposledy = new Rectangle();

	public JBarvovyDvojSlider() {
		this(BublinkaHodnoty::new);
	}

	/** V testech se bublinky vyrábějí s vlastními hodinami a časovačem. */
	JBarvovyDvojSlider(final Function<Runnable, BublinkaHodnoty> vyrobaBublinky) {
		bublinkaBarvy = vyrobaBublinky.apply(this::prekresliBublinky);
		bublinkaPruhlednosti = vyrobaBublinky.apply(this::prekresliBublinky);
		initComponents();
		addHierarchyListener(e -> {
			if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing()) {
				zrusBublinky();
			}
		});
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
	}

	private Font fontBublinky() {
		final Font f = getFont() != null ? getFont() : new JLabel().getFont();
		return f.deriveFont(Font.BOLD, 12f);
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

	/** Jas v procentech; 0 je černá, 100 bílá. */
	public static String textBarvy(final int hodnota) {
		return "Jas " + Math.round(hodnota * 100f / MAX) + " %";
	}

	/** Průhlednost v procentech z krytí 0 až 255: 0 % je plně krycí, 100 % neviditelná. */
	public static String textPruhlednosti(final int krytiAlfa) {
		return "Průhlednost " + pruhlednostProcent(krytiAlfa) + " %";
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

	/** Nakreslí bublinky s hodnotami u jezdců; volá vrstva, které posuvníky patří, ze své kresby potomků. Vrací souhrnný obdélník ve značkách vrstvy. */
	public Rectangle kresliBublinky(final Graphics2D g, final Component vrstva) {
		Rectangle souhrn = null;
		for (final JSlider s : new JSlider[] { iBarvovnik, iPruhlednik }) {
			final BublinkaHodnoty b = bublinka(s);
			if (b.viditelnost() <= 0f) {
				continue;
			}
			final Point p = SwingUtilities.convertPoint(s, 0, 0, vrstva);
			final int stredY = p.y + polohaJezdce(s.getHeight() - s.getInsets().top - s.getInsets().bottom, okraj(s), s.getValue(), s.getMinimum(), s.getMaximum()) + s.getInsets().top;
			final Rectangle r = b.kresli(g, fontBublinky(), p.x - MEZERA_BUBLINKY, stredY);
			if (r != null) {
				souhrn = souhrn == null ? r : souhrn.union(r);
			}
		}
		naposledy = souhrn == null ? new Rectangle() : souhrn;
		return souhrn;
	}

	private BublinkaHodnoty bublinka(final JSlider s) {
		return s == iBarvovnik ? bublinkaBarvy : bublinkaPruhlednosti;
	}

	/** Kolik pixelů z každého konce zabírá jezdec nebo polovina popisku. */
	private static int okraj(final JSlider s) {
		int vyska = 16;
		final Icon jezdec = UIManager.getIcon("Slider.verticalThumbIcon");
		if (jezdec != null) {
			vyska = Math.max(vyska, jezdec.getIconHeight());
		}
		if (s.getLabelTable() != null) {
			final java.util.Enumeration<?> popisky = s.getLabelTable().elements();
			while (popisky.hasMoreElements()) {
				final Object o = popisky.nextElement();
				if (o instanceof Component) {
					vyska = Math.max(vyska, ((Component) o).getPreferredSize().height);
				}
			}
		}
		return vyska / 2;
	}

	/** Překreslí jen oblast bublinek, ne celou mapu. */
	private void prekresliBublinky() {
		final Container rodic = getParent();
		if (rodic == null) {
			repaint();
			return;
		}
		final Rectangle oblast = new Rectangle(naposledy);
		final FontMetrics fm = getFontMetrics(fontBublinky());
		for (final JSlider s : new JSlider[] { iBarvovnik, iPruhlednik }) {
			final BublinkaHodnoty b = bublinka(s);
			if (!b.jeAktivni()) {
				continue;
			}
			final Point p = SwingUtilities.convertPoint(s, 0, 0, rodic);
			final int stredY = p.y + polohaJezdce(s.getHeight() - s.getInsets().top - s.getInsets().bottom, okraj(s), s.getValue(), s.getMinimum(), s.getMaximum()) + s.getInsets().top;
			final int w = fm.stringWidth(b.getText()) + 16;
			final int h = fm.getHeight() + 8;
			oblast.add(new Rectangle(p.x - MEZERA_BUBLINKY - w, stredY - h / 2, w, h));
		}
		if (oblast.isEmpty()) {
			return;
		}
		oblast.grow(2, 2);
		rodic.repaint(oblast.x, oblast.y, oblast.width, oblast.height);
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
		final Hashtable<Integer, JComponent> popiskyBarvy = new Hashtable<>();
		popiskyBarvy.put(0, new JPopisekNaMape("Černá", true));
		popiskyBarvy.put(128, new JPopisekNaMape("Šedá", true));
		popiskyBarvy.put(MAX, new JPopisekNaMape("Bílá", true));
		nastavOsu(iBarvovnik, popiskyBarvy);

		iPruhlednik = new JSlider(SwingConstants.VERTICAL);
		iPruhlednik.setOpaque(false);
		iPruhlednik.setToolTipText("Nastavení průhlednosti kruhů (161 m), kterými se zobrazí kešemi obsazené oblasti.");
		iPruhlednik.setFocusable(false);
		final DefaultBoundedRangeModel pruhlednikModel = new DefaultBoundedRangeModel(128, 0, 0, MAX);
		iPruhlednik.setModel(pruhlednikModel);
		final Hashtable<Integer, JComponent> popiskyPruhlednosti = new Hashtable<>();
		for (int procent = 0; procent <= 100; procent += 25) {
			popiskyPruhlednosti.put(krytiProPruhlednost(procent), new JPopisekNaMape(procent + " %", true));
		}
		nastavOsu(iPruhlednik, popiskyPruhlednosti);

		sledujBublinku(iBarvovnik, bublinkaBarvy, JBarvovyDvojSlider::textBarvy);
		sledujBublinku(iPruhlednik, bublinkaPruhlednosti, JBarvovyDvojSlider::textPruhlednosti);

		add(Box.createRigidArea(new Dimension(0, 20)));
		add(nadpis("Barva"));
		add(Box.createRigidArea(new Dimension(0, 8)));
		add(iBarvovnik);
		add(Box.createRigidArea(new Dimension(0, 20)));
		add(nadpis("Průhlednost"));
		add(Box.createRigidArea(new Dimension(0, 8)));
		add(iPruhlednik);
		add(Box.createRigidArea(new Dimension(0, 20)));
	}

	private static JLabel nadpis(final String text) {
		final JLabel l = new JPopisekNaMape(text, false);
		l.setAlignmentX(Component.CENTER_ALIGNMENT);
		return l;
	}

	private static void nastavOsu(final JSlider s, final Hashtable<Integer, JComponent> popisky) {
		s.setLabelTable(popisky);
		s.setPaintLabels(true);
	}

	/** Bublinka se ukazuje při tažení i při změně kolečkem nebo klávesou, ne při nastavení z modelu ani při pouhé změně příznaku tažení. */
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
			} else if (sl.tahne) {
				sl.tahne = false;
				sl.posledni = hodnota;
				bublinka.tahne(t);
				bublinka.pust();
			} else if (hodnota != sl.posledni) {
				sl.posledni = hodnota;
				bublinka.dotkni(t);
			}
		});
	}
}
