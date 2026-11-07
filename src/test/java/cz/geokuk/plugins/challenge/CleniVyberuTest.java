package cz.geokuk.plugins.challenge;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.kes.EKesDiffTerRating;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Členové výběru a cache zvýraznění; bez displeje. */
public class CleniVyberuTest {

	private static final double LAT = 50.0;
	private static final double LON = 14.0;

	private static Wpt wpt(final String id, final double lat, final double lon) {
		final Kes k = new Kes();
		k.setIdentifier(id);
		k.setVztahx(EKesVztah.NORMAL);
		k.setDifficulty(EKesDiffTerRating.values()[2]);
		k.setTerrain(EKesDiffTerRating.values()[2]);
		final Wpt w = new Wpt();
		w.setWgs(new Wgs(lat, lon));
		w.setSym("Geocache");
		k.addWpt(w);
		return w;
	}

	/** Bod v azimutu 100,5° ve vzdálenosti metrů od středu; patří do sektoru 100. */
	private static Wpt vSektoru100(final String id, final double metru) {
		final double a = Math.toRadians(100.5);
		return wpt(id, LAT + metru * Math.cos(a) / 111_000.0, LON + metru * Math.sin(a) / (111_000.0 * Math.cos(Math.toRadians(LAT))));
	}

	private static PrehledChallenge prehled(final Oblast oblast) {
		final BitSet ob = new BitSet(360);
		ob.set(0, 360);
		final Challenge c = new Challenge("A", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
		return PrehledChallenge.zeSektoru(c, new VysledekSektoru(ob, new BitSet(360), new VysledekSektoru.Kandidati[360], 360, new ArrayList<>()), LAT, LON, oblast);
	}

	private static Indexator<Wpt> index(final List<Wpt> wpts) {
		return Indexator.postav(BoundingRect.ALL, wpts, w -> w.getMou().xx, w -> w.getMou().yy);
	}

	@Test
	public void kesMimoOblastSeNezvyrazni() {
		final Wpt uvnitr = vSektoru100("GC1", 1000);
		final Wpt venku = vSektoru100("GC2", 5000);
		final Oblast jenKratke = new Oblast() {
			@Override
			public boolean obsahuje(final double lat, final double lon) {
				return Math.abs(lat - LAT) < 0.01 && Math.abs(lon - LON) < 0.02;
			}

			@Override
			public String verze() {
				return "t";
			}
		};
		Assert.assertTrue(jenKratke.obsahuje(uvnitr.getWgs().lat, uvnitr.getWgs().lon));
		Assert.assertFalse(jenKratke.obsahuje(venku.getWgs().lat, venku.getWgs().lon));
		final CleniVyberu c = CleniVyberu.spust(prehled(jenKratke), index(Arrays.asList(uvnitr, venku)), new VyberZvyrazneni(100, null, null), null, null);
		Assert.assertArrayEquals(new Wpt[] { uvnitr }, c.clenove());
	}

	@Test
	public void bezOblastiSeZvyrazniVsechnyKeseSektoru() {
		final Wpt a = vSektoru100("GC1", 1000);
		final Wpt b = vSektoru100("GC2", 5000);
		final CleniVyberu c = CleniVyberu.spust(prehled(null), index(Arrays.asList(a, b)), new VyberZvyrazneni(100, null, null), null, null);
		Assert.assertEquals(2, c.clenove().length);
	}

	@Test
	public void dalsiWaypointKeseSeNezvyrazni() {
		final Wpt hlavni = vSektoru100("GC1", 1000);
		final Wpt dalsi = vSektoru100("GC2", 1500);
		final Wpt vedlejsi = new Wpt();
		vedlejsi.setWgs(dalsi.getWgs());
		vedlejsi.setSym("Geocache");
		dalsi.getKesoid().addWpt(vedlejsi);
		final CleniVyberu c = CleniVyberu.spust(prehled(null), index(Arrays.asList(hlavni, dalsi, vedlejsi)), new VyberZvyrazneni(100, null, null), null, null);
		Assert.assertEquals("hlavní waypointy dvou keší, ne vedlejší", 2, c.clenove().length);
		for (final Wpt w : c.clenove()) {
			Assert.assertSame(w, w.getKesoid().getMainWpt());
		}
	}

	@Test
	public void zvyrazneniSePrestaviJenKdyzSeZmeniOknoMeritkoVyberNeboCleni() {
		final Wpt a = vSektoru100("GC1", 1000);
		final PrehledChallenge p = prehled(null);
		final Indexator<Wpt> ix = index(Arrays.asList(a));
		final VyberZvyrazneni v = new VyberZvyrazneni(100, null, null);
		final CleniVyberu cl = CleniVyberu.spust(p, ix, v, null, null);
		final TvarPlanovace t = TvarPlanovace.z(p, null);
		final Coord c = new Coord(14, new Wgs(LAT, LON).toMou(), new Dimension(800, 600), 0);
		final ZvyrazneniKese z = ZvyrazneniKese.postav(t, c, ix, cl, 1.0, v);
		Assert.assertEquals(1, z.pocet);
		Assert.assertTrue(z.jeStejne(t, c, ix, cl, 1.0, v));
		final Coord posun = new Coord(14, new Wgs(LAT, LON + 0.01).toMou(), new Dimension(800, 600), 0);
		Assert.assertFalse("jiný tvar", z.jeStejne(TvarPlanovace.z(p, null), c, ix, cl, 1.0, v));
		Assert.assertFalse("posun okna", z.jeStejne(t, posun, ix, cl, 1.0, v));
		Assert.assertFalse("zoom", z.jeStejne(t, new Coord(15, new Wgs(LAT, LON).toMou(), new Dimension(800, 600), 0), ix, cl, 1.0, v));
		Assert.assertFalse("měřítko ikon", z.jeStejne(t, c, ix, cl, 1.5, v));
		Assert.assertFalse("jiný výběr", z.jeStejne(t, c, ix, cl, 1.0, new VyberZvyrazneni(101, null, null)));
		Assert.assertFalse("jiní členové", z.jeStejne(t, c, ix, CleniVyberu.spust(p, ix, v, null, null), 1.0, v));
	}

	@Test
	public void jeStejnyPorovnavaPrehledIndexatorAVyber() {
		final Wpt a = vSektoru100("GC1", 1000);
		final PrehledChallenge p = prehled(null);
		final Indexator<Wpt> ix = index(Arrays.asList(a));
		final VyberZvyrazneni v = new VyberZvyrazneni(100, null, null);
		final CleniVyberu cl = CleniVyberu.spust(p, ix, v, null, null);
		Assert.assertTrue(cl.jeStejny(p, ix, v));
		Assert.assertFalse("jiný přehled", cl.jeStejny(prehled(null), ix, v));
		Assert.assertFalse("jiný index", cl.jeStejny(p, index(Arrays.asList(a)), v));
		Assert.assertFalse("jiný výběr", cl.jeStejny(p, ix, new VyberZvyrazneni(101, null, null)));
	}

	/** Keš v dané vzdálenosti pixelů od středu okna 800 × 600. */
	private static ZvyrazneniKese zvyrazneniKesiNaPixelu(final int dx, final int dy) {
		final Coord c = new Coord(14, new Wgs(LAT, LON).toMou(), new Dimension(800, 600), 0);
		final cz.geokuk.core.coordinates.Mou mou = c.transform(new java.awt.Point(400 + dx, 300 + dy));
		final Wgs wgs = mou.toWgs();
		final Wpt umisteny = vSektoru100("GC2", 1000);
		umisteny.setWgs(wgs);
		final PrehledChallenge p = prehled(null);
		final Indexator<Wpt> ix = index(Arrays.asList(umisteny));
		final int sektor = p.sektorBodu(wgs.lat, wgs.lon);
		final VyberZvyrazneni v = new VyberZvyrazneni(sektor, null, null);
		return ZvyrazneniKese.postav(TvarPlanovace.z(p, null), c, ix, CleniVyberu.spust(p, ix, v, null, null), 1.0, v);
	}

	@Test
	public void kesNaOkrajiOknaSeBereAzDoOkrajeVyrezu() {
		Assert.assertEquals(1, zvyrazneniKesiNaPixelu(-400 - 12, 0).pocet);
		Assert.assertEquals(0, zvyrazneniKesiNaPixelu(-400 - 13, 0).pocet);
		Assert.assertEquals(1, zvyrazneniKesiNaPixelu(400 + 12, 0).pocet);
		Assert.assertEquals(0, zvyrazneniKesiNaPixelu(400 + 13, 0).pocet);
		Assert.assertEquals(1, zvyrazneniKesiNaPixelu(0, -300 - 12).pocet);
		Assert.assertEquals(0, zvyrazneniKesiNaPixelu(0, -300 - 13).pocet);
		Assert.assertEquals(1, zvyrazneniKesiNaPixelu(0, 300 + 12).pocet);
		Assert.assertEquals(0, zvyrazneniKesiNaPixelu(0, 300 + 13).pocet);
	}
}
