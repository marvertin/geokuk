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
		Assert.assertEquals(java.util.Arrays.asList("Plánovač: jen kandidáti – Zapnuto", "Plánovač: jen kandidáti – Vypnuto"), zpravy);
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
		Assert.assertEquals(java.util.Arrays.asList("Plánovač – žádný kandidát na sever", "Plánovač – žádný kandidát na východ", "Plánovač – žádný kandidát na jih",
				"Plánovač – žádný kandidát na západ"), zpravy);
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

	/** Chybí 10°, 20° a 30°; kandidáty mají 10° a 20°. */
	private void pripravSektory(final Challenge c) throws Exception {
		model.aktivuj(c);
		model.setZapnuto(true);
		model.nastavVysledek(c, TextyPopupuPlanovaceTest.vysledek(new int[] { 10, 20, 30 }, new int[0], new double[][] { { 10, 1000 }, { 20, 2000 } }));
		SwingUtilities.invokeAndWait(() -> {});
	}

	@Test
	public void posunJdePoSektorechSKandidatyBezVyberuKeseABezVystredeni() throws Exception {
		pripravSektory(Sablony.cesko360());
		Assert.assertEquals("10°", posun(true));
		Assert.assertEquals("20°", posun(true));
		Assert.assertEquals("30° nemá kandidáty, kružnice se uzavře", "10°", posun(true));
		Assert.assertEquals("20°", posun(false));
		Assert.assertEquals(20, model.vybranySektor(Sablony.cesko360()));
		Assert.assertNull("keš se nevybírá", model.vybranyKandidat(Sablony.cesko360()));
		Assert.assertTrue("mapa se nevystřeďuje", vystredeno.isEmpty());
	}

	/** Posun ve vlákně Swingu jako z tlačítka nebo menu. */
	private String posun(final boolean dopredu) throws Exception {
		final String[] r = new String[1];
		SwingUtilities.invokeAndWait(() -> r[0] = ChallengeAkce.posun(model, dopredu));
		return r[0];
	}

	/** Kandidáti v 10° (GC10, sever od středu 50/14) a 20° (GC20); oba sektory chybí a mají kandidáty. */
	private void pripravSektorySKesemi() throws Exception {
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		final java.util.BitSet obsazene = new java.util.BitSet(360);
		obsazene.set(0, 360);
		final VysledekSektoru.Kandidati[] k = new VysledekSektoru.Kandidati[360];
		for (final int s : new int[] { 10, 20 }) {
			obsazene.clear(s);
			k[s] = new VysledekSektoru.Kandidati(s);
			k[s].pridej("GC" + s, 1000 * s);
		}
		final java.util.List<VysledekSektoru.KandidatKes> kese = java.util.Arrays.asList(new VysledekSektoru.KandidatKes("GC10", 50.01, 14.0, 10),
				new VysledekSektoru.KandidatKes("GC20", 49.99, 14.0, 20));
		model.nastavVysledek(Sablony.cesko360(), new VysledekSektoru(obsazene, new java.util.BitSet(360), k, 358, kese));
		SwingUtilities.invokeAndWait(() -> {});
	}

	@Test
	public void skokVeSmeruVybereIJehoSektorAPosunKesNecha() throws Exception {
		pripravSektorySKesemi();
		final PlanovacChallenge p = planovac();
		final VybranyKandidat[] k = new VybranyKandidat[1];
		SwingUtilities.invokeAndWait(() -> k[0] = ChallengeAkce.skoc(model, p, SmerSkoku.SEVER));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("GC10", k[0].id());
		Assert.assertEquals("Shift+šipka vybere i sektor keše", 10, model.vybranySektor(Sablony.cesko360()));
		Assert.assertEquals("▶ jde z vybraného sektoru dál", "20°", posun(true));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("◀ ▶ vybranou keš nechají", "GC10", model.vybranyKandidat(Sablony.cesko360()).id());
		Assert.assertEquals(20, model.vybranySektor(Sablony.cesko360()));
	}

	/** Kandidát v sektoru 0° (přímo na sever): Shift+šipka vybere i sektor 0. */
	@Test
	public void skokNaKandidataVSektoruNulaVybereSektorNula() throws Exception {
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		final java.util.BitSet obsazene = new java.util.BitSet(360);
		obsazene.set(1, 360);
		final VysledekSektoru.Kandidati[] k = new VysledekSektoru.Kandidati[360];
		k[0] = new VysledekSektoru.Kandidati(0);
		k[0].pridej("GC0", 1000);
		model.nastavVysledek(Sablony.cesko360(),
				new VysledekSektoru(obsazene, new java.util.BitSet(360), k, 359, java.util.Collections.singletonList(new VysledekSektoru.KandidatKes("GC0", 50.01, 14.0, 0))));
		SwingUtilities.invokeAndWait(() -> {});
		final PlanovacChallenge p = planovac();
		final VybranyKandidat[] v = new VybranyKandidat[1];
		SwingUtilities.invokeAndWait(() -> v[0] = ChallengeAkce.skoc(model, p, SmerSkoku.SEVER));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("GC0", v[0].id());
		Assert.assertEquals(0, model.vybranySektor(Sablony.cesko360()));
	}

	@Test
	public void nazevATooltipPosunuPodleTypu() {
		Assert.assertEquals("Další sektor", ChallengeAkce.nazevPosunu(Challenge.Typ.SEKTORY_360, true));
		Assert.assertEquals("Předchozí políčko", ChallengeAkce.nazevPosunu(Challenge.Typ.DT_MATICE, false));
		Assert.assertEquals("Další sektor s kandidáty po kružnici; Shift+šipky skočí na kandidáta ve směru šipky", ChallengeAkce.tipPosunu(Challenge.Typ.SEKTORY_360, true));
		Assert.assertEquals("Předchozí políčko s kandidáty po řádcích matice; Shift+šipky skočí na kandidáta ve směru šipky", ChallengeAkce.tipPosunu(Challenge.Typ.DT_MATICE, false));
		Assert.assertEquals("Další sektor", dalsi.getValue(Action.NAME));
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(dt);
		model.setZapnuto(true);
		Assert.assertEquals("Další políčko", dalsi.getValue(Action.NAME));
		Assert.assertEquals("Předchozí políčko", predchozi.getValue(Action.NAME));
		Assert.assertEquals(ChallengeAkce.tipPosunu(Challenge.Typ.DT_MATICE, true), dalsi.getValue(Action.SHORT_DESCRIPTION));
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
		Assert.assertEquals("na jihu nikdo", java.util.Collections.singletonList("Plánovač – žádný kandidát na jih"), zpravy);
		Assert.assertEquals(1, vystredeno.size());
	}

	@Test
	public void akcePosunouVyberPriStisku() throws Exception {
		pripravSektory(Sablony.cesko360());
		SwingUtilities.invokeAndWait(() -> {
			dalsi.actionPerformed(new ActionEvent(this, 0, ""));
			dalsi.actionPerformed(new ActionEvent(this, 0, ""));
		});
		Assert.assertEquals(20, model.vybranySektor(Sablony.cesko360()));
		SwingUtilities.invokeAndWait(() -> predchozi.actionPerformed(new ActionEvent(this, 0, "")));
		Assert.assertEquals(10, model.vybranySektor(Sablony.cesko360()));
	}

	@Test
	public void bezChallengeBezPlanovaceNeboBezModeluNicNeudelaANespadne() throws Exception {
		Assert.assertNull("bez aktivní challenge", posun(true));
		Assert.assertNull("bez modelu", ChallengeAkce.posun(null, false));
		priravVysledek();
		Assert.assertNull("bez sektorů s kandidáty není kam", posun(true));
		dalsi.actionPerformed(new ActionEvent(this, 0, ""));
		predchozi.actionPerformed(new ActionEvent(this, 0, ""));
		Assert.assertTrue("nic se nevystředí", vystredeno.isEmpty());
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
		model.nastavVysledek(Sablony.praha360(), TextyPopupuPlanovaceTest.vysledek(new int[] { 7 }, new int[0], new double[][] { { 7, 500 } }));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("další jde na aktivní challenge", "7°", posun(true));
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
