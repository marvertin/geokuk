package cz.geokuk.plugins.challenge;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;
import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

public class PlanovacChallengeTest {

	private static final double LAT = 50.0;
	private static final double LON = 14.0;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private ChallengeModel model;
	private PlanovacChallenge planovac;
	private final List<ChallengeZmenenyEvent> udalosti = new CopyOnWriteArrayList<>();
	private final List<VybranyKandidat> vystredeno = new CopyOnWriteArrayList<>();
	private final Map<String, Oblast> oblasti = new HashMap<>();
	private volatile Wgs domov = new Wgs(LAT, LON);
	private volatile Wgs stredMapy = new Wgs(LAT, LON);
	private Path adresar;
	/** Fáze předpočtu v okamžiku každého hlášení do modelu. */
	private final List<StavPredpoctu.Faze> hlasenaFaze = new CopyOnWriteArrayList<>();

	@Before
	public void setUp() throws Exception {
		adresar = tmp.newFolder("predpocet").toPath();
		model = new ChallengeModel();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
			}
		});
		final MapoveUloziste uloziste = new MapoveUloziste();
		model.setUloziste(() -> uloziste);
		model.initAfterInject();
		planovac = new PlanovacChallenge(model, id -> Optional.ofNullable(oblasti.get(id)), () -> domov, () -> adresar, () -> stredMapy, vystredeno::add, 50);
	}

	@After
	public void zavri() {
		planovac.zavri();
	}

	private static Challenge vlastni(final String nazev, final Stred stred, final OblastDef oblast) {
		return new Challenge(nazev, Typ.SEKTORY_360, stred, oblast, ZdrojPravidel.PGC);
	}

	private static List<VypocetSektoruTest.Kes> kese(final int pocet, final long seed) {
		final Random r = new Random(seed);
		final List<VypocetSektoruTest.Kes> v = new ArrayList<>();
		for (int i = 0; i < pocet; i++) {
			v.add(new VypocetSektoruTest.Kes("GC" + i, 49.4 + r.nextDouble() * 1.3, 13.4 + r.nextDouble() * 2.0, r.nextInt(3) == 0));
		}
		return v;
	}

	private static void pockej(final BooleanSupplier podminka, final String co) throws Exception {
		final long konec = System.currentTimeMillis() + 30_000;
		while (!podminka.getAsBoolean() && System.currentTimeMillis() < konec) {
			Thread.sleep(20);
		}
		Assert.assertTrue("nedočkal jsem se: " + co, podminka.getAsBoolean());
	}

	private void zapni(final Challenge... c) throws Exception {
		javax.swing.SwingUtilities.invokeAndWait(() -> {
			for (final Challenge d : c) {
				model.aktivuj(d);
			}
			model.setZapnuto(true);
		});
	}

	/** Model hlásí změny, planovač na ně reaguje stejně jako v programu. */
	private void napojNaModel() {
		final ChallengeModel m = model;
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
				final ChallengeZmenenyEvent e = (ChallengeZmenenyEvent) event;
				if (e.druh() == ChallengeZmenenyEvent.Druh.PREDPOCET) {
					hlasenaFaze.add(model.stavPredpoctu(e.challenge()).faze());
				}
				planovac.modelZmenen(e);
			}
		});
	}

	private long pocet(final ChallengeZmenenyEvent.Druh druh) {
		return udalosti.stream().filter(e -> e.druh() == druh).count();
	}

	@Test
	public void zapnutiPocitaVysledekShodnyPrimemuVypoctu() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		final List<VypocetSektoruTest.Kes> data = kese(4000, 1);
		planovac.keseNacteny(data);
		zapni(c);
		pockej(() -> model.vysledek(c) != null, "výsledek");
		final VysledekSektoru ocekavany = VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, Collections.<String> emptySet(), data);
		Assert.assertEquals(ocekavany.chybejici(), model.vysledek(c).chybejici());
		Assert.assertEquals(ocekavany.pocetZapocitanychNalezu(), model.vysledek(c).pocetZapocitanychNalezu());
		Assert.assertEquals(ocekavany.vsichniKandidati().size(), model.vysledek(c).vsichniKandidati().size());
		pockej(() -> model.stavPredpoctu(c).faze() == StavPredpoctu.Faze.HOTOVO, "stav hotovo");
		Assert.assertEquals(360 - ocekavany.pocetObsazenych(), model.pocetChybejicich(c));
	}

	@Test
	public void vypnutyPlanovacNicNepocita() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		javax.swing.SwingUtilities.invokeAndWait(() -> model.aktivuj(c));
		planovac.keseNacteny(kese(500, 1));
		Thread.sleep(600);
		Assert.assertNull(model.vysledek(c));
		Assert.assertEquals(0, pocet(ChallengeZmenenyEvent.Druh.VYSLEDEK));
	}

	@Test
	public void vypnutiPriDalsiZmeneVysledekNenahradi() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		planovac.keseNacteny(kese(500, 1));
		zapni(c);
		pockej(() -> model.vysledek(c) != null, "výsledek");
		javax.swing.SwingUtilities.invokeAndWait(() -> model.setZapnuto(false));
		final long pred = pocet(ChallengeZmenenyEvent.Druh.VYSLEDEK);
		planovac.keseNacteny(kese(500, 2));
		Thread.sleep(600);
		Assert.assertEquals("vypnuto, nic se nepřepočítá", pred, pocet(ChallengeZmenenyEvent.Druh.VYSLEDEK));
	}

	@Test
	public void rychleZmenySeSlouciDoJednohoVypoctu() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		final List<VypocetSektoruTest.Kes> data = kese(800, 3);
		zapni(c);
		for (int i = 0; i < 10; i++) {
			planovac.keseNacteny(data);
			planovac.domovZmenen();
		}
		pockej(() -> model.vysledek(c) != null, "výsledek");
		Thread.sleep(400);
		Assert.assertEquals("jeden výpočet po dvaceti změnách", 1, pocet(ChallengeZmenenyEvent.Druh.VYSLEDEK));
	}

	@Test
	public void novaDataPrepocitajVysledek() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		final List<VypocetSektoruTest.Kes> data = kese(2000, 4);
		planovac.keseNacteny(data);
		zapni(c);
		pockej(() -> model.vysledek(c) != null, "první výsledek");
		final VysledekSektoru prvni = model.vysledek(c);
		final List<VypocetSektoruTest.Kes> jina = kese(2000, 5);
		planovac.keseNacteny(jina);
		pockej(() -> model.vysledek(c) != prvni, "nový výsledek");
		Assert.assertEquals(VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, Collections.<String> emptySet(), jina).chybejici(), model.vysledek(c).chybejici());
	}

	@Test
	public void zmenaDomovaPrepocitaChallengeSeStredemVDomove() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		final List<VypocetSektoruTest.Kes> data = kese(3000, 6);
		planovac.keseNacteny(data);
		zapni(c);
		pockej(() -> model.vysledek(c) != null, "výsledek");
		final VysledekSektoru prvni = model.vysledek(c);
		domov = new Wgs(49.5, 15.0);
		planovac.domovZmenen();
		pockej(() -> model.vysledek(c) != prvni, "přepočet po změně domova");
		Assert.assertEquals(VypocetSektoru.spocti(ZdrojPravidel.PGC, 49.5, 15.0, null, Collections.<String> emptySet(), data).chybejici(), model.vysledek(c).chybejici());
	}

	@Test
	public void vicAktivnichSeCitaPostupneAKazdaMaSvujVysledek() throws Exception {
		napojNaModel();
		oblasti.put("o1", new OblastPolygony("o1", "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(49.4, 13.4, 50.0, 14.4), Collections.<Okraj> emptyList()))));
		final Challenge a = model.ulozVlastni(vlastni("A", Stred.domov(), OblastDef.zadna()));
		final Challenge b = model.ulozVlastni(vlastni("B", Stred.souradnice(49.7, 13.9), OblastDef.stat("o1")));
		final Challenge d = model.ulozVlastni(vlastni("C", Stred.souradnice(50.4, 14.9), OblastDef.zadna()));
		final List<VypocetSektoruTest.Kes> data = kese(5000, 7);
		planovac.keseNacteny(data);
		zapni(a, b, d);
		pockej(() -> model.vysledek(a) != null && model.vysledek(b) != null && model.vysledek(d) != null, "tři výsledky");
		Assert.assertEquals(VypocetSektoru.spocti(ZdrojPravidel.PGC, 49.7, 13.9, oblasti.get("o1"), Collections.<String> emptySet(), data).chybejici(), model.vysledek(b).chybejici());
		Assert.assertNotEquals(model.vysledek(a).chybejici(), model.vysledek(d).chybejici());
		// Předpočty běžely postupně: stav další challenge se začal hlásit až po dokončení předchozí.
		final List<String> poradi = new ArrayList<>();
		for (final ChallengeZmenenyEvent e : udalosti) {
			if (e.druh() == ChallengeZmenenyEvent.Druh.PREDPOCET && model.stavPredpoctu(e.challenge()) != null) {
				poradi.add(e.challenge().nazev());
			}
		}
		Assert.assertEquals("A", poradi.get(0));
		Assert.assertTrue(poradi.indexOf("B") > poradi.lastIndexOf("A"));
		Assert.assertTrue(poradi.indexOf("C") > poradi.lastIndexOf("B"));
	}

	@Test
	public void definiceBezDatOblastiMaStavChybaAOstatniSeSpoctou() throws Exception {
		napojNaModel();
		final Challenge dobra = model.ulozVlastni(vlastni("Dobrá", Stred.domov(), OblastDef.zadna()));
		final Challenge spatna = model.ulozVlastni(vlastni("Špatná", Stred.domov(), OblastDef.stat("stat-cz")));
		planovac.keseNacteny(kese(500, 8));
		zapni(spatna, dobra);
		pockej(() -> model.vysledek(dobra) != null, "výsledek dobré");
		pockej(() -> model.stavPredpoctu(spatna).faze() == StavPredpoctu.Faze.CHYBA, "chyba špatné");
		Assert.assertTrue(model.stavPredpoctu(spatna).chyba(), model.stavPredpoctu(spatna).chyba().contains("stat-cz"));
		Assert.assertNull(model.vysledek(spatna));
	}

	@Test
	public void stredListinguJeKesZNactenychDat() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Listing", Stred.listing("GCSTRED"), OblastDef.zadna()));
		final List<VypocetSektoruTest.Kes> data = kese(1500, 9);
		data.add(new VypocetSektoruTest.Kes("GCSTRED", 49.9, 14.1, false));
		planovac.keseNacteny(data);
		zapni(c);
		pockej(() -> model.vysledek(c) != null, "výsledek");
		Assert.assertEquals(VypocetSektoru.spocti(ZdrojPravidel.PGC, 49.9, 14.1, null, Collections.<String> emptySet(), data).chybejici(), model.vysledek(c).chybejici());
		final Challenge bez = model.ulozVlastni(vlastni("Bez", Stred.listing("GCNENI"), OblastDef.zadna()));
		javax.swing.SwingUtilities.invokeAndWait(() -> model.aktivuj(bez));
		pockej(() -> model.stavPredpoctu(bez).faze() == StavPredpoctu.Faze.CHYBA, "chyba listingu");
	}

	@Test
	public void dalsiChybejiciVybiraPodleStreduMapyCykliceAVystredujeMapu() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		Assert.assertNull("bez výsledku není co vybrat", planovac.dalsiChybejici(c));
		final List<VypocetSektoruTest.Kes> data = new ArrayList<>();
		for (int s = 0; s < 300; s++) {
			data.add(VypocetSektoruTest.vAzimutu("N" + s, s + 0.5, 20_000, true));
		}
		for (int s = 300; s < 306; s++) {
			data.add(VypocetSektoruTest.vAzimutu("K" + s, s + 0.5, 10_000 + 5_000 * (s - 300), false));
		}
		planovac.keseNacteny(data);
		zapni(c);
		pockej(() -> model.vysledek(c) != null, "výsledek");
		final VysledekSektoru v = model.vysledek(c);
		Assert.assertEquals(60, v.chybejici().size());
		Assert.assertEquals(6, v.vsichniKandidati().size());
		// Střed mapy u kandidáta K305 (nejdál od středu challenge).
		final VysledekSektoru.KandidatKes nejdal = v.vsichniKandidati().get(5);
		stredMapy = new Wgs(nejdal.lat(), nejdal.lon());
		final VybranyKandidat prvni = planovac.dalsiChybejici(c);
		Assert.assertEquals("K305", prvni.id());
		Assert.assertEquals(prvni.id(), vystredeno.get(0).id());
		pockej(() -> model.vybranyKandidat(c) != null, "kandidát v modelu");
		Assert.assertEquals("K305", model.vybranyKandidat(c).id());
		Assert.assertEquals(305, model.vybranyKandidat(c).sektor());
		final List<String> vybrani = new ArrayList<>();
		vybrani.add(prvni.id());
		for (int i = 0; i < 5; i++) {
			vybrani.add(planovac.dalsiChybejici(c).id());
		}
		Assert.assertEquals("všech šest kandidátů, každý jednou", 6, new java.util.HashSet<>(vybrani).size());
		Assert.assertEquals("po kole se začíná znovu", "K305", planovac.dalsiChybejici(c).id());
	}

	@Test
	public void zavreniZastaviPlanovani() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		zapni(c);
		planovac.zavri();
		planovac.keseNacteny(kese(300, 1));
		Thread.sleep(500);
		Assert.assertNull(model.vysledek(c));
	}

	@Test
	public void novaZmenaZrusiBeziciVypocetAVysledekJeZPosledniZmeny() throws Exception {
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Domov", Stred.domov(), OblastDef.zadna()));
		final List<VypocetSektoruTest.Kes> velka = kese(400_000, 10);
		planovac.keseNacteny(velka);
		zapni(c);
		Thread.sleep(300);
		final List<VypocetSektoruTest.Kes> mala = kese(1000, 11);
		planovac.keseNacteny(mala);
		pockej(() -> model.vysledek(c) != null && model.vysledek(c).pocetZapocitanychNalezu() > 0
				&& model.vysledek(c).pocetZapocitanychNalezu() == VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, Collections.<String> emptySet(), mala).pocetZapocitanychNalezu(), "výsledek z malých dat");
	}

	@Test
	public void predpoctyNebeziSoucasne() throws Exception {
		final java.util.concurrent.atomic.AtomicInteger bezi = new java.util.concurrent.atomic.AtomicInteger();
		final java.util.concurrent.atomic.AtomicInteger nejvic = new java.util.concurrent.atomic.AtomicInteger();
		final Oblast o = new OblastPolygony("o1", "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(49.0, 13.0, 51.0, 15.0), Collections.<Okraj> emptyList())));
		planovac.zavri();
		planovac = new PlanovacChallenge(model, id -> {
			nejvic.accumulateAndGet(bezi.incrementAndGet(), Math::max);
			try {
				Thread.sleep(150);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			bezi.decrementAndGet();
			return Optional.of(o);
		}, () -> domov, () -> adresar, () -> stredMapy, vystredeno::add, 20);
		napojNaModel();
		final Challenge[] c = new Challenge[4];
		for (int i = 0; i < c.length; i++) {
			c[i] = model.ulozVlastni(vlastni("C" + i, Stred.souradnice(49.5 + i * 0.1, 14.0), OblastDef.stat("o1")));
		}
		planovac.keseNacteny(kese(300, 1));
		zapni(c);
		pockej(() -> {
			for (final Challenge d : c) {
				if (model.vysledek(d) == null) {
					return false;
				}
			}
			return true;
		}, "všechny výsledky");
		Assert.assertEquals(1, nejvic.get());
	}

	@Test
	public void zastaralyBehNepublikujeVysledek() throws Exception {
		final java.util.concurrent.CountDownLatch uvnitr = new java.util.concurrent.CountDownLatch(1);
		final java.util.concurrent.CountDownLatch pust = new java.util.concurrent.CountDownLatch(1);
		final java.util.concurrent.atomic.AtomicInteger volani = new java.util.concurrent.atomic.AtomicInteger();
		final Oblast o = new OblastPolygony("o1", "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(49.0, 13.0, 51.0, 15.0), Collections.<Okraj> emptyList())));
		planovac.zavri();
		planovac = new PlanovacChallenge(model, id -> {
			if (volani.incrementAndGet() == 1) {
				uvnitr.countDown();
				try {
					pust.await();
				} catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			return Optional.of(o);
		}, () -> domov, () -> adresar, () -> stredMapy, vystredeno::add, 20);
		napojNaModel();
		final Challenge c = model.ulozVlastni(vlastni("Oblast", Stred.souradnice(50.0, 14.0), OblastDef.stat("o1")));
		final List<VypocetSektoruTest.Kes> stara = kese(500, 1);
		final List<VypocetSektoruTest.Kes> nova = kese(700, 2);
		planovac.keseNacteny(stara);
		zapni(c);
		Assert.assertTrue(uvnitr.await(20, java.util.concurrent.TimeUnit.SECONDS));
		planovac.keseNacteny(nova);
		pust.countDown();
		final int ocekavano = VypocetSektoru.spocti(ZdrojPravidel.PGC, 50.0, 14.0, o, Collections.<String> emptySet(), nova).pocetZapocitanychNalezu();
		pockej(() -> model.vysledek(c) != null && model.vysledek(c).pocetZapocitanychNalezu() == ocekavano, "výsledek z nových dat");
		Thread.sleep(500);
		Assert.assertEquals("starý běh nic nepublikoval", 1, pocet(ChallengeZmenenyEvent.Druh.VYSLEDEK));
	}

	private Challenge oblastni(final Oblast o) {
		oblasti.put("o1", o);
		return model.ulozVlastni(vlastni("Oblast", Stred.souradnice(50.0, 14.0), OblastDef.stat("o1")));
	}

	private static Oblast ctverec() {
		return new OblastPolygony("o1", "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(49.0, 13.0, 51.0, 15.0), Collections.<Okraj> emptyList())));
	}

	@Test
	public void dalsiZmenaZrusiBeziciPredpocet() throws Exception {
		napojNaModel();
		final List<Predpocet> predpocty = new CopyOnWriteArrayList<>();
		final java.util.concurrent.CountDownLatch bezi = new java.util.concurrent.CountDownLatch(1);
		final java.util.concurrent.CountDownLatch pust = new java.util.concurrent.CountDownLatch(1);
		planovac.poVytvoreni = p -> {
			predpocty.add(p);
			if (predpocty.size() == 1) {
				p.pridejPosluchace(s -> {
					if (s.faze() == StavPredpoctu.Faze.POCITA && bezi.getCount() > 0) {
						bezi.countDown();
						try {
							pust.await();
						} catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				});
			}
		};
		final Challenge c = oblastni(ctverec());
		planovac.keseNacteny(kese(500, 1));
		zapni(c);
		Assert.assertTrue(bezi.await(20, java.util.concurrent.TimeUnit.SECONDS));
		planovac.keseNacteny(kese(600, 2));
		pust.countDown();
		pockej(() -> model.vysledek(c) != null, "výsledek");
		Assert.assertEquals(StavPredpoctu.Faze.ZRUSENO, predpocty.get(0).stav().faze());
		Assert.assertTrue(predpocty.size() >= 2);
		pockej(() -> model.stavPredpoctu(c).faze() == StavPredpoctu.Faze.HOTOVO, "stav hotovo");
		Assert.assertFalse("zrušený běh se do modelu nehlásí: " + hlasenaFaze, hlasenaFaze.contains(StavPredpoctu.Faze.ZRUSENO));
	}

	@Test
	public void kazdyPredpocetJePoPrepoctuZavreny() throws Exception {
		napojNaModel();
		final List<Predpocet> predpocty = new CopyOnWriteArrayList<>();
		planovac.poVytvoreni = predpocty::add;
		final Challenge c = oblastni(ctverec());
		planovac.keseNacteny(kese(300, 1));
		zapni(c);
		pockej(() -> model.vysledek(c) != null, "výsledek");
		pockej(() -> !predpocty.isEmpty() && predpocty.stream().allMatch(Predpocet::jeZavreno), "předpočty zavřené");
	}

	@Test
	public void zrusenyPredpocetNedaVysledek() throws Exception {
		napojNaModel();
		final List<Predpocet> predpocty = new CopyOnWriteArrayList<>();
		planovac.poVytvoreni = p -> {
			predpocty.add(p);
			p.pridejPosluchace(s -> {
				if (s.faze() == StavPredpoctu.Faze.POCITA) {
					p.zrus();
				}
			});
		};
		final Challenge c = oblastni(ctverec());
		planovac.keseNacteny(kese(300, 1));
		zapni(c);
		pockej(() -> !predpocty.isEmpty() && predpocty.get(0).jeZavreno(), "předpočet zavřený");
		Thread.sleep(300);
		Assert.assertEquals(StavPredpoctu.Faze.ZRUSENO, predpocty.get(0).stav().faze());
		Assert.assertNull(model.vysledek(c));
	}
}
