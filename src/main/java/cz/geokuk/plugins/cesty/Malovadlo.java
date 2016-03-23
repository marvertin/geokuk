package cz.geokuk.plugins.cesty;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.plugins.cesty.data.*;

public class Malovadlo {
	private final Graphics2D		g;

	private static final int		SIRKA_CARY_VYBRANE	= 6;

	private final Stroke			strokeVzdusny		= new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 3 }, 0);

	private final Doc				doc;

	private final Bousek0			blizkyBousek;

	private final Coord				soord;

	/** Obsahuje bod, který je přidáván, pak se kreslí k němu přidávací čára */
	private final Mou				pridavanyBod1;
	private final Mou				pridavanyBod2;

	private final Mou				mouDeliciNaBlizkemBousku;

	private final Cesta				curta;

	private final Mouable			poziceMouable;

	private static final Polygon	bodovaSipecka		= new Polygon(new int[] { -3, 2, 5, 2, -3 }, new int[] { 3, 3, 0, -3, -3 }, 5);

	private static final Ellipse2D	bodStartocilu		= new Ellipse2D.Float(-10, -10, 20, 20);

	public Malovadlo(final Graphics2D g2, final MalovadloParams params) {
		g = g2;
		doc = params.doc;
		curta = params.curta;
		blizkyBousek = params.blizkyBousek;
		pridavanyBod1 = params.mouPridavanyBod1;
		pridavanyBod2 = params.mouPridavanyBod2;
		soord = params.soord;
		mouDeliciNaBlizkemBousku = params.mouDeliciNaBlizkemBousku;
		poziceMouable = params.poziceMouable;
	}

	public void paint() {
		// long kvadratMaximalniVzdalenosti = getKvadratMaximalniVzdalenosti();

		for (final Cesta cesta : doc.getCesty()) {
			if (cesta == null || cesta.isEmpty()) {
				continue;
			}
			final MalovadloCesty malovadloCesty = new MalovadloCesty(cesta);
			malovadloCesty.paint();
		}
		paintPridavaciUsecka();

	}

	private Graphics2D createGraphics() {
		return (Graphics2D) g.create();

	}

	private void paintPridavaciUsecka() {
		if (pridavanyBod1 == null || pridavanyBod2 == null) {
			return;
		}
		final Point p1 = soord.transform(pridavanyBod1);
		// Point p2 = getSoord().transform(semSePridava.getMou());
		// Point p2 = getSoord().transform(moucur);
		final Point p2 = soord.transform(pridavanyBod2);
		final Graphics2D gg = createGraphics();
		// gg.setColor(FBarvy.USEK_PRIDAVANY);
		gg.translate(p1.x, p1.y);
		gg.rotate(Math.atan2(p2.y - p1.y, p2.x - p1.x));
		final double delka = Math.hypot(p2.x - p1.x, p2.y - p1.y);
		gg.setStroke(new BasicStroke(2));
		gg.drawLine(0, -2, (int) delka, -2);
		gg.drawLine(0, 2, (int) delka, 2);
	}

	class MalovadloCesty {

		private final Cesta		cesta;
		private final boolean	jeCurta;
		private final boolean	jeBlizkyBousekVTetoCeste;

		private final Color		barvaCestyPredKurzorem;
		private final Color		barvaCestyZaKurzorem;

		public MalovadloCesty(final Cesta cesta) {
			this.cesta = cesta;
			jeCurta = cesta == curta;
			jeBlizkyBousekVTetoCeste = blizkyBousek != null && blizkyBousek.getCesta() == cesta;
			if (jeCurta) {
				if (jeBlizkyBousekVTetoCeste) {
					barvaCestyPredKurzorem = FBarvy.CURTA_PRED_KURZOREM;
					barvaCestyZaKurzorem = FBarvy.CURTA_ZA_KURZOREM;
				} else {
					barvaCestyPredKurzorem = FBarvy.CURTA_NORMALNE;
					barvaCestyZaKurzorem = FBarvy.CURTA_NORMALNE;
				}
			} else {
				if (jeBlizkyBousekVTetoCeste) {
					barvaCestyPredKurzorem = FBarvy.TLUMENA_PRED_KURZOREM;
					barvaCestyZaKurzorem = FBarvy.TLUMENA_ZA_KURZOREM;
				} else {
					barvaCestyPredKurzorem = FBarvy.TLUMENA_NORMALNE;
					barvaCestyZaKurzorem = FBarvy.TLUMENA_NORMALNE;
				}
			}

		}

		private void paint() {

			paintUseky();

			paintBody();

			// // průsečíková kolečka
			// for (Usek usek : cesta.getUseky()) {
			// {
			// g.setColor(Color.BLUE);
			// Mou nejblizsiBod = usek.getNejblizsiBodKolmoKUsecce(moucur);
			// if (nejblizsiBod != null) {
			// Point p = getSoord().transform(nejblizsiBod);
			// fillCircle(g, p, 4);
			// }
			// }
			// {
			// g.setColor(Color.PINK);
			// Mou nejblizsiBod = usek.getNejblizsiBodKPrimce(moucur);
			// if (nejblizsiBod != null) {
			// Point p = getSoord().transform(nejblizsiBod);
			// fillCircle(g, p, 2);
			// }
			// }
			// }

		}

		private void paintUseky() {
			// Bousek0 usbodKamNejlepeVlozit =
			// cesta.locateBousekKamNejlepeVlozit(moucur);
			// Bousek0 nejblizsi = cesta.locateNejblizsi(moucur);

			// Vykreslování úseků této cesty
			g.setColor(barvaCestyPredKurzorem);
			for (final Usek usek : cesta.getUseky()) {
				final Point p1 = soord.transform(usek.getBvzad().getMou());
				final Point p2 = soord.transform(usek.getBvpred().getMou());
				g.setStroke(new BasicStroke(SIRKA_CARY_VYBRANE));
				if (jeCurta) {
					g.setStroke(new BasicStroke(SIRKA_CARY_VYBRANE));
				}
				if (usek.isVzdusny()) {
					g.setStroke(strokeVzdusny);
				}
				if (blizkyBousek == usek) {
					// bod musi být na úsečce, ale nějaká zaokrouhlování mohouz působit,
					// že není
					final Mou nejblizsiBod = usek.getNejblizsiBodKPrimce(mouDeliciNaBlizkemBousku);
					final Point p = soord.transform(nejblizsiBod);
					g.drawLine(p1.x, p1.y, p.x, p.y);
					g.setColor(barvaCestyZaKurzorem);
					g.drawLine(p.x, p.y, p2.x, p2.y);
					// a ještě vykreslit zvýraznění
					final Graphics2D gg = createGraphics();
					gg.setColor(FBarvy.USEK_ZVYRAZNENY);
					gg.setStroke(new BasicStroke(1));
					gg.drawLine(p1.x, p1.y, p2.x, p2.y);
				} else {
					if (blizkyBousek == usek.getBousekVzad()) {
						g.setColor(barvaCestyZaKurzorem);
					}
					// Hlavní vykreslování úseku
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
				// if (usek == nejblizsi) {
				// g.setColor(Color.BLACK);
				// g.setStroke(new BasicStroke(1));
				// g.drawLine(p1.x, p1.y, p2.x, p2.y);
				// }
				// if (usek == usbodKamNejlepeVlozit) {
				// g.setColor(Color.WHITE);
				// g.setStroke(new BasicStroke(2));
				// g.drawLine(p1.x, p1.y, p2.x, p2.y);
				// }
			}
		}

		private void paintBody() {
			paintKoncoveBody();
			// vykreslování bodů přes již vykreslené úseky
			final Bod cilovyBodyKruhoveCesty = cesta.isKruh() ? cesta.getCil() : null;
			for (final Bod bod : cesta.getBody()) {
				if (bod == poziceMouable) {
					g.setColor(Color.RED);
				} else {
					g.setColor(barvaCestyPredKurzorem);
				}
				final Point p = soord.transform(bod.getMou());
				if (jeBlizkyBousekVTetoCeste) {
					if (blizkyBousek == bod || blizkyBousek == bod.getUvzad()) {
						g.setColor(barvaCestyZaKurzorem);
					}
				} else {
				}
				// boolean blizkyBod = bod.jeDoKvadratuVzdalenosti(moucur,
				// kvadratMaximalniVzdalenosti);
				// if (blizkyBod) {
				// g.setColor(Color.ORANGE);
				// }
				if (jeCurta && bod != cilovyBodyKruhoveCesty && !bod.isWpt()) {
					final Graphics2D gg = (Graphics2D) g.create();
					gg.translate(p.x, p.y);
					natocVeSmeru(gg, bod);
					// gg.rotate(1);
					gg.fillRect(-4, -4, 8, 8);
					gg.setColor(FBarvy.ZVYRAZNOVAC_BLIZKEHO_BOUSKU);
					gg.setStroke(new BasicStroke(2));
					gg.drawPolygon(bodovaSipecka);
					// gg.drawLineRect(- 3, - 3, 6, 6);
				}
				if (bod == blizkyBousek) {
					final Graphics2D gg = (Graphics2D) g.create();
					gg.setColor(FBarvy.ZVYRAZNOVAC_BLIZKEHO_BOUSKU);
					gg.translate(p.x, p.y);
					natocVeSmeru(gg, bod);
					// if (bod.isWpt()) {
					// // gg.drawLine(- 7, - 7, + 70, + 70);
					// // gg.drawLine( + 7, - 7, - 7, + 7);
					// } else {
					// //gg.fillRect(p.x - 7, p.y - 7, 14, 14);
					// }
					gg.fillPolygon(bodovaSipecka);
				} else {
				}

			}
			// zvýrazňování
			// if (bod == nejblizsi) {
			// g.setColor(Color.BLACK);
			// g.setStroke(new BasicStroke(4));
			// g.drawRect(p.x - 15, p.y - 15, 30, 30);
			// }
			// if (bod == usbodKamNejlepeVlozit) {
			// g.setColor(Color.WHITE);
			// g.setStroke(new BasicStroke(4));
			// g.drawRect(p.x - 18, p.y - 18, 36, 36);
			// }
			// Vykreslení přidáváného bodu pře CTRL
		}

		private void natocVeSmeru(final Graphics2D gg, final Bod bod) {
			final Usek uvpred = bod.getUvpred();
			if (uvpred != null) {
				gg.rotate(uvpred.getUhel() - Math.PI / 2);
			} else {
				final Usek uvzad = bod.getUvzad();
				if (uvzad != null) {
					gg.rotate(uvzad.getUhel() - Math.PI / 2);
				}
			}
		}

		private void paintKoncovyBod(final Bod bod, final Color color) {
			final Graphics2D gg = (Graphics2D) g.create();
			final Point p = soord.transform(bod.getMou());
			gg.translate(p.x, p.y);
			natocVeSmeru(gg, bod);
			gg.scale(2, 2);
			gg.setColor(color);
			gg.fillPolygon(bodovaSipecka);
		}

		private void paintKoncoveBody() {
			if (cesta.isJednobodova()) {
				final Graphics2D gg = (Graphics2D) g.create();
				final Point p = soord.transform(cesta.getStart().getMou());
				gg.translate(p.x, p.y);
				gg.fill(bodStartocilu);
			} else if (cesta.isKruh()) {
				final Graphics2D gg = (Graphics2D) g.create();
				final Point p = soord.transform(cesta.getStart().getMou());
				gg.translate(p.x, p.y);
				gg.fill(bodStartocilu);
			} else { // normální cesta
				final Bod start = cesta.getStart();
				paintKoncovyBod(start, barvaCestyPredKurzorem);
				final Bod cil = cesta.getCil();
				paintKoncovyBod(cil, barvaCestyZaKurzorem);
			}
		}

		// private void fillCircle(Graphics2D g, Point p, int r) {
		// int r2 = 2 * r;
		// g.fillOval(p.x - r, p.y - r, r2, r2);
		// }

	} // MalovadloCesty
}
