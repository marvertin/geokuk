package cz.geokuk.core.coord;

import java.awt.Dimension;
import java.awt.Point;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Wgs;

public class CoordTest {

	@Test
	public void test1() {
		Coord c = new Coord(12, new Wgs(50, 15).toMou(), new Dimension(800, 600), 0);
		Point p1 = new Point(200, 225);
		Point p2 = c.transform(c.transform(p1));
		Assert.assertEquals(p1, p2);
	}
}
