package cz.geokuk.plugins.challenge;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;
import cz.geokuk.plugins.challenge.ResolverDefinice.Vyreseno;

public class ResolverDefiniceTest {

	private final Map<String, Oblast> oblasti = new HashMap<>();
	private Wgs domov = new Wgs(50.0875, 14.4208);
	private final Map<String, VypocetSektoruTest.Kes> kese = new HashMap<>();

	private final ResolverDefinice resolver = new ResolverDefinice(id -> Optional.ofNullable(oblasti.get(id)), () -> domov);

	private Vyreseno vyres(final Challenge c) {
		return resolver.vyres(c, kod -> Optional.ofNullable(kese.get(kod)));
	}

	private static Oblast ctverec(final String id, final double lat0) {
		return new OblastPolygony(id, "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(lat0, 14, lat0 + 1, 15), Collections.<Okraj> emptyList())));
	}

	private static Challenge definice(final Stred stred, final OblastDef oblast) {
		return new Challenge("T", Typ.SEKTORY_360, stred, oblast, ZdrojPravidel.PGC);
	}

	@Test
	public void domovBereSouradniceDomovaAJeBezOblasti() {
		final Vyreseno v = vyres(definice(Stred.domov(), OblastDef.zadna()));
		Assert.assertTrue(v.jeUplne());
		Assert.assertEquals(50.0875, v.lat, 0);
		Assert.assertEquals(14.4208, v.lon, 0);
		Assert.assertNull(v.oblast);
	}

	@Test
	public void domovBezSouradnicJeNeuplny() {
		domov = null;
		final Vyreseno v = vyres(definice(Stred.domov(), OblastDef.zadna()));
		Assert.assertFalse(v.jeUplne());
		Assert.assertTrue(v.duvod, v.duvod.contains("Domovské"));
	}

	@Test
	public void souradniceSePrevezmou() {
		final Vyreseno v = vyres(definice(Stred.souradnice(49.5, 15.5), OblastDef.zadna()));
		Assert.assertEquals(49.5, v.lat, 0);
		Assert.assertEquals(15.5, v.lon, 0);
	}

	@Test
	public void listingBereSouradniceKesePodleKodu() {
		kese.put("GC4PCQB", new VypocetSektoruTest.Kes("GC4PCQB", 49.8, 15.4, false));
		final Vyreseno v = vyres(definice(Stred.listing("GC4PCQB"), OblastDef.zadna()));
		Assert.assertTrue(v.jeUplne());
		Assert.assertEquals(49.8, v.lat, 0);
		Assert.assertEquals(15.4, v.lon, 0);
	}

	@Test
	public void listingKterySeNenacetlJeNeuplny() {
		final Vyreseno v = vyres(definice(Stred.listing("GC56R49"), OblastDef.zadna()));
		Assert.assertFalse(v.jeUplne());
		Assert.assertTrue(v.duvod, v.duvod.contains("GC56R49"));
	}

	@Test
	public void staStatemJednaOblastASadaJeSjednoceni() {
		oblasti.put("stat-cz", ctverec("stat", 49));
		oblasti.put("a", ctverec("a", 50));
		oblasti.put("b", ctverec("b", 52));
		final Vyreseno stat = vyres(definice(Stred.domov(), OblastDef.stat("stat-cz")));
		Assert.assertSame(oblasti.get("stat-cz"), stat.oblast);
		final Vyreseno sada = vyres(definice(Stred.domov(), OblastDef.sada("a", "b")));
		Assert.assertTrue(sada.oblast.obsahuje(50.5, 14.5));
		Assert.assertTrue(sada.oblast.obsahuje(52.5, 14.5));
		Assert.assertFalse(sada.oblast.obsahuje(51.5, 14.5));
	}

	@Test
	public void chybejiciDataOblastiJsouNeuplnaDefinice() {
		oblasti.put("a", ctverec("a", 50));
		final Vyreseno v = vyres(definice(Stred.domov(), OblastDef.sada("a", "okres-praha")));
		Assert.assertFalse(v.jeUplne());
		Assert.assertTrue(v.duvod, v.duvod.contains("okres-praha"));
	}
}
