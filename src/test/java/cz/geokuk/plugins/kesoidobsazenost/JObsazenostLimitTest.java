package cz.geokuk.plugins.kesoidobsazenost;

import java.awt.Dimension;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Wgs;

/** Obsazenost se nekreslí jen tehdy, když je keší ve výřezu víc než strop kruhů. */
public class JObsazenostLimitTest {

	private static JObsazenost slide() {
		final JObsazenost s = new JObsazenost();
		s.setSoord(new Coord(12, new Wgs(50, 15).toMou(), new Dimension(800, 600), 0));
		return s;
	}

	@Test
	public void maloKesiSeKresli() {
		Assert.assertFalse(slide().prekrocenLimit(100));
	}

	@Test
	public void viceNezStropKruhuSeNekresli() {
		Assert.assertFalse(slide().prekrocenLimit(JObsazenost.MAX_KRUHU));
		Assert.assertTrue(slide().prekrocenLimit(JObsazenost.MAX_KRUHU + 1));
	}

	/** Hustě obsazené území je právě to, co má obsazenost ukázat. */
	@Test
	public void hustaSkupinaSeKresli() {
		Assert.assertFalse(slide().prekrocenLimit(10_000));
	}
}
