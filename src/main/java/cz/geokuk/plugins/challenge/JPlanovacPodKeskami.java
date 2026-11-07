package cz.geokuk.plugins.challenge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import cz.geokuk.core.coord.Coord;

/** Vrstva pod kešemi: prstenec sektorů kolem středu (splněno tenká zelená, chybí silná červená, nejisté šedé čárkované), klín vybraného sektoru a čára k vybranému kandidátovi. */
public class JPlanovacPodKeskami extends JVrstvaPlanovace {

	private static final long serialVersionUID = 1L;

	static final Color SPLNENY = new Color(0x2E8B3A);
	static final Color CHYBI = new Color(0xD32F2F);
	static final Color NEJISTY = new Color(0x707070);
	private static final Color LEM = new Color(255, 255, 255, 200);

	/** Připravené tvary pro jedno měřítko a polohu mapy a jeden podklad; kreslení z nich jen čerpá. */
	private static final class Tvary {
		final TvarPlanovace tvar;
		final Coord coord;
		final List<Path2D> useky = new ArrayList<>();
		final List<GeometrieSektoru.Stav> stavy = new ArrayList<>();
		Path2D klin;
		Point stred;
		Point vybrany;

		Tvary(final TvarPlanovace tvar, final Coord coord) {
			this.tvar = tvar;
			this.coord = coord;
			final double polomer = TvarPlanovace.polomerPrstence(coord);
			final Point p = new Point();
			for (final GeometrieSektoru.Usek u : tvar.prstenec(polomer)) {
				useky.add(cesta(coord, u.body(), p, false));
				stavy.add(u.stav());
			}
			final List<GeometrieSektoru.Bod> k = tvar.klin(polomer);
			if (!k.isEmpty()) {
				klin = cesta(coord, k, p, true);
			}
			stred = PixelyMapy.transform(coord, tvar.stred, new Point());
			if (tvar.vybranyPozice != null) {
				vybrany = PixelyMapy.transform(coord, tvar.vybranyPozice, new Point());
			}
		}
	}

	private transient Tvary tvary;

	@Override
	public void paintComponent(final Graphics aG) {
		final TvarPlanovace t = tvar();
		if (t == null || t.stred == null || !kresliSektory() || !isSoordInitialized()) {
			return;
		}
		final Coord c = getSoord();
		Tvary v = tvary;
		if (v == null || v.tvar != t || !v.coord.equals(c)) {
			v = new Tvary(t, c);
			tvary = v;
		}
		final Graphics2D g = (Graphics2D) aG.create();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			final Color a = AkcentPlanovace.barva();
			if (v.klin != null) {
				g.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 90));
				g.fill(v.klin);
				g.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 200));
				g.setStroke(new BasicStroke(1.2f));
				g.draw(v.klin);
			}
			if (v.vybrany != null && kesoidyViditelne()) {
				g.setColor(LEM);
				g.setStroke(new BasicStroke(3.5f));
				g.drawLine(v.stred.x, v.stred.y, v.vybrany.x, v.vybrany.y);
				g.setColor(a);
				g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[] { 6f, 4f }, 0f));
				g.drawLine(v.stred.x, v.stred.y, v.vybrany.x, v.vybrany.y);
			}
			for (int i = 0; i < v.useky.size(); i++) {
				final GeometrieSektoru.Stav stav = v.stavy.get(i);
				final boolean chybi = stav == GeometrieSektoru.Stav.CHYBI;
				final float sirka = chybi ? 4.5f : 2.2f;
				g.setColor(LEM);
				g.setStroke(new BasicStroke(sirka + 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
				g.draw(v.useky.get(i));
				g.setColor(chybi ? CHYBI : stav == GeometrieSektoru.Stav.NEJISTY ? NEJISTY : SPLNENY);
				g.setStroke(stav == GeometrieSektoru.Stav.NEJISTY ? new BasicStroke(sirka, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[] { 5f, 3f }, 0f)
						: new BasicStroke(sirka, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
				g.draw(v.useky.get(i));
			}
		} finally {
			g.dispose();
		}
	}

	private static Path2D cesta(final Coord c, final List<GeometrieSektoru.Bod> body, final Point p, final boolean uzavrit) {
		final Path2D.Double cesta = new Path2D.Double();
		boolean prvni = true;
		for (final GeometrieSektoru.Bod b : body) {
			PixelyMapy.transform(c, PixelyMapy.mou(b.lat, b.lon), p);
			if (prvni) {
				cesta.moveTo(p.x, p.y);
				prvni = false;
			} else {
				cesta.lineTo(p.x, p.y);
			}
		}
		if (uzavrit && !prvni) {
			cesta.closePath();
		}
		return cesta;
	}
}
