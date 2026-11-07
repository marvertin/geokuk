package cz.geokuk.plugins.challenge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.MouseGestureContext;

/**
 * Vrstva nad kešemi: značka a štítek středu, u okraje mapy šipka ke středu mimo výřez (klik ho vystředí), kroužky a štítky kandidátů (číslo sektoru, D/T) a štítek vybraného sektoru.
 * Štítky se umisťují hladově bez překryvu, nevešlé se vynechají a kroužky zůstanou.
 */
public class JPlanovacNadKeskami extends JVrstvaPlanovace {

	private static final long serialVersionUID = 1L;

	private static final int OKRAJ_SIPKY = 24;
	private static final int POLOMER_KROUZKU = 7;
	private static final int OKRAJ_VYREZU = 12;

	private transient VyrezModel vyrezModel;
	/** Kde se naposledy kreslila šipka ke středu (pro klik), nebo null. */
	private transient volatile Rectangle sipka;
	private transient volatile TvarPlanovace sipkaTvar;

	// pracovní pole kreslení, jen z EDT
	private static final int NEJVIC_PREKAZEK = 16;
	private final transient int[] ox = new int[TvarPlanovace.NEJVIC_ZNACEK + 8 + NEJVIC_PREKAZEK];
	private final transient int[] oy = new int[ox.length];
	private final transient int[] ow = new int[ox.length];
	private final transient int[] oh = new int[ox.length];
	private transient int pocetObdelniku;
	/** Kolik prvních obdélníků jsou překážky, ne štítky. */
	private transient int pocetPrekazek;
	private transient int posledniZnacek;
	private transient java.util.List<Rectangle> prekazkyKresleni = java.util.Collections.emptyList();

	/** Výběr kandidátů ve výřezu pro jeden podklad a jednu polohu mapy; kreslení z něj čte, dokud se nic nezmění. */
	private static final class Vyber {
		final TvarPlanovace tvar;
		final Coord coord;
		int pocet;
		int[] index = new int[0];
		int[] x = new int[0];
		int[] y = new int[0];

		Vyber(final TvarPlanovace tvar, final Coord coord) {
			this.tvar = tvar;
			this.coord = coord;
		}
	}

	private transient Vyber vyber;

	/** Šipka ke středu mimo výřez z posledního kreslení, nebo null. */
	Rectangle sipka() {
		return sipka;
	}

	/** Kolik kroužků kandidátů se při posledním kreslení nakreslilo. */
	int posledniZnacek() {
		return posledniZnacek;
	}

	/** Obdélníky štítků z posledního kreslení. */
	java.util.List<Rectangle> obdelnikyStitku() {
		final java.util.List<Rectangle> l = new java.util.ArrayList<>();
		for (int i = 0; i < pocetObdelniku; i++) {
			l.add(new Rectangle(ox[i], oy[i], ow[i], oh[i]));
		}
		return l;
	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	@Override
	public void paintComponent(final Graphics aG) {
		sipka = null;
		posledniZnacek = 0;
		pocetObdelniku = 0;
		final TvarPlanovace t = tvar();
		if (t == null || !isSoordInitialized()) {
			return;
		}
		final Coord c = getSoord();
		final Graphics2D g = (Graphics2D) aG.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			final Font zaklad = UIManager.getFont("ToolTip.font") != null ? UIManager.getFont("ToolTip.font") : getFont();
			final Font pismo = zaklad.deriveFont(Font.PLAIN, zaklad.getSize2D() - 1f);
			final Font tucne = pismo.deriveFont(Font.BOLD);
			pocetObdelniku = 0;
			prekazkyKresleni = prekazky(c.getWidth(), c.getHeight());
			for (final Rectangle r : prekazkyKresleni) {
				if (pocetObdelniku < NEJVIC_PREKAZEK) {
					zapis(r.x, r.y, r.width, r.height);
				}
			}
			pocetPrekazek = pocetObdelniku;
			kresliStred(g, c, t, tucne);
			if (kresliSektory() && t.vybranyPozice != null && t.popisVybraneho != null && t.stred != null) {
				final Point k = PixelyMapy.transform(c, t.vybranyPozice, new Point());
				umistiOkoloBodu(g, tucne, t.popisVybraneho, k.x, k.y, POLOMER_KROUZKU + 12);
			}
			if (kresliStitky()) {
				kresliKandidaty(g, c, t, pismo);
			}
		} finally {
			g.dispose();
		}
	}

	private void kresliStred(final Graphics2D g, final Coord c, final TvarPlanovace t, final Font tucne) {
		if (t.stred == null) {
			return;
		}
		final Point s = PixelyMapy.transform(c, t.stred, new Point());
		final int w = c.getWidth();
		final int h = c.getHeight();
		final Color akcent = AkcentPlanovace.barva();
		if (s.x >= 0 && s.y >= 0 && s.x < w && s.y < h) {
			final int r = 9;
			g.setColor(Color.WHITE);
			g.fill(new Ellipse2D.Double(s.x - r - 2, s.y - r - 2, 2 * r + 4, 2 * r + 4));
			g.setColor(akcent);
			g.fill(new Ellipse2D.Double(s.x - r, s.y - r, 2 * r, 2 * r));
			g.setColor(Color.WHITE);
			g.setFont(tucne);
			final FontMetrics fm = g.getFontMetrics();
			g.drawString("S", s.x - fm.stringWidth("S") / 2, s.y + (fm.getAscent() - fm.getDescent()) / 2);
			zapis(s.x - r - 3, s.y - r - 3, 2 * r + 6, 2 * r + 6);
			umistiOkoloBodu(g, tucne, TextyKresleni.stred(t.popisStredu), s.x, s.y, r + 4);
			return;
		}
		kresliSipku(g, c, t, tucne, s, akcent);
	}

	/** Šipka u okraje okna ve směru středu a štítek se vzdáleností; klik na ni vystředí mapu. Leží-li na překážce (měřítko, panel), posune se podél okraje. */
	private void kresliSipku(final Graphics2D g, final Coord c, final TvarPlanovace t, final Font tucne, final Point s, final Color akcent) {
		final int w = c.getWidth();
		final int h = c.getHeight();
		double dx = s.x - w / 2.0;
		double dy = s.y - h / 2.0;
		final double d = Math.hypot(dx, dy);
		if (d < 1) {
			return;
		}
		dx /= d;
		dy /= d;
		final double tx = dx == 0 ? Double.MAX_VALUE : Math.abs((w / 2.0 - OKRAJ_SIPKY) / dx);
		final double ty = dy == 0 ? Double.MAX_VALUE : Math.abs((h / 2.0 - OKRAJ_SIPKY) / dy);
		final double k = Math.min(tx, ty);
		final double ang = Math.atan2(dy, dx);
		final Wgs stredMapy = c.getMoustred().toWgs();
		final String text = TextyKresleni.stredMimoVyrez(t.popisStredu, Wgs.vzdalenost(stredMapy, t.stredWgs));
		final Dimension rozmer = rozmerStitku(g, tucne, text);
		// Poloha na obvodu okna: nejdřív ve směru středu, jinak nejbližší volná kolem dokola; když žádná není volná, ta s nejmenším překryvem.
		final double obvodSirka = w - 2.0 * OKRAJ_SIPKY;
		final double obvodVyska = h - 2.0 * OKRAJ_SIPKY;
		final double obvod = 2 * (obvodSirka + obvodVyska);
		final double u0 = parametrObvodu(w / 2.0 + dx * k, h / 2.0 + dy * k, obvodSirka, obvodVyska);
		Path2D.Double nejlepsiSipka = null;
		Rectangle nejlepsiStitek = null;
		double nejmensiPrekryv = Double.MAX_VALUE;
		for (int krok = 0; krok * KROK_OBVODU <= obvod; krok++) {
			final double u = u0 + (krok % 2 == 0 ? 1 : -1) * ((krok + 1) / 2) * KROK_OBVODU;
			final double[] bod = bodObvodu(((u % obvod) + obvod) % obvod, obvodSirka, obvodVyska);
			final double x = bod[0];
			final double y = bod[1];
			double ix = w / 2.0 - x;
			double iy = h / 2.0 - y;
			final double il = Math.hypot(ix, iy);
			ix /= il;
			iy /= il;
			final Path2D.Double tr = trojuhelnik(x, y, ang);
			final double lx = Math.max(6, Math.min(w - rozmer.width - 6, x + ix * (SIPKA_DELKA + rozmer.width / 2.0 + 4) - rozmer.width / 2.0));
			final double ly = Math.max(6, Math.min(h - rozmer.height - 6, y + iy * (SIPKA_DELKA + rozmer.height / 2.0 + 4) - rozmer.height / 2.0));
			final Rectangle st = new Rectangle((int) lx, (int) ly, rozmer.width, rozmer.height);
			final double prekryv = prekryvSPrekazkami(tr.getBounds()) + prekryvSPrekazkami(st);
			if (prekryv < nejmensiPrekryv) {
				nejmensiPrekryv = prekryv;
				nejlepsiSipka = tr;
				nejlepsiStitek = st;
				if (prekryv == 0) {
					break;
				}
			}
		}
		final Path2D.Double p = nejlepsiSipka;
		final Rectangle stitek = nejlepsiStitek;
		g.setColor(Color.WHITE);
		g.setStroke(new BasicStroke(3f));
		g.draw(p);
		g.setColor(akcent);
		g.fill(p);
		nakresliStitek(g, tucne, text, stitek.x, stitek.y, akcent);
		sipka = stitek.union(p.getBounds());
		sipkaTvar = t;
	}

	/** Krok, kterým se hledá volná poloha šipky po obvodu okna. */
	private static final double KROK_OBVODU = 12;

	/** Vzdálenost po obvodu obdélníku (od levého horního rohu po směru hodin) k bodu na obvodu. */
	private static double parametrObvodu(final double x, final double y, final double sirka, final double vyska) {
		final double lx = x - OKRAJ_SIPKY;
		final double ly = y - OKRAJ_SIPKY;
		final double d = Math.min(Math.min(Math.abs(ly), Math.abs(ly - vyska)), Math.min(Math.abs(lx), Math.abs(lx - sirka)));
		if (d == Math.abs(ly)) {
			return Math.max(0, Math.min(sirka, lx));
		}
		if (d == Math.abs(lx - sirka)) {
			return sirka + Math.max(0, Math.min(vyska, ly));
		}
		if (d == Math.abs(ly - vyska)) {
			return sirka + vyska + sirka - Math.max(0, Math.min(sirka, lx));
		}
		return 2 * sirka + vyska + vyska - Math.max(0, Math.min(vyska, ly));
	}

	/** Bod na obvodu obdélníku daný vzdáleností od levého horního rohu po směru hodin. */
	private static double[] bodObvodu(final double u, final double sirka, final double vyska) {
		if (u < sirka) {
			return new double[] { OKRAJ_SIPKY + u, OKRAJ_SIPKY };
		}
		if (u < sirka + vyska) {
			return new double[] { OKRAJ_SIPKY + sirka, OKRAJ_SIPKY + u - sirka };
		}
		if (u < 2 * sirka + vyska) {
			return new double[] { OKRAJ_SIPKY + sirka - (u - sirka - vyska), OKRAJ_SIPKY + vyska };
		}
		return new double[] { OKRAJ_SIPKY, OKRAJ_SIPKY + vyska - (u - 2 * sirka - vyska) };
	}

	/** Součet ploch, kterými obdélník zasahuje do překážek. */
	private double prekryvSPrekazkami(final Rectangle r) {
		double suma = 0;
		for (final Rectangle p : prekazkyKresleni) {
			final Rectangle i = p.intersection(r);
			if (!i.isEmpty()) {
				suma += (double) i.width * i.height;
			}
		}
		return suma;
	}

	private static final double SIPKA_DELKA = 18;

	private static Path2D.Double trojuhelnik(final double x, final double y, final double ang) {
		final Path2D.Double p = new Path2D.Double();
		p.moveTo(x + SIPKA_DELKA * Math.cos(ang), y + SIPKA_DELKA * Math.sin(ang));
		p.lineTo(x + SIPKA_DELKA * 0.7 * Math.cos(ang + 2.5), y + SIPKA_DELKA * 0.7 * Math.sin(ang + 2.5));
		p.lineTo(x + SIPKA_DELKA * 0.7 * Math.cos(ang - 2.5), y + SIPKA_DELKA * 0.7 * Math.sin(ang - 2.5));
		p.closePath();
		return p;
	}

	private boolean kryjeVNekterou(final Rectangle r) {
		for (final Rectangle p : prekazkyKresleni) {
			if (p.intersects(r)) {
				return true;
			}
		}
		return false;
	}

	/** Výběr nejvýš {@link TvarPlanovace#NEJVIC_ZNACEK} kandidátů ve výřezu nejblíž středu okna; přepočítá se jen při změně podkladu nebo mapy. */
	private Vyber vyber(final Coord c, final TvarPlanovace t) {
		Vyber v = vyber;
		if (v != null && v.tvar == t && v.coord.equals(c)) {
			return v;
		}
		v = new Vyber(t, c);
		final int w = c.getWidth();
		final int h = c.getHeight();
		int n = 0;
		int[] idx = new int[256];
		int[] xs = new int[256];
		int[] ys = new int[256];
		double[] vz = new double[256];
		final Point p = new Point();
		for (int i = 0; i < t.pozice.length; i++) {
			PixelyMapy.transform(c, t.pozice[i], p);
			if (p.x < -OKRAJ_VYREZU || p.y < -OKRAJ_VYREZU || p.x > w + OKRAJ_VYREZU || p.y > h + OKRAJ_VYREZU) {
				continue;
			}
			if (n == idx.length) {
				idx = Arrays.copyOf(idx, n * 2);
				xs = Arrays.copyOf(xs, n * 2);
				ys = Arrays.copyOf(ys, n * 2);
				vz = Arrays.copyOf(vz, n * 2);
			}
			idx[n] = i;
			xs[n] = p.x;
			ys[n] = p.y;
			final double dx = p.x - w / 2.0;
			final double dy = p.y - h / 2.0;
			vz[n] = dx * dx + dy * dy;
			n++;
		}
		final int kolik = Math.min(n, TvarPlanovace.NEJVIC_ZNACEK);
		v.index = new int[kolik];
		v.x = new int[kolik];
		v.y = new int[kolik];
		if (kolik > 0) {
			final double[] serazene = Arrays.copyOf(vz, n);
			Arrays.sort(serazene);
			final double prah = serazene[kolik - 1];
			int m = 0;
			final int[] poradi = new int[kolik];
			for (int i = 0; i < n && m < kolik; i++) {
				if (vz[i] <= prah) {
					int j = m++;
					while (j > 0 && vz[poradi[j - 1]] > vz[i]) {
						poradi[j] = poradi[j - 1];
						j--;
					}
					poradi[j] = i;
				}
			}
			for (int q = 0; q < m; q++) {
				v.index[q] = idx[poradi[q]];
				v.x[q] = xs[poradi[q]];
				v.y[q] = ys[poradi[q]];
			}
			v.pocet = m;
		}
		vyber = v;
		return v;
	}

	private void kresliKandidaty(final Graphics2D g, final Coord c, final TvarPlanovace t, final Font pismo) {
		final Vyber v = vyber(c, t);
		final Color akcent = AkcentPlanovace.barva();
		final int r = POLOMER_KROUZKU;
		final Rectangle krouzek = new Rectangle();
		final boolean[] zobrazit = new boolean[v.pocet];
		int kresleno = 0;
		for (int q = 0; q < v.pocet; q++) {
			krouzek.setBounds(v.x[q] - r - 2, v.y[q] - r - 2, 2 * r + 4, 2 * r + 4);
			if (kryjeVNekterou(krouzek)) {
				continue;
			}
			zobrazit[q] = true;
			kresleno++;
			g.setColor(Color.WHITE);
			g.setStroke(new BasicStroke(3.6f));
			g.drawOval(v.x[q] - r, v.y[q] - r, 2 * r, 2 * r);
			g.setColor(akcent);
			g.setStroke(new BasicStroke(2.2f));
			g.drawOval(v.x[q] - r, v.y[q] - r, 2 * r, 2 * r);
		}
		posledniZnacek = kresleno;
		for (int q = 0; q < v.pocet; q++) {
			if (zobrazit[q]) {
				umistiOkoloBodu(g, pismo, t.texty[v.index[q]], v.x[q], v.y[q], POLOMER_KROUZKU + 3);
			}
		}
	}

	private Dimension rozmerStitku(final Graphics2D g, final Font f, final String text) {
		final FontMetrics fm = g.getFontMetrics(f);
		return new Dimension(fm.stringWidth(text) + 8, fm.getHeight() + 2);
	}

	/** Zkusí osm poloh kolem bodu a vybere první bez překryvu; jinak štítek vynechá a vrátí false. */
	private boolean umistiOkoloBodu(final Graphics2D g, final Font f, final String text, final int cx, final int cy, final int od) {
		final Dimension d = rozmerStitku(g, f, text);
		final int[][] polohy = { { od, -d.height / 2 }, { -d.width - od, -d.height / 2 }, { -d.width / 2, -od - d.height }, { -d.width / 2, od }, { od, -d.height }, { od, 0 },
				{ -d.width - od, -d.height }, { -d.width - od, 0 } };
		for (final int[] p : polohy) {
			if (pocetObdelniku < ox.length && !kryje(cx + p[0], cy + p[1], d.width, d.height)) {
				nakresliStitek(g, f, text, cx + p[0], cy + p[1], AkcentPlanovace.barva());
				zapis(cx + p[0], cy + p[1], d.width, d.height);
				return true;
			}
		}
		return false;
	}

	private boolean kryje(final int x, final int y, final int w, final int h) {
		for (int i = 0; i < pocetObdelniku; i++) {
			if (x < ox[i] + ow[i] && x + w > ox[i] && y < oy[i] + oh[i] && y + h > oy[i]) {
				return true;
			}
		}
		return false;
	}

	private void zapis(final int x, final int y, final int w, final int h) {
		if (pocetObdelniku < ox.length) {
			ox[pocetObdelniku] = x;
			oy[pocetObdelniku] = y;
			ow[pocetObdelniku] = w;
			oh[pocetObdelniku] = h;
			pocetObdelniku++;
		}
	}

	private Rectangle nakresliStitek(final Graphics2D g, final Font f, final String text, final int x, final int y, final Color akcent) {
		g.setFont(f);
		final FontMetrics fm = g.getFontMetrics();
		final int w = fm.stringWidth(text) + 8;
		final int h = fm.getHeight() + 2;
		final RoundRectangle2D.Double tvar = new RoundRectangle2D.Double(x, y, w, h, 4, 4);
		g.setColor(AkcentPlanovace.pozadiStitku());
		g.fill(tvar);
		g.setColor(new Color(255, 255, 255, 190));
		g.setStroke(new BasicStroke(3f));
		g.draw(tvar);
		g.setColor(akcent);
		g.setStroke(new BasicStroke(1.4f));
		g.draw(tvar);
		g.setColor(AkcentPlanovace.textStitku());
		g.drawString(text, x + 4, y + 1 + fm.getAscent());
		return new Rectangle(x, y, w, h);
	}

	@Override
	public void mouseClicked(final MouseEvent e, final MouseGestureContext ctx) {
		final Rectangle s = sipka;
		final TvarPlanovace t = sipkaTvar;
		if (s != null && t != null && t.stred != null && vyrezModel != null && SwingUtilities.isLeftMouseButton(e) && s.contains(e.getPoint())) {
			vyrezModel.presunMapuNaMoustred(t.stred);
			return;
		}
		super.mouseClicked(e, ctx);
	}
}
