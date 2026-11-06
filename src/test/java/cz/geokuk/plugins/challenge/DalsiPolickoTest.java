package cz.geokuk.plugins.challenge;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class DalsiPolickoTest {

	private static VysledekPolicek v(final KandidatPolicka... k) {
		return new VysledekPolicek(81, Arrays.asList("D1/T1"), Arrays.asList(k));
	}

	private static KandidatPolicka k(final String id, final double lat, final Integer dni) {
		return new KandidatPolicka(id, lat, 14, "D1/T1", "D1/T1", dni);
	}

	@Test
	public void bezDatVybiraNejblizsiAPakCykluje() {
		final VysledekPolicek v = v(k("A", 50.0, null), k("B", 50.1, null), k("C", 51, null));
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertEquals("B", d.dalsi(v, 50.12, 14).id());
		Assert.assertEquals("A", d.dalsi(v, 50.1, 14).id());
		Assert.assertEquals("C", d.dalsi(v, 50.0, 14).id());
		Assert.assertEquals("B", d.dalsi(v, 50.12, 14).id());
	}

	@Test
	public void prednostMaBlizsiDatumPotomVzdalenost() {
		final VysledekPolicek v = v(k("daleko", 55, 3), k("blizko", 50, 9), k("stejneDaleko", 52, 3));
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertEquals("stejneDaleko", d.dalsi(v, 50, 14).id());
		Assert.assertEquals("daleko", d.dalsi(v, 50, 14).id());
		Assert.assertEquals("blizko", d.dalsi(v, 50, 14).id());
	}

	@Test
	public void novyVysledekZacneKoloZnovu() {
		final DalsiPolicko d = new DalsiPolicko();
		final VysledekPolicek a = v(k("A", 50, null), k("B", 51, null));
		Assert.assertEquals("A", d.dalsi(a, 50, 14).id());
		Assert.assertEquals("A", d.dalsi(v(k("A", 50, null), k("B", 51, null)), 50, 14).id());
	}

	@Test
	public void resetZacneKoloZnovu() {
		final DalsiPolicko d = new DalsiPolicko();
		final VysledekPolicek a = v(k("A", 50, null), k("B", 51, null));
		Assert.assertEquals("A", d.dalsi(a, 50, 14).id());
		d.reset();
		Assert.assertEquals("A", d.dalsi(a, 50, 14).id());
	}

	@Test
	public void priShodeVyhrajePrvni() {
		final VysledekPolicek v = v(k("PRVNI", 50, 3), k("DRUHA", 50, 3));
		Assert.assertEquals("PRVNI", new DalsiPolicko().dalsi(v, 50, 14).id());
	}

	@Test
	public void bezKandidatuJeNull() {
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertNull(d.dalsi(v(), 50, 14));
		Assert.assertNull(d.dalsi(null, 50, 14));
	}

	@Test
	public void predchoziVratiPredchozihoVybranehoAAktualniSeMuzeVybratZnovu() {
		final VysledekPolicek v = v(k("A", 50.0, null), k("B", 50.1, null), k("C", 50.5, null));
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertNull("nic nebylo vybráno", d.predchozi(v));
		Assert.assertEquals("A", d.dalsi(v, 50.0, 14).id());
		Assert.assertNull("před prvním nic není", d.predchozi(v));
		Assert.assertEquals("B", d.dalsi(v, 50.0, 14).id());
		Assert.assertEquals("C", d.dalsi(v, 50.0, 14).id());
		Assert.assertEquals("B", d.predchozi(v).id());
		Assert.assertEquals("C je zase k dispozici", "C", d.dalsi(v, 50.0, 14).id());
		Assert.assertEquals("B", d.predchozi(v).id());
		Assert.assertEquals("A", d.predchozi(v).id());
		Assert.assertNull(d.predchozi(v));
		d.dalsi(v, 50.0, 14);
		d.dalsi(v, 50.0, 14);
		Assert.assertNull("jiná instance výsledku, i se stejnými kandidáty", d.predchozi(v(k("A", 50.0, null), k("B", 50.1, null), k("C", 50.5, null))));
		Assert.assertNull(d.predchozi(null));
	}

	@Test
	public void predchoziPoResetuAPoNovemKoluNeniKam() {
		final VysledekPolicek v = v(k("A", 50.0, null), k("B", 50.1, null));
		final DalsiPolicko d = new DalsiPolicko();
		d.dalsi(v, 50.0, 14);
		d.dalsi(v, 50.0, 14);
		d.reset();
		Assert.assertNull(d.predchozi(v));
		d.dalsi(v, 50.0, 14);
		d.dalsi(v, 50.0, 14);
		d.dalsi(v, 50.0, 14);
		Assert.assertNull("nové kolo začíná od jediného vybraného", d.predchozi(v));
	}

	@Test
	public void vysledekTehozVstupuPokracujeVKole() {
		final Object vstup = new Object();
		final VysledekPolicek v1 = v(k("A", 50.0, null), k("B", 50.1, null), k("C", 50.5, null)).sVstupem(vstup);
		final VysledekPolicek v2 = v(k("A", 50.0, null), k("B", 50.1, null), k("C", 50.5, null)).sVstupem(vstup);
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertEquals("A", d.dalsi(v1, 50.0, 14).id());
		Assert.assertEquals("pokračuje, nezačíná od A", "B", d.dalsi(v2, 50.0, 14).id());
		Assert.assertEquals("předchozí platí i přes nový výsledek", "A", d.predchozi(v2).id());
		Assert.assertEquals("B", d.dalsi(v2, 50.0, 14).id());
	}

	@Test
	public void predchoziVraciKandidataIKdyzNovyVysledekHoUzNema() {
		final Object vstup = new Object();
		final VysledekPolicek v1 = v(k("A", 50.0, null), k("B", 50.1, null)).sVstupem(vstup);
		final VysledekPolicek v2 = v(k("B", 50.1, null), k("C", 50.5, null)).sVstupem(vstup);
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertEquals("A", d.dalsi(v1, 50.0, 14).id());
		Assert.assertEquals("B", d.dalsi(v2, 50.1, 14).id());
		Assert.assertEquals("A", d.predchozi(v2).id());
	}

	@Test
	public void vysledekJinehoVstupuZacneKoloAHistoriiSmaze() {
		final Object a = new Object();
		final VysledekPolicek v1 = v(k("A", 50.0, null), k("B", 50.1, null)).sVstupem(a);
		final VysledekPolicek v2 = v(k("A", 50.0, null), k("B", 50.1, null)).sVstupem(new Object());
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertEquals("A", d.dalsi(v1, 50.0, 14).id());
		Assert.assertEquals("B", d.dalsi(v1, 50.0, 14).id());
		Assert.assertEquals("A", d.dalsi(v2, 50.0, 14).id());
		Assert.assertNull("historie původního vstupu je pryč", d.predchozi(v2));
		Assert.assertNull("výsledek původního vstupu už neplatí", d.predchozi(v1));
	}

	@Test(expected = NullPointerException.class)
	public void vstupMusiBytZadany() {
		v(k("A", 50.0, null)).sVstupem(null);
	}
}
