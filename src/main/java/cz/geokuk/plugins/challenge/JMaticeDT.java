package cz.geokuk.plugins.challenge;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Klikací matice D/T 9×9 (obtížnost v řádcích, terén ve sloupcích, 1 až 5 po půlkách). Políčko ukazuje stav barvou i znakem: ✓ splněno, tučný počet kandidátů (na limitu
 * „50+“), – chybí bez kandidátů. Klik nebo Enter na políčku s kandidáty ho vybere, druhý klik nebo Esc výběr zruší; šipky posouvají fokus.
 */
public class JMaticeDT extends JComponent {

	private static final long serialVersionUID = 1L;

	static final int N = 9;
	static final int SIRKA = 32, VYSKA = 24, ZAHLAVI = 18, POPISKY = 34;

	/** Stav políčka: splněno, chybí bez kandidátů, chybí s kandidáty. */
	enum Stav {
		SPLNENO, BEZ_KANDIDATU, S_KANDIDATY
	}

	private final Set<String> chybejici = new HashSet<>();
	private final Map<String, Integer> pocty = new HashMap<>();
	private transient VysledekPolicek vysledek;
	/** Kolikrát splnit každé políčko (násobnost); 1 = jednou. */
	private int cil = 1;
	private String vybrane;
	private int fokusD = 0, fokusT = 0;
	private transient Consumer<String> naVyber = s -> {};

	public JMaticeDT() {
		setFocusable(true);
		setToolTipText("");
		setOpaque(false);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				requestFocusInWindow();
				final int[] p = policko(e.getX(), e.getY());
				if (p != null) {
					fokusD = p[0];
					fokusT = p[1];
					prepni(p[0], p[1]);
				}
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (klavesa(e.getKeyCode())) {
					e.consume();
				}
			}
		});
	}

	/** Co se stane po výběru políčka; null = výběr zrušen. */
	public void setNaVyber(final Consumer<String> naVyber) {
		this.naVyber = naVyber == null ? s -> {} : naVyber;
	}

	/** Data z výsledku: chybějící políčka a počty kandidátů po štítcích. */
	public void nastav(final VysledekPolicek v) {
		chybejici.clear();
		pocty.clear();
		vysledek = v;
		cil = v == null ? 1 : Math.max(1, v.cil());
		if (v != null) {
			chybejici.addAll(v.stitkyChybejicich());
			pocty.putAll(v.pocetKandidatuPoPolickach());
		}
		if (vybrane != null && stav(vybrane) != Stav.S_KANDIDATY) {
			vybrane = null;
		}
		repaint();
	}

	/** Vybrané políčko podle modelu (bez ohlášení). */
	public void setVybrane(final String stitek) {
		vybrane = stitek;
		if (stitek != null) {
			final int[] p = poloha(stitek);
			if (p != null) {
				fokusD = p[0];
				fokusT = p[1];
			}
		}
		repaint();
	}

	public String vybrane() {
		return vybrane;
	}

	static String stitek(final int d, final int t) {
		return VypocetMatice.stitek(d, t);
	}

	static String hodnota(final int pulky) {
		return pulky % 2 == 0 ? String.valueOf(1 + pulky / 2) : (1 + pulky / 2) + ",5";
	}

	private static int[] poloha(final String stitek) {
		for (int d = 0; d < N; d++) {
			for (int t = 0; t < N; t++) {
				if (stitek(d, t).equals(stitek)) {
					return new int[] { d, t };
				}
			}
		}
		return null;
	}

	Stav stav(final String stitek) {
		if (!chybejici.contains(stitek)) {
			return Stav.SPLNENO;
		}
		return pocty.getOrDefault(stitek, 0) > 0 ? Stav.S_KANDIDATY : Stav.BEZ_KANDIDATU;
	}

	/** Počet kandidátů jako text: plné políčko „50+“. */
	String pocet(final String stitek) {
		final int n = pocty.getOrDefault(stitek, 0);
		return jePlne(stitek) ? n + "+" : String.valueOf(n);
	}

	private boolean jePlne(final String stitek) {
		return vysledek != null && vysledek.jePlne(stitek);
	}

	/** Znak v políčku: „✓“, u N = 1 počet kandidátů nebo „–“, s násobností nálezy z cíle („1/3“). */
	String znak(final String stitek) {
		switch (stav(stitek)) {
		case SPLNENO:
			return "✓";
		case BEZ_KANDIDATU:
			return cil > 1 ? nalezyZCile(stitek) : "–";
		default:
			return cil > 1 ? nalezyZCile(stitek) : pocet(stitek);
		}
	}

	/** S násobností ukazuje políčko nálezy; kandidáty značí tečka vpravo nahoře. */
	boolean maTecku(final String stitek) {
		return cil > 1 && stav(stitek) == Stav.S_KANDIDATY;
	}

	/** Nálezy políčka z cíle („1/3“), jen s násobností. */
	String nalezyZCile(final String stitek) {
		return Math.min(nalezu(stitek), cil) + "/" + cil;
	}

	private int nalezu(final String stitek) {
		return vysledek == null ? 0 : Math.max(0, vysledek.pocetNalezu(stitek));
	}

	String popis(final String stitek) {
		if (cil > 1) {
			final String nalezy = stitek + " – " + TextyPopupuPlanovace.nalezu(nalezu(stitek)) + ", cíl " + cil;
			switch (stav(stitek)) {
			case SPLNENO:
				return nalezy + ", splněno";
			case BEZ_KANDIDATU:
				return nalezy + ", nemá kandidáty";
			default:
				final int n = pocty.getOrDefault(stitek, 0);
				return nalezy + ", " + (jePlne(stitek) ? n + "+ kandidátů" : TextyPopupuPlanovace.kandidatu(n));
			}
		}
		switch (stav(stitek)) {
		case SPLNENO:
			return stitek + " – splněno";
		case BEZ_KANDIDATU:
			return stitek + " – chybí, nemá kandidáty";
		default:
			final int n = pocty.getOrDefault(stitek, 0);
			return stitek + " – chybí, " + (jePlne(stitek) ? n + "+ kandidátů" : TextyPopupuPlanovace.kandidatu(n));
		}
	}

	private void prepni(final int d, final int t) {
		final String s = stitek(d, t);
		if (stav(s) != Stav.S_KANDIDATY) {
			repaint();
			return;
		}
		vybrane = s.equals(vybrane) ? null : s;
		repaint();
		naVyber.accept(vybrane);
	}

	/** Klávesy matice; vrací, zda je zpracovala. Esc jen při výběru, jinak ho nechá popupu. */
	boolean klavesa(final int kod) {
		switch (kod) {
		case KeyEvent.VK_LEFT:
			fokusT = Math.max(0, fokusT - 1);
			break;
		case KeyEvent.VK_RIGHT:
			fokusT = Math.min(N - 1, fokusT + 1);
			break;
		case KeyEvent.VK_UP:
			fokusD = Math.max(0, fokusD - 1);
			break;
		case KeyEvent.VK_DOWN:
			fokusD = Math.min(N - 1, fokusD + 1);
			break;
		case KeyEvent.VK_ENTER:
		case KeyEvent.VK_SPACE:
			prepni(fokusD, fokusT);
			return true;
		case KeyEvent.VK_ESCAPE:
			if (vybrane == null) {
				return false;
			}
			vybrane = null;
			naVyber.accept(null);
			break;
		case KeyEvent.VK_HOME:
		case KeyEvent.VK_END:
			skocNaKraj(kod == KeyEvent.VK_HOME);
			break;
		default:
			return false;
		}
		repaint();
		return true;
	}

	private void skocNaKraj(final boolean prvni) {
		for (int i = 0; i < N * N; i++) {
			final int j = prvni ? i : N * N - 1 - i;
			if (stav(stitek(j / N, j % N)) == Stav.S_KANDIDATY) {
				fokusD = j / N;
				fokusT = j % N;
				return;
			}
		}
	}

	int[] fokus() {
		return new int[] { fokusD, fokusT };
	}

	/** Políčko pod bodem, nebo null. */
	int[] policko(final int x, final int y) {
		final int t = (x - POPISKY) / SIRKA;
		final int d = (y - ZAHLAVI) / VYSKA;
		if (x < POPISKY || y < ZAHLAVI || t >= N || d >= N) {
			return null;
		}
		return new int[] { d, t };
	}

	Rectangle obdelnik(final int d, final int t) {
		return new Rectangle(POPISKY + t * SIRKA, ZAHLAVI + d * VYSKA, SIRKA, VYSKA);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(POPISKY + N * SIRKA + 1, ZAHLAVI + N * VYSKA + 1);
	}

	@Override
	public String getToolTipText(final MouseEvent e) {
		final int[] p = policko(e.getX(), e.getY());
		return p == null ? null : popis(stitek(p[0], p[1]));
	}

	static double jas(final Color c) {
		return (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 255000.0;
	}

	static Color smichej(final Color a, final Color b, final double t) {
		return new Color((int) Math.round(a.getRed() * (1 - t) + b.getRed() * t), (int) Math.round(a.getGreen() * (1 - t) + b.getGreen() * t),
				(int) Math.round(a.getBlue() * (1 - t) + b.getBlue() * t));
	}

	@Override
	protected void paintComponent(final Graphics g0) {
		final Graphics2D g = (Graphics2D) g0.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			final Color pozadi = UIManager.getColor("Panel.background");
			final Color text = UIManager.getColor("Label.foreground");
			final Font pismo = UIManager.getFont("Label.font");
			final Font tucne = pismo.deriveFont(Font.BOLD);
			final Font zahlavi = tucne.deriveFont(tucne.getSize2D() - 1);
			g.setFont(zahlavi);
			g.setColor(text);
			stred(g, "D \\ T", new Rectangle(0, 0, POPISKY, ZAHLAVI));
			for (int i = 0; i < N; i++) {
				stred(g, hodnota(i), new Rectangle(POPISKY + i * SIRKA, 0, SIRKA, ZAHLAVI));
				final FontMetrics fm = g.getFontMetrics();
				final String h = hodnota(i);
				g.drawString(h, POPISKY - 4 - fm.stringWidth(h), ZAHLAVI + i * VYSKA + (VYSKA + fm.getAscent() - fm.getDescent()) / 2);
			}
			for (int d = 0; d < N; d++) {
				for (int t = 0; t < N; t++) {
					kresliPolicko(g, d, t, pozadi, text, pismo, tucne);
				}
			}
		} finally {
			g.dispose();
		}
	}

	private void kresliPolicko(final Graphics2D g, final int d, final int t, final Color pozadi, final Color text, final Font pismo, final Font tucne) {
		final String s = stitek(d, t);
		final Rectangle r = obdelnik(d, t);
		final Stav st = stav(s);
		final boolean vyb = s.equals(vybrane);
		Color bg;
		Color fg;
		String znak;
		Font f = pismo;
		znak = znak(s);
		switch (st) {
		case SPLNENO:
			bg = smichej(pozadi, text, 0.10);
			fg = smichej(text, pozadi, 0.35);
			break;
		case BEZ_KANDIDATU:
			bg = pozadi;
			fg = smichej(text, pozadi, 0.45);
			break;
		default:
			bg = UIManager.getColor("Button.background");
			fg = text;
			f = tucne;
		}
		if (vyb) {
			bg = MetalLookAndFeel.getPrimaryControl();
			fg = jas(bg) > 0.5 ? new Color(0x202020) : new Color(0xF0F0F0);
			f = tucne;
		}
		g.setColor(bg);
		g.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
		g.setColor(smichej(text, pozadi, 0.7));
		g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
		if (st == Stav.S_KANDIDATY && !vyb) {
			g.setColor(smichej(text, pozadi, 0.45));
			g.drawRect(r.x + 1, r.y + 1, r.width - 3, r.height - 3);
		}
		if (vyb) {
			final Color ram = MetalLookAndFeel.getPrimaryControlDarkShadow();
			g.setColor(ram != null && Math.abs(jas(ram) - jas(bg)) > 0.3 ? ram : text);
			g.setStroke(new BasicStroke(2.5f));
			g.drawRect(r.x + 2, r.y + 2, r.width - 5, r.height - 5);
		}
		if (isFocusOwner() && d == fokusD && t == fokusT) {
			g.setColor(vyb ? fg : text);
			g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 2f, 2f }, 0));
			g.drawRect(r.x + 4, r.y + 4, r.width - 9, r.height - 9);
		}
		g.setStroke(new BasicStroke(1f));
		g.setFont(f);
		g.setColor(fg);
		stred(g, znak, r);
		if (maTecku(s)) {
			g.fillOval(r.x + r.width - 8, r.y + 3, 4, 4);
		}
	}

	private static void stred(final Graphics2D g, final String s, final Rectangle r) {
		final FontMetrics fm = g.getFontMetrics();
		g.drawString(s, r.x + (r.width - fm.stringWidth(s)) / 2, r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2);
	}
}
