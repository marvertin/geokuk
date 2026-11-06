package cz.geokuk.plugins.challenge;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Akce menu Kešoidy > Challenge: stav z modelu, zkratka F4 pro jen kandidáty. */
public class ChallengeAkceTest {

	private ChallengeModel model;
	private final ChallengeZapnoutAction zapnout = new ChallengeZapnoutAction();
	private final ChallengeDalsiAction dalsi = new ChallengeDalsiAction();
	private final ChallengePredchoziAction predchozi = new ChallengePredchoziAction();
	private final ChallengeJenKandidatiAction kandidati = new ChallengeJenKandidatiAction();

	@Before
	public void setUp() {
		model = new ChallengeModel();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				final ChallengeZmenenyEvent e = (ChallengeZmenenyEvent) event;
				zapnout.onEvent(e);
				dalsi.onEvent(e);
				predchozi.onEvent(e);
				kandidati.onEvent(e);
			}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
		zapnout.inject(model);
		dalsi.inject(model);
		predchozi.inject(model);
		kandidati.inject(model);
	}

	/** Plánovač zapisuje vybraného kandidáta ve vlákně Swingu; zápis z testu musí doběhnout, než začne další test. */
	@After
	public void pockejNaEdt() throws Exception {
		SwingUtilities.invokeAndWait(() -> {});
	}

	@Test
	public void f4PrepinaJenKandidatyDalsiAPredchoziBezZkratky() {
		Assert.assertEquals(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), kandidati.getValue(Action.ACCELERATOR_KEY));
		Assert.assertNull(dalsi.getValue(Action.ACCELERATOR_KEY));
		Assert.assertNull(predchozi.getValue(Action.ACCELERATOR_KEY));
		Assert.assertNull("lišta jen úchytem a menu", zapnout.getValue(Action.ACCELERATOR_KEY));
	}

	@Test
	public void zapnoutPrepinaModelAStavSeVraciZModelu() {
		Assert.assertFalse(zapnout.isSelected());
		zapnout.onSlectedChange(true);
		Assert.assertTrue(model.jeZapnuto());
		Assert.assertTrue(zapnout.isSelected());
		model.setZapnuto(false);
		Assert.assertFalse(zapnout.isSelected());
	}

	@Test
	public void dalsiAPredchoziAFiltrJsouDostupneJenSViditelnymPruhem() {
		Assert.assertFalse(dalsi.isEnabled());
		Assert.assertFalse(predchozi.isEnabled());
		Assert.assertFalse(kandidati.isEnabled());
		model.setZapnuto(true);
		Assert.assertFalse("zapnuto, ale bez aktivní challenge", dalsi.isEnabled());
		model.aktivuj(Sablony.cesko360());
		Assert.assertTrue(dalsi.isEnabled());
		Assert.assertTrue(predchozi.isEnabled());
		Assert.assertTrue(kandidati.isEnabled());
		model.setZapnuto(false);
		Assert.assertFalse(dalsi.isEnabled());
		Assert.assertFalse(kandidati.isEnabled());
	}

	@Test
	public void jenKandidatiZapneFiltrPrvniAktivniChallengeAVypneHo() {
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		Assert.assertEquals(Sablony.cesko360(), model.zobrazena());
		kandidati.onSlectedChange(true);
		Assert.assertEquals(Sablony.cesko360(), model.jenKandidatiPro());
		Assert.assertTrue(kandidati.isSelected());
		kandidati.onSlectedChange(false);
		Assert.assertNull(model.jenKandidatiPro());
		Assert.assertFalse(kandidati.isSelected());
	}

	@Test
	public void f4UkazeZpravuSeStavem() {
		final java.util.List<String> zpravy = new java.util.ArrayList<>();
		kandidati.inject(new cz.geokuk.plugins.kesoid.ToastModel() {
			@Override
			public void ukaz(final String text) {
				zpravy.add(text);
			}
		});
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		kandidati.onSlectedChange(true);
		kandidati.onSlectedChange(false);
		Assert.assertEquals(java.util.Arrays.asList("Challenge: jen kandidáti – Zapnuto", "Challenge: jen kandidáti – Vypnuto"), zpravy);
	}

	@Test
	public void shiftSipkySkaceNaKandidataAHlasiKdyzZadnyNeni() {
		final java.util.List<String> zpravy = new java.util.ArrayList<>();
		final cz.geokuk.plugins.kesoid.ToastModel toast = new cz.geokuk.plugins.kesoid.ToastModel() {
			@Override
			public void ukaz(final String text) {
				zpravy.add(text);
			}
		};
		final int[] klavesy = { KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT };
		for (final SmerSkoku smer : SmerSkoku.values()) {
			final ChallengeSkokAction skok = new ChallengeSkokAction(smer);
			Assert.assertEquals(KeyStroke.getKeyStroke(klavesy[smer.ordinal()], java.awt.event.InputEvent.SHIFT_DOWN_MASK), skok.getValue(Action.ACCELERATOR_KEY));
			Assert.assertFalse("bez lišty nejde", skok.isEnabled());
			skok.inject(model);
			skok.inject(toast);
			skok.setPlanovac(() -> null);
			skok.actionPerformed(null);
		}
		Assert.assertEquals(java.util.Arrays.asList("Challenge – žádný kandidát na sever", "Challenge – žádný kandidát na východ", "Challenge – žádný kandidát na jih",
				"Challenge – žádný kandidát na západ"), zpravy);
	}

	@Test
	public void jenKandidatiBezAktivniChallengeNicNezapne() {
		kandidati.onSlectedChange(true);
		Assert.assertNull(model.jenKandidatiPro());
		Assert.assertFalse(kandidati.isSelected());
	}

	@Test
	public void zobrazenaJeJedinaAktivni() {
		Assert.assertNull(model.zobrazena());
		model.aktivuj(Sablony.cesko360());
		Assert.assertEquals(Sablony.cesko360(), model.zobrazena());
		final Challenge druha = model.ulozVlastni(new Challenge("Druhá", Challenge.Typ.SEKTORY_360, Challenge.Stred.souradnice(50, 14), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(druha);
		Assert.assertEquals("zapnutí druhé vypnulo první", druha, model.zobrazena());
		model.deaktivuj(druha);
		Assert.assertNull(model.zobrazena());
	}

	private final java.util.List<VybranyKandidat> vystredeno = new java.util.ArrayList<>();

	private PlanovacChallenge planovac() {
		return new PlanovacChallenge(model, id -> java.util.Optional.empty(), () -> new cz.geokuk.core.coordinates.Wgs(50, 14), () -> java.nio.file.Paths.get("neexistuje"),
				() -> new cz.geokuk.core.coordinates.Wgs(50.0, 14.0), vystredeno::add);
	}

	private void priravVysledek() throws Exception {
		model.aktivuj(Sablony.cesko360());
		final java.util.List<VysledekSektoru.KandidatKes> kandidati = new java.util.ArrayList<>();
		kandidati.add(new VysledekSektoru.KandidatKes("GC1", 50.01, 14.0, 10));
		kandidati.add(new VysledekSektoru.KandidatKes("GC2", 50.20, 14.0, 20));
		model.nastavVysledek(Sablony.cesko360(), new VysledekSektoru(new java.util.BitSet(360), new java.util.BitSet(360), new VysledekSektoru.Kandidati[360], 0, kandidati));
		SwingUtilities.invokeAndWait(() -> {});
	}

	@Test
	public void prejdiVolaPlanovacSHlavniChallengeAVystredi() throws Exception {
		priravVysledek();
		final PlanovacChallenge p = planovac();
		Assert.assertEquals("GC1", ChallengeAkce.prejdi(model, p, true).id());
		Assert.assertEquals("GC2", ChallengeAkce.prejdi(model, p, true).id());
		Assert.assertEquals(2, vystredeno.size());
		Assert.assertEquals("GC1", ChallengeAkce.prejdi(model, p, false).id());
		Assert.assertEquals("GC1", vystredeno.get(2).id());
	}

	@Test
	public void shiftSipkaVyberePlanovacemKandidataVeSmeru() throws Exception {
		priravVysledek();
		model.setZapnuto(true);
		final PlanovacChallenge p = planovac();
		final java.util.List<String> zpravy = new java.util.ArrayList<>();
		final cz.geokuk.plugins.kesoid.ToastModel toast = new cz.geokuk.plugins.kesoid.ToastModel() {
			@Override
			public void ukaz(final String text) {
				zpravy.add(text);
			}
		};
		final ChallengeSkokAction sever = new ChallengeSkokAction(SmerSkoku.SEVER);
		final ChallengeSkokAction jih = new ChallengeSkokAction(SmerSkoku.JIH);
		for (final ChallengeSkokAction a : new ChallengeSkokAction[] { sever, jih }) {
			a.inject(model);
			a.inject(toast);
			a.setPlanovac(() -> p);
		}
		sever.actionPerformed(new ActionEvent(this, 0, ""));
		Assert.assertEquals("na severu je kandidát, mapa se na něj vystředí", 1, vystredeno.size());
		Assert.assertTrue("bez zprávy", zpravy.isEmpty());
		jih.actionPerformed(new ActionEvent(this, 0, ""));
		Assert.assertEquals("na jihu nikdo", java.util.Collections.singletonList("Challenge – žádný kandidát na jih"), zpravy);
		Assert.assertEquals(1, vystredeno.size());
	}

	@Test
	public void akceVolajiPlanovacPriStisku() throws Exception {
		priravVysledek();
		final PlanovacChallenge p = planovac();
		dalsi.setPlanovac(() -> p);
		predchozi.setPlanovac(() -> p);
		dalsi.actionPerformed(new ActionEvent(this, 0, ""));
		dalsi.actionPerformed(new ActionEvent(this, 0, ""));
		Assert.assertEquals(java.util.Arrays.asList("GC1", "GC2"), ids());
		predchozi.actionPerformed(new ActionEvent(this, 0, ""));
		Assert.assertEquals(java.util.Arrays.asList("GC1", "GC2", "GC1"), ids());
	}

	private java.util.List<String> ids() {
		final java.util.List<String> l = new java.util.ArrayList<>();
		for (final VybranyKandidat k : vystredeno) {
			l.add(k.id());
		}
		return l;
	}

	@Test
	public void bezChallengeBezPlanovaceNeboBezModeluNicNeudelaANespadne() throws Exception {
		Assert.assertNull("bez aktivní challenge", ChallengeAkce.prejdi(model, planovac(), true));
		priravVysledek();
		Assert.assertNull("bez plánovače", ChallengeAkce.prejdi(model, null, true));
		Assert.assertNull("bez modelu", ChallengeAkce.prejdi(null, planovac(), false));
		dalsi.actionPerformed(new ActionEvent(this, 0, ""));
		predchozi.actionPerformed(new ActionEvent(this, 0, ""));
		Assert.assertTrue("akce bez plánovače nic nevystředí", vystredeno.isEmpty());
	}

	@Test
	public void stitekJeCitelnyNaPozadiVsechTemat() {
		final Color[] pozadi = { new Color(204, 204, 204), new Color(238, 238, 238), new Color(102, 102, 102), Color.WHITE, Color.BLACK, new Color(40, 40, 40) };
		for (final Color p : pozadi) {
			Assert.assertTrue(p + ": kontrast " + StitekChallenge.kontrast(StitekChallenge.barva(p), p), StitekChallenge.kontrast(StitekChallenge.barva(p), p) >= 4.5);
		}
		Assert.assertEquals("kde stačí, zůstane oranžová", new Color(200, 100, 0), StitekChallenge.barva(Color.BLACK));
		Assert.assertEquals(new Color(200, 100, 0), StitekChallenge.barva(null));
	}

	@Test
	public void predchoziAkceJeDostupnaUVsechTypu() {
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(dt);
		model.setZapnuto(true);
		Assert.assertTrue(dalsi.isEnabled());
		Assert.assertTrue(predchozi.isEnabled());
		model.setZapnuto(false);
		Assert.assertFalse(predchozi.isEnabled());
	}

	@Test
	public void akceAFiltrPracujiSAktivniChallenge() throws Exception {
		model.aktivuj(Sablony.cesko360());
		model.aktivuj(Sablony.praha360());
		model.setZapnuto(true);
		Assert.assertEquals(Sablony.praha360(), model.zobrazena());
		kandidati.onSlectedChange(true);
		Assert.assertEquals("filtr patří aktivní challenge", Sablony.praha360(), model.jenKandidatiPro());
	}

	@Test
	public void dalsiJdeNaAktivniChallengeAFiltrSeZobrazujeProNi() throws Exception {
		model.aktivuj(Sablony.cesko360());
		model.aktivuj(Sablony.praha360());
		model.setZapnuto(true);
		final java.util.List<VysledekSektoru.KandidatKes> kandidati2 = new java.util.ArrayList<>();
		kandidati2.add(new VysledekSektoru.KandidatKes("GC9", 50.01, 14.0, 10));
		model.nastavVysledek(Sablony.praha360(), new VysledekSektoru(new java.util.BitSet(360), new java.util.BitSet(360), new VysledekSektoru.Kandidati[360], 0, kandidati2));
		SwingUtilities.invokeAndWait(() -> {});
		final PlanovacChallenge p = planovac();
		Assert.assertEquals("další jde na aktivní challenge", "GC9", ChallengeAkce.prejdi(model, p, true).id());
		// Vybraného kandidáta zapíše plánovač ve vlákně Swingu; model se dál mění až po něm.
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertFalse("filtr ještě není zapnutý", kandidati.isSelected());
		model.setJenKandidatiPro(Sablony.praha360());
		Assert.assertTrue("přepínač filtru ukazuje stav aktivní challenge", kandidati.isSelected());
		model.aktivuj(Sablony.cesko360());
		Assert.assertFalse("zapnutí jiné challenge vypne i filtr", kandidati.isSelected());
	}

	private Challenge kalendar() {
		return model.ulozVlastni(new Challenge("Kal", Challenge.Typ.KALENDAR, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
	}

	@Test
	public void ulozenyAktivniKalendarSeNepromitaDoPruhuADtJdeZaNim() {
		final Challenge kal = kalendar();
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(kal);
		model.aktivuj(dt);
		model.setZapnuto(true);
		Assert.assertEquals("kalendář je první aktivní, ale pruh ukazuje D/T", dt, model.zobrazena());
		Assert.assertTrue(model.jePruhViditelny());
		Assert.assertEquals(Challenge.Typ.DT_MATICE, model.stavPruhu().typ());
		Assert.assertEquals("kalendář se nepočítá mezi aktivní v pruhu", 1, model.stavPruhu().pocetAktivnich());
		Assert.assertTrue(dalsi.isEnabled());
	}

	@Test
	public void jenKalendarAktivniPruhNeukazeAAkceJsouVypnute() {
		final Challenge kal = kalendar();
		model.aktivuj(kal);
		model.setZapnuto(true);
		Assert.assertNull(model.zobrazena());
		Assert.assertFalse(model.jePruhViditelny());
		Assert.assertFalse(model.stavPruhu().jeViditelny());
		Assert.assertFalse(dalsi.isEnabled());
		Assert.assertTrue("data se nemažou, kalendář zůstává aktivní", model.jeAktivni(kal));
		try {
			model.setZobrazena(kal);
			Assert.fail("kalendář se v pruhu zobrazit nedá");
		} catch (final ChallengeChyba e) {
			Assert.assertFalse(e.getMessage().isEmpty());
		}
	}

	@Test
	public void kalendarSeVUiNenabizi() {
		Assert.assertTrue(model.jeVUi(Sablony.cesko360()));
		final Challenge kal = kalendar();
		Assert.assertFalse(model.jeVUi(kal));
		model.setKalendarVUi(true);
		Assert.assertTrue("jen pro testy a budoucí vydání", model.jeVUi(kal));
	}
}
