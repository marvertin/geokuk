package cz.geokuk.util.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import org.junit.*;

/** Legenda, nadpisy a bubliny u posuvníků kruhů Obsazenost; bez displeje. */
public class JBarvovyDvojSliderTest {

	private static final class FalesnyCasovac implements BublinkaHodnoty.Casovac {
		boolean bezi;

		@Override
		public void spust() {
			bezi = true;
		}

		@Override
		public void zastav() {
			bezi = false;
		}

		@Override
		public boolean bezi() {
			return bezi;
		}
	}

	private final AtomicLong cas = new AtomicLong(5000);
	private final List<FalesnyCasovac> casovace = new ArrayList<>();
	private final List<BublinkaHodnoty> bublinky = new ArrayList<>();
	private final List<ViditelnostLegendy> legendy = new ArrayList<>();
	private final JBarvovyDvojSlider slider = new JBarvovyDvojSlider(prekresli -> {
		final FalesnyCasovac c = new FalesnyCasovac();
		casovace.add(c);
		final BublinkaHodnoty b = new BublinkaHodnoty(cas::get, c, prekresli);
		bublinky.add(b);
		return b;
	}, prekresli -> {
		final FalesnyCasovac c = new FalesnyCasovac();
		casovace.add(c);
		final ViditelnostLegendy l = new ViditelnostLegendy(cas::get, c, prekresli);
		legendy.add(l);
		return l;
	});

	private ViditelnostLegendy legenda() {
		return legendy.get(0);
	}

	private boolean vsechnyCasovaceStoji() {
		for (final FalesnyCasovac c : casovace) {
			if (c.bezi) {
				return false;
			}
		}
		return true;
	}

	private JPanel rodic() {
		final JPanel rodic = new JPanel(new BorderLayout());
		rodic.setOpaque(false);
		rodic.add(slider, BorderLayout.EAST);
		rodic.setSize(500, 500);
		rozloz(rodic);
		return rodic;
	}

	private static void rozloz(final Component c) {
		c.doLayout();
		if (c instanceof Container) {
			for (final Component k : ((Container) c).getComponents()) {
				rozloz(k);
			}
		}
	}

	private Rectangle kresli(final JPanel rodic, final BufferedImage obraz) {
		final Graphics2D g = obraz.createGraphics();
		try {
			return slider.kresliNadMapou(g, rodic);
		} finally {
			g.dispose();
		}
	}

	private static BufferedImage obraz() {
		return new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
	}

	// --- obsah a hodnoty ---

	@Test
	public void osaBarvyMaPopiskyNaKrajichAUprostred() {
		final List<JBarvovyDvojSlider.Popisek> osa = JBarvovyDvojSlider.osaBarvy();
		Assert.assertEquals(3, osa.size());
		Assert.assertEquals("Černá", osa.get(0).text);
		Assert.assertEquals(0, osa.get(0).hodnota);
		Assert.assertEquals("Šedá", osa.get(1).text);
		Assert.assertEquals(128, osa.get(1).hodnota);
		Assert.assertEquals("Bílá", osa.get(2).text);
		Assert.assertEquals(255, osa.get(2).hodnota);
	}

	@Test
	public void osaPruhlednostiMaZnackyPo25Procentech() {
		final List<JBarvovyDvojSlider.Popisek> osa = JBarvovyDvojSlider.osaPruhlednosti();
		Assert.assertEquals(5, osa.size());
		final String[] texty = { "0 %", "25 %", "50 %", "75 %", "100 %" };
		final int[] hodnoty = { 255, 191, 128, 64, 0 };
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals(texty[i], osa.get(i).text);
			Assert.assertEquals(hodnoty[i], osa.get(i).hodnota);
		}
	}

	@Test
	public void rozsahyAHodnotyZustalyBezeZmeny() {
		Assert.assertEquals(0, slider.getBarvovnik().getMinimum());
		Assert.assertEquals(255, slider.getBarvovnik().getMaximum());
		Assert.assertEquals(0, slider.getPruhlednik().getMinimum());
		Assert.assertEquals(255, slider.getPruhlednik().getMaximum());
		Assert.assertEquals(new Color(128, 128, 128, 128), slider.getColor());
		slider.setColor(new Color(10, 10, 10, 77));
		Assert.assertEquals(new Color(10, 10, 10, 77), slider.getColor());
	}

	@Test
	public void bezInterakcePosuvnikyNemajiZadnyText() {
		Assert.assertFalse("popisky se z posuvníků nekreslí, jsou v legendě", slider.getBarvovnik().getPaintLabels());
		Assert.assertFalse(slider.getPruhlednik().getPaintLabels());
		for (final Component c : slider.getComponents()) {
			Assert.assertFalse("v panelu nejsou štítky", c instanceof javax.swing.JLabel);
		}
		final Dimension d = slider.getPreferredSize();
		Assert.assertTrue("tenký pruh: " + d, d.width < 40);
		Assert.assertFalse(slider.isOpaque());
	}

	@Test
	public void textyBublinekJsouJenCislaSProcenty() {
		Assert.assertEquals("0 %", JBarvovyDvojSlider.textBarvy(255));
		Assert.assertEquals("50 %", JBarvovyDvojSlider.textBarvy(128));
		Assert.assertEquals("100 %", JBarvovyDvojSlider.textBarvy(0));
		Assert.assertEquals("0 %", JBarvovyDvojSlider.textPruhlednosti(255));
		Assert.assertEquals("50 %", JBarvovyDvojSlider.textPruhlednosti(128));
		Assert.assertEquals("73 %", JBarvovyDvojSlider.textPruhlednosti(70));
		Assert.assertEquals("100 %", JBarvovyDvojSlider.textPruhlednosti(0));
		for (int v = 0; v <= 255; v++) {
			Assert.assertTrue(v + ": " + JBarvovyDvojSlider.textBarvy(v), JBarvovyDvojSlider.textBarvy(v).matches("\\d{1,3} %"));
			Assert.assertTrue(JBarvovyDvojSlider.textPruhlednosti(v).matches("\\d{1,3} %"));
		}
	}

	/** Číslo roste od prvního popisku osy (nahoře) k poslednímu (dole), u obou posuvníků stejně. */
	@Test
	public void ciselnaHodnotaRosteSmeremOsy() {
		final List<JBarvovyDvojSlider.Popisek> barva = JBarvovyDvojSlider.osaBarvy();
		// osa je vykreslena shora: Bílá (hodnota 255) nahoře, Černá (0) dole
		final JBarvovyDvojSlider.Popisek nahore = barva.get(barva.size() - 1);
		final JBarvovyDvojSlider.Popisek dole = barva.get(0);
		Assert.assertEquals("Bílá", nahore.text);
		Assert.assertEquals("Černá", dole.text);
		Assert.assertEquals("0 %", JBarvovyDvojSlider.textBarvy(nahore.hodnota));
		Assert.assertEquals("100 %", JBarvovyDvojSlider.textBarvy(dole.hodnota));
		final List<JBarvovyDvojSlider.Popisek> pruhlednost = JBarvovyDvojSlider.osaPruhlednosti();
		Assert.assertEquals("0 %", JBarvovyDvojSlider.textPruhlednosti(pruhlednost.get(0).hodnota));
		Assert.assertEquals("100 %", JBarvovyDvojSlider.textPruhlednosti(pruhlednost.get(pruhlednost.size() - 1).hodnota));
		// směrem dolů po ose čísla neklesají
		int predchozi = -1;
		for (int v = 255; v >= 0; v--) {
			final int p = Integer.parseInt(JBarvovyDvojSlider.textBarvy(v).replace(" %", ""));
			Assert.assertTrue(p >= predchozi);
			predchozi = p;
		}
	}

	@Test
	public void barvaSeZaokrouhlujeNeOrezava() {
		Assert.assertEquals("šedá 41 = 83,92 % tmavosti", "84 %", JBarvovyDvojSlider.textBarvy(41));
		Assert.assertEquals("šedá 40 = 84,31 %", "84 %", JBarvovyDvojSlider.textBarvy(40));
		Assert.assertEquals("2 %", JBarvovyDvojSlider.textBarvy(250));
		Assert.assertEquals("98 %", JBarvovyDvojSlider.textBarvy(4));
	}

	@Test
	public void popiskyOsOdpovidajiTextumBublin() {
		for (int procent = 0; procent <= 100; procent += 25) {
			Assert.assertEquals(procent, JBarvovyDvojSlider.pruhlednostProcent(JBarvovyDvojSlider.krytiProPruhlednost(procent)));
		}
	}

	@Test
	public void polohaJezdce() {
		Assert.assertEquals("nejvyšší hodnota nahoře", 10, JBarvovyDvojSlider.polohaJezdce(200, 10, 255, 0, 255));
		Assert.assertEquals("nejnižší dole", 190, JBarvovyDvojSlider.polohaJezdce(200, 10, 0, 0, 255));
		Assert.assertEquals(100, JBarvovyDvojSlider.polohaJezdce(200, 10, 128, 0, 255), 1);
		Assert.assertEquals(10, JBarvovyDvojSlider.polohaJezdce(200, 10, 5, 5, 5));
	}

	// --- legenda se kreslí jen při interakci ---

	@Test
	public void bezInterakceSeNicNekresli() {
		final JPanel rodic = rodic();
		Assert.assertNull(kresli(rodic, obraz()));
		slider.setColor(new Color(40, 40, 40, 90));
		Assert.assertNull("nastavení z modelu nic neukáže", kresli(rodic, obraz()));
	}

	@Test
	public void legendaSeUkazePoNajetiMysiAPoZpozdeni() {
		final JPanel rodic = rodic();
		final BufferedImage obraz = obraz();
		legenda().mys(true);
		cas.addAndGet(ViditelnostLegendy.ZPOZDENI_NAJETI_MS - 1);
		legenda().tik();
		Assert.assertNull("před uplynutím zpoždění nic", kresli(rodic, obraz));
		cas.addAndGet(1 + BublinkaHodnoty.ZACATEK_MS);
		legenda().tik();
		final Rectangle r = kresli(rodic, obraz);
		Assert.assertNotNull(r);
		final Point osa = SwingUtilities.convertPoint(slider.getBarvovnik(), slider.getBarvovnik().getWidth() / 2, 0, rodic);
		Assert.assertTrue("legenda je vlevo od osy a nad posuvníky, nadpis na pravém okraji pruhu", r.x < osa.x && r.y < osa.y);
		Assert.assertTrue("nadpisy jsou v rámci vrstvy", r.x >= 0 && r.y >= 0 && r.x + r.width <= rodic.getWidth() + 1);
	}

	@Test
	public void legendaObsahujeNadpisyAPopiskyOsVHorniAVSpodniCasti() {
		final JPanel rodic = rodic();
		legenda().tahne(true);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		final BufferedImage obraz = obraz();
		final Rectangle r = kresli(rodic, obraz);
		Assert.assertNotNull(r);
		final Point nahore = SwingUtilities.convertPoint(slider.getBarvovnik(), 0, 0, rodic);
		final Point dole = SwingUtilities.convertPoint(slider.getPruhlednik(), 0, slider.getPruhlednik().getHeight(), rodic);
		Assert.assertTrue("nadpis Barva nad horním posuvníkem", r.y < nahore.y);
		Assert.assertTrue("popisky až k dolnímu konci druhého posuvníku", r.y + r.height >= dole.y - 30);
		int neprazdnych = 0;
		for (int y = r.y; y < r.y + r.height; y += 2) {
			if ((obraz.getRGB(r.x + 4, y) >>> 24) != 0) {
				neprazdnych++;
			}
		}
		Assert.assertTrue("legenda něco nakreslila", neprazdnych > 0);
	}

	@Test
	public void legendaSeVejdeDoVyhrazenePraveSirky() {
		final JPanel rodic = rodic();
		legenda().tahne(true);
		cas.addAndGet(2 * BublinkaHodnoty.ZACATEK_MS);
		final Rectangle r = kresli(rodic, obraz());
		Assert.assertNotNull(r);
		Assert.assertTrue("legenda zasahuje " + (500 - r.x) + " bodů od pravého okraje", 500 - r.x <= cz.geokuk.framework.RozmisteniPrekryvu.SIRKA_LEGENDY);
	}

	@Test
	public void viditelnostLegendyRoztahujeAlfuKresby() {
		final JPanel rodic = rodic();
		legenda().tahne(true);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS / 2);
		final BufferedImage poloviny = obraz();
		final Rectangle r1 = kresli(rodic, poloviny);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		final BufferedImage plny = obraz();
		final Rectangle r2 = kresli(rodic, plny);
		Assert.assertEquals(r1, r2);
		int sumaPoloviny = 0;
		int sumaPlny = 0;
		for (int y = r2.y; y < r2.y + r2.height; y++) {
			for (int x = r2.x; x < r2.x + r2.width; x++) {
				sumaPoloviny += poloviny.getRGB(x, y) >>> 24;
				sumaPlny += plny.getRGB(x, y) >>> 24;
			}
		}
		Assert.assertTrue("poloviční viditelnost je řidší: " + sumaPoloviny + " < " + sumaPlny, sumaPoloviny < sumaPlny);
	}

	// --- zapojení vstupů na legendu a bublinky ---

	@Test
	public void najetiMysiNaPosuvnikASchozeniMimoPruh() {
		rodic();
		final JSlider s = slider.getBarvovnik();
		s.dispatchEvent(new MouseEvent(s, MouseEvent.MOUSE_ENTERED, cas.get(), 0, 2, 20, 0, false));
		Assert.assertTrue("myš nad pruhem: legenda čeká na zpoždění", legenda().casovacBezi());
		cas.addAndGet(ViditelnostLegendy.ZPOZDENI_NAJETI_MS + BublinkaHodnoty.ZACATEK_MS);
		legenda().tik();
		Assert.assertEquals(1f, legenda().viditelnost(), 0f);
		// myš přejde na druhý posuvník: pořád nad pruhem
		final JSlider p = slider.getPruhlednik();
		s.dispatchEvent(new MouseEvent(s, MouseEvent.MOUSE_EXITED, cas.get(), 0, 2, s.getHeight() + 1, 0, false));
		Assert.assertEquals("přechod na sousední posuvník legendu neschová", 1f, legenda().viditelnost(), 0f);
		cas.addAndGet(5 * (BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS));
		legenda().tik();
		Assert.assertTrue("myš je pořád nad pruhem, legenda se nezačne ztrácet", legenda().jeAktivni());
		Assert.assertEquals(1f, legenda().viditelnost(), 0f);
		// myš odejde z pruhu doleva
		p.dispatchEvent(new MouseEvent(p, MouseEvent.MOUSE_EXITED, cas.get(), 0, -30, 5, 0, false));
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS);
		legenda().tik();
		Assert.assertFalse(legenda().jeAktivni());
		Assert.assertTrue(vsechnyCasovaceStoji());
	}

	@Test
	public void tazeniUkazeLegendu() {
		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(200);
		Assert.assertTrue(legenda().jeAktivni());
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS + 5000);
		Assert.assertEquals("při tažení se drží", 1f, legenda().viditelnost(), 0f);
		slider.getBarvovnik().setValueIsAdjusting(false);
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS);
		legenda().tik();
		bublinky.get(0).tik();
		Assert.assertFalse("po puštění legenda zmizí", legenda().jeAktivni());
		Assert.assertTrue(vsechnyCasovaceStoji());
	}

	@Test
	public void kolečkoNeboKlavesaLegenduKratkoUkaze() {
		slider.getPruhlednik().setValue(33);
		Assert.assertTrue(legenda().jeAktivni());
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		Assert.assertEquals(1f, legenda().viditelnost(), 0f);
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS);
		legenda().tik();
		Assert.assertFalse(legenda().jeAktivni());
	}

	@Test
	public void focusNaPosuvnikuUkazeLegendu() {
		final JSlider s = slider.getBarvovnik();
		for (final java.awt.event.FocusListener l : s.getFocusListeners()) {
			l.focusGained(new java.awt.event.FocusEvent(s, java.awt.event.FocusEvent.FOCUS_GAINED));
		}
		Assert.assertTrue(legenda().jeAktivni());
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS + 3000);
		Assert.assertEquals("při fokusu se drží", 1f, legenda().viditelnost(), 0f);
		for (final java.awt.event.FocusListener l : s.getFocusListeners()) {
			l.focusLost(new java.awt.event.FocusEvent(s, java.awt.event.FocusEvent.FOCUS_LOST));
		}
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS);
		legenda().tik();
		Assert.assertFalse(legenda().jeAktivni());
	}

	@Test
	public void stejnaHodnotaBezTazeniLegenduNeukaze() {
		slider.getBarvovnik().setValue(slider.getBarvovnik().getValue());
		Assert.assertFalse(legenda().jeAktivni());
	}

	@Test
	public void legendaSeRusiPriOdebraniAPriSkryti() {
		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(100);
		Assert.assertFalse(vsechnyCasovaceStoji());
		slider.removeNotify();
		Assert.assertTrue(vsechnyCasovaceStoji());
		Assert.assertFalse(legenda().jeAktivni());

		slider.getPruhlednik().setValueIsAdjusting(true);
		slider.getPruhlednik().setValue(100);
		Assert.assertTrue(legenda().jeAktivni());
		slider.dispatchEvent(new java.awt.event.HierarchyEvent(slider, java.awt.event.HierarchyEvent.HIERARCHY_CHANGED, slider, null, java.awt.event.HierarchyEvent.SHOWING_CHANGED));
		Assert.assertFalse(legenda().jeAktivni());
		Assert.assertTrue(vsechnyCasovaceStoji());
	}

	@Test
	public void pozdniPusteniPoSkryteLegenduZnovuNeukaze() {
		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(100);
		slider.zrusBublinky();
		slider.getBarvovnik().setValueIsAdjusting(false);
		Assert.assertFalse(legenda().jeAktivni());
		Assert.assertFalse(bublinky.get(0).jeAktivni());
		Assert.assertTrue(vsechnyCasovaceStoji());
	}

	// --- bublinky ---

	@Test
	public void bublinkaStojiVeStejnemSloupciJakoPopiskyOs() {
		final JPanel rodic = rodic();
		slider.getPruhlednik().setValueIsAdjusting(true);
		slider.getPruhlednik().setValue(10);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		final Rectangle r = pouzeBublinka(rodic);
		Assert.assertNotNull(r);
		final Point osa = SwingUtilities.convertPoint(slider.getPruhlednik(), slider.getPruhlednik().getWidth() / 2, 0, rodic);
		Assert.assertEquals("pravý okraj bublinky je v pravém okraji popisků os, 8 px od osy", osa.x - 8, r.x + r.width);
		Assert.assertEquals(8, JBarvovyDvojSlider.MEZERA_OSY);
		slider.getPruhlednik().setValueIsAdjusting(false);
	}

	@Test
	public void bublinkaLegendeNerozsiriAniNeposune() {
		final JPanel rodic = rodic();
		legenda().tahne(true);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		final Rectangle bez = kresli(rodic, obraz());
		slider.getPruhlednik().setValueIsAdjusting(true);
		slider.getPruhlednik().setValue(100);
		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(40);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		final Rectangle s = kresli(rodic, obraz());
		Assert.assertEquals("bublina je uvnitř sloupce popisků, legenda je stejně široká", bez.x, s.x);
		Assert.assertEquals(bez.width, s.width);
		slider.getPruhlednik().setValueIsAdjusting(false);
		slider.getBarvovnik().setValueIsAdjusting(false);
	}

	@Test
	public void popisekPodBublinkouSeSkryjeAJinePopiskyZustanou() {
		final JPanel rodic = rodic();
		legenda().tahne(true);
		slider.getBarvovnik().setValueIsAdjusting(true);
		// bílá je nahoře: bublinka leží přesně na popisku Bílá
		slider.getBarvovnik().setValue(255);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS + 50);
		final BufferedImage sLegendou = obraz();
		final Rectangle r = kresli(rodic, sLegendou);
		final Rectangle bublina = pouzeBublinka(rodic);
		final BufferedImage jenBublina = obraz();
		final Graphics2D g = jenBublina.createGraphics();
		legendy.get(0).zrus();
		slider.kresliNadMapou(g, rodic);
		g.dispose();
		Assert.assertNotNull(r);
		Assert.assertNotNull(bublina);
		for (int y = bublina.y; y < bublina.y + bublina.height; y++) {
			for (int x = bublina.x - 30; x < bublina.x + bublina.width; x++) {
				Assert.assertEquals("pixel " + x + "," + y + " je jen bublinka, popisek pod ní je skrytý", jenBublina.getRGB(x, y), sLegendou.getRGB(x, y));
			}
		}
		slider.getBarvovnik().setValueIsAdjusting(false);
	}

	private Rectangle pouzeBublinka(final JPanel rodic) {
		final ViditelnostLegendy l = legenda();
		final boolean aktivni = l.jeAktivni();
		l.zrus();
		final Rectangle r = kresli(rodic, obraz());
		if (aktivni) {
			l.tahne(true);
		}
		return r;
	}

	@Test
	public void popisekMimoBublinkuZustaneViditelny() {
		final JPanel rodic = rodic();
		legenda().tahne(true);
		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(255);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS + 50);
		final BufferedImage sLegendou = obraz();
		kresli(rodic, sLegendou);
		final Rectangle bublina = pouzeBublinka(rodic);
		// popisek Černá dole na ose barvy je daleko od bublinky nahoře
		final Point nahore = SwingUtilities.convertPoint(slider.getBarvovnik(), 0, 0, rodic);
		final int y = nahore.y + slider.getBarvovnik().getHeight() - 8;
		int viditelnych = 0;
		for (int dy = -6; dy <= 6; dy++) {
			for (int x = bublina.x - 30; x < bublina.x + bublina.width; x++) {
				if ((sLegendou.getRGB(x, y + dy) >>> 24) != 0) {
					viditelnych++;
				}
			}
		}
		Assert.assertTrue("popisky mimo bublinku se nekryjí a zůstávají: " + viditelnych, viditelnych > 0);
		slider.getBarvovnik().setValueIsAdjusting(false);
	}

	@Test
	public void prekryvaniBublinkyAPopiskuPodleVzdalenostiStredu() {
		final int hp = 15;
		final int hb = 20;
		final int mez = 2;
		final int hranice = (hp + hb + 2 * mez) / 2; // 19,5: dy = 19 ještě překrývá, dy = 20 už ne
		Assert.assertTrue(JBarvovyDvojSlider.prekryvaBublinku(100, hp, 100, hb));
		Assert.assertTrue(JBarvovyDvojSlider.prekryvaBublinku(100, hp, 100 + hranice - 1, hb));
		Assert.assertTrue(JBarvovyDvojSlider.prekryvaBublinku(100, hp, 100 + hranice, hb));
		Assert.assertFalse(JBarvovyDvojSlider.prekryvaBublinku(100, hp, 100 + hranice + 1, hb));
		Assert.assertTrue("záleží jen na vzdálenosti, ne na směru", JBarvovyDvojSlider.prekryvaBublinku(100, hp, 100 - hranice, hb));
		Assert.assertFalse(JBarvovyDvojSlider.prekryvaBublinku(100, hp, 100 - hranice - 1, hb));
		Assert.assertFalse("daleko", JBarvovyDvojSlider.prekryvaBublinku(10, hp, 300, hb));
		// bublinka mezi dvěma popisky překryje oba, když jsou blízko; u kraje stupnice jen jeden
		final int y1 = 100;
		final int y2 = 120;
		Assert.assertTrue(JBarvovyDvojSlider.prekryvaBublinku(y1, hp, 110, hb));
		Assert.assertTrue(JBarvovyDvojSlider.prekryvaBublinku(y2, hp, 110, hb));
		Assert.assertTrue(JBarvovyDvojSlider.prekryvaBublinku(10, hp, 5, hb));
		Assert.assertFalse(JBarvovyDvojSlider.prekryvaBublinku(60, hp, 5, hb));
	}

	@Test
	public void hranicePrekryvaniPriSudemSouctuVysek() {
		// výšky 16 a 20 a mezera 2: součet 40, střed od středu o 20 px se ještě nedotýká
		Assert.assertTrue(JBarvovyDvojSlider.prekryvaBublinku(100, 16, 119, 20));
		Assert.assertFalse(JBarvovyDvojSlider.prekryvaBublinku(100, 16, 120, 20));
		Assert.assertFalse(JBarvovyDvojSlider.prekryvaBublinku(100, 16, 80, 20));
		Assert.assertTrue(JBarvovyDvojSlider.prekryvaBublinku(100, 16, 81, 20));
	}

	@Test
	public void viditelnostPopiskuMiziSObjevovanimBublinky() {
		Assert.assertEquals(0.8f, JBarvovyDvojSlider.viditelnostPopisku(0.8f, 0f, true), 0f);
		Assert.assertEquals(0f, JBarvovyDvojSlider.viditelnostPopisku(0.8f, 1f, true), 0f);
		Assert.assertEquals(0.4f, JBarvovyDvojSlider.viditelnostPopisku(0.8f, 0.5f, true), 0.0001f);
		Assert.assertEquals("mimo bublinku se nic nemění", 0.8f, JBarvovyDvojSlider.viditelnostPopisku(0.8f, 1f, false), 0f);
		Assert.assertEquals("bez legendy nic", 0f, JBarvovyDvojSlider.viditelnostPopisku(0f, 0f, true), 0f);
	}

	@Test
	public void tazeniAPusteniBublinkyDobehneAZastaviCasovac() {
		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(200);
		Assert.assertTrue(casovace.get(0).bezi);
		Assert.assertTrue(bublinky.get(0).jeAktivni());
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS + 3000);
		Assert.assertEquals(1f, bublinky.get(0).viditelnost(), 0f);
		slider.getBarvovnik().setValueIsAdjusting(false);
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS - 1);
		Assert.assertEquals(1f, bublinky.get(0).viditelnost(), 0f);
		cas.addAndGet(1 + BublinkaHodnoty.ZANIKNUTI_MS);
		bublinky.get(0).tik();
		legenda().tik();
		Assert.assertFalse(bublinky.get(0).jeAktivni());
		Assert.assertTrue(vsechnyCasovaceStoji());
	}

	@Test
	public void zmenaHodnotyBezTazeniUkazeBublinku() {
		slider.getBarvovnik().setValue(10);
		Assert.assertTrue(bublinky.get(0).jeAktivni());
		Assert.assertEquals("96 %", bublinky.get(0).getText());
	}

	@Test
	public void bublinkySeKresliNadLegendouBezOhleduNaViditelnostLegendy() {
		final JPanel rodic = rodic();
		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(30);
		slider.getPruhlednik().setValueIsAdjusting(true);
		slider.getPruhlednik().setValue(220);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		Assert.assertNotNull(kresli(rodic, obraz()));
		slider.getBarvovnik().setValueIsAdjusting(false);
		slider.getPruhlednik().setValueIsAdjusting(false);
	}

	@Test
	public void pusteniJednohoZDvouTazenychPosuvnikuLegenduNezhasne() {
		rodic();
		final JSlider b = slider.getBarvovnik();
		final JSlider p = slider.getPruhlednik();
		b.setValueIsAdjusting(true);
		b.setValue(b.getValue() > 100 ? b.getValue() - 20 : b.getValue() + 20);
		p.setValueIsAdjusting(true);
		p.setValue(p.getValue() > 100 ? p.getValue() - 20 : p.getValue() + 20);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		legenda().tik();
		Assert.assertEquals(1f, legenda().viditelnost(), 0f);
		b.setValueIsAdjusting(false);
		cas.addAndGet(2 * (BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS));
		legenda().tik();
		Assert.assertEquals("druhý posuvník se pořád táhne", 1f, legenda().viditelnost(), 0f);
		p.setValueIsAdjusting(false);
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS - 1);
		legenda().tik();
		Assert.assertEquals("po puštění posledního se legenda drží", 1f, legenda().viditelnost(), 0f);
		cas.addAndGet(BublinkaHodnoty.ZANIKNUTI_MS + 1);
		legenda().tik();
		Assert.assertEquals(0f, legenda().viditelnost(), 0f);
	}
}
