package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.ChallengeZmenenyEvent.Druh;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

public class ChallengeModelTest {

	private ChallengeModel model;
	private MapoveUloziste uloziste;
	private final List<ChallengeZmenenyEvent> udalosti = new CopyOnWriteArrayList<>();

	private static Challenge vlastni(final String nazev) {
		return new Challenge(nazev, Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	private static VysledekSektoru vysledek(final int obsazenych) {
		final BitSet b = new BitSet(360);
		b.set(0, obsazenych);
		return new VysledekSektoru(b, new BitSet(360), new VysledekSektoru.Kandidati[360], obsazenych);
	}

	private ChallengeModel novyModel(final MapoveUloziste u) {
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
			}
		});
		m.setUloziste(() -> u);
		return m;
	}

	@Before
	public void setUp() {
		uloziste = new MapoveUloziste();
		model = novyModel(uloziste);
		model.initAfterInject();
		udalosti.clear();
	}

	private static void pockejNaEdt() throws Exception {
		SwingUtilities.invokeAndWait(() -> {});
	}

	private List<Druh> druhy() {
		final List<Druh> d = new ArrayList<>();
		for (final ChallengeZmenenyEvent e : udalosti) {
			d.add(e.druh());
		}
		return d;
	}

	@Test
	public void pocatecniStav() {
		Assert.assertFalse(model.jeZapnuto());
		Assert.assertEquals(4, model.sablony().size());
		Assert.assertEquals(4, model.vsechny().size());
		Assert.assertTrue(model.vlastni().isEmpty());
		Assert.assertTrue(model.aktivni().isEmpty());
		Assert.assertEquals(1, model.strop());
		Assert.assertNull(model.jenKandidatiPro());
		Assert.assertEquals(-1, model.pocetChybejicich(Sablony.praha360()));
		Assert.assertNull(model.vysledek(Sablony.praha360()));
		Assert.assertEquals(StavPredpoctu.Faze.NEBEZI, model.stavPredpoctu(Sablony.praha360()).faze());
	}

	@Test
	public void pocatecniNactenyStavSeOhlasi() {
		final List<ChallengeZmenenyEvent> uvod = new ArrayList<>();
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				uvod.add((ChallengeZmenenyEvent) event);
			}
		});
		m.setUloziste(MapoveUloziste::new);
		m.initAfterInject();
		Assert.assertEquals(1, uvod.size());
		Assert.assertEquals(Druh.NACTENO, uvod.get(0).druh());
	}

	@Test
	public void zapnutiSeUlozeiOhlasi() {
		model.setZapnuto(true);
		Assert.assertTrue(model.jeZapnuto());
		Assert.assertEquals(Collections.singletonList(Druh.ZAPNUTO), druhy());
		Assert.assertEquals("true", uloziste.get("zapnuto"));
		model.setZapnuto(true);
		Assert.assertEquals("beze změny se nic neohlásí", 1, udalosti.size());
	}

	@Test
	public void definiceAAktivniSeUkladajiAPresVyDalsiModelNactou() {
		final Challenge moje = model.ulozVlastni(vlastni("Moje"));
		model.kopieSablony(Sablony.praha360());
		model.aktivuj(Sablony.cesko360());
		model.aktivuj(moje);
		model.setZapnuto(true);
		final ChallengeModel druhy = novyModel(uloziste);
		druhy.initAfterInject();
		Assert.assertTrue(druhy.jeZapnuto());
		Assert.assertEquals(2, druhy.vlastni().size());
		Assert.assertEquals("zapnutí další vypnulo dosavadní", 1, druhy.aktivni().size());
		Assert.assertEquals("Moje", druhy.aktivni().get(0).nazev());
	}

	@Test
	public void udalostiPriZmenachDefinicAAktivnich() {
		final Challenge moje = model.ulozVlastni(vlastni("Moje"));
		model.aktivuj(moje);
		model.aktivuj(moje);
		model.deaktivuj(moje);
		model.deaktivuj(moje);
		Assert.assertEquals(java.util.Arrays.asList(Druh.DEFINICE, Druh.AKTIVNI, Druh.AKTIVNI), druhy());
		Assert.assertEquals("Moje", udalosti.get(0).challenge().nazev());
	}

	@Test
	public void sablonuNejdePrepsatSmazatAniUlozitJakoVlastni() {
		try {
			model.ulozVlastni(vlastni(Sablony.NAZEV_PRAHA));
			Assert.fail();
		} catch (final ChallengeChyba e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("kopii"));
		}
		Assert.assertTrue(udalosti.isEmpty());
		Assert.assertTrue("šablona zůstala", model.jeSablona(Sablony.praha360()));
		Assert.assertTrue(model.vlastni().isEmpty());
		Assert.assertNull("nic se neuložilo", uloziste.get("definice.0.nazev"));
	}

	@Test
	public void zapnutiDalsiChallengeVypneDosavadniSJejimStavemAFiltrem() throws Exception {
		final Challenge a = model.ulozVlastni(vlastni("A"));
		final Challenge b = model.ulozVlastni(vlastni("B"));
		model.aktivuj(a);
		model.nastavVysledek(a, vysledek(100));
		// Výsledek se do modelu zapíše ve vlákně Swingu; než se zapne další challenge, musí tam být.
		SwingUtilities.invokeAndWait(() -> {});
		model.setJenKandidatiPro(a);
		udalosti.clear();
		model.aktivuj(b);
		Assert.assertEquals(java.util.Arrays.asList(b), model.aktivni());
		Assert.assertFalse(model.jeAktivni(a));
		Assert.assertNull("výsledek vypnuté se zahodil", model.vysledek(a));
		Assert.assertNull("filtr patřil vypnuté", model.jenKandidatiPro());
		Assert.assertEquals(java.util.Arrays.asList(Druh.JEN_KANDIDATI, Druh.AKTIVNI, Druh.AKTIVNI), druhy());
		Assert.assertEquals("vypnutá, zapnutá", java.util.Arrays.asList("A", "B"), java.util.Arrays.asList(udalosti.get(1).challenge().nazev(), udalosti.get(2).challenge().nazev()));
		udalosti.clear();
		model.aktivuj(b);
		Assert.assertEquals("zapnutá znovu nic nehlásí", 0, udalosti.size());
		Assert.assertEquals(1, model.aktivni().size());
	}

	@Test
	public void ulozenoViceAktivnichPonechaPrvniAOstatniVypneBezeZtratyDefinic() {
		final Challenge a = model.ulozVlastni(vlastni("A"));
		final Challenge b = model.ulozVlastni(vlastni("B"));
		model.aktivuj(a);
		final String definice = uloziste.get("verze");
		uloziste.put("aktivni", "B\nA\n360° od domova");
		final ChallengeModel druhy = novyModel(uloziste);
		druhy.initAfterInject();
		Assert.assertEquals(java.util.Arrays.asList("B"), java.util.Arrays.asList(druhy.aktivni().get(0).nazev()));
		Assert.assertEquals(1, druhy.aktivni().size());
		Assert.assertEquals("definice zůstaly", 2, druhy.vlastni().size());
		Assert.assertEquals(definice, uloziste.get("verze"));
		Assert.assertEquals("uložení se zatím nepřepsalo", "B\nA\n360° od domova", uloziste.get("aktivni"));
		druhy.setZapnuto(true);
		Assert.assertEquals("po prvním uložení jen jedna", "B", uloziste.get("aktivni"));
	}

	private Challenge dtAktivni(final String nazev) {
		final Challenge c = model.ulozVlastni(new Challenge(nazev, Typ.DT_MATICE, Stred.souradnice(50, 14), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(c);
		return c;
	}

	private static VysledekPolicek policka(final String... chybejici) {
		final List<KandidatPolicka> k = new ArrayList<>();
		for (final String s : chybejici) {
			k.add(new KandidatPolicka("GC_" + s, 50, 14, s, s, null));
		}
		return new VysledekPolicek(81, java.util.Arrays.asList(chybejici), k);
	}

	@Test
	public void vybranePolickoSeVybiraJenZChybejicichAOhlasi() throws Exception {
		final Challenge c = dtAktivni("DT");
		Assert.assertNull(model.vybranePolicko(c));
		model.setVybranePolicko(c, "D1/T1");
		pockejNaEdt();
		Assert.assertNull("bez výsledku nejde vybrat", model.vybranePolicko(c));
		model.nastavVysledekPolicek(c, policka("D1/T1", "D2/T1"));
		pockejNaEdt();
		udalosti.clear();
		model.setVybranePolicko(c, "D4/T4");
		pockejNaEdt();
		Assert.assertNull("splněné nebo neznámé políčko se ignoruje", model.vybranePolicko(c));
		Assert.assertEquals(0, udalosti.size());
		model.setVybranePolicko(c, "D2/T1");
		pockejNaEdt();
		Assert.assertEquals("D2/T1", model.vybranePolicko(c));
		Assert.assertEquals(Collections.singletonList(Druh.POLICKO), druhy());
		model.setVybranePolicko(c, "D2/T1");
		pockejNaEdt();
		Assert.assertEquals("beze změny se neohlásí", 1, udalosti.size());
		model.setVybranePolicko(c, null);
		pockejNaEdt();
		Assert.assertNull(model.vybranePolicko(c));
		Assert.assertEquals(java.util.Arrays.asList(Druh.POLICKO, Druh.POLICKO), druhy());
	}

	@Test
	public void vybranePolickoBezKandidatuSeNevybereAKdyzPrijdeOKandidatyZrusi() throws Exception {
		final Challenge c = dtAktivni("DT");
		final List<KandidatPolicka> k = Collections.singletonList(new KandidatPolicka("GC1", 50, 14, "D1/T1", "D1/T1", null));
		model.nastavVysledekPolicek(c, new VysledekPolicek(81, java.util.Arrays.asList("D1/T1", "D2/T1"), k));
		pockejNaEdt();
		model.setVybranePolicko(c, "D2/T1");
		pockejNaEdt();
		Assert.assertNull("chybí, ale nemá kandidáty", model.vybranePolicko(c));
		model.setVybranePolicko(c, "D1/T1");
		pockejNaEdt();
		Assert.assertEquals("D1/T1", model.vybranePolicko(c));
		udalosti.clear();
		model.nastavVysledekPolicek(c, new VysledekPolicek(81, java.util.Arrays.asList("D1/T1", "D2/T1"), Collections.<KandidatPolicka> emptyList()));
		pockejNaEdt();
		Assert.assertNull("kandidáti zmizeli", model.vybranePolicko(c));
		Assert.assertEquals(java.util.Arrays.asList(Druh.VYSLEDEK, Druh.POLICKO), druhy());
	}

	@Test
	public void vybranePolickoUKalendareA360NicNezmeni() throws Exception {
		final Challenge kalendar = model.ulozVlastni(new Challenge("Kal", Typ.KALENDAR, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(kalendar);
		model.nastavVysledekPolicek(kalendar, policka("14. 3.", "15. 3."));
		pockejNaEdt();
		udalosti.clear();
		model.setVybranePolicko(kalendar, "14. 3.");
		pockejNaEdt();
		Assert.assertNull("kalendář políčka nevybírá", model.vybranePolicko(kalendar));
		Assert.assertEquals(0, udalosti.size());
		final Challenge sektory = model.ulozVlastni(vlastni("Sektory"));
		model.aktivuj(sektory);
		model.nastavVysledekPolicek(sektory, policka("D1/T1"));
		pockejNaEdt();
		udalosti.clear();
		model.setVybranePolicko(sektory, "D1/T1");
		pockejNaEdt();
		Assert.assertNull("360° políčka nemá ani s výsledkem políček", model.vybranePolicko(sektory));
		Assert.assertEquals(0, udalosti.size());
	}

	@Test
	public void vybranePolickoJeJenUMaticeAUAktivni() throws Exception {
		final Challenge sektory = model.ulozVlastni(vlastni("Sektory"));
		model.aktivuj(sektory);
		model.setVybranePolicko(sektory, "D1/T1");
		pockejNaEdt();
		Assert.assertNull("360° políčka nemá", model.vybranePolicko(sektory));
		final Challenge neaktivni = model.ulozVlastni(new Challenge("Nea", Typ.DT_MATICE, Stred.souradnice(50, 14), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.setVybranePolicko(neaktivni, "D1/T1");
		pockejNaEdt();
		Assert.assertNull(model.vybranePolicko(neaktivni));
	}

	@Test
	public void vybranePolickoSeRusiPriZmeneChallengeAKdyzPrestaneChybet() throws Exception {
		Challenge c = dtAktivni("DT");
		model.nastavVysledekPolicek(c, policka("D1/T1", "D2/T1"));
		pockejNaEdt();
		model.setVybranePolicko(c, "D1/T1");
		pockejNaEdt();
		udalosti.clear();
		model.nastavVysledekPolicek(c, policka("D1/T1"));
		pockejNaEdt();
		Assert.assertEquals("pořád chybí, výběr zůstane", "D1/T1", model.vybranePolicko(c));
		Assert.assertEquals(Collections.singletonList(Druh.VYSLEDEK), druhy());
		udalosti.clear();
		model.nastavVysledekPolicek(c, policka("D2/T1"));
		pockejNaEdt();
		Assert.assertNull("políčko se splnilo", model.vybranePolicko(c));
		Assert.assertEquals(java.util.Arrays.asList(Druh.VYSLEDEK, Druh.POLICKO), druhy());
		model.setVybranePolicko(c, "D2/T1");
		pockejNaEdt();
		c = model.ulozVlastni(new Challenge("DT", Typ.DT_MATICE, Stred.souradnice(51, 15), OblastDef.zadna(), ZdrojPravidel.PGC));
		Assert.assertNull("nová definice výběr zruší", model.vybranePolicko(c));
		model.aktivuj(c);
		model.nastavVysledekPolicek(c, policka("D2/T1"));
		pockejNaEdt();
		model.setVybranePolicko(c, "D2/T1");
		pockejNaEdt();
		model.deaktivuj(c);
		Assert.assertNull("vypnutí výběr zruší", model.vybranePolicko(c));
	}

	@Test
	public void vybranePolickoPrezijePrejmenovaniAZrusiSeSmazanim() throws Exception {
		final Challenge c = dtAktivni("DT");
		model.nastavVysledekPolicek(c, policka("D1/T1"));
		pockejNaEdt();
		model.setVybranePolicko(c, "D1/T1");
		pockejNaEdt();
		final Challenge nova = model.prejmenuj(c, "Jiny");
		Assert.assertEquals("D1/T1", model.vybranePolicko(nova));
		Assert.assertNull(model.vybranePolicko(c));
		model.smazVlastni(nova);
		Assert.assertNull(model.vybranePolicko(nova));
	}

	@Test
	public void vyberPolickaZrusiVybranehoKandidataZJinehoPolicka() throws Exception {
		final Challenge c = dtAktivni("DT");
		model.nastavVysledekPolicek(c, policka("D1/T1", "D2/T1"));
		pockejNaEdt();
		model.nastavVybranyKandidat(c, new VybranyKandidat("GC_D1/T1", 50, 14, -1, 10, "D1/T1"));
		pockejNaEdt();
		udalosti.clear();
		model.setVybranePolicko(c, "D1/T1");
		pockejNaEdt();
		Assert.assertNotNull("kandidát je v políčku", model.vybranyKandidat(c));
		model.setVybranePolicko(c, "D2/T1");
		pockejNaEdt();
		Assert.assertNull("kandidát je z jiného políčka", model.vybranyKandidat(c));
		Assert.assertEquals(java.util.Arrays.asList(Druh.POLICKO, Druh.KANDIDAT, Druh.POLICKO), druhy());
	}

	@Test
	public void nactenoNastaveniVyberPolickaVynuluje() throws Exception {
		final Challenge c = dtAktivni("DT");
		model.nastavVysledekPolicek(c, policka("D1/T1"));
		pockejNaEdt();
		model.setVybranePolicko(c, "D1/T1");
		pockejNaEdt();
		model.initAfterInject();
		Assert.assertNull(model.vybranePolicko(c));
	}

	@Test
	public void pocetKandidatuPoPolickach() {
		final List<KandidatPolicka> k = new ArrayList<>();
		k.add(new KandidatPolicka("A", 50, 14, "D1/T1", "D1/T1", null));
		k.add(new KandidatPolicka("B", 50, 14, "D1/T1", "D1/T1", null));
		k.add(new KandidatPolicka("C", 50, 14, "D3/T2", "D3/T2", null));
		final VysledekPolicek v = new VysledekPolicek(81, java.util.Arrays.asList("D1/T1", "D2/T1", "D3/T2"), k);
		Assert.assertEquals(2, v.pocetKandidatu("D1/T1"));
		Assert.assertEquals("chybí, ale nemá kandidáty", 0, v.pocetKandidatu("D2/T1"));
		Assert.assertEquals(1, v.pocetKandidatu("D3/T2"));
		Assert.assertEquals("neznámý štítek", 0, v.pocetKandidatu("D9/T9"));
		Assert.assertEquals("pořadí jako chybějící", java.util.Arrays.asList("D1/T1", "D2/T1", "D3/T2"), new ArrayList<>(v.pocetKandidatuPoPolickach().keySet()));
		Assert.assertSame("neměnná a sdílená", v.pocetKandidatuPoPolickach(), v.pocetKandidatuPoPolickach());
		try {
			v.pocetKandidatuPoPolickach().put("X", 1);
			Assert.fail();
		} catch (final UnsupportedOperationException e) {
			// očekáváno
		}
	}

	@Test
	public void vysledekSeUkladaJenProAktivniAOhlasiVEdt() throws Exception {
		final Challenge moje = model.ulozVlastni(vlastni("Moje"));
		model.nastavVysledek(moje, vysledek(100));
		pockejNaEdt();
		Assert.assertNull("neaktivní se zahodí", model.vysledek(moje));
		model.aktivuj(moje);
		udalosti.clear();
		final AtomicBoolean naEdt = new AtomicBoolean();
		final CountDownLatch ohlaseno = new CountDownLatch(1);
		final ChallengeModel m = new ChallengeModel() {
			@Override
			public void fire(final Event0<?> event) {
				naEdt.set(SwingUtilities.isEventDispatchThread());
				super.fire(event);
				ohlaseno.countDown();
			}
		};
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
			}
		});
		m.setUloziste(() -> uloziste);
		m.initAfterInject();
		udalosti.clear();
		final Challenge a = m.aktivni().get(0);
		Assert.assertFalse("test běží mimo vlákno Swingu", SwingUtilities.isEventDispatchThread());
		m.nastavVysledek(a, vysledek(260));
		Assert.assertTrue(ohlaseno.await(10, TimeUnit.SECONDS));
		pockejNaEdt();
		Assert.assertTrue("událost jde ve vlákně Swingu", naEdt.get());
		Assert.assertEquals(100, m.pocetChybejicich(a));
		Assert.assertEquals(Druh.VYSLEDEK, udalosti.get(udalosti.size() - 1).druh());
	}

	@Test
	public void stavPredpoctuAKandidatSeUkladajiProAktivni() throws Exception {
		final Challenge moje = model.ulozVlastni(vlastni("Moje"));
		model.aktivuj(moje);
		udalosti.clear();
		model.nastavStavPredpoctu(moje, new StavPredpoctu(StavPredpoctu.Faze.POCITA, 5, 10, null));
		model.nastavVybranyKandidat(moje, new VybranyKandidat("GC1", 50.1, 14.2, 30, 1234));
		pockejNaEdt();
		Assert.assertEquals(StavPredpoctu.Faze.POCITA, model.stavPredpoctu(moje).faze());
		Assert.assertEquals("GC1", model.vybranyKandidat(moje).id());
		Assert.assertEquals(30, model.vybranyKandidat(moje).sektor());
		Assert.assertEquals(java.util.Arrays.asList(Druh.PREDPOCET, Druh.KANDIDAT), druhy());
		model.nastavVybranyKandidat(moje, null);
		pockejNaEdt();
		Assert.assertNull(model.vybranyKandidat(moje));
	}

	@Test
	public void vypnutiAMazaniChallengeZahodiVysledekStavAKandidata() throws Exception {
		final Challenge moje = model.ulozVlastni(vlastni("Moje"));
		model.aktivuj(moje);
		model.nastavVysledek(moje, vysledek(10));
		model.nastavStavPredpoctu(moje, new StavPredpoctu(StavPredpoctu.Faze.HOTOVO, 1, 1, null));
		model.nastavVybranyKandidat(moje, new VybranyKandidat("GC1", 1, 1, 1, 1));
		pockejNaEdt();
		model.deaktivuj(moje);
		Assert.assertNull(model.vysledek(moje));
		Assert.assertNull(model.vybranyKandidat(moje));
		Assert.assertEquals(StavPredpoctu.Faze.NEBEZI, model.stavPredpoctu(moje).faze());
		model.aktivuj(moje);
		model.nastavVysledek(moje, vysledek(10));
		pockejNaEdt();
		model.smazVlastni(moje);
		Assert.assertNull(model.vysledek(moje));
		Assert.assertTrue(model.vlastni().isEmpty());
	}

	@Test
	public void upravaDefiniceZahodiStareVysledkyAPrejmenovaniJePrenese() throws Exception {
		final Challenge moje = model.ulozVlastni(vlastni("Moje"));
		model.aktivuj(moje);
		model.nastavVysledek(moje, vysledek(10));
		pockejNaEdt();
		final Challenge prejmenovana = model.prejmenuj(moje, "Nová");
		Assert.assertEquals(350, model.pocetChybejicich(prejmenovana));
		Assert.assertTrue(model.jeAktivni(prejmenovana));
		model.ulozVlastni(new Challenge("Nová", Typ.SEKTORY_360, Stred.souradnice(51, 14), OblastDef.zadna(), ZdrojPravidel.PGC));
		Assert.assertEquals("po úpravě definice výsledek neplatí", -1, model.pocetChybejicich(prejmenovana));
		Assert.assertTrue(model.jeAktivni(prejmenovana));
	}

	@Test
	public void filtrJenKandidatiJeJedenAJenProAktivni() {
		final Challenge a = model.ulozVlastni(vlastni("A"));
		final Challenge b = model.ulozVlastni(vlastni("B"));
		try {
			model.setJenKandidatiPro(a);
			Assert.fail();
		} catch (final ChallengeChyba e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("aktivní"));
		}
		model.aktivuj(a);
		udalosti.clear();
		model.setJenKandidatiPro(a);
		model.setJenKandidatiPro(a);
		Assert.assertEquals("A", model.jenKandidatiPro().nazev());
		try {
			model.setJenKandidatiPro(b);
			Assert.fail();
		} catch (final ChallengeChyba e) {
			Assert.assertEquals("A", model.jenKandidatiPro().nazev());
		}
		model.deaktivuj(a);
		Assert.assertNull("vypnutí challenge vypne i filtr", model.jenKandidatiPro());
		Assert.assertEquals(java.util.Arrays.asList(Druh.JEN_KANDIDATI, Druh.JEN_KANDIDATI, Druh.AKTIVNI), druhy());
		model.setJenKandidatiPro(null);
		Assert.assertEquals("vypnutí vypnutého se neohlásí", 3, udalosti.size());
	}

	@Test
	public void chybaUlozisteNeshodiModel() {
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
			}
		});
		m.setUloziste(() -> {
			throw new IllegalStateException("nastavení nejde otevřít");
		});
		m.initAfterInject();
		Assert.assertFalse(m.jeZapnuto());
		Assert.assertEquals(4, m.vsechny().size());
		m.setZapnuto(true);
		Assert.assertTrue("změna se v paměti projeví i bez uložení", m.jeZapnuto());
	}

	@Test
	public void zmenaProfiluNactouNastaveniZnovu() {
		model.ulozVlastni(vlastni("Moje"));
		model.aktivuj(model.vlastni().get(0));
		final MapoveUloziste jineUloziste = new MapoveUloziste();
		model.setUloziste(() -> jineUloziste);
		udalosti.clear();
		model.onEvent(new cz.geokuk.framework.PreferencesProfileChangedEvent());
		Assert.assertEquals(Collections.singletonList(Druh.NACTENO), druhy());
		Assert.assertTrue(model.vlastni().isEmpty());
		Assert.assertTrue(model.aktivni().isEmpty());
	}

	@Test
	public void novejsiFormatUlozenehoNastaveniSeNeprepisuje() throws Exception {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("format", "2");
		u.put("zapnuto", "true");
		u.put("budouci", "x");
		final ChallengeModel m = novyModel(u);
		m.initAfterInject();
		final Challenge c = m.ulozVlastni(vlastni("Nová"));
		m.aktivuj(c);
		m.setZapnuto(true);
		Assert.assertEquals("2", u.get("format"));
		Assert.assertEquals("x", u.get("budouci"));
		Assert.assertNull(u.get("verze"));
		Assert.assertNull(u.get("aktivni"));
	}

	@Test
	public void vysledekStareDefiniceTehozNazvuSeZahodi() throws Exception {
		final Challenge stara = model.ulozVlastni(vlastni("Moje"));
		model.aktivuj(stara);
		final Challenge nova = model.ulozVlastni(new Challenge("Moje", Typ.SEKTORY_360, Stred.souradnice(51, 15), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.nastavVysledek(stara, vysledek(100));
		pockejNaEdt();
		Assert.assertNull(model.vysledek(nova));
		model.nastavVysledek(nova, vysledek(50));
		pockejNaEdt();
		Assert.assertEquals(50, model.vysledek(nova).pocetObsazenych());
	}

	@Test
	public void chybaPriNacteniNastaveniVypneUkladani() throws Exception {
		final MapoveUloziste u = new MapoveUloziste() {
			boolean cteni = true;

			@Override
			public java.util.Set<String> klice() {
				if (cteni) {
					cteni = false;
					throw new IllegalStateException("poškozené úložiště");
				}
				return super.klice();
			}
		};
		u.put("budouci", "x");
		final ChallengeModel m = novyModel(u);
		m.initAfterInject();
		final Challenge c = m.ulozVlastni(vlastni("Nová"));
		m.aktivuj(c);
		m.setZapnuto(true);
		Assert.assertNull(u.get("verze"));
		Assert.assertNull(u.get("zapnuto"));
		Assert.assertEquals("x", u.get("budouci"));
	}

	@Test
	public void definiceSNeznamymTypemSeUlozenimNesmaze() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("format", "1");
		u.put("verze", "3");
		u.put("definice.3.0.nazev", "Budoucí");
		u.put("definice.3.0.typ", "BUDOUCI_TYP");
		u.put("definice.3.0.zdroj", "PGC");
		u.put("definice.3.0.stred", "DOMOV");
		u.put("definice.3.0.oblast", "ZADNA");
		final java.util.Map<String, String> pred = new java.util.LinkedHashMap<>(u.m);
		final ChallengeModel m = novyModel(u);
		m.initAfterInject();
		m.ulozVlastni(vlastni("Nová"));
		m.setZapnuto(true);
		Assert.assertEquals("uložené nastavení zůstalo beze změny", pred, u.m);
	}
}
