package cz.geokuk.plugins.challenge;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import javax.swing.SwingUtilities;

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

/** Plánovač challenge při rychlých posunech středu mapy, měnících se datech a vypnutí uprostřed práce. */
public class PlanovacZatezTest {

	private static final Wgs A = new Wgs(50.0, 14.0);
	private static final Wgs B = new Wgs(51.5, 16.0);
	private static final LocalDate DNES = LocalDate.of(2026, 11, 5);

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	/** Vlákna plánovače, která v JVM žila ještě před vznikem plánovače tohoto testu (po jiných testech). */
	private java.util.Set<Thread> vlaknaPredTestem;
	private ChallengeModel model;
	private PlanovacChallenge planovac;
	private volatile Wgs stred = A;
	private final List<ChallengeZmenenyEvent> udalosti = new CopyOnWriteArrayList<>();

	@Before
	public void setUp() throws Exception {
		final Path adresar = tmp.newFolder("predpocet").toPath();
		vlaknaPredTestem = vlaknaPlanovace();
		model = new ChallengeModel();
		model.setUloziste(() -> new MapoveUloziste());
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
				planovac.modelZmenen((ChallengeZmenenyEvent) event);
			}
		});
		planovac = new PlanovacChallenge(model, id -> Optional.<Oblast> empty(), () -> A, () -> adresar, () -> stred, k -> {}, 20);
		planovac.dnes = () -> DNES;
		model.initAfterInject();
	}

	@After
	public void zavri() {
		planovac.zavri();
	}

	private static void pockej(final BooleanSupplier podminka, final String co) throws Exception {
		final long konec = System.currentTimeMillis() + 30_000;
		while (!podminka.getAsBoolean() && System.currentTimeMillis() < konec) {
			Thread.sleep(10);
		}
		Assert.assertTrue("nedočkal jsem se: " + co, podminka.getAsBoolean());
	}

	private static void naEdt(final Runnable r) throws Exception {
		SwingUtilities.invokeAndWait(r);
	}

	/** Dvě nenalezená D/T keše na stejném políčku, jedna u bodu A a druhá u bodu B, a jedna nalezená, aby výpočet něco nalezl. */
	private static List<PolicekKes> dvaKandidati() {
		final List<PolicekKes> v = new ArrayList<>();
		v.add(PolicekKes.dt("K_A", A.lat + 0.01, A.lon + 0.01, false, 3, 5));
		v.add(PolicekKes.dt("K_B", B.lat + 0.01, B.lon + 0.01, false, 3, 5));
		v.add(PolicekKes.dt("FOUND", 50.5, 15.0, true, 0, 0));
		return v;
	}

	private Challenge matice() {
		return model.ulozVlastni(new Challenge("Matice", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
	}

	private String prvniKandidat(final Challenge c) {
		final VysledekPolicek v = model.vysledekPolicek(c);
		if (v == null) {
			return null;
		}
		for (final KandidatPolicka k : v.kandidati()) {
			if (k.popis().equals("D2,5/T3,5")) {
				return k.id();
			}
		}
		return null;
	}

	@Test
	public void rychleStridaniStreduNechaPoDoznivaniVysledekProPoslediStred() throws Exception {
		final Challenge c = matice();
		planovac.keseNacteny(dvaKandidati());
		naEdt(() -> {
			model.aktivuj(c);
			model.setZapnuto(true);
		});
		pockej(() -> "K_A".equals(prvniKandidat(c)), "výsledek pro A");
		for (int kolo = 0; kolo < 300; kolo++) {
			stred = kolo % 2 == 0 ? B : A;
			naEdt(planovac::stredMapyZmenen);
		}
		stred = B;
		naEdt(planovac::stredMapyZmenen);
		pockej(() -> "K_B".equals(prvniKandidat(c)), "výsledek pro poslední střed B");
		Thread.sleep(200);
		Assert.assertEquals("K_B", prvniKandidat(c));
	}

	@Test
	public void souberneZmenyDatAStreduDajiPoDoznivaniStavOdpovidajiciPoslednimuVstupu() throws Exception {
		final Challenge c = matice();
		planovac.keseNacteny(dvaKandidati());
		naEdt(() -> {
			model.aktivuj(c);
			model.setZapnuto(true);
		});
		pockej(() -> "K_A".equals(prvniKandidat(c)), "výsledek pro A");
		final Random r = new Random(2);
		for (int kolo = 0; kolo < 200; kolo++) {
			stred = r.nextBoolean() ? B : A;
			naEdt(planovac::stredMapyZmenen);
			if (kolo % 17 == 0) {
				planovac.keseNacteny(dvaKandidati());
			}
		}
		stred = A;
		naEdt(planovac::stredMapyZmenen);
		planovac.keseNacteny(dvaKandidati());
		pockej(() -> "K_A".equals(prvniKandidat(c)), "poslední střed A");
		Thread.sleep(200);
		Assert.assertEquals("K_A", prvniKandidat(c));
		Assert.assertEquals(StavPredpoctu.Faze.HOTOVO, model.stavPredpoctu(c).faze());
	}

	@Test
	public void zavreniUprostredPraceNezanechaVlaknaAnizadneDalsiUdalosti() throws Exception {
		final Challenge c = matice();
		final List<PolicekKes> data = new ArrayList<>();
		final Random r = new Random(1);
		for (int i = 0; i < 100_000; i++) {
			data.add(PolicekKes.dt("GC" + i, 49 + r.nextDouble() * 3, 13 + r.nextDouble() * 4, i % 2 == 0, r.nextInt(9), r.nextInt(9)));
		}
		planovac.keseNacteny(data);
		naEdt(() -> {
			model.aktivuj(c);
			model.setZapnuto(true);
		});
		pockej(() -> model.stavPredpoctu(c) != null, "výpočet začal");
		for (int kolo = 0; kolo < 50; kolo++) {
			stred = kolo % 2 == 0 ? B : A;
			naEdt(planovac::stredMapyZmenen);
			planovac.keseNacteny(data);
		}
		planovac.zavri();
		final long konec = System.currentTimeMillis() + 15_000;
		while (System.currentTimeMillis() < konec && jeJesteVlakno()) {
			Thread.sleep(20);
		}
		Assert.assertFalse("vlákna plánovače po zavření nezanikla", jeJesteVlakno());
		naEdt(() -> {});
		final int po = udalosti.size();
		Thread.sleep(300);
		naEdt(() -> {});
		Assert.assertEquals("po zavření už nic nepřichází", po, udalosti.size());
		planovac.keseNacteny(data);
		planovac.stredMapyZmenen();
		planovac.domovZmenen();
		planovac.zavri();
	}

	private static java.util.Set<Thread> vlaknaPlanovace() {
		final java.util.Set<Thread> v = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
		for (final Thread t : Thread.getAllStackTraces().keySet()) {
			final String n = t.getName();
			if (t.isAlive() && ("Plánovač challenge".equals(n) || "Odklad plánovače challenge".equals(n) || "Předpočet challenge".equals(n))) {
				v.add(t);
			}
		}
		return v;
	}

	/** Žije ještě některé vlákno, které vzniklo až po začátku tohoto testu? */
	private boolean jeJesteVlakno() {
		final java.util.Set<Thread> ted = vlaknaPlanovace();
		ted.removeAll(vlaknaPredTestem);
		return !ted.isEmpty();
	}


	@Test
	public void posunStreduZpetPocasPrepoctuNenechaVysledekProStarySteD() throws Exception {
		model.setKalendarVUi(true);
		final Challenge kal = model.ulozVlastni(new Challenge("Kal", Typ.KALENDAR, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		final List<PolicekKes> data = new ArrayList<>();
		data.add(new PolicekKes("K_A", A.lat + 0.01, A.lon + 0.01, false, 0, 0, null, DNES.plusDays(10), true));
		data.add(new PolicekKes("K_B", B.lat + 0.01, B.lon + 0.01, false, 0, 0, null, DNES.plusDays(10), true));
		planovac.keseNacteny(data);
		naEdt(() -> {
			model.aktivuj(kal);
			model.setZapnuto(true);
		});
		pockej(() -> prvniKandidatKalendare(kal, "K_A"), "výsledek pro A");
		final long vysledkuPred = udalostiVysledku();
		final CountDownLatch zacal = new CountDownLatch(1);
		final CountDownLatch pust = new CountDownLatch(1);
		planovac.dnes = () -> {
			zacal.countDown();
			try {
				pust.await(30, TimeUnit.SECONDS);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return DNES;
		};
		stred = B;
		naEdt(planovac::stredMapyZmenen);
		Assert.assertTrue("přepočet pro B začal", zacal.await(30, TimeUnit.SECONDS));
		stred = A;
		naEdt(planovac::stredMapyZmenen);
		pust.countDown();
		pockej(() -> udalostiVysledku() > vysledkuPred, "doběhl přepočet po návratu středu");
		pockej(() -> prvniKandidatKalendare(kal, "K_A"), "výsledek odpovídá středu mapy A po návratu");
	}

	private long udalostiVysledku() {
		return udalosti.stream().filter(e -> e.druh() == ChallengeZmenenyEvent.Druh.VYSLEDEK).count();
	}

	private boolean prvniKandidatKalendare(final Challenge kal, final String id) {
		final VysledekPolicek v = model.vysledekPolicek(kal);
		return v != null && !v.kandidati().isEmpty() && id.equals(v.kandidati().get(0).id());
	}

	@Test
	public void prazdnaAJednoprvkovaDataAStredNullPodZatiziNeshodiPlanovac() throws Exception {
		final Challenge c = matice();
		naEdt(() -> {
			model.aktivuj(c);
			model.setZapnuto(true);
		});
		for (int kolo = 0; kolo < 100; kolo++) {
			stred = kolo % 3 == 0 ? null : kolo % 3 == 1 ? A : B;
			naEdt(planovac::stredMapyZmenen);
			planovac.keseNacteny(kolo % 2 == 0 ? Collections.<PolicekKes> emptyList() : dvaKandidati());
			planovac.domovZmenen();
		}
		stred = A;
		planovac.keseNacteny(dvaKandidati());
		pockej(() -> "K_A".equals(prvniKandidat(c)), "po bouři vstupů se spočítá poslední stav");
	}
}
