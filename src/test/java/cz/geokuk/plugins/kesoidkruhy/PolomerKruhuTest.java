package cz.geokuk.plugins.kesoidkruhy;

import org.junit.*;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.MyPreferences;

/** Poloměr jednotkových kruhů v metrech: presety posuvníku, text pole, přepočet na pixely a uložení. */
public class PolomerKruhuTest {

	@Test
	public void presetyJsouSerazeneAObsahujiZadaneHodnoty() {
		Assert.assertArrayEquals(new int[] { 10, 25, 50, 100, 250, 500, 1000, 2000, 5000 }, PolomerKruhu.PRESETY);
		for (int i = 0; i < PolomerKruhu.PRESETY.length; i++) {
			Assert.assertEquals(PolomerKruhu.PRESETY[i], PolomerKruhu.metru(i));
			Assert.assertEquals(i, PolomerKruhu.pozice(PolomerKruhu.PRESETY[i]));
		}
	}

	@Test
	public void hodnotaMimoPresetyDostaneNejblizsiPozici() {
		Assert.assertEquals("161 m je blíž 250 m než 100 m", 4, PolomerKruhu.pozice(161));
		Assert.assertEquals(3, PolomerKruhu.pozice(150));
		Assert.assertEquals(0, PolomerKruhu.pozice(1));
		Assert.assertEquals(8, PolomerKruhu.pozice(50_000));
		Assert.assertEquals(6, PolomerKruhu.pozice(1400));
		Assert.assertEquals("1,5 km je geometricky blíž 2 km", 7, PolomerKruhu.pozice(1500));
	}

	@Test
	public void metruZmimoRozsahPoziceSeOrizne() {
		Assert.assertEquals(10, PolomerKruhu.metru(-3));
		Assert.assertEquals(5000, PolomerKruhu.metru(99));
	}

	@Test
	public void parsovaniPole() {
		Assert.assertEquals(161, PolomerKruhu.parsuj("161"));
		Assert.assertEquals(333, PolomerKruhu.parsuj("333 m"));
		Assert.assertEquals(333, PolomerKruhu.parsuj("333m"));
		Assert.assertEquals(1500, PolomerKruhu.parsuj("1,5 km"));
		Assert.assertEquals(1500, PolomerKruhu.parsuj("1.5km"));
		Assert.assertEquals(1000, PolomerKruhu.parsuj(" 1 KM "));
		Assert.assertEquals(2500, PolomerKruhu.parsuj("2,5 km"));
		Assert.assertEquals(50_000, PolomerKruhu.parsuj("50 km"));
		Assert.assertEquals(50_000, PolomerKruhu.parsuj("50000"));
		Assert.assertEquals(1, PolomerKruhu.parsuj("1"));
		Assert.assertEquals(1, PolomerKruhu.parsuj("0,6 m"));
	}

	@Test
	public void neplatnyVstupVraciNulu() {
		for (final String t : new String[] { null, "", " ", "abc", "m", "km", "0", "0 m", "-5", "1e3", "12 cm", "1,2,3 km", "50,1 km", "50001", "0,4", "1 km 2", "∞" }) {
			Assert.assertEquals("'" + t + "'", 0, PolomerKruhu.parsuj(t));
		}
	}

	@Test
	public void formatovaniPole() {
		Assert.assertEquals("10 m", PolomerKruhu.format(10));
		Assert.assertEquals("161 m", PolomerKruhu.format(161));
		Assert.assertEquals("999 m", PolomerKruhu.format(999));
		Assert.assertEquals("1 km", PolomerKruhu.format(1000));
		Assert.assertEquals("1,5 km", PolomerKruhu.format(1500));
		Assert.assertEquals("1,234 km", PolomerKruhu.format(1234));
		Assert.assertEquals("50 km", PolomerKruhu.format(50_000));
	}

	@Test
	public void textSeDaVratitZpet() {
		for (final int m : new int[] { 1, 10, 161, 999, 1000, 1001, 1234, 1500, 12_345, 50_000 }) {
			Assert.assertEquals(m, PolomerKruhu.parsuj(PolomerKruhu.format(m)));
		}
	}

	@Test
	public void pixlyJsouUmernePribliZeniBezSkoku() {
		double predchozi = PolomerKruhu.pixlu(100, 0.05);
		for (double p = 0.05; p < 1; p *= 1.004) {
			final double r = PolomerKruhu.pixlu(100, p);
			Assert.assertTrue("skok z " + predchozi + " na " + r, Math.abs(r - predchozi) <= 1);
			predchozi = r;
		}
		Assert.assertEquals(50, PolomerKruhu.pixlu(100, 0.5), 1e-9);
		Assert.assertEquals(51, PolomerKruhu.pixlu(100, 0.51), 1e-9);
		Assert.assertEquals("žádné skoky po mocninách 10", 37, PolomerKruhu.pixlu(100, 0.37), 1e-9);
		Assert.assertEquals(370, PolomerKruhu.pixlu(1000, 0.37), 1e-9);
	}

	@Test
	public void prilisMalyKruhSeNekresliVelkyAno() {
		Assert.assertEquals(0, PolomerKruhu.pixlu(100, 0.02), 0);
		Assert.assertEquals(3, PolomerKruhu.pixlu(100, 0.03), 1e-9);
		Assert.assertEquals(0, PolomerKruhu.pixlu(1, 2.9), 0);
		Assert.assertEquals("bez horní hranice", 500_000, PolomerKruhu.pixlu(50_000, 10), 1e-6);
	}

	@Test
	public void starseNastaveniBezPoleMaStometru() throws Exception {
		final MyPreferences pref = MyPreferences.current().node("test-kruhy-polomer-ulozeni");
		try {
			final KruhySettings s = new KruhySettings();
			s.setVelikost(33);
			s.setJednotkovaVelikost(true);
			s.setOnoff(true);
			s.setPolomerMetru(777);
			pref.putStructure("OPEN_STREET", s);
			Assert.assertEquals(777, pref.getStructure("OPEN_STREET", new KruhySettings()).getPolomerMetru());
			pref.node("OPEN_STREET").remove("polomerMetru");
			final KruhySettings nacteno = pref.getStructure("OPEN_STREET", new KruhySettings());
			Assert.assertEquals(100, nacteno.getPolomerMetru());
			Assert.assertEquals(33, nacteno.getVelikost());
			Assert.assertTrue(nacteno.isJednotkovaVelikost());
			Assert.assertEquals(FPref.ZVYRAZNOVACI_KRUHY_node, "zvyraznovaciKruhy");
		} finally {
			pref.removeNode();
		}
	}

	@Test
	public void pixelyZPole() {
		Assert.assertEquals(0, PolomerKruhu.parsujPixely("0"));
		Assert.assertEquals(40, PolomerKruhu.parsujPixely("40"));
		Assert.assertEquals(40, PolomerKruhu.parsujPixely(" 40 px "));
		Assert.assertEquals(40, PolomerKruhu.parsujPixely("40PX"));
		Assert.assertEquals(100, PolomerKruhu.parsujPixely("100"));
		for (final String t : new String[] { null, "", "abc", "-1", "101", "4,5", "40 m", "px", "1e2", "00000" }) {
			Assert.assertEquals("'" + t + "'", -1, PolomerKruhu.parsujPixely(t));
		}
		Assert.assertEquals("20 px", PolomerKruhu.formatPixely(20));
	}
}
