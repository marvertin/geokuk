package cz.geokuk.plugins.challenge;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

public class FormatOblastiTest {

	@Test
	public void nacteOblastSDirouAVicCastmi() throws IOException {
		final String text = "GKOBL 1 dvojice v7\nC\nO 5000000 1400000 200000 0 0 200000 -200000 0\nD 5050000 1450000 50000 0 0 50000 -50000 0\nC\nO 6000000 2400000 100000 0 0 100000 -100000 0\n";
		final OblastPolygony o = FormatOblasti.nacti(new StringReader(text));
		Assert.assertEquals("dvojice", o.id());
		Assert.assertTrue(o.verze().startsWith("v7/"));
		Assert.assertEquals(2, o.casti().size());
		Assert.assertTrue(o.obsahuje(50.01, 14.01));
		Assert.assertFalse("díra", o.obsahuje(50.525, 14.525));
		Assert.assertTrue(o.obsahuje(60.5, 24.5));
		Assert.assertFalse(o.obsahuje(55, 20));
	}

	@Test
	public void zapisANactiVraciStejnouOblast() throws IOException {
		final Okraj vnejsi = new Okraj(new double[] { 50.12345, 50.2, 50.3, 50.1 }, new double[] { 14.55555, 14.7, 14.4, 14.3 });
		final Okraj dira = OblastPolygonyTest.ctverec(50.15, 14.45, 50.17, 14.5);
		final OblastPolygony puvodni = new OblastPolygony("cr", "2026", Arrays.asList(new Cast(vnejsi, Collections.singletonList(dira)), new Cast(OblastPolygonyTest.ctverec(40, 10, 41, 11), Collections.<Okraj> emptyList())));
		final StringWriter w = new StringWriter();
		FormatOblasti.zapis(puvodni, "2026", w);
		final OblastPolygony nactena = FormatOblasti.nacti(new StringReader(w.toString()));
		Assert.assertEquals(puvodni.verze(), nactena.verze());
		Assert.assertEquals(2, nactena.casti().size());
		Assert.assertEquals(2, nactena.casti().get(0).okraje().size());
		Assert.assertEquals(50.12345, nactena.casti().get(0).okraje().get(0).lat(0), 1e-9);
	}

	@Test(expected = IOException.class)
	public void spatnaHlavickaNeprojde() throws IOException {
		FormatOblasti.nacti(new StringReader("JINY 1 a b\nC\nO 1 1 1 1 1 1\n"));
	}

	@Test(expected = IOException.class)
	public void prazdnyVstupNeprojde() throws IOException {
		FormatOblasti.nacti(new StringReader(""));
	}

	@Test(expected = IOException.class)
	public void bezCastiNeprojde() throws IOException {
		FormatOblasti.nacti(new StringReader("GKOBL 1 a b\n"));
	}

	@Test(expected = IOException.class)
	public void neplatneCisloNeprojde() throws IOException {
		FormatOblasti.nacti(new StringReader("GKOBL 1 a b\nC\nO 1 1 x 1 1 1\n"));
	}

	@Test(expected = IOException.class)
	public void licyPocetCiselNeprojde() throws IOException {
		FormatOblasti.nacti(new StringReader("GKOBL 1 a b\nC\nO 1 1 1 1 1\n"));
	}

	@Test(expected = IOException.class)
	public void dveVnejsiHraniceVJedneCastiNeprojdou() throws IOException {
		FormatOblasti.nacti(new StringReader("GKOBL 1 a b\nC\nO 1 1 1 1 1 1\nO 1 1 1 1 1 1\n"));
	}

	@Test
	public void poskytovatelNactePrilozenouOblastAPametujeSi() {
		final ZdrojovyPoskytovatelOblasti p = new ZdrojovyPoskytovatelOblasti("/cz/geokuk/plugins/challenge/testoblasti/");
		final Oblast o = p.najdi("ctverec").orElseThrow(AssertionError::new);
		Assert.assertTrue(o.obsahuje(50.5, 14.5));
		Assert.assertFalse(o.obsahuje(52, 14.5));
		Assert.assertSame(o, p.najdi("ctverec").get());
		Assert.assertFalse(p.najdi("neexistuje").isPresent());
		Assert.assertFalse("neplatné označení", p.najdi("../x").isPresent());
		Assert.assertFalse(p.najdi("").isPresent());
	}

	@Test
	public void obrysPresPolednikNeprojdePriNacteni() {
		try {
			FormatOblasti.nacti(new StringReader("GKOBL 1 a b\nC\nO 6000000 17900000 0 -35800000 100000 0 0 35800000\n"));
			Assert.fail();
		} catch (final IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("180"));
		}
	}

	@Test
	public void castSVic256OkrajiNeprojdePriNacteni() {
		final StringBuilder sb = new StringBuilder("GKOBL 1 a b\nC\nO 5000000 1400000 100000 0 0 100000 -100000 0\n");
		for (int i = 0; i < OblastPolygony.NEJVIC_OKRAJU; i++) {
			sb.append("D 5010000 1410000 1000 0 0 1000 -1000 0\n");
		}
		try {
			FormatOblasti.nacti(new StringReader(sb.toString()));
			Assert.fail();
		} catch (final IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("256"));
		}
	}
}
