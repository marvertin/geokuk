package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

public class PruhModeluTest {

	private ChallengeModel model;
	private final List<ChallengeZmenenyEvent> udalosti = new CopyOnWriteArrayList<>();

	@Before
	public void setUp() {
		model = new ChallengeModel();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
			}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
		model.setKalendarVUi(true);
		udalosti.clear();
	}

	private static Challenge definice(final String nazev, final Typ typ) {
		return new Challenge(nazev, typ, Stred.souradnice(50, 14), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	private static void flush() throws Exception {
		SwingUtilities.invokeAndWait(() -> {});
	}

	private static VysledekSektoru sektory(final int obsazenych, final VysledekSektoru.Kandidati... kandidati) {
		final BitSet b = new BitSet(360);
		b.set(0, obsazenych);
		final VysledekSektoru.Kandidati[] pole = new VysledekSektoru.Kandidati[360];
		for (final VysledekSektoru.Kandidati k : kandidati) {
			pole[k.sektor()] = k;
		}
		return new VysledekSektoru(b, new BitSet(360), pole, obsazenych);
	}

	private static VysledekSektoru.Kandidati kandidati(final int sektor, final int pocet) {
		final VysledekSektoru.Kandidati k = new VysledekSektoru.Kandidati(sektor);
		for (int i = 0; i < pocet; i++) {
			k.pridej("GC" + sektor + "_" + i, 1000 + i);
		}
		return k;
	}

	private static VysledekPolicek policka(final int chybi) {
		final List<String> stitky = new ArrayList<>();
		for (int i = 0; i < chybi; i++) {
			stitky.add("D" + i + "/T1");
		}
		return new VysledekPolicek(81, stitky, new ArrayList<KandidatPolicka>());
	}

	@Test
	public void bezPlanovaceNeboAktivnichJePruhSkryty() {
		Assert.assertSame(StavPruhu.SKRYTY, model.stavPruhu());
		final Challenge c = model.ulozVlastni(definice("A", Typ.SEKTORY_360));
		model.aktivuj(c);
		Assert.assertSame("vypnutý plánovač", StavPruhu.SKRYTY, model.stavPruhu());
		model.setZapnuto(true);
		Assert.assertTrue(model.stavPruhu().jeViditelny());
		model.deaktivuj(c);
		Assert.assertSame("žádná aktivní", StavPruhu.SKRYTY, model.stavPruhu());
		Assert.assertFalse(StavPruhu.SKRYTY.jeViditelny());
		Assert.assertNull(StavPruhu.SKRYTY.typ());
		Assert.assertEquals("", StavPruhu.SKRYTY.stitekTypu());
	}

	@Test
	public void zobrazenaJeJedinaAktivniAZapnutiDalsiJiMeni() throws Exception {
		final Challenge a = model.ulozVlastni(definice("A", Typ.SEKTORY_360));
		final Challenge b = model.ulozVlastni(definice("B", Typ.DT_MATICE));
		Assert.assertNull(model.zobrazena());
		model.aktivuj(a);
		model.setZapnuto(true);
		Assert.assertEquals("A", model.zobrazena().nazev());
		udalosti.clear();
		model.aktivuj(b);
		Assert.assertEquals("B", model.zobrazena().nazev());
		Assert.assertEquals("D/T", model.stavPruhu().stitekTypu());
		Assert.assertEquals(1, model.stavPruhu().pocetAktivnich());
		model.deaktivuj(b);
		Assert.assertNull("bez aktivní není co zobrazit", model.zobrazena());
	}

	@Test
	public void zobrazitJdeJenAktivniAPrejmenovaniZobrazeneZachova() {
		final Challenge a = model.ulozVlastni(definice("A", Typ.SEKTORY_360));
		final Challenge b = model.ulozVlastni(definice("B", Typ.KALENDAR));
		model.aktivuj(a);
		try {
			model.setZobrazena(b);
			Assert.fail();
		} catch (final ChallengeChyba e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("aktivní"));
		}
		model.aktivuj(b);
		model.setZobrazena(b);
		final Challenge nova = model.prejmenuj(b, "Kalendář 2");
		Assert.assertEquals("Kalendář 2", model.zobrazena().nazev());
		Assert.assertEquals(nova, model.zobrazena());
	}

	@Test
	public void badgeAStitekSektoru() throws Exception {
		final Challenge c = model.ulozVlastni(definice("A", Typ.SEKTORY_360));
		model.aktivuj(c);
		model.setZapnuto(true);
		Assert.assertEquals("dokud není výsledek", -1, model.stavPruhu().badge());
		Assert.assertNull(model.stavPruhu().stitek());
		model.nastavVysledek(c, sektory(300, kandidati(310, 5)));
		model.nastavVybranyKandidat(c, new VybranyKandidat("GC310_0", 50.1, 14.1, 310, 2_840));
		flush();
		final StavPruhu s = model.stavPruhu();
		Assert.assertEquals(60, s.badge());
		Assert.assertEquals("360°", s.stitekTypu());
		Assert.assertEquals("Sektor 310° · 5 kandidátů · 2,8 km", s.stitek());
		Assert.assertEquals("GC310_0", s.kandidat().id());
		Assert.assertFalse(s.jenKandidati());
		Assert.assertNull(s.chyba());
	}

	@Test
	public void badgeAStitekPolickaAKalendare() throws Exception {
		final Challenge dt = model.ulozVlastni(definice("DT", Typ.DT_MATICE));
		model.aktivuj(dt);
		model.setZapnuto(true);
		model.nastavVysledekPolicek(dt, policka(12));
		model.nastavVybranyKandidat(dt, new VybranyKandidat("GC1", 50, 14, -1, 1_000, "D4/T1"));
		flush();
		Assert.assertEquals(12, model.stavPruhu().badge());
		Assert.assertEquals("D4/T1", model.stavPruhu().stitek());
		Assert.assertEquals(12, model.pocetChybejicich(dt));
		final Challenge kal = model.ulozVlastni(definice("Kal", Typ.KALENDAR));
		model.aktivuj(kal);
		model.setZobrazena(kal);
		model.nastavVysledekPolicek(kal, policka(40));
		model.nastavVybranyKandidat(kal, new VybranyKandidat("GC2", 50, 14, -1, 1_000, "14. 3. (za 131 dní)"));
		flush();
		Assert.assertEquals("Kalendář", model.stavPruhu().stitekTypu());
		Assert.assertEquals(40, model.stavPruhu().badge());
		Assert.assertEquals("14. 3. (za 131 dní)", model.stavPruhu().stitek());
	}

	@Test
	public void vysledekPolickaNeaktivniNeboZmenenedefiniceSeZahodi() throws Exception {
		final Challenge dt = model.ulozVlastni(definice("DT", Typ.DT_MATICE));
		model.nastavVysledekPolicek(dt, policka(3));
		flush();
		Assert.assertNull("neaktivní", model.vysledekPolicek(dt));
		model.aktivuj(dt);
		final Challenge nova = model.ulozVlastni(new Challenge("DT", Typ.DT_MATICE, Stred.souradnice(51, 15), OblastDef.zadna(), ZdrojPravidel.PGC));
		model.nastavVysledekPolicek(dt, policka(3));
		flush();
		Assert.assertNull("stará definice", model.vysledekPolicek(nova));
		model.nastavVysledekPolicek(nova, policka(4));
		flush();
		Assert.assertEquals(4, model.pocetChybejicich(nova));
	}

	@Test
	public void filtrJenKandidatiAChybaPredpoctuSePromitnouDoPruhu() throws Exception {
		final Challenge a = model.ulozVlastni(definice("A", Typ.SEKTORY_360));
		final Challenge b = model.ulozVlastni(definice("B", Typ.SEKTORY_360));
		model.aktivuj(a);
		model.setZapnuto(true);
		model.setJenKandidatiPro(a);
		Assert.assertTrue(model.stavPruhu().jenKandidati());
		model.aktivuj(b);
		Assert.assertFalse("zapnutí jiné challenge vypnulo filtr", model.stavPruhu().jenKandidati());
		model.setJenKandidatiPro(b);
		Assert.assertTrue(model.stavPruhu().jenKandidati());
		model.nastavStavPredpoctu(b, new StavPredpoctu(StavPredpoctu.Faze.CHYBA, 0, 0, "Keš X (střed challenge) není v načtených datech."));
		flush();
		Assert.assertEquals("Keš X (střed challenge) není v načtených datech.", model.stavPruhu().chyba());
	}

	@Test
	public void typyDefiniceSeUlozeiANactou() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {}
		});
		m.setUloziste(() -> u);
		m.initAfterInject();
		m.ulozVlastni(definice("DT", Typ.DT_MATICE));
		m.ulozVlastni(definice("Kal", Typ.KALENDAR));
		final ChallengeModel druhy = new ChallengeModel();
		druhy.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {}
		});
		druhy.setUloziste(() -> u);
		druhy.initAfterInject();
		Assert.assertEquals(Typ.DT_MATICE, druhy.vlastni().get(0).typ());
		Assert.assertEquals(Typ.KALENDAR, druhy.vlastni().get(1).typ());
	}
}
