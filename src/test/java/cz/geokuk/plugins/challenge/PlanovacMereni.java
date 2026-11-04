package cz.geokuk.plugins.challenge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.swing.SwingUtilities;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

/** Ruční měření plánovače se třemi aktivními challenge na syntetických datech: {@code PlanovacMereni 174000 1300000}. */
public final class PlanovacMereni {

	private PlanovacMereni() {}

	private static long pamet() {
		for (int i = 0; i < 3; i++) {
			System.gc();
		}
		final Runtime r = Runtime.getRuntime();
		return (r.totalMemory() - r.freeMemory()) >> 20;
	}

	public static void main(final String[] args) throws Exception {
		final int vrcholu = 3000;
		final double[] lat = new double[vrcholu];
		final double[] lon = new double[vrcholu];
		final Random rr = new Random(1);
		for (int i = 0; i < vrcholu; i++) {
			final double u = 2 * Math.PI * i / vrcholu;
			final double polomer = 1.0 + 0.15 * Math.sin(9 * u) + 0.03 * rr.nextDouble();
			lat[i] = 49.8 + 1.2 * polomer * Math.sin(u);
			lon[i] = 15.4 + 2.2 * polomer * Math.cos(u);
		}
		final Oblast oblast = new OblastPolygony("mereni", "v1", Collections.singletonList(new Cast(new Okraj(lat, lon), Collections.<Okraj> emptyList())));
		for (final String a : args) {
			final int n = Integer.parseInt(a);
			final Random r = new Random(n);
			final List<VypocetSektoruTest.Kes> kese = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				kese.add(new VypocetSektoruTest.Kes("GC" + Integer.toString(i, 36), 47.2 + r.nextDouble() * 7.8, 6.0 + r.nextDouble() * 9.0, r.nextInt(30) == 0));
			}
			final long pamet0 = pamet();
			final Path adresar = Files.createTempDirectory("planovac-mereni");
			final ChallengeModel model = new ChallengeModel();
			model.inject(new EventFirer() {
				@Override
				public void fire(final Event0<?> event) {}
			});
			final NastaveniChallengeTest.MapoveUloziste u = new NastaveniChallengeTest.MapoveUloziste();
			model.setUloziste(() -> u);
			model.initAfterInject();
			final Challenge[] c = {
					model.ulozVlastni(new Challenge("A", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC)),
					model.ulozVlastni(new Challenge("B", Typ.SEKTORY_360, Stred.souradnice(50.0875, 14.4208), OblastDef.stat("mereni"), ZdrojPravidel.PGC)),
					model.ulozVlastni(new Challenge("C", Typ.SEKTORY_360, Stred.souradnice(49.5, 15.0), OblastDef.stat("mereni"), ZdrojPravidel.STATOR)) };
			SwingUtilities.invokeAndWait(() -> {
				for (final Challenge d : c) {
					model.aktivuj(d);
				}
				model.setZapnuto(true);
			});
			for (int kolo = 1; kolo <= 2; kolo++) {
				final PlanovacChallenge p = new PlanovacChallenge(model, id -> Optional.of(oblast), () -> new Wgs(50.0875, 14.4208), () -> adresar, () -> new Wgs(50, 14), k -> {}, 0);
				final long t0 = System.nanoTime();
				final long[] hotovo = new long[3];
				final VysledekSektoru[] pred = new VysledekSektoru[3];
				for (int i = 0; i < 3; i++) {
					pred[i] = model.vysledek(c[i]);
				}
				p.keseNacteny(kese);
				int zbyva = 3;
				while (zbyva > 0) {
					Thread.sleep(2);
					zbyva = 0;
					for (int i = 0; i < 3; i++) {
						if (hotovo[i] == 0 && model.vysledek(c[i]) != pred[i]) {
							hotovo[i] = (System.nanoTime() - t0) / 1_000_000;
						}
						if (hotovo[i] == 0) {
							zbyva++;
						}
					}
				}
				System.out.println(n + " keší, " + (kolo == 1 ? "z čista" : "ze souborů") + ": výsledky A/B/C po " + hotovo[0] + "/" + hotovo[1] + "/" + hotovo[2] + " ms, paměť navíc "
						+ (pamet() - pamet0) + " MB (včetně dat keší)");
				p.zavri();
				for (final Challenge d : c) {
					SwingUtilities.invokeAndWait(() -> model.deaktivuj(d));
					SwingUtilities.invokeAndWait(() -> model.aktivuj(d));
				}
			}
		}
		System.exit(0);
	}
}
