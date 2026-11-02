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
		Assert.assertEquals(2, pocet);
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
