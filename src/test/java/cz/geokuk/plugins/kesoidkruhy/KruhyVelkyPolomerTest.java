package cz.geokuk.plugins.kesoidkruhy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Jednotkové kruhy v metrech se kreslí i při velkém přiblížení, kdy zakrývají většinu obrazovky. */
public class KruhyVelkyPolomerTest {

	private static final Mou MISTO = new Wgs(50.0755, 14.4378).toMou();
	private static final int W = 800;
	private static final int H = 600;

	private static Wpt wpt(final Mou mou) {
		final Wpt w = mock(Wpt.class);
		when(w.getMou()).thenReturn(mou);
		return w;
	}

	private static JZvyraznovaciKruhySlide slide(final int zoom, final List<Mou> kese) {
		final List<Wpt> wpts = new ArrayList<>();
		for (final Mou m : kese) {
			wpts.add(wpt(m));
		}
		final Indexator<Wpt> indexator = Indexator.postav(BoundingRect.ALL, wpts, w -> w.getMou().xx, w -> w.getMou().yy);
		final KesBag bag = mock(KesBag.class);
		when(bag.getIndexator()).thenReturn(indexator);
		final JZvyraznovaciKruhySlide slide = new JZvyraznovaciKruhySlide();
		slide.setSoord(new Coord(zoom, MISTO, new Dimension(W, H), 0));
		slide.setSize(W, H);
		slide.onEvent(new KeskyVyfiltrovanyEvent(bag, bag));
		return slide;
	}

	private static List<Mou> jedna() {
		final List<Mou> l = new ArrayList<>();
		l.add(MISTO);
		return l;
	}

	private static KruhyPreferencesChangeEvent metry(final int metru) {
		final KruhySettings s = new KruhySettings();
		s.setOnoff(true);
		s.setJednotkovaVelikost(true);
		s.setPolomerMetru(metru);
		s.setBarva(new Color(200, 0, 0, 255));
		return new KruhyPreferencesChangeEvent(s);
	}

	private static KruhyPreferencesChangeEvent pixely(final int px) {
		final KruhySettings s = new KruhySettings();
		s.setOnoff(true);
		s.setVelikost(px);
		s.setBarva(new Color(200, 0, 0, 255));
		return new KruhyPreferencesChangeEvent(s);
	}

	private static BufferedImage nakresli(final JZvyraznovaciKruhySlide slide) {
		final BufferedImage i = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = i.createGraphics();
		slide.paintComponent(g);
		g.dispose();
		return i;
	}

	private static boolean obarveno(final BufferedImage i, final int x, final int y) {
		return (i.getRGB(x, y) >>> 24) != 0;
	}

	private static double polomerPx(final JZvyraznovaciKruhySlide s, final int metru) {
		return metru * s.getSoord().getPixluNaMetr();
	}

	@Test
	public void kruh161mPriDetailuZakryvaObrazovkuAKresliSe() {
		final JZvyraznovaciKruhySlide s = slide(20, jedna());
		Assert.assertTrue("poloměr 161 m je při tomto přiblížení nad půl obrazovky: " + polomerPx(s, 161), polomerPx(s, 161) > 500);
		s.onEvent(metry(161));
		final BufferedImage i = nakresli(s);
		Assert.assertTrue(obarveno(i, 10, 10));
		Assert.assertTrue(obarveno(i, W - 10, H - 10));
		Assert.assertTrue(obarveno(i, W / 2, H / 2));
	}

	@Test
	public void kruh1kmPriDetailuSeKresli() {
		final JZvyraznovaciKruhySlide s = slide(20, jedna());
		s.onEvent(metry(1000));
		final BufferedImage i = nakresli(s);
		Assert.assertTrue(obarveno(i, 5, 5));
		Assert.assertTrue(obarveno(i, W - 5, H - 5));
	}

	@Test(timeout = 5000)
	public void obriPolomerSeKresliRychleBezPretecenia() {
		final JZvyraznovaciKruhySlide s = slide(22, jedna());
		Assert.assertTrue("poloměr přes milion pixelů: " + polomerPx(s, 50_000), polomerPx(s, 50_000) > 1_000_000);
		s.onEvent(metry(50_000));
		final BufferedImage i = nakresli(s);
		Assert.assertTrue(obarveno(i, 0, 0));
		Assert.assertTrue(obarveno(i, W - 1, H - 1));
	}

	@Test
	public void okrajKruhuSeKresliJenKdyzProtinaVyrez() {
		final JZvyraznovaciKruhySlide s = slide(17, jedna());
		final double r = polomerPx(s, 161);
		Assert.assertTrue("okraj uvnitř okna: " + r, r > 100 && r < 380);
		s.onEvent(metry(161));
		final BufferedImage i = nakresli(s);
		Assert.assertTrue(obarveno(i, W / 2 + (int) r - 5, H / 2));
		Assert.assertFalse(obarveno(i, W / 2 + (int) r + 5, H / 2));
	}

	@Test
	public void mnohoKesiBezOhleduNaPokryti() {
		final List<Mou> kese = new ArrayList<>();
		for (int k = 0; k < 200; k++) {
			kese.add(MISTO);
		}
		final JZvyraznovaciKruhySlide s = slide(18, kese);
		s.onEvent(metry(300));
		Assert.assertTrue("v metrech se pravidlo pokrytí nepoužije", obarveno(nakresli(s), W / 2, H / 2));
	}

	@Test
	public void vPixelechHustotaDalSkryva() {
		final List<Mou> kese = new ArrayList<>();
		for (int k = 0; k < 200; k++) {
			kese.add(MISTO);
		}
		final JZvyraznovaciKruhySlide s = slide(18, kese);
		s.onEvent(pixely(100));
		Assert.assertFalse("v pixelech příliš husté kruhy mizí", obarveno(nakresli(s), W / 2, H / 2));
		final JZvyraznovaciKruhySlide ridke = slide(18, jedna());
		ridke.onEvent(pixely(100));
		Assert.assertTrue(obarveno(nakresli(ridke), W / 2, H / 2));
	}

	@Test
	public void podTriPixlySeNekresli() {
		final JZvyraznovaciKruhySlide s = slide(12, jedna());
		s.onEvent(metry(1));
		Assert.assertTrue(polomerPx(s, 1) < 3);
		Assert.assertFalse(obarveno(nakresli(s), W / 2, H / 2));
	}

	/** Kruh, jehož střed je daleko od okna a okraj protíná okno, se vykreslí jen tím, co je v okně. */
	@Test
	public void obriKruhSOkrajemUvnitrOknaSeKresliCastecne() {
		final JZvyraznovaciKruhySlide ref = slide(22, jedna());
		final double r = polomerPx(ref, 5000);
		Assert.assertTrue(r > 100_000);
		// keš vlevo od okna tak, aby okraj kruhu procházel okolo x = 600
		final Mou vlevo = new Mou(MISTO.xx - (int) Math.round((r - 200) * mouNaPixel(ref)), MISTO.yy);
		final JZvyraznovaciKruhySlide s = slide(22, java.util.Collections.singletonList(vlevo));
		final Point p = s.getSoord().transform(vlevo);
		Assert.assertEquals("střed daleko vlevo", W / 2 - (r - 200), p.x, 2);
		s.onEvent(metry(5000));
		final BufferedImage i = nakresli(s);
		Assert.assertTrue("uvnitř kruhu", obarveno(i, 100, H / 2));
		Assert.assertFalse("za okrajem kruhu", obarveno(i, W - 50, H / 2));
	}

	private static double mouNaPixel(final JZvyraznovaciKruhySlide s) {
		final Point a = s.getSoord().transform(MISTO);
		final Point b = s.getSoord().transform(new Mou(MISTO.xx + 1_000_000, MISTO.yy));
		return 1_000_000.0 / Math.abs(b.x - a.x);
	}

	private static JZvyraznovaciKruhySlide.Rozhodnuti rozhodni(final boolean vMetrech, final double r, final int pocet, final int limit, final boolean husta) {
		return JZvyraznovaciKruhySlide.rozhodni(vMetrech, r, pocet, limit, husta);
	}

	@Test
	public void rozhodnutiOKresleniAJehoDuvod() {
		final JZvyraznovaciKruhySlide.Rozhodnuti kresli = JZvyraznovaciKruhySlide.Rozhodnuti.KRESLI;
		// metry: velký poloměr i řídké kruhy se kreslí, pokrytí se nehlídá
		Assert.assertEquals(kresli, rozhodni(true, 5000, 1, 90_000, true));
		Assert.assertEquals(kresli, rozhodni(true, 3, 500, 90_000, true));
		// pixely: hustota dál skrývá
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.PRILIS_HUSTE, rozhodni(false, 100, 200, 90_000, true));
		Assert.assertEquals(kresli, rozhodni(false, 100, 2, 90_000, false));
		// počet nad limitem skrývá v obou režimech
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.LIMIT_POCTU, rozhodni(true, 50, 90_001, 90_000, false));
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.LIMIT_POCTU, rozhodni(false, 50, 90_001, 90_000, false));
		Assert.assertEquals("na hranici limitu se ještě kreslí", kresli, rozhodni(true, 50, 90_000, 90_000, false));
		// bez poloměru se nekreslí
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.POLOMER_PRILIS_MALY, rozhodni(true, 0, 1, 90_000, false));
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.POLOMER_PRILIS_MALY, rozhodni(false, 0, 1, 90_000, false));
		// nejdřív poloměr, pak limit
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.POLOMER_PRILIS_MALY, rozhodni(true, 0, 99_999, 90_000, true));
	}

	@Test
	public void geometrieKruhuAVyrezu() {
		final PolomerKruhu.Kresleni nic = PolomerKruhu.Kresleni.NIC;
		final PolomerKruhu.Kresleni vypln = PolomerKruhu.Kresleni.VYPLN_VYREZ;
		final PolomerKruhu.Kresleni kruh = PolomerKruhu.Kresleni.KRUH;
		// výřez 0,0 – 800,600
		Assert.assertEquals(vypln, PolomerKruhu.kresleni(400, 300, 1000, 0, 0, 800, 600));
		Assert.assertEquals("přesně do rohu", vypln, PolomerKruhu.kresleni(400, 300, 500, 0, 0, 800, 600));
		Assert.assertEquals("o chlup menší", kruh, PolomerKruhu.kresleni(400, 300, 499.9, 0, 0, 800, 600));
		Assert.assertEquals(kruh, PolomerKruhu.kresleni(400, 300, 100, 0, 0, 800, 600));
		Assert.assertEquals("kruh vlevo mimo", nic, PolomerKruhu.kresleni(-500, 300, 400, 0, 0, 800, 600));
		Assert.assertEquals("dotýká se okraje", kruh, PolomerKruhu.kresleni(-400, 300, 400, 0, 0, 800, 600));
		Assert.assertEquals("střed mimo, kruh protíná", kruh, PolomerKruhu.kresleni(-50, -50, 100, 0, 0, 800, 600));
		Assert.assertEquals("střed mimo, kruh nezasáhne roh", nic, PolomerKruhu.kresleni(-50, -50, 60, 0, 0, 800, 600));
		Assert.assertEquals("obří kruh se středem daleko, okno uvnitř", vypln, PolomerKruhu.kresleni(-1.0e6, 300, 1.0e6 + 900, 0, 0, 800, 600));
		Assert.assertEquals("obří kruh, okraj prochází oknem", kruh, PolomerKruhu.kresleni(-1.0e6, 300, 1.0e6 + 400, 0, 0, 800, 600));
	}

	@Test
	public void slozeneKrytiVrstevOdpovidaPostupnemuKresleni() {
		Assert.assertEquals(0, PolomerKruhu.slozAlfu(70, 0));
		Assert.assertEquals(70, PolomerKruhu.slozAlfu(70, 1));
		Assert.assertEquals(255, PolomerKruhu.slozAlfu(255, 5));
		Assert.assertEquals(0, PolomerKruhu.slozAlfu(0, 100));
		// dvě vrstvy: 1 - (1 - 70/255)^2 = 0,4735
		Assert.assertEquals(121, PolomerKruhu.slozAlfu(70, 2));
		Assert.assertTrue(PolomerKruhu.slozAlfu(70, 90_000) >= 254);
		for (final int pocet : new int[] { 2, 3, 7 }) {
			final BufferedImage i = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = i.createGraphics();
			g.setColor(new Color(200, 0, 0, 70));
			for (int k = 0; k < pocet; k++) {
				g.fillRect(0, 0, 2, 2);
			}
			g.dispose();
			Assert.assertEquals("vrstev " + pocet, i.getRGB(0, 0) >>> 24, PolomerKruhu.slozAlfu(70, pocet), 2);
		}
	}

	@Test(timeout = 5000)
	public void mnohoKruhuPokryvajicichObrazovkuSeKresliRychleAKrytiSeSklada() {
		final List<Mou> kese = new ArrayList<>();
		for (int k = 0; k < 3000; k++) {
			kese.add(MISTO);
		}
		final JZvyraznovaciKruhySlide s = slide(20, kese);
		final KruhySettings st = new KruhySettings();
		st.setOnoff(true);
		st.setJednotkovaVelikost(true);
		st.setPolomerMetru(161);
		st.setBarva(new Color(200, 0, 0, 70));
		s.onEvent(new KruhyPreferencesChangeEvent(st));
		final BufferedImage i = nakresli(s);
		Assert.assertTrue("3000 vrstev krytí 70 je prakticky plné", (i.getRGB(W / 2, H / 2) >>> 24) >= 254);

		final JZvyraznovaciKruhySlide tri = slide(20, java.util.Arrays.asList(MISTO, MISTO, MISTO));
		tri.onEvent(new KruhyPreferencesChangeEvent(st));
		final int alfa = nakresli(tri).getRGB(W / 2, H / 2) >>> 24;
		Assert.assertEquals(PolomerKruhu.slozAlfu(70, 3), alfa, 2);
	}

	// --- pojistka: rozpočet ploch kruhů s hranou ve výřezu, kreslí se nejbližší ke středu ---

	/** Poloměr kruhů v testech pojistky: v okně 800×600 jsou jen částečné a drahé. */
	private static final int R_PX = 250;

	/** Limit pro poloměr, který slide skutečně dostane (z metrů zaokrouhlených na celé). */
	private static int limitProOkno() {
		final JZvyraznovaciKruhySlide ref = slide(17, jedna());
		final int metru = polomerMetruProPixely(ref, R_PX);
		return PolomerKruhu.limitKruhu(metru * ref.getSoord().getPixluNaMetr(), (double) W * H);
	}

	private static int polomerMetruProPixely(final JZvyraznovaciKruhySlide s, final int pixelu) {
		return (int) Math.round(pixelu / s.getSoord().getPixluNaMetr());
	}

	@Test
	public void rozpoctovaKonstanta() {
		Assert.assertEquals(15_000_000L, PolomerKruhu.ROZPOCET_PLOCHY);
		Assert.assertEquals(2000, PolomerKruhu.MAX_CASTECNYCH_KRUHU);
	}

	@Test
	public void maleKruhySeKresliVsechnyAzDoTvrdehoStropu() {
		final double fhd = 1920.0 * 1080;
		Assert.assertEquals("r = 26 px: plocha 2124 px, rozpočet vystačí na tisíce", PolomerKruhu.MAX_CASTECNYCH_KRUHU, PolomerKruhu.limitKruhu(26, fhd));
		Assert.assertEquals(PolomerKruhu.MAX_CASTECNYCH_KRUHU, PolomerKruhu.limitKruhu(3, fhd));
		Assert.assertTrue("100 kruhů po 26 px se vejde", PolomerKruhu.limitKruhu(26, fhd) >= 100);
	}

	@Test
	public void velkeKruhySeOmezi() {
		final double fhd = 1920.0 * 1080;
		Assert.assertEquals("r = 652 px: 1,335 M px, 11 kruhů", 11, PolomerKruhu.limitKruhu(652, fhd));
		Assert.assertEquals("r = 1305 px: ořezané na výřez 2,07 M px, 7 kruhů", 7, PolomerKruhu.limitKruhu(1305, fhd));
		Assert.assertEquals("kruh větší než výřez stojí nejvýš plochu výřezu", 7, PolomerKruhu.limitKruhu(100_000, fhd));
		Assert.assertEquals(1, PolomerKruhu.limitKruhu(1000, 1e12, 100));
		Assert.assertTrue("12 kruhů o r = 652 px se omezí", PolomerKruhu.limitKruhu(652, fhd) < 12);
	}

	@Test
	public void hraniceRozpoctuJeMeziDvemaPocty() {
		final double plocha = 1000.0;
		final double r = Math.sqrt(plocha / Math.PI);
		Assert.assertEquals(10, PolomerKruhu.limitKruhu(r, 1e9, 10_000));
		Assert.assertEquals("o pixel větší plocha už 10 nevejde", 9, PolomerKruhu.limitKruhu(r * 1.001, 1e9, 10_000));
		Assert.assertEquals(11, PolomerKruhu.limitKruhu(r * 0.95, 1e9, 10_000));
	}

	@Test
	public void rozpoctovaFunkceSeNeridiPocetnymKonstantou() {
		Assert.assertNotEquals("plocha rozhoduje, ne počet", PolomerKruhu.limitKruhu(26, 2e6), PolomerKruhu.limitKruhu(652, 2e6));
	}

	@Test
	public void nejblizsiVybiraNejblizsiBodyOdNejblizsiho() {
		final List<int[]> body = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			body.add(new int[] { 100 + 7 * ((i * 3) % 10), 0 });
		}
		final List<int[]> tri = PolomerKruhu.nejblizsi(body, 100, 0, 3);
		Assert.assertEquals(3, tri.size());
		Assert.assertEquals(100, tri.get(0)[0]);
		Assert.assertEquals(107, tri.get(1)[0]);
		Assert.assertEquals(114, tri.get(2)[0]);
		Assert.assertSame("do limitu se nic nemění", body, PolomerKruhu.nejblizsi(body, 100, 0, 10));
		Assert.assertEquals(10, body.size());
	}

	private JZvyraznovaciKruhySlide slideSCastecnymi(final int pocet, final boolean sDalekym) {
		final JZvyraznovaciKruhySlide ref = slide(17, jedna());
		final int metru = polomerMetruProPixely(ref, R_PX);
		final List<Mou> kese = new ArrayList<>();
		for (int k = 0; k < pocet; k++) {
			kese.add(MISTO); // blízko středu: kruhy r = 100 px jsou v okně 800×600 jen částečné
		}
		if (sDalekym) {
			// jeden kruh nejdál od středu: vpravo, jeho pravý okraj je v pravé části okna
			final Mou vpravo = new Mou(MISTO.xx + (int) Math.round(300 * mouNaPixel(ref)), MISTO.yy);
			kese.add(vpravo);
		}
		final JZvyraznovaciKruhySlide s = slide(17, kese);
		s.onEvent(metry(metru));
		return s;
	}

	@Test
	public void dalsiCastecnyKruhPoVycerpaniRozpoctuSeVynechaAKresliSeNejblizsi() {
		final JZvyraznovaciKruhySlide s = slideSCastecnymi(limitProOkno(), true);
		final BufferedImage i = nakresli(s);
		Assert.assertEquals(1, s.getVynechanychKruhu());
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.KRESLI, s.getDuvod());
		Assert.assertTrue("střed se kreslí", obarveno(i, W / 2, H / 2));
		Assert.assertFalse("nejvzdálenější kruh se vynechal", obarveno(i, W / 2 + 385, H / 2));
	}

	@Test
	public void kruhyVRozpoctuSeKresliVsechny() {
		final JZvyraznovaciKruhySlide s = slideSCastecnymi(limitProOkno() - 1, true);
		final BufferedImage i = nakresli(s);
		Assert.assertEquals(0, s.getVynechanychKruhu());
		Assert.assertTrue("i nejvzdálenější se kreslí, když se vejde do limitu", obarveno(i, W / 2 + 385, H / 2));
	}

	@Test
	public void pojistkaNezasahujeDoSlozenehoPokrytiVyrezu() {
		// 500 kruhů pokrývajících celé okno + 10 částečných: pokrytí se kreslí vždy, částečné všechny
		final List<Mou> kese = new ArrayList<>();
		final JZvyraznovaciKruhySlide ref = slide(20, jedna());
		for (int k = 0; k < 500; k++) {
			kese.add(MISTO);
		}
		final JZvyraznovaciKruhySlide s = slide(20, kese);
		s.onEvent(metry(161));
		final BufferedImage i = nakresli(s);
		Assert.assertEquals(0, s.getVynechanychKruhu());
		Assert.assertTrue(obarveno(i, 3, 3));
		Assert.assertNotNull(ref);
	}

	@Test
	public void vynechanychKruhuSeNulujePriDalsimKresleni() {
		final JZvyraznovaciKruhySlide s = slideSCastecnymi(limitProOkno() + 5, false);
		nakresli(s);
		Assert.assertEquals(5, s.getVynechanychKruhu());
		s.onEvent(pixely(10));
		nakresli(s);
		Assert.assertEquals(0, s.getVynechanychKruhu());
	}

	@Test
	public void duvodSkrytiSeZaznamenava() {
		final JZvyraznovaciKruhySlide s = slide(12, jedna());
		s.onEvent(metry(1));
		nakresli(s);
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.POLOMER_PRILIS_MALY, s.getDuvod());
		s.onEvent(metry(5000));
		nakresli(s);
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.KRESLI, s.getDuvod());
		final List<Mou> husto = new ArrayList<>();
		for (int k = 0; k < 200; k++) {
			husto.add(MISTO);
		}
		final JZvyraznovaciKruhySlide h = slide(18, husto);
		h.onEvent(pixely(100));
		nakresli(h);
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.PRILIS_HUSTE, h.getDuvod());
	}

	@Test
	public void nejblizsiPocitaVzdalenostOdNenulovehoStredu() {
		final List<int[]> body = new ArrayList<>();
		for (int k = 0; k < 10; k++) {
			body.add(new int[] { 500 + 3 * k - 13, 300 + (k % 3) });
		}
		body.add(new int[] { 0, 0 });
		body.add(new int[] { 10, 10 });
		body.add(new int[] { 500, 0 });
		body.add(new int[] { 505, 5 });
		body.add(new int[] { 1000, 600 });
		final List<int[]> vybrane = PolomerKruhu.nejblizsi(body, 500, 300, 10);
		Assert.assertEquals(10, vybrane.size());
		for (final int[] b : vybrane) {
			Assert.assertEquals("vybrané jsou kolem středu (500, 300): " + b[0] + "," + b[1], 300, b[1], 2);
			Assert.assertTrue(Math.abs(b[0] - 500) <= 14);
		}
	}

	@Test
	public void vynechanyJeNejvzdalenejsiOdStreduClipuMimoPocatek() {
		final JZvyraznovaciKruhySlide ref = slide(17, jedna());
		final int metru = polomerMetruProPixely(ref, R_PX);
		final double px = mouNaPixel(ref);
		final List<Mou> kese = new ArrayList<>();
		// osa y souřadnic Mou míří nahoru; střed oříznutého výřezu (100, 50, 500, 400) je (350, 250); okno má střed MISTO v (400, 300)
		for (int k = 0; k < limitProOkno(); k++) {
			kese.add(new Mou(MISTO.xx + (int) Math.round((-50 + k % 3) * px), MISTO.yy + (int) Math.round((50 - k / 3) * px)));
		}
		// kruh těsně u levého horního rohu výřezu je od jeho středu nejdál
		kese.add(new Mou(MISTO.xx + (int) Math.round(-290 * px), MISTO.yy + (int) Math.round(240 * px)));
		final JZvyraznovaciKruhySlide s = slide(17, kese);
		s.onEvent(metry(metru));
		final BufferedImage i = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = i.createGraphics();
		g.setClip(100, 50, 500, 400);
		s.paintComponent(g);
		g.dispose();
		Assert.assertEquals(1, s.getVynechanychKruhu());
		Assert.assertFalse("vynechán kruh u rohu výřezu", obarveno(i, 110, 60));
		Assert.assertTrue("střed výřezu se kreslí", obarveno(i, 350, 250));
	}

	private JZvyraznovaciKruhySlide slideSeMrizkou(final int pocet, final int rPx) {
		final JZvyraznovaciKruhySlide ref = slide(17, jedna());
		final double px = mouNaPixel(ref);
		final List<Mou> kese = new ArrayList<>();
		for (int k = 0; k < pocet; k++) {
			kese.add(new Mou(MISTO.xx + (int) Math.round((-390 + (k % 66) * 12) * px), MISTO.yy + (int) Math.round((250 - (k / 66) * 12) * px)));
		}
		final JZvyraznovaciKruhySlide s = slide(17, kese);
		s.onEvent(metry(polomerMetruProPixely(ref, rPx)));
		return s;
	}

	@Test
	public void stoMalychKruhuSeNakresliVsech() {
		final JZvyraznovaciKruhySlide s = slideSeMrizkou(100, 26);
		final BufferedImage i = nakresli(s);
		Assert.assertEquals(0, s.getVynechanychKruhu());
		Assert.assertEquals(JZvyraznovaciKruhySlide.Rozhodnuti.KRESLI, s.getDuvod());
		Assert.assertTrue("první", obarveno(i, 10, 50));
		Assert.assertTrue("poslední v řadě", obarveno(i, 10 + 33 * 12, 50 + 0));
		Assert.assertTrue("druhá řada", obarveno(i, 10, 62));
	}

	@Test
	public void tisiceMalychKruhuSeOmeziAzTvrdymStropem() {
		final JZvyraznovaciKruhySlide s = slideSeMrizkou(2500, 26);
		nakresli(s);
		Assert.assertEquals(2500 - PolomerKruhu.MAX_CASTECNYCH_KRUHU, s.getVynechanychKruhu());
		final JZvyraznovaciKruhySlide mene = slideSeMrizkou(PolomerKruhu.MAX_CASTECNYCH_KRUHU, 26);
		nakresli(mene);
		Assert.assertEquals(0, mene.getVynechanychKruhu());
	}

	@Test
	public void velkeKruhySeOmeziPodlePlochy() {
		final JZvyraznovaciKruhySlide s = slideSeMrizkou(300, 300);
		nakresli(s);
		final int limit = PolomerKruhu.limitKruhu(300, (double) W * H);
		Assert.assertEquals(300 - limit, s.getVynechanychKruhu());
		Assert.assertTrue("velké kruhy se omezují výrazně pod tvrdý strop", limit < 300);
	}

	@Test
	public void vyberHaldouDavaStejneVzdalenostiJakoUplneRazeni() {
		final java.util.Random rnd = new java.util.Random(7);
		final List<int[]> body = new ArrayList<>();
		for (int k = 0; k < 600; k++) {
			body.add(new int[] { rnd.nextInt(2000) - 500, rnd.nextInt(1500) - 400 });
		}
		final double cx = 640.5;
		final double cy = 310.25;
		final List<int[]> vybrane = PolomerKruhu.nejblizsi(body, cx, cy, 37);
		final List<Double> ocekavane = new ArrayList<>();
		for (final int[] b : body) {
			ocekavane.add((b[0] - cx) * (b[0] - cx) + (b[1] - cy) * (b[1] - cy));
		}
		java.util.Collections.sort(ocekavane);
		Assert.assertEquals(37, vybrane.size());
		for (int k = 0; k < 37; k++) {
			final int[] b = vybrane.get(k);
			Assert.assertEquals("pořadí od nejbližšího, " + k, ocekavane.get(k), (b[0] - cx) * (b[0] - cx) + (b[1] - cy) * (b[1] - cy), 1e-9);
		}
		Assert.assertEquals("vstup se nemění", 600, body.size());
	}
}
