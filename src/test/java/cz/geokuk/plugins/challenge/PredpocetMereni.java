package cz.geokuk.plugins.challenge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

/**
 * Ruční měření předpočtu na syntetických datech, ne test: {@code java -cp target/classes:target/test-classes:... cz.geokuk.plugins.challenge.PredpocetMereni 174000 1300000}.
 * Keše jsou rovnoměrně v obdélníku Německa (nebo s parametrem {@code uvnitr} v obdélníku okolo oblasti), oblast je nepravidelný obrys o 3000 vrcholech zhruba velikosti ČR.
 */
public final class PredpocetMereni {

	private PredpocetMereni() {}

	private static long vyuzitaPamet() {
		for (int i = 0; i < 3; i++) {
			System.gc();
		}
		final Runtime r = Runtime.getRuntime();
		return (r.totalMemory() - r.freeMemory()) >> 20;
	}

	public static void main(final String[] args) throws Exception {
		final Path adresar = Files.createTempDirectory("predpocet-mereni");
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
		final boolean uvnitr = args.length > 0 && "uvnitr".equals(args[0]);
		for (final String a : args) {
			if ("uvnitr".equals(a)) {
				continue;
			}
			final int n = Integer.parseInt(a);
			final Random r = new Random(n);
			final long pamet0 = vyuzitaPamet();
			final List<VypocetSektoruTest.Kes> kese = new ArrayList<>(n);
			for (int i = 0; i < n; i++) {
				if (uvnitr) {
					kese.add(new VypocetSektoruTest.Kes("GC" + Integer.toString(i, 36), 48.5 + r.nextDouble() * 2.6, 12.9 + r.nextDouble() * 5.0, r.nextInt(30) == 0));
				} else {
					kese.add(new VypocetSektoruTest.Kes("GC" + Integer.toString(i, 36), 47.2 + r.nextDouble() * 7.8, 6.0 + r.nextDouble() * 9.0, r.nextInt(30) == 0));
				}
			}
			final long pametKese = vyuzitaPamet() - pamet0;
			final Predpocet nove = new Predpocet(adresar.resolve("n" + n), "mereni", ZdrojPravidel.PGC, 50.0875, 14.4208, oblast);
			long t = System.nanoTime();
			nove.spust(kese).get();
			final long studeny = (System.nanoTime() - t) / 1_000_000;
			final long pametPo = vyuzitaPamet() - pamet0 - pametKese;
			final Path soubor;
			try (java.util.stream.Stream<Path> s = Files.list(adresar.resolve("n" + n))) {
				soubor = s.filter(x -> x.toString().endsWith(".bin")).findFirst().get();
			}
			final Predpocet znovu = new Predpocet(adresar.resolve("n" + n), "mereni", ZdrojPravidel.PGC, 50.0875, 14.4208, oblast);
			t = System.nanoTime();
			znovu.spust(kese).get();
			final long zUlozeni = (System.nanoTime() - t) / 1_000_000;
			final VypocetSektoruTest.Kes puvodni = kese.get(n / 2);
			kese.set(n / 2, new VypocetSektoruTest.Kes(puvodni.id, puvodni.lat + 0.001, puvodni.lon, puvodni.nalezena));
			final Predpocet po = new Predpocet(adresar.resolve("n" + n), "mereni", ZdrojPravidel.PGC, 50.0875, 14.4208, oblast);
			t = System.nanoTime();
			po.spust(kese).get();
			final long poZmene = (System.nanoTime() - t) / 1_000_000;
			t = System.nanoTime();
			final VysledekSektoru v = VypocetSektoru.spoctiSKlasifikaci(ZdrojPravidel.PGC, 50.0875, 14.4208, po.klasifikator(), Collections.<String> emptySet(), kese, () -> false);
			final long vypocet = (System.nanoTime() - t) / 1_000_000;
			t = System.nanoTime();
			VypocetSektoru.spocti(ZdrojPravidel.PGC, 50.0875, 14.4208, oblast, Collections.<String> emptySet(), kese);
			final long primyVypocet = (System.nanoTime() - t) / 1_000_000;
			System.out.printf("%d keší: studený předpočet %d ms, z uložení %d ms, po změně jedné keše %d ms (přepočteno %d); výpočet sektorů z předpočtu %d ms, přímo %d ms; "
					+ "soubor %d kB; paměť předpočtu %d MB, data keší %d MB; v oblasti nalezeno %d, chybí %d sektorů%n", n, studeny, zUlozeni, poZmene, po.pocetPrepocitanych(), vypocet, primyVypocet, Files.size(soubor) >> 10,
					pametPo, pametKese, v.pocetZapocitanychNalezu(), v.chybejici().size());
			nove.zavri();
			znovu.zavri();
			po.zavri();
		}
	}
}
