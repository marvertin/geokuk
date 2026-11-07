package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/** Násobnost matice D/T: výpočet pro N = 1 až 9, hranice, uložení a návrat ke starší verzi. */
public class OpakovaniMaticeTest {

	private static final class K implements VstupDT {
		final String id;
		final int d;
		final int t;
		final boolean nalezena;

		K(final String id, final int d, final int t, final boolean nalezena) {
			this.id = id;
			this.d = d;
			this.t = t;
			this.nalezena = nalezena;
		}

		@Override
		public String id() {
			return id;
		}

		@Override
		public double lat() {
			return 50;
		}

		@Override
		public double lon() {
			return 14;
		}

		@Override
		public boolean nalezena() {
			return nalezena;
		}

		@Override
		public boolean lab() {
			return false;
		}

		@Override
		public boolean locationless() {
			return false;
		}

		@Override
		public boolean vlastni() {
			return false;
		}

		@Override
		public boolean neaktivni() {
			return false;
		}

		@Override
		public StavLogu logNaGc() {
			return StavLogu.NEZNAMY;
		}

		@Override
		public int obtiznost() {
			return d;
		}

		@Override
		public int teren() {
			return t;
		}
	}

	private static List<VstupDT> nalezy(final int d, final int t, final int kolik) {
		final List<VstupDT> v = new ArrayList<>();
		for (int i = 0; i < kolik; i++) {
			v.add(new K("N" + d + "_" + t + "_" + i, d, t, true));
		}
		return v;
	}

	private static VysledekPolicek spocti(final int n, final List<VstupDT> kese) {
		return VypocetMatice.spocti(PravidlaTypu.proMatici().sOpakovanim(n), 50, 14, kese);
	}

	private static Challenge matice(final int n) {
		return Sablony.maticeDT().sOpakovanim(n);
	}

	@Test
	public void kazdaNasobnostJeRozdelenaNaSplneneCastecneAPrazdnePolicko() {
		for (int n = 1; n <= Challenge.NEJVETSI_OPAKOVANI; n++) {
			final List<VstupDT> kese = new ArrayList<>();
			kese.addAll(nalezy(0, 0, n));
			kese.addAll(nalezy(4, 4, n - 1));
			kese.addAll(nalezy(8, 8, n + 3));
			final VysledekPolicek v = spocti(n, kese);
			final String msg = "N=" + n;
			Assert.assertEquals(msg, n, v.cil());
			Assert.assertEquals(msg, 81 * n, v.celkemNalezu());
			final int castecne = n - 1;
			Assert.assertEquals(msg, n + castecne + n, v.splnenoNalezu());
			Assert.assertEquals(msg, 81 * n - (n + castecne + n), v.chybiNalezu());
			Assert.assertEquals(msg, 2, v.splnenoPolicek());
			Assert.assertEquals(msg, 79, v.pocetChybejicich());
			Assert.assertEquals(msg, n + 3, v.pocetNalezu(VypocetMatice.stitek(8, 8)));
			Assert.assertEquals(msg, 0, v.pocetNalezu(VypocetMatice.stitek(1, 1)));
			Assert.assertEquals(msg, v.celkemNalezu() - v.chybiNalezu(), v.splnenoNalezu());
		}
	}

	@Test
	public void nalezuVicNezCilNeubereJinemuPolickuAHotovePolickoNemaKandidaty() {
		for (int n = 1; n <= 9; n++) {
			final List<VstupDT> kese = new ArrayList<>(nalezy(2, 2, n + 5));
			kese.add(new K("H", 2, 2, false));
			final VysledekPolicek v = spocti(n, kese);
			Assert.assertEquals("N=" + n, n, v.splnenoNalezu());
			Assert.assertEquals("N=" + n, 0, v.pocetKandidatu(VypocetMatice.stitek(2, 2)));
			Assert.assertFalse("N=" + n, v.stitkyChybejicich().contains(VypocetMatice.stitek(2, 2)));
		}
	}

	@Test
	public void nalezuMeneNezCilPolickoNechaChybetAKandidatMaOsmdesatJedenPoradi() {
		final List<VstupDT> kese = new ArrayList<>(nalezy(5, 6, 2));
		kese.add(new K("H", 5, 6, false));
		final VysledekPolicek v = spocti(3, kese);
		final String s = VypocetMatice.stitek(5, 6);
		Assert.assertTrue(v.stitkyChybejicich().contains(s));
		Assert.assertEquals(1, v.pocetKandidatu(s));
		Assert.assertEquals(81, v.celkem());
		Assert.assertEquals("pořadí štítků je po řádcích D, v řádku po T", VypocetMatice.stitek(0, 0), v.stitkyChybejicich().get(0));
		Assert.assertEquals(VypocetMatice.stitek(5, 7), v.stitkyChybejicich().get(5 * 9 + 7));
		Assert.assertEquals(VypocetMatice.stitek(8, 8), v.stitkyChybejicich().get(v.pocetChybejicich() - 1));
	}

	@Test
	public void nalezyMimoRozsahDTSeDoPoctuNepocitaji() {
		final List<VstupDT> kese = new ArrayList<>();
		kese.add(new K("A", -1, 0, true));
		kese.add(new K("B", 0, 9, true));
		kese.add(new K("C", 9, 0, true));
		kese.add(new K("D", Integer.MIN_VALUE, Integer.MAX_VALUE, true));
		final VysledekPolicek v = spocti(2, kese);
		Assert.assertEquals(0, v.splnenoNalezu());
		Assert.assertEquals(81, v.pocetChybejicich());
	}

	@Test
	public void nasobnostMimoRozsahVMaticiPravidelSeOdmitne() {
		for (final int n : new int[] { 0, -1, Integer.MIN_VALUE }) {
			try {
				PravidlaTypu.proMatici().sOpakovanim(n);
				Assert.fail("N=" + n);
			} catch (final IllegalArgumentException e) {
				// očekáváno
			}
		}
	}

	@Test
	public void definiceDovoliJenJedenAzDevetAHraniceSvazanyKonstantou() {
		Assert.assertEquals(9, Challenge.NEJVETSI_OPAKOVANI);
		for (int n = 1; n <= 9; n++) {
			Assert.assertEquals(n, matice(n).opakovani());
		}
		for (final int n : new int[] { 0, -1, 10, 100, Integer.MAX_VALUE, Integer.MIN_VALUE }) {
			try {
				matice(n);
				Assert.fail("N=" + n);
			} catch (final ChallengeChyba e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().contains("1 do 9"));
			}
		}
	}

	@Test
	public void jinyTypNezMaticeJeVzdyJednoAVyssiNasobnostOdmitne() {
		for (final Challenge c : Sablony.vse()) {
			if (c.typ() == Challenge.Typ.DT_MATICE) {
				continue;
			}
			Assert.assertEquals(1, c.opakovani());
			Assert.assertSame("N=1 nic nemění", c.opakovani(), c.sOpakovanim(1).opakovani());
			try {
				c.sOpakovanim(2);
				Assert.fail(c.nazev());
			} catch (final ChallengeChyba e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().contains("matice D/T"));
			}
		}
	}

	@Test
	public void zapisKlicOpakovaniJenProVetsiNezJedna() {
		Assert.assertFalse(matice(1).naMapu().containsKey("opakovani"));
		for (int n = 2; n <= 9; n++) {
			Assert.assertEquals(Integer.toString(n), matice(n).naMapu().get("opakovani"));
		}
		for (final Challenge c : Sablony.vse()) {
			if (c.typ() != Challenge.Typ.DT_MATICE) {
				Assert.assertFalse(c.nazev(), c.naMapu().containsKey("opakovani"));
			}
		}
	}

	@Test
	public void kazdaNasobnostProjdeUlozenimAZpetRovnaSe() {
		for (int n = 1; n <= 9; n++) {
			final Challenge c = matice(n);
			final Challenge zpet = Challenge.zMapy(c.naMapu());
			Assert.assertEquals("N=" + n, c, zpet);
			Assert.assertEquals("N=" + n, c.hashCode(), zpet.hashCode());
			Assert.assertEquals("N=" + n, n, zpet.opakovani());
		}
		Assert.assertNotEquals("násobnost je součástí rovnosti", matice(2), matice(3));
		Assert.assertEquals("přejmenování nesmí násobnost ztratit", 4, matice(4).sNazvem("Jiná").opakovani());
	}

	@Test
	public void chybnaUlozenaHodnotaJeJednaADefiniceZustaneBezZmeny() {
		final String[] spatne = { "0", "10", "-3", "99999999999", "moc", "2,5", "3.0", "1e1", "\u0000" };
		for (final String h : spatne) {
			final Map<String, String> m = new HashMap<>(matice(5).naMapu());
			m.put("opakovani", h);
			final Challenge c = Challenge.zMapy(m);
			Assert.assertEquals("hodnota „" + h + "“", 1, c.opakovani());
			Assert.assertEquals(matice(1), c);
		}
		for (final String h : new String[] { "", " ", null }) {
			final Map<String, String> m = new HashMap<>(matice(5).naMapu());
			m.put("opakovani", h);
			Assert.assertEquals("prázdná hodnota", 1, Challenge.zMapy(m).opakovani());
		}
		final Map<String, String> m = new HashMap<>(matice(5).naMapu());
		m.put("opakovani", " 7 ");
		Assert.assertEquals("okolní mezery se tolerují", 7, Challenge.zMapy(m).opakovani());
	}

	@Test
	public void uloziStaryProgramBezKliceDostaneStejnouDefiniciSJednou() {
		final Map<String, String> m = new HashMap<>(matice(6).naMapu());
		m.remove("opakovani");
		final Challenge c = Challenge.zMapy(m);
		Assert.assertEquals(1, c.opakovani());
		Assert.assertEquals(Sablony.maticeDT(), c);
	}

	@Test
	public void klicNaJinemTypuNezMaticeSeIgnoruje() {
		final Map<String, String> m = new HashMap<>(Sablony.cesko360().naMapu());
		m.put("opakovani", "5");
		final Challenge c = Challenge.zMapy(m);
		Assert.assertEquals(1, c.opakovani());
		Assert.assertEquals(Sablony.cesko360(), c);
	}

	@Test
	public void seznamUlozenyAnacitanyZachovaNasobnostKazdeDefinice() {
		final NastaveniChallengeTest.MapoveUloziste u = new NastaveniChallengeTest.MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(matice(9).sNazvem("Devět"));
		s.ulozVlastni(matice(2).sNazvem("Dvě"));
		NastaveniChallenge.uloz(s, false, u);
		final ChallengeSeznam zpet = NastaveniChallenge.nacti(u).seznam;
		Assert.assertEquals(9, zpet.najdi("Devět").opakovani());
		Assert.assertEquals(2, zpet.najdi("Dvě").opakovani());
		NastaveniChallenge.uloz(zpet, false, u);
		Assert.assertEquals("druhé uložení nic nemění", 9, NastaveniChallenge.nacti(u).seznam.najdi("Devět").opakovani());
	}

	@Test
	public void souhrnAProcentaPoctouNalezyAProPrehledSedi() {
		final List<VstupDT> kese = new ArrayList<>(nalezy(0, 0, 3));
		kese.addAll(nalezy(1, 1, 1));
		final VysledekPolicek v = spocti(3, kese);
		Assert.assertEquals("Splněno 4 z 81 × 3 (2 %), chybí 239", TextyPopupuPlanovace.souhrn(v));
		Assert.assertEquals("2 %", TextyPopupuPlanovace.procenta(v));
		final VysledekPolicek jedna = spocti(1, kese);
		Assert.assertEquals("Splněno 2 z 81 (2 %), chybí 79", TextyPopupuPlanovace.souhrn(jedna));
	}

	@Test
	public void nasobnostNaPolickuVyplnenemNaDevetNeniSplnenaDokudNeniDevetNalezu() {
		final VysledekPolicek osm = spocti(9, nalezy(3, 3, 8));
		Assert.assertEquals(81, osm.pocetChybejicich());
		Assert.assertEquals(8, osm.splnenoNalezu());
		final VysledekPolicek devet = spocti(9, nalezy(3, 3, 9));
		Assert.assertEquals(80, devet.pocetChybejicich());
		Assert.assertEquals(9, devet.splnenoNalezu());
	}

	@Test
	public void petsetTisicKesiSeSpocitaRychleACelkoveCislaSedi() {
		final int pocet = 500_000;
		final List<VstupDT> kese = new ArrayList<>(pocet);
		final int[] cekano = new int[81];
		for (int i = 0; i < pocet; i++) {
			final int d = i % 9;
			final int t = (i / 9) % 9;
			kese.add(new K("GC" + i, d, t, i % 3 != 0));
			if (i % 3 != 0) {
				cekano[d * 9 + t]++;
			}
		}
		spocti(9, kese);
		final long od = System.nanoTime();
		final VysledekPolicek v = spocti(9, kese);
		final long ms = (System.nanoTime() - od) / 1_000_000;
		int splneno = 0;
		for (int i = 0; i < 81; i++) {
			Assert.assertEquals(cekano[i], v.pocetNalezu(VypocetMatice.stitek(i / 9, i % 9)));
			splneno += Math.min(9, cekano[i]);
		}
		Assert.assertEquals(splneno, v.splnenoNalezu());
		Assert.assertTrue("500 000 keší trvalo " + ms + " ms", ms < 10_000);
	}

	@Test
	public void bezPoctuNalezuSeSplnenePolickaNasobiCilem() {
		final List<String> chybi = new ArrayList<>();
		chybi.add("D1/T1");
		final VysledekPolicek v = new VysledekPolicek(81, chybi, new ArrayList<KandidatPolicka>(), 50, 3, null);
		Assert.assertEquals(80 * 3, v.splnenoNalezu());
		Assert.assertEquals(81 * 3, v.celkemNalezu());
		Assert.assertEquals(3, v.chybiNalezu());
		Assert.assertEquals(-1, v.pocetNalezu("D1/T1"));
	}
}
