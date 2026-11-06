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

	private static Challenge sestav(final DruhStredu druh, final String souradnice, final String kod) {
		return FormularDefinice.sestav(null, " Moje ", ZdrojPravidel.STATOR, druh, souradnice, kod, oblast(null), ZdrojeNalezu.ZAPNUTE);
	}

	@Test
	public void souradniceSeCtouVeFormatuSchranky() {
		final Challenge c = sestav(DruhStredu.SOURADNICE, " N50°03.253 E14°23.399 ", null);
		Assert.assertEquals(50.0 + 3.253 / 60, c.stred().lat(), 1e-9);
		Assert.assertEquals(14.0 + 23.399 / 60, c.stred().lon(), 1e-9);
		Assert.assertEquals(DruhStredu.SOURADNICE, c.stred().druh());
		final Challenge s = sestav(DruhStredu.SOURADNICE, "S12°30.000 W45°00.500", null);
		Assert.assertEquals(-12.5, s.stred().lat(), 1e-9);
		Assert.assertEquals(-45.0 - 0.5 / 60, s.stred().lon(), 1e-9);
	}

	@Test
	public void souradniceSeCtouiJakoDesetinneStupne() {
		final Challenge c = sestav(DruhStredu.SOURADNICE, "50,0875 14.42", null);
		Assert.assertEquals(50.0875, c.stred().lat(), 0);
		Assert.assertEquals(14.42, c.stred().lon(), 0);
	}

	@Test
	public void zobrazeniAZapisSeShodujiRoundTrip() {
		final Challenge c = sestav(DruhStredu.SOURADNICE, FormularDefinice.UKAZKA_SOURADNIC, null);
		Assert.assertEquals(FormularDefinice.UKAZKA_SOURADNIC, FormularDefinice.formatSouradnic(c.stred().lat(), c.stred().lon()));
		final Challenge znovu = sestav(DruhStredu.SOURADNICE, FormularDefinice.formatSouradnic(c.stred().lat(), c.stred().lon()), null);
		Assert.assertEquals(c.stred().lat(), znovu.stred().lat(), 1e-12);
		Assert.assertEquals(c.stred().lon(), znovu.stred().lon(), 1e-12);
	}

	@Test
	public void nezmenenyTextZachovaPuvodniPresnost() {
		final Challenge puvodni = FormularDefinice.sestav(null, "X", ZdrojPravidel.STATOR, DruhStredu.SOURADNICE, "50.123456789 14.987654321", null, oblast(null), ZdrojeNalezu.ZAPNUTE);
		final String text = FormularDefinice.formatSouradnic(puvodni.stred().lat(), puvodni.stred().lon());
		final Challenge beze = FormularDefinice.sestav(puvodni, "Y", ZdrojPravidel.STATOR, DruhStredu.SOURADNICE, text, null, oblast(null), ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals(50.123456789, beze.stred().lat(), 0);
		Assert.assertEquals(14.987654321, beze.stred().lon(), 0);
		final Challenge zmenene = FormularDefinice.sestav(puvodni, "Y", ZdrojPravidel.STATOR, DruhStredu.SOURADNICE, "N50°00.000 E14°00.000", null, oblast(null), ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals(50.0, zmenene.stred().lat(), 0);
	}

	@Test
	public void souradniceMimoRozsahANerozpoznaneJsouChyby() {
		for (final String s : new String[] { "abc", "", "50", "N95°00.000 E14°00.000", "N50°00.000 E181°00.000", "NaN 14", null }) {
			try {
				sestav(DruhStredu.SOURADNICE, s, null);
				Assert.fail(String.valueOf(s));
			} catch (final ChallengeChyba e) {
				Assert.assertFalse(e.getMessage().isEmpty());
			}
		}
		try {
			sestav(DruhStredu.SOURADNICE, "abc", null);
		} catch (final ChallengeChyba e) {
			Assert.assertEquals("Souřadnice neznám. Zadejte např. N50°03.253 E14°23.399.", e.getMessage());
		}
		Assert.assertEquals(90.0, sestav(DruhStredu.SOURADNICE, "N90°00.000 E180°00.000", null).stred().lat(), 0);
	}

	@Test
	public void kodListinguSeNormalizujeAOveruje() {
		Assert.assertEquals("GC4PCQB", sestav(DruhStredu.LISTING, null, " gc4pcqb ").stred().kodListingu());
		for (final String k : new String[] { "", "ABC123", "GC", "GC 123", "GC12345678", null }) {
			try {
				sestav(DruhStredu.LISTING, null, k);
				Assert.fail(String.valueOf(k));
			} catch (final ChallengeChyba e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().contains("GC"));
			}
		}
	}

	@Test
	public void domovNepotrebujeSouradniceAniKod() {
		Assert.assertEquals(DruhStredu.DOMOV, sestav(DruhStredu.DOMOV, "xx", "!!").stred().druh());
	}

	@Test
	public void souradniceSeCtouJenKdyzJeStredSouradnice() {
		Assert.assertEquals("kód listingu se u souřadnic nečte", DruhStredu.SOURADNICE, sestav(DruhStredu.SOURADNICE, "50 14", "!!").stred().druh());
	}

	@Test
	public void oblastSeMapujeNaDefinici() {
		final Challenge zadna = FormularDefinice.sestav(null, "A", ZdrojPravidel.PGC, DruhStredu.DOMOV, null, null, oblast(null), ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals(OblastDef.zadna(), zadna.oblast());
		final Challenge cesko = FormularDefinice.sestav(null, "A", ZdrojPravidel.PGC, DruhStredu.DOMOV, null, null, oblast(Sablony.STAT_CESKO), ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals(OblastDef.stat(Sablony.STAT_CESKO), cesko.oblast());
		final Challenge praha = FormularDefinice.sestav(null, "A", ZdrojPravidel.PGC, DruhStredu.DOMOV, null, null, oblast(Sablony.PRAHA), ZdrojeNalezu.VSECHNY);
		Assert.assertEquals(OblastDef.sada(Sablony.PRAHA), praha.oblast());
		Assert.assertEquals(ZdrojeNalezu.VSECHNY, praha.nalezy());
	}

	@Test
	public void sablonyMajiVolbuOblastiAFormularJeVraciBezeZmeny() {
		for (final Challenge s : Sablony.vse()) {
			final Oblast o = FormularDefinice.oblastDefinice(s);
			final Challenge zpet = FormularDefinice.sestav(s, s.nazev(), s.zdroj(), s.stred().druh(), "50 14", s.stred().kodListingu(), o, s.nalezy());
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
		Assert.assertEquals("beze změny volby", okresy.oblast(), FormularDefinice.sestav(okresy, "Okresy", ZdrojPravidel.STATOR, DruhStredu.DOMOV, null, null, beze, ZdrojeNalezu.ZAPNUTE).oblast());
		Assert.assertEquals("uživatel zvolil Česko", OblastDef.stat(Sablony.STAT_CESKO),
				FormularDefinice.sestav(okresy, "Okresy", ZdrojPravidel.PGC, DruhStredu.DOMOV, null, null, oblast(Sablony.STAT_CESKO), ZdrojeNalezu.ZAPNUTE).oblast());
		Assert.assertTrue(FormularDefinice.zachovaOblast(Sablony.cesko360()));
		Assert.assertTrue(FormularDefinice.zachovaOblast(Sablony.od360Domova()));
	}

	@Test
	public void souhrnPopisujePravidlaStredAOblast() {
		Assert.assertEquals("360° · Project-GC · střed: GC4PCQB · oblast: česká republika", FormularDefinice.souhrn(Sablony.cesko360()));
		Assert.assertEquals("360° · Project-GC · střed: domov · oblast: bez omezení", FormularDefinice.souhrn(Sablony.od360Domova()));
		final Challenge c = FormularDefinice.sestav(null, "X", ZdrojPravidel.STATOR, DruhStredu.SOURADNICE, "50,5 14,25", null, oblast(null), ZdrojeNalezu.ZAPNUTE);
		Assert.assertEquals("360° · Stator · střed: N50°30.000 E14°15.000 · oblast: bez omezení", FormularDefinice.souhrn(c));
	}

	@Test
	public void upravaZachovaTypDefinice() {
		for (final Challenge.Typ typ : Challenge.Typ.values()) {
			final Challenge puvodni = new Challenge("X", typ, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
			final Challenge nova = FormularDefinice.sestav(puvodni, "Y", ZdrojPravidel.STATOR, DruhStredu.DOMOV, null, null, oblast(null), ZdrojeNalezu.ZAPNUTE);
			Assert.assertEquals(typ, nova.typ());
		}
		Assert.assertEquals("nová definice je 360°", Challenge.Typ.SEKTORY_360, sestav(DruhStredu.DOMOV, null, null).typ());
		Assert.assertTrue(FormularDefinice.souhrn(new Challenge("X", Challenge.Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC)).startsWith("D/T · "));
		Assert.assertTrue(FormularDefinice.souhrn(new Challenge("X", Challenge.Typ.KALENDAR, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC)).startsWith("Kalendář · "));
	}

	@Test
	public void dtZFormulareBereJenNazevANalezyOstatniZustane() {
		final Challenge puvodni = new Challenge("DT", Challenge.Typ.DT_MATICE, Stred.souradnice(50, 14), OblastDef.stat(Sablony.STAT_CESKO), ZdrojPravidel.STATOR);
		final Challenge nova = FormularDefinice.sestav(puvodni, "Nové DT", ZdrojPravidel.PGC, DruhStredu.LISTING, "xx", "!!", oblast(null), ZdrojeNalezu.VSECHNY);
		Assert.assertEquals("Nové DT", nova.nazev());
		Assert.assertEquals(Challenge.Typ.DT_MATICE, nova.typ());
		Assert.assertEquals(ZdrojeNalezu.VSECHNY, nova.nalezy());
		Assert.assertEquals("střed zůstal, formulářové hodnoty se u D/T nečtou", puvodni.stred(), nova.stred());
		Assert.assertEquals(puvodni.oblast(), nova.oblast());
		Assert.assertEquals(ZdrojPravidel.STATOR, nova.zdroj());
	}

}
