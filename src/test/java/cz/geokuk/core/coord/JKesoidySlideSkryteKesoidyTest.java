package cz.geokuk.core.coord;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Dimension;
import java.util.Collections;

import org.junit.Test;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.JKesoidySlide;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Skryté kešoidy se nechytají myší: kříž nesmí přeskočit na kešoid, který není vidět. */
public class JKesoidySlideSkryteKesoidyTest {

	private static final Mou MISTO = new Wgs(50.0755, 14.4378).toMou();

	/** Vrstva s jedním kešoidem přesně na místě MISTO. */
	private static JKesoidySlide slideSKesoidem() {
		final Kesoid kesoid = mock(Kesoid.class);
		final Wpt wpt = mock(Wpt.class);
		when(wpt.getMou()).thenReturn(MISTO);
		when(wpt.getWgs()).thenReturn(new Wgs(50.0755, 14.4378));
		when(wpt.getKesoid()).thenReturn(kesoid);
		when(wpt.isMainWpt()).thenReturn(true);
		when(kesoid.getKesoidKind()).thenReturn(cz.geokuk.plugins.kesoid.data.EKesoidKind.values()[0]);
		final Indexator<Wpt> indexator = Indexator.postav(BoundingRect.ALL, Collections.singletonList(wpt), w -> MISTO.xx, w -> MISTO.yy);
		final KesBag bag = mock(KesBag.class);
		when(bag.getIndexator()).thenReturn(indexator);
		final JKesoidySlide slide = new JKesoidySlide(false);
		slide.setSoord(new Coord(12, MISTO, new Dimension(800, 600), 0));
		slide.onEvent(new KeskyVyfiltrovanyEvent(bag, bag));
		return slide;
	}

	@Test
	public void viditeleKesoidySePoziciNaNeChytaji() {
		final JKesoidySlide slide = slideSKesoidem();
		final PoziceSeMaMenitEvent udalost = new PoziceSeMaMenitEvent(MISTO);
		slide.onEvent(udalost);
		assertNotNull("zapnuté kešoidy se chytají", udalost.getUchopenec());
	}

	@Test
	public void priSkrytychKesoidechSePoziceNaKesoidNechyta() {
		final JKesoidySlide slide = slideSKesoidem();
		slide.onEvent(new KesoidOnoffEvent(false));
		assertFalse(slide.isVisible());
		final PoziceSeMaMenitEvent udalost = new PoziceSeMaMenitEvent(MISTO);
		slide.onEvent(udalost);
		assertNull("vypnuté kešoidy se nechytají", udalost.getUchopenec());
		slide.onEvent(new KesoidOnoffEvent(true));
		final PoziceSeMaMenitEvent znovu = new PoziceSeMaMenitEvent(MISTO);
		slide.onEvent(znovu);
		assertNotNull("po zapnutí se zase chytají", znovu.getUchopenec());
	}

	private static Object pole(final JKesoidySlide slide, final String jmeno) throws Exception {
		final java.lang.reflect.Field f = JKesoidySlide.class.getDeclaredField(jmeno);
		f.setAccessible(true);
		return f.get(slide);
	}

	/** Vypnutí kešoidů zruší zvýraznění a bublinu pod myší, ať po opětovném zapnutí nevisí na starém místě. */
	@Test
	public void vypnutiKesoiduZrusiKesoidAbublinuPodMysi() throws Exception {
		final JKesoidySlide slide = slideSKesoidem();
		final java.awt.Point uprostred = new java.awt.Point(400, 300);
		slide.mouseMoved(new java.awt.event.MouseEvent(slide, java.awt.event.MouseEvent.MOUSE_MOVED, 0, 0, uprostred.x, uprostred.y, 0, false), null);
		assertNotNull("kešoid je pod myší", pole(slide, "kesoidPodMysi"));
		assertTrue("bublina je vidět", ((javax.swing.JLabel) pole(slide, "jakoTooltip")).isVisible());

		slide.onEvent(new KesoidOnoffEvent(false));
		assertNull("po vypnutí kešoidů žádný kešoid pod myší", pole(slide, "kesoidPodMysi"));
		assertNull(pole(slide, "wptPodMysi"));
		assertFalse("bublina je skrytá", ((javax.swing.JLabel) pole(slide, "jakoTooltip")).isVisible());
	}
}
