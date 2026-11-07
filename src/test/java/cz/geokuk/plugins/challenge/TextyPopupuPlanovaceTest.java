package cz.geokuk.plugins.challenge;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.BitSet;

import org.junit.Test;

/** Texty popupu plánovače: české tvary, vzdálenosti, pořadí chybějících sektorů. */
public class TextyPopupuPlanovaceTest {

	/** Výsledek, kde chybí zadané sektory; kandidáti jsou dvojice sektor → vzdálenosti v metrech. */
	static VysledekSektoru vysledek(final int[] chybi, final int[] nejiste, final double[][] kandidati) {
		final BitSet obsazene = new BitSet(360);
		obsazene.set(0, 360);
		final VysledekSektoru.Kandidati[] k = new VysledekSektoru.Kandidati[360];
		for (final int s : chybi) {
			obsazene.clear(s);
			k[s] = new VysledekSektoru.Kandidati(s);
		}
		for (final double[] radek : kandidati) {
			final int s = (int) radek[0];
			for (int i = 1; i < radek.length; i++) {
				k[s].pridej("GC" + s + "_" + i, radek[i]);
			}
		}
		final BitSet n = new BitSet(360);
		for (final int s : nejiste) {
			n.set(s);
		}
		return new VysledekSektoru(obsazene, n, k, 1000);
	}

	@Test
	public void tvaryKandidatu() {
		assertEquals("0 kandidátů", TextyPopupuPlanovace.kandidatu(0));
		assertEquals("1 kandidát", TextyPopupuPlanovace.kandidatu(1));
		assertEquals("2 kandidáti", TextyPopupuPlanovace.kandidatu(2));
		assertEquals("4 kandidáti", TextyPopupuPlanovace.kandidatu(4));
		assertEquals("5 kandidátů", TextyPopupuPlanovace.kandidatu(5));
		assertEquals("22 kandidátů", TextyPopupuPlanovace.kandidatu(22));
	}

	@Test
	public void vzdalenosti() {
		assertEquals("850 m", TextyPopupuPlanovace.vzdalenost(849.6));
		assertEquals("1,0 km", TextyPopupuPlanovace.vzdalenost(1000));
		assertEquals("2,8 km", TextyPopupuPlanovace.vzdalenost(2790));
		assertEquals("12,3 km", TextyPopupuPlanovace.vzdalenost(12_340));
	}

	@Test
	public void radkySektoru() {
		final VysledekSektoru v = vysledek(new int[] { 213, 305 }, new int[0], new double[][] { { 213, 4000, 2790, 3000, 5000, 6000 } });
		assertEquals("213°  5 kandidátů  nejbližší 2,8 km od středu", TextyPopupuPlanovace.radekSektoru(213, v.kandidati(213)));
		assertEquals("305°  0 kandidátů  –", TextyPopupuPlanovace.radekSektoru(305, v.kandidati(305)));
		assertEquals("10°  0 kandidátů  –", TextyPopupuPlanovace.radekSektoru(10, null));
	}

	@Test
	public void souhrnAProcenta() {
		final int[] chybi = new int[82];
		for (int i = 0; i < chybi.length; i++) {
			chybi[i] = i;
		}
		final VysledekSektoru v = vysledek(chybi, new int[0], new double[0][]);
		assertEquals("Splněno 278 z 360 (77 %), chybí 82", TextyPopupuPlanovace.souhrn(v));
		assertEquals("77 %", TextyPopupuPlanovace.procenta(v));
	}

	@Test
	public void nejisteSektory() {
		assertEquals("", TextyPopupuPlanovace.nejiste(vysledek(new int[] { 1 }, new int[0], new double[0][])));
		assertEquals("U 1 sektoru leží nalezená keš do 0,05° od hranice se sousedním sektorem, takže není jisté, do kterého patří. Na mapě má šedý čárkovaný prstenec.", TextyPopupuPlanovace.nejiste(vysledek(new int[] { 1 }, new int[] { 7 }, new double[0][])));
		assertEquals("U 3 sektorů leží nalezená keš do 0,05° od hranice se sousedním sektorem, takže není jisté, do kterého patří. Na mapě mají šedý čárkovaný prstenec.", TextyPopupuPlanovace.nejiste(vysledek(new int[] { 1 }, new int[] { 1, 2, 3 }, new double[0][])));
		assertEquals("U 5 sektorů leží nalezená keš do 0,05° od hranice se sousedním sektorem, takže není jisté, do kterého patří. Na mapě mají šedý čárkovaný prstenec.", TextyPopupuPlanovace.nejiste(vysledek(new int[] { 1 }, new int[] { 1, 2, 3, 4, 5 }, new double[0][])));
	}

	@Test
	public void chybejiciOdNejblizsihoKandidataBezKandidatuNakonec() {
		final VysledekSektoru v = vysledek(new int[] { 47, 211, 213, 305, 2 }, new int[0], new double[][] { { 213, 2800 }, { 211, 4000 }, { 47, 6200, 9000 } });
		assertEquals(Arrays.asList(213, 211, 47, 2, 305), TextyPopupuPlanovace.serazeneChybejici(v));
	}

	@Test
	public void pravidlaAStred() {
		assertEquals("Stator", TextyPopupuPlanovace.pravidla(Challenge.ZdrojPravidel.STATOR));
		assertEquals("Project-GC", TextyPopupuPlanovace.pravidla(Challenge.ZdrojPravidel.PGC));
		assertEquals("domov", TextyPopupuPlanovace.stred(Challenge.Stred.domov()));
		assertEquals("výchozí bod GC12345", TextyPopupuPlanovace.stred(Challenge.Stred.listing("GC12345")));
		assertEquals("50.08700, 14.42100", TextyPopupuPlanovace.stred(Challenge.Stred.souradnice(50.087, 14.421)));
	}

	@Test
	public void policka() {
		final VysledekPolicek p = new VysledekPolicek(81, Arrays.asList("D1/T1", "D5/T5"), java.util.Collections.emptyList());
		assertEquals("Splněno 79 z 81 (98 %), chybí 2", TextyPopupuPlanovace.souhrn(p));
		assertEquals("98 %", TextyPopupuPlanovace.procenta(p));
		assertEquals("0 %", TextyPopupuPlanovace.procenta(new VysledekPolicek(0, java.util.Collections.emptyList(), java.util.Collections.emptyList())));
		assertEquals("Chybějící políčka D/T:", TextyPopupuPlanovace.nadpisSeznamu(Challenge.Typ.DT_MATICE));
		assertEquals("Chybějící dny:", TextyPopupuPlanovace.nadpisSeznamu(Challenge.Typ.KALENDAR));
		assertEquals("Chybějící sektory (podle vzdálenosti nejbližšího kandidáta):", TextyPopupuPlanovace.nadpisSeznamu(Challenge.Typ.SEKTORY_360));
	}

	@Test
	public void omezeniStitkuSeUkazeJenKdyzSeUplatniStrop() {
		assertEquals(" ", TextyPopupuPlanovace.omezeniStitku(-1));
		assertEquals(" ", TextyPopupuPlanovace.omezeniStitku(200));
		assertEquals("Štítky na mapě: 200 nejbližších z 1\u00a0234", TextyPopupuPlanovace.omezeniStitku(1234));
	}
}
