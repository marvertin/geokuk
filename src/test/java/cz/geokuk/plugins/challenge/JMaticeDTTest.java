package cz.geokuk.plugins.challenge;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.lookandfeel.CharcoalTheme;

/** Klikací matice D/T: stav políček, „50+“, výběr myší a klávesnicí, čitelnost ve skinech. */
public class JMaticeDTTest {

	/** D1/T1 chybí s 3 kandidáty, D3/T4 s 50 (plné), D5/T5 bez kandidátů, ostatní splněno. */
	static VysledekPolicek vysledek() {
		final List<KandidatPolicka> k = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			k.add(new KandidatPolicka("A" + i, 50, 14, "D1/T1", "", null));
		}
		for (int i = 0; i < 50; i++) {
			k.add(new KandidatPolicka("B" + i, 50, 14, "D3/T4", "", null));
		}
		return new VysledekPolicek(81, Arrays.asList("D1/T1", "D3/T4", "D5/T5"), k, 50);
	}

	private final List<String> vybrana = new ArrayList<>();

	/** S násobností 3 ukazuje políčko nálezy z cíle a tooltip i počet nálezů; splněné je až při 3 nálezech. */
	@Test
	public void nasobnostUkazujeNalezyZCile() {
		final int[] nalezu = new int[81];
		nalezu[VypocetMatice.indexStitku("D1/T1")] = 1;
		nalezu[VypocetMatice.indexStitku("D2/T1")] = 4;
		final List<String> chybi = new ArrayList<>();
		for (int i = 0; i < 81; i++) {
			if (nalezu[i] < 3) {
				chybi.add(VypocetMatice.stitek(i / 9, i % 9));
			}
		}
		final List<KandidatPolicka> k = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			k.add(new KandidatPolicka("A" + i, 50, 14, "D1/T1", "", null));
		}
		final JMaticeDT m = new JMaticeDT();
		m.nastav(new VysledekPolicek(81, chybi, k, 50, 3, nalezu));
		Assert.assertEquals("1/3", m.nalezyZCile("D1/T1"));
		Assert.assertEquals(JMaticeDT.Stav.S_KANDIDATY, m.stav("D1/T1"));
		Assert.assertEquals("D1/T1 – 1 nález, cíl 3, 5 kandidátů", m.popis("D1/T1"));
		Assert.assertEquals(JMaticeDT.Stav.SPLNENO, m.stav("D2/T1"));
		Assert.assertEquals("skutečný počet je vidět i nad cílem", "D2/T1 – 4 nálezy, cíl 3, splněno", m.popis("D2/T1"));
		Assert.assertEquals(JMaticeDT.Stav.BEZ_KANDIDATU, m.stav("D5/T5"));
		Assert.assertEquals("0/3", m.nalezyZCile("D5/T5"));
		Assert.assertEquals("D5/T5 – 0 nálezů, cíl 3, nemá kandidáty", m.popis("D5/T5"));
		Assert.assertEquals("nálezy nad cílem se omezí na cíl", "3/3", m.nalezyZCile("D2/T1"));
		Assert.assertEquals("s násobností i bez kandidátů nálezy, ne pomlčka", "0/3", m.znak("D5/T5"));
		Assert.assertEquals("1/3", m.znak("D1/T1"));
		Assert.assertEquals("✓", m.znak("D2/T1"));
		Assert.assertTrue("tečka u políčka s kandidáty", m.maTecku("D1/T1"));
		Assert.assertFalse(m.maTecku("D5/T5"));
		Assert.assertFalse(m.maTecku("D2/T1"));
		m.setSize(m.getPreferredSize());
		final java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(m.getWidth(), m.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
		final java.awt.Graphics2D g = img.createGraphics();
		m.paint(g);
		g.dispose();
	}

	/** Bez násobnosti ukazuje políčko počet kandidátů nebo „–“ a nemá tečku. */
	@Test
	public void bezNasobnostiPocetAPomlckaBezTecky() {
		final JMaticeDT m = matice();
		Assert.assertEquals("3", m.znak("D1/T1"));
		Assert.assertEquals("–", m.znak("D5/T5"));
		Assert.assertEquals("✓", m.znak("D2/T2"));
		Assert.assertFalse("N = 1 bez tečky", m.maTecku("D1/T1"));
	}

	@Test
	public void zahlaviPoPulkach() {
		final String[] ocekavane = { "1", "1,5", "2", "2,5", "3", "3,5", "4", "4,5", "5" };
		for (int i = 0; i < ocekavane.length; i++) {
			Assert.assertEquals(ocekavane[i], JMaticeDT.hodnota(i));
		}
	}

	/** Chybějící políčko bez kandidátů má „–“ a nedá se vybrat; plné „50+“. */
	@Test
	public void bezKandidatuPomlckaAPlne50Plus() {
		final JMaticeDT m = matice();
		Assert.assertEquals(JMaticeDT.Stav.BEZ_KANDIDATU, m.stav("D5/T5"));
		Assert.assertEquals("D5/T5 – chybí, nemá kandidáty", m.popis("D5/T5"));
		klikni(m, 8, 8);
		Assert.assertNull("bez kandidátů nejde vybrat", m.vybrane());
		Assert.assertTrue(vybrana.isEmpty());
		Assert.assertEquals("50+", m.pocet("D3/T4"));
		Assert.assertEquals("D3/T4 – chybí, 50+ kandidátů", m.popis("D3/T4"));
	}

	private JMaticeDT matice() {
		final JMaticeDT m = new JMaticeDT();
		m.nastav(vysledek());
		m.setNaVyber(vybrana::add);
		m.setSize(m.getPreferredSize());
		return m;
	}

	private static MouseEvent klik(final JMaticeDT m, final int d, final int t) {
		final Rectangle r = m.obdelnik(d, t);
		return new MouseEvent(m, MouseEvent.MOUSE_PRESSED, 0, 0, r.x + r.width / 2, r.y + r.height / 2, 1, false, MouseEvent.BUTTON1);
	}

	private static void klikni(final JMaticeDT m, final int d, final int t) {
		final MouseEvent e = klik(m, d, t);
		for (final java.awt.event.MouseListener l : m.getMouseListeners()) {
			l.mousePressed(e);
		}
	}

	@Test
	public void stavyPolicekAPocty() {
		final JMaticeDT m = matice();
		Assert.assertEquals(JMaticeDT.Stav.S_KANDIDATY, m.stav("D1/T1"));
		Assert.assertEquals(JMaticeDT.Stav.BEZ_KANDIDATU, m.stav("D5/T5"));
		Assert.assertEquals(JMaticeDT.Stav.SPLNENO, m.stav("D2/T2"));
		Assert.assertEquals("3", m.pocet("D1/T1"));
		Assert.assertEquals("plné políčko", "50+", m.pocet("D3/T4"));
		Assert.assertEquals("D1/T1 – chybí, 3 kandidáti", m.popis("D1/T1"));
		Assert.assertEquals("D3/T4 – chybí, 50+ kandidátů", m.popis("D3/T4"));
		Assert.assertEquals("D5/T5 – chybí, nemá kandidáty", m.popis("D5/T5"));
		Assert.assertEquals("D2/T2 – splněno", m.popis("D2/T2"));
		Assert.assertEquals("tooltip pod myší", "D1/T1 – chybí, 3 kandidáti", m.getToolTipText(klik(m, 0, 0)));
		Assert.assertNull("mimo políčka", m.getToolTipText(new MouseEvent(m, MouseEvent.MOUSE_MOVED, 0, 0, 2, 2, 0, false)));
		Assert.assertEquals("D1,5/T3,5", JMaticeDT.stitek(1, 5));
	}

	@Test
	public void klikVyberePolickoSKandidatyDruhyKlikZrusi() {
		final JMaticeDT m = matice();
		klikni(m, 4, 6);
		Assert.assertEquals("D3/T4", m.vybrane());
		klikni(m, 0, 0);
		Assert.assertEquals("jiné políčko výběr přesune", "D1/T1", m.vybrane());
		klikni(m, 0, 0);
		Assert.assertNull(m.vybrane());
		Assert.assertEquals(Arrays.asList("D3/T4", "D1/T1", null), vybrana);
		klikni(m, 8, 8);
		klikni(m, 1, 1);
		Assert.assertNull("bez kandidátů a splněné nejde vybrat", m.vybrane());
		Assert.assertEquals(3, vybrana.size());
	}

	@Test
	public void klavesnice() {
		final JMaticeDT m = matice();
		Assert.assertFalse("Esc bez výběru nechá popupu", m.klavesa(KeyEvent.VK_ESCAPE));
		Assert.assertTrue(m.klavesa(KeyEvent.VK_END));
		Assert.assertArrayEquals("poslední s kandidáty", new int[] { 4, 6 }, m.fokus());
		Assert.assertTrue(m.klavesa(KeyEvent.VK_ENTER));
		Assert.assertEquals("D3/T4", m.vybrane());
		Assert.assertTrue(m.klavesa(KeyEvent.VK_ESCAPE));
		Assert.assertNull(m.vybrane());
		Assert.assertTrue(m.klavesa(KeyEvent.VK_HOME));
		Assert.assertArrayEquals(new int[] { 0, 0 }, m.fokus());
		m.klavesa(KeyEvent.VK_UP);
		m.klavesa(KeyEvent.VK_LEFT);
		Assert.assertArrayEquals("u kraje zůstane", new int[] { 0, 0 }, m.fokus());
		m.klavesa(KeyEvent.VK_DOWN);
		m.klavesa(KeyEvent.VK_RIGHT);
		Assert.assertArrayEquals(new int[] { 1, 1 }, m.fokus());
		Assert.assertTrue(m.klavesa(KeyEvent.VK_SPACE));
		Assert.assertNull("splněné nejde vybrat ani klávesou", m.vybrane());
		Assert.assertFalse(m.klavesa(KeyEvent.VK_A));
		Assert.assertEquals(Arrays.asList("D3/T4", null), vybrana);
	}

	@Test
	public void vyberZModeluANovyVysledek() {
		final JMaticeDT m = matice();
		m.setVybrane("D3/T4");
		Assert.assertEquals("D3/T4", m.vybrane());
		Assert.assertArrayEquals("fokus na vybraném", new int[] { 4, 6 }, m.fokus());
		Assert.assertTrue("model výběr nehlásí zpět", vybrana.isEmpty());
		m.nastav(new VysledekPolicek(81, Arrays.asList("D1/T1"), Arrays.asList(new KandidatPolicka("A", 50, 14, "D1/T1", "", null)), 50));
		Assert.assertNull("splněné políčko už vybrané není", m.vybrane());
	}

	private static BufferedImage vykresli(final JMaticeDT m) {
		final BufferedImage img = new BufferedImage(m.getWidth(), m.getHeight(), BufferedImage.TYPE_INT_RGB);
		final java.awt.Graphics2D g = img.createGraphics();
		g.setColor(UIManager.getColor("Panel.background"));
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		m.paint(g);
		g.dispose();
		return img;
	}

	/** Vybrané políčko má jiné pozadí než nevybrané s kandidáty a znaky jsou vidět ve všech skinech. */
	@Test
	public void citelneVeVsechSkinech() throws Exception {
		final javax.swing.LookAndFeel puvodni = UIManager.getLookAndFeel();
		final MetalTheme puvodniTema = MetalLookAndFeel.getCurrentTheme();
		try {
			for (final MetalTheme t : new MetalTheme[] { new OceanTheme(), new DefaultMetalTheme(), new CharcoalTheme() }) {
				MetalLookAndFeel.setCurrentTheme(t);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				SwingUtilities.invokeAndWait(() -> {
					final JMaticeDT m = matice();
					m.setVybrane("D3/T4");
					final BufferedImage img = vykresli(m);
					final Rectangle vyb = m.obdelnik(4, 6);
					final Rectangle jine = m.obdelnik(0, 0);
					final Color pozadiVyb = new Color(img.getRGB(vyb.x + 5, vyb.y + vyb.height / 2));
					final Color pozadiJine = new Color(img.getRGB(jine.x + 5, jine.y + jine.height / 2));
					Assert.assertNotEquals(t.getName() + ": vybrané × nevybrané", pozadiVyb.getRGB(), pozadiJine.getRGB());
					for (final int[] p : new int[][] { { 0, 0 }, { 1, 1 }, { 8, 8 }, { 4, 6 } }) {
						final Rectangle r = m.obdelnik(p[0], p[1]);
						final Color podklad = new Color(img.getRGB(r.x + 4, r.y + r.height / 2));
						double nejvic = 0;
						for (int x = r.x + 8; x < r.x + r.width - 8; x++) {
							for (int y = r.y + 6; y < r.y + r.height - 6; y++) {
								nejvic = Math.max(nejvic, Math.abs(JMaticeDT.jas(new Color(img.getRGB(x, y))) - JMaticeDT.jas(podklad)));
							}
						}
						Assert.assertTrue(t.getName() + " " + JMaticeDT.stitek(p[0], p[1]) + ": znak je vidět (" + nejvic + ")", nejvic > 0.25);
					}
				});
			}
		} finally {
			MetalLookAndFeel.setCurrentTheme(puvodniTema);
			UIManager.setLookAndFeel(puvodni);
		}
	}
}
