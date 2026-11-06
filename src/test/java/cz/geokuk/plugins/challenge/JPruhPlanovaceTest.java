package cz.geokuk.plugins.challenge;

import java.awt.Component;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.napoveda.UkazatelVykonu;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.framework.RozmisteniPrekryvu;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Pruh plánovače: rozložení prvků na třech stupních šířky, viditelnost podle stavu modelu a klikání jen v obdélníku pruhu. */
public class JPruhPlanovaceTest {

	@Test
	public void prvkyZaplniPruhNaVsechTrechStupnich() {
		for (final int sirka : RozmisteniPrekryvu.STUPNE_PRUHU) {
			final Rectangle pruh = new Rectangle(RozmisteniPrekryvu.OKRAJ, RozmisteniPrekryvu.OKRAJ, sirka, RozmisteniPrekryvu.VYSKA_PRUHU);
			final Rectangle[] r = JPruhPlanovace.rozloz(sirka);
			final List<Rectangle> videt = new ArrayList<>();
			for (final Rectangle x : r) {
				if (!x.isEmpty()) {
					Assert.assertTrue(sirka + ": " + x, pruh.contains(x));
					videt.add(x);
				}
			}
			for (int i = 0; i < videt.size(); i++) {
				for (int j = i + 1; j < videt.size(); j++) {
					Assert.assertFalse(sirka + ": " + videt.get(i) + " × " + videt.get(j), videt.get(i).intersects(videt.get(j)));
				}
			}
			Assert.assertEquals("šipky u pravého okraje s podšívkou", pruh.x + pruh.width - 4, r[JPruhPlanovace.DAL].x + r[JPruhPlanovace.DAL].width);
			Assert.assertEquals("vlevo podšívka", pruh.x + 4, videt.get(0).x);
		}
	}

	@Test
	public void stupneUbiraji() {
		final Rectangle[] uplny = JPruhPlanovace.rozloz(267);
		final Rectangle[] stredni = JPruhPlanovace.rozloz(178);
		final Rectangle[] uzky = JPruhPlanovace.rozloz(106);
		Assert.assertEquals(111, uplny[JPruhPlanovace.KANDIDATI].width);
		Assert.assertEquals("jen ikona trychtýře", 22, stredni[JPruhPlanovace.KANDIDATI].width);
		Assert.assertFalse(stredni[JPruhPlanovace.TYP].isEmpty());
		Assert.assertTrue(uzky[JPruhPlanovace.TYP].isEmpty());
		Assert.assertTrue(uzky[JPruhPlanovace.KANDIDATI].isEmpty());
		Assert.assertEquals("odznak zabere zbytek", 42, uzky[JPruhPlanovace.ODZNAK].width);
		Assert.assertEquals(24, uzky[JPruhPlanovace.ZPET].width);
	}

	@Test
	public void sirkaSeVoliPodleSirkyMapyBezRezervyVpravo() {
		Assert.assertEquals(267, RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(1280)));
		Assert.assertEquals(267, RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(521)));
		Assert.assertEquals(178, RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(520)));
		Assert.assertEquals(178, RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(432)));
		Assert.assertEquals(106, RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(431)));
		Assert.assertEquals(106, RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(300)));
	}

	private final List<ChallengeZmenenyEvent> udalosti = new ArrayList<>();

	private ChallengeModel model;
	private JPruhPlanovace pruh;

	@After
	public void uklid() {
		UkazatelVykonu.setPodPruhem(false);
	}

	private void priprav() {
		model = new ChallengeModel();
		pruh = new JPruhPlanovace();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {
				udalosti.add((ChallengeZmenenyEvent) event);
				pruh.onEvent((ChallengeZmenenyEvent) event);
			}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
		pruh.inject(model);
		pruh.setSize(1280, 700);
	}

	@Test
	public void pruhJeVidetJenKdyzJeZapnutoAJeCoUkazovat() throws Exception {
		priprav();
		Assert.assertFalse(pruh.isVisible());
		model.setZapnuto(true);
		javax.swing.SwingUtilities.invokeAndWait(() -> {});
		Assert.assertFalse("bez aktivní challenge není co ukázat", pruh.isVisible());
		model.aktivuj(Sablony.cesko360());
		javax.swing.SwingUtilities.invokeAndWait(() -> {});
		Assert.assertTrue(pruh.isVisible());
		Assert.assertTrue(model.jePruhViditelny());
		Assert.assertEquals("ukazatel výkonu je pod pruhem", RozmisteniPrekryvu.UKAZATEL_VYKONU, UkazatelVykonu.oblast());
		model.setZapnuto(false);
		Assert.assertFalse(pruh.isVisible());
		Assert.assertEquals("bez pruhu je ukazatel nahoře", RozmisteniPrekryvu.UKAZATEL_VYKONU_BEZ_PRUHU, UkazatelVykonu.oblast());
		Assert.assertFalse(model.jePruhViditelny());
	}

	@Test
	public void mimoObdelnikPruhuMysPropousti() {
		priprav();
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		final Rectangle p = RozmisteniPrekryvu.pruhPlanovace(RozmisteniPrekryvu.volnaSirkaPruhu(1280));
		Assert.assertTrue(pruh.contains(p.x + 1, p.y + 1));
		Assert.assertTrue(pruh.contains(p.x + p.width - 1, p.y + p.height - 1));
		Assert.assertFalse(pruh.contains(p.x + p.width, p.y));
		Assert.assertFalse(pruh.contains(p.x, p.y + p.height));
		Assert.assertFalse(pruh.contains(p.x - 1, p.y));
		Assert.assertFalse("střed mapy", pruh.contains(640, 350));
	}

	@Test
	public void rozlozeniDetiSeMeniSeSirkouMapy() {
		priprav();
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		for (final int sirka : new int[] { 1280, 500, 400 }) {
			pruh.setSize(sirka, 700);
			pruh.doLayout();
			final Rectangle[] r = JPruhPlanovace.rozloz(RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(sirka)));
			int viditelnych = 0;
			for (final Component c : pruh.getComponents()) {
				if (c.isVisible()) {
					viditelnych++;
					Assert.assertTrue(sirka + ": " + c.getBounds(), pruh.contains(c.getX(), c.getY()));
				}
			}
			int ocekavano = 0;
			for (final Rectangle x : r) {
				ocekavano += x.isEmpty() ? 0 : 1;
			}
			Assert.assertEquals(String.valueOf(sirka), ocekavano, viditelnych);
		}
	}

	private static double jas(final int rgb) {
		final double[] k = new double[3];
		for (int i = 0; i < 3; i++) {
			final double c = ((rgb >> (16 - 8 * i)) & 0xff) / 255.0;
			k[i] = c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
		}
		return 0.2126 * k[0] + 0.7152 * k[1] + 0.0722 * k[2];
	}

	private static double kontrast(final int a, final int b) {
		final double x = jas(a);
		final double y = jas(b);
		return (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05);
	}

	/** Popisek typu je čitelný na podkladu pruhu v každém tématu Metalu, i v tmavém Charcoal. */
	@Test
	public void popisekTypuMaDostatecnyKontrastVeVsechTematech() throws Exception {
		final javax.swing.LookAndFeel puvodni = javax.swing.UIManager.getLookAndFeel();
		final javax.swing.plaf.metal.MetalTheme puvodniTema = javax.swing.plaf.metal.MetalLookAndFeel.getCurrentTheme();
		try {
			final javax.swing.plaf.metal.MetalTheme[] temata = { new javax.swing.plaf.metal.DefaultMetalTheme(), new javax.swing.plaf.metal.OceanTheme(), new cz.geokuk.core.lookandfeel.CharcoalTheme() };
			for (final javax.swing.plaf.metal.MetalTheme tema : temata) {
				javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(tema);
				javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
				final int[] vysledek = new int[1];
				javax.swing.SwingUtilities.invokeAndWait(() -> {
					priprav();
					model.aktivuj(Sablony.cesko360());
					model.setZapnuto(true);
					final javax.swing.JPanel root = new javax.swing.JPanel(null);
					root.setBackground(new java.awt.Color(190, 205, 180));
					root.setSize(1280, 120);
					pruh.setBounds(0, 0, 1280, 120);
					root.add(pruh);
					pruh.doLayout();
					final java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1280, 120, java.awt.image.BufferedImage.TYPE_INT_RGB);
					final java.awt.Graphics2D g = img.createGraphics();
					root.paint(g);
					g.dispose();
					final Rectangle r = JPruhPlanovace.rozloz(RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(1280)))[JPruhPlanovace.TYP];
					final int podklad = img.getRGB(r.x + 1, r.y + 1);
					double nejvic = 1;
					for (int y = r.y; y < r.y + r.height; y++) {
						for (int x = r.x; x < r.x + r.width; x++) {
							nejvic = Math.max(nejvic, kontrast(podklad, img.getRGB(x, y)));
						}
					}
					vysledek[0] = (int) Math.round(nejvic * 100);
				});
				Assert.assertTrue(tema.getName() + ": kontrast textu " + vysledek[0] / 100.0, vysledek[0] >= 450);
			}
		} finally {
			javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(puvodniTema);
			javax.swing.UIManager.setLookAndFeel(puvodni);
		}
	}

	@Test
	public void popiskyOdznakuAFiltruPodleTypuChallenge() {
		Assert.assertEquals("Chybějící sektory", JPruhPlanovace.popisChybejicich(Challenge.Typ.SEKTORY_360));
		Assert.assertEquals("Chybějící políčka D/T", JPruhPlanovace.popisChybejicich(Challenge.Typ.DT_MATICE));
		Assert.assertEquals("Chybějící dny", JPruhPlanovace.popisChybejicich(Challenge.Typ.KALENDAR));
		Assert.assertTrue(JPruhPlanovace.popisKandidatu(Challenge.Typ.SEKTORY_360).endsWith("sektor"));
		Assert.assertTrue(JPruhPlanovace.popisKandidatu(Challenge.Typ.DT_MATICE).endsWith("políčko D/T"));
		Assert.assertTrue(JPruhPlanovace.popisKandidatu(Challenge.Typ.KALENDAR).endsWith("den"));
	}

	@Test
	public void tooltipOdznakuSledujeTypZobrazeneChallenge() throws Exception {
		priprav();
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(dt);
		model.setZapnuto(true);
		final javax.swing.AbstractButton odznak = najdiOdznak();
		Assert.assertEquals("Chybějící políčka D/T – DT", odznak.getToolTipText());
		model.aktivuj(Sablony.cesko360());
		model.setZobrazena(Sablony.cesko360());
		Assert.assertEquals("Chybějící sektory – " + Sablony.cesko360().nazev(), odznak.getToolTipText());
	}

	private javax.swing.AbstractButton najdiOdznak() {
		for (final Component c : pruh.getComponents()) {
			if (c instanceof javax.swing.AbstractButton && !(c instanceof javax.swing.JToggleButton) && ((javax.swing.AbstractButton) c).getToolTipText() != null && ((javax.swing.AbstractButton) c).getToolTipText().startsWith("Chybějící")) {
				return (javax.swing.AbstractButton) c;
			}
		}
		throw new AssertionError("odznak");
	}

	@Test
	public void tooltipTypuAOdznakuMaNazevZobrazeneChallenge() throws Exception {
		priprav();
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		final javax.swing.AbstractButton odznak = najdiOdznak();
		Assert.assertTrue(odznak.getToolTipText(), odznak.getToolTipText().endsWith(" – " + Sablony.cesko360().nazev()));
		boolean typSNazvem = false;
		for (final Component c : pruh.getComponents()) {
			if (c instanceof javax.swing.JLabel && ((javax.swing.JLabel) c).getToolTipText() != null && ((javax.swing.JLabel) c).getToolTipText().contains(Sablony.cesko360().nazev())) {
				typSNazvem = true;
			}
		}
		Assert.assertTrue("štítek typu má v tooltipu název", typSNazvem);
	}
}
