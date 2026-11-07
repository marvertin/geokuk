package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

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

/**
 * Uložení z beta.32 (až pět aktivních challenge, původní názvy šablon, Kalendář v nabídce) se po přechodu na jednu aktivní a skrytý Kalendář načte bez ztráty definic a
 * bez pádu; formát uložení se nemění, aby ho četl i starší program.
 */
public class MigraceUlozeniTest {

	private static final String STARY_PRAHA = "360° of Prague";
	private static final String STARY_CESKO = "360° of the Czech Republic";

	private final MapoveUloziste uloziste = new MapoveUloziste();

	private ChallengeModel model() {
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				// události tu nejsou důležité
			}
		});
		m.setUloziste(() -> uloziste);
		m.initAfterInject();
		return m;
	}

	/** Uložení, jak ho zapsala beta.32: až pět aktivních, vlastní definice včetně kalendáře a matice, názvy šablon bez GC kódů. */
	private void beta32(final String aktivni, final boolean zapnuto) {
		uloziste.put("format", "1");
		uloziste.put("verze", "7");
		uloziste.put("zapnuto", Boolean.toString(zapnuto));
		if (aktivni != null) {
			uloziste.put("aktivni", aktivni);
		}
		definice(0, "Můj kalendář", Typ.KALENDAR, "DOMOV", null);
		definice(1, "Moje matice", Typ.DT_MATICE, "DOMOV", null);
		definice(2, "Moje sektory", Typ.SEKTORY_360, "SOURADNICE", new String[] { "50.1", "14.2" });
	}

	private void definice(final int i, final String nazev, final Typ typ, final String stred, final String[] souradnice) {
		final String p = "definice.7." + i + ".";
		uloziste.put(p + "nazev", nazev);
		uloziste.put(p + "typ", typ.name());
		uloziste.put(p + "zdroj", "PGC");
		uloziste.put(p + "nalezy", "ZAPNUTE");
		uloziste.put(p + "stred", stred);
		if (souradnice != null) {
			uloziste.put(p + "stred.lat", souradnice[0]);
			uloziste.put(p + "stred.lon", souradnice[1]);
		}
		uloziste.put(p + "oblast", "ZADNA");
	}

	private static java.util.List<String> nazvy(final java.util.List<Challenge> v) {
		final java.util.List<String> n = new java.util.ArrayList<>();
		for (final Challenge c : v) {
			n.add(c.nazev());
		}
		return n;
	}

	private Map<String, String> kopieUloziste() {
		return new TreeMap<>(uloziste.m);
	}

	// ---- Více aktivních → jedna

	@Test
	public void viceAktivnichZBeta32ZapneJenPrvniNabizenouAOstatniDefiniceZustanou() {
		beta32(STARY_CESKO + "\nMoje matice\nMoje sektory\n" + STARY_PRAHA, true);
		final ChallengeModel m = model();
		Assert.assertEquals("zapnuto z uložení", true, m.jeZapnuto());
		Assert.assertEquals(Collections.singletonList(Sablony.NAZEV_CESKO), nazvy(m.aktivni()));
		Assert.assertEquals("definice se neztratily", Arrays.asList("Můj kalendář", "Moje matice", "Moje sektory"), nazvy(m.vlastni()));
		Assert.assertEquals(Sablony.NAZEV_CESKO, m.zobrazena().nazev());
		Assert.assertEquals("pruh ukazuje první aktivní", Sablony.NAZEV_CESKO, m.stavPruhu().challenge().nazev());
	}

	@Test
	public void pouhaNacteniNicVUlozistiNezmeni() {
		beta32(STARY_PRAHA + "\nMoje matice\nMůj kalendář", true);
		final Map<String, String> pred = kopieUloziste();
		final ChallengeModel m = model();
		Assert.assertEquals(1, m.aktivni().size());
		Assert.assertEquals("načtení nepřepisuje uložení z beta.32", pred, kopieUloziste());
	}

	@Test
	public void prvniUlozenaZmenaPoMigraciZapiseJednuAktivniASaduDefinicBezeZtraty() {
		beta32(STARY_PRAHA + "\nMoje matice\nMůj kalendář", true);
		final ChallengeModel m = model();
		m.setKresliSektory(false);
		Assert.assertEquals("přepínač se uložil", "false", uloziste.get("kresli.sektory"));
		final Challenge sektory = m.vlastni().get(2);
		m.ulozVlastni(sektory);
		Assert.assertEquals("jediná aktivní s novým názvem šablony", Sablony.NAZEV_PRAHA, uloziste.get("aktivni"));
		Assert.assertEquals("true", uloziste.get("zapnuto"));
		Assert.assertEquals(8, Long.parseLong(uloziste.get("verze")));
		final ChallengeModel znovu = model();
		Assert.assertEquals(Arrays.asList("Můj kalendář", "Moje matice", "Moje sektory"), nazvy(znovu.vlastni()));
		Assert.assertEquals(Collections.singletonList(Sablony.NAZEV_PRAHA), nazvy(znovu.aktivni()));
		Assert.assertFalse(znovu.isKresliSektory());
	}

	// ---- Kalendář skrytý

	@Test
	public void kalendarMeziUlozenymiAktivnimiSePreskociAZapneSeDalsiNabizena() {
		beta32("Můj kalendář\nMoje matice\n" + STARY_PRAHA, true);
		final ChallengeModel m = model();
		Assert.assertEquals(Collections.singletonList("Moje matice"), nazvy(m.aktivni()));
		Assert.assertEquals("Moje matice", m.zobrazena().nazev());
	}

	@Test
	public void jenKalendarJeUlozenyJakoAktivniNicSeNezapneAPruhJeSkryty() {
		beta32("Můj kalendář", true);
		final ChallengeModel m = model();
		Assert.assertTrue(m.jeZapnuto());
		Assert.assertTrue(m.aktivni().isEmpty());
		Assert.assertNull(m.zobrazena());
		Assert.assertSame(StavPruhu.SKRYTY, m.stavPruhu());
		Assert.assertEquals("definice kalendáře zůstala", "Můj kalendář", m.vlastni().get(0).nazev());
		Assert.assertFalse(m.jePruhViditelny());
	}

	@Test
	public void kalendarSeDaZapnoutJenVTestuADefiniceJeStaleVSeznamu() throws Exception {
		beta32("Můj kalendář", true);
		final ChallengeModel m = model();
		final Challenge kal = m.vlastni().get(0);
		Assert.assertFalse(m.jeVUi(kal));
		m.setKalendarVUi(true);
		Assert.assertTrue(m.jeVUi(kal));
		javax.swing.SwingUtilities.invokeAndWait(() -> m.aktivuj(kal));
		Assert.assertEquals(Collections.singletonList(kal), m.aktivni());
		Assert.assertEquals(kal, m.zobrazena());
	}

	@Test
	public void kalendarAktivniVModeluAPotomSkrytySeVPruhuNeukazeAProhodiSeZaDalsiAktivni() throws Exception {
		beta32(STARY_PRAHA, true);
		final ChallengeModel m = model();
		m.setKalendarVUi(true);
		final Challenge kal = m.vlastni().get(0);
		javax.swing.SwingUtilities.invokeAndWait(() -> m.aktivuj(kal));
		Assert.assertEquals("zapnutí kalendáře vypnulo dosavadní", Collections.singletonList(kal), m.aktivni());
		Assert.assertEquals(kal, m.zobrazena());
		m.setKalendarVUi(false);
		Assert.assertNull("skrytý kalendář nemůže být v pruhu", m.zobrazena());
		Assert.assertSame(StavPruhu.SKRYTY, m.stavPruhu());
	}

	// ---- Šablony a názvy

	@Test
	public void obaStareNazvySablonSeNactouPodNovymiAKopieSeStarymNazvemZustane() {
		beta32(STARY_PRAHA, false);
		definice(3, STARY_PRAHA + " (vlastní)", Typ.SEKTORY_360, "DOMOV", null);
		final ChallengeModel m = model();
		Assert.assertEquals(Collections.singletonList(Sablony.NAZEV_PRAHA), nazvy(m.aktivni()));
		Assert.assertTrue(nazvy(m.vlastni()).contains(STARY_PRAHA + " (vlastní)"));
		Assert.assertFalse("starý název už není šablonou", m.sablony().stream().anyMatch(s -> s.nazev().equals(STARY_PRAHA)));
		Assert.assertFalse(m.jeZapnuto());
	}

	// ---- Neznámé hodnoty

	@Test
	public void definiceSTypemZBudouciVerzeNeniVSeznamuAUlozeniSeVypneAbyPrepsaniNepripravilo() {
		beta32(STARY_PRAHA, true);
		definice(3, "Z budoucna", Typ.SEKTORY_360, "DOMOV", null);
		uloziste.put("definice.7.3.typ", "TYP_Z_BUDOUCNA");
		final Map<String, String> pred = kopieUloziste();
		final ChallengeModel m = model();
		Assert.assertFalse(nazvy(m.vlastni()).contains("Z budoucna"));
		Assert.assertEquals(3, m.vlastni().size());
		m.setKresliStitky(false);
		m.ulozVlastni(m.vlastni().get(0));
		Assert.assertEquals("uložení s definicí, které program nerozumí, se nepřepisuje", pred, kopieUloziste());
	}

	@Test
	public void neznamaHodnotaVAktivnichNaModeluNicNeshodiAZnamaSeZapne() {
		beta32("Neexistuje\n\nTaké ne\n" + STARY_PRAHA, true);
		final ChallengeModel m = model();
		Assert.assertEquals(Collections.singletonList(Sablony.NAZEV_PRAHA), nazvy(m.aktivni()));
	}

	@Test
	public void neplatneHodnotyPrepinacuKresleniAZapnutoVRoceBeta32SeBeruJakoVychozi() {
		beta32(null, true);
		uloziste.put("zapnuto", "možná");
		Assert.assertFalse("neplatné zapnuto = vypnuto", model().jeZapnuto());
		Assert.assertTrue("chybějící přepínače = zapnuto", model().isKresliSektory() && model().isKresliStitky());
		uloziste.put("kresli.sektory", "nesmysl");
		uloziste.put("kresli.stitky", "");
		Assert.assertTrue(model().isKresliSektory());
		Assert.assertTrue(model().isKresliStitky());
		uloziste.put("kresli.sektory", "false");
		Assert.assertFalse(model().isKresliSektory());
	}

	// ---- Downgrade: formát uložení se nemění

	@Test
	public void formatUlozeniJeBezeZmenyAProstejSeMuzeNacistStarsiProgram() {
		final ChallengeModel m = model();
		final Challenge moje = m.ulozVlastni(new Challenge("Moje", Typ.SEKTORY_360, Stred.souradnice(50.5, 14.25), OblastDef.sada("praha"), ZdrojPravidel.STATOR, ZdrojeNalezu.VSECHNY));
		final Challenge kopieMatice = m.ulozVlastni(new Challenge("Matice", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		final Challenge listing = m.ulozVlastni(new Challenge("Listing", Typ.KALENDAR, Stred.listing("GC4PCQB"), OblastDef.stat("stat-cz"), ZdrojPravidel.PGC));
		try {
			javax.swing.SwingUtilities.invokeAndWait(() -> {
				m.aktivuj(moje);
				m.setZapnuto(true);
			});
		} catch (final Exception e) {
			throw new AssertionError(e);
		}
		final long v = Long.parseLong(uloziste.get("verze"));
		final Map<String, String> ocekavano = new LinkedHashMap<>();
		ocekavano.put("format", "1");
		ocekavano.put("verze", Long.toString(v));
		ocekavano.put("zapnuto", "true");
		ocekavano.put("aktivni", "Moje");
		final String p = "definice." + v + ".";
		ocekavano.put(p + "0.nazev", "Moje");
		ocekavano.put(p + "0.typ", "SEKTORY_360");
		ocekavano.put(p + "0.zdroj", "STATOR");
		ocekavano.put(p + "0.nalezy", "VSECHNY");
		ocekavano.put(p + "0.stred", "SOURADNICE");
		ocekavano.put(p + "0.stred.lat", "50.5");
		ocekavano.put(p + "0.stred.lon", "14.25");
		ocekavano.put(p + "0.oblast", "SADA");
		ocekavano.put(p + "0.oblast.id", "praha");
		ocekavano.put(p + "1.nazev", "Matice");
		ocekavano.put(p + "1.typ", "DT_MATICE");
		ocekavano.put(p + "1.zdroj", "PGC");
		ocekavano.put(p + "1.nalezy", "ZAPNUTE");
		ocekavano.put(p + "1.stred", "DOMOV");
		ocekavano.put(p + "1.oblast", "ZADNA");
		ocekavano.put(p + "2.nazev", "Listing");
		ocekavano.put(p + "2.typ", "KALENDAR");
		ocekavano.put(p + "2.zdroj", "PGC");
		ocekavano.put(p + "2.nalezy", "ZAPNUTE");
		ocekavano.put(p + "2.stred", "LISTING");
		ocekavano.put(p + "2.stred.kod", "GC4PCQB");
		ocekavano.put(p + "2.oblast", "STAT");
		ocekavano.put(p + "2.oblast.id", "stat-cz");
		Assert.assertEquals(new TreeMap<>(ocekavano), kopieUloziste());
		Assert.assertNotNull(kopieMatice);
		Assert.assertNotNull(listing);
	}

	@Test
	public void ulozeniNovouVerziProgramuSeCtouJakoUlozeniBeta32SeStrop5() {
		final ChallengeModel m = model();
		final Challenge a = m.ulozVlastni(new Challenge("A", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC));
		try {
			javax.swing.SwingUtilities.invokeAndWait(() -> {
				m.aktivuj(a);
				m.setZapnuto(true);
			});
		} catch (final Exception e) {
			throw new AssertionError(e);
		}
		// Starší program mohl mít až pět aktivních a četl stejný formát: ze stejných klíčů se bez ztráty složí totéž.
		final NastaveniChallenge.Nacteno stary = NastaveniChallenge.nacti(uloziste, c -> true);
		Assert.assertEquals(Collections.singletonList("A"), nazvy(stary.seznam.aktivni()));
		Assert.assertEquals(0, stary.preskoceno);
		Assert.assertTrue(stary.zapnuto);
		Assert.assertFalse(stary.neznamyFormat);
	}
}
