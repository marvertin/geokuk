package cz.geokuk.plugins.mapy.kachle.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.core.coord.PrvniNacitaniMapy;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.data.Ka;
import cz.geokuk.plugins.mapy.kachle.data.KaLoc;
import cz.geokuk.plugins.mapy.kachle.podklady.KachloStav;
import cz.geokuk.plugins.mapy.kachle.podklady.Priority;

public class LogoNacitaniTest {

	@Before
	public void obnovLogo() {
		PrvniNacitaniMapy.obnovProTesty();
	}

	private static final int SIRKA = 800;
	private static final int VYSKA = 600;

	private static class Kachlovnik extends JKachlovnik {
		private static final long serialVersionUID = 1L;
		private final boolean logo;

		Kachlovnik(final boolean logo) {
			super("test", Priority.KACHLE);
			this.logo = logo;
			setSize(SIRKA, VYSKA);
		}

		@Override
		protected boolean kreslitLogoNacitani() {
			return logo;
		}
	}

	private static JKachle dlazdice() {
		final KaLoc loc = KaLoc.ofJZ(new Mou(0x23800000, 0x28100000), 13);
		return new JKachle(null, new Ka(loc, EKaType.OFFLINE_MF));
	}

	private static Kachlovnik kachlovnikSDlazdici(final boolean logo, final JKachle dlazdice) {
		final Kachlovnik k = new Kachlovnik(logo);
		dlazdice.setLocation(272, 172);
		k.add(dlazdice);
		return k;
	}

	private static BufferedImage vykresliPozadi(final Kachlovnik k) {
		final BufferedImage img = new BufferedImage(SIRKA, VYSKA, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();
		try {
			k.paintComponent(g);
		} finally {
			g.dispose();
		}
		return img;
	}

	private static boolean maBarevnyPixel(final BufferedImage img) {
		for (int y = 172; y < 428; y++) {
			for (int x = 272; x < 528; x++) {
				final int p = img.getRGB(x, y);
				if (p >>> 24 != 0 && (p & 0xFFFFFF) != 0xFFFFFF) {
					return true;
				}
			}
		}
		return false;
	}

	@Test
	public void cekajiciDlazdiceNekresliNicSama() {
		final JKachle d = dlazdice();
		d.setSize(256, 256);
		final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();
		try {
			d.paintComponent(g);
		} finally {
			g.dispose();
		}
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++) {
				Assert.assertEquals(new Color(0, 0, 0, 0).getRGB(), img.getRGB(x, y));
			}
		}
	}

	@Test
	public void dlazdiceCekaDokudNemaObrazekNeboChybu() {
		final JKachle d = dlazdice();
		Assert.assertTrue(d.cekaNaObrazek());
		d.prijmi(new KachloStav(new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)), Priority.KACHLE);
		Assert.assertFalse(d.cekaNaObrazek());
		final JKachle chybna = dlazdice();
		chybna.prijmi(new KachloStav(new RuntimeException("chyba")), Priority.KACHLE);
		Assert.assertFalse(chybna.cekaNaObrazek());
	}

	@Test
	public void logoSeKresliJenPriCekani() {
		final JKachle d = dlazdice();
		final Kachlovnik k = kachlovnikSDlazdici(true, d);
		Assert.assertTrue(maBarevnyPixel(vykresliPozadi(k)));
		d.prijmi(new KachloStav(new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)), Priority.KACHLE);
		Assert.assertFalse(maBarevnyPixel(vykresliPozadi(k)));
	}

	@Test
	public void logoSeNekresliPriChybe() {
		final JKachle d = dlazdice();
		final Kachlovnik k = kachlovnikSDlazdici(true, d);
		d.prijmi(new KachloStav(new RuntimeException("chyba")), Priority.KACHLE);
		Assert.assertFalse(maBarevnyPixel(vykresliPozadi(k)));
	}

	@Test
	public void logoSeNekresliVKachlovnikuBezLoga() {
		final Kachlovnik k = kachlovnikSDlazdici(false, dlazdice());
		Assert.assertFalse(maBarevnyPixel(vykresliPozadi(k)));
	}

	@Test
	public void velikostLogaSeridiOknem() {
		Assert.assertEquals(240, LogoNacitani.velikost(800, 600, 1));
		Assert.assertEquals(480, LogoNacitani.velikost(800, 600, 2));
		Assert.assertEquals(0, LogoNacitani.velikost(100, 100, 1));
	}

	private static void vymenDlazdici(final Kachlovnik k, final JKachle stara, final JKachle nova) {
		k.remove(stara);
		nova.setLocation(272, 172);
		k.add(nova);
	}

	private static void nacti(final JKachle d) {
		d.prijmi(new KachloStav(new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)), Priority.KACHLE);
	}

	@Test
	public void logoSeNekresliPoPrvnimUplnemNacteniVyrezu() {
		final JKachle d = dlazdice();
		final Kachlovnik k = kachlovnikSDlazdici(true, d);
		Assert.assertTrue("před načtením ano", maBarevnyPixel(vykresliPozadi(k)));
		nacti(d);
		Assert.assertFalse(maBarevnyPixel(vykresliPozadi(k)));
		final JKachle dalsi = dlazdice();
		vymenDlazdici(k, d, dalsi);
		Assert.assertFalse("při dalším čekání na dlaždice už ne", maBarevnyPixel(vykresliPozadi(k)));
	}

	@Test
	public void logoSeNekresliPoPrvniChybeDlazdice() {
		final JKachle d = dlazdice();
		final Kachlovnik k = kachlovnikSDlazdici(true, d);
		d.prijmi(new KachloStav(new RuntimeException("chyba")), Priority.KACHLE);
		vykresliPozadi(k);
		final JKachle dalsi = dlazdice();
		vymenDlazdici(k, d, dalsi);
		Assert.assertFalse(maBarevnyPixel(vykresliPozadi(k)));
	}

	@Test
	public void logoSeNekresliPoLimitu() {
		final long[] cas = { 5_000 };
		PrvniNacitaniMapy.nastavHodiny(() -> cas[0]);
		final Kachlovnik k = kachlovnikSDlazdici(true, dlazdice());
		Assert.assertTrue(maBarevnyPixel(vykresliPozadi(k)));
		cas[0] = PrvniNacitaniMapy.LIMIT_MS;
		Assert.assertTrue("přesně na limitu ještě ano", maBarevnyPixel(vykresliPozadi(k)));
		cas[0] = PrvniNacitaniMapy.LIMIT_MS + 1;
		Assert.assertFalse(maBarevnyPixel(vykresliPozadi(k)));
	}

	@Test
	public void prazdnyKachlovnikLogoNeukonci() {
		final Kachlovnik prazdny = new Kachlovnik(true);
		vykresliPozadi(prazdny);
		Assert.assertTrue(PrvniNacitaniMapy.probiha());
	}

	private static List<JKachle> dlazdice(final Kachlovnik k, final int pocet) {
		final List<JKachle> d = new ArrayList<>();
		for (int i = 0; i < pocet; i++) {
			final JKachle j = dlazdice();
			j.setLocation(i * 256, 0);
			k.add(j);
			d.add(j);
		}
		return d;
	}

	@Test
	public void vrstvyNadMapouSeZobraziPoCtyricetiProcentechDlazdicALogoTrvaDoUplnehoNacteni() {
		final Kachlovnik k = new Kachlovnik(true);
		final List<JKachle> d = dlazdice(k, 5);
		vykresliPozadi(k);
		Assert.assertTrue("před načtením vrstvy čekají", PrvniNacitaniMapy.vrstvyCekaji());
		nacti(d.get(0));
		vykresliPozadi(k);
		Assert.assertTrue("20 % ještě ne", PrvniNacitaniMapy.vrstvyCekaji());
		nacti(d.get(1));
		vykresliPozadi(k);
		Assert.assertFalse("40 % ano", PrvniNacitaniMapy.vrstvyCekaji());
		Assert.assertTrue("logo trvá do úplného načtení", PrvniNacitaniMapy.probiha());
		for (final JKachle j : d.subList(2, 5)) {
			nacti(j);
		}
		vykresliPozadi(k);
		Assert.assertFalse(PrvniNacitaniMapy.probiha());
	}

	@Test
	public void poPrvnimDosazeniPrahuSeVrstvyUzNeskryji() {
		final Kachlovnik k = new Kachlovnik(true);
		final List<JKachle> d = dlazdice(k, 5);
		nacti(d.get(0));
		nacti(d.get(1));
		vykresliPozadi(k);
		Assert.assertFalse(PrvniNacitaniMapy.vrstvyCekaji());
		// Posun do nové oblasti: všechny dlaždice jsou nové a čekají.
		for (final JKachle j : d) {
			k.remove(j);
		}
		dlazdice(k, 5);
		vykresliPozadi(k);
		Assert.assertFalse(PrvniNacitaniMapy.vrstvyCekaji());
	}

	@Test
	public void chybnaDlazdiceUkonciCekaniVrstev() {
		final Kachlovnik k = new Kachlovnik(true);
		final List<JKachle> d = dlazdice(k, 5);
		d.get(0).prijmi(new KachloStav(new RuntimeException("chyba")), Priority.KACHLE);
		vykresliPozadi(k);
		Assert.assertFalse(PrvniNacitaniMapy.vrstvyCekaji());
		Assert.assertFalse(PrvniNacitaniMapy.probiha());
	}

	@Test
	public void chybnaDlazdiceSePoPrahuNepocitaZaHotovou() {
		Assert.assertFalse(PrvniNacitaniMapy.jeDostNactenych(0, 5));
		final JKachle d = dlazdice();
		d.prijmi(new KachloStav(new RuntimeException("chyba")), Priority.KACHLE);
		Assert.assertFalse(d.maHotovyObrazek());
		final JKachle ok = dlazdice();
		nacti(ok);
		Assert.assertTrue(ok.maHotovyObrazek());
	}

	@Test
	public void vrstvyNadMapouSeKresliPoLimitu() {
		final long[] cas = { 5_000 };
		PrvniNacitaniMapy.nastavHodiny(() -> cas[0]);
		Assert.assertTrue(PrvniNacitaniMapy.vrstvyCekaji());
		cas[0] = PrvniNacitaniMapy.LIMIT_MS + 1;
		Assert.assertFalse(PrvniNacitaniMapy.vrstvyCekaji());
	}

	@Test
	public void krytiJeProSvetlouATmavouPlochuRozdilne() {
		Assert.assertEquals(0.13f, LogoNacitani.KRYTI_SVETLE, 0f);
		Assert.assertEquals(0.32f, LogoNacitani.KRYTI_TMAVE, 0f);
		Assert.assertEquals(LogoNacitani.KRYTI_SVETLE, LogoNacitani.kryti(false), 0f);
		Assert.assertEquals(LogoNacitani.KRYTI_TMAVE, LogoNacitani.kryti(true), 0f);
	}

	/** Průměrná odchylka barvy ve středu od pozadí; míra toho, jak je logo vidět. */
	private static double odchylka(final BufferedImage img, final Color pozadi) {
		long soucet = 0;
		int pocet = 0;
		for (int y = 172; y < 428; y++) {
			for (int x = 272; x < 528; x++) {
				final int p = img.getRGB(x, y);
				soucet += Math.abs(((p >> 16) & 0xFF) - pozadi.getRed()) + Math.abs(((p >> 8) & 0xFF) - pozadi.getGreen()) + Math.abs((p & 0xFF) - pozadi.getBlue());
				pocet += 3;
			}
		}
		return (double) soucet / pocet;
	}

	private static BufferedImage logoNaPozadi(final Color pozadi, final boolean tmave) {
		final BufferedImage img = new BufferedImage(SIRKA, VYSKA, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = img.createGraphics();
		try {
			g.setColor(pozadi);
			g.fillRect(0, 0, SIRKA, VYSKA);
			new LogoNacitani().kresli(g, SIRKA, VYSKA, null, tmave);
		} finally {
			g.dispose();
		}
		return img;
	}

	@Test
	public void logoNaTmaveJeViditelnejsiSTmavymKrytim() {
		final Color tmava = new Color(0x3C3F41);
		Assert.assertTrue(odchylka(logoNaPozadi(tmava, true), tmava) > odchylka(logoNaPozadi(tmava, false), tmava));
		Assert.assertTrue(odchylka(logoNaPozadi(Color.WHITE, false), Color.WHITE) > 0);
	}

	/** Kachlovník v tmavém vzhledu kreslí logo s tmavým krytím, ve světlém se světlým. */
	@Test
	public void kachlovnikVybiraKrytiPodlePozadiPanelu() {
		final Object puvodni = javax.swing.UIManager.get("Panel.background");
		// Bez vlastní hodnoty se po testu vrátí hodnota vzhledu; vlastní hodnota by přebila i pozdější přepnutí vzhledu v jiných testech.
		final boolean vlastni = puvodni != javax.swing.UIManager.getLookAndFeelDefaults().get("Panel.background");
		try {
			final Color tmava = new Color(0x3C3F41);
			javax.swing.UIManager.put("Panel.background", tmava);
			final Kachlovnik k = kachlovnikSDlazdici(true, dlazdice());
			final BufferedImage nadTmavou = vykresliPozadi(k);
			Assert.assertTrue("na tmavé ploše je logo vidět s tmavým krytím", odchylka(nadTmavou, tmava) > 3.5);
		} finally {
			javax.swing.UIManager.put("Panel.background", vlastni ? puvodni : null);
		}
	}
}
