package cz.geokuk.core.program;

import java.io.File;
import java.util.*;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.core.render.*;
import cz.geokuk.plugins.kesoid.mvc.*;
import cz.geokuk.plugins.mapy.*;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.util.file.Filex;

public class JPrehledSouboruTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void bublinyNacitatAzPoVybraniRadiTabulkuZdroju() {
		final JPrehledSouboru panel = new JPrehledSouboru(null);
		int pocet = 0;
		for (final javax.swing.JCheckBox c : checkboxy(panel)) {
			if ("Nové databáze načíst až po vybrání".equals(c.getText())) {
				pocet++;
				Assert.assertTrue(c.getToolTipText(), c.getToolTipText().contains("„Zdroje:“ ve stavovém řádku") && c.getToolTipText().contains("Soubor &gt; Přehled zdrojů"));
			}
		}
		Assert.assertEquals(3, pocet);
	}

	/** Zdroje keší jsou na jedné záložce, každý v rámečku s názvem a klávesovou zkratkou. */
	@Test
	public void zdrojeKesiMajiJednuZalozkuSNadpisyAZkratkami() {
		final JPrehledSouboru panel = new JPrehledSouboru(null);
		final javax.swing.JTabbedPane zalozky = (javax.swing.JTabbedPane) najdi(panel, javax.swing.JTabbedPane.class).get(0);
		final List<String> nazvyZalozek = new ArrayList<>();
		for (int i = 0; i < zalozky.getTabCount(); i++) {
			nazvyZalozek.add(zalozky.getTitleAt(i));
		}
		Assert.assertEquals(java.util.Arrays.asList("Zdroje keší", "Mapy", "Rendr", "Program"), nazvyZalozek);
		final List<String> nadpisy = new ArrayList<>();
		for (final java.awt.Component c : najdi((java.awt.Container) zalozky.getComponentAt(0), javax.swing.JPanel.class)) {
			if (((javax.swing.JPanel) c).getBorder() instanceof javax.swing.border.CompoundBorder
					&& ((javax.swing.border.CompoundBorder) ((javax.swing.JPanel) c).getBorder()).getOutsideBorder() instanceof javax.swing.border.TitledBorder) {
				nadpisy.add(((javax.swing.border.TitledBorder) ((javax.swing.border.CompoundBorder) ((javax.swing.JPanel) c).getBorder()).getOutsideBorder()).getTitle());
			}
		}
		Assert.assertEquals(java.util.Arrays.asList("GPX (Ctrl+1)", "GeoGet (Ctrl+2)", "GSAK (Ctrl+3)", "OpenSAK (Ctrl+4)"), nadpisy);
	}

	/** Panel složky GeoGet, GSAK a OpenSAK se nesmí zkrátit pod výšku, kterou potřebují přidaná políčka (jinak by se políčka neukázala). */
	@Test
	public void panelSlozkyMaMistoProPridanaPolicka() {
		final JPrehledSouboru panel = new JPrehledSouboru(null);
		int pocet = 0;
		for (final javax.swing.JCheckBox c : checkboxy(panel)) {
			if ("Nové databáze načíst až po vybrání".equals(c.getText())) {
				pocet++;
				final java.awt.Container rodic = c.getParent();
				Assert.assertTrue(rodic.getMaximumSize().height + " < " + rodic.getPreferredSize().height, rodic.getMaximumSize().height >= rodic.getPreferredSize().height);
			}
		}
		Assert.assertEquals(3, pocet);
	}

	/** Na malé obrazovce (třeba 1366×768 při 150 %) se záložky posouvají a řádek s tlačítkem Uložit zůstane vidět. */
	@Test
	public void minimalniVyskaPanelujeMalaATlacitkoUlozitJeVOknuVidet() {
		final JPrehledSouboru panel = new JPrehledSouboru(null);
		Assert.assertTrue("minimální výška " + panel.getMinimumSize().height, panel.getMinimumSize().height <= 290);
		Assert.assertTrue(panel.getMinimumSize().height < JUmisteniSouboruDialog.NEJMENSI_VELIKOST.height - 100);
		for (final java.awt.Dimension okno : new java.awt.Dimension[] { JUmisteniSouboruDialog.NEJMENSI_VELIKOST, new java.awt.Dimension(910, 460) }) {
			// Bez skutečného okna, ať test běží i bez displeje: panel dostane velikost okna a rozloží se.
			panel.setSize(okno);
			rozloz(panel);
			final javax.swing.JButton ulozit = najdiTlacitko(panel, "Uložit");
			Assert.assertNotNull(ulozit);
			final java.awt.Point levyHorni = javax.swing.SwingUtilities.convertPoint(ulozit.getParent(), ulozit.getLocation(), panel);
			Assert.assertTrue("Uložit leží uvnitř okna " + okno + ": y=" + levyHorni.y + " h=" + ulozit.getHeight() + " výška=" + panel.getHeight(), levyHorni.y + ulozit.getHeight() <= panel.getHeight());
			Assert.assertTrue(levyHorni.x + ulozit.getWidth() <= panel.getWidth());
		}
	}

	private static void rozloz(final java.awt.Component c) {
		c.doLayout();
		if (c instanceof java.awt.Container) {
			for (final java.awt.Component k : ((java.awt.Container) c).getComponents()) {
				rozloz(k);
			}
		}
	}

	private static javax.swing.JButton najdiTlacitko(final java.awt.Container kde, final String text) {
		for (final java.awt.Component c : najdi(kde, javax.swing.JButton.class)) {
			if (text.equals(((javax.swing.JButton) c).getText())) {
				return (javax.swing.JButton) c;
			}
		}
		return null;
	}

	@Test
	public void dialogJeVetsiNezMinimum() {
		Assert.assertTrue(JUmisteniSouboruDialog.VYCHOZI_VELIKOST.width > JUmisteniSouboruDialog.NEJMENSI_VELIKOST.width);
		Assert.assertTrue(JUmisteniSouboruDialog.VYCHOZI_VELIKOST.height > JUmisteniSouboruDialog.NEJMENSI_VELIKOST.height);
		Assert.assertTrue(JUmisteniSouboruDialog.VYCHOZI_VELIKOST.width >= 900);
	}

	private static List<java.awt.Component> najdi(final java.awt.Container kde, final Class<?> typ) {
		final List<java.awt.Component> vysledek = new ArrayList<>();
		for (final java.awt.Component c : kde.getComponents()) {
			if (typ.isInstance(c)) {
				vysledek.add(c);
			}
			if (c instanceof java.awt.Container) {
				vysledek.addAll(najdi((java.awt.Container) c, typ));
			}
		}
		return vysledek;
	}

	private static List<javax.swing.JCheckBox> checkboxy(final java.awt.Container kde) {
		final List<javax.swing.JCheckBox> vysledek = new ArrayList<>();
		for (final java.awt.Component c : kde.getComponents()) {
			if (c instanceof javax.swing.JCheckBox) {
				vysledek.add((javax.swing.JCheckBox) c);
			}
			if (c instanceof java.awt.Container) {
				vysledek.addAll(checkboxy((java.awt.Container) c));
			}
		}
		return vysledek;
	}

	/** „Načítat až po vybrání“ a nová složka GSAK v jednom uložení: model musí dostat volbu dřív než složku. */
	@Test
	public void volbaGsakSeUloziPredSlozkami() throws Exception {
		final JPrehledSouboru panel = new JPrehledSouboru(null);
		final List<String> volani = new ArrayList<>();
		final KesoidModel kesoidModel = new KesoidModel() {
			@Override
			public void setGsakParametryNacitani(final GsakParametryNacitani g) {
				volani.add("gsak " + g.isNoveDatabazeHned());
			}

			@Override
			public void setUmisteniSouboru(final KesoidUmisteniSouboru u) {
				volani.add("složky");
			}
		};
		final RenderModel renderModel = new RenderModel() {
			@Override
			public void setUmisteniSouboru(final RenderUmisteniSouboru u) {
				volani.add("render");
			}
		};
		panel.inject(kesoidModel);
		panel.inject(renderModel);

		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		u.setKesDir(slozka("kese"));
		u.setGeogetDataDir(slozka("geoget"));
		u.setGsakDataDir(slozka("gsak"));
		u.setOpensakDataDir(slozka("opensak"));
		panel.onEvent(new KesoidUmisteniSouboruChangedEvent(u));
		final RenderUmisteniSouboru r = new RenderUmisteniSouboru();
		r.setOziDir(slozka("ozi"));
		r.setKmzDir(slozka("kmz"));
		r.setPictureDir(slozka("obrazky"));
		panel.onEvent(new RenderUmisteniSouboruChangedEvent(r));
		panel.inject(new KachleModel() {
			@Override
			public void setUmisteniSouboru(final KachleUmisteniSouboru k) {
				volani.add("mapy");
			}
		});
		final KachleUmisteniSouboru k = new KachleUmisteniSouboru();
		k.setKachleCacheDir(KachleUmisteniSouboru.KACHLE_CACHE_DIR);
		k.setOfflineMapyDir(slozka("mapy"));
		panel.onEvent(new KachleUmisteniSouboruChangedEvent(k));
		final GsakParametryNacitani g = new GsakParametryNacitani();
		g.setCasNalezu(Collections.emptySet());
		g.setNoveDatabazeHned(false);
		panel.onEvent(new GsakParametryNacitaniChangedEvent(g));

		panel.uloz();

		Assert.assertEquals(Arrays.asList("gsak false", "složky", "render", "mapy"), volani);
	}

	/** Volba „Nové databáze načíst až po vybrání“ každého zdroje se z modelu načte do svého zaškrtávátka a z něj zase uloží; pořadí je GeoGet, GSAK, OpenSAK. */
	@Test
	public void volbyNacitatAzPoVybraniSeNactouAUlozi() throws Exception {
		final JPrehledSouboru panel = new JPrehledSouboru(null);
		final List<GsakParametryNacitani> ulozeno = new ArrayList<>();
		panel.inject(new KesoidModel() {
			@Override
			public void setGsakParametryNacitani(final GsakParametryNacitani g) {
				ulozeno.add(g);
			}

			@Override
			public void setUmisteniSouboru(final KesoidUmisteniSouboru u) {}
		});
		panel.inject(new RenderModel() {
			@Override
			public void setUmisteniSouboru(final RenderUmisteniSouboru u) {}
		});
		panel.inject(new KachleModel() {
			@Override
			public void setUmisteniSouboru(final KachleUmisteniSouboru k) {}
		});
		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		u.setKesDir(slozka("kese3"));
		u.setGeogetDataDir(slozka("geoget3"));
		u.setGsakDataDir(slozka("gsak3"));
		u.setOpensakDataDir(slozka("opensak3"));
		panel.onEvent(new KesoidUmisteniSouboruChangedEvent(u));
		final RenderUmisteniSouboru r = new RenderUmisteniSouboru();
		r.setOziDir(slozka("ozi3"));
		r.setKmzDir(slozka("kmz3"));
		r.setPictureDir(slozka("obrazky3"));
		panel.onEvent(new RenderUmisteniSouboruChangedEvent(r));
		final KachleUmisteniSouboru k = new KachleUmisteniSouboru();
		k.setKachleCacheDir(KachleUmisteniSouboru.KACHLE_CACHE_DIR);
		k.setOfflineMapyDir(slozka("mapy3"));
		panel.onEvent(new KachleUmisteniSouboruChangedEvent(k));

		final GsakParametryNacitani g = new GsakParametryNacitani();
		g.setCasNalezu(Collections.emptySet());
		g.setNoveDatabazeGeogetuHned(false);
		g.setNoveDatabazeHned(true);
		g.setNoveDatabazeOpensakuHned(true);
		panel.onEvent(new GsakParametryNacitaniChangedEvent(g));
		final List<javax.swing.JCheckBox> volby = volbyNacitatAzPoVybrani(panel);
		Assert.assertEquals(Arrays.asList(true, false, false), vybrane(volby));

		panel.uloz();
		Assert.assertFalse(ulozeno.get(0).isNoveDatabazeGeogetuHned());
		Assert.assertTrue(ulozeno.get(0).isNoveDatabazeHned());
		Assert.assertTrue(ulozeno.get(0).isNoveDatabazeOpensakuHned());

		g.setNoveDatabazeGeogetuHned(true);
		g.setNoveDatabazeHned(false);
		g.setNoveDatabazeOpensakuHned(false);
		panel.onEvent(new GsakParametryNacitaniChangedEvent(g));
		Assert.assertEquals(Arrays.asList(false, true, true), vybrane(volby));

		volby.get(0).setSelected(true);
		volby.get(1).setSelected(false);
		volby.get(2).setSelected(false);
		panel.uloz();
		Assert.assertFalse(ulozeno.get(1).isNoveDatabazeGeogetuHned());
		Assert.assertTrue(ulozeno.get(1).isNoveDatabazeHned());
		Assert.assertTrue(ulozeno.get(1).isNoveDatabazeOpensakuHned());
	}

	private static List<javax.swing.JCheckBox> volbyNacitatAzPoVybrani(final JPrehledSouboru panel) {
		final List<javax.swing.JCheckBox> volby = new ArrayList<>();
		for (final javax.swing.JCheckBox c : checkboxy(panel)) {
			if ("Nové databáze načíst až po vybrání".equals(c.getText())) {
				volby.add(c);
			}
		}
		Assert.assertEquals(3, volby.size());
		return volby;
	}

	private static List<Boolean> vybrane(final List<javax.swing.JCheckBox> volby) {
		final List<Boolean> vysledek = new ArrayList<>();
		for (final javax.swing.JCheckBox c : volby) {
			vysledek.add(c.isSelected());
		}
		return vysledek;
	}

	/** Uložit prověřuje složky mimo EDT: tlačítko je mezitím zakázané a vidět „Kontroluji složky…“, pak se nastavení uloží. */
	@Test(timeout = 20_000)
	public void ulozitProverujeSlozkyMimoEdt() throws Exception {
		ulozitSKontrolou(true);
	}

	/** Dialog zavřený křížkem během kontroly složek nic neuloží. */
	@Test(timeout = 20_000)
	public void zavrenyDialogBehemKontrolyNicNeulozi() throws Exception {
		ulozitSKontrolou(false);
	}

	private void ulozitSKontrolou(final boolean otevrene) throws Exception {
		final JPrehledSouboru panel = new JPrehledSouboru(null);
		final List<String> volani = Collections.synchronizedList(new ArrayList<>());
		panel.inject(new KesoidModel() {
			@Override
			public void setGsakParametryNacitani(final GsakParametryNacitani g) {}

			@Override
			public void setUmisteniSouboru(final KesoidUmisteniSouboru u) {
				volani.add("složky");
			}
		});
		panel.inject(new RenderModel() {
			@Override
			public void setUmisteniSouboru(final RenderUmisteniSouboru u) {
				volani.add("render");
			}
		});
		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		u.setKesDir(slozka("kese2"));
		u.setGeogetDataDir(slozka("geoget2"));
		u.setGsakDataDir(slozka("gsak2"));
		u.setOpensakDataDir(slozka("opensak2"));
		panel.onEvent(new KesoidUmisteniSouboruChangedEvent(u));
		final RenderUmisteniSouboru r = new RenderUmisteniSouboru();
		r.setOziDir(slozka("ozi2"));
		r.setKmzDir(slozka("kmz2"));
		r.setPictureDir(slozka("obrazky2"));
		panel.onEvent(new RenderUmisteniSouboruChangedEvent(r));
		panel.inject(new KachleModel() {
			@Override
			public void setUmisteniSouboru(final KachleUmisteniSouboru k) {
				volani.add("mapy");
			}
		});
		final KachleUmisteniSouboru k = new KachleUmisteniSouboru();
		k.setKachleCacheDir(KachleUmisteniSouboru.KACHLE_CACHE_DIR);
		k.setOfflineMapyDir(slozka("mapy2"));
		panel.onEvent(new KachleUmisteniSouboruChangedEvent(k));

		panel.oknoOtevrene = () -> otevrene;
		final java.util.concurrent.CountDownLatch pustit = new java.util.concurrent.CountDownLatch(1);
		final java.util.function.Function<File, JJedenSouborPanel.StavSlozky> puvodni = JJedenSouborPanel.kontrola;
		JJedenSouborPanel.kontrola = d -> {
			try {
				pustit.await();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return puvodni.apply(d);
		};
		try {
			final javax.swing.JButton ulozit = tlacitko(panel, "Uložit");
			javax.swing.SwingUtilities.invokeAndWait(ulozit::doClick);
			javax.swing.SwingUtilities.invokeAndWait(() -> {
				Assert.assertFalse("Uložit je během kontroly zakázané", ulozit.isEnabled());
				Assert.assertTrue(panel.kontroluji.isVisible());
				Assert.assertTrue("model se mění až po kontrole", volani.isEmpty());
			});
			pustit.countDown();
			while (!ulozitPovolene(ulozit)) {
				Thread.sleep(20);
			}
			javax.swing.SwingUtilities.invokeAndWait(() -> Assert.assertFalse(panel.kontroluji.isVisible()));
			Assert.assertEquals(otevrene ? Arrays.asList("složky", "render", "mapy") : Collections.emptyList(), volani);
		} finally {
			pustit.countDown();
			JJedenSouborPanel.kontrola = puvodni;
		}
	}

	/** Uložit se povolí v done() na EDT, dotaz přes EDT tedy proběhne až po případném zápisu do modelů. */
	private static boolean ulozitPovolene(final javax.swing.JButton ulozit) throws Exception {
		final boolean[] povolene = new boolean[1];
		javax.swing.SwingUtilities.invokeAndWait(() -> povolene[0] = ulozit.isEnabled());
		return povolene[0];
	}

	private static javax.swing.JButton tlacitko(final java.awt.Container c, final String text) {
		for (final java.awt.Component k : c.getComponents()) {
			if (k instanceof javax.swing.JButton && text.equals(((javax.swing.JButton) k).getText())) {
				return (javax.swing.JButton) k;
			}
			if (k instanceof java.awt.Container) {
				final javax.swing.JButton b = tlacitko((java.awt.Container) k, text);
				if (b != null) {
					return b;
				}
			}
		}
		return null;
	}

	private Filex slozka(final String jmeno) throws Exception {
		final File f = tmp.newFolder(jmeno);
		Assert.assertTrue(f.isDirectory());
		return new Filex(f, false, true);
	}
}
