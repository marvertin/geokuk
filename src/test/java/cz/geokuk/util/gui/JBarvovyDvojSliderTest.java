package cz.geokuk.util.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.junit.*;

/** Popisky, nadpisy a bublinky u posuvníků kruhů Obsazenost; bez displeje. */
public class JBarvovyDvojSliderTest {

	private final JBarvovyDvojSlider slider = new JBarvovyDvojSlider();

	private static Map<Integer, String> popisky(final JSlider s) {
		final Map<Integer, String> m = new HashMap<>();
		final Enumeration<?> klice = s.getLabelTable().keys();
		while (klice.hasMoreElements()) {
			final Integer k = (Integer) klice.nextElement();
			m.put(k, ((JLabel) s.getLabelTable().get(k)).getText());
		}
		return m;
	}

	@Test
	public void osaBarvyMaPopiskyNaKrajichAUprostred() {
		Assert.assertTrue(slider.getBarvovnik().getPaintLabels());
		final Map<Integer, String> m = popisky(slider.getBarvovnik());
		Assert.assertEquals(3, m.size());
		Assert.assertEquals("Černá", m.get(0));
		Assert.assertEquals("Šedá", m.get(128));
		Assert.assertEquals("Bílá", m.get(255));
	}

	@Test
	public void osaPruhlednostiMaZnackyPo25Procentech() {
		final Map<Integer, String> m = popisky(slider.getPruhlednik());
		Assert.assertEquals(5, m.size());
		Assert.assertEquals("0 %", m.get(255));
		Assert.assertEquals("25 %", m.get(191));
		Assert.assertEquals("50 %", m.get(128));
		Assert.assertEquals("75 %", m.get(64));
		Assert.assertEquals("100 %", m.get(0));
	}

	@Test
	public void popiskyOsMajiZnackuANadpisyNe() {
		for (final JSlider s : new JSlider[] { slider.getBarvovnik(), slider.getPruhlednik() }) {
			final Enumeration<?> popisky = s.getLabelTable().elements();
			while (popisky.hasMoreElements()) {
				Assert.assertTrue(((JPopisekNaMape) popisky.nextElement()).maZnacku());
			}
		}
		final java.util.List<String> nadpisy = new java.util.ArrayList<>();
		for (final Component c : slider.getComponents()) {
			if (c instanceof JPopisekNaMape) {
				nadpisy.add(((JPopisekNaMape) c).getText());
				Assert.assertFalse(((JPopisekNaMape) c).maZnacku());
			}
		}
		Assert.assertEquals(java.util.Arrays.asList("Barva", "Průhlednost"), nadpisy);
	}

	@Test
	public void nadpisyJsouNadPosuvniky() {
		final Component[] c = slider.getComponents();
		int barva = -1, pruhlednost = -1, sBarvy = -1, sPruhlednosti = -1;
		for (int i = 0; i < c.length; i++) {
			if (c[i] instanceof JPopisekNaMape && "Barva".equals(((JLabel) c[i]).getText())) {
				barva = i;
			}
			if (c[i] instanceof JPopisekNaMape && "Průhlednost".equals(((JLabel) c[i]).getText())) {
				pruhlednost = i;
			}
			if (c[i] == slider.getBarvovnik()) {
				sBarvy = i;
			}
			if (c[i] == slider.getPruhlednik()) {
				sPruhlednosti = i;
			}
		}
		Assert.assertEquals(barva + 2, sBarvy);
		Assert.assertEquals(pruhlednost + 2, sPruhlednosti);
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
	public void textyBublinek() {
		Assert.assertEquals("Jas 0 %", JBarvovyDvojSlider.textBarvy(0));
		Assert.assertEquals("Jas 50 %", JBarvovyDvojSlider.textBarvy(128));
		Assert.assertEquals("Jas 100 %", JBarvovyDvojSlider.textBarvy(255));
		Assert.assertEquals("Průhlednost 0 %", JBarvovyDvojSlider.textPruhlednosti(255));
		Assert.assertEquals("Průhlednost 50 %", JBarvovyDvojSlider.textPruhlednosti(128));
		Assert.assertEquals("Průhlednost 73 %", JBarvovyDvojSlider.textPruhlednosti(70));
		Assert.assertEquals("Průhlednost 100 %", JBarvovyDvojSlider.textPruhlednosti(0));
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
			return slider.kresliBublinky(g, rodic);
		} finally {
			g.dispose();
		}
	}

	@Test
	public void bublinkaSeUkazePriTazeniAPriNastaveniZModeluNe() throws Exception {
		final JPanel rodic = rodic();
		final BufferedImage obraz = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
		slider.setColor(new Color(40, 40, 40, 90));
		Assert.assertNull("nastavení z modelu bublinu neukáže", kresli(rodic, obraz));
		Thread.sleep(BublinkaHodnoty.ZACATEK_MS + 80);
		Assert.assertNull(kresli(rodic, obraz));

		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(200);
		Thread.sleep(BublinkaHodnoty.ZACATEK_MS + 80);
		Assert.assertNotNull("při tažení se ukáže", kresli(rodic, obraz));
		slider.getBarvovnik().setValueIsAdjusting(false);
	}

	@Test
	public void zmenaBezTazeniSeTakyUkaze() throws Exception {
		final JPanel rodic = rodic();
		final BufferedImage obraz = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
		slider.getPruhlednik().setValue(33);
		Thread.sleep(BublinkaHodnoty.ZACATEK_MS + 80);
		Assert.assertNotNull(kresli(rodic, obraz));
	}

	@Test
	public void bublinkaJeVedleJezdceNaMapeVlevoOdPosuvniku() throws Exception {
		final JPanel rodic = rodic();
		final BufferedImage obraz = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
		slider.getPruhlednik().setValueIsAdjusting(true);
		slider.getPruhlednik().setValue(10);
		Thread.sleep(BublinkaHodnoty.ZACATEK_MS + 80);
		final Rectangle r = kresli(rodic, obraz);
		Assert.assertNotNull(r);
		final Point p = javax.swing.SwingUtilities.convertPoint(slider.getPruhlednik(), 0, 0, rodic);
		Assert.assertTrue("vlevo od posuvníku", r.x + r.width <= p.x);
		Assert.assertTrue(r.y >= p.y - 20 && r.y + r.height <= p.y + slider.getPruhlednik().getHeight() + 20);
		slider.getPruhlednik().setValueIsAdjusting(false);
	}

	@Test
	public void jednaBublinkaNaKazdyPosuvnik() throws Exception {
		final JPanel rodic = rodic();
		final BufferedImage obraz = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
		slider.getBarvovnik().setValueIsAdjusting(true);
		slider.getBarvovnik().setValue(30);
		slider.getPruhlednik().setValueIsAdjusting(true);
		slider.getPruhlednik().setValue(220);
		Thread.sleep(BublinkaHodnoty.ZACATEK_MS + 80);
		final Rectangle souhrn = kresli(rodic, obraz);
		Assert.assertNotNull(souhrn);
		final Point pb = javax.swing.SwingUtilities.convertPoint(slider.getBarvovnik(), 0, 0, rodic);
		final Point pp = javax.swing.SwingUtilities.convertPoint(slider.getPruhlednik(), 0, 0, rodic);
		Assert.assertTrue(souhrn.height > 30 + (pp.y - pb.y) / 4);
		slider.getBarvovnik().setValueIsAdjusting(false);
		slider.getPruhlednik().setValueIsAdjusting(false);
	}

	@Test
	public void nadpisyAPopiskyMajiCitelnePozadi() {
		final JPopisekNaMape l = new JPopisekNaMape("Šedá", true);
		l.setSize(l.getPreferredSize());
		final BufferedImage i = new BufferedImage(l.getWidth(), l.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = i.createGraphics();
		l.paint(g);
		g.dispose();
		Assert.assertTrue("podklad je poloprůhledný, ne prázdný", (i.getRGB(l.getWidth() / 2, 1) >>> 24) > 100);
		Assert.assertEquals(Color.WHITE, l.getForeground());
	}

	@Test
	public void rozlozeniSeVejdeDoSvislehoPruhu() {
		final Dimension d = slider.getPreferredSize();
		Assert.assertTrue(d.width < 160 && d.width > 40);
		Assert.assertTrue(d.height > 150);
		final JComponent c = slider;
		Assert.assertFalse(c.isOpaque());
	}

	// --- zapojení bublinek na posuvníky s vlastními hodinami a časovačem ---

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

	private final java.util.concurrent.atomic.AtomicLong cas = new java.util.concurrent.atomic.AtomicLong(5000);
	private final java.util.List<FalesnyCasovac> casovace = new java.util.ArrayList<>();
	private final java.util.List<BublinkaHodnoty> bublinky = new java.util.ArrayList<>();

	private JBarvovyDvojSlider sOvladanymCasem() {
		return new JBarvovyDvojSlider(prekresli -> {
			final FalesnyCasovac c = new FalesnyCasovac();
			casovace.add(c);
			final BublinkaHodnoty b = new BublinkaHodnoty(cas::get, c, prekresli);
			bublinky.add(b);
			return b;
		});
	}

	private boolean vsechnyCasovaceStoji() {
		for (final FalesnyCasovac c : casovace) {
			if (c.bezi) {
				return false;
			}
		}
		return true;
	}

	@Test
	public void tazeniAPusteniDobehneAZastaviCasovac() {
		final JBarvovyDvojSlider s = sOvladanymCasem();
		s.getBarvovnik().setValueIsAdjusting(true);
		s.getBarvovnik().setValue(200);
		Assert.assertTrue("při tažení časovač běží", casovace.get(0).bezi);
		Assert.assertTrue(bublinky.get(0).jeAktivni());
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS + 3000);
		Assert.assertEquals("při tažení se drží", 1f, bublinky.get(0).viditelnost(), 0f);
		s.getBarvovnik().setValueIsAdjusting(false);
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS - 1);
		Assert.assertEquals(1f, bublinky.get(0).viditelnost(), 0f);
		cas.addAndGet(1 + BublinkaHodnoty.ZANIKNUTI_MS);
		bublinky.get(0).tik();
		Assert.assertEquals("po puštění bublinka zmizí", 0f, bublinky.get(0).viditelnost(), 0f);
		Assert.assertFalse(bublinky.get(0).jeAktivni());
		Assert.assertTrue("a časovač se zastaví", vsechnyCasovaceStoji());
	}

	@Test
	public void pruhlednikSeChovaStejne() {
		final JBarvovyDvojSlider s = sOvladanymCasem();
		s.getPruhlednik().setValueIsAdjusting(true);
		s.getPruhlednik().setValue(20);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		s.getPruhlednik().setValueIsAdjusting(false);
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS);
		bublinky.get(1).tik();
		Assert.assertFalse(bublinky.get(1).jeAktivni());
		Assert.assertTrue(vsechnyCasovaceStoji());
	}

	@Test
	public void odebraniZeHierarchieBublinkuZrusi() {
		final JBarvovyDvojSlider s = sOvladanymCasem();
		s.getBarvovnik().setValueIsAdjusting(true);
		s.getBarvovnik().setValue(100);
		s.getPruhlednik().setValueIsAdjusting(true);
		s.getPruhlednik().setValue(100);
		Assert.assertFalse(vsechnyCasovaceStoji());
		s.removeNotify();
		Assert.assertTrue(vsechnyCasovaceStoji());
		Assert.assertFalse(bublinky.get(0).jeAktivni());
		Assert.assertFalse(bublinky.get(1).jeAktivni());
	}

	@Test
	public void skryteVrstvyBublinkuZrusi() {
		final JBarvovyDvojSlider s = sOvladanymCasem();
		s.getBarvovnik().setValueIsAdjusting(true);
		s.getBarvovnik().setValue(100);
		Assert.assertFalse(vsechnyCasovaceStoji());
		// vrstva se skryla (F5, F9) a komponenta už není vidět
		s.dispatchEvent(new java.awt.event.HierarchyEvent(s, java.awt.event.HierarchyEvent.HIERARCHY_CHANGED, s, null, java.awt.event.HierarchyEvent.SHOWING_CHANGED));
		Assert.assertTrue(vsechnyCasovaceStoji());
		Assert.assertFalse(bublinky.get(0).jeAktivni());
	}

	@Test
	public void pozdniPusteniPoSkryteBublinkuZnovuNeukaze() {
		final JBarvovyDvojSlider s = sOvladanymCasem();
		s.getBarvovnik().setValueIsAdjusting(true);
		s.getBarvovnik().setValue(100);
		s.zrusBublinky();
		s.getBarvovnik().setValueIsAdjusting(false);
		Assert.assertFalse(bublinky.get(0).jeAktivni());
		Assert.assertTrue(vsechnyCasovaceStoji());
	}

	@Test
	public void zmenaHodnotyBezTazeniSeUkazeAleStejnaHodnotaNe() {
		final JBarvovyDvojSlider s = sOvladanymCasem();
		s.getBarvovnik().setValue(s.getBarvovnik().getValue());
		Assert.assertFalse("beze změny hodnoty nic", bublinky.get(0).jeAktivni());
		s.getBarvovnik().setValue(10);
		Assert.assertTrue(bublinky.get(0).jeAktivni());
		Assert.assertEquals("Jas 4 %", bublinky.get(0).getText());
	}

	@Test
	public void jasSeZaokrouhlujeNeOrezava() {
		Assert.assertEquals("šedá 40 = 15,69 %", "Jas 16 %", JBarvovyDvojSlider.textBarvy(40));
		Assert.assertEquals("Jas 2 %", JBarvovyDvojSlider.textBarvy(4));
		Assert.assertEquals("Jas 98 %", JBarvovyDvojSlider.textBarvy(250));
	}

	@Test
	public void nadpisMaOdPosuvnikuMezeru() {
		final Component[] c = slider.getComponents();
		for (int i = 0; i + 2 < c.length; i++) {
			if (c[i] instanceof JPopisekNaMape && !((JPopisekNaMape) c[i]).maZnacku()) {
				Assert.assertTrue("mezi nadpisem a posuvníkem je odsazení", c[i + 1] instanceof javax.swing.Box.Filler);
				Assert.assertTrue(c[i + 2] instanceof JSlider);
			}
		}
	}
}
