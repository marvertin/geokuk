package cz.geokuk.plugins.kesoidkruhy;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.plugins.kesoid.mvc.LimityKresleniEvent;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Vrstva kruhů hlásí, proč se zapnuté kruhy nekreslí, z výřezu a nastavení, ne z kreslení. */
public class KruhyStavTest {

	private static final Mou MISTO = new Wgs(50.0755, 14.4378).toMou();
	private static final int W = 800;
	private static final int H = 600;

	private final List<Event0<?>> udalosti = new ArrayList<>();
	private final StavVrstevModel stav = new StavVrstevModel();

	private static KesBag bag(final int kesi) {
		final Wpt bod = mock(Wpt.class);
		when(bod.getMou()).thenReturn(MISTO);
		final List<Wpt> wpts = new ArrayList<>();
		for (int k = 0; k < kesi; k++) {
			wpts.add(bod);
		}
		final Indexator<Wpt> indexator = Indexator.postav(BoundingRect.ALL, wpts, w -> w.getMou().xx, w -> w.getMou().yy);
		final KesBag bag = mock(KesBag.class);
		when(bag.getIndexator()).thenReturn(indexator);
		return bag;
	}

	private JZvyraznovaciKruhySlide slide(final int zoom, final int kesi) {
		stav.inject((EventFirer) udalosti::add);
		final List<Wpt> wpts = new ArrayList<>();
		final Wpt bod = mock(Wpt.class);
		when(bod.getMou()).thenReturn(MISTO);
		for (int k = 0; k < kesi; k++) {
			wpts.add(bod);
		}
		final Indexator<Wpt> indexator = Indexator.postav(BoundingRect.ALL, wpts, w -> w.getMou().xx, w -> w.getMou().yy);
		final KesBag bag = mock(KesBag.class);
		when(bag.getIndexator()).thenReturn(indexator);
		final JZvyraznovaciKruhySlide s = new JZvyraznovaciKruhySlide();
		s.inject(stav);
		s.setSoord(new Coord(zoom, MISTO, new Dimension(W, H), 0));
		s.setSize(W, H);
		s.onEvent(new KeskyVyfiltrovanyEvent(bag, bag));
		return s;
	}

	private static KruhyPreferencesChangeEvent metry(final int metru, final boolean onoff) {
		final KruhySettings s = new KruhySettings();
		s.setOnoff(onoff);
		s.setJednotkovaVelikost(true);
		s.setPolomerMetru(metru);
		s.setBarva(new Color(200, 0, 0, 255));
		return new KruhyPreferencesChangeEvent(s);
	}

	private static KruhyPreferencesChangeEvent pixely(final int px, final boolean onoff) {
		final KruhySettings s = new KruhySettings();
		s.setOnoff(onoff);
		s.setVelikost(px);
		s.setBarva(new Color(200, 0, 0, 255));
		return new KruhyPreferencesChangeEvent(s);
	}

	@Test
	public void vypnuteKruhyNicNehlasi() {
		final JZvyraznovaciKruhySlide s = slide(17, 5);
		s.onEvent(pixely(10, false));
		Assert.assertEquals(DuvodNekresleni.KRESLI, s.aktualniDuvod());
		Assert.assertFalse(stav.getStav().maSkryte());
	}

	@Test
	public void kresliSeNicNehlasi() {
		final JZvyraznovaciKruhySlide s = slide(17, 5);
		s.onEvent(pixely(10, true));
		Assert.assertFalse(stav.getStav().maSkryte());
		Assert.assertTrue("bez změny stavu žádná událost", udalosti.isEmpty());
	}

	@Test
	public void priliHusteVPixelechSeHlasiZVyrezuBezKresleni() {
		final JZvyraznovaciKruhySlide s = slide(18, 200);
		s.onEvent(pixely(100, true));
		Assert.assertEquals(DuvodNekresleni.PRILIS_HUSTE, stav.getStav().get(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY).getDuvod());
		Assert.assertNull(stav.getStav().get(VrstvaNadMapou.JEDNOTKOVE_KRUHY));
		Assert.assertEquals(DuvodNekresleni.KRESLI, s.getDuvod());
	}

	@Test
	public void limitPoctuNesePosledniLimit() {
		final JZvyraznovaciKruhySlide s = slide(17, LimityKresleni.MIN_IKON + 1);
		s.onEvent(pixely(1, true));
		s.onEvent(new LimityKresleniEvent(LimityKresleni.of(LimityKresleni.MIN_IKON + 1, 100_000)));
		Assert.assertFalse("počet přesně rovný limitu se kreslí", stav.getStav().maSkryte());
		s.onEvent(new LimityKresleniEvent(LimityKresleni.of(LimityKresleni.MIN_IKON, 100_000)));
		final StavVrstev.Skryta skryta = stav.getStav().get(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY);
		Assert.assertEquals(DuvodNekresleni.LIMIT_POCTU, skryta.getDuvod());
		Assert.assertEquals(LimityKresleni.MIN_IKON, skryta.getLimit());
	}

	@Test
	public void jednotkoveKruhyPodTriPixelyJsouJednotkovyDuvod() {
		final JZvyraznovaciKruhySlide s = slide(12, 1);
		s.onEvent(metry(1, true));
		Assert.assertEquals(DuvodNekresleni.POLOMER_PRILIS_MALY, stav.getStav().get(VrstvaNadMapou.JEDNOTKOVE_KRUHY).getDuvod());
		Assert.assertNull("zvýrazňovací se nehlásí, když jedou jednotkové", stav.getStav().get(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY));
		s.onEvent(metry(5000, true));
		Assert.assertFalse(stav.getStav().maSkryte());
	}

	@Test
	public void prepnutiRezimuPresunePricinuZJednohoRezimuDoDruheho() {
		final JZvyraznovaciKruhySlide s = slide(18, 200);
		s.onEvent(pixely(100, true));
		Assert.assertNotNull(stav.getStav().get(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY));
		s.onEvent(metry(1, true));
		Assert.assertNull(stav.getStav().get(VrstvaNadMapou.ZVYRAZNOVACI_KRUHY));
		Assert.assertNotNull(stav.getStav().get(VrstvaNadMapou.JEDNOTKOVE_KRUHY));
	}

	@Test
	public void vypnutiKeSoiduVrstvuVyradiAnizmenilaSeNastaveni() {
		final JZvyraznovaciKruhySlide s = slide(18, 200);
		s.onEvent(pixely(100, true));
		Assert.assertTrue(stav.getStav().maSkryte());
		s.onEvent(new KesoidOnoffEvent(false));
		Assert.assertFalse("F5 vypnul i kruhy, štítek nemá co vysvětlovat", stav.getStav().maSkryte());
		s.onEvent(new KesoidOnoffEvent(true));
		Assert.assertTrue(stav.getStav().maSkryte());
	}

	@Test
	public void zmenaVyrezuPrepocitaStavBezKresleni() {
		final JZvyraznovaciKruhySlide s = slide(12, 1);
		s.onEvent(metry(50, true));
		Assert.assertEquals(DuvodNekresleni.POLOMER_PRILIS_MALY, stav.getStav().get(VrstvaNadMapou.JEDNOTKOVE_KRUHY).getDuvod());
		s.setSoord(new Coord(17, MISTO, new Dimension(W, H), 0));
		s.onVyrezChanged();
		Assert.assertFalse(stav.getStav().maSkryte());
		s.setSoord(new Coord(12, MISTO, new Dimension(W, H), 0));
		s.onVyrezChanged();
		Assert.assertTrue(stav.getStav().maSkryte());
	}

	@Test
	public void kopieProRendrovaniNehlasi() {
		final JZvyraznovaciKruhySlide s = slide(18, 200);
		final JZvyraznovaciKruhySlide kopie = (JZvyraznovaciKruhySlide) s.createRenderableSlide();
		kopie.setSoord(new Coord(18, MISTO, new Dimension(W, H), 0));
		kopie.onEvent(pixely(100, true));
		kopie.onVyrezChanged();
		Assert.assertTrue(udalosti.isEmpty());
	}

	private static void dokonciUdalosti() throws Exception {
		javax.swing.SwingUtilities.invokeAndWait(() -> {
		});
	}

	private void nakresliCely(final JZvyraznovaciKruhySlide s) {
		final java.awt.image.BufferedImage i = new java.awt.image.BufferedImage(W, H, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g = i.createGraphics();
		s.paintComponent(g);
		g.dispose();
	}

	/** Poloměr kruhů v metrech a z něj skutečný limit počtu kreslených kruhů v okně. */
	private int metruProPolomerPx(final int px) {
		return (int) Math.round(px / slide(17, 1).getSoord().getPixluNaMetr());
	}

	private int limitPro(final int metru) {
		return PolomerKruhu.limitKruhu(metru * slide(17, 1).getSoord().getPixluNaMetr(), (double) W * H);
	}

	@Test
	public void vynechaneCastecneKruhySeHlasiPoKresleniCelehoVyrezu() throws Exception {
		final int metru = metruProPolomerPx(250);
		final int limit = limitPro(metru);
		final JZvyraznovaciKruhySlide s = slide(17, limit + 3);
		s.onEvent(metry(metru, true));
		nakresliCely(s);
		dokonciUdalosti();
		Assert.assertEquals(3, stav.getStav().getVynechanychKruhu());
		Assert.assertEquals("kresleno je skutečný limit, ne konstanta", limit, stav.getStav().getKreslenychKruhu());
		Assert.assertTrue(stav.getStav().tooltip(), stav.getStav().tooltip().contains("kresleno jen " + limit + " nejbližších"));
		Assert.assertTrue(stav.getStav().jeKVideni());
		Assert.assertFalse(stav.getStav().maSkryte());
		final int udalostiPred = udalosti.size();
		nakresliCely(s);
		dokonciUdalosti();
		Assert.assertEquals("stejný počet se neoznamuje znovu", udalostiPred, udalosti.size());
		s.onEvent(pixely(10, true));
		Assert.assertEquals("přepnutí do pixelů vynechání ruší", 0, stav.getStav().getVynechanychKruhu());
	}

	@Test
	public void castecnePrekresleniVynechaneNeoznamuje() throws Exception {
		final int metru = metruProPolomerPx(250);
		final JZvyraznovaciKruhySlide s = slide(17, limitPro(metru) + 3);
		s.onEvent(metry(metru, true));
		final java.awt.image.BufferedImage i = new java.awt.image.BufferedImage(W, H, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g = i.createGraphics();
		g.setClip(150, 100, 500, 400);
		s.paintComponent(g);
		g.dispose();
		dokonciUdalosti();
		Assert.assertEquals(0, stav.getStav().getVynechanychKruhu());
	}

	@Test
	public void zmenaKreslenychPriStejnemPoctuVynechanychSeHlasiZnovu() throws Exception {
		final int metru1 = metruProPolomerPx(250);
		final int metru2 = metruProPolomerPx(200);
		final int limit1 = limitPro(metru1);
		final int limit2 = limitPro(metru2);
		Assert.assertNotEquals(limit1, limit2);
		final JZvyraznovaciKruhySlide s = slide(17, limit1 + 3);
		s.onEvent(metry(metru1, true));
		nakresliCely(s);
		dokonciUdalosti();
		Assert.assertEquals(limit1, stav.getStav().getKreslenychKruhu());
		final KesBag vic = bag(limit2 + 3);
		s.onEvent(new KeskyVyfiltrovanyEvent(vic, vic));
		s.onEvent(metry(metru2, true));
		nakresliCely(s);
		dokonciUdalosti();
		Assert.assertEquals("pořád 3 vynechané", 3, stav.getStav().getVynechanychKruhu());
		Assert.assertEquals("bublina se aktualizovala na nový počet kreslených", limit2, stav.getStav().getKreslenychKruhu());
	}
}
