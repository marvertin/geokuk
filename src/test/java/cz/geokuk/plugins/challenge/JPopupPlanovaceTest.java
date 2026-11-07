package cz.geokuk.plugins.challenge;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.framework.RozmisteniPrekryvu;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Popup plánovače: poloha pod odznakem uvnitř mapy, myš jen v jeho obdélníku, zavírání Esc a kliknutím mimo, obsah podle výsledku. */
public class JPopupPlanovaceTest {

	private ChallengeModel model;
	private JPopupPlanovace popup;
	private JPanel mapa;

	private void priprav() throws Exception {
		model = new ChallengeModel();
		popup = new JPopupPlanovace();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				popup.onEvent((ChallengeZmenenyEvent) event);
			}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
		popup.inject(model);
		mapa = new JPanel(null);
		mapa.setSize(1280, 700);
		mapa.add(popup);
		popup.setBounds(0, 0, 1280, 700);
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		SwingUtilities.invokeAndWait(() -> {});
	}

	private Challenge aktivni() {
		return model.aktivni().get(0);
	}

	private void vysledek(final VysledekSektoru v) throws Exception {
		model.nastavVysledek(aktivni360(), v);
		SwingUtilities.invokeAndWait(() -> {});
	}

	private static MouseEvent stisk(final Component c, final int x, final int y) {
		return new MouseEvent(c, MouseEvent.MOUSE_PRESSED, 0, 0, x, y, 1, false, MouseEvent.BUTTON1);
	}

	@Test
	public void polohaPodOdznakemUvnitrMapy() {
		final Dimension pozadovana = new Dimension(420, 330);
		final Rectangle odznak = JPruhPlanovace.oblastOdznaku(1920);
		final Rectangle r = JPopupPlanovace.poloha(new Dimension(1920, 1000), pozadovana, java.util.Collections.<Rectangle> emptyList());
		Assert.assertEquals("pod pruhem", RozmisteniPrekryvu.OKRAJ + RozmisteniPrekryvu.VYSKA_PRUHU + RozmisteniPrekryvu.POD_PRUHEM, r.y);
		Assert.assertEquals("kousek před odznakem", odznak.x - RozmisteniPrekryvu.PRED_ODZNAKEM, r.x);
		Assert.assertEquals(pozadovana, r.getSize());
		Assert.assertTrue("odznak zůstane vidět nad popupem", odznak.y + odznak.height <= r.y);
		for (final Dimension m : new Dimension[] { new Dimension(1920, 1000), new Dimension(1280, 700), new Dimension(800, 500), new Dimension(450, 400), new Dimension(400, 300), new Dimension(300, 200) }) {
			final Rectangle p = JPopupPlanovace.poloha(m, pozadovana, java.util.Collections.<Rectangle> emptyList());
			final Rectangle uvnitr = new Rectangle(RozmisteniPrekryvu.OKRAJ, 0, m.width - 2 * RozmisteniPrekryvu.OKRAJ, m.height - RozmisteniPrekryvu.OKRAJ);
			Assert.assertTrue(m + ": " + p, uvnitr.contains(p));
			Assert.assertFalse(m + ": popup nepřekrývá pruh " + p, p.intersects(RozmisteniPrekryvu.pruhPlanovace(RozmisteniPrekryvu.volnaSirkaPruhu(m.width))));
		}
		final Rectangle stredni = JPopupPlanovace.poloha(new Dimension(450, 400), pozadovana, java.util.Collections.<Rectangle> emptyList());
		Assert.assertEquals("když se vedle odznaku nevejde, posune se doleva", 450 - RozmisteniPrekryvu.OKRAJ, stredni.x + stredni.width);
		Assert.assertEquals(pozadovana.width, stredni.width);
		Assert.assertEquals("v úzké mapě se zúží", 400 - 2 * RozmisteniPrekryvu.OKRAJ, JPopupPlanovace.poloha(new Dimension(400, 300), pozadovana, java.util.Collections.<Rectangle> emptyList()).width);
		Assert.assertEquals("v nízké mapě se zkrátí nad měřítkem", RozmisteniPrekryvu.meritko(new Dimension(800, 300)).y - RozmisteniPrekryvu.OKRAJ - r.y,
				JPopupPlanovace.poloha(new Dimension(800, 300), pozadovana, java.util.Collections.<Rectangle> emptyList()).height);
	}

	@Test
	public void mysZachytavaJenOtevrenyAJenVeSvemObdelniku() throws Exception {
		priprav();
		Assert.assertFalse(popup.contains(100, 100));
		popup.prepni();
		popup.doLayout();
		final Rectangle o = popup.obsah.getBounds();
		Assert.assertTrue(popup.jeOtevreno());
		Assert.assertTrue(popup.contains(o.x + 1, o.y + 1));
		Assert.assertTrue(popup.contains(o.x + o.width - 1, o.y + o.height - 1));
		Assert.assertFalse(popup.contains(o.x + o.width, o.y + 5));
		Assert.assertFalse(popup.contains(o.x + 5, o.y + o.height));
		Assert.assertFalse("střed mapy", popup.contains(900, 500));
		Assert.assertFalse("pruh nad popupem", popup.contains(10, 10));
	}

	@Test
	public void escZavre() throws Exception {
		priprav();
		final javax.swing.Action esc = popup.getActionMap().get(popup.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).get(javax.swing.KeyStroke.getKeyStroke("ESCAPE")));
		Assert.assertFalse("zavřený popup Esc neobsazuje", esc.isEnabled());
		popup.prepni();
		Assert.assertTrue(esc.isEnabled());
		esc.actionPerformed(new ActionEvent(popup, ActionEvent.ACTION_PERFORMED, "esc"));
		Assert.assertFalse(popup.jeOtevreno());
		Assert.assertFalse(popup.isVisible());
	}

	@Test
	public void klikMimoZavreKlikDovnitrANaOdznakNe() throws Exception {
		priprav();
		popup.prepni();
		popup.doLayout();
		final Rectangle o = popup.obsah.getBounds();
		popup.mysKdekoli(stisk(popup.obsah.souhrn, 2, 2));
		Assert.assertTrue("klik do popupu", popup.jeOtevreno());
		final Rectangle odznak = JPruhPlanovace.oblastOdznaku(1280);
		popup.mysKdekoli(stisk(mapa, odznak.x + 2, odznak.y + 2));
		Assert.assertTrue("stisk na odznaku nechá přepnutí na odznaku", popup.jeOtevreno());
		popup.mysKdekoli(new MouseEvent(mapa, MouseEvent.MOUSE_RELEASED, 0, 0, 900, 500, 1, false, MouseEvent.BUTTON1));
		Assert.assertTrue("puštění tlačítka nic nemění", popup.jeOtevreno());
		final MouseEvent mimo = stisk(mapa, o.x + o.width + 50, 500);
		popup.mysKdekoli(mimo);
		Assert.assertFalse("klik do mapy mimo popup", popup.jeOtevreno());
		Assert.assertFalse("klik mimo se neztratí", mimo.isConsumed());
	}

	@Test
	public void odznakPrepina() throws Exception {
		priprav();
		popup.prepni();
		Assert.assertTrue(popup.jeOtevreno());
		popup.prepni();
		Assert.assertFalse(popup.jeOtevreno());
	}

	@Test
	public void bezPruhuSeNeotevreAVypnutiZavre() throws Exception {
		priprav();
		model.setZapnuto(false);
		popup.prepni();
		Assert.assertFalse("bez pruhu není co ukázat", popup.jeOtevreno());
		model.setZapnuto(true);
		popup.prepni();
		Assert.assertTrue(popup.jeOtevreno());
		model.setZapnuto(false);
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertFalse("vypnutím plánovače se popup zavře", popup.jeOtevreno());
	}

	@Test
	public void obsahPodleVysledku() throws Exception {
		priprav();
		popup.prepni();
		final JPopupPlanovace.Obsah o = popup.obsah;
		Assert.assertTrue(o.souhrn.getText(), o.souhrn.getText().startsWith("Počítá se"));
		Assert.assertFalse("bez výsledku není kam jít", o.dalsi.isEnabled());
		Assert.assertEquals(0, o.radky.size());
		vysledek(TextyPopupuPlanovaceTest.vysledek(new int[] { 47, 213, 305 }, new int[] { 213 }, new double[][] { { 213, 2800, 3000 }, { 47, 6200 } }));
		Assert.assertEquals("Splněno 357 z 360 (99 %), chybí 3", o.souhrn.getText());
		Assert.assertEquals(357, o.postup.getValue());
		Assert.assertEquals("99 %", o.postup.getString());
		final List<String> radky = new ArrayList<>();
		for (int i = 0; i < o.radky.size(); i++) {
			radky.add(o.radky.get(i));
		}
		Assert.assertEquals("jen sektory s kandidáty, po kružnici", java.util.Arrays.asList("47°  1 kandidát  nejbližší 6,2 km od středu", "213°  2 kandidáti  nejbližší 2,8 km od středu"), radky);
		Assert.assertEquals("+ 1 sektor bez kandidátů", o.bezKandidatu.getText());
		Assert.assertTrue(o.nejiste.getText(), o.nejiste.getText().contains("U 1 sektoru leží nalezená keš do 0,05° od hranice"));
		Assert.assertTrue(o.dalsi.isEnabled());
		Assert.assertEquals("▶ Další sektor", o.dalsi.getText());
		Assert.assertEquals(TextyPopupuPlanovace.napovedaSeznamu(Challenge.Typ.SEKTORY_360), o.napoveda.getText());
		Assert.assertFalse("nadpis je jen název challenge", o.nadpis.getText().startsWith("Challenge "));
		Assert.assertEquals("Typ: 360°    Pravidla: " + TextyPopupuPlanovace.pravidla(aktivni().zdroj()), o.typ.getText());
		Assert.assertTrue(o.typ.getText(), o.typ.getText().endsWith("Pravidla: Project-GC") || o.typ.getText().endsWith("Pravidla: Stator"));
		Assert.assertFalse("zkratka PGC se nepíše", o.typ.getText().contains("PGC"));
	}

	@Test
	public void popupPiseOStropuStitkuNaMapeJenKdyzSeUplatni() throws Exception {
		priprav();
		popup.prepni();
		final JPopupPlanovace.Obsah o = popup.obsah;
		Assert.assertEquals(" ", o.omezeniStitku.getText());
		model.nahlasStitkyNaMape(200, 1234);
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("Štítky na mapě: 200 nejbližších z 1\u00a0234", o.omezeniStitku.getText());
		model.nahlasStitkyNaMape(150, 150);
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals(" ", o.omezeniStitku.getText());
	}

	@Test
	public void jenKandidatiPrepinaFiltrModelu() throws Exception {
		priprav();
		popup.prepni();
		popup.obsah.kandidati.doClick();
		Assert.assertEquals(aktivni(), model.jenKandidatiPro());
		popup.obsah.kandidati.doClick();
		Assert.assertNull(model.jenKandidatiPro());
		model.setJenKandidatiPro(aktivni());
		SwingUtilities.invokeAndWait(() -> {});
		popup.obnov();
		Assert.assertTrue("stav přepínače podle modelu (třeba z pruhu)", popup.obsah.kandidati.isSelected());
	}

	@Test
	public void nastaveniBezNapojeniNedostupneSNapojenimZavreAZavola() throws Exception {
		priprav();
		popup.initAfterInject();
		Assert.assertFalse(popup.obsah.nastaveni.isEnabled());
		final int[] volano = { 0 };
		popup.setNaNastaveni(() -> volano[0]++);
		popup.prepni();
		popup.obsah.nastaveni.doClick();
		Assert.assertEquals(1, volano[0]);
		Assert.assertFalse(popup.jeOtevreno());
	}

	@Test
	public void splnenaChallengeNemaKamDal() throws Exception {
		priprav();
		popup.prepni();
		vysledek(TextyPopupuPlanovaceTest.vysledek(new int[0], new int[0], new double[0][]));
		Assert.assertEquals("Splněno 360 z 360 (100 %), chybí 0", popup.obsah.souhrn.getText());
		Assert.assertEquals(0, popup.obsah.radky.size());
		Assert.assertFalse(popup.obsah.dalsi.isEnabled());
		Assert.assertEquals("100 %", popup.obsah.postup.getString());
	}

	private static int posluchacuMysi() {
		return Toolkit.getDefaultToolkit().getAWTEventListeners(AWTEvent.MOUSE_EVENT_MASK).length;
	}

	/** Otevřený popup má jediný globální posluchač myši a po zavření (i Esc, kliknutím mimo, vypnutím, odebráním z okna) žádný. */
	@Test
	public void posluchacMysiJenPoDobuOtevreni() throws Exception {
		priprav();
		final int pred = posluchacuMysi();
		for (int i = 0; i < 3; i++) {
			popup.prepni();
			Assert.assertEquals("otevřený", pred + 1, posluchacuMysi());
			popup.prepni();
			Assert.assertEquals("zavřený odznakem", pred, posluchacuMysi());
		}
		popup.prepni();
		popup.doLayout();
		popup.mysKdekoli(stisk(mapa, 1200, 600));
		Assert.assertEquals("zavřený kliknutím mimo", pred, posluchacuMysi());
		popup.prepni();
		model.setZapnuto(false);
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("zavřený vypnutím plánovače", pred, posluchacuMysi());
		model.setZapnuto(true);
		popup.prepni();
		popup.removeNotify();
		Assert.assertFalse(popup.jeOtevreno());
		Assert.assertEquals("zavřený odebráním z okna", pred, posluchacuMysi());
	}

	private static List<String> radky(final JPopupPlanovace.Obsah o) {
		final List<String> r = new ArrayList<>();
		for (int i = 0; i < o.radky.size(); i++) {
			r.add(o.radky.get(i));
		}
		return r;
	}

	/** Popup ukazuje aktivní challenge; po zapnutí jiné ukáže tu. */
	@Test
	public void ukazujeAktivniChallenge() throws Exception {
		priprav();
		model.aktivuj(Sablony.praha360());
		SwingUtilities.invokeAndWait(() -> {});
		popup.prepni();
		Assert.assertEquals(TextyPopupuPlanovace.nadpis(Sablony.praha360()), popup.obsah.nadpis.getText());
		model.aktivuj(Sablony.cesko360());
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("po zapnutí jiné", TextyPopupuPlanovace.nadpis(Sablony.cesko360()), popup.obsah.nadpis.getText());
	}

	private Challenge aktivujPolicka(final Challenge.Typ typ) throws Exception {
		final Challenge c = model.ulozVlastni(new Challenge("Test " + typ, typ, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(c);
		model.setZobrazena(c);
		SwingUtilities.invokeAndWait(() -> {});
		return c;
	}

	@Test
	public void dtMaticeUkazujeChybejiciPolicka() throws Exception {
		priprav();
		final Challenge c = aktivujPolicka(Challenge.Typ.DT_MATICE);
		popup.prepni();
		final JPopupPlanovace.Obsah o = popup.obsah;
		Assert.assertEquals("Typ: D/T", o.typ.getText());
		Assert.assertTrue("bez výsledku průběh", o.souhrn.getText().startsWith("Počítá se"));
		Assert.assertFalse(o.dalsi.isEnabled());
		model.nastavVysledekPolicek(c, new VysledekPolicek(81, java.util.Arrays.asList("D1/T1", "D5/T5"), java.util.Collections.emptyList()));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("Splněno 79 z 81 (98 %), chybí 2", o.souhrn.getText());
		Assert.assertEquals(81, o.postup.getMaximum());
		Assert.assertEquals(79, o.postup.getValue());
		Assert.assertEquals("98 %", o.postup.getString());
		Assert.assertEquals(TextyPopupuPlanovace.nadpisSeznamu(Challenge.Typ.DT_MATICE), o.nadpisSeznamu.getText());
		Assert.assertTrue("bez kandidátů žádné řádky", radky(o).isEmpty());
		Assert.assertEquals("+ 2 políčka bez kandidátů", o.bezKandidatu.getText());
		Assert.assertFalse("střed je jen u 360°", o.stred.isVisible());
		Assert.assertEquals(" ", o.nejiste.getText());
		Assert.assertEquals("▶ Další políčko", o.dalsi.getText());
		Assert.assertFalse("bez kandidátů není kam jít", o.dalsi.isEnabled());
		model.nastavVysledekPolicek(c, new VysledekPolicek(81, java.util.Arrays.asList("D1/T1", "D5/T5"), java.util.Collections.singletonList(new KandidatPolicka("GC5", 50, 14, "D5/T5", "D5/T5", null))));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertTrue(o.dalsi.isEnabled());
		o.dalsi.doClick();
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("▶ vybere políčko s kandidáty", "D5/T5", model.vybranePolicko(c));
		Assert.assertEquals(java.util.Collections.singletonList("D5/T5"), radky(o));
		Assert.assertEquals("+ 1 políčko bez kandidátů", o.bezKandidatu.getText());
		Assert.assertEquals("vybrané políčko je vybraný řádek", 0, o.seznam.getSelectedIndex());
		model.nastavVysledekPolicek(c, new VysledekPolicek(81, java.util.Collections.emptyList(), java.util.Collections.emptyList()));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("Splněno 81 z 81 (100 %), chybí 0", o.souhrn.getText());
		Assert.assertFalse("splněná matice nemá kam dál", o.dalsi.isEnabled());
		Assert.assertEquals(0, o.radky.size());
	}

	@Test
	public void kalendarUkazujeChybejiciDnyAPo360SeVratiSektory() throws Exception {
		priprav();
		model.setKalendarVUi(true);
		final Challenge c = aktivujPolicka(Challenge.Typ.KALENDAR);
		model.nastavVysledekPolicek(c, new VysledekPolicek(366, java.util.Arrays.asList("14. 3.", "29. 2."), java.util.Collections.emptyList()));
		SwingUtilities.invokeAndWait(() -> {});
		popup.prepni();
		final JPopupPlanovace.Obsah o = popup.obsah;
		Assert.assertEquals("Typ: Kalendář", o.typ.getText());
		Assert.assertEquals("Chybějící dny:", o.nadpisSeznamu.getText());
		Assert.assertEquals(366, o.postup.getMaximum());
		Assert.assertEquals(java.util.Arrays.asList("14. 3.", "29. 2."), radky(o));
		model.aktivuj(Sablony.cesko360());
		vysledek(TextyPopupuPlanovaceTest.vysledek(new int[] { 47 }, new int[0], new double[][] { { 47, 6200 } }));
		Assert.assertEquals("Typ: 360°    Pravidla: " + TextyPopupuPlanovace.pravidla(aktivni360().zdroj()), o.typ.getText());
		Assert.assertEquals(360, o.postup.getMaximum());
		Assert.assertTrue(o.stred.isVisible());
		Assert.assertEquals(TextyPopupuPlanovace.nadpisSeznamu(Challenge.Typ.SEKTORY_360), o.nadpisSeznamu.getText());
		Assert.assertEquals(java.util.Collections.singletonList("47°  1 kandidát  nejbližší 6,2 km od středu"), radky(o));
	}

	private Challenge aktivni360() {
		for (final Challenge c : model.aktivni()) {
			if (c.typ() == Challenge.Typ.SEKTORY_360) {
				return c;
			}
		}
		throw new AssertionError("není aktivní 360°");
	}

	/** Nastavení… zavře popup a spustí akci dialogu plánovače z menu (bez displeje a továrny akce skončí výjimkou, ta ukáže, kudy volání šlo). */
	@Test
	public void nastaveniSpustiAkciDialogu() throws Exception {
		priprav();
		final cz.geokuk.core.program.Akce akce = new cz.geokuk.core.program.Akce();
		popup.inject(akce);
		Assert.assertTrue(popup.obsah.nastaveni.isEnabled());
		popup.prepni();
		try {
			popup.obsah.nastaveni.doClick();
			Assert.fail("akce dialogu se měla spustit");
		} catch (final RuntimeException e) {
			// Bez displeje selže už vytvoření dialogu (zásobník vede přes akci), s displejem akce ohlásí chybějící továrnu (zpráva nese třídu akce).
			boolean akceDialogu = e.getMessage() != null && e.getMessage().contains(ChallengeNastaveniAction.class.getName());
			for (final StackTraceElement r : e.getStackTrace()) {
				akceDialogu |= r.getClassName().equals(ChallengeNastaveniAction.class.getName());
			}
			Assert.assertTrue("volání prošlo akcí dialogu: " + e, akceDialogu);
		}
		Assert.assertFalse("popup se před dialogem zavře", popup.jeOtevreno());
	}

	@Test
	public void dlouhyNazevChallengeNerozsiriPopupAVeZkracenemTvaruMaTooltip() {
		final JPopupPlanovace.Obsah o = new JPopupPlanovace.Obsah();
		final String text = TextyPopupuPlanovace.nadpis(Sablony.cesko360());
		o.nadpis.setPlnyText(text);
		Assert.assertEquals("getText je plný", text, o.nadpis.getText());
		Assert.assertTrue(o.nadpis.getPreferredSize().width <= JPopupPlanovace.NEJVETSI_SIRKA_NADPISU);
		o.nadpis.setSize(o.nadpis.getPreferredSize());
		Assert.assertTrue("zkrácený text: " + text.length(), o.nadpis.getToolTipText(null) != null || o.nadpis.getPreferredSize().width >= o.nadpis.getFontMetrics(o.nadpis.getFont()).stringWidth(text));
		o.nadpis.setSize(150, 20);
		Assert.assertEquals(text, o.nadpis.getToolTipText(null));
	}

	/** Klik na řádek s kandidáty vybere sektor (bez výběru keše), druhý klik výběr zruší; řádek bez kandidátů nic nedělá. ▶ jde po kružnici. */
	@Test
	public void klikNaRadekVyberSektorATlacitkoDalsiPoKruznici() throws Exception {
		priprav();
		popup.prepni();
		final JPopupPlanovace.Obsah o = popup.obsah;
		vysledek(TextyPopupuPlanovaceTest.vysledek(new int[] { 47, 213, 305 }, new int[0], new double[][] { { 213, 2800, 3000 }, { 47, 6200 } }));
		final Challenge c = aktivni360();
		Assert.assertEquals(-1, o.seznam.getSelectedIndex());
		popup.klikNaRadek(o.polozky.get(1));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals(213, model.vybranySektor(c));
		Assert.assertEquals(1, o.seznam.getSelectedIndex());
		Assert.assertNull("keš se nevybírá", model.vybranyKandidat(c));
		popup.klikNaRadek(o.polozky.get(1));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("druhý klik výběr zruší", PosunVyberu.ZADNY_SEKTOR, model.vybranySektor(c));
		Assert.assertEquals(-1, o.seznam.getSelectedIndex());
		o.dalsi.doClick();
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("▶ od začátku kružnice", 47, model.vybranySektor(c));
		o.dalsi.doClick();
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals(213, model.vybranySektor(c));
		o.dalsi.doClick();
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("za posledním s kandidáty zase první", 47, model.vybranySektor(c));
		Assert.assertEquals(0, o.seznam.getSelectedIndex());
	}

	/** Výběr v seznamu se nemění klávesnicí ani přímým výběrem, jen podle modelu. */
	@Test
	public void vyberVSeznamuJenPodleModelu() throws Exception {
		priprav();
		popup.prepni();
		final JPopupPlanovace.Obsah o = popup.obsah;
		vysledek(TextyPopupuPlanovaceTest.vysledek(new int[] { 47, 213 }, new int[0], new double[][] { { 213, 2800 }, { 47, 6200 } }));
		o.seznam.setSelectedIndex(1);
		Assert.assertEquals(-1, o.seznam.getSelectedIndex());
		Assert.assertEquals(PosunVyberu.ZADNY_SEKTOR, model.vybranySektor(aktivni360()));
	}
}
