package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.Challenge.ZdrojeNalezu;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Ukládání nastavení modelu: všechny druhy definic, verze sad, nedokončený zápis, novější formát a neznámé klíče. */
public class UlozeniModeluTest {

	private final MapoveUloziste uloziste = new MapoveUloziste();
	private final List<ChallengeZmenenyEvent> udalosti = new ArrayList<>();

	private ChallengeModel model() {
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
			}
		});
		m.setUloziste(() -> uloziste);
		m.initAfterInject();
		return m;
	}

	private static void naEdt(final Runnable r) throws Exception {
		SwingUtilities.invokeAndWait(r);
	}

	private static String osmdesat(final int n) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			b.append('n');
		}
		return b.toString();
	}

	private static long verze(final MapoveUloziste u) {
		return Long.parseLong(u.get("verze"));
	}

	private static int pocetKlicu(final MapoveUloziste u, final String predpona) {
		int n = 0;
		for (final String k : u.m.keySet()) {
			if (k.startsWith(predpona)) {
				n++;
			}
		}
		return n;
	}

	private static Challenge definice(final String nazev) {
		return new Challenge(nazev, Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	@Test
	public void kazdaKombinaceDefiniceProjdeUlozenimBezeZtratyVcetneNazvuASouradnic() {
		final ChallengeModel a = model();
		final List<Challenge> vsechny = new ArrayList<>();
		final Stred[] stredy = { Stred.domov(), Stred.souradnice(50.123456789012345, 14.987654321098765), Stred.souradnice(-90, 180), Stred.souradnice(90, -180), Stred.souradnice(1e-300, -1e-300),
				Stred.souradnice(0, 0), Stred.listing("GC1ABC") };
		final OblastDef[] oblasti = { OblastDef.zadna(), OblastDef.stat("stat-cz"), OblastDef.sada("praha"), OblastDef.sada("okres-a", "okres-b", "okres-c") };
		final String[] nazvy = { "Jednoduchý", "Ěščřžýáíé Ďťň ů", "S mezerami   uvnitř", "Emoji 😀 a čínsky 汉字", "Čárka, tečka. a lomítko/", "a=b;c:d", osmdesat(70) };
		int i = 0;
		for (final Typ typ : Typ.values()) {
			for (final ZdrojPravidel zdroj : ZdrojPravidel.values()) {
				for (final ZdrojeNalezu nalezy : ZdrojeNalezu.values()) {
					final Challenge c = new Challenge(nazvy[i % nazvy.length] + " " + i, typ, stredy[i % stredy.length], oblasti[(i / 2) % oblasti.length], zdroj, nalezy);
					vsechny.add(a.ulozVlastni(c));
					i++;
				}
			}
		}
		final ChallengeModel b = model();
		Assert.assertEquals(vsechny, b.vlastni());
		for (final Challenge c : vsechny) {
			Assert.assertEquals(c.naMapu(), b.vlastni().get(vsechny.indexOf(c)).naMapu());
		}
	}

	@Test
	public void nazevSOsmdesatiZnakyProjdeAOsmdesatJedenAZnakyRizeniNe() {
		final ChallengeModel m = model();
		final StringBuilder osmdesat = new StringBuilder();
		for (int i = 0; i < ChallengeSeznam.NEJDELSI_NAZEV; i++) {
			osmdesat.append('n');
		}
		Assert.assertEquals(osmdesat.toString(), m.ulozVlastni(definice(osmdesat.toString())).nazev());
		for (final String zly : new String[] { osmdesat + "x", "", "   ", "a\nb", "a\u0000b", "tab\tznak", "\u0007" }) {
			try {
				m.ulozVlastni(definice(zly));
				Assert.fail("[" + zly + "]");
			} catch (final ChallengeChyba e) {
				// očekávané
			}
		}
		Assert.assertEquals(1, m.vlastni().size());
	}

	@Test
	public void sedmdesatPetUlozeniNerostePocetKlicuAVerzeStoupaOJedna() {
		final ChallengeModel m = model();
		final Challenge c = m.ulozVlastni(definice("Stále stejná"));
		final long v0 = verze(uloziste);
		final int klicu = uloziste.m.size();
		for (int i = 1; i <= 75; i++) {
			m.ulozVlastni(definice("Stále stejná"));
			Assert.assertEquals(v0 + i, verze(uloziste));
			Assert.assertEquals("klíčů je pořád stejně: " + uloziste.m.keySet(), klicu, uloziste.m.size());
		}
		Assert.assertEquals(Collections.singletonList(c), model().vlastni());
	}

	@Test
	public void sadaZNepodareneHlaseniVerzeSeIgnorujeAPriDalsimUlozeniSeUklidi() {
		final ChallengeModel a = model();
		a.ulozVlastni(definice("Prvni"));
		final long v = verze(uloziste);
		// Zápis nové sady se zastavil před přepnutím klíče verze: v úložišti zůstala osiřelá sada s vyšší verzí.
		uloziste.put("definice." + (v + 1) + ".0.nazev", "Sirotek");
		uloziste.put("definice." + (v + 1) + ".0.typ", "SEKTORY_360");
		uloziste.put("definice." + (v + 1) + ".0.zdroj", "PGC");
		uloziste.put("definice." + (v + 1) + ".0.stred", "DOMOV");
		uloziste.put("definice." + (v + 1) + ".0.oblast", "ZADNA");
		final ChallengeModel b = model();
		Assert.assertEquals("platí sada podle klíče verze", Collections.singletonList("Prvni"), nazvy(b.vlastni()));
		b.ulozVlastni(definice("Druha"));
		Assert.assertEquals("nová sada je nad osiřelou", v + 2, verze(uloziste));
		Assert.assertEquals(0, pocetKlicu(uloziste, "definice." + (v + 1) + "."));
		Assert.assertEquals(0, pocetKlicu(uloziste, "definice." + v + "."));
		Assert.assertEquals(Arrays_asList("Prvni", "Druha"), nazvy(model().vlastni()));
	}

	private static List<String> Arrays_asList(final String... s) {
		return java.util.Arrays.asList(s);
	}

	private static List<String> nazvy(final List<Challenge> v) {
		final List<String> n = new ArrayList<>();
		for (final Challenge c : v) {
			n.add(c.nazev());
		}
		return n;
	}

	@Test
	public void uloziste_kterePadaPriPutVerzeNechaPuvodniSaduAModelDalFunguje() throws Exception {
		final boolean[] padat = { false };
		final MapoveUloziste vadne = new MapoveUloziste() {
			@Override
			public void put(final String klic, final String hodnota) {
				if (padat[0] && klic.equals("verze")) {
					throw new IllegalStateException("disk plný");
				}
				super.put(klic, hodnota);
			}
		};
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
			}
		});
		m.setUloziste(() -> vadne);
		m.initAfterInject();
		final Challenge prvni = m.ulozVlastni(definice("Prvni"));
		final long v = verze(vadne);
		padat[0] = true;
		final int udalostiPred = udalosti.size();
		final Challenge druha = m.ulozVlastni(definice("Druha"));
		Assert.assertEquals("v paměti je změna vidět", Arrays_asList("Prvni", "Druha"), nazvy(m.vlastni()));
		Assert.assertTrue("změna se ohlásila i přes selhání zápisu", udalosti.size() > udalostiPred);
		Assert.assertEquals("klíč verze zůstal", v, verze(vadne));
		final ChallengeModel po = new ChallengeModel();
		po.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				// nic
			}
		});
		po.setUloziste(() -> vadne);
		po.initAfterInject();
		Assert.assertEquals("po restartu je původní sada", Collections.singletonList(prvni), po.vlastni());
		padat[0] = false;
		m.ulozVlastni(druha);
		Assert.assertTrue(verze(vadne) > v + 1);
		final ChallengeModel znovu = new ChallengeModel();
		znovu.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				// nic
			}
		});
		znovu.setUloziste(() -> vadne);
		znovu.initAfterInject();
		Assert.assertEquals(Arrays_asList("Prvni", "Druha"), nazvy(znovu.vlastni()));
	}

	@Test
	public void neznameKlicePatriciDefiniciSeIgnorujiAnizDefiniceNeprijdou() {
		final ChallengeModel a = model();
		a.ulozVlastni(definice("Prvni"));
		final long v = verze(uloziste);
		uloziste.put("definice." + v + ".0.novaVlastnostZNovejsiVerze", "hodnota");
		uloziste.put("definice." + v + ".0.stred.neco", "x");
		uloziste.put("nejakyJinyKlic", "zůstane");
		final ChallengeModel b = model();
		Assert.assertEquals(Collections.singletonList(definice("Prvni")), b.vlastni());
		b.ulozVlastni(definice("Druha"));
		Assert.assertEquals("klíč mimo definice zůstane", "zůstane", uloziste.get("nejakyJinyKlic"));
		Assert.assertEquals(Arrays_asList("Prvni", "Druha"), nazvy(model().vlastni()));
	}

	@Test
	public void novejsiFormatBlokujeUkladaniAleNicSeNesmaze() throws Exception {
		final ChallengeModel a = model();
		final Challenge c = a.ulozVlastni(definice("Prvni"));
		naEdt(() -> {
			a.aktivuj(c);
			a.setZapnuto(true);
		});
		uloziste.put("format", "2");
		final String pred = uloziste.m.toString();
		final ChallengeModel b = model();
		Assert.assertTrue(b.vlastni().isEmpty());
		Assert.assertFalse(b.jeZapnuto());
		b.ulozVlastni(definice("Nova"));
		final Challenge nova = b.vlastni().get(0);
		naEdt(() -> {
			b.aktivuj(nova);
			b.setZapnuto(true);
			b.deaktivuj(nova);
			b.smazVlastni(nova);
		});
		Assert.assertEquals(pred, uloziste.m.toString());
		uloziste.put("format", "1");
		Assert.assertEquals("po návratu starého formátu se data zase čtou", Collections.singletonList(c), model().vlastni());
	}

	@Test
	public void ulozeniPredOhlasenimZmenyKazdaZmenaOhlasiPresneJednuUdalostSpravnehoDruhu() throws Exception {
		final List<Long> verzePriUdalosti = new ArrayList<>();
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
				verzePriUdalosti.add(uloziste.get("verze") == null ? -1L : verze(uloziste));
			}
		});
		m.setUloziste(() -> uloziste);
		m.initAfterInject();
		udalosti.clear();
		verzePriUdalosti.clear();
		final Challenge c = m.ulozVlastni(definice("A"));
		naEdt(() -> m.aktivuj(c));
		naEdt(() -> m.setZapnuto(true));
		naEdt(() -> m.deaktivuj(c));
		Assert.assertEquals(4, udalosti.size());
		Assert.assertEquals(ChallengeZmenenyEvent.Druh.DEFINICE, udalosti.get(0).druh());
		Assert.assertEquals(ChallengeZmenenyEvent.Druh.AKTIVNI, udalosti.get(1).druh());
		Assert.assertEquals(ChallengeZmenenyEvent.Druh.ZAPNUTO, udalosti.get(2).druh());
		Assert.assertEquals(ChallengeZmenenyEvent.Druh.AKTIVNI, udalosti.get(3).druh());
		for (int i = 0; i < verzePriUdalosti.size(); i++) {
			Assert.assertEquals("při události " + i + " už je změna uložená", (long) (i + 1), (long) verzePriUdalosti.get(i));
		}
	}

	@Test(timeout = 60_000)
	public void petsetVlastnichDefinicProjdeUlozenimARychleSeNacte() {
		final ChallengeModel m = model();
		for (int i = 0; i < 500; i++) {
			m.ulozVlastni(new Challenge("Definice č. " + i, Typ.values()[i % 3], Stred.souradnice(i % 90, i % 180), OblastDef.sada("a" + i, "b" + i), ZdrojPravidel.values()[i % 2]));
		}
		Assert.assertEquals(500, model().vlastni().size());
		final String sada = "definice." + verze(uloziste) + ".";
		for (final String k : uloziste.m.keySet()) {
			Assert.assertTrue("jen aktuální sada: " + k, !k.startsWith("definice.") || k.startsWith(sada));
		}
	}

	@Test
	public void aktivniSeUkladajiVPoradiZapnutiAPoRestartuSeOmezi() throws Exception {
		final ChallengeModel a = model();
		final List<Challenge> c = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			c.add(a.ulozVlastni(definice("C" + i)));
		}
		naEdt(() -> {
			for (int i = 4; i >= 0; i--) {
				a.aktivuj(c.get(i));
			}
		});
		Assert.assertEquals(Arrays_asList("C4\nC3\nC2\nC1\nC0".split("\n")), nazvy(a.aktivni()));
		Assert.assertEquals("C4\nC3\nC2\nC1\nC0", uloziste.get("aktivni"));
		Assert.assertEquals(nazvy(a.aktivni()), nazvy(model().aktivni()));
		final Set<String> pred = uloziste.klice();
		Assert.assertTrue(pred.contains("aktivni"));
		naEdt(() -> {
			for (final Challenge d : new ArrayList<>(a.aktivni())) {
				a.deaktivuj(d);
			}
		});
		Assert.assertNull(uloziste.get("aktivni"));
	}

	@Test
	public void nacteniNastaveniZnovuVymazeVysledkyStavyKandidataFiltrAPruhAOhlasiNacteno() throws Exception {
		final ChallengeModel m = model();
		final Challenge c = m.ulozVlastni(definice("A"));
		naEdt(() -> {
			m.aktivuj(c);
			m.setZapnuto(true);
			m.nastavVysledek(c, new VysledekSektoru(new java.util.BitSet(360), new java.util.BitSet(360), new VysledekSektoru.Kandidati[360], 0));
			m.nastavVysledekPolicek(c, new VysledekPolicek(81, Collections.<String> emptyList(), Collections.<KandidatPolicka> emptyList()));
			m.nastavStavPredpoctu(c, new StavPredpoctu(StavPredpoctu.Faze.HOTOVO, 1, 1, null));
			m.nastavVybranyKandidat(c, new VybranyKandidat("GC1", 50, 14, 5, 100, "x"));
			m.setZobrazena(c);
			m.setJenKandidatiPro(c);
		});
		Assert.assertNotNull(m.vysledek(c));
		Assert.assertNotNull(m.vysledekPolicek(c));
		Assert.assertNotNull(m.vybranyKandidat(c));
		Assert.assertEquals(c, m.jenKandidatiPro());
		udalosti.clear();
		m.reloadPreferences();
		Assert.assertNull(m.vysledek(c));
		Assert.assertNull(m.vysledekPolicek(c));
		Assert.assertNull(m.vybranyKandidat(c));
		Assert.assertNull(m.jenKandidatiPro());
		Assert.assertEquals("stav předpočtu se po načtení nepamatuje", StavPredpoctu.Faze.NEBEZI, m.stavPredpoctu(c).faze());
		Assert.assertEquals("aktivní a zapnuto zůstávají z úložiště", c, m.zobrazena());
		Assert.assertTrue(m.jeZapnuto());
		Assert.assertEquals(1, udalosti.size());
		Assert.assertEquals(ChallengeZmenenyEvent.Druh.NACTENO, udalosti.get(0).druh());
	}

	@Test
	public void prejmenovaniPrenesVysledekPolickaAStavPredpoctu() throws Exception {
		final ChallengeModel m = model();
		final Challenge c = m.ulozVlastni(new Challenge("Matice", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		final VysledekPolicek v = new VysledekPolicek(81, Collections.singletonList("D1/T1"), Collections.<KandidatPolicka> emptyList());
		final StavPredpoctu stav = new StavPredpoctu(StavPredpoctu.Faze.HOTOVO, 3, 3, null);
		naEdt(() -> {
			m.aktivuj(c);
			m.nastavVysledekPolicek(c, v);
			m.nastavStavPredpoctu(c, stav);
		});
		final Challenge nova = m.prejmenuj(c, "Přejmenovaná matice");
		Assert.assertSame(v, m.vysledekPolicek(nova));
		Assert.assertSame(stav, m.stavPredpoctu(nova));
		Assert.assertNull("pod starým názvem nic nezůstalo", m.vysledekPolicek(c));
	}

	@Test
	public void challengeSeRovnaPodleVsechPoliAMaStejnyHashAPopis() {
		final Challenge zaklad = new Challenge("Název", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.sada("a", "b"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE);
		final Challenge stejna = new Challenge("Název", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.sada("a", "b"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals(zaklad, stejna);
		Assert.assertEquals(zaklad.hashCode(), stejna.hashCode());
		Assert.assertTrue(zaklad.toString(), zaklad.toString().startsWith("Název "));
		final Challenge[] jine = { new Challenge("Jiný", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.sada("a", "b"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE),
				new Challenge("Název", Typ.DT_MATICE, Stred.souradnice(50, 14), OblastDef.sada("a", "b"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE),
				new Challenge("Název", Typ.SEKTORY_360, Stred.souradnice(50, 15), OblastDef.sada("a", "b"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE),
				new Challenge("Název", Typ.SEKTORY_360, Stred.souradnice(51, 14), OblastDef.sada("a", "b"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE),
				new Challenge("Název", Typ.SEKTORY_360, Stred.listing("GC1"), OblastDef.sada("a", "b"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE),
				new Challenge("Název", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.sada("a", "c"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE),
				new Challenge("Název", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.stat("a"), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE),
				new Challenge("Název", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.sada("a", "b"), ZdrojPravidel.STATOR, ZdrojeNalezu.ZAPNUTE),
				new Challenge("Název", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.sada("a", "b"), ZdrojPravidel.PGC, ZdrojeNalezu.VSECHNY) };
		for (final Challenge j : jine) {
			Assert.assertNotEquals(j.toString(), zaklad, j);
			Assert.assertNotEquals(j.toString(), zaklad.hashCode(), j.hashCode());
		}
		Assert.assertNotEquals(zaklad, "Název");
		Assert.assertEquals(Stred.listing("GC1"), Stred.listing("GC1"));
		Assert.assertNotEquals(Stred.listing("GC1"), Stred.listing("GC2"));
		Assert.assertNotEquals(Stred.domov(), Stred.listing("GC1"));
		Assert.assertEquals(OblastDef.sada("a", "b"), OblastDef.sada("a", "b"));
		Assert.assertEquals(OblastDef.sada("a", "b").hashCode(), OblastDef.sada("a", "b").hashCode());
		Assert.assertNotEquals(OblastDef.sada("a", "b"), OblastDef.sada("a"));
		Assert.assertNotEquals(OblastDef.zadna(), OblastDef.stat("a"));
		Assert.assertNotEquals(OblastDef.sada("a", "b").hashCode(), OblastDef.sada("a", "c").hashCode());
	}

	@Test
	public void ulozisteVPreferencichMaStejneChovaniJakoMapaVcetneNejdelsichHodnotAKlicu() throws Exception {
		final cz.geokuk.framework.MyPreferences pref = cz.geokuk.framework.MyPreferences.current().node("test-challenge-uloziste");
		try {
			final PreferenceUloziste u = new PreferenceUloziste(pref);
			Assert.assertTrue(u.klice().isEmpty());
			Assert.assertNull(u.get("neni"));
			final StringBuilder dlouha = new StringBuilder();
			for (int i = 0; i < ChallengeSeznam.NEJDELSI_HODNOTA; i++) {
				dlouha.append('ž');
			}
			final StringBuilder klic = new StringBuilder();
			for (int i = 0; i < 80; i++) {
				klic.append('k');
			}
			u.put("a", "1");
			u.put(klic.toString(), dlouha.toString());
			Assert.assertEquals("1", u.get("a"));
			Assert.assertEquals(dlouha.toString(), u.get(klic.toString()));
			Assert.assertEquals(new java.util.HashSet<>(java.util.Arrays.asList("a", klic.toString())), u.klice());
			u.remove("a");
			u.remove("neexistuje");
			Assert.assertNull(u.get("a"));
			Assert.assertEquals(Collections.singleton(klic.toString()), u.klice());

			final ChallengeSeznam s = new ChallengeSeznam();
			s.ulozVlastni(new Challenge("Z prefs 😀", Typ.KALENDAR, Stred.souradnice(50.123456789012345, 14.5), OblastDef.sada("a", "b"), ZdrojPravidel.STATOR));
			NastaveniChallenge.uloz(s, true, u);
			final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
			Assert.assertTrue(n.zapnuto);
			Assert.assertEquals(s.vlastni(), n.seznam.vlastni());
			Assert.assertEquals(0, n.preskoceno);
		} finally {
			pref.removeNode();
		}
	}
}
