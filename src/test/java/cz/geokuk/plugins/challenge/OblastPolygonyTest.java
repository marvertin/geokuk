package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

public class OblastPolygonyTest {

	static Okraj ctverec(final double lat0, final double lon0, final double lat1, final double lon1) {
		return new Okraj(new double[] { lat0, lat1, lat1, lat0 }, new double[] { lon0, lon0, lon1, lon1 });
	}

	private static OblastPolygony oblast(final Cast... casti) {
		return new OblastPolygony("t", "v1", Arrays.asList(casti));
	}

	@Test
	public void bodVCtverci() {
		final OblastPolygony o = oblast(new Cast(ctverec(50, 14, 51, 15), Collections.<Okraj> emptyList()));
		Assert.assertTrue(o.obsahuje(50.5, 14.5));
		Assert.assertFalse(o.obsahuje(49.99, 14.5));
		Assert.assertFalse(o.obsahuje(51.01, 14.5));
		Assert.assertFalse(o.obsahuje(50.5, 13.99));
		Assert.assertFalse(o.obsahuje(50.5, 15.01));
	}

	@Test
	public void diraNeniSouCastiOblasti() {
		final OblastPolygony o = oblast(new Cast(ctverec(50, 14, 52, 16), Collections.singletonList(ctverec(50.5, 14.5, 51.5, 15.5))));
		Assert.assertTrue(o.obsahuje(50.2, 14.2));
		Assert.assertFalse("v díře", o.obsahuje(51, 15));
		Assert.assertTrue(o.obsahuje(51.8, 15.8));
	}

	@Test
	public void vicCastiJeSjednoceni() {
		final OblastPolygony o = oblast(new Cast(ctverec(50, 14, 51, 15), Collections.<Okraj> emptyList()), new Cast(ctverec(60, 24, 61, 25), Collections.<Okraj> emptyList()));
		Assert.assertTrue(o.obsahuje(50.5, 14.5));
		Assert.assertTrue(o.obsahuje(60.5, 24.5));
		Assert.assertFalse(o.obsahuje(55, 20));
	}

	@Test
	public void nekonvexniOkraj() {
		// Písmeno L: vodorovná a svislá část.
		final Okraj l = new Okraj(new double[] { 50, 50, 50.5, 50.5, 51, 51 }, new double[] { 14, 15, 15, 14.5, 14.5, 14 });
		final OblastPolygony o = oblast(new Cast(l, Collections.<Okraj> emptyList()));
		Assert.assertTrue(o.obsahuje(50.2, 14.8));
		Assert.assertTrue(o.obsahuje(50.8, 14.2));
		Assert.assertFalse("ve výřezu písmene", o.obsahuje(50.8, 14.8));
	}

	@Test
	public void pasyDavajiStejnyVysledekJakoPrimyVypocet() {
		// Nepravidelný okraj s mnoha hranami a dírou; porovnání s přímým průchodem všemi hranami.
		final Random r = new Random(7);
		final int n = 200;
		final double[] lat = new double[n];
		final double[] lon = new double[n];
		for (int i = 0; i < n; i++) {
			final double uhel = 2 * Math.PI * i / n;
			final double polomer = 1 + 0.4 * Math.sin(7 * uhel) + 0.1 * r.nextDouble();
			lat[i] = 50 + polomer * Math.sin(uhel);
			lon[i] = 14 + 1.5 * polomer * Math.cos(uhel);
		}
		final Okraj vnejsi = new Okraj(lat, lon);
		final Okraj dira = ctverec(49.8, 13.8, 50.2, 14.2);
		final OblastPolygony o = oblast(new Cast(vnejsi, Collections.singletonList(dira)));
		for (int i = 0; i < 20000; i++) {
			final double la = 48 + 4 * r.nextDouble();
			final double lo = 11.5 + 5 * r.nextDouble();
			final boolean ocekavano = naivne(vnejsi, la, lo) ^ naivne(dira, la, lo);
			Assert.assertEquals(la + "," + lo, ocekavano, o.obsahuje(la, lo));
		}
	}

	private static boolean naivne(final Okraj o, final double lat, final double lon) {
		boolean uvnitr = false;
		final int n = o.pocetBodu();
		for (int i = 0, j = n - 1; i < n; j = i++) {
			if (o.lat(i) > lat != o.lat(j) > lat && lon < (o.lon(j) - o.lon(i)) * (lat - o.lat(i)) / (o.lat(j) - o.lat(i)) + o.lon(i)) {
				uvnitr = !uvnitr;
			}
		}
		return uvnitr;
	}

	@Test
	public void bodNaHorniAdolniMeziSeNerozpadne() {
		final OblastPolygony o = oblast(new Cast(ctverec(50, 14, 51, 15), Collections.<Okraj> emptyList()));
		// Kraje: nesmí vyhodit výjimku; na dolní hraně leží uvnitř, na horní vně (pravidlo půlotevřeného intervalu).
		Assert.assertTrue(o.obsahuje(50, 14.5));
		Assert.assertFalse(o.obsahuje(51, 14.5));
	}

	@Test
	public void verzeSeMeniSDatyAObsahem() {
		final Cast c = new Cast(ctverec(50, 14, 51, 15), Collections.<Okraj> emptyList());
		final String v1 = new OblastPolygony("t", "v1", Collections.singletonList(c)).verze();
		Assert.assertTrue(v1, v1.startsWith("v1/"));
		Assert.assertEquals(v1, new OblastPolygony("t", "v1", Collections.singletonList(c)).verze());
		Assert.assertNotEquals(v1, new OblastPolygony("t", "v2", Collections.singletonList(c)).verze());
		final Cast jina = new Cast(ctverec(50, 14, 51, 15.1), Collections.<Okraj> emptyList());
		Assert.assertNotEquals(v1, new OblastPolygony("t", "v1", Collections.singletonList(jina)).verze());
	}

	@Test(expected = IllegalArgumentException.class)
	public void okrajMaAspon3Body() {
		new Okraj(new double[] { 1, 2 }, new double[] { 1, 2 });
	}

	@Test(expected = IllegalArgumentException.class)
	public void oblastBezCastiNeni() {
		new OblastPolygony("t", "v1", new ArrayList<Cast>());
	}

	@Test
	public void sjednoceniJeBodVKterekoliOblasti() {
		final OblastPolygony a = oblast(new Cast(ctverec(50, 14, 51, 15), Collections.<Okraj> emptyList()));
		final OblastPolygony b = new OblastPolygony("b", "v1", Collections.singletonList(new Cast(ctverec(52, 16, 53, 17), Collections.<Okraj> emptyList())));
		final SjednoceniOblasti s = new SjednoceniOblasti(Arrays.asList(a, b));
		Assert.assertTrue(s.obsahuje(50.5, 14.5));
		Assert.assertTrue(s.obsahuje(52.5, 16.5));
		Assert.assertFalse(s.obsahuje(51.5, 15.5));
		final SjednoceniOblasti obracene = new SjednoceniOblasti(Arrays.asList(b, a));
		Assert.assertNotEquals("pořadí je součástí verze", s.verze(), obracene.verze());
		Assert.assertNotEquals(s.verze(), new SjednoceniOblasti(Collections.singletonList(a)).verze());
	}

	@Test
	public void okrajPresPolednik180SeNepodporuje() {
		try {
			new Okraj(new double[] { 60, 60, 61, 61 }, new double[] { 179, -179, -179, 179 });
			Assert.fail();
		} catch (final IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("180"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void bodMimoRozsahSouradnicJeChyba() {
		new Okraj(new double[] { 50, 50, 91 }, new double[] { 14, 15, 14 });
	}

	@Test
	public void castSNejvic256OkrajiSeNactea257NeniPripustne() {
		final List<Okraj> diry = new ArrayList<>();
		for (int i = 0; i < OblastPolygony.NEJVIC_OKRAJU - 1; i++) {
			diry.add(ctverec(50.1 + i * 0.001, 14.1, 50.1005 + i * 0.001, 14.2));
		}
		final OblastPolygony o = oblast(new Cast(ctverec(50, 14, 52, 16), diry));
		Assert.assertTrue(o.obsahuje(50.05, 14.5));
		Assert.assertFalse("v 256. okraji (díře 255)", o.obsahuje(50.1 + 254 * 0.001 + 0.0002, 14.15));
		diry.add(ctverec(51, 15, 51.1, 15.1));
		try {
			new Cast(ctverec(50, 14, 52, 16), diry);
			Assert.fail();
		} catch (final IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("256"));
		}
	}

	@Test
	public void okrajSeSouradnicemNaMeziRozsahuJeVporadku() {
		Assert.assertEquals(4, new Okraj(new double[] { -90, 90, 90, -90 }, new double[] { -180, -180, 0, 0 }).pocetBodu());
		Assert.assertEquals(4, new Okraj(new double[] { -90, 90, 90, -90 }, new double[] { 0, 0, 180, 180 }).pocetBodu());
	}

	@Test
	public void souradniceTesneZaMezemRozsahuJsouChyba() {
		final double[][][] spatne = {
				{ { -90.0001, 0, 1 }, { 0, 1, 1 } },
				{ { 90.0001, 0, 1 }, { 0, 1, 1 } },
				{ { 0, 0, 1 }, { -180.0001, 1, 1 } },
				{ { 0, 0, 1 }, { 180.0001, 1, 1 } } };
		for (final double[][] s : spatne) {
			try {
				new Okraj(s[0], s[1]);
				Assert.fail(Arrays.toString(s[0]) + " " + Arrays.toString(s[1]));
			} catch (final IllegalArgumentException e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().contains("mimo rozsah"));
			}
		}
	}

	@Test
	public void rozdilDelekPresne180StupnuAVychodniDelkyBezPresahuJsouVporadku() {
		new Okraj(new double[] { 0, 1, 2 }, new double[] { 0, 180, 0 });
		new Okraj(new double[] { 0, 1, 2 }, new double[] { 100, 120, 110 });
		try {
			new Okraj(new double[] { 0, 1, 2 }, new double[] { 0, 180.0001, 0 });
			Assert.fail();
		} catch (final IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("180"));
		}
	}

	private static String verzeBodu(final double[] lat, final double[] lon) {
		final String v = new OblastPolygony("t", "d", Collections.singletonList(new Cast(new Okraj(lat, lon), Collections.<Okraj> emptyList()))).verze();
		return v.substring(v.indexOf('/') + 1);
	}

	@Test
	public void otiskGeometrieRozlisujeMikrostupneOddelovaceAPoradiSouradnic() {
		final double u = 1e-6;
		final double[] lat0 = { 10, 10.5, 11 };
		final double[] lon0 = { 20, 21, 20.5 };
		final String zaklad = verzeBodu(lat0, lon0);
		Assert.assertEquals("shodná geometrie", zaklad, verzeBodu(lat0.clone(), lon0.clone()));
		final double[] lat1 = lat0.clone();
		lat1[0] += u;
		Assert.assertNotEquals("změna šířky o 1e-6", zaklad, verzeBodu(lat1, lon0));
		final double[] lon1 = lon0.clone();
		lon1[0] += u;
		Assert.assertNotEquals("změna délky o 1e-6", zaklad, verzeBodu(lat0, lon1));
		Assert.assertNotEquals("záměna šířky a délky", verzeBodu(new double[] { 10, 11, 12 }, new double[] { 20, 21, 22 }), verzeBodu(new double[] { 20, 21, 22 }, new double[] { 10, 11, 12 }));
		// Spojení číslic bez oddělovače by dalo stejný otisk.
		Assert.assertNotEquals("oddělovač šířky a délky", verzeBodu(new double[] { 1 * u, 4 * u, 6 * u }, new double[] { 23 * u, 5 * u, 7 * u }),
				verzeBodu(new double[] { 12 * u, 4 * u, 6 * u }, new double[] { 3 * u, 5 * u, 7 * u }));
		Assert.assertNotEquals("oddělovač bodů", verzeBodu(new double[] { 1 * u, 4 * u, 6 * u }, new double[] { 23 * u, 5 * u, 7 * u }),
				verzeBodu(new double[] { 1 * u, 34 * u, 6 * u }, new double[] { 2 * u, 5 * u, 7 * u }));
	}

	private static Oblast oblastSVerzi(final String verze) {
		return new Oblast() {
			@Override
			public boolean obsahuje(final double lat, final double lon) {
				return false;
			}

			@Override
			public String verze() {
				return verze;
			}
		};
	}

	@Test
	public void sjednoceniOddelujeVerzeJednotlivychOblasti() {
		final String ab = new SjednoceniOblasti(Arrays.asList(oblastSVerzi("a"), oblastSVerzi("bc"))).verze();
		final String ba = new SjednoceniOblasti(Arrays.asList(oblastSVerzi("ab"), oblastSVerzi("c"))).verze();
		Assert.assertNotEquals(ab, ba);
		Assert.assertTrue(ab, ab.startsWith("U2/"));
	}
}
