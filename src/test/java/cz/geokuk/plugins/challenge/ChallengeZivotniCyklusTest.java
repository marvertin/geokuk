package cz.geokuk.plugins.challenge;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
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

/**
 * Celý život challenge bez displeje: definice, zapnutí a aktivace, přepočet plánovačem, výběr dalšího a předchozího chybějícího, uložení, nové spuštění programu nad stejným
 * úložištěm a odolnost vůči poškozenému nebo novějšímu nastavení.
 */
public class ChallengeZivotniCyklusTest {

	private static final double LAT = 50.0;
	private static final double LON = 14.0;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final MapoveUloziste uloziste = new MapoveUloziste();
	private final List<Instance> instance = new ArrayList<>();
	private volatile Wgs domov = new Wgs(LAT, LON);
	private volatile Wgs stredMapy = new Wgs(LAT, LON);
	private Path adresar;

	/** Jedno spuštění programu: model s plánovačem nad sdíleným úložištěm. */
	private final class Instance {
		final ChallengeModel model = new ChallengeModel();
		final List<VybranyKandidat> vystredeno = new CopyOnWriteArrayList<>();
		final List<ChallengeZmenenyEvent> udalosti = new CopyOnWriteArrayList<>();
		final PlanovacChallenge planovac;

		Instance() {
			planovac = new PlanovacChallenge(model, id -> Optional.<Oblast> empty(), () -> domov, () -> adresar, () -> stredMapy, vystredeno::add, 20);
			model.inject(new EventFirer() {
				@Override
				public void fire(final Event0<?> event) {
					udalosti.add((ChallengeZmenenyEvent) event);
					planovac.modelZmenen((ChallengeZmenenyEvent) event);
				}
			});
			model.setUloziste(() -> uloziste);
			model.initAfterInject();
		}
	}

	@Before
	public void setUp() throws Exception {
		adresar = tmp.newFolder("predpocet").toPath();
	}

	@After
	public void zavri() {
		for (final Instance i : instance) {
			i.planovac.zavri();
		}
	}

	private Instance spust() {
		final Instance i = new Instance();
		instance.add(i);
		return i;
	}

	private static void pockej(final BooleanSupplier podminka, final String co) throws Exception {
		final long konec = System.currentTimeMillis() + 30_000;
		while (!podminka.getAsBoolean() && System.currentTimeMillis() < konec) {
			Thread.sleep(20);
		}
		Assert.assertTrue("nedočkal jsem se: " + co, podminka.getAsBoolean());
	}

	private static void naEdt(final Runnable r) throws Exception {
		SwingUtilities.invokeAndWait(r);
	}

	private static List<VypocetSektoruTest.Kes> sektoroveKese() {
		final List<VypocetSektoruTest.Kes> data = new ArrayList<>();
		for (int s = 0; s < 300; s++) {
			data.add(VypocetSektoruTest.vAzimutu("N" + s, s + 0.5, 20_000, true));
		}
		for (int s = 300; s < 306; s++) {
			data.add(VypocetSektoruTest.vAzimutu("K" + s, s + 0.5, 10_000 + 5_000 * (s - 300), false));
		}
		return data;
	}

	private static List<PolicekKes> dtKese() {
		final List<PolicekKes> v = new ArrayList<>();
		for (int d = 0; d < 9; d++) {
			for (int t = 0; t < 3; t++) {
				v.add(PolicekKes.dt("F" + d + "_" + t, 50.0 + d * 0.001, 14.0 + t * 0.001, true, d, t));
			}
		}
		v.add(PolicekKes.dt("K1", 50.01, 14.01, false, 3, 5));
		v.add(PolicekKes.dt("K2", 50.30, 14.30, false, 3, 5));
		v.add(PolicekKes.dt("K3", 50.02, 14.02, false, 8, 8));
		return v;
	}

	private static long verze(final MapoveUloziste u) {
		return Long.parseLong(u.get("verze"));
	}

	// ---- Život sektorové challenge

	@Test
	public void sektorovaChallengeProjdeCelymZivotemPresRestartProgramu() throws Exception {
		final Instance a = spust();
		Assert.assertFalse(a.model.jeZapnuto());
		Assert.assertTrue("vlastní definice nejsou, šablony ano", a.model.vlastni().isEmpty() && !a.model.sablony().isEmpty());
		final Challenge c = a.model.ulozVlastni(new Challenge("Můj prstenec", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		final long verzeDefinice = verze(uloziste);
		naEdt(() -> {
			a.model.aktivuj(c);
			a.model.setZapnuto(true);
		});
		Assert.assertTrue("aktivace i zapnutí se ukládají: " + uloziste.m, verze(uloziste) > verzeDefinice);
		a.planovac.keseNacteny(sektoroveKese());
		pockej(() -> a.model.vysledek(c) != null, "výsledek");
		final VysledekSektoru v = a.model.vysledek(c);
		Assert.assertEquals(60, v.chybejici().size());
		Assert.assertEquals(6, v.vsichniKandidati().size());

		// Další a předchozí chybějící podle středu mapy.
		final VysledekSektoru.KandidatKes nejdal = v.vsichniKandidati().get(5);
		stredMapy = new Wgs(nejdal.lat(), nejdal.lon());
		Assert.assertNull("před prvním výběrem není předchozí", a.planovac.predchoziChybejici(c));
		final VybranyKandidat prvni = a.planovac.dalsiChybejici(c);
		final VybranyKandidat druhy = a.planovac.dalsiChybejici(c);
		Assert.assertEquals("K305", prvni.id());
		Assert.assertNotEquals(prvni.id(), druhy.id());
		Assert.assertEquals(druhy.id(), a.vystredeno.get(1).id());
		final VybranyKandidat zpet = a.planovac.predchoziChybejici(c);
		Assert.assertEquals("předchozí je první vybraný", prvni.id(), zpet.id());
		Assert.assertEquals(prvni.id(), a.vystredeno.get(a.vystredeno.size() - 1).id());

		// Restart: stejné úložiště, nová instance.
		final long verzePredRestartem = verze(uloziste);
		final Instance b = spust();
		Assert.assertTrue(b.model.jeZapnuto());
		Assert.assertEquals(Collections.singletonList(c), b.model.vlastni());
		Assert.assertEquals(Collections.singletonList(c), b.model.aktivni());
		Assert.assertNull("výsledky se neukládají, počítají se znovu", b.model.vysledek(c));
		Assert.assertNull("vybraný kandidát se neukládá", b.model.vybranyKandidat(c));
		Assert.assertEquals("pouhé načtení nic nepřepisuje", verzePredRestartem, verze(uloziste));
		b.planovac.keseNacteny(sektoroveKese());
		pockej(() -> b.model.vysledek(c) != null, "výsledek po restartu");
		Assert.assertEquals(v.chybejici(), b.model.vysledek(c).chybejici());
		Assert.assertEquals(v.vsichniKandidati().size(), b.model.vysledek(c).vsichniKandidati().size());
		pockej(() -> b.model.stavPredpoctu(c).faze() == StavPredpoctu.Faze.HOTOVO, "předpočet po restartu");

		// Změna po restartu se ukládá s vyšší verzí a třetí start ji vidí.
		naEdt(() -> b.model.deaktivuj(c));
		Assert.assertTrue(verze(uloziste) > verzePredRestartem);
		final Instance d = spust();
		Assert.assertTrue(d.model.aktivni().isEmpty());
		Assert.assertEquals(Collections.singletonList(c), d.model.vlastni());
	}

	@Test
	public void verzeUlozeniRosteSKazdouZmenouAStarSadaDefinicSeUklidi() throws Exception {
		final Instance a = spust();
		final Challenge c1 = a.model.ulozVlastni(new Challenge("A", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		final long v1 = verze(uloziste);
		a.model.ulozVlastni(new Challenge("B", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.STATOR));
		final long v2 = verze(uloziste);
		Assert.assertTrue(v2 > v1);
		naEdt(() -> a.model.smazVlastni(c1));
		final long v3 = verze(uloziste);
		Assert.assertTrue(v3 > v2);
		for (final String k : uloziste.m.keySet()) {
			if (k.startsWith("definice.")) {
				Assert.assertTrue("jen aktuální sada definic: " + k, k.startsWith("definice." + v3 + "."));
			}
		}
		final Instance b = spust();
		Assert.assertEquals(1, b.model.vlastni().size());
		Assert.assertEquals("B", b.model.vlastni().get(0).nazev());
	}

	private static long pocet(final Instance a, final ChallengeZmenenyEvent.Druh druh) {
		return a.udalosti.stream().filter(e -> e.druh() == druh).count();
	}

	@Test
	public void prejmenovaniPrenesVysledekVybranehoKandidataPruhAFiltrAUlozi() throws Exception {
		final Instance a = spust();
		final Challenge c = a.model.ulozVlastni(new Challenge("Původní", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		naEdt(() -> {
			a.model.aktivuj(c);
			a.model.setZapnuto(true);
		});
		a.planovac.keseNacteny(sektoroveKese());
		pockej(() -> a.model.vysledek(c) != null, "výsledek");
		final VysledekSektoru v = a.model.vysledek(c);
		final VybranyKandidat k = a.planovac.dalsiChybejici(c);
		naEdt(() -> {
			a.model.setZobrazena(c);
			a.model.setJenKandidatiPro(c);
		});
		pockej(() -> a.model.vybranyKandidat(c) != null, "vybraný kandidát");
		final long definic = pocet(a, ChallengeZmenenyEvent.Druh.DEFINICE);

		final Challenge nova = a.model.prejmenuj(c, "Přejmenovaná");
		Assert.assertEquals("Přejmenovaná", nova.nazev());
		Assert.assertSame("výsledek se přenesl", v, a.model.vysledek(nova));
		Assert.assertEquals(k.id(), a.model.vybranyKandidat(nova).id());
		Assert.assertNotNull(a.model.stavPredpoctu(nova));
		Assert.assertEquals("pruh ukazuje přejmenovanou", nova, a.model.zobrazena());
		Assert.assertEquals("filtr kandidátů platí pro přejmenovanou", nova, a.model.jenKandidatiPro());
		Assert.assertEquals(definic + 1, pocet(a, ChallengeZmenenyEvent.Druh.DEFINICE));
		Assert.assertEquals(Collections.singletonList(nova), a.model.aktivni());

		final Instance b = spust();
		Assert.assertEquals(Collections.singletonList(nova), b.model.vlastni());
		Assert.assertEquals(Collections.singletonList(nova), b.model.aktivni());
		for (final String klic : uloziste.m.values()) {
			Assert.assertNotEquals("starý název v úložišti nezůstal", "Původní", klic);
		}
	}

	@Test
	public void prejmenovaniNaJizExistujiciNazevSeOdmitneAStavZustane() throws Exception {
		final Instance a = spust();
		final Challenge c1 = a.model.ulozVlastni(new Challenge("Jedna", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		a.model.ulozVlastni(new Challenge("Dvě", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		final long verzePred = verze(uloziste);
		final long udalostiPred = a.udalosti.size();
		try {
			a.model.prejmenuj(c1, "dvě");
			Assert.fail();
		} catch (final ChallengeChyba e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("už existuje"));
		}
		Assert.assertEquals(verzePred, verze(uloziste));
		Assert.assertEquals(udalostiPred, a.udalosti.size());
		Assert.assertEquals("Jedna", a.model.vlastni().get(0).nazev());
	}

	@Test
	public void kopieSablonyJeVlastniUlozenaDefiniceAPoRestartuZustane() throws Exception {
		final Instance a = spust();
		final Challenge sablona = a.model.sablony().get(0);
		Assert.assertTrue(a.model.jeSablona(sablona));
		final long definic = pocet(a, ChallengeZmenenyEvent.Druh.DEFINICE);
		final Challenge kopie = a.model.kopieSablony(sablona);
		Assert.assertFalse("kopie není šablona", a.model.jeSablona(kopie));
		Assert.assertNotEquals(sablona.nazev(), kopie.nazev());
		Assert.assertEquals(definic + 1, pocet(a, ChallengeZmenenyEvent.Druh.DEFINICE));
		Assert.assertEquals(Collections.singletonList(kopie), a.model.vlastni());
		final Instance b = spust();
		Assert.assertEquals(Collections.singletonList(kopie), b.model.vlastni());
		Assert.assertTrue(b.model.jeSablona(sablona));
	}

	@Test
	public void smazaniAktivniChallengeUklidiStavFiltrAUlozeniAHlasiDefiniciIAktivni() throws Exception {
		final Instance a = spust();
		final Challenge c = a.model.ulozVlastni(new Challenge("Ke smazání", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		naEdt(() -> {
			a.model.aktivuj(c);
			a.model.setZapnuto(true);
		});
		a.planovac.keseNacteny(sektoroveKese());
		pockej(() -> a.model.vysledek(c) != null, "výsledek");
		a.planovac.dalsiChybejici(c);
		naEdt(() -> a.model.setJenKandidatiPro(c));
		final long definic = pocet(a, ChallengeZmenenyEvent.Druh.DEFINICE);
		final long aktivni = pocet(a, ChallengeZmenenyEvent.Druh.AKTIVNI);
		final long filtr = pocet(a, ChallengeZmenenyEvent.Druh.JEN_KANDIDATI);

		naEdt(() -> a.model.smazVlastni(c));
		Assert.assertNull(a.model.vysledek(c));
		Assert.assertNull(a.model.vybranyKandidat(c));
		Assert.assertNull("filtr kandidátů se vypne", a.model.jenKandidatiPro());
		Assert.assertTrue(a.model.aktivni().isEmpty());
		Assert.assertEquals(definic + 1, pocet(a, ChallengeZmenenyEvent.Druh.DEFINICE));
		Assert.assertEquals("mazání aktivní hlásí i změnu aktivních", aktivni + 1, pocet(a, ChallengeZmenenyEvent.Druh.AKTIVNI));
		Assert.assertEquals(filtr + 1, pocet(a, ChallengeZmenenyEvent.Druh.JEN_KANDIDATI));
		final Instance b = spust();
		Assert.assertTrue(b.model.vlastni().isEmpty());
		Assert.assertTrue(b.model.aktivni().isEmpty());
	}

	@Test
	public void smazaniNeaktivniChallengeNehlasiZmenuAktivnich() throws Exception {
		final Instance a = spust();
		final Challenge c = a.model.ulozVlastni(new Challenge("Neaktivní", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		final long aktivni = pocet(a, ChallengeZmenenyEvent.Druh.AKTIVNI);
		naEdt(() -> a.model.smazVlastni(c));
		Assert.assertEquals(aktivni, pocet(a, ChallengeZmenenyEvent.Druh.AKTIVNI));
		Assert.assertTrue(a.model.vlastni().isEmpty());
	}

	// ---- Život D/T matice

	@Test
	public void dtMaticeProjdeZivotemSVyberemDalsihoAPredchozihoPolickaPresRestart() throws Exception {
		final Instance a = spust();
		final Challenge c = a.model.ulozVlastni(new Challenge("Matice", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		naEdt(() -> {
			a.model.aktivuj(c);
			a.model.setZapnuto(true);
		});
		a.planovac.keseNacteny(dtKese());
		pockej(() -> a.model.vysledekPolicek(c) != null, "výsledek D/T");
		final VysledekPolicek v = a.model.vysledekPolicek(c);
		Assert.assertEquals(81, v.celkem());
		Assert.assertEquals(54, v.pocetChybejicich());
		Assert.assertEquals(3, v.kandidati().size());

		Assert.assertNull("před prvním výběrem není předchozí", a.planovac.predchoziChybejici(c));
		final VybranyKandidat prvni = a.planovac.dalsiChybejici(c);
		final VybranyKandidat druhy = a.planovac.dalsiChybejici(c);
		Assert.assertNotNull(prvni);
		Assert.assertNotEquals(prvni.id(), druhy.id());
		Assert.assertEquals(prvni.id(), a.planovac.predchoziChybejici(c).id());

		final Instance b = spust();
		Assert.assertEquals(Collections.singletonList(c), b.model.aktivni());
		b.planovac.keseNacteny(dtKese());
		pockej(() -> b.model.vysledekPolicek(c) != null, "výsledek D/T po restartu");
		Assert.assertEquals(54, b.model.vysledekPolicek(c).pocetChybejicich());
		Assert.assertEquals("výběr začíná znovu od nejbližšího", prvni.id(), b.planovac.dalsiChybejici(c).id());
	}

	// ---- Odolnost

	@Test
	public void novejsiFormatNastaveniSeNacteJakoVychoziANikdyNeprepise() throws Exception {
		uloziste.put("format", "99");
		uloziste.put("verze", "7");
		uloziste.put("zapnuto", "true");
		uloziste.put("definice.7.0.nazev", "Budoucí");
		uloziste.put("definice.7.0.typ", "BUDOUCI_TYP");
		final String pred = uloziste.m.toString();
		final Instance a = spust();
		Assert.assertFalse(a.model.jeZapnuto());
		Assert.assertTrue(a.model.vlastni().isEmpty());
		final Challenge c = a.model.ulozVlastni(new Challenge("Nová", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		naEdt(() -> {
			a.model.aktivuj(c);
			a.model.setZapnuto(true);
		});
		Assert.assertEquals("novější nastavení zůstalo beze změny", pred, uloziste.m.toString());
	}

	@Test
	public void poskozeneUlozisteDaVychoziStavABezeAnyPadu() throws Exception {
		uloziste.put("verze", "není číslo");
		uloziste.put("zapnuto", "možná");
		uloziste.put("aktivni", "\n\nNeexistuje\n\n");
		uloziste.put("definice.x.y", "nesmysl");
		uloziste.put("definice..", "nic");
		uloziste.put("definice.1.", "nic");
		final Instance a = spust();
		Assert.assertFalse(a.model.jeZapnuto());
		Assert.assertTrue(a.model.vlastni().isEmpty());
		Assert.assertTrue(a.model.aktivni().isEmpty());
		Assert.assertFalse(a.model.sablony().isEmpty());
		final Challenge c = a.model.ulozVlastni(new Challenge("Po opravě", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		Assert.assertEquals("uložení po poškození znovu funguje", Collections.singletonList(c), spust().model.vlastni());
	}

	@Test
	public void ulozisteKteréSelheNaCteniNerozbijeProgramAUkladaniSeVypne() throws Exception {
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				// nic
			}
		});
		final int[] zapisu = { 0 };
		m.setUloziste(() -> new MapoveUloziste() {
			@Override
			public String get(final String klic) {
				throw new IllegalStateException("nečitelné");
			}

			@Override
			public void put(final String klic, final String hodnota) {
				zapisu[0]++;
			}
		});
		m.initAfterInject();
		Assert.assertFalse(m.jeZapnuto());
		m.ulozVlastni(new Challenge("X", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		Assert.assertEquals("po nečitelném nastavení se nic nezapisuje", 0, zapisu[0]);
	}

	@Test
	public void prazdnaDataAChybejiciStredNeshoziPlanovacASdelNeUplne() throws Exception {
		final Instance a = spust();
		final Challenge c = a.model.ulozVlastni(new Challenge("Domov", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		final Challenge dt = a.model.ulozVlastni(new Challenge("Matice", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		naEdt(() -> {
			a.model.aktivuj(c);
			a.model.setZapnuto(true);
		});
		// Prázdný výčet keší: všechno chybí, žádný kandidát.
		a.planovac.keseNacteny(Collections.<VstupKes> emptyList());
		pockej(() -> a.model.vysledek(c) != null, "výsledek pro prázdná data");
		Assert.assertEquals(360, a.model.vysledek(c).chybejici().size());
		Assert.assertTrue(a.model.vysledek(c).vsichniKandidati().isEmpty());
		stredMapy = new Wgs(LAT, LON);
		Assert.assertNull("není kam jít", a.planovac.dalsiChybejici(c));
		Assert.assertNull(a.planovac.predchoziChybejici(c));

		// Střed mapy neznámý (null): výběr nic nevrátí a nespadne.
		a.planovac.keseNacteny(sektoroveKese());
		pockej(() -> a.model.vysledek(c).vsichniKandidati().size() == 6, "výsledek s daty");
		stredMapy = null;
		Assert.assertNull(a.planovac.dalsiChybejici(c));
		Assert.assertNull(a.planovac.predchoziChybejici(c));

		// Domov neznámý (null): challenge se středem v domově hlásí, proč nejde spočítat.
		domov = null;
		a.planovac.domovZmenen();
		pockej(() -> a.model.stavPredpoctu(c).faze() == StavPredpoctu.Faze.CHYBA, "chyba pro neznámý domov");
		Assert.assertNotNull(a.model.stavPredpoctu(c).chyba());
		Assert.assertFalse(a.model.stavPredpoctu(c).chyba().isEmpty());

		// D/T matice na prázdných datech.
		domov = new Wgs(LAT, LON);
		stredMapy = new Wgs(LAT, LON);
		a.planovac.keseNacteny(Collections.<VstupKes> emptyList());
		naEdt(() -> a.model.aktivuj(dt));
		pockej(() -> a.model.vysledekPolicek(dt) != null, "výsledek D/T pro prázdná data");
		Assert.assertEquals(81, a.model.vysledekPolicek(dt).pocetChybejicich());
		stredMapy = new Wgs(LAT, LON);
		Assert.assertNull(a.planovac.dalsiChybejici(dt));
		Assert.assertNull(a.planovac.predchoziChybejici(dt));
		stredMapy = null;
		Assert.assertNull(a.planovac.dalsiChybejici(dt));
	}

	@Test
	public void keseSNeplatnymiSouradnicemNeshodiVypocet() throws Exception {
		final Instance a = spust();
		final Challenge c = a.model.ulozVlastni(new Challenge("Domov", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		naEdt(() -> {
			a.model.aktivuj(c);
			a.model.setZapnuto(true);
		});
		final List<VypocetSektoruTest.Kes> data = new ArrayList<>(sektoroveKese());
		data.add(new VypocetSektoruTest.Kes("NAN", Double.NaN, Double.NaN, true));
		data.add(new VypocetSektoruTest.Kes("MIMO", 95, 200, false));
		data.add(new VypocetSektoruTest.Kes("NEKONECNO", Double.POSITIVE_INFINITY, 14, true));
		a.planovac.keseNacteny(data);
		pockej(() -> a.model.vysledek(c) != null, "výsledek i s vadnými souřadnicemi");
		Assert.assertEquals("platná data se spočítají stejně", 60, a.model.vysledek(c).chybejici().size());
		for (final VysledekSektoru.KandidatKes k : a.model.vysledek(c).vsichniKandidati()) {
			Assert.assertFalse(k.id(), "NAN".equals(k.id()) || "MIMO".equals(k.id()) || "NEKONECNO".equals(k.id()));
		}
	}
}
