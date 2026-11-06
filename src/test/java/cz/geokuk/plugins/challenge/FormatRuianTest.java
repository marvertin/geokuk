package cz.geokuk.plugins.challenge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class FormatRuianTest {

	private static final String HLAVICKA = "# Zdroj dat: test\n# staženo 2026-11-05\n";

	/** Čtverec 1° × 1° s rohem 50° s. š., 14° v. d.; okruh je v setinách tisícin stupně. */
	private static final String CTVEREC = "oblast ctverec Čtverec\nokruh 4\n1400000 5000000\n100000 0\n0 100000\n-100000 0\nkonec\n";

	private static Map<String, OblastPolygony> nacti(final String text) throws IOException {
		return FormatRuian.nacti(new StringReader(text));
	}

	private static void odmitne(final String text, final String cast) {
		try {
			nacti(text);
			Assert.fail("mělo se odmítnout: " + cast);
		} catch (final IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains(cast));
		}
	}

	@Test
	public void precteOblastARozpoznaVnitrekAVerzi() throws Exception {
		final OblastPolygony o = nacti(HLAVICKA + CTVEREC).get("ctverec");
		Assert.assertTrue(o.obsahuje(50.5, 14.5));
		Assert.assertFalse(o.obsahuje(51.5, 14.5));
		Assert.assertFalse(o.obsahuje(50.5, 15.5));
		Assert.assertTrue(o.verze(), o.verze().startsWith("ruian-2026-11-05"));
	}

	@Test
	public void viceOblastiARuzneOkruhy() throws Exception {
		final String druha = "oblast dva Dva\nokruh 3\n1600000 5000000\n100000 0\n0 100000\nokruh 3\n1800000 5000000\n100000 0\n0 100000\nkonec\n";
		final Map<String, OblastPolygony> m = nacti(HLAVICKA + CTVEREC + druha);
		Assert.assertEquals(2, m.size());
		Assert.assertEquals(2, m.get("dva").casti().size());
	}

	@Test
	public void zmenaDatMeniVerziOblasti() throws Exception {
		final String stary = nacti(HLAVICKA + CTVEREC).get("ctverec").verze();
		Assert.assertNotEquals(stary, nacti("# staženo 2026-12-01\n" + CTVEREC).get("ctverec").verze());
		final String jinyTvar = CTVEREC.replace("100000 0\n0 100000", "200000 0\n0 100000");
		Assert.assertNotEquals(stary, nacti(HLAVICKA + jinyTvar).get("ctverec").verze());
	}

	@Test
	public void chybnyVstupSeOdmitne() {
		odmitne(CTVEREC, "verzi");
		odmitne(HLAVICKA + CTVEREC.replace("konec\n", ""), "Chybí konec");
		odmitne(HLAVICKA + CTVEREC.replace("okruh 4", "okruh 5"), "");
		odmitne(HLAVICKA + CTVEREC.replace("okruh 4", "okruh 2"), "");
		odmitne(HLAVICKA + CTVEREC.replace("okruh 4", "okruh 999999"), "mimo rozsah");
		odmitne(HLAVICKA + CTVEREC.replace("1400000 5000000", "1400000 abc"), "číslo");
		odmitne(HLAVICKA + CTVEREC.replace("1400000 5000000", "99999999 5000000"), "mimo rozsah");
		odmitne(HLAVICKA + CTVEREC.replace("0 100000\n-100000 0", "0 100000 7\n-100000 0"), "dvě čísla");
		odmitne(HLAVICKA + CTVEREC + CTVEREC, "Opakovaná");
		odmitne(HLAVICKA + CTVEREC.replace("oblast ctverec", "oblast Ctverec Velké"), "hlavička");
		odmitne(HLAVICKA + "okruh 3\n0 0\n1 1\n1 -1\nkonec\n", "mimo oblast");
		odmitne(HLAVICKA + "oblast prazdna P\nkonec\n", "bez okruhu");
		odmitne(HLAVICKA + "nesmysl\n", "Neznámý řádek");
	}

	@Test
	public void vnorenyOkruhSeOdmitne() {
		odmitne(HLAVICKA + "oblast vn V\nokruh 4\n1400000 5000000\n100000 0\n0 100000\n-100000 0\nokruh 4\n1430000 5030000\n10000 0\n0 10000\n-10000 0\nkonec\n", "vnořený");
	}

	@Test
	public void prilisDlouhyRadekASouborSeOdmitne() {
		final StringBuilder dlouhy = new StringBuilder(HLAVICKA + "oblast x X\nokruh 3\n");
		for (int i = 0; i < 100; i++) {
			dlouhy.append("1 ");
		}
		odmitne(dlouhy.toString(), "dlouhý");
	}

	@Test
	public void prilisVelkySouborSeOdmitne() {
		final Reader nekonecny = new Reader() {
			@Override
			public int read(final char[] buf, final int off, final int len) {
				for (int i = 0; i < len; i++) {
					buf[off + i] = i % 8 == 7 ? '\n' : '#';
				}
				return len;
			}

			@Override
			public void close() {}
		};
		try {
			FormatRuian.nacti(nekonecny);
			Assert.fail();
		} catch (final IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("velký"));
		}
	}

	// ---- Zabudované hranice

	private static Oblast oblast(final String id) {
		final Optional<Oblast> o = new ZdrojovyPoskytovatelOblasti().najdi(id);
		Assert.assertTrue("chybí oblast " + id, o.isPresent());
		return o.get();
	}

	@Test
	public void zabudovanaOblastCeskoAPrahaSeNactou() {
		final Oblast cr = oblast(Sablony.STAT_CESKO);
		final Oblast praha = oblast(Sablony.PRAHA);
		Assert.assertTrue(cr.obsahuje(49.8, 15.5));
		Assert.assertFalse(cr.obsahuje(48.2, 16.4)); // Vídeň
		Assert.assertTrue(praha.obsahuje(50.0875, 14.4208));
		Assert.assertTrue("Říčany", praha.obsahuje(49.99, 14.66));
		Assert.assertTrue("Černošice", praha.obsahuje(49.96, 14.32));
		Assert.assertFalse("Kolín", praha.obsahuje(50.03, 15.2));
		Assert.assertTrue(cr.verze(), cr.verze().startsWith("ruian-"));
		Assert.assertNotEquals(cr.verze(), praha.verze());
	}

	@Test
	public void sablonyMajiOblastiVeZdrojich() {
		final ZdrojovyPoskytovatelOblasti p = new ZdrojovyPoskytovatelOblasti();
		for (final Challenge c : Sablony.vse()) {
			for (final String id : c.oblast().idOblasti()) {
				Assert.assertTrue("oblast " + id + " šablony " + c.nazev(), p.najdi(id).isPresent());
			}
		}
	}

	@Test
	public void testovaciVektoryDlouhoOdObrysu() throws Exception {
		final List<String> chyby = new ArrayList<>();
		int zkouseno = 0;
		try (InputStream in = FormatRuianTest.class.getResourceAsStream("ruian-vektory.txt");
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String radek = r.readLine(); radek != null; radek = r.readLine()) {
				if (radek.isEmpty() || radek.startsWith("#")) {
					continue;
				}
				final String[] c = radek.trim().split("\\s+");
				final String druh = c[1];
				final boolean ocekavano = "1".equals(c[5]);
				if (!"uvnitr".equals(druh) && !"vne".equals(druh)) {
					continue; // u okraje závisí na toleranci zjednodušení
				}
				final String id = "cr".equals(c[0]) ? Sablony.STAT_CESKO : Sablony.PRAHA;
				final boolean je = oblast(id).obsahuje(Double.parseDouble(c[3]), Double.parseDouble(c[2]));
				zkouseno++;
				if (je != ocekavano) {
					chyby.add(radek);
				}
			}
		}
		Assert.assertTrue("málo vektorů: " + zkouseno, zkouseno >= 30);
		Assert.assertTrue(chyby.toString(), chyby.isEmpty());
	}

	@Test
	public void okrajovePrimerySeShodujiSZjednodusenymVysledkemAspoMajoritne() throws Exception {
		int shoda = 0;
		int celkem = 0;
		try (InputStream in = FormatRuianTest.class.getResourceAsStream("ruian-vektory.txt");
				BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			for (String radek = r.readLine(); radek != null; radek = r.readLine()) {
				if (radek.isEmpty() || radek.startsWith("#")) {
					continue;
				}
				final String[] c = radek.trim().split("\\s+");
				if (!"okraj".equals(c[1])) {
					continue;
				}
				final String id = "cr".equals(c[0]) ? Sablony.STAT_CESKO : Sablony.PRAHA;
				celkem++;
				if (oblast(id).obsahuje(Double.parseDouble(c[3]), Double.parseDouble(c[2])) == "1".equals(c[5])) {
					shoda++;
				}
			}
		}
		Assert.assertTrue(celkem > 0);
		Assert.assertTrue(shoda + " z " + celkem, shoda * 10 >= celkem * 9);
	}

	// ---- Hranice limitů

	private static String ctverecSNazvem(final String kod) {
		return "oblast " + kod + " X\nokruh 3\n1400000 5000000\n100000 0\n0 100000\nkonec\n";
	}

	private static String opakuj(final char znak, final int delka) {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < delka; i++) {
			b.append(znak);
		}
		return b.toString();
	}

	private static String mezera(final int delka) {
		return opakuj(' ', delka);
	}

	@Test
	public void souborPresneNaLimituVelikostiSeNacteJedenZnakNavicNe() throws Exception {
		final String zaklad = HLAVICKA + CTVEREC;
		final StringBuilder b = new StringBuilder(zaklad);
		// Do limitu se počítají řádky mimo body okruhů; komentář může být delší než řádek dat.
		final int bodyOkruhu = "1400000 5000000\n100000 0\n0 100000\n-100000 0\n".length();
		final int zbyva = FormatRuian.NEJVIC_ZNAKU - (zaklad.length() - bodyOkruhu) - 1;
		b.append('#');
		for (int i = 1; i < zbyva; i++) {
			b.append('x');
		}
		b.append('\n');
		Assert.assertEquals(FormatRuian.NEJVIC_ZNAKU + bodyOkruhu, b.length());
		Assert.assertEquals(1, nacti(b.toString()).size());
		odmitne(b.insert(zaklad.length() + 1, 'x').toString(), "velký");
	}

	@Test
	public void radekSOsmdesatiZnakyProjdeSedmdesatDevetNeAOsmdesatJedenNe() throws Exception {
		final String hlavicka = "oblast kod ";
		final String okruh = "okruh 3\n1400000 5000000\n100000 0\n0 100000\nkonec\n";
		Assert.assertEquals(1, nacti(HLAVICKA + hlavicka + opakuj('n', 80 - hlavicka.length()) + "\n" + okruh).size());
		odmitne(HLAVICKA + hlavicka + opakuj('n', 81 - hlavicka.length()) + "\n" + okruh, "dlouhý");
	}

	@Test
	public void chybaUkazujeCisloRadkuVcetneBoduOkruhu() {
		// 1 hlavička, 2 datum, 3 oblast, 4 okruh, 5 až 7 body, 8 neznámý řádek
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n1400000 5000000\n100000 0\n0 100000\nnesmysl\n", "Řádek 8:");
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n1400000 5000000\n100000 0\n0 100000\nokruh x\n", "Neplatné číslo na řádku 8");
	}

	@Test
	public void pocetOblastiJeOmezenNaCtyriaSedesat() throws Exception {
		final StringBuilder b = new StringBuilder(HLAVICKA);
		for (int i = 0; i < FormatRuian.NEJVIC_OBLASTI; i++) {
			b.append(ctverecSNazvem("o" + i));
		}
		Assert.assertEquals(FormatRuian.NEJVIC_OBLASTI, nacti(b.toString()).size());
		b.append(ctverecSNazvem("navic"));
		odmitne(b.toString(), "příliš mnoho");
	}

	@Test
	public void pocetOkruhuVOblastiJeOmezenNaDveStaPadesatSest() throws Exception {
		final StringBuilder b = new StringBuilder(HLAVICKA + "oblast a A\n");
		for (int i = 0; i < FormatRuian.NEJVIC_OKRUHU; i++) {
			b.append("okruh 3\n").append(1000000 + i * 1000).append(" 5000000\n100 0\n0 100\n");
		}
		b.append("konec\n");
		Assert.assertEquals(FormatRuian.NEJVIC_OKRUHU, nacti(b.toString()).get("a").casti().size());
		b.insert(b.length() - "konec\n".length(), "okruh 3\n1900000 5000000\n100 0\n0 100\n");
		odmitne(b.toString(), "příliš mnoho okruhů");
	}

	@Test
	public void pocetBoduOkruhuMaDolniAHorniMez() throws Exception {
		odmitne(HLAVICKA + "oblast a A\nokruh 2\n1 1\n1 1\nkonec\n", "mimo rozsah");
		final StringBuilder b = new StringBuilder(HLAVICKA + "oblast a A\nokruh " + FormatRuian.NEJVIC_BODU_OKRUHU + "\n1400000 5000000\n");
		for (int i = 1; i < FormatRuian.NEJVIC_BODU_OKRUHU; i++) {
			b.append(i % 2 == 0 ? "1 1\n" : "-1 -1\n");
		}
		b.append("konec\n");
		Assert.assertEquals(1, nacti(b.toString()).size());
		odmitne(HLAVICKA + "oblast a A\nokruh " + (FormatRuian.NEJVIC_BODU_OKRUHU + 1) + "\n", "mimo rozsah");
	}

	@Test
	public void souradniceNaMeziRozsahuSeNactouOPoJednotkuVicNe() throws Exception {
		final String ok = HLAVICKA + "oblast a A\nokruh 3\n18000000 9000000\n-18000000 -9000000\n9000000 9000000\nkonec\n";
		Assert.assertEquals(1, nacti(ok).size());
		odmitne(ok.replace("18000000 9000000", "18000001 9000000"), "mimo rozsah souřadnic");
		odmitne(ok.replace("18000000 9000000", "18000000 9000001"), "mimo rozsah souřadnic");
		odmitne(ok.replace("-18000000 -9000000", "-18000000 -9000000").replace("18000000 9000000", "-18000001 9000000"), "mimo rozsah souřadnic");
	}

	@Test
	public void bodOkruhuSOsmdesatiZnakyProjdeOsmdesatJedenNe() throws Exception {
		final String zacatek = HLAVICKA + "oblast a A\nokruh 3\n";
		final String konec = "\n100000 0\n0 100000\nkonec\n";
		final String bod = "1400000 5000000";
		Assert.assertEquals(1, nacti(zacatek + bod + mezera(80 - bod.length()) + konec).size());
		odmitne(zacatek + bod + mezera(81 - bod.length()) + konec, "dlouhý");
	}
}
