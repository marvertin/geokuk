package cz.geokuk.framework;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.framework.RozmisteniPrekryvu.Prvek;
import cz.geokuk.util.gui.CornerLayoutManager;
import cz.geokuk.util.gui.ERoh;
import cz.geokuk.util.gui.PopisekNaMape;

/**
 * Překryvné prvky na mapě se nekrývají při žádné velikosti okna, ani s náhledem fotomapy vlevo dole, panely v pravých rozích a legendou Obsazenosti. Velikosti okna odpovídají mapě
 * při 100 %, 125 % a 150 % zvětšení obrazovky na FHD (okno v logických bodech) a úzkému oknu.
 */
public class RozmisteniPrekryvuTest {

	private static final Dimension[] OKNA = { new Dimension(1920, 960), new Dimension(1536, 744), new Dimension(1280, 620), new Dimension(1000, 600), new Dimension(800, 560),
			new Dimension(2560, 1200) };
	private static final String[] ZPRAVY = { "Kešoidy skryté včetně kruhů, obsazenosti a popisků", "Zapnuté zvýrazňovací kruhy s poloměrem v metrech",
			"Kruhy zapnuté, ale skryté: příliš mnoho bodů ve výřezu (limit 90 000), přibližte mapu" };

	private static Dimension zprava(final String text, final float velikostPisma) {
		final BufferedImage obraz = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = obraz.createGraphics();
		try {
			final Font font = new Font(Font.DIALOG, Font.BOLD, 12).deriveFont(velikostPisma);
			final FontMetrics fm = g.getFontMetrics(font);
			final Rectangle r = PopisekNaMape.hranice(fm, text, 0, 0);
			return new Dimension(r.width, r.height);
		} finally {
			g.dispose();
		}
	}

	private static Map<Prvek, Rectangle> rozmisti(final Dimension mapa, final Dimension zprava, final boolean nahledFotomapy, final boolean panelyVpravo, final boolean legenda) {
		final Map<Prvek, Rectangle> o = RozmisteniPrekryvu.prazdne();
		final List<Rectangle> prekazky = new ArrayList<>();
		final Dimension velky = new Dimension(10_000, 10_000);
		final Rectangle sv = RozmisteniPrekryvu.pravyHorniRoh(mapa, new Dimension(300, 200), new Dimension(100, 100), new Dimension(600, 400));
		final Rectangle jv = RozmisteniPrekryvu.pravyDolniRoh(mapa, new Dimension(300, 250), new Dimension(100, 100), new Dimension(600, 400));
		final Rectangle jz = RozmisteniPrekryvu.levyDolniRoh(mapa, new Dimension(200, 200), new Dimension(100, 100), new Dimension(300, 300));
		if (nahledFotomapy) {
			o.put(Prvek.NAHLED_FOTOMAPY, jz);
			prekazky.add(jz);
		}
		if (panelyVpravo) {
			o.put(Prvek.PANEL_CEST, sv);
			o.put(Prvek.DETAIL_KESOIDU, jv);
			prekazky.add(sv);
			prekazky.add(jv);
		}
		if (legenda) {
			o.put(Prvek.LEGENDA_OBSAZENOSTI, RozmisteniPrekryvu.legendaObsazenosti(mapa));
			prekazky.add(RozmisteniPrekryvu.legendaObsazenosti(mapa));
		}
		// pruh plánovače se zkrátí podle volné šířky do pravého horního panelu nebo okraje
		final int volno = (panelyVpravo ? sv.x : mapa.width) - 2 * RozmisteniPrekryvu.OKRAJ - RozmisteniPrekryvu.OKRAJ;
		o.put(Prvek.PRUH_PLANOVACE, RozmisteniPrekryvu.pruhPlanovace(volno));
		o.put(Prvek.UKAZATEL_VYKONU, new Rectangle(RozmisteniPrekryvu.UKAZATEL_VYKONU));
		o.put(Prvek.MERITKO, RozmisteniPrekryvu.meritko(mapa));
		o.put(Prvek.ZPRAVA, RozmisteniPrekryvu.zprava(mapa, zprava, prekazky));
		Assert.assertNotNull(velky);
		return o;
	}

	@Test
	public void zadneDvaPrvkyNaMapeSeNekryjiPriZadneVelikostiOkna() {
		for (final Dimension okno : OKNA) {
			for (final String text : ZPRAVY) {
				for (final float pismo : new float[] { 12f, 15f }) {
					for (final boolean nahled : new boolean[] { false, true }) {
						for (final boolean panely : new boolean[] { false, true }) {
							for (final boolean legenda : new boolean[] { false, true }) {
								final Map<Prvek, Rectangle> o = rozmisti(okno, zprava(text, pismo), nahled, panely, legenda);
								final String popis = okno.width + "×" + okno.height + ", " + text.substring(0, 12) + ", písmo " + pismo + ", náhled " + nahled + ", panely " + panely + ", legenda " + legenda;
								Assert.assertTrue(popis + ": " + RozmisteniPrekryvu.kolize(o), RozmisteniPrekryvu.kolize(o).isEmpty());
								for (final Map.Entry<Prvek, Rectangle> e : o.entrySet()) {
									Assert.assertTrue(popis + ": " + e.getKey() + " je celá na mapě " + e.getValue(), new Rectangle(okno).contains(e.getValue()));
								}
							}
						}
					}
				}
			}
		}
	}

	@Test
	public void kolizeSeNajdouAPrekryvLegendyPodPanelemJeDovoleny() {
		final Map<Prvek, Rectangle> o = RozmisteniPrekryvu.prazdne();
		o.put(Prvek.MERITKO, new Rectangle(100, 100, 50, 50));
		o.put(Prvek.ZPRAVA, new Rectangle(120, 120, 50, 50));
		Assert.assertEquals(Arrays.asList("MERITKO × ZPRAVA"), RozmisteniPrekryvu.kolize(o));
		o.put(Prvek.ZPRAVA, new Rectangle(150, 100, 50, 50));
		Assert.assertTrue("dotyk hranou není překryv", RozmisteniPrekryvu.kolize(o).isEmpty());
		final Map<Prvek, Rectangle> p = RozmisteniPrekryvu.prazdne();
		p.put(Prvek.LEGENDA_OBSAZENOSTI, new Rectangle(0, 0, 100, 100));
		p.put(Prvek.PANEL_CEST, new Rectangle(50, 0, 100, 40));
		p.put(Prvek.DETAIL_KESOIDU, new Rectangle(50, 60, 100, 100));
		Assert.assertTrue("panely v pravých rozích leží nad legendou", RozmisteniPrekryvu.kolize(p).isEmpty());
		p.put(Prvek.ZPRAVA, new Rectangle(60, 30, 10, 40));
		Assert.assertEquals("zpráva kryje legendu i oba panely", 3, RozmisteniPrekryvu.kolize(p).size());
	}

	@Test
	public void pruhPlanovaceSeZkratiPodleVolneSirky() {
		Assert.assertEquals(267, RozmisteniPrekryvu.pruhPlanovace(1000).width);
		Assert.assertEquals(267, RozmisteniPrekryvu.pruhPlanovace(267).width);
		Assert.assertEquals(178, RozmisteniPrekryvu.pruhPlanovace(266).width);
		Assert.assertEquals(178, RozmisteniPrekryvu.pruhPlanovace(178).width);
		Assert.assertEquals(106, RozmisteniPrekryvu.pruhPlanovace(177).width);
		Assert.assertEquals(106, RozmisteniPrekryvu.pruhPlanovace(106).width);
		Assert.assertEquals("užší nevejde nic, zůstane nejužší", 106, RozmisteniPrekryvu.pruhPlanovace(10).width);
		Assert.assertEquals(new Rectangle(4, 4, 267, 28), RozmisteniPrekryvu.pruhPlanovace(500));
		Assert.assertEquals("pruh končí na y = 32, ukazatel výkonu začíná pod ním", 32, RozmisteniPrekryvu.pruhPlanovace(500).y + RozmisteniPrekryvu.pruhPlanovace(500).height);
		Assert.assertEquals(new Rectangle(4, 36, 330, 20), RozmisteniPrekryvu.UKAZATEL_VYKONU);
		Assert.assertTrue(RozmisteniPrekryvu.UKAZATEL_VYKONU.y > 32);
	}

	@Test
	public void meritkoJeUprostredDoleNejvicPolovinaSirky() {
		final Rectangle m = RozmisteniPrekryvu.meritko(new Dimension(1000, 600));
		Assert.assertEquals(250, m.x);
		Assert.assertEquals(500, m.width);
		Assert.assertEquals(600 - RozmisteniPrekryvu.VYSKA_MERITKA, m.y);
		Assert.assertEquals(RozmisteniPrekryvu.VYSKA_MERITKA, m.height);
		Assert.assertEquals(500, RozmisteniPrekryvu.maximalniSirkaMeritka(1000));
		Assert.assertEquals(cz.geokuk.plugins.mrizky.JMeritkoSlide.maximalniSirka(1280), RozmisteniPrekryvu.maximalniSirkaMeritka(1280));
	}

	@Test
	public void zpravaPreskociPrekazkuAZustaneNadNiAPoNeNaMape() {
		final Dimension mapa = new Dimension(700, 500);
		final Dimension z = new Dimension(330, 24);
		final Rectangle bezPrekazky = RozmisteniPrekryvu.zprava(mapa, z, new ArrayList<Rectangle>());
		final Rectangle roh = new Rectangle(bezPrekazky.x + 10, 300, 150, 200);
		final Rectangle nad = RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(roh));
		Assert.assertEquals("zpráva se posune nad překážku s mezerou", 300 - 8 - 24, nad.y);
		Assert.assertEquals("vodorovně zůstane", bezPrekazky.x, nad.x);
		Assert.assertFalse(nad.intersects(roh));
		final Rectangle mimo = new Rectangle(0, 300, 5, 50);
		Assert.assertEquals("překážka mimo zprávu ji nehne", bezPrekazky, RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(mimo)));
		final Rectangle cela = new Rectangle(0, 0, 700, 500);
		final Rectangle vsude = RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(cela));
		Assert.assertEquals("překážka přes celou mapu: zpráva zůstane na výchozím místě", bezPrekazky, vsude);
	}

	@Test
	public void rohySeSkladajiStejneJakoCornerLayoutManager() {
		for (final Dimension okno : OKNA) {
			final JPanel p = new JPanel(new CornerLayoutManager());
			p.setSize(okno);
			final JPanel jz = panel(200, 200, 100, 100, 300, 300);
			final JPanel jv = panel(300, 250, 100, 100, 600, 400);
			final JPanel sv = panel(300, 200, 100, 100, 600, 400);
			final JPanel podklad = new JPanel();
			p.add(jz, ERoh.JZ);
			p.add(jv, ERoh.JV);
			p.add(sv, ERoh.SV);
			p.add(podklad);
			p.doLayout();
			Assert.assertEquals(okno + " JZ", jz.getBounds(), RozmisteniPrekryvu.levyDolniRoh(okno, new Dimension(200, 200), new Dimension(100, 100), new Dimension(300, 300)));
			Assert.assertEquals(okno + " JV", jv.getBounds(), RozmisteniPrekryvu.pravyDolniRoh(okno, new Dimension(300, 250), new Dimension(100, 100), new Dimension(600, 400)));
			Assert.assertEquals(okno + " SV", sv.getBounds(), RozmisteniPrekryvu.pravyHorniRoh(okno, new Dimension(300, 200), new Dimension(100, 100), new Dimension(600, 400)));
		}
	}

	private static JPanel panel(final int pw, final int ph, final int mw, final int mh, final int xw, final int xh) {
		final JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(pw, ph));
		p.setMinimumSize(new Dimension(mw, mh));
		p.setMaximumSize(new Dimension(xw, xh));
		return p;
	}

	@Test
	public void dlouhaZpravaSeVUzkemOkneZkratiAbyNezasahovalaDoLegendy() {
		final Dimension mapa = new Dimension(800, 560);
		final Rectangle r = RozmisteniPrekryvu.zprava(mapa, new Dimension(900, 24), new ArrayList<Rectangle>());
		Assert.assertEquals(800 - 2 * 12 - 90, r.width);
		Assert.assertEquals(RozmisteniPrekryvu.nejvetsiSirkaZpravy(800), r.width);
		Assert.assertTrue(r.x + r.width <= 800 - RozmisteniPrekryvu.SIRKA_LEGENDY);
		Assert.assertEquals("krátká zpráva se nemění", 300, RozmisteniPrekryvu.zprava(mapa, new Dimension(300, 24), new ArrayList<Rectangle>()).width);
		Assert.assertEquals("nejmenší šířka", 100, RozmisteniPrekryvu.nejvetsiSirkaZpravy(100));
	}

	@Test
	public void zpravaSePriVicePrekazkachPosunePodleNejvyssiAProjdeVsechny() {
		final Dimension mapa = new Dimension(1000, 600);
		final Dimension z = new Dimension(400, 24);
		final Rectangle vychozi = RozmisteniPrekryvu.zprava(mapa, z, new ArrayList<Rectangle>());
		final Rectangle nizka = new Rectangle(vychozi.x, 480, 200, 120);
		final Rectangle vysoka = new Rectangle(vychozi.x + 100, 380, 200, 220);
		final Rectangle r = RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(nizka, vysoka));
		Assert.assertEquals("nad nejvyšší překážkou", 380 - 8 - 24, r.y);
		Assert.assertFalse(r.intersects(nizka));
		Assert.assertFalse(r.intersects(vysoka));
		final Rectangle r2 = RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(vysoka, nizka));
		Assert.assertEquals("pořadí překážek nehraje roli", r, r2);
	}

	@Test
	public void zpravaSePosunePresDveAZtrojPrekazkyNadSebouZaSebou() {
		final Dimension mapa = new Dimension(1000, 600);
		final Dimension z = new Dimension(400, 24);
		final Rectangle vychozi = RozmisteniPrekryvu.zprava(mapa, z, new ArrayList<Rectangle>());
		// náhled fotomapy, nad ním legenda; mezi nimi je mezera menší než zpráva s mezerou, takže jeden posun nestačí
		final Rectangle nahled = new Rectangle(vychozi.x, 470, 300, 130);
		final Rectangle nad = new Rectangle(vychozi.x, 420, 300, 30);
		final Rectangle jeste = new Rectangle(vychozi.x, 370, 300, 30);
		final Rectangle r = RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(nahled, nad, jeste));
		Assert.assertEquals(370 - 8 - 24, r.y);
		for (final Rectangle p : Arrays.asList(nahled, nad, jeste)) {
			Assert.assertFalse(p.toString(), r.intersects(p));
		}
		final Rectangle jen2 = RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(nahled, nad));
		Assert.assertEquals(420 - 8 - 24, jen2.y);
	}

	@Test
	public void kdyzNadNejvyssiPrekazkouNeniMistoZustaneVychoziPolohaAneSKokemPodNizsi() {
		final Dimension mapa = new Dimension(1000, 600);
		final Dimension z = new Dimension(400, 24);
		final Rectangle vychozi = RozmisteniPrekryvu.zprava(mapa, z, new ArrayList<Rectangle>());
		final Rectangle vysoka = new Rectangle(vychozi.x, 20, 300, vychozi.y + 10);
		final Rectangle nizsi = new Rectangle(vychozi.x, 60, 300, 600);
		Assert.assertEquals(vychozi, RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(vysoka, nizsi)));
		Assert.assertEquals(vychozi, RozmisteniPrekryvu.zprava(mapa, z, Arrays.asList(nizsi, vysoka)));
	}

	@Test
	public void prekazkyZRohuJsouVSouradnicichPodkladuAJenViditelne() {
		final JPanel rohy = new JPanel(new CornerLayoutManager());
		rohy.setSize(1000, 600);
		final JPanel jz = panel(200, 200, 100, 100, 300, 300);
		final JPanel jv = panel(300, 250, 100, 100, 600, 400);
		final JPanel sv = panel(300, 200, 100, 100, 600, 400);
		final JPanel podklad = new JPanel();
		rohy.add(jz, ERoh.JZ);
		rohy.add(jv, ERoh.JV);
		rohy.add(sv, ERoh.SV);
		rohy.add(podklad);
		rohy.doLayout();
		Assert.assertEquals("podklad vyplňuje kontejner s rohy", new Rectangle(0, 0, 1000, 600), podklad.getBounds());
		Assert.assertEquals(3, RozmisteniPrekryvu.prekazky(rohy, podklad, false).size());
		Assert.assertTrue(RozmisteniPrekryvu.prekazky(rohy, podklad, false).contains(jz.getBounds()));
		Assert.assertTrue(RozmisteniPrekryvu.prekazky(rohy, podklad, false).contains(jv.getBounds()));
		Assert.assertTrue(RozmisteniPrekryvu.prekazky(rohy, podklad, false).contains(sv.getBounds()));
		Assert.assertEquals(4, RozmisteniPrekryvu.prekazky(rohy, podklad, true).size());
		Assert.assertTrue(RozmisteniPrekryvu.prekazky(rohy, podklad, true).contains(RozmisteniPrekryvu.legendaObsazenosti(new Dimension(1000, 600))));
		jz.setVisible(false);
		Assert.assertEquals("skrytý panel není překážka", 2, RozmisteniPrekryvu.prekazky(rohy, podklad, false).size());
		podklad.setBounds(10, 5, 980, 590);
		Assert.assertTrue("přepočet o polohu podkladu", RozmisteniPrekryvu.prekazky(rohy, podklad, false).contains(new Rectangle(jv.getX() - 10, jv.getY() - 5, jv.getWidth(), jv.getHeight())));
	}
}
