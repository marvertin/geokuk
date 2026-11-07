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

	private static final int[] SIRKY = { RozmisteniPrekryvu.PRUH_PLNY, RozmisteniPrekryvu.PRUH_IKONY, RozmisteniPrekryvu.PRUH_UZKY };

	@Test
	public void prvkySeVejdouNaVsechStupnichANeprekryvajiSe() {
		for (final int sirka : SIRKY) {
			final Rectangle pruh = new Rectangle(RozmisteniPrekryvu.OKRAJ, RozmisteniPrekryvu.OKRAJ, sirka, RozmisteniPrekryvu.VYSKA_PRUHU);
			final Rectangle[] r = JPruhPlanovace.rozloz(sirka);
			final List<Rectangle> videt = new ArrayList<>();
			int konec = 0;
			for (final Rectangle x : r) {
				if (!x.isEmpty()) {
					Assert.assertTrue(sirka + ": " + x, pruh.contains(x));
					videt.add(x);
					konec = Math.max(konec, x.x + x.width);
				}
			}
			for (int i = 0; i < videt.size(); i++) {
				for (int j = i + 1; j < videt.size(); j++) {
					Assert.assertFalse(sirka + ": " + videt.get(i) + " × " + videt.get(j), videt.get(i).intersects(videt.get(j)));
				}
			}
			Assert.assertEquals(sirka + ": vlevo podšívka", pruh.x + 4, r[JPruhPlanovace.UCHYT].x);
			Assert.assertEquals(sirka + ": vpravo podšívka", pruh.x + pruh.width - 4, konec);
		}
	}

	@Test
	public void stupneUbiraji() {
		final Rectangle[] plny = JPruhPlanovace.rozloz(RozmisteniPrekryvu.PRUH_PLNY);
		final Rectangle[] ikony = JPruhPlanovace.rozloz(RozmisteniPrekryvu.PRUH_IKONY);
		final Rectangle[] uzky = JPruhPlanovace.rozloz(RozmisteniPrekryvu.PRUH_UZKY);
		Assert.assertEquals(124, plny[JPruhPlanovace.KANDIDATI].width);
		Assert.assertEquals(116, plny[JPruhPlanovace.NASTAVENI].width);
		Assert.assertEquals("odznak s popiskem „chybí 360“", 80, plny[JPruhPlanovace.ODZNAK].width);
		Assert.assertEquals("jen ikona", 26, ikony[JPruhPlanovace.KANDIDATI].width);
		Assert.assertEquals(26, ikony[JPruhPlanovace.SEKTORY].width);
		Assert.assertEquals(26, ikony[JPruhPlanovace.NASTAVENI].width);
		for (final int i : new int[] { JPruhPlanovace.KANDIDATI, JPruhPlanovace.SEKTORY, JPruhPlanovace.STITKY }) {
			Assert.assertTrue("nejužší bez " + i, uzky[i].isEmpty());
		}
		for (final int i : new int[] { JPruhPlanovace.UCHYT, JPruhPlanovace.VYBER, JPruhPlanovace.ODZNAK, JPruhPlanovace.ZPET, JPruhPlanovace.DAL, JPruhPlanovace.NASTAVENI }) {
			Assert.assertFalse("nejužší s " + i, uzky[i].isEmpty());
		}
		Assert.assertEquals("výběr stejně široký na všech stupních", plny[JPruhPlanovace.VYBER].width, uzky[JPruhPlanovace.VYBER].width);
	}

	@Test
	public void sirkaSeVoliPodleSirkyMapyBezRezervyVpravo() {
		Assert.assertEquals(664, RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(1280)));
		Assert.assertEquals(664, RozmisteniPrekryvu.sirkaPruhu(664));
		Assert.assertEquals(368, RozmisteniPrekryvu.sirkaPruhu(663));
		Assert.assertEquals(368, RozmisteniPrekryvu.sirkaPruhu(368));
		Assert.assertEquals(274, RozmisteniPrekryvu.sirkaPruhu(367));
		Assert.assertEquals("užší nevejde nic, zůstane nejužší", 274, RozmisteniPrekryvu.sirkaPruhu(10));
		Assert.assertEquals(RozmisteniPrekryvu.PRUH_UCHYT, RozmisteniPrekryvu.pruhPlanovace(1000, false).width);
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

	private List<Component> viditelne() {
		final List<Component> v = new ArrayList<>();
		for (final Component c : pruh.getComponents()) {
			if (c.isVisible()) {
				v.add(c);
			}
		}
		return v;
	}

	@Test
	public void vypnutyPlanovacNechaJenUchytZapnutyCelouListu() throws Exception {
		priprav();
		pruh.doLayout();
		Assert.assertTrue("úchyt je vidět i u vypnutého plánovače", pruh.isVisible());
		Assert.assertEquals(1, viditelne().size());
		Assert.assertTrue(pruh.contains(10, 10));
		Assert.assertFalse("za úchytem mapa", pruh.contains(RozmisteniPrekryvu.OKRAJ + RozmisteniPrekryvu.PRUH_UCHYT + 1, 10));
		Assert.assertEquals("ukazatel výkonu pod úchytem", RozmisteniPrekryvu.UKAZATEL_VYKONU, UkazatelVykonu.oblast());
		najdiUchyt().doClick();
		javax.swing.SwingUtilities.invokeAndWait(() -> {});
		Assert.assertTrue("úchyt zapne plánovač", model.jeZapnuto());
		pruh.doLayout();
		Assert.assertEquals("bez aktivní challenge celá lišta", 9, viditelne().size());
		Assert.assertFalse("bez challenge odznak nejde", najdiOdznak().isEnabled());
		model.aktivuj(Sablony.cesko360());
		javax.swing.SwingUtilities.invokeAndWait(() -> {});
		Assert.assertTrue(najdiOdznak().isEnabled());
		Assert.assertTrue(model.jePruhViditelny());
		Assert.assertEquals("ukazatel výkonu se nehýbe", RozmisteniPrekryvu.UKAZATEL_VYKONU, UkazatelVykonu.oblast());
		najdiUchyt().doClick();
		Assert.assertFalse("úchyt vypne plánovač", model.jeZapnuto());
		pruh.doLayout();
		Assert.assertEquals(1, viditelne().size());
		Assert.assertEquals(RozmisteniPrekryvu.UKAZATEL_VYKONU, UkazatelVykonu.oblast());
	}

	private javax.swing.AbstractButton najdiUchyt() {
		return (javax.swing.AbstractButton) pruh.getComponent(0);
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
		for (final int sirka : new int[] { 1280, 700, 400 }) {
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

	/** Typ v tlačítku výběru je čitelný v každém tématu Metalu, i v tmavém Charcoal. */
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
					final Rectangle r = JPruhPlanovace.rozloz(RozmisteniPrekryvu.sirkaPruhu(RozmisteniPrekryvu.volnaSirkaPruhu(1280)))[JPruhPlanovace.VYBER];
					final int podklad = img.getRGB(r.x + 4, r.y + r.height / 2);
					double nejvic = 1;
					for (int y = r.y; y < r.y + r.height; y++) {
						for (int x = r.x; x < r.x + r.width; x++) {
							nejvic = Math.max(nejvic, kontrast(podklad, img.getRGB(x, y)));
						}
					}
					vysledek[0] = (int) Math.round(nejvic * 100);
				});
				// Tučný text tlačítka skinu: stačí kontrast 3 : 1 jako u velkého textu.
				Assert.assertTrue(tema.getName() + ": kontrast textu " + vysledek[0] / 100.0, vysledek[0] >= 300);
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
		Assert.assertEquals("bez výsledku", "Chybějící políčka D/T – počítá se", odznak.getToolTipText());
		Assert.assertEquals("chybí …", odznak.getText());
		model.aktivuj(Sablony.cesko360());
		model.setZobrazena(Sablony.cesko360());
		Assert.assertEquals("Chybějící sektory – počítá se", odznak.getToolTipText());
	}

	private javax.swing.AbstractButton najdiOdznak() {
		for (final Component c : pruh.getComponents()) {
			if (c instanceof javax.swing.AbstractButton && !(c instanceof javax.swing.JToggleButton) && ((javax.swing.AbstractButton) c).getToolTipText() != null && ((javax.swing.AbstractButton) c).getToolTipText().startsWith("Chyb")) {
				return (javax.swing.AbstractButton) c;
			}
		}
		throw new AssertionError("odznak");
	}

	@Test
	public void tooltipVyberuAOdznakuMaNazevZobrazeneChallenge() throws Exception {
		priprav();
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		final javax.swing.AbstractButton odznak = najdiOdznak();
		Assert.assertTrue("odznak má popisek", odznak.getText().startsWith("chybí "));
		final javax.swing.AbstractButton vyber = (javax.swing.AbstractButton) pruh.getComponent(JPruhPlanovace.VYBER);
		Assert.assertEquals("na liště jen typ", "360° ▾", vyber.getText());
		Assert.assertTrue("název challenge je v tooltipu výběru", vyber.getToolTipText().contains(Sablony.cesko360().nazev()));
	}

	@Test
	public void tooltipOdznakuSPoctemACelkem() {
		Assert.assertEquals("Chybí 193 sektorů z 360 – klik ukáže podrobnosti", JPruhPlanovace.tipOdznaku(Challenge.Typ.SEKTORY_360, 193, 360));
		Assert.assertEquals("Chybí 1 sektor z 360 – klik ukáže podrobnosti", JPruhPlanovace.tipOdznaku(Challenge.Typ.SEKTORY_360, 1, 360));
		Assert.assertEquals("Chybí 3 políčka z 81 – klik ukáže podrobnosti", JPruhPlanovace.tipOdznaku(Challenge.Typ.DT_MATICE, 3, 81));
		Assert.assertEquals("Chybí 38 políček z 81 – klik ukáže podrobnosti", JPruhPlanovace.tipOdznaku(Challenge.Typ.DT_MATICE, 38, 81));
		Assert.assertEquals("Chybí 5 dní z 366 – klik ukáže podrobnosti", JPruhPlanovace.tipOdznaku(Challenge.Typ.KALENDAR, 5, 366));
		Assert.assertEquals("bez známého celku", "Chybí 2 políčka – klik ukáže podrobnosti", JPruhPlanovace.tipOdznaku(Challenge.Typ.DT_MATICE, 2, 0));
	}

	@Test
	public void odznakSVysledkemUkazeChybiACelkem() throws Exception {
		priprav();
		model.aktivuj(Sablony.cesko360());
		model.setZapnuto(true);
		final java.util.BitSet b = new java.util.BitSet(360);
		b.set(0, 167);
		javax.swing.SwingUtilities.invokeAndWait(() -> model.nastavVysledek(Sablony.cesko360(), new VysledekSektoru(b, new java.util.BitSet(360), new VysledekSektoru.Kandidati[360], 167)));
		final javax.swing.AbstractButton odznak = najdiOdznak();
		Assert.assertEquals("chybí 193", odznak.getText());
		Assert.assertEquals("Chybí 193 sektorů z 360 – klik ukáže podrobnosti", odznak.getToolTipText());
	}

	/**
	 * Texty tlačítek a odznaku („Nastavení…“, „chybí 360“) se na plné liště vejdou celé ve všech skinech, s písmem systému i se širokým DejaVu Sans (výchozí na
	 * Linuxu), je-li nainstalované.
	 */
	@Test
	public void textyListySeVejdouVeVsechSkinech() throws Exception {
		final javax.swing.LookAndFeel puvodni = javax.swing.UIManager.getLookAndFeel();
		final javax.swing.plaf.metal.MetalTheme puvodniTema = javax.swing.plaf.metal.MetalLookAndFeel.getCurrentTheme();
		final List<String> pisma = new java.util.ArrayList<>(java.util.Collections.singletonList((String) null));
		if (java.util.Arrays.asList(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()).contains("DejaVu Sans")) {
			pisma.add("DejaVu Sans");
		}
		try {
			for (final String pismo : pisma) {
				for (final javax.swing.plaf.metal.MetalTheme tema : new javax.swing.plaf.metal.MetalTheme[] { new javax.swing.plaf.metal.OceanTheme(), new javax.swing.plaf.metal.DefaultMetalTheme(),
						new cz.geokuk.core.lookandfeel.CharcoalTheme() }) {
					javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(tema);
					javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
					if (pismo != null) {
						for (final String klic : new String[] { "Button.font", "ToggleButton.font", "Label.font" }) {
							final java.awt.Font f = javax.swing.UIManager.getFont(klic);
							javax.swing.UIManager.put(klic, new javax.swing.plaf.FontUIResource(pismo, f.getStyle(), f.getSize()));
						}
					}
					javax.swing.SwingUtilities.invokeAndWait(() -> {
						priprav();
						model.aktivuj(Sablony.cesko360());
						model.setZapnuto(true);
						model.nastavVysledek(Sablony.cesko360(), new VysledekSektoru(new java.util.BitSet(360), new java.util.BitSet(360), new VysledekSektoru.Kandidati[360], 0));
						pruh.doLayout();
						Assert.assertEquals("chybí 360", najdiOdznak().getText());
						for (final Component c : pruh.getComponents()) {
							if (c.isVisible() && c instanceof javax.swing.AbstractButton) {
								final javax.swing.AbstractButton b = (javax.swing.AbstractButton) c;
								Assert.assertTrue(tema.getName() + " " + b.getFont().getFamily() + ": „" + b.getText() + "“ " + b.getPreferredSize().width + " > " + b.getWidth(), b.getPreferredSize().width <= b.getWidth());
							}
						}
					});
				}
			}
		} finally {
			javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(puvodniTema);
			javax.swing.UIManager.setLookAndFeel(puvodni);
		}
	}
}
