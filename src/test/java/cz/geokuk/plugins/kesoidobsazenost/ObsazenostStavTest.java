package cz.geokuk.plugins.kesoidobsazenost;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Obsazenost hlásí, proč se zapnutá nekreslí: pod nejnižším zoomem a nad limitem kruhů. */
public class ObsazenostStavTest {

	/** Při tomto zoomu v Praze je poloměr 161 m právě 3 px. */
	private static final int HRANICNI_ZOOM = 11;
	private static final Mou MISTO = new Wgs(50.0755, 14.4378).toMou();

	private final List<Event0<?>> udalosti = new ArrayList<>();
	private final StavVrstevModel stav = new StavVrstevModel();

	private JObsazenost vrstva(final int zoom, final int kesi, final boolean zapnuto) {
		stav.inject((EventFirer) udalosti::add);
		final Wpt bod = mock(Wpt.class);
		when(bod.getMou()).thenReturn(MISTO);
		final List<Wpt> wpts = new ArrayList<>();
		for (int k = 0; k < kesi; k++) {
			wpts.add(bod);
		}
		final Indexator<Wpt> indexator = Indexator.postav(BoundingRect.ALL, wpts, w -> w.getMou().xx, w -> w.getMou().yy);
		final KesBag bag = mock(KesBag.class);
		when(bag.getIndexator()).thenReturn(indexator);
		final JObsazenost o = new JObsazenost();
		o.inject(stav);
		o.setSoord(new Coord(zoom, MISTO, new Dimension(800, 600), 0));
		o.onEvent(new KeskyNactenyEvent(bag));
		final ObsazenostOnoffEvent onoff = new ObsazenostOnoffEvent();
		onoff.onoff = zapnuto;
		o.onEvent(onoff);
		return o;
	}

	@Test
	public void rozhodnutiHranice() {
		Assert.assertEquals(DuvodNekresleni.POLOMER_PRILIS_MALY, JObsazenost.rozhodni(2, 1));
		Assert.assertEquals(DuvodNekresleni.POLOMER_PRILIS_MALY, JObsazenost.rozhodni(0, 100_000));
		Assert.assertEquals(DuvodNekresleni.KRESLI, JObsazenost.rozhodni(3, 1));
		Assert.assertEquals(DuvodNekresleni.KRESLI, JObsazenost.rozhodni(3, JObsazenost.MAX_KRUHU));
		Assert.assertEquals(DuvodNekresleni.LIMIT_POCTU, JObsazenost.rozhodni(3, JObsazenost.MAX_KRUHU + 1));
		Assert.assertEquals("malý poloměr má přednost před limitem", DuvodNekresleni.POLOMER_PRILIS_MALY, JObsazenost.rozhodni(2, JObsazenost.MAX_KRUHU + 1));
	}

	@Test
	public void nejnizsiZoomObsazenostiJePodTriPixelyPodHranicnimZoomem() {
		Assert.assertEquals(DuvodNekresleni.POLOMER_PRILIS_MALY, vrstva(HRANICNI_ZOOM - 1, 1, true).aktualniDuvod());
		Assert.assertEquals(DuvodNekresleni.KRESLI, vrstva(HRANICNI_ZOOM, 1, true).aktualniDuvod());
	}

	@Test
	public void podZoomemSeHlasiAPoPriblizeniVyradi() {
		final JObsazenost o = vrstva(HRANICNI_ZOOM - 1, 1, true);
		Assert.assertEquals(DuvodNekresleni.POLOMER_PRILIS_MALY, stav.getStav().get(VrstvaNadMapou.OBSAZENOST).getDuvod());
		o.setSoord(new Coord(HRANICNI_ZOOM, MISTO, new Dimension(800, 600), 0));
		o.onVyrezChanged();
		Assert.assertFalse(stav.getStav().maSkryte());
	}

	@Test
	public void vypnutaObsazenostNicNehlasi() {
		vrstva(HRANICNI_ZOOM - 1, 1, false);
		Assert.assertFalse(stav.getStav().maSkryte());
		Assert.assertTrue(udalosti.isEmpty());
	}

	@Test
	public void kesoidyVypnuteF5NicNehlasi() {
		final JObsazenost o = vrstva(HRANICNI_ZOOM - 1, 1, true);
		Assert.assertTrue(stav.getStav().maSkryte());
		o.onEvent(new KesoidOnoffEvent(false));
		Assert.assertFalse(stav.getStav().maSkryte());
	}

	@Test
	public void limitKruhuSeHlasiSLimitem() {
		vrstva(15, JObsazenost.MAX_KRUHU + 1, true);
		final StavVrstev.Skryta s = stav.getStav().get(VrstvaNadMapou.OBSAZENOST);
		Assert.assertEquals(DuvodNekresleni.LIMIT_POCTU, s.getDuvod());
		Assert.assertEquals(JObsazenost.MAX_KRUHU, s.getLimit());
	}

	@Test
	public void kopieProRendrovaniNehlasi() {
		final JObsazenost o = vrstva(HRANICNI_ZOOM, 1, true);
		udalosti.clear();
		final JObsazenost kopie = (JObsazenost) o.createRenderableSlide();
		kopie.setSoord(new Coord(HRANICNI_ZOOM - 3, MISTO, new Dimension(800, 600), 0));
		kopie.onVyrezChanged();
		Assert.assertTrue(udalosti.isEmpty());
	}
}
