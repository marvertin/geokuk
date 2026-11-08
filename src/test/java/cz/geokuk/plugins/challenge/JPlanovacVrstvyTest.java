package cz.geokuk.plugins.challenge;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Vrstvy plánovače na mapě vykreslené do obrázku: prstenec, střed, šipka mimo výřez, kandidáti a jejich štítky; potřebuje displej. */
public class JPlanovacVrstvyTest {

	private static final int W = 1280;
	private static final int H = 720;

	private ChallengeModel model;
	private JPlanovacPodKeskami pod;
	private JPlanovacNadKeskami nad;
	private Mou predchoziStred;

	@Before
	public void setUp() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		model = new ChallengeModel();
		model.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {}
		});
		final MapoveUloziste u = new MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
		TvarCache.vymaz();
		pod = new JPlanovacPodKeskami();
		pod.inject(model);
		nad = new JPlanovacNadKeskami();
		nad.inject(model);
	}

	private void zobraz(final VysledekSektoru v, final double stredLat, final double stredLon) throws Exception {
		final Challenge c = model.ulozVlastni(TvarPlanovaceTest.sektory());
		model.aktivuj(c);
		model.setZapnuto(true);
		model.nastavVysledek(c, v, stredLat, stredLon);
		SwingUtilities.invokeAndWait(() -> {});
	}

	private BufferedImage nakresli(final JVrstvaPlanovace slide, final Coord coord) throws Exception {
		final BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
		SwingUtilities.invokeAndWait(() -> {
			slide.setSoord(coord);
			slide.setSize(W, H);
			final Graphics2D g = img.createGraphics();
			slide.paintComponent(g);
			g.dispose();
		});
		return img;
	}

	private static Coord mapa(final double lat, final double lon, final int moumer) {
		return new Coord(moumer, new Wgs(lat, lon).toMou(), new Dimension(W, H), 0);
	}

	/** Výřez, ve kterém leží oba kandidáti z {@link TvarPlanovaceTest#vysledek()} mimo lištu plánovače nahoře. */
	private static Coord mapaSKandidaty() {
		return mapa(50.015, 14.0, 14);
	}

	private static BitSet obsazene(final int od, final int do_) {
		final BitSet b = new BitSet(360);
		b.set(od, do_);
		return b;
	}

	/** Leží na paprsku od středu v daném azimutu pixel dané barvy? */
	private static boolean pixelNaPaprsku(final BufferedImage img, final int cx, final int cy, final double azimut, final int rgb) {
		final double a = Math.toRadians(azimut);
		for (int r = 40; r < 600; r++) {
			final int x = cx + (int) Math.round(r * Math.sin(a));
			final int y = cy - (int) Math.round(r * Math.cos(a));
			if (x < 0 || y < 0 || x >= W || y >= H) {
				return false;
			}
			if (img.getRGB(x, y) == rgb) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void vybranePolickoDtMaStitkyJenSvychKandidatu() throws Exception {
		final Challenge dt = model.ulozVlastni(new Challenge("DT", Challenge.Typ.DT_MATICE, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(dt);
		model.setZapnuto(true);
		final java.util.List<KandidatPolicka> kandidati = new ArrayList<>();
		kandidati.add(new KandidatPolicka("GC1", 50.001, 14.001, "D3/T4", "D3/T4", null, 150));
		kandidati.add(new KandidatPolicka("GC2", 50.002, 13.998, "D3/T4", "D3/T4", null, 250));
		kandidati.add(new KandidatPolicka("GC3", 49.998, 14.002, "D1/T1", "D1/T1", null, 250));
		model.nastavVysledekPolicek(dt, new VysledekPolicek(81, java.util.Arrays.asList("D3/T4", "D1/T1"), kandidati), 50.0, 14.0);
		SwingUtilities.invokeAndWait(() -> {});
		final Coord c = mapa(50.0, 14.0, 14);
		nakresli(nad, c);
		Assert.assertEquals("bez výběru políčka mají štítky všichni kandidáti", 3, nad.obdelnikyStitkuKandidatu().size());
		SwingUtilities.invokeAndWait(() -> model.setVybranePolicko(dt, "D3/T4"));
		nakresli(nad, c);
		Assert.assertEquals("s vybraným políčkem jen jeho kandidáti", 2, nad.obdelnikyStitkuKandidatu().size());
		SwingUtilities.invokeAndWait(() -> model.setVybranePolicko(dt, null));
		nakresli(nad, c);
		Assert.assertEquals("po zrušení výběru zase všichni", 3, nad.obdelnikyStitkuKandidatu().size());
	}

	@Test
	public void prstenecMaSplnenySektorZelenyAChybejiciCerveny() throws Exception {
		zobraz(new VysledekSektoru(obsazene(0, 180), new BitSet(360), new VysledekSektoru.Kandidati[360], 180, new ArrayList<>()), 50.0, 14.0);
		final Coord c = mapa(50.0, 14.0, 13);
		final BufferedImage img = nakresli(pod, c);
		final java.awt.Point s = c.transform(new Wgs(50.0, 14.0).toMou());
		Assert.assertTrue("sektor 90 splněný", pixelNaPaprsku(img, s.x, s.y, 90.5, JPlanovacPodKeskami.SPLNENY.getRGB()));
		Assert.assertTrue("sektor 270 chybí", pixelNaPaprsku(img, s.x, s.y, 270.5, JPlanovacPodKeskami.CHYBI.getRGB()));
		Assert.assertFalse("sektor 270 není zelený", pixelNaPaprsku(img, s.x, s.y, 270.5, JPlanovacPodKeskami.SPLNENY.getRGB()));
		Assert.assertEquals("mimo prstenec je průhledno", 0, img.getRGB(5, 5) >>> 24);
	}

	@Test
	public void bezPrepinaceSektoruSePrstenecNekresli() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		model.setKresliSektory(false);
		final BufferedImage img = nakresli(pod, mapa(50.0, 14.0, 13));
		for (int y = 0; y < H; y += 3) {
			for (int x = 0; x < W; x += 3) {
				Assert.assertEquals(0, img.getRGB(x, y) >>> 24);
			}
		}
	}

	@Test
	public void bezVysledkuSeNicNekresli() throws Exception {
		final BufferedImage img = nakresli(nad, mapa(50.0, 14.0, 13));
		Assert.assertEquals(0, img.getRGB(W / 2, H / 2) >>> 24);
		Assert.assertNull(nad.sipka());
	}

	@Test
	public void stredVOknuMaZnackuVBarvePrehledu() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		final Coord c = mapa(50.0, 14.0, 13);
		final BufferedImage img = nakresli(nad, c);
		final java.awt.Point s = c.transform(new Wgs(50.0, 14.0).toMou());
		Assert.assertEquals(AkcentPlanovace.barva().getRGB(), img.getRGB(s.x - 7, s.y));
		Assert.assertNull("střed je ve výřezu, šipka není", nad.sipka());
		Assert.assertFalse("štítek středu je zapsán", nad.obdelnikyStitku().isEmpty());
	}

	@Test
	public void stredMimoVyrezMaSipkuUOkrajeAKlikVystredujeMapu() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 51.0, 14.0);
		final Coord c = mapa(50.0, 14.0, 13);
		nakresli(nad, c);
		final Rectangle s = nad.sipka();
		Assert.assertNotNull("šipka", s);
		Assert.assertTrue("šipka je v okně: " + s, new Rectangle(0, 0, W, H).contains(s));
		Assert.assertTrue("střed leží na sever, šipka je nahoře: " + s, s.y + s.height / 2 < 80);
		final List<Mou> presunuto = new ArrayList<>();
		nad.inject(new VyrezModel() {
			@Override
			public void presunMapuNaMoustred(final Mou moustred) {
				presunuto.add(moustred);
			}
		});
		final MouseEvent e = new MouseEvent(nad, MouseEvent.MOUSE_CLICKED, 0, MouseEvent.BUTTON1_DOWN_MASK, s.x + s.width / 2, s.y + s.height / 2, 1, false, MouseEvent.BUTTON1);
		SwingUtilities.invokeAndWait(() -> nad.mouseClicked(e, null));
		Assert.assertEquals(1, presunuto.size());
		final Mou stred = new Wgs(51.0, 14.0).toMou();
		Assert.assertEquals(stred.xx, presunuto.get(0).xx);
		Assert.assertEquals(stred.yy, presunuto.get(0).yy);
	}

	@Test
	public void kandidatiMajiNejvicDveStaStitkuAPocetyVeVyrezuSeHlasiModelu() throws Exception {
		final List<VysledekSektoru.KandidatKes> k = new ArrayList<>();
		for (int i = 0; i < 500; i++) {
			k.add(new VysledekSektoru.KandidatKes("GC" + i, 50.0 + (i % 25 - 12) * 0.0002, 14.0 + (i / 25 - 10) * 0.0003, 150 + i % 100, 1000, false));
		}
		zobraz(new VysledekSektoru(obsazene(0, 100), new BitSet(360), new VysledekSektoru.Kandidati[360], 100, k), 50.0, 14.0);
		nakresli(nad, mapa(50.0, 14.0, 15));
		Assert.assertEquals(TvarPlanovace.NEJVIC_ZNACEK, nad.posledniZnacek());
		Assert.assertEquals(TvarPlanovace.NEJVIC_ZNACEK, nad.obdelnikyStitkuKandidatu().size());
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertTrue("model ví, kolik kandidátů je ve výřezu: " + model.kandidatuVeVyrezu(), model.kandidatuVeVyrezu() > TvarPlanovace.NEJVIC_ZNACEK);
	}

	@Test
	public void stitekKandidataStojiPevneVlevoOdIkonyBezVyhybaniStitkum() throws Exception {
		final List<VysledekSektoru.KandidatKes> k = new ArrayList<>();
		k.add(new VysledekSektoru.KandidatKes("GC1", 50.0, 14.0, 150, 1000, false));
		k.add(new VysledekSektoru.KandidatKes("GC2", 50.0, 14.0, 151, 1000, false));
		zobraz(new VysledekSektoru(obsazene(0, 100), new BitSet(360), new VysledekSektoru.Kandidati[360], 100, k), 50.01, 14.0);
		final Coord c = mapa(50.0, 14.0, 16);
		nakresli(nad, c);
		final java.awt.Point p = PixelyMapy.transform(c, PixelyMapy.mou(50.0, 14.0), new java.awt.Point());
		final List<Rectangle> r = nad.obdelnikyStitkuKandidatu();
		Assert.assertEquals(2, r.size());
		for (final Rectangle q : r) {
			Assert.assertEquals("těsně vlevo od ikony (polovina neznámé ikony 8 + mezera 2)", p.x - 8 - 2, q.x + q.width);
			Assert.assertEquals("svisle na středu ikony", p.y, q.y + q.height / 2, 1);
		}
		Assert.assertEquals("štítky na sebe leží (pravý okraj)", r.get(0).x + r.get(0).width, r.get(1).x + r.get(1).width);
		Assert.assertEquals("štítky na sebe leží (y)", r.get(0).y, r.get(1).y);
	}

	@Test
	public void odstupStitkuSeSkalujeSeZvetsenimIkon() throws Exception {
		final List<VysledekSektoru.KandidatKes> k = new ArrayList<>();
		k.add(new VysledekSektoru.KandidatKes("GC1", 50.0, 14.0, 150, 1000, false));
		zobraz(new VysledekSektoru(obsazene(0, 100), new BitSet(360), new VysledekSektoru.Kandidati[360], 100, k), 50.01, 14.0);
		final Coord c = mapa(50.0, 14.0, 16);
		final java.awt.Point p = PixelyMapy.transform(c, PixelyMapy.mou(50.0, 14.0), new java.awt.Point());
		nad.onEvent(new cz.geokuk.plugins.kesoid.mvc.ZvetseniIkonEvent(200));
		nakresli(nad, c);
		final Rectangle r = nad.obdelnikyStitkuKandidatu().get(0);
		Assert.assertEquals("při dvojnásobných ikonách: polovina ikony 16 + mezera 4 vlevo", p.x - 16 - 4, r.x + r.width);
	}

	@Test
	public void stitkyStreduAVybranehoSektoruLeziNadStitkyKandidatu() throws Exception {
		final List<VysledekSektoru.KandidatKes> k = new ArrayList<>();
		k.add(new VysledekSektoru.KandidatKes("GC1", 50.0, 14.0, 150, 1000, false));
		zobraz(new VysledekSektoru(obsazene(0, 100), new BitSet(360), new VysledekSektoru.Kandidati[360], 100, k), 50.0, 14.0);
		final Coord c = mapa(50.0, 14.0, 16);
		final BufferedImage img = nakresli(nad, c);
		final java.awt.Point s = PixelyMapy.transform(c, PixelyMapy.mou(50.0, 14.0), new java.awt.Point());
		Assert.assertEquals("v průniku štítku kandidáta a štítku středu je vidět štítek středu (kreslí se nad ním)", AkcentPlanovace.pozadiStitku().getRGB(), img.getRGB(s.x + 15, s.y - 5));
	}

	@Test
	public void bezPrepinaceStitkuSeKandidatiNekresli() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		model.setKresliStitky(false);
		nakresli(nad, mapaSKandidaty());
		Assert.assertEquals(0, nad.posledniZnacek());
		model.setKresliStitky(true);
		nakresli(nad, mapaSKandidaty());
		Assert.assertEquals(2, nad.posledniZnacek());
	}

	@Test
	public void sipkaSePosunePodMeritkoAPanel() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 49.0, 14.0);
		final Rectangle panel = new Rectangle(W - 330, H - 160, 330, 160);
		nad.setPrekazky(() -> java.util.Collections.singletonList(panel));
		nakresli(nad, mapa(50.0, 14.0, 13));
		final Rectangle s = nad.sipka();
		Assert.assertNotNull(s);
		Assert.assertFalse("šipka s popiskem není na měřítku: " + s, s.intersects(cz.geokuk.framework.RozmisteniPrekryvu.meritko(new Dimension(W, H))));
		Assert.assertFalse("ani na panelu: " + s, s.intersects(panel));
		Assert.assertTrue("zůstala v okně", new Rectangle(0, 0, W, H).contains(s));
	}

	@Test
	public void kandidatiAStitkyNelezouPodPanelem() throws Exception {
		final List<VysledekSektoru.KandidatKes> k = new ArrayList<>();
		for (int i = 0; i < 300; i++) {
			k.add(new VysledekSektoru.KandidatKes("GC" + i, 50.0 + (i % 20 - 10) * 0.0006, 14.0 + (i / 20 - 7) * 0.0009, 150 + i % 100, 1000, false));
		}
		zobraz(new VysledekSektoru(obsazene(0, 100), new BitSet(360), new VysledekSektoru.Kandidati[360], 100, k), 50.0, 14.0);
		final Coord c = mapa(50.0, 14.0, 15);
		nakresli(nad, c);
		final int bezPanelu = nad.posledniZnacek();
		final Rectangle panel = new Rectangle(W / 2, 0, W / 2, H);
		nad.setPrekazky(() -> java.util.Collections.singletonList(panel));
		nakresli(nad, c);
		Assert.assertTrue("pod panelem se kroužky nekreslí: " + nad.posledniZnacek() + " < " + bezPanelu, nad.posledniZnacek() < bezPanelu);
		for (final Rectangle r : nad.obdelnikyStitku()) {
			if (!r.equals(panel) && !cz.geokuk.framework.RozmisteniPrekryvu.meritko(new Dimension(W, H)).equals(r) && r.width < 300) {
				Assert.assertFalse("štítek není pod panelem: " + r, r.intersects(panel) && r.x >= panel.x - 1 && !r.equals(panel));
			}
		}
	}

	@Test
	public void prstenecAStredPrekracujiAntimeridian() throws Exception {
		zobraz(new VysledekSektoru(obsazene(0, 180), new BitSet(360), new VysledekSektoru.Kandidati[360], 180, new ArrayList<>()), -17.7, 180.0);
		final Coord c = mapa(-17.7, 179.99, 13);
		final BufferedImage img = nakresli(pod, c);
		final java.awt.Point s = PixelyMapy.transform(c, PixelyMapy.mou(-17.7, 180.0), new java.awt.Point());
		Assert.assertTrue("východní polovina prstence (za antimeridiánem) je splněná", pixelNaPaprsku(img, s.x, s.y, 90.5, JPlanovacPodKeskami.SPLNENY.getRGB()));
		Assert.assertTrue("západní polovina prstence chybí", pixelNaPaprsku(img, s.x, s.y, 270.5, JPlanovacPodKeskami.CHYBI.getRGB()));
		nad.setPrekazky(java.util.Collections::emptyList);
		nakresli(nad, mapa(-17.7, 178.0, 13));
		final Rectangle sipka = nad.sipka();
		Assert.assertNotNull(sipka);
		Assert.assertTrue("střed leží na východ, šipka je vpravo: " + sipka, sipka.x + sipka.width / 2 > W * 3 / 4);
		nakresli(nad, mapa(-17.7, -178.0, 13));
		Assert.assertTrue("a z druhé strany vlevo: " + nad.sipka(), nad.sipka().x + nad.sipka().width / 2 < W / 4);
	}

	@Test
	public void klinMaVTretiTematechDostatecnyKontrastKMape() {
		final javax.swing.plaf.metal.MetalTheme[] temata = { new DefaultMetalTheme(), new OceanTheme(), new cz.geokuk.core.lookandfeel.CharcoalTheme() };
		final javax.swing.LookAndFeel puvodni = UIManager.getLookAndFeel();
		final javax.swing.plaf.metal.MetalTheme puvodniTema = MetalLookAndFeel.getCurrentTheme();
		try {
			for (final javax.swing.plaf.metal.MetalTheme t : temata) {
				MetalLookAndFeel.setCurrentTheme(t);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				final Color a = AkcentPlanovace.barva();
				final Color mapa = new Color(0xF1EEE8);
				final double al = 90 / 255.0;
				final Color smes = new Color((int) (a.getRed() * al + mapa.getRed() * (1 - al)), (int) (a.getGreen() * al + mapa.getGreen() * (1 - al)), (int) (a.getBlue() * al + mapa.getBlue() * (1 - al)));
				Assert.assertTrue(t.getName() + " klín proti mapě " + kontrast(smes, mapa), kontrast(smes, mapa) >= 1.5);
			}
		} catch (final Exception e) {
			throw new AssertionError(e);
		} finally {
			MetalLookAndFeel.setCurrentTheme(puvodniTema);
			try {
				UIManager.setLookAndFeel(puvodni);
			} catch (final Exception e) {
				// bez vlivu na test
			}
		}
	}

	@Test
	public void sipkaDoleNezasahujeDoMeritkaVCelemRozsahuSmeru() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 48.0, 14.0);
		final Rectangle meritko = cz.geokuk.framework.RozmisteniPrekryvu.meritko(new Dimension(W, H));
		meritko.grow(14, 0);
		final Coord c = mapa(50.0, 14.0, 13);
		int poblizLevehoKonce = 0;
		for (final double lat : new double[] { 49.2, 48.5 }) {
			for (double lon = 11.0; lon <= 17.0; lon += 0.1) {
				zobraz(TvarPlanovaceTest.vysledek(), lat, lon);
				nakresli(nad, c);
				final Rectangle s = nad.sipka();
				Assert.assertNotNull("šipka pro " + lat + ", " + lon, s);
				Assert.assertFalse("šipka a štítek mimo měřítko i s přesahem popisku (" + lat + ", " + lon + "): " + s + " × " + meritko, s.intersects(meritko));
				if (s.y > H - 120 && Math.abs(s.x + s.width / 2 - meritko.x) < 80) {
					poblizLevehoKonce++;
				}
			}
		}
		Assert.assertTrue("zkouška opravdu zasáhla okolí konců měřítka: " + poblizLevehoKonce, poblizLevehoKonce > 0);
	}

	@Test
	public void stitkyStreduAVybranehoSektoruNepokryvajiZnackuStredu() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		model.nastavVybranyKandidat(model.zobrazena(), new VybranyKandidat("GC1", 50.0005, 14.0004, 150, 100));
		SwingUtilities.invokeAndWait(() -> {});
		final Coord c = mapa(50.0, 14.0, 17);
		nakresli(nad, c);
		final java.awt.Point s = PixelyMapy.transform(c, PixelyMapy.mou(50.0, 14.0), new java.awt.Point());
		final Rectangle znacka = new Rectangle(s.x - 12, s.y - 12, 24, 24);
		int kryji = 0;
		for (final Rectangle r : nad.obdelnikyStitku()) {
			if (r.intersects(znacka)) {
				kryji++;
				Assert.assertEquals("jen vlastní rezervace značky: " + r, znacka.width, r.width);
			}
		}
		Assert.assertEquals(1, kryji);
	}

	@Test
	public void podkladSeStaviMimoEdtADoDokonceniSeNekresli() throws Exception {
		final List<Runnable> fronta = new ArrayList<>();
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		pod.setStavba(fronta::add);
		nad.setStavba(fronta::add);
		final Coord c = mapaSKandidaty();
		BufferedImage img = nakresli(nad, c);
		Assert.assertEquals("podklad se ještě staví", 0, img.getRGB(W / 2, H / 2) >>> 24);
		Assert.assertEquals("stavba je jedna pro obě vrstvy", 1, fronta.size());
		SwingUtilities.invokeAndWait(() -> fronta.get(0).run());
		SwingUtilities.invokeAndWait(() -> {});
		SwingUtilities.invokeAndWait(() -> {});
		img = nakresli(nad, c);
		Assert.assertNotEquals("po dokončení se kreslí", 0, nad.obdelnikyStitku().size());
		Assert.assertEquals(2, nad.posledniZnacek());
	}

	@Test
	public void sipkaNajdeVolneMistoIKdyzJeDoleFotomapaMeritkoADetail() throws Exception {
		final Rectangle fotomapa = new Rectangle(0, H - 240, 260, 240);
		final Rectangle detail = new Rectangle(W - 340, H - 430, 340, 430);
		nad.setPrekazky(() -> java.util.Arrays.asList(fotomapa, detail));
		final Rectangle meritko = cz.geokuk.framework.RozmisteniPrekryvu.meritko(new Dimension(W, H));
		meritko.grow(14, 0);
		final Coord c = mapa(50.0, 14.0, 13);
		int pocet = 0;
		for (final double lat : new double[] { 49.0, 49.3, 49.7 }) {
			for (double lon = 12.5; lon <= 14.2; lon += 0.1) {
				zobraz(TvarPlanovaceTest.vysledek(), lat, lon);
				nakresli(nad, c);
				final Rectangle s = nad.sipka();
				Assert.assertNotNull(s);
				pocet++;
				Assert.assertFalse("šipka a štítek mimo fotomapu (" + lat + ", " + lon + "): " + s, s.intersects(fotomapa));
				Assert.assertFalse("mimo detail (" + lat + ", " + lon + "): " + s, s.intersects(detail));
				Assert.assertFalse("mimo měřítko (" + lat + ", " + lon + "): " + s, s.intersects(meritko));
			}
		}
		Assert.assertTrue(pocet > 30);
	}

	@Test
	public void selhaniStavbyPodkladuNerozbijeKresleniAZalogujeSeJednou() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		nakresli(nad, mapaSKandidaty());
		Assert.assertEquals("nejdřív se kreslí", 2, nad.posledniZnacek());
		final List<VysledekSektoru.KandidatKes> k = new ArrayList<>();
		k.add(null);
		model.nastavVysledek(model.zobrazena(), new VysledekSektoru(obsazene(0, 100), new BitSet(360), new VysledekSektoru.Kandidati[360], 100, k), 50.0, 14.0);
		SwingUtilities.invokeAndWait(() -> {});
		final BufferedImage img = nakresli(nad, mapa(50.0, 14.0, 14));
		Assert.assertEquals("po selhání se nekreslí ani předchozí podklad", 0, nad.posledniZnacek());
		Assert.assertEquals("nic se nekreslí", 0, img.getRGB(W / 2, H / 2) >>> 24);
		nakresli(nad, mapa(50.0, 14.0, 14));
		nakresli(pod, mapa(50.0, 14.0, 14));
		Assert.assertTrue(TvarCache.selhalo());
	}

	@Test
	public void sipajiciSeVybertaNeprepisujeStarouStavbuAStaraSeZrusi() throws Exception {
		final List<Runnable> fronta = new ArrayList<>();
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		final int[] prekresleni = { 0 };
		final JPlanovacNadKeskami n = new JPlanovacNadKeskami() {
			private static final long serialVersionUID = 1L;

			@Override
			public void repaint() {
				prekresleni[0]++;
			}
		};
		n.inject(model);
		n.setStavba(fronta::add);
		final Coord c = mapaSKandidaty();
		nakresli(n, c);
		nakresli(n, c);
		nakresli(n, c);
		Assert.assertEquals("stavba se zadala jednou", 1, fronta.size());
		model.nastavVybranyKandidat(model.zobrazena(), new VybranyKandidat("GC1", 50.01, 14.0, 150, 1200));
		SwingUtilities.invokeAndWait(() -> {});
		nakresli(n, c);
		Assert.assertEquals("nový stav, nová stavba", 2, fronta.size());
		final int zrusenychPred = TvarCache.ZRUSENYCH.get();
		SwingUtilities.invokeAndWait(() -> fronta.get(0).run());
		SwingUtilities.invokeAndWait(() -> {});
		nakresli(n, c);
		Assert.assertEquals("stará stavba se zrušila, nedokončila", zrusenychPred + 1, TvarCache.ZRUSENYCH.get());
		Assert.assertEquals("zrušená stavba nic neukáže", 0, n.posledniZnacek());
		SwingUtilities.invokeAndWait(() -> fronta.get(1).run());
		SwingUtilities.invokeAndWait(() -> {});
		SwingUtilities.invokeAndWait(() -> {});
		nakresli(n, c);
		Assert.assertEquals(2, n.posledniZnacek());
		Assert.assertEquals("na každou stavbu jedno překreslení, i když se při stavbě kreslilo víckrát", 2, prekresleni[0]);
	}

	@Test
	public void poVypnutiPlanovaceSePodkladZapomene() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		nakresli(nad, mapa(50.0, 14.0, 14));
		Assert.assertFalse(TvarCache.prazdna());
		model.setZapnuto(false);
		SwingUtilities.invokeAndWait(() -> {});
		nakresli(nad, mapa(50.0, 14.0, 14));
		Assert.assertTrue(TvarCache.prazdna());
	}

	@Test
	public void priSkrytychKesoidechSeKandidatiNekresliAleStredAPrstenecAno() throws Exception {
		zobraz(TvarPlanovaceTest.vysledek(), 50.0, 14.0);
		model.nastavVybranyKandidat(model.zobrazena(), new VybranyKandidat("GC1", 50.01, 14.0, 150, 1200));
		SwingUtilities.invokeAndWait(() -> {});
		final Coord c = mapa(50.005, 14.0, 14);
		final java.awt.Point s = PixelyMapy.transform(c, PixelyMapy.mou(50.0, 14.0), new java.awt.Point());
		nakresli(nad, c);
		final int obdelniku = nad.obdelnikyStitku().size();
		Assert.assertEquals("kandidáti jsou vidět", 1, nad.posledniZnacek() > 0 ? 1 : 0);
		nad.onEvent(new cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent(false));
		final BufferedImage img = nakresli(nad, c);
		Assert.assertEquals("žádné kroužky kandidátů", 0, nad.posledniZnacek());
		final int pocetSkryte = nad.obdelnikyStitku().size();
		Assert.assertTrue("kandidáti odpadli: " + pocetSkryte + " < " + obdelniku, pocetSkryte < obdelniku);
		model.nastavVybranyKandidat(model.zobrazena(), null);
		SwingUtilities.invokeAndWait(() -> {});
		nakresli(nad, c);
		Assert.assertEquals("ani štítek vybraného sektoru (stejně štítků jako bez výběru)", nad.obdelnikyStitku().size(), pocetSkryte);
		model.nastavVybranyKandidat(model.zobrazena(), new VybranyKandidat("GC1", 50.01, 14.0, 150, 1200));
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("značka středu zůstává", AkcentPlanovace.barva().getRGB(), img.getRGB(s.x - 7, s.y));
		nad.onEvent(new cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent(true));
		nakresli(nad, c);
		Assert.assertTrue("po zapnutí zpět", nad.posledniZnacek() > 0);
	}

	@Test
	public void priSkrytychKesoidechNeniCaraKKandidatoviAlePrstenecAKlinZustavaji() throws Exception {
		zobraz(new VysledekSektoru(obsazene(0, 100), new BitSet(360), new VysledekSektoru.Kandidati[360], 100, TvarPlanovaceTest.vysledek().vsichniKandidati()), 50.0, 14.0);
		model.nastavVybranyKandidat(model.zobrazena(), new VybranyKandidat("GC1", 50.01, 14.0, 150, 1200));
		SwingUtilities.invokeAndWait(() -> {});
		final Coord c = mapa(50.005, 14.0, 14);
		final java.awt.Point a = PixelyMapy.transform(c, PixelyMapy.mou(50.0, 14.0), new java.awt.Point());
		final java.awt.Point b = PixelyMapy.transform(c, PixelyMapy.mou(50.01, 14.0), new java.awt.Point());
		BufferedImage img = nakresli(pod, c);
		Assert.assertTrue("čára je vidět", pixelNaUsecce(img, a, b, AkcentPlanovace.barva().getRGB()));
		pod.onEvent(new cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent(false));
		img = nakresli(pod, c);
		Assert.assertFalse("čára zmizí", pixelNaUsecce(img, a, b, AkcentPlanovace.barva().getRGB()));
		final Coord siroka = mapa(50.0, 14.0, 13);
		final java.awt.Point stred = PixelyMapy.transform(siroka, PixelyMapy.mou(50.0, 14.0), new java.awt.Point());
		Assert.assertTrue("prstenec zůstává", pixelNaPaprsku(nakresli(pod, siroka), stred.x, stred.y, 90.5, JPlanovacPodKeskami.SPLNENY.getRGB()));
	}

	private static boolean pixelNaUsecce(final BufferedImage img, final java.awt.Point a, final java.awt.Point b, final int rgb) {
		final int n = Math.max(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
		for (int i = 0; i <= n; i++) {
			final int x = a.x + (b.x - a.x) * i / Math.max(1, n);
			final int y = a.y + (b.y - a.y) * i / Math.max(1, n);
			if (x >= 0 && y >= 0 && x < W && y < H && img.getRGB(x, y) == rgb) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void stitekKandidataMaVyplnAkcentuBezKrouzkuAHalaNadIkonou() throws Exception {
		final List<VysledekSektoru.KandidatKes> k = new ArrayList<>();
		k.add(new VysledekSektoru.KandidatKes("GC1", 50.0, 14.0, 150, 1000, false));
		zobraz(new VysledekSektoru(obsazene(0, 100), new BitSet(360), new VysledekSektoru.Kandidati[360], 100, k), 50.01, 14.0);
		final Coord c = mapa(50.0, 14.0, 16);
		final BufferedImage img = nakresli(nad, c);
		final java.awt.Point p = PixelyMapy.transform(c, PixelyMapy.mou(50.0, 14.0), new java.awt.Point());
		for (int dy = -6; dy <= 6; dy++) {
			for (int dx = -6; dx <= 6; dx++) {
				Assert.assertEquals("nad místem keše nic není (kroužek ani halo): " + dx + "," + dy, 0, img.getRGB(p.x + dx, p.y + dy) >>> 24);
			}
		}
		Assert.assertEquals(1, nad.obdelnikyStitkuKandidatu().size());
		final Rectangle stitek = nad.obdelnikyStitkuKandidatu().get(0);
		Assert.assertEquals("výplň štítku je akcent", AkcentPlanovace.barva().getRGB(), img.getRGB(stitek.x + 1, stitek.y + stitek.height / 2));
	}

	@Test
	public void bilyTextNaZvyraznenemStitkuMaVeTrechTematechKontrast() {
		final javax.swing.plaf.metal.MetalTheme[] temata = { new DefaultMetalTheme(), new OceanTheme(), new cz.geokuk.core.lookandfeel.CharcoalTheme() };
		final javax.swing.LookAndFeel puvodni = UIManager.getLookAndFeel();
		final javax.swing.plaf.metal.MetalTheme puvodniTema = MetalLookAndFeel.getCurrentTheme();
		try {
			for (final javax.swing.plaf.metal.MetalTheme t : temata) {
				MetalLookAndFeel.setCurrentTheme(t);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				Assert.assertTrue(t.getName() + " bílý text na akcentu " + kontrast(Color.WHITE, AkcentPlanovace.barva()), kontrast(Color.WHITE, AkcentPlanovace.barva()) >= 4.5);
			}
		} catch (final Exception e) {
			throw new AssertionError(e);
		} finally {
			MetalLookAndFeel.setCurrentTheme(puvodniTema);
			try {
				UIManager.setLookAndFeel(puvodni);
			} catch (final Exception e) {
				// bez vlivu na test
			}
		}
	}

	@Test
	public void stitekMaCitelnyTextVeTrechTematech() {
		final javax.swing.plaf.metal.MetalTheme[] temata = { new DefaultMetalTheme(), new OceanTheme(), new cz.geokuk.core.lookandfeel.CharcoalTheme() };
		final javax.swing.LookAndFeel puvodni = UIManager.getLookAndFeel();
		final javax.swing.plaf.metal.MetalTheme puvodniTema = MetalLookAndFeel.getCurrentTheme();
		try {
			for (final javax.swing.plaf.metal.MetalTheme t : temata) {
				MetalLookAndFeel.setCurrentTheme(t);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				Assert.assertTrue(t.getName() + " text/pozadí", kontrast(AkcentPlanovace.textStitku(), AkcentPlanovace.pozadiStitku()) >= 4.4);
				Assert.assertTrue(t.getName() + " akcent/mapa", kontrast(AkcentPlanovace.barva(), new Color(0xF1EEE8)) >= 4.5);
			}
		} catch (final Exception e) {
			throw new AssertionError(e);
		} finally {
			MetalLookAndFeel.setCurrentTheme(puvodniTema);
			try {
				UIManager.setLookAndFeel(puvodni);
			} catch (final Exception e) {
				// bez vlivu na test
			}
		}
	}

	private static double jas(final Color c) {
		final double[] v = { c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0 };
		for (int i = 0; i < 3; i++) {
			v[i] = v[i] <= 0.03928 ? v[i] / 12.92 : Math.pow((v[i] + 0.055) / 1.055, 2.4);
		}
		return 0.2126 * v[0] + 0.7152 * v[1] + 0.0722 * v[2];
	}

	private static double kontrast(final Color a, final Color b) {
		final double x = jas(a);
		final double y = jas(b);
		return (Math.max(x, y) + 0.05) / (Math.min(x, y) + 0.05);
	}
}
