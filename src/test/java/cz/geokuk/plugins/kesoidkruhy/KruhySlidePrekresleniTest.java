package cz.geokuk.plugins.kesoidkruhy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Změna nastavení kruhů se projeví hned, na vrstvě i na její kopii pro rendrování, bez čekání na změnu zoomu. */
public class KruhySlidePrekresleniTest {

	private static final Mou MISTO = new Wgs(50.0755, 14.4378).toMou();
	private final AtomicInteger prekresleni = new AtomicInteger();

	private JZvyraznovaciKruhySlide slide() {
		final Wpt wpt = mock(Wpt.class);
		when(wpt.getMou()).thenReturn(MISTO);
		final Indexator<Wpt> indexator = Indexator.postav(BoundingRect.ALL, Collections.singletonList(wpt), w -> MISTO.xx, w -> MISTO.yy);
		final KesBag bag = mock(KesBag.class);
		when(bag.getIndexator()).thenReturn(indexator);
		final JZvyraznovaciKruhySlide slide = new JZvyraznovaciKruhySlide() {
			private static final long serialVersionUID = 1L;

			@Override
			public void repaint(final long tm, final int x, final int y, final int width, final int height) {
				prekresleni.incrementAndGet();
				super.repaint(tm, x, y, width, height);
			}
		};
		slide.setSoord(new Coord(12, MISTO, new Dimension(800, 600), 0));
		slide.setSize(800, 600);
		slide.onEvent(new KeskyVyfiltrovanyEvent(bag, bag));
		return slide;
	}

	private static KruhyPreferencesChangeEvent kruhy(final boolean zapnuto, final int velikost, final boolean jednotkove) {
		final KruhySettings s = new KruhySettings();
		s.setOnoff(zapnuto);
		s.setVelikost(velikost);
		s.setJednotkovaVelikost(jednotkove);
		s.setBarva(new Color(200, 0, 0, 255));
		return new KruhyPreferencesChangeEvent(s);
	}

	private static String diag(final JZvyraznovaciKruhySlide s) {
		try {
			final java.lang.reflect.Field l = JZvyraznovaciKruhySlide.class.getDeclaredField("limitIkon");
			l.setAccessible(true);
			final java.lang.reflect.Field i = JZvyraznovaciKruhySlide.class.getDeclaredField("iIndexator");
			i.setAccessible(true);
			final Indexator<?> ind = (Indexator<?>) i.get(s);
			return "limit=" + l.get(s) + " pocet=" + (ind == null ? "null" : ind.count(s.getSoord().getBoundingRect())) + " dim=" + s.getSoord().getDim() + " vel=" + s.getWidth() + "x" + s.getHeight()
			        + " bound=" + s.getSoord().getBoundingRect();
		} catch (final Exception e) {
			return e.toString();
		}
	}

	private static boolean obarveno(final JZvyraznovaciKruhySlide slide, final int dx) {
		final BufferedImage i = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = i.createGraphics();
		slide.paintComponent(g);
		g.dispose();
		return (i.getRGB(400 + dx, 300) >>> 24) != 0;
	}

	@Test
	public void zmenaNastaveniPrekresliVrstvuAHnedMaNovouVelikost() {
		final JZvyraznovaciKruhySlide s = slide();
		s.onEvent(kruhy(true, 20, false));
		Assert.assertTrue(diag(s), obarveno(s, 15));
		Assert.assertFalse(obarveno(s, 35));
		prekresleni.set(0);
		s.onEvent(kruhy(true, 40, false));
		Assert.assertTrue("vrstva se překreslí", prekresleni.get() > 0);
		Assert.assertTrue(diag(s), obarveno(s, 35));
	}

	@Test
	public void velikostZmenenaPriVypnutychKruzichPlatiPoZapnuti() {
		final JZvyraznovaciKruhySlide s = slide();
		s.onEvent(kruhy(true, 20, false));
		s.onEvent(kruhy(false, 20, false));
		s.onEvent(kruhy(false, 40, false));
		prekresleni.set(0);
		s.onEvent(kruhy(true, 40, false));
		Assert.assertTrue(s.isVisible());
		Assert.assertTrue("po zapnutí se vrstva překreslí", prekresleni.get() > 0);
		Assert.assertTrue(diag(s), obarveno(s, 35));
	}

	@Test
	public void kopiePrenderovaniDostaneNastaveniZUdalosti() {
		final JZvyraznovaciKruhySlide puvodni = slide();
		final JZvyraznovaciKruhySlide kopie = (JZvyraznovaciKruhySlide) puvodni.createRenderableSlide();
		kopie.setSoord(new Coord(12, MISTO, new Dimension(800, 600), 0));
		kopie.setSize(800, 600);
		final Wpt wpt = mock(Wpt.class);
		when(wpt.getMou()).thenReturn(MISTO);
		final KesBag bag = mock(KesBag.class);
		when(bag.getIndexator()).thenReturn(Indexator.postav(BoundingRect.ALL, Collections.singletonList(wpt), w -> MISTO.xx, w -> MISTO.yy));
		kopie.onEvent(new KeskyVyfiltrovanyEvent(bag, bag));
		kopie.onEvent(kruhy(true, 40, false));
		Assert.assertTrue(diag(kopie), obarveno(kopie, 35));
		kopie.onEvent(kruhy(true, 20, false));
		Assert.assertFalse(obarveno(kopie, 35));
	}
}
