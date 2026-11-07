package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

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
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Štítky keší na mapě: výsledky sektorů a políček i přehled aktivní challenge v modelu. */
public class PrehledChallengeTest {

	private ChallengeModel model;

	@Before
	public void setUp() {
		model = new ChallengeModel();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
	}

	private static void naEdt() throws Exception {
		SwingUtilities.invokeAndWait(() -> {});
	}

	private static Challenge sektory(final String nazev, final Stred stred) {
		return new Challenge(nazev, Typ.SEKTORY_360, stred, OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	/** Sektory 0 až 99 splněné, kandidáti GC1 (sektor 150) a GC2 (sektor 151, nejistý) ve vzdálenostech 1200 a 3400 m. */
	private static VysledekSektoru vysledekSektoru() {
		final BitSet obsazene = new BitSet(360);
		obsazene.set(0, 100);
		final BitSet nejiste = new BitSet(360);
		nejiste.set(151);
		final List<VysledekSektoru.KandidatKes> kandidati = new ArrayList<>();
		kandidati.add(new VysledekSektoru.KandidatKes("GC1", 50.01, 14.0, 150, 1200, false));
		kandidati.add(new VysledekSektoru.KandidatKes("GC2", 50.03, 14.0, 151, 3400, true));
		return new VysledekSektoru(obsazene, nejiste, new VysledekSektoru.Kandidati[360], 100, kandidati);
	}

	@Test
	public void stitekKandidataSektoruMaTextSektorNejistotuAVzdalenost() {
		final VysledekSektoru v = vysledekSektoru();
		final StitekKese a = v.stitek("GC1");
		Assert.assertEquals("150", a.text());
		Assert.assertEquals(150, a.sektor());
		Assert.assertFalse(a.nejisty());
		Assert.assertEquals(1200, a.vzdalenostM(), 0);
		final StitekKese b = v.stitek("GC2");
		Assert.assertEquals("151", b.text());
		Assert.assertTrue(b.nejisty());
		Assert.assertEquals(3400, b.vzdalenostM(), 0);
		Assert.assertNull("není kandidát", v.stitek("GC3"));
		Assert.assertNull(v.stitek(""));
		Assert.assertSame("sdílený objekt bez alokace", a, v.stitek("GC1"));
	}

	@Test
	public void textySektoruJsouCisla0Az359() {
		for (final int s : new int[] { 0, 9, 10, 99, 100, 359 }) {
			Assert.assertEquals(Integer.toString(s), new VysledekSektoru.KandidatKes("X", 0, 0, s).text());
		}
	}

	@Test
	public void spocitanyVysledekMaStitkyKandidatuSVzdalenostiANejistotou() {
		final List<VstupKes> kese = new ArrayList<>();
		kese.add(new VypocetSektoruTest.Kes("N1", 50.0, 14.1, true));
		kese.add(new VypocetSektoruTest.Kes("K1", 50.1, 14.0, false));
		kese.add(new VypocetSektoruTest.Kes("K2", 50.0, 14.1, false));
		final VysledekSektoru v = VypocetSektoru.spocti(ZdrojPravidel.PGC, 50.0, 14.0, null, Collections.<String> emptySet(), kese);
		final StitekKese k1 = v.stitek("K1");
		Assert.assertEquals("keš na sever od středu", 0, k1.sektor());
		Assert.assertEquals(Geodezie.vzdalenostKoule(50.0, 14.0, 50.1, 14.0), k1.vzdalenostM(), 1e-6);
		Assert.assertNull("sektor východ je splněný nálezem N1", v.stitek("K2"));
		Assert.assertNull("nález není kandidát", v.stitek("N1"));
		Assert.assertTrue("sever je u hranice stupně, tedy nejistý", k1.nejisty());
	}

	@Test
	public void stitekPolickaMaTextBezSektoruSVzdalenosti() {
		final List<VstupDT> kese = new ArrayList<>();
		kese.add(PolicekKes.dt("K1", 50.2, 14.0, false, 6, 0));
		kese.add(PolicekKes.dt("K2", 50.1, 14.0, false, 6, 0));
		final VysledekPolicek v = VypocetMatice.spocti(PravidlaTypu.proMatici(), 50.0, 14.0, kese);
		final StitekKese k = v.stitek("K2");
		Assert.assertEquals("D4/T1", k.text());
		Assert.assertEquals(-1, k.sektor());
		Assert.assertFalse(k.nejisty());
		Assert.assertEquals(Geodezie.vzdalenostKoule(50.0, 14.0, 50.1, 14.0), k.vzdalenostM(), 1e-6);
		Assert.assertNotNull(v.stitek("K1"));
		Assert.assertNull(v.stitek("K3"));
		Assert.assertSame(k, v.stitek("K2"));
	}

	@Test
	public void bezPrehleduDokudNeniAktivniVysledek() throws Exception {
		Assert.assertNull("nic není aktivní", model.prehled());
		final Challenge c = model.ulozVlastni(sektory("A", Stred.domov()));
		model.aktivuj(c);
		Assert.assertNull("bez výsledku", model.prehled());
		model.nastavVysledek(c, vysledekSektoru(), 50.0, 14.0);
		naEdt();
		Assert.assertNotNull(model.prehled());
		model.deaktivuj(c);
		Assert.assertNull("po vypnutí", model.prehled());
	}

	@Test
	public void tentyzVysledekDaTentyzPrehledNovyVysledekNovy() throws Exception {
		final Challenge c = model.ulozVlastni(sektory("A", Stred.domov()));
		model.aktivuj(c);
		model.nastavVysledek(c, vysledekSektoru(), 50.0, 14.0);
		naEdt();
		final PrehledChallenge p = model.prehled();
		Assert.assertSame("tentýž výsledek, tentýž objekt", p, model.prehled());
		model.nastavVysledek(c, vysledekSektoru(), 50.0, 14.0);
		naEdt();
		Assert.assertNotSame("nový výsledek, nový přehled", p, model.prehled());
	}

	private static Challenge dt(final String nazev) {
		return new Challenge(nazev, Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	@Test
	public void sektorBoduSedisSVypoctemSektoruNaKouliIElipsoidu() {
		final java.util.Random r = new java.util.Random(42);
		for (final ZdrojPravidel zdroj : new ZdrojPravidel[] { ZdrojPravidel.PGC, ZdrojPravidel.STATOR }) {
			final Challenge c = new Challenge("S", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.zadna(), zdroj);
			final PrehledChallenge p = PrehledChallenge.zeSektoru(c, vysledekSektoru(), 50, 14);
			final VypocetSektoru.Klasifikator klasifikator = VypocetSektoru.primy(zdroj, 50, 14, null);
			for (int i = 0; i < 2000; i++) {
				final double lat = 50 + (r.nextDouble() - 0.5) * (i % 10 == 0 ? 80 : 2);
				final double lon = 14 + (r.nextDouble() - 0.5) * (i % 10 == 0 ? 320 : 2);
				final VypocetSektoru.Klasifikace k = klasifikator.klasifikuj(new VypocetSektoruTest.Kes("X", lat, lon, false));
				Assert.assertTrue(k.jeVOblasti());
				Assert.assertEquals(zdroj + " " + lat + ", " + lon, k.sektor(), p.sektorBodu(lat, lon));
			}
		}
	}

	@Test
	public void sektorBoduVraciMinusJednaKdyzNeniCoSpocitat() {
		final PrehledChallenge p = PrehledChallenge.zeSektoru(sektory("S", Stred.souradnice(50, 14)), vysledekSektoru(), 50, 14);
		Assert.assertEquals("severně od středu", 0, p.sektorBodu(50.1, 14));
		final PrehledChallenge naRovniku = PrehledChallenge.zeSektoru(sektory("R", Stred.souradnice(0, 14)), vysledekSektoru(), 0, 14);
		Assert.assertEquals("východně po rovníku", 90, naRovniku.sektorBodu(0, 14.1));
		Assert.assertEquals("západně po rovníku", 270, naRovniku.sektorBodu(0, 13.9));
		Assert.assertEquals("jižně", 180, p.sektorBodu(49.9, 14));
		Assert.assertEquals("přesně ve středu", -1, p.sektorBodu(50, 14));
		Assert.assertEquals("neplatná šířka", -1, p.sektorBodu(91, 14));
		Assert.assertEquals("neplatná délka", -1, p.sektorBodu(50, 181));
		Assert.assertEquals("NaN", -1, p.sektorBodu(Double.NaN, 14));
		Assert.assertEquals("bez středu", -1, PrehledChallenge.zeSektoru(sektory("S", Stred.domov()), vysledekSektoru(), Double.NaN, Double.NaN).sektorBodu(50.1, 14));
		Assert.assertEquals("střed mimo rozsah", -1, PrehledChallenge.zeSektoru(sektory("S", Stred.domov()), vysledekSektoru(), 95, 14).sektorBodu(50.1, 14));
		Assert.assertEquals("matice sektory nemá", -1, PrehledChallenge.zPolicek(dt("DT"), new VysledekPolicek(81, Collections.<String> emptyList(), Collections.<KandidatPolicka> emptyList()), 50, 14).sektorBodu(50.1, 14));
	}

	@Test
	public void jeVOblastiBezOmezeniVzdyAJinakPodleOblasti() throws Exception {
		Assert.assertTrue(PrehledChallenge.zeSektoru(sektory("S", Stred.domov()), vysledekSektoru(), 50, 14).jeVOblasti(10, 10));
		final Oblast jenSever = new Oblast() {
			@Override
			public boolean obsahuje(final double lat, final double lon) {
				return lat > 50;
			}

			@Override
			public String verze() {
				return "t";
			}
		};
		final PrehledChallenge p = PrehledChallenge.zeSektoru(sektory("S", Stred.domov()), vysledekSektoru(), 50, 14, jenSever);
		Assert.assertTrue(p.jeVOblasti(50.5, 14));
		Assert.assertFalse(p.jeVOblasti(49.5, 14));
		final Challenge c = model.ulozVlastni(sektory("A", Stred.domov()));
		model.aktivuj(c);
		model.nastavVysledek(c, vysledekSektoru(), 50, 14, jenSever);
		naEdt();
		Assert.assertFalse("oblast jde z modelu do přehledu", model.prehled().jeVOblasti(49.5, 14));
		Assert.assertTrue(model.prehled().jeVOblasti(50.5, 14));
		model.nastavVysledek(c, vysledekSektoru(), 50, 14);
		naEdt();
		Assert.assertTrue("nový výsledek bez oblasti ji zruší", model.prehled().jeVOblasti(49.5, 14));
	}

	@Test
	public void stitekAIndexPolickaPodlePulekJenUMatice() {
		final PrehledChallenge p = PrehledChallenge.zPolicek(dt("DT"), new VysledekPolicek(81, Collections.<String> emptyList(), Collections.<KandidatPolicka> emptyList()), 50, 14);
		Assert.assertEquals("D1/T1", p.stitekPolicka(0, 0));
		Assert.assertEquals("D3/T4", p.stitekPolicka(4, 6));
		Assert.assertEquals("D2,5/T3,5", p.stitekPolicka(3, 5));
		Assert.assertEquals("D5/T5", p.stitekPolicka(8, 8));
		Assert.assertSame("stejný objekt, bez alokace", p.stitekPolicka(4, 6), p.stitekPolicka(4, 6));
		Assert.assertEquals(VypocetMatice.stitek(4, 6), p.stitekPolicka(4, 6));
		Assert.assertNull(p.stitekPolicka(-1, 0));
		Assert.assertNull(p.stitekPolicka(0, -1));
		Assert.assertNull(p.stitekPolicka(9, 0));
		Assert.assertNull(p.stitekPolicka(0, 9));
		Assert.assertEquals(0, p.indexPolicka(0, 0));
		Assert.assertEquals(8, p.indexPolicka(0, 8));
		Assert.assertEquals(9, p.indexPolicka(1, 0));
		Assert.assertEquals(80, p.indexPolicka(8, 8));
		Assert.assertEquals(-1, p.indexPolicka(-1, 0));
		Assert.assertEquals(-1, p.indexPolicka(0, -1));
		Assert.assertEquals(-1, p.indexPolicka(9, 0));
		Assert.assertEquals(-1, p.indexPolicka(0, 9));
		final PrehledChallenge sektory = PrehledChallenge.zeSektoru(sektory("S", Stred.domov()), vysledekSektoru(), 50, 14);
		Assert.assertNull("360° políčka nemá", sektory.stitekPolicka(0, 0));
		Assert.assertEquals(-1, sektory.indexPolicka(0, 0));
		final PrehledChallenge kalendar = PrehledChallenge.zPolicek(new Challenge("K", Typ.KALENDAR, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC),
				new VysledekPolicek(365, Collections.<String> emptyList(), Collections.<KandidatPolicka> emptyList()), 50, 14);
		Assert.assertNull("kalendář není matice", kalendar.stitekPolicka(0, 0));
		Assert.assertEquals(-1, kalendar.indexPolicka(0, 0));
	}

	@Test
	public void stitkyVypoctuMaticeSeShodujiSPredpocitanymiPoRozsahu() {
		for (int d = 0; d < 9; d++) {
			for (int t = 0; t < 9; t++) {
				Assert.assertEquals("D" + (d % 2 == 0 ? String.valueOf(1 + d / 2) : (1 + d / 2) + ",5") + "/T" + (t % 2 == 0 ? String.valueOf(1 + t / 2) : (1 + t / 2) + ",5"),
						VypocetMatice.stitek(d, t));
			}
		}
		Assert.assertEquals("mimo rozsah se počítá jako dřív", "D6/T1", VypocetMatice.stitek(10, 0));
	}

	@Test
	public void prehledSektoruMaBarvuSplnenoStredAStitky() throws Exception {
		final Challenge c = model.ulozVlastni(sektory("A", Stred.domov()));
		model.aktivuj(c);
		model.nastavVysledek(c, vysledekSektoru(), 50.0, 14.0);
		naEdt();
		final PrehledChallenge p = model.prehled();
		Assert.assertEquals(c, p.challenge());
		Assert.assertEquals(Typ.SEKTORY_360, p.typ());
		Assert.assertEquals(100, p.splneno());
		Assert.assertEquals(360, p.celkem());
		Assert.assertTrue(p.maStred());
		Assert.assertEquals(50.0, p.stredLat(), 0);
		Assert.assertEquals(14.0, p.stredLon(), 0);
		Assert.assertEquals("domov", p.stredPopis());
		Assert.assertEquals("150", p.stitek("GC1").text());
		Assert.assertNull(p.stitek("GC9"));
		Assert.assertSame("stejný výsledek, stejný přehled", p, model.prehled());
		model.nastavVysledek(c, vysledekSektoru(), 50.0, 14.0);
		naEdt();
		Assert.assertNotSame("nový výsledek, nový přehled", p, model.prehled());
	}

	@Test
	public void popisStreduPodleDruhuStreduAStredMapyUPolicek() throws Exception {
		final Challenge listing = model.ulozVlastni(sektory("L", Stred.listing("GC4PCQB")));
		model.aktivuj(listing);
		model.nastavVysledek(listing, vysledekSektoru(), 49.8, 15.4);
		naEdt();
		Assert.assertEquals("GC4PCQB", model.prehled().stredPopis());
		final Challenge souradnice = model.ulozVlastni(sektory("S", Stred.souradnice(49, 15)));
		model.deaktivuj(listing);
		model.aktivuj(souradnice);
		model.nastavVysledek(souradnice, vysledekSektoru(), 49, 15);
		naEdt();
		Assert.assertEquals("zadané souřadnice", model.prehled().stredPopis());
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.deaktivuj(souradnice);
		model.aktivuj(dt);
		model.nastavVysledekPolicek(dt, new VysledekPolicek(81, Arrays.asList("D1/T1", "D1/T1,5"), Collections.<KandidatPolicka> emptyList()), 50.2, 14.3);
		naEdt();
		final PrehledChallenge p = model.prehled();
		Assert.assertEquals("střed mapy", p.stredPopis());
		Assert.assertEquals(81, p.celkem());
		Assert.assertEquals(79, p.splneno());
		Assert.assertEquals(50.2, p.stredLat(), 0);
		Assert.assertEquals(14.3, p.stredLon(), 0);
	}

	@Test
	public void bezStreduVPrehleduNeniStred() throws Exception {
		final Challenge c = model.ulozVlastni(sektory("A", Stred.domov()));
		model.aktivuj(c);
		model.nastavVysledek(c, vysledekSektoru());
		naEdt();
		final PrehledChallenge p = model.prehled();
		Assert.assertFalse(p.maStred());
		Assert.assertTrue(Double.isNaN(p.stredLat()) && Double.isNaN(p.stredLon()));
		model.nastavVysledek(c, vysledekSektoru(), 50, 14);
		naEdt();
		Assert.assertTrue(model.prehled().maStred());
		model.nastavVysledek(c, vysledekSektoru(), Double.NaN, 14);
		naEdt();
		Assert.assertFalse("chybí-li jedna souřadnice, střed není", model.prehled().maStred());
		model.nastavVysledek(c, vysledekSektoru(), 50, Double.NaN);
		naEdt();
		Assert.assertFalse(model.prehled().maStred());
	}

	@Test
	public void barvaJePodleTypuAVyhrazenaBarvyStavuSeNepouzivaji() {
		Assert.assertEquals(0x7B3FC4, PaletaChallenge.barva(Typ.SEKTORY_360));
		Assert.assertEquals(0x1E6FD9, PaletaChallenge.barva(Typ.DT_MATICE));
		Assert.assertEquals(0xD98200, PaletaChallenge.barva(Typ.KALENDAR));
		for (final Typ typ : Typ.values()) {
			final int rgb = PaletaChallenge.barva(typ);
			final int r = rgb >> 16 & 0xFF;
			final int g = rgb >> 8 & 0xFF;
			final int b = rgb & 0xFF;
			Assert.assertEquals(typ + " bez alfa kanálu", rgb, rgb & 0xFFFFFF);
			Assert.assertFalse(typ + " není zelená", g >= 0x90 && r < 0x80 && b < 0x80);
			Assert.assertFalse(typ + " není červená", r >= 0xC0 && g < 0x60 && b < 0x60);
		}
		final java.util.Set<Integer> ruzne = new java.util.HashSet<>();
		for (final Typ typ : Typ.values()) {
			ruzne.add(PaletaChallenge.barva(typ));
		}
		Assert.assertEquals("každý typ má jinou barvu", Typ.values().length, ruzne.size());
	}

	@Test
	public void barvaPrehleduJePodleTypuChallengeNezavisleNaNazvu() throws Exception {
		final Challenge a = model.ulozVlastni(sektory("A", Stred.domov()));
		model.aktivuj(a);
		model.nastavVysledek(a, vysledekSektoru(), 50, 14);
		naEdt();
		Assert.assertEquals(PaletaChallenge.barva(Typ.SEKTORY_360), model.prehled().barva());
		model.deaktivuj(a);
		final Challenge b = model.ulozVlastni(sektory("Úplně jiný název", Stred.souradnice(1, 2)));
		model.aktivuj(b);
		model.nastavVysledek(b, vysledekSektoru(), 1, 2);
		naEdt();
		Assert.assertEquals("stejný typ, stejná barva", PaletaChallenge.barva(Typ.SEKTORY_360), model.prehled().barva());
		model.deaktivuj(b);
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(dt);
		model.nastavVysledekPolicek(dt, new VysledekPolicek(81, Collections.<String> emptyList(), Collections.<KandidatPolicka> emptyList()), 50, 14);
		naEdt();
		Assert.assertEquals(PaletaChallenge.barva(Typ.DT_MATICE), model.prehled().barva());
	}

	@Test
	public void skrytyKalendarNemaPrehled() throws Exception {
		final Challenge kal = model.ulozVlastni(new Challenge("Kal", Typ.KALENDAR, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.aktivuj(kal);
		model.nastavVysledekPolicek(kal, new VysledekPolicek(365, Collections.<String> emptyList(), Collections.<KandidatPolicka> emptyList()), 50, 14);
		naEdt();
		Assert.assertNull(model.prehled());
		model.setKalendarVUi(true);
		Assert.assertNotNull(model.prehled());
		Assert.assertEquals(365, model.prehled().celkem());
	}
}
