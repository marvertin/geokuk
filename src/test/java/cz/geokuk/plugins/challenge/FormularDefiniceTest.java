package cz.geokuk.plugins.challenge;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.DruhStredu;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.Challenge.ZdrojeNalezu;
import cz.geokuk.plugins.challenge.FormularDefinice.Oblast;

/** Převod hodnot formuláře dialogu na definici challenge a zpět. */
public class FormularDefiniceTest {

	private static Oblast oblast(final String id) {
		for (final Oblast o : FormularDefinice.oblasti()) {
			if (o.id == null ? id == null : o.id.equals(id)) {
				return o;
			}
		}
		throw new AssertionError(id);
	}

	private static Challenge sestav(final DruhStredu druh, final String lat, final String lon, final String kod) {
		return FormularDefinice.sestav(null, " Moje ", ZdrojPravidel.STATOR, druh, lat, lon, kod, oblast(null), ZdrojeNalezu.ZAPNUTE);
	}

	@Test
	public void souradniceSeCtouSTeckouINarodnimIiCarkou() {
		final Challenge c = sestav(DruhStredu.SOURADNICE, "50,0875", " 14.42 ", null);
		Assert.assertEquals(50.0875, c.stred().lat(), 0);
		Assert.assertEquals(14.42, c.stred().lon(), 0);
		Assert.assertEquals(DruhStredu.SOURADNICE, c.stred().druh());
	}

	@Test
	public void souradniceMimoRozsahANecisloJsouChyby() {
		for (final String[] s : new String[][] { { "90,1", "14" }, { "-90,1", "14" }, { "50", "180,1" }, { "50", "-181" }, { "abc", "14" }, { "50", "" }, { "NaN", "14" } }) {
			try {
				sestav(DruhStredu.SOURADNICE, s[0], s[1], null);
				Assert.fail(s[0] + " " + s[1]);
			} catch (final ChallengeChyba e) {
				Assert.assertFalse(e.getMessage().isEmpty());
			}
		}
		Assert.assertEquals(90.0, sestav(DruhStredu.SOURADNICE, "90", "180", null).stred().lat(), 0);
		Assert.assertEquals(-180.0, sestav(DruhStredu.SOURADNICE, "-90", "-180", null).stred().lon(), 0);
	}

	@Test
	public void kodListinguSeNormalizujeAOveruje() {
		Assert.assertEquals("GC4PCQB", sestav(DruhStredu.LISTING, null, null, " gc4pcqb ").stred().kodListingu());
		for (final String k : new String[] { "", "ABC123", "GC", "GC 123", "GC12345678", null }) {
			try {
				sestav(DruhStredu.LISTING, null, null, k);
				Assert.fail(String.valueOf(k));
			} catch (final ChallengeChyba e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().contains("GC"));
			}
		}
	}

	@Test
	public void domovNepotrebujeSouradniceAniKod() {
		Assert.assertEquals(DruhStredu.DOMOV, sestav(DruhStredu.DOMOV, "xx", "yy", "!!").stred().druh());
	}

	@Test
	public void souradniceSeCtouJenKdyzJeStredSouradnice() {
		Assert.assertEquals("kód listingu se u souřadnic nečte", DruhStredu.SOURADNICE, sestav(DruhStredu.SOURADNICE, "50", "14", "!!").stred().druh());
	}

	@Test
	public void oblastSeMapujeNaDefinici() {
		final Challenge zadna = FormularDefinice.sestav(null, "A", ZdrojPravidel.PGC, DruhStredu.DOMOV, null, null, null, oblast(null), ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals(OblastDef.zadna(), zadna.oblast());
		final Challenge cesko = FormularDefinice.sestav(null, "A", ZdrojPravidel.PGC, DruhStredu.DOMOV, null, null, null, oblast(Sablony.STAT_CESKO), ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals(OblastDef.stat(Sablony.STAT_CESKO), cesko.oblast());
		final Challenge praha = FormularDefinice.sestav(null, "A", ZdrojPravidel.PGC, DruhStredu.DOMOV, null, null, null, oblast(Sablony.PRAHA), ZdrojeNalezu.VSECHNY);
		Assert.assertEquals(OblastDef.sada(Sablony.PRAHA), praha.oblast());
		Assert.assertEquals(ZdrojeNalezu.VSECHNY, praha.nalezy());
	}

	@Test
	public void sablonyMajiVolbuOblastiAFormularJeVraciBezeZmeny() {
		for (final Challenge s : Sablony.vse()) {
			final Oblast o = FormularDefinice.oblastDefinice(s);
			final Challenge zpet = FormularDefinice.sestav(s, s.nazev(), s.zdroj(), s.stred().druh(), "50", "14", s.stred().kodListingu(), o, s.nalezy());
			Assert.assertEquals(s.oblast(), zpet.oblast());
			Assert.assertEquals(s.zdroj(), zpet.zdroj());
			Assert.assertEquals(s.nalezy(), zpet.nalezy());
			Assert.assertEquals(s.stred().druh(), zpet.stred().druh());
		}
		Assert.assertEquals("Česká republika", FormularDefinice.oblastDefinice(Sablony.cesko360()).popis);
		Assert.assertEquals("Praha a okolí", FormularDefinice.oblastDefinice(Sablony.praha360()).popis);
		Assert.assertNull(FormularDefinice.oblastDefinice(Sablony.od360Domova()).id);
	}

	@Test
	public void nezamaOblastSeBezZmenyVolbyZachova() {
		final Challenge okresy = new Challenge("Okresy", Challenge.Typ.SEKTORY_360, Stred.domov(), OblastDef.sada("okres-a", "okres-b"), ZdrojPravidel.PGC);
		Assert.assertFalse(FormularDefinice.zachovaOblast(okresy));
		final Oblast beze = FormularDefinice.oblastDefinice(okresy);
		Assert.assertEquals("beze změny volby", okresy.oblast(), FormularDefinice.sestav(okresy, "Okresy", ZdrojPravidel.STATOR, DruhStredu.DOMOV, null, null, null, beze, ZdrojeNalezu.ZAPNUTE).oblast());
		Assert.assertEquals("uživatel zvolil Česko", OblastDef.stat(Sablony.STAT_CESKO),
				FormularDefinice.sestav(okresy, "Okresy", ZdrojPravidel.PGC, DruhStredu.DOMOV, null, null, null, oblast(Sablony.STAT_CESKO), ZdrojeNalezu.ZAPNUTE).oblast());
		Assert.assertTrue(FormularDefinice.zachovaOblast(Sablony.cesko360()));
		Assert.assertTrue(FormularDefinice.zachovaOblast(Sablony.od360Domova()));
	}

	@Test
	public void souhrnPopisujePravidlaStredAOblast() {
		Assert.assertEquals("360° · Project-GC · střed: GC4PCQB · oblast: česká republika", FormularDefinice.souhrn(Sablony.cesko360()));
		Assert.assertEquals("360° · Project-GC · střed: domov · oblast: bez omezení", FormularDefinice.souhrn(Sablony.od360Domova()));
		final Challenge c = FormularDefinice.sestav(null, "X", ZdrojPravidel.STATOR, DruhStredu.SOURADNICE, "50,5", "14,25", null, oblast(null), ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals("360° · Stator · střed: 50.50000, 14.25000 · oblast: bez omezení", FormularDefinice.souhrn(c));
	}

	@Test
	public void upravaZachovaTypDefinice() {
		for (final Challenge.Typ typ : Challenge.Typ.values()) {
			final Challenge puvodni = new Challenge("X", typ, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
			final Challenge nova = FormularDefinice.sestav(puvodni, "Y", ZdrojPravidel.STATOR, DruhStredu.DOMOV, null, null, null, oblast(null), ZdrojeNalezu.ZAPNUTE);
			Assert.assertEquals(typ, nova.typ());
		}
		Assert.assertEquals("nová definice je 360°", Challenge.Typ.SEKTORY_360, sestav(DruhStredu.DOMOV, null, null, null).typ());
		Assert.assertTrue(FormularDefinice.souhrn(new Challenge("X", Challenge.Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC)).startsWith("D/T · "));
		Assert.assertTrue(FormularDefinice.souhrn(new Challenge("X", Challenge.Typ.KALENDAR, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC)).startsWith("Kalendář · "));
	}

	@Test
	public void dtZFormulareBereJenNazevANalezyOstatniZustane() {
		final Challenge puvodni = new Challenge("DT", Challenge.Typ.DT_MATICE, Stred.souradnice(50, 14), OblastDef.stat(Sablony.STAT_CESKO), ZdrojPravidel.STATOR);
		final Challenge nova = FormularDefinice.sestav(puvodni, "Nové DT", ZdrojPravidel.PGC, DruhStredu.LISTING, "xx", "yy", "!!", oblast(null), ZdrojeNalezu.VSECHNY);
		Assert.assertEquals("Nové DT", nova.nazev());
		Assert.assertEquals(Challenge.Typ.DT_MATICE, nova.typ());
		Assert.assertEquals(ZdrojeNalezu.VSECHNY, nova.nalezy());
		Assert.assertEquals("střed zůstal, formulářové hodnoty se u D/T nečtou", puvodni.stred(), nova.stred());
		Assert.assertEquals(puvodni.oblast(), nova.oblast());
		Assert.assertEquals(ZdrojPravidel.STATOR, nova.zdroj());
	}

}
