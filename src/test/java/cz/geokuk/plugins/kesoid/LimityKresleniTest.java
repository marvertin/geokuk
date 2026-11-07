package cz.geokuk.plugins.kesoid;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.JKesoidySlide.Kresleni;

public class LimityKresleniTest {

	@After
	public void uklid() {
		System.clearProperty("geokuk.limitIkon");
		System.clearProperty("geokuk.limitTecek");
	}

	@Test
	public void vychozi() {
		assertEquals(90_000, LimityKresleni.VYCHOZI.getIkon());
		assertEquals(300_000, LimityKresleni.VYCHOZI.getTecek());
	}

	@Test
	public void mimoRozsahSeOrizne() {
		assertEquals(30_000, LimityKresleni.of(10, -5).getIkon());
		assertEquals(60_000, LimityKresleni.of(10, -5).getTecek());
		assertEquals(LimityKresleni.of(2_000_000, 2_000_000), LimityKresleni.of(5_000_000, Integer.MAX_VALUE));
	}

	@Test
	public void tecekAsponTolikJakoIkon() {
		final LimityKresleni l = LimityKresleni.of(120_000, 50_000);
		assertEquals(120_000, l.getIkon());
		assertEquals(120_000, l.getTecek());
	}

	@Test
	public void vlastnostiPrebijiUlozene() {
		try {
			System.setProperty("geokuk.limitIkon", "60000");
			System.setProperty("geokuk.limitTecek", "1000000");
			assertEquals(LimityKresleni.of(60_000, 1_000_000), LimityKresleni.VYCHOZI.sVlastnostmi());
			System.clearProperty("geokuk.limitTecek");
			assertEquals(LimityKresleni.of(60_000, 300_000), LimityKresleni.VYCHOZI.sVlastnostmi());
		} finally {
			System.clearProperty("geokuk.limitIkon");
			System.clearProperty("geokuk.limitTecek");
		}
	}

	@Test
	public void automatickyIkonyDoLimituPakTeckyPakNic() {
		final LimityKresleni l = LimityKresleni.of(90_000, 300_000);
		assertEquals(Kresleni.IKONY, JKesoidySlide.kresleni(EZobrazeniKesi.AUTOMATICKY, 15, 90_000, l, false));
		assertEquals(Kresleni.TECKY, JKesoidySlide.kresleni(EZobrazeniKesi.AUTOMATICKY, 15, 90_001, l, false));
		assertEquals(Kresleni.TECKY, JKesoidySlide.kresleni(EZobrazeniKesi.AUTOMATICKY, 15, 300_000, l, false));
		assertEquals(Kresleni.NAD_LIMITEM_TECEK, JKesoidySlide.kresleni(EZobrazeniKesi.AUTOMATICKY, 15, 300_001, l, false));
		assertEquals("malé měřítko = tečky", Kresleni.TECKY, JKesoidySlide.kresleni(EZobrazeniKesi.AUTOMATICKY, 10, 100, l, false));
	}

	@Test
	public void vynuceneIkonyATecky() {
		final LimityKresleni l = LimityKresleni.of(90_000, 300_000);
		assertEquals(Kresleni.NAD_LIMITEM_IKON, JKesoidySlide.kresleni(EZobrazeniKesi.IKONY, 15, 90_001, l, false));
		assertEquals(Kresleni.IKONY, JKesoidySlide.kresleni(EZobrazeniKesi.IKONY, 5, 100, l, false));
		assertEquals(Kresleni.TECKY, JKesoidySlide.kresleni(EZobrazeniKesi.TECKY, 15, 100, l, false));
		assertEquals(Kresleni.NAD_LIMITEM_TECEK, JKesoidySlide.kresleni(EZobrazeniKesi.TECKY, 15, 300_001, l, false));
	}

	@Test
	public void doSouboruBezLimitu() {
		final LimityKresleni l = LimityKresleni.of(30_000, 60_000);
		assertEquals(Kresleni.IKONY, JKesoidySlide.kresleni(EZobrazeniKesi.IKONY, 15, 1_000_000, l, true));
		assertEquals(Kresleni.TECKY, JKesoidySlide.kresleni(EZobrazeniKesi.TECKY, 15, 1_000_000, l, true));
	}
}
