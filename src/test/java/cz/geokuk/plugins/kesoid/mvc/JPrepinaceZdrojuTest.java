package cz.geokuk.plugins.kesoid.mvc;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

import org.junit.*;

import cz.geokuk.plugins.kesoid.importek.StavZdroje;
import cz.geokuk.plugins.kesoid.importek.StavZdroju;
import cz.geokuk.plugins.kesoid.importek.TypZdroje;

/** Blok Zdroje ve stavovém řádku: pevné rozměry, aktivní plochy, popupy a jejich zavírání. */
public class JPrepinaceZdrojuTest {

	private final StavyZdrojuProTesty data = StavyZdrojuProTesty.vzorek();
	private final StavyZdrojuProTesty.Zaznam ovladani = new StavyZdrojuProTesty.Zaznam();
	private JFrame okno;
	private JPrepinaceZdroju blok;

	@Before
	public void pred() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		SwingUtilities.invokeAndWait(() -> {
			blok = new JPrepinaceZdroju();
			blok.setOvladani(ovladani);
			blok.obnov(data.snimek());
			okno = new JFrame();
			final JPanel radek = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			radek.add(blok);
			okno.add(radek, BorderLayout.SOUTH);
			// Celé okno na obrazovce (runner Windows má 1024 px), aby se popup nemusel posouvat.
			final Rectangle obrazovka = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds();
			okno.setBounds(obrazovka.x, obrazovka.y + 100, Math.min(1200, obrazovka.width), 300);
			okno.setVisible(true);
		});
		naEdt(() -> blok.prizpusob());
	}

	@After
	public void po() throws Exception {
		if (okno != null) {
			SwingUtilities.invokeAndWait(() -> {
				blok.zavriSeznam();
				okno.dispose();
			});
		}
	}

	private static void naEdt(final Runnable r) throws Exception {
		SwingUtilities.invokeAndWait(r);
	}

	/** Systém okna (macOS) může polohu a velikost okna po setBounds ještě upravit; čeká, až se nemění. */
	private void pockejNaUstaleniOkna() throws Exception {
		Rectangle predtim = null;
		for (int i = 0; i < 40; i++) {
			final Rectangle[] r = new Rectangle[1];
			naEdt(() -> r[0] = new Rectangle(blok.getLocationOnScreen(), okno.getSize()));
			if (r[0].equals(predtim)) {
				return;
			}
			predtim = r[0];
			Thread.sleep(100);
		}
	}

	private Dimension rozmer(final StavZdroju stav) throws Exception {
		final Dimension[] d = new Dimension[1];
		naEdt(() -> {
			blok.obnov(stav);
			d[0] = blok.getPreferredSize();
		});
		return d[0];
	}

	/** Vzhled nastavený až po vytvoření bloku (jiné písmo, větší při zvětšení obrazovky) nezkrátí popisek ani názvy. */
	@Test
	public void popisekCelyPoZmeneVzhledu() throws Exception {
		naEdt(() -> {
			zvetsiPismo(blok);
			okno.validate();
			for (final Component c : blok.getComponents()) {
				vejdeSe(c);
			}
		});
	}

	private static void zvetsiPismo(final Component c) {
		c.setFont(c.getFont().deriveFont(c.getFont().getSize2D() * 1.5f));
		if (c instanceof Container) {
			for (final Component d : ((Container) c).getComponents()) {
				zvetsiPismo(d);
			}
		}
	}

	private static void vejdeSe(final Component c) {
		if (c.isVisible() && c instanceof JComponent) {
			Assert.assertTrue(c + " " + c.getWidth(), c.getWidth() >= c.getPreferredSize().width);
			for (final Component d : ((Container) c).getComponents()) {
				vejdeSe(d);
			}
		}
	}

	/** Dlouhý seznam sahá až k panelu nástrojů okna, výš ne; zbytek se posouvá. */
	@Test
	public void popupAzKPaneluNastroju() throws Exception {
		final StavyZdrojuProTesty mnoho = new StavyZdrojuProTesty();
		for (int i = 0; i < 60; i++) {
			mnoho.pridej(TypZdroje.GPX, "trasa" + i + ".gpx");
		}
		mnoho.prepis();
		final JToolBar lista = new JToolBar();
		lista.add(new JButton("Nástroj"));
		naEdt(() -> {
			okno.add(lista, BorderLayout.NORTH);
			final Rectangle obrazovka = okno.getGraphicsConfiguration().getBounds();
			okno.setBounds(obrazovka.x, obrazovka.y + 20, okno.getWidth(), Math.min(700, obrazovka.height - 20));
			okno.validate();
		});
		pockejNaUstaleniOkna();
		naEdt(() -> {
			blok.obnov(mnoho.snimek());
			blok.ukazSouhrn();
		});
		naEdt(() -> {
			final JComponent obsah = blok.viditelny();
			Assert.assertNotNull(obsah);
			final int hranice = Math.max(okno.getGraphicsConfiguration().getBounds().y, lista.getLocationOnScreen().y + lista.getHeight());
			Assert.assertEquals(hranice, obsah.getLocationOnScreen().y);
			Assert.assertEquals(blok.getLocationOnScreen().y, obsah.getLocationOnScreen().y + obsah.getHeight());
			Assume.assumeTrue("málo místa na obrazovce", blok.getLocationOnScreen().y - hranice > 24 * JTabulkaZdroju.VYSKA_RADKU);
			Assert.assertTrue("víc než 20 řádků: " + obsah.getHeight(), obsah.getHeight() > 22 * JTabulkaZdroju.VYSKA_RADKU);
		});
	}

	/** Název hned za zaškrtávátkem, ikona stavu za názvem. */
	@Test
	public void poradiZaskrtavatkoNazevIkona() throws Exception {
		naEdt(() -> {
			for (final TypZdroje typ : TypZdroje.values()) {
				final int z = blok.getZaskrtavatko(typ).getX();
				final int n = blok.getNazev(typ).getX();
				final int i = blok.getIkona(typ).getX();
				Assert.assertTrue(typ + ": " + z + " " + n + " " + i, z < n && n < i);
			}
		});
	}

	/** Prázdný slot ikony za názvem vypnutého typu drží šířku buňky. */
	@Test
	public void vypnutyTypStalaSirkaBunky() throws Exception {
		final int[] sirka = new int[1];
		naEdt(() -> sirka[0] = blok.getBunka(TypZdroje.GSAK).getPreferredSize().width);
		data.vypnuteTypy.add(TypZdroje.GSAK);
		data.prepisZapnuti();
		naEdt(() -> {
			blok.obnov(data.snimek());
			okno.validate();
			Assert.assertEquals(sirka[0], blok.getBunka(TypZdroje.GSAK).getPreferredSize().width);
			Assert.assertTrue(blok.getIkona(TypZdroje.GSAK).getWidth() > 0);
		});
	}

	/** Ikona „načítá se“ se točí jen během načítání a jen když je blok vidět. */
	@Test
	public void tocitkoJenBehemNacitani() throws Exception {
		final Dimension nacteno = rozmer(data.snimek());
		naEdt(() -> Assert.assertFalse(blok.animuje()));
		final java.io.File gg = data.snimek().getPolozky(TypZdroje.GEOGET).get(0).getSoubor();
		data.registr.zacina(data.registr.getGenerace(), gg);
		Assert.assertEquals("slot ikony drží šířku", nacteno, rozmer(data.snimek()));
		naEdt(() -> {
			Assert.assertTrue(blok.animuje());
			Assert.assertFalse("schovaná tabulka se nepřekresluje", blok.getUplna().animuje());
			blok.ukazSouhrn();
		});
		naEdt(() -> {
			Assert.assertTrue(blok.getUplna().animuje());
			blok.zavriSeznam();
			okno.setVisible(false);
		});
		naEdt(() -> {
			Assert.assertFalse("schovaný blok se nepřekresluje", blok.animuje());
			Assert.assertFalse(blok.getUplna().animuje());
			okno.setVisible(true);
		});
		naEdt(() -> Assert.assertTrue("po zobrazení se točí dál", blok.animuje()));
		data.registr.hotovo(data.registr.getGenerace(), gg, 10, 10);
		naEdt(() -> {
			blok.obnov(data.snimek());
			Assert.assertFalse(blok.animuje());
			Assert.assertFalse(blok.getUplna().animuje());
		});
	}

	@Test
	public void rozmerNezavisiNaStavu() throws Exception {
		final Dimension nacteno = rozmer(data.snimek());
		final File gsak = data.snimek().getPolozky(TypZdroje.GSAK).get(0).getSoubor();
		data.registr.cekaNaZapis(data.registr.getGenerace(), gsak);
		Assert.assertEquals(nacteno, rozmer(data.snimek()));
		data.registr.chyba(data.registr.getGenerace(), data.snimek().getPolozky(TypZdroje.GPX).get(0).getSoubor(), "Vadný soubor");
		Assert.assertEquals(nacteno, rozmer(data.snimek()));
		data.vypnuteTypy.addAll(Arrays.asList(TypZdroje.values()));
		data.prepisZapnuti();
		Assert.assertEquals(nacteno, rozmer(data.snimek()));
		Assert.assertEquals(nacteno, rozmer(StavZdroju.PRAZDNY));
	}

	@Test
	public void vypnutyTypMaPrazdnouIkonuASedyNazev() throws Exception {
		data.vypnuteTypy.add(TypZdroje.GSAK);
		data.prepisZapnuti();
		naEdt(() -> blok.obnov(data.snimek()));
		Assert.assertSame(IkonyZdroju.prazdna(), blok.getIkona(TypZdroje.GSAK).getIcon());
		final Color seda = UIManager.getColor("Label.disabledForeground");
		Assert.assertEquals(seda != null ? seda : Color.GRAY, blok.getNazev(TypZdroje.GSAK).getForeground());
		Assert.assertNotEquals(blok.getNazev(TypZdroje.GSAK).getForeground(), blok.getNazev(TypZdroje.GEOGET).getForeground());
		Assert.assertSame(IkonyZdroju.pro(StavZdroje.NACTENO), blok.getIkona(TypZdroje.GEOGET).getIcon());
	}

	@Test
	public void zamekVBubline() throws Exception {
		data.registr.cekaNaZapis(data.registr.getGenerace(), data.snimek().getPolozky(TypZdroje.GSAK).get(0).getSoubor());
		naEdt(() -> blok.obnov(data.snimek()));
		Assert.assertSame(IkonyZdroju.pro(StavZdroje.CEKA_NA_ZAPIS), blok.getIkona(TypZdroje.GSAK).getIcon());
		final String tip = blok.getIkona(TypZdroje.GSAK).getToolTipText();
		Assert.assertTrue(tip, tip.contains("Zamčeno jiným programem") && tip.contains("Domov.db3") && tip.contains("Zavřete program"));

		for (final cz.geokuk.plugins.kesoid.importek.StavPolozky p : data.snimek().getPolozky(TypZdroje.GEOGET)) {
			data.registr.cekaNaZapis(data.registr.getGenerace(), p.getSoubor());
		}
		naEdt(() -> blok.obnov(data.snimek()));
		final String dve = blok.getIkona(TypZdroje.GEOGET).getToolTipText();
		Assert.assertTrue("každá databáze na vlastním řádku: " + dve, dve.contains("<br>Cesko.db3<br>Slovensko.db3<br>Zavřete program"));
	}

	@Test
	public void aktivniPlochyNaCelouVyskuAMezeryNereaguji() throws Exception {
		naEdt(() -> {
			final Insets ins = blok.getInsets();
			final int vyska = blok.getHeight() - ins.top - ins.bottom;
			final List<JPrepinaceZdroju.Oblast> oblasti = blok.oblasti();
			for (final JPrepinaceZdroju.Oblast o : oblasti) {
				Assert.assertEquals(o.druh + " " + o.typ, vyska, o.plocha.height);
			}
			final JComponent zc = blok.getZaskrtavatko(TypZdroje.GEOGET);
			final JComponent ic = blok.getIkona(TypZdroje.GEOGET);
			final Rectangle z = SwingUtilities.convertRectangle(zc.getParent(), zc.getBounds(), blok);
			final Rectangle i = SwingUtilities.convertRectangle(ic.getParent(), ic.getBounds(), blok);
			Assert.assertTrue("mezera mezi zaškrtávátkem a ikonou", i.x > z.x + z.width);
			final Point mezera = new Point(z.x + z.width, z.y + z.height / 2);
			for (final JPrepinaceZdroju.Oblast o : oblasti) {
				Assert.assertFalse(o.druh + " " + o.typ, o.plocha.contains(mezera));
			}
		});
	}

	@Test
	public void popisekOteviraUplnouANazevDetail() throws Exception {
		naEdt(() -> {
			otevri(blok.getPopisek());
			Assert.assertSame(blok.getObsahSouhrnu(), blok.viditelny());
			otevri(blok.getNazev(TypZdroje.GSAK));
			Assert.assertSame(blok.getObsahDetailu(TypZdroje.GSAK), blok.viditelny());
			otevri(blok.getIkona(TypZdroje.GEOGET));
			Assert.assertSame("ikona s názvem detail neotvírá", blok.getObsahDetailu(TypZdroje.GSAK), blok.viditelny());
		});
	}

	/** Najetí a vydržení na prvku, dokud se popup neotevře či nepřepne. */
	private void otevri(final JComponent c) {
		najed(c);
		blok.tik(stred(c), System.currentTimeMillis() + JPrepinaceZdroju.PRODLEVA_PREPNUTI_MS);
	}

	private static Point stred(final JComponent c) {
		final Point p = c.getLocationOnScreen();
		return new Point(p.x + c.getWidth() / 2, p.y + c.getHeight() / 2);
	}

	private static void odjed(final JComponent c) {
		final java.awt.event.MouseEvent e = new java.awt.event.MouseEvent(c, java.awt.event.MouseEvent.MOUSE_EXITED, 0, 0, 2, 2, 0, false);
		for (final java.awt.event.MouseListener l : c.getMouseListeners()) {
			l.mouseExited(e);
		}
	}

	private static void najed(final JComponent c) {
		final java.awt.event.MouseEvent e = new java.awt.event.MouseEvent(c, java.awt.event.MouseEvent.MOUSE_ENTERED, 0, 0, 2, 2, 0, false);
		for (final java.awt.event.MouseListener l : c.getMouseListeners()) {
			l.mouseEntered(e);
		}
	}

	@Test
	public void popupTesneNadBlokemAUvnitrObrazovky() throws Exception {
		naEdt(() -> {
			otevri(blok.getPopisek());
			final JComponent p = blok.viditelny();
			final Point bp = blok.getLocationOnScreen();
			final Point pp = p.getLocationOnScreen();
			Assert.assertEquals(bp.y, pp.y + p.getHeight());
			Assert.assertEquals("úplná zarovnaná vpravo", bp.x + blok.getWidth(), pp.x + p.getWidth());
			otevri(blok.getNazev(TypZdroje.GPX));
			final JComponent d = blok.viditelny();
			Assert.assertEquals(bp.y, d.getLocationOnScreen().y + d.getHeight());
			final Rectangle obrazovka = blok.getGraphicsConfiguration().getBounds();
			Assert.assertTrue(obrazovka.contains(new Rectangle(d.getLocationOnScreen(), d.getSize())));
		});
	}

	@Test
	public void zaviraSePo500msMimo() throws Exception {
		naEdt(() -> {
			otevri(blok.getPopisek());
			final Point mimo = new Point(blok.getLocationOnScreen().x - 300, blok.getLocationOnScreen().y + 5);
			final Point uvnitr = new Point(blok.getLocationOnScreen().x + 5, blok.getLocationOnScreen().y + 5);
			blok.tik(mimo, 1000);
			blok.tik(mimo, 1499);
			Assert.assertNotNull(blok.viditelny());
			blok.tik(uvnitr, 1510);
			blok.tik(mimo, 1600);
			blok.tik(mimo, 2099);
			Assert.assertNotNull("návrat do bloku odpočet zrušil", blok.viditelny());
			blok.tik(mimo, 2100);
			Assert.assertNull(blok.viditelny());
		});
	}

	@Test
	public void zaskrtavatkoPrepinaTyp() throws Exception {
		naEdt(() -> blok.getZaskrtavatko(TypZdroje.GSAK).doClick());
		Assert.assertEquals(Arrays.asList("typ GSAK false"), ovladani.volani);
	}

	@Test
	public void uzkeOknoBezNazvuAIkonaOteviraDetail() throws Exception {
		final Dimension siroky = rozmer(data.snimek());
		naEdt(() -> okno.setSize(800, 300));
		// Správce oken může šířku potvrdit až později; do té doby by blok počítal se starou.
		final long konec = System.currentTimeMillis() + 5000;
		final int[] sirka = new int[1];
		do {
			naEdt(() -> sirka[0] = okno.getWidth());
		} while (sirka[0] != 800 && System.currentTimeMillis() < konec);
		naEdt(() -> {
			blok.prizpusob();
			okno.validate();
		});
		naEdt(() -> {
			Assert.assertTrue(blok.isKompaktni());
			Assert.assertTrue(blok.getPreferredSize().width < siroky.width);
			Assert.assertTrue(blok.getUplna().isUzka());
			otevri(blok.getIkona(TypZdroje.OPENSAK));
			Assert.assertSame(blok.getObsahDetailu(TypZdroje.OPENSAK), blok.viditelny());
			Assert.assertTrue(blok.viditelny().getWidth() <= 800);
		});
	}

	@Test
	public void najetiAZmenaStavuNemeniRozlozeni() throws Exception {
		final Rectangle[] pred = new Rectangle[2];
		naEdt(() -> {
			okno.validate();
			pred[0] = blok.getBounds();
			pred[1] = blok.getParent().getBounds();
			otevri(blok.getPopisek());
			otevri(blok.getNazev(TypZdroje.GEOGET));
			data.registr.zacina(data.registr.getGenerace(), data.snimek().getPolozky(TypZdroje.GEOGET).get(0).getSoubor());
			blok.obnov(data.snimek());
			okno.validate();
			Assert.assertEquals(pred[0], blok.getBounds());
			Assert.assertEquals(pred[1], blok.getParent().getBounds());
		});
		data.registr.hotovo(data.registr.getGenerace(), data.snimek().getPolozky(TypZdroje.GEOGET).get(0).getSoubor(), 10, 10);
		naEdt(() -> {
			blok.obnov(data.snimek());
			blok.zavriSeznam();
			okno.validate();
			Assert.assertEquals(pred[0], blok.getBounds());
		});
	}

	@Test
	public void jenTypyPovoleneVNastaveni() throws Exception {
		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		u.setKesDir(new cz.geokuk.util.file.Filex(new File("/kese"), false, true));
		u.setGeogetDataDir(new cz.geokuk.util.file.Filex(new File("/geoget"), false, true));
		u.setGsakDataDir(new cz.geokuk.util.file.Filex(new File("/gsak"), false, false));
		u.setOpensakDataDir(new cz.geokuk.util.file.Filex(new File("/opensak"), false, false));
		Assert.assertEquals(java.util.EnumSet.of(TypZdroje.GPX, TypZdroje.GEOGET), JPrepinaceZdroju.povoleneTypy(u));
		final int plna = rozmer(data.snimek()).width;
		naEdt(() -> {
			blok.setPovoleneTypy(JPrepinaceZdroju.povoleneTypy(u));
			okno.validate();
			Assert.assertFalse(blok.getBunka(TypZdroje.GSAK).isVisible());
			Assert.assertTrue(blok.getBunka(TypZdroje.GEOGET).isVisible());
			Assert.assertTrue(blok.getPreferredSize().width < plna);
			Assert.assertEquals("vpravo zůstává", blok.getParent().getWidth(), blok.getX() + blok.getWidth());
			Assert.assertEquals(2 + 3, blok.getUplna().getRadky().size());
			for (final JPrepinaceZdroju.Oblast o : blok.oblasti()) {
				Assert.assertNotEquals(TypZdroje.GSAK, o.typ);
			}
		});
	}

	@Test
	public void otevreSeAzPoZdrzeniAPrebehnutiNazvuHoNeprepne() throws Exception {
		naEdt(() -> {
			final Point naPopisku = stred(blok.getPopisek());
			final long t0 = System.currentTimeMillis();
			najed(blok.getPopisek());
			blok.tik(naPopisku, t0 + JPrepinaceZdroju.PRODLEVA_OTEVRENI_MS - 1);
			Assert.assertNull("hned se neotevře", blok.viditelny());
			final long t1 = System.currentTimeMillis();
			blok.tik(naPopisku, t1 + JPrepinaceZdroju.PRODLEVA_OTEVRENI_MS);
			Assert.assertSame(blok.getObsahSouhrnu(), blok.viditelny());

			// Cestou k popupu přes název GSAK: krátké přeběhnutí popup nepřepne.
			final long t2 = System.currentTimeMillis();
			najed(blok.getNazev(TypZdroje.GSAK));
			blok.tik(stred(blok.getNazev(TypZdroje.GSAK)), t2 + JPrepinaceZdroju.PRODLEVA_PREPNUTI_MS - 1);
			odjed(blok.getNazev(TypZdroje.GSAK));
			blok.tik(naPopisku, t2 + 2 * JPrepinaceZdroju.PRODLEVA_PREPNUTI_MS);
			Assert.assertSame(blok.getObsahSouhrnu(), blok.viditelny());

			// Vydržení na názvu přepne.
			najed(blok.getNazev(TypZdroje.GSAK));
			final long t3 = System.currentTimeMillis();
			blok.tik(stred(blok.getNazev(TypZdroje.GSAK)), t3 + JPrepinaceZdroju.PRODLEVA_PREPNUTI_MS);
			Assert.assertSame(blok.getObsahDetailu(TypZdroje.GSAK), blok.viditelny());
		});
	}

	@Test
	public void bublinyJsouCeleNadBlokem() throws Exception {
		data.registr.cekaNaZapis(data.registr.getGenerace(), data.snimek().getPolozky(TypZdroje.GSAK).get(0).getSoubor());
		naEdt(() -> {
			blok.obnov(data.snimek());
			for (final JComponent c : new JComponent[] { blok.getZaskrtavatko(TypZdroje.GSAK), blok.getIkona(TypZdroje.GSAK) }) {
				final java.awt.event.MouseEvent e = new java.awt.event.MouseEvent(c, java.awt.event.MouseEvent.MOUSE_MOVED, 0, 0, 3, c.getHeight() - 1, 0, false);
				final Point p = c.getToolTipLocation(e);
				final JToolTip tip = c.createToolTip();
				tip.setTipText(c.getToolTipText(e));
				final Point vBloku = SwingUtilities.convertPoint(c, p, blok);
				Assert.assertTrue(c.getClass() + " " + vBloku, vBloku.y + tip.getPreferredSize().height < 0);
			}
		});
	}

	@Test
	public void bublinaZamkuMaKazdouDatabaziNaRadku() {
		final StavyZdrojuProTesty s = new StavyZdrojuProTesty();
		final File a = s.pridej(TypZdroje.GSAK, "A.db3");
		final File b = s.pridej(TypZdroje.GSAK, "B.db3");
		s.prepis();
		s.registr.cekaNaZapis(s.registr.getGenerace(), a);
		s.registr.cekaNaZapis(s.registr.getGenerace(), b);
		Assert.assertEquals("<html>GSAK: " + JTabulkaZdroju.ZAMCENO + "<br>A.db3<br>B.db3<br>" + JTabulkaZdroju.RADA_ZAMCENO, JPrepinaceZdroju.tooltipIkony(s.snimek(), TypZdroje.GSAK));
	}

	/** Záznam kliknutí v Diagnostice bere text bubliny a u přepínačů jejich vlastní stav; zaškrtávátko typu proto přepínačem není a bublina říká, co klik udělá. */
	@Test
	public void zaskrtavatkoNeniPrepinacABublinaRikaAkci() {
		Assert.assertFalse((Object) blok.getZaskrtavatko(TypZdroje.GSAK) instanceof JToggleButton);
		Assert.assertEquals("GSAK zapnout (Ctrl+3); " + JTabulkaZdroju.KLAVESA_JEN + "+klik: jen GSAK", JPrepinaceZdroju.tooltipZaskrtavatka(TypZdroje.GSAK, cz.geokuk.plugins.kesoid.importek.StavZdroju.StavVyberu.VYPNUTO));
		Assert.assertEquals("GSAK vypnout (Ctrl+3); " + JTabulkaZdroju.KLAVESA_JEN + "+klik: jen GSAK", JPrepinaceZdroju.tooltipZaskrtavatka(TypZdroje.GSAK, cz.geokuk.plugins.kesoid.importek.StavZdroju.StavVyberu.ZAPNUTO));
		Assert.assertTrue(JPrepinaceZdroju.tooltipZaskrtavatka(TypZdroje.GSAK, cz.geokuk.plugins.kesoid.importek.StavZdroju.StavVyberu.CASTECNE).startsWith("GSAK vypnout (Ctrl+3)"));
	}

	@Test
	public void prazdnaNeboNedostupnaSlozkaVBloku() throws Exception {
		data.registr.setProblemySlozek(data.registr.getGenerace(), java.util.Collections.singletonMap(TypZdroje.OPENSAK, "Složka není dostupná."));
		final Dimension pred = rozmer(data.snimek());
		naEdt(() -> {
			Assert.assertSame(IkonyZdroju.pro(StavZdroje.CHYBA), blok.getIkona(TypZdroje.OPENSAK).getIcon());
			final String tip = blok.getIkona(TypZdroje.OPENSAK).getToolTipText();
			Assert.assertTrue(tip, tip.contains("OpenSAK: Složka není dostupná") && tip.contains("Umístění souborů"));
			Assert.assertTrue(JTabulkaZdroju.RADA_SLOZKA.length() > 0);
		});
		Assert.assertEquals("rozměr bloku se nemění", pred, rozmer(StavZdroju.PRAZDNY));
	}
}
