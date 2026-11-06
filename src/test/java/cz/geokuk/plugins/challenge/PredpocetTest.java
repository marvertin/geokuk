package cz.geokuk.plugins.challenge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;
import cz.geokuk.plugins.challenge.SouborPredpoctu.Tabulka;
import cz.geokuk.plugins.challenge.VypocetSektoru.Klasifikator;

public class PredpocetTest {

	private static final double LAT = 50.0875;
	private static final double LON = 14.4208;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private Path adresar;
	private Oblast oblast;
	private final List<Predpocet> otevrene = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		adresar = tmp.newFolder("predpocet").toPath();
		oblast = new OblastPolygony("o", "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(49.5, 13.5, 50.7, 15.3), Collections.<Okraj> emptyList())));
	}

	private Predpocet predpocet(final String zdroj, final Oblast o, final ZdrojPravidel pravidla, final double lat) {
		final Predpocet p = new Predpocet(adresar, zdroj, pravidla, lat, LON, o);
		otevrene.add(p);
		return p;
	}

	@org.junit.After
	public void zavri() {
		for (final Predpocet p : otevrene) {
			p.zavri();
		}
	}

	private static List<VypocetSektoruTest.Kes> kese(final int pocet, final long seed) {
		final Random r = new Random(seed);
		final List<VypocetSektoruTest.Kes> v = new ArrayList<>();
		for (int i = 0; i < pocet; i++) {
			v.add(new VypocetSektoruTest.Kes("GC" + i, 49.0 + r.nextDouble() * 2.2, 13.0 + r.nextDouble() * 3.0, r.nextInt(3) == 0));
		}
		return v;
	}

	private static void pockej(final Predpocet p, final java.util.concurrent.Future<?> f) throws Exception {
		f.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(StavPredpoctu.Faze.HOTOVO, p.stav().faze());
	}

	private Path jedinySoubor() throws IOException {
		try (java.util.stream.Stream<Path> s = Files.list(adresar)) {
			final List<Path> v = new ArrayList<>();
			s.filter(x -> x.getFileName().toString().startsWith("challenge-") && x.toString().endsWith(".bin")).forEach(v::add);
			Assert.assertEquals(v.toString(), 1, v.size());
			return v.get(0);
		}
	}

	@Test
	public void predpocitaneVysledkySeRovnajiPrimemuVypoctu() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(3000, 5);
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p, p.spust(kese));
		Assert.assertEquals(3000, p.pocetPrepocitanych());
		final Klasifikator primy = VypocetSektoru.primy(ZdrojPravidel.PGC, LAT, LON, oblast);
		for (final VypocetSektoruTest.Kes k : kese) {
			Assert.assertEquals(k.id, primy.klasifikuj(k), p.klasifikator().klasifikuj(k));
		}
		final VysledekSektoru a = VypocetSektoru.spoctiSKlasifikaci(ZdrojPravidel.PGC, LAT, LON, primy, Collections.<String> emptySet(), kese, () -> false);
		final VysledekSektoru b = VypocetSektoru.spoctiSKlasifikaci(ZdrojPravidel.PGC, LAT, LON, p.klasifikator(), Collections.<String> emptySet(), kese, () -> false);
		Assert.assertEquals(a.chybejici(), b.chybejici());
		for (int s = 0; s < 360; s++) {
			Assert.assertEquals(a.jeNejisty(s), b.jeNejisty(s));
		}
	}

	@Test
	public void vysledekZPrimehoVypoctuSePoOblastiShodujeSPuvodnimRozhranim() {
		final List<VypocetSektoruTest.Kes> kese = kese(2000, 8);
		final VysledekSektoru puvodni = VypocetSektoru.spocti(ZdrojPravidel.STATOR, LAT, LON, oblast, Collections.<String> emptySet(), kese);
		final VysledekSektoru sKlasifikaci = VypocetSektoru.spoctiSKlasifikaci(ZdrojPravidel.STATOR, LAT, LON, VypocetSektoru.primy(ZdrojPravidel.STATOR, LAT, LON, oblast),
				Collections.<String> emptySet(), kese, () -> false);
		Assert.assertEquals(puvodni.chybejici(), sKlasifikaci.chybejici());
		Assert.assertEquals(puvodni.pocetZapocitanychNalezu(), sKlasifikaci.pocetZapocitanychNalezu());
	}

	@Test
	public void souborPredpoctuMaVerziVypoctu2ASouborZeStarsiVerzeSeNenacte() throws Exception {
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p, p.spust(kese(50, 9)));
		final Path soubor = jedinySoubor();
		final String zbytek = "|z1|" + oblast.verze() + "|" + String.format(java.util.Locale.ROOT, "%.7f,%.7f", LAT, LON) + "|PGC";
		Assert.assertNotNull("aktuální verze výpočtu", SouborPredpoctu.nacti(soubor, "GKPC2" + zbytek));
		Assert.assertNull("předchozí verze výpočtu", SouborPredpoctu.nacti(soubor, "GKPC1" + zbytek));
	}

	@Test
	public void podruheseNicNepocitaAPoZmeneJedneKesePocitaJenJi() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(2500, 6);
		final Predpocet p1 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p1, p1.spust(kese));
		final Path soubor = jedinySoubor();
		final long velikost = Files.size(soubor);

		final Predpocet p2 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p2, p2.spust(kese));
		Assert.assertEquals("vše z uložení", 0, p2.pocetPrepocitanych());

		final List<VypocetSektoruTest.Kes> zmenene = new ArrayList<>(kese);
		final VypocetSektoruTest.Kes puvodni = zmenene.get(10);
		zmenene.set(10, new VypocetSektoruTest.Kes(puvodni.id, puvodni.lat + 0.01, puvodni.lon, puvodni.nalezena));
		final Predpocet p3 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p3, p3.spust(zmenene));
		Assert.assertEquals("jen změněná keš", 1, p3.pocetPrepocitanych());

		zmenene.add(new VypocetSektoruTest.Kes("GCNOVA", 50.0, 14.0, true));
		final Predpocet p4 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p4, p4.spust(zmenene));
		Assert.assertEquals("jen nová keš", 1, p4.pocetPrepocitanych());
		Assert.assertTrue("soubor s novou keší je větší", Files.size(soubor) > velikost);

		zmenene.remove(zmenene.size() - 1);
		final Predpocet p5 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p5, p5.spust(zmenene));
		Assert.assertEquals(0, p5.pocetPrepocitanych());
		Assert.assertEquals("odebraná keš ze souboru zmizela", velikost, Files.size(soubor));
	}

	@Test
	public void jinyKlicPocitaVseASoubory() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(500, 7);
		final Predpocet p1 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p1, p1.spust(kese));
		final Predpocet jinyZdroj = predpocet("z2", oblast, ZdrojPravidel.PGC, LAT);
		pockej(jinyZdroj, jinyZdroj.spust(kese));
		Assert.assertEquals(500, jinyZdroj.pocetPrepocitanych());
		final Predpocet jinyStred = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT + 0.5);
		pockej(jinyStred, jinyStred.spust(kese));
		Assert.assertEquals(500, jinyStred.pocetPrepocitanych());
		final Predpocet jinaPravidla = predpocet("z1", oblast, ZdrojPravidel.STATOR, LAT);
		pockej(jinaPravidla, jinaPravidla.spust(kese));
		Assert.assertEquals(500, jinaPravidla.pocetPrepocitanych());
		final Oblast jina = new OblastPolygony("o", "v2", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(49.5, 13.5, 50.7, 15.3), Collections.<Okraj> emptyList())));
		final Predpocet jinaVerze = predpocet("z1", jina, ZdrojPravidel.PGC, LAT);
		pockej(jinaVerze, jinaVerze.spust(kese));
		Assert.assertEquals("jiná verze oblasti", 500, jinaVerze.pocetPrepocitanych());
		try (java.util.stream.Stream<Path> s = Files.list(adresar)) {
			Assert.assertEquals("pět různých klíčů, pět souborů", 5, s.filter(x -> x.toString().endsWith(".bin")).count());
		}
		final Predpocet znovu = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(znovu, znovu.spust(kese));
		Assert.assertEquals(0, znovu.pocetPrepocitanych());
	}

	@Test
	public void poskozenyNeboCiziSouborSeZahodiAPocitaSeZnovu() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(800, 9);
		final Predpocet p1 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p1, p1.spust(kese));
		final Path soubor = jedinySoubor();
		final byte[] dobry = Files.readAllBytes(soubor);

		final byte[] zlomeny = dobry.clone();
		zlomeny[zlomeny.length / 2] ^= 0x55;
		Files.write(soubor, zlomeny);
		Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p, p.spust(kese));
		Assert.assertEquals("po změně bajtu platí CRC jen u nového souboru", 800, p.pocetPrepocitanych());
		Assert.assertArrayEquals("opravený soubor je zase platný", dobry, Files.readAllBytes(soubor));

		Files.write(soubor, java.util.Arrays.copyOf(dobry, dobry.length - 20));
		p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p, p.spust(kese));
		Assert.assertEquals("useknutý soubor", 800, p.pocetPrepocitanych());

		Files.write(soubor, "nesmysl".getBytes(java.nio.charset.StandardCharsets.UTF_8));
		p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p, p.spust(kese));
		Assert.assertEquals("cizí obsah", 800, p.pocetPrepocitanych());

		Files.write(soubor, new byte[0]);
		p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p, p.spust(kese));
		Assert.assertEquals("prázdný soubor", 800, p.pocetPrepocitanych());
		Assert.assertArrayEquals(dobry, Files.readAllBytes(soubor));
	}

	@Test
	public void souborSJinymKlicemSeNenacte() throws Exception {
		final Tabulka t = new Tabulka(10);
		t.vloz(SouborPredpoctu.otisk("GC1"), 1, 2, (short) 3);
		final Path soubor = adresar.resolve("x.bin");
		SouborPredpoctu.zapis(soubor, "klic-a", t);
		Assert.assertNotNull(SouborPredpoctu.nacti(soubor, "klic-a"));
		Assert.assertNull(SouborPredpoctu.nacti(soubor, "klic-b"));
		Assert.assertEquals(3, SouborPredpoctu.nacti(soubor, "klic-a").najdi(SouborPredpoctu.otisk("GC1"), 1, 2));
		Assert.assertEquals("jiné souřadnice", Tabulka.NENI, SouborPredpoctu.nacti(soubor, "klic-a").najdi(SouborPredpoctu.otisk("GC1"), 1, 3));
		Assert.assertEquals("jiná keš", Tabulka.NENI, SouborPredpoctu.nacti(soubor, "klic-a").najdi(SouborPredpoctu.otisk("GC2"), 1, 2));
	}

	@Test
	public void tabulkaSeZvetsiAZachovaZaznamy() {
		final Tabulka t = new Tabulka(1);
		for (int i = 1; i <= 5000; i++) {
			t.vloz(SouborPredpoctu.otisk("GC" + i), i, -i, (short) (i % 3000));
		}
		Assert.assertEquals(5000, t.pocet());
		for (int i = 1; i <= 5000; i++) {
			Assert.assertEquals(i % 3000, t.najdi(SouborPredpoctu.otisk("GC" + i), i, -i));
		}
	}

	@Test
	public void stavyAPrubehSeHlasiPosluchaci() throws Exception {
		final List<StavPredpoctu> stavy = new CopyOnWriteArrayList<>();
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		Assert.assertEquals(StavPredpoctu.Faze.NEBEZI, p.stav().faze());
		p.pridejPosluchace(stavy::add);
		pockej(p, p.spust(kese(20_000, 3)));
		Assert.assertEquals(StavPredpoctu.Faze.POCITA, stavy.get(0).faze());
		Assert.assertEquals(20_000, stavy.get(0).celkem());
		Assert.assertEquals(StavPredpoctu.Faze.HOTOVO, stavy.get(stavy.size() - 1).faze());
		Assert.assertEquals(1.0, p.stav().podil(), 0);
		long predchozi = -1;
		boolean prostredek = false;
		for (final StavPredpoctu s : stavy) {
			Assert.assertTrue(s.hotovo() >= predchozi);
			predchozi = s.hotovo();
			prostredek |= s.faze() == StavPredpoctu.Faze.POCITA && s.hotovo() > 0 && s.hotovo() < 20_000;
		}
		Assert.assertTrue("průběžné stavy", prostredek);
	}

	@Test
	public void vyjimkaVPosluchaciPredpocetNepreruší() throws Exception {
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		p.pridejPosluchace(s -> {
			throw new IllegalStateException("posluchač");
		});
		pockej(p, p.spust(kese(300, 1)));
	}

	@Test
	public void zruseniZastaviPredpocetANeulozi() throws Exception {
		final CountDownLatch zacal = new CountDownLatch(1);
		final CountDownLatch pustit = new CountDownLatch(1);
		final List<VypocetSektoruTest.Kes> kese = kese(50_000, 2);
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		p.pridejPosluchace(s -> {
			if (s.faze() == StavPredpoctu.Faze.POCITA && s.hotovo() > 0) {
				zacal.countDown();
				try {
					pustit.await(10, TimeUnit.SECONDS);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		final java.util.concurrent.Future<?> f = p.spust(kese);
		Assert.assertTrue(zacal.await(30, TimeUnit.SECONDS));
		p.zrus();
		pustit.countDown();
		f.get(30, TimeUnit.SECONDS);
		Assert.assertEquals(StavPredpoctu.Faze.ZRUSENO, p.stav().faze());
		try (java.util.stream.Stream<Path> s = Files.list(adresar)) {
			Assert.assertEquals("nic se neuložilo", 0, s.count());
		}
	}

	@Test
	public void novySpustZrusiBeziciAPoctyKlasifikatoruSeNepletou() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(30_000, 4);
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		final java.util.concurrent.Future<?> prvni = p.spust(kese);
		final java.util.concurrent.Future<?> druhy = p.spust(kese);
		druhy.get(60, TimeUnit.SECONDS);
		Assert.assertTrue(prvni.isDone());
		Assert.assertEquals(StavPredpoctu.Faze.HOTOVO, p.stav().faze());
	}

	@Test
	public void chybaVeVypoctuJeStavChyba() throws Exception {
		final Oblast vadna = new Oblast() {
			@Override
			public boolean obsahuje(final double lat, final double lon) {
				throw new IllegalStateException("oblast je rozbitá");
			}

			@Override
			public String verze() {
				return "vadna";
			}
		};
		final Predpocet p = predpocet("z1", vadna, ZdrojPravidel.PGC, LAT);
		p.spust(kese(10, 1)).get(30, TimeUnit.SECONDS);
		Assert.assertEquals(StavPredpoctu.Faze.CHYBA, p.stav().faze());
		Assert.assertTrue(p.stav().chyba(), p.stav().chyba().contains("rozbitá"));
		Assert.assertTrue("klasifikátor dál počítá přímo", p.klasifikator() != null);
	}

	@Test
	public void ulozeniNaNedostupnemMisteNerozbijeVypocet() throws Exception {
		final Path soubor = tmp.newFile("neni-adresar").toPath();
		final Predpocet p = new Predpocet(soubor.resolve("podadresar"), "z1", ZdrojPravidel.PGC, LAT, LON, oblast);
		otevrene.add(p);
		pockej(p, p.spust(kese(100, 1)));
		Assert.assertEquals(100, p.pocetPrepocitanych());
	}

	@Test
	public void keseBezPredpoctuSeSpocitaPriDotazu() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(100, 1);
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p, p.spust(kese));
		final VypocetSektoruTest.Kes nova = new VypocetSektoruTest.Kes("GCNEW", 50.2, 14.9, true);
		Assert.assertEquals(VypocetSektoru.primy(ZdrojPravidel.PGC, LAT, LON, oblast).klasifikuj(nova), p.klasifikator().klasifikuj(nova));
	}

	@Test(expected = IllegalArgumentException.class)
	public void neplatnyStredJeChyba() {
		new Predpocet(adresar, "z", ZdrojPravidel.PGC, 95, 14, null);
	}

	@Test
	public void bezOblastiPredpocetFunguje() throws Exception {
		final Predpocet p = predpocet("z1", null, ZdrojPravidel.STATOR, LAT);
		final List<VypocetSektoruTest.Kes> kese = kese(300, 11);
		pockej(p, p.spust(kese));
		final AtomicInteger vOblasti = new AtomicInteger();
		for (final VypocetSektoruTest.Kes k : kese) {
			if (p.klasifikator().klasifikuj(k).jeVOblasti()) {
				vOblasti.incrementAndGet();
			}
		}
		Assert.assertEquals("bez omezení jsou všechny v oblasti", 300, vOblasti.get());
	}

	private static final long STARE = 1_000_000L;

	private void nastavStari(final Path soubor, final int poradi) throws IOException {
		Files.setLastModifiedTime(soubor, java.nio.file.attribute.FileTime.fromMillis(System.currentTimeMillis() - (100 - poradi) * STARE));
	}

	private long vlastniSoubory() throws IOException {
		try (java.util.stream.Stream<Path> s = Files.list(adresar)) {
			return s.filter(x -> SouborPredpoctu.JMENO.matcher(x.getFileName().toString()).matches() && Files.isRegularFile(x) && SouborPredpoctu.maZnacku(x)).count();
		}
	}

	@Test
	public void uklidMazeJenVlastniNejstarsiSouboryACizinecNechaBeze() throws Exception {
		final Path cizi1 = adresar.resolve("challenge-moje.bin");
		final Path cizi2 = adresar.resolve("challenge-0123abcd-5.bin");
		final Path cizi3 = adresar.resolve("challenge-0123abcd-6.txt");
		final Path adresarSPodobnymJmenem = adresar.resolve("challenge-aaaaaaaa-1.bin");
		Files.write(cizi1, "moje poznámky".getBytes(StandardCharsets.UTF_8));
		Files.write(cizi2, "tohle není předpočet".getBytes(StandardCharsets.UTF_8));
		Files.write(cizi3, "text".getBytes(StandardCharsets.UTF_8));
		Files.createDirectories(adresarSPodobnymJmenem);
		Files.write(adresarSPodobnymJmenem.resolve("uvnitr"), new byte[] { 1 });
		final Path zbylyDocasny = adresar.resolve("challenge-0123abcd-9.bin.new");
		Files.write(zbylyDocasny, new byte[] { 1, 2, 3 });
		for (final Path p : new Path[] { cizi1, cizi2, cizi3 }) {
			nastavStari(p, 0);
		}
		final List<VypocetSektoruTest.Kes> kese = kese(50, 1);
		final List<Path> vlastni = new ArrayList<>();
		for (int i = 0; i < 11; i++) {
			final Predpocet p = predpocet("z" + i, oblast, ZdrojPravidel.PGC, LAT);
			pockej(p, p.spust(kese));
			try (java.util.stream.Stream<Path> s = Files.list(adresar)) {
				s.filter(x -> SouborPredpoctu.JMENO.matcher(x.getFileName().toString()).matches() && Files.isRegularFile(x) && !vlastni.contains(x) && !x.equals(cizi2)).forEach(vlastni::add);
			}
			nastavStari(vlastni.get(vlastni.size() - 1), i + 1);
		}
		Assert.assertEquals("nejvýš osm vlastních souborů", 8, vlastniSoubory());
		Assert.assertFalse("nejstarší vlastní zmizel", Files.exists(vlastni.get(0)));
		Assert.assertTrue("nejnovější zůstal", Files.exists(vlastni.get(vlastni.size() - 1)));
		Assert.assertTrue(Files.exists(cizi1));
		Assert.assertEquals("moje poznámky", new String(Files.readAllBytes(cizi1), StandardCharsets.UTF_8));
		Assert.assertTrue("podobné jméno bez značky zůstane", Files.exists(cizi2));
		Assert.assertTrue(Files.exists(cizi3));
		Assert.assertTrue("složka se jménem předpočtu zůstane", Files.isDirectory(adresarSPodobnymJmenem));
		Assert.assertTrue(Files.exists(adresarSPodobnymJmenem.resolve("uvnitr")));
		Assert.assertFalse("zbylý dočasný soubor se uklidí", Files.exists(zbylyDocasny));
	}

	@Test
	public void uklidNicNemazeKdyzZapisSelze() throws Exception {
		// Do složky nejde zapsat (je to soubor), vlastní soubory jinde zůstávají; předpočet dobře skončí.
		final Path soubor = tmp.newFile("soubor-misto-slozky").toPath();
		final Predpocet p = new Predpocet(soubor, "z1", ZdrojPravidel.PGC, LAT, LON, oblast);
		otevrene.add(p);
		pockej(p, p.spust(kese(20, 1)));
		Assert.assertTrue(Files.isRegularFile(soubor));
	}

	@Test
	public void hlavickaSObrimPoctemZaznamuSeZahodiBezAlokace() throws Exception {
		final String klic = "klic-test";
		final Path soubor = adresar.resolve("challenge-00000000-1.bin");
		for (final int pocet : new int[] { 50_000_000, Integer.MAX_VALUE, 20_000_001, 1_000_000 }) {
			try (java.io.DataOutputStream out = new java.io.DataOutputStream(Files.newOutputStream(soubor))) {
				out.writeInt(0x474B5043);
				out.writeInt(1);
				out.writeUTF(klic);
				out.writeInt(pocet);
				out.write(new byte[100]);
			}
			Assert.assertNull("počet " + pocet, SouborPredpoctu.nacti(soubor, klic));
		}
		try (java.io.DataOutputStream out = new java.io.DataOutputStream(Files.newOutputStream(soubor))) {
			out.writeInt(0x474B5043);
			out.writeInt(1);
			out.writeUTF(klic);
			out.writeInt(-5);
		}
		Assert.assertNull(SouborPredpoctu.nacti(soubor, klic));
	}

	@Test
	public void souborSPrebyvajicimiBajtyNeniPlatny() throws Exception {
		final Tabulka t = new Tabulka(10);
		t.vloz(SouborPredpoctu.otisk("GC1"), 1, 2, (short) 3);
		final Path soubor = adresar.resolve("x.bin");
		SouborPredpoctu.zapis(soubor, "klic-é", t);
		Assert.assertNotNull("klíč s diakritikou", SouborPredpoctu.nacti(soubor, "klic-é"));
		Files.write(soubor, java.util.Arrays.copyOf(Files.readAllBytes(soubor), (int) Files.size(soubor) + 3));
		Assert.assertNull(SouborPredpoctu.nacti(soubor, "klic-é"));
	}

	@Test
	public void poskozenaHlavickaNeznemozniDalsiBehy() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(300, 4);
		final Predpocet p1 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p1, p1.spust(kese));
		final Path soubor = jedinySoubor();
		final byte[] dobry = Files.readAllBytes(soubor);
		final byte[] zly = dobry.clone();
		final int pozicePoctu = 4 + 4 + 2 + (int) (dobry.length - 4 - 4 - 2 - 4 - 300L * 18 - 4);
		zly[pozicePoctu] = 0x02;
		zly[pozicePoctu + 1] = (byte) 0xFA;
		zly[pozicePoctu + 2] = (byte) 0xF0;
		zly[pozicePoctu + 3] = (byte) 0x80;
		Files.write(soubor, zly);
		final Predpocet p2 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p2, p2.spust(kese));
		Assert.assertEquals(300, p2.pocetPrepocitanych());
		final Predpocet p3 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p3, p3.spust(kese));
		Assert.assertEquals("příště už z opraveného souboru", 0, p3.pocetPrepocitanych());
	}

	@Test(timeout = 60000)
	public void spustNeblokujeVolajicihoAniKdyzPredchoziBeziASekaSeVPosluchaci() throws Exception {
		final CountDownLatch uvnitr = new CountDownLatch(1);
		final CountDownLatch pustit = new CountDownLatch(1);
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		p.pridejPosluchace(s -> {
			if (s.faze() == StavPredpoctu.Faze.POCITA) {
				uvnitr.countDown();
				try {
					pustit.await(30, TimeUnit.SECONDS);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		final java.util.concurrent.Future<?> prvni = p.spust(kese(100, 1));
		Assert.assertTrue(uvnitr.await(30, TimeUnit.SECONDS));
		final long zacatek = System.nanoTime();
		final java.util.concurrent.Future<?> druhy = p.spust(kese(100, 2));
		Assert.assertTrue("spust se nesmí zablokovat", (System.nanoTime() - zacatek) / 1_000_000 < 2000);
		pustit.countDown();
		druhy.get(30, TimeUnit.SECONDS);
		Assert.assertTrue(prvni.isDone());
		Assert.assertEquals(StavPredpoctu.Faze.HOTOVO, p.stav().faze());
	}

	@Test
	public void predpocetBeziNaPojmenovanemDemonuSNizsiPriorityAJdeZavrit() throws Exception {
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		final List<Thread> vlakna = new CopyOnWriteArrayList<>();
		p.pridejPosluchace(stav -> vlakna.add(Thread.currentThread()));
		pockej(p, p.spust(kese(5, 1)));
		Assert.assertFalse(vlakna.isEmpty());
		final Thread t = vlakna.get(0);
		Assert.assertEquals("Předpočet challenge", t.getName());
		Assert.assertTrue(t.isDaemon());
		Assert.assertEquals(Thread.NORM_PRIORITY - 2, t.getPriority());
		Assert.assertFalse(p.jeZavreno());
		p.zavri();
		Assert.assertTrue(p.jeZavreno());
	}

	@Test
	public void novySpustZrusiBeziciPredpocetACestouJeStavZruseno() throws Exception {
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		final CountDownLatch spusteno = new CountDownLatch(1);
		final CountDownLatch pokracuj = new CountDownLatch(1);
		final List<StavPredpoctu.Faze> faze = new CopyOnWriteArrayList<>();
		p.pridejPosluchace(stav -> {
			faze.add(stav.faze());
			if (spusteno.getCount() > 0) {
				spusteno.countDown();
				try {
					pokracuj.await(30, TimeUnit.SECONDS);
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		});
		final java.util.concurrent.Future<?> prvni = p.spust(kese(100, 1));
		Assert.assertTrue(spusteno.await(30, TimeUnit.SECONDS));
		final java.util.concurrent.Future<?> druhy = p.spust(kese(100, 2));
		pokracuj.countDown();
		prvni.get(60, TimeUnit.SECONDS);
		druhy.get(60, TimeUnit.SECONDS);
		Assert.assertTrue(faze.toString(), faze.contains(StavPredpoctu.Faze.ZRUSENO));
		Assert.assertEquals(StavPredpoctu.Faze.HOTOVO, p.stav().faze());
	}

	@Test
	public void souborSeZnovuNezapisujeKdyzSeNicNezmenilAleZapisePriUbytkuKesi() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(200, 3);
		final Predpocet p1 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p1, p1.spust(kese));
		final Path soubor = jedinySoubor();
		final java.nio.file.attribute.FileTime davno = java.nio.file.attribute.FileTime.fromMillis(1_000_000_000_000L);
		Files.setLastModifiedTime(soubor, davno);
		final Predpocet p2 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p2, p2.spust(kese));
		Assert.assertEquals(0, p2.pocetPrepocitanych());
		Assert.assertEquals("beze změny se soubor nepřepisuje", davno, Files.getLastModifiedTime(soubor));
		final Predpocet p3 = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p3, p3.spust(kese.subList(0, 150)));
		Assert.assertEquals(0, p3.pocetPrepocitanych());
		Assert.assertNotEquals("při ubytku keší se soubor zapíše", davno, Files.getLastModifiedTime(soubor));
	}

	@Test
	public void chybaBezZpravyMaVStavuNazevVyjimkyAChybaSeZpravouJenJiZpravu() throws Exception {
		final Oblast bezZpravy = new Oblast() {
			@Override
			public boolean obsahuje(final double lat, final double lon) {
				throw new IllegalStateException();
			}

			@Override
			public String verze() {
				return "bez-zpravy";
			}
		};
		final Predpocet p = predpocet("z1", bezZpravy, ZdrojPravidel.PGC, LAT);
		p.spust(kese(3, 1)).get(30, TimeUnit.SECONDS);
		Assert.assertEquals("java.lang.IllegalStateException", p.stav().chyba());
		final Oblast sNazvem = new Oblast() {
			@Override
			public boolean obsahuje(final double lat, final double lon) {
				throw new IllegalStateException("přesná zpráva");
			}

			@Override
			public String verze() {
				return "se-zpravou";
			}
		};
		final Predpocet q = predpocet("z1", sNazvem, ZdrojPravidel.PGC, LAT);
		q.spust(kese(3, 1)).get(30, TimeUnit.SECONDS);
		Assert.assertEquals("přesná zpráva", q.stav().chyba());
	}

	@Test
	public void poPredpoctuSeKlasifikatorMeniZPrimehoNaPredpocitanyAVraciStejneHodnoty() throws Exception {
		final List<VypocetSektoruTest.Kes> kese = kese(300, 5);
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		final Klasifikator pred = p.klasifikator();
		pockej(p, p.spust(kese));
		final Klasifikator po = p.klasifikator();
		Assert.assertNotSame(pred, po);
		for (final VypocetSektoruTest.Kes k : kese) {
			Assert.assertEquals(pred.klasifikuj(k), po.klasifikuj(k));
		}
	}

	@Test
	public void uklidPonechaOsmNejnovejsichSouboruPodleCasuUpravyNeDlePoradiVeSlozce() throws Exception {
		final Tabulka tabulka = new Tabulka(4);
		final List<Path> soubory = new ArrayList<>();
		final Random r = new Random(11);
		final List<Integer> poradi = new ArrayList<>();
		for (int i = 0; i < 24; i++) {
			poradi.add(i);
		}
		Collections.shuffle(poradi, r);
		for (int i = 0; i < 24; i++) {
			final Path f = adresar.resolve(String.format("challenge-%08x-7.bin", 1000 + i));
			SouborPredpoctu.zapis(f, "klic" + i, tabulka);
			nastavStari(f, poradi.get(i));
			soubory.add(f);
		}
		final Predpocet p = predpocet("z-nove", oblast, ZdrojPravidel.PGC, LAT);
		pockej(p, p.spust(kese(20, 1)));
		Assert.assertEquals(8, vlastniSoubory());
		// nové dílo je nejnovější, dalších sedm nejnovějších ze staré sady zůstane
		for (int i = 0; i < 24; i++) {
			final boolean maZustat = poradi.get(i) >= 24 - 7;
			Assert.assertEquals("soubor " + i + " s pořadím stáří " + poradi.get(i), maZustat, Files.exists(soubory.get(i)));
		}
	}

	@Test
	public void prubehSeHlasiKazdychCtyriTisiceKesi() throws Exception {
		final Predpocet p = predpocet("z1", oblast, ZdrojPravidel.PGC, LAT);
		final List<Long> hotovo = new CopyOnWriteArrayList<>();
		p.pridejPosluchace(stav -> {
			if (stav.faze() == StavPredpoctu.Faze.POCITA) {
				hotovo.add(stav.hotovo());
			}
		});
		pockej(p, p.spust(kese(9000, 1)));
		Assert.assertEquals(java.util.Arrays.asList(0L, 4096L, 8192L), hotovo);
	}
}
