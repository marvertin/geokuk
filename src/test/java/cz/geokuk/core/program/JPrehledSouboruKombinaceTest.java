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

/** Uložit v Umístění souborů se všemi osmi složkami: jedna vadná složka zastaví celé uložení, jinak se uloží přesně zadané cesty. */
public class JPrehledSouboruKombinaceTest {

	private static final String[] SLOZKY = { "kese", "geoget", "gsak", "opensak", "mapy", "ozi", "kmz", "obrazky" };
	private static final Set<String> JINE_PROGRAMY = new HashSet<>(Arrays.asList("geoget", "gsak", "opensak"));

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final List<String> volani = new ArrayList<>();
	private KesoidUmisteniSouboru ulozeneKesoid;
	private RenderUmisteniSouboru ulozeneRender;
	private KachleUmisteniSouboru ulozeneKachle;

	@Test
	public void jednaVadnaSlozkaNicNeulozi() throws Exception {
		for (final String vadna : SLOZKY) {
			volani.clear();
			final File koren = tmp.newFolder("vadna-" + vadna);
			final Map<String, File> cesty = new LinkedHashMap<>();
			for (final String s : SLOZKY) {
				cesty.put(s, existujici(koren, s));
			}
			// Jiný program: složka neexistuje. Vlastní složka: nejde založit, protože místo nadřazené složky je soubor.
			final File rodic = new File(koren, "soubor");
			rodic.createNewFile();
			cesty.put(vadna, JINE_PROGRAMY.contains(vadna) ? new File(koren, "neni/" + vadna) : new File(rodic, vadna));
			final JPrehledSouboru panel = panel(cesty, Collections.emptySet());
			try {
				panel.uloz();
				Assert.fail(vadna + ": uložení mělo skončit chybou");
			} catch (final JPrehledSouboru.YNejdeTo e) {
				Assert.assertTrue(vadna + ": " + e.getMessage(), e.getMessage().contains(cesty.get(vadna).getName()));
			}
			Assert.assertEquals(vadna + ": do modelů se nesmí nic zapsat", Collections.emptyList(), volani);
		}
	}

	@Test
	public void spravneSlozkySDiakritikouSeUlozi() throws Exception {
		final File koren = tmp.newFolder("Příliš žluťoučký kůň");
		final Map<String, File> cesty = new LinkedHashMap<>();
		for (final String s : SLOZKY) {
			// Vlastní složky neexistují a založí se, složky jiných programů existují (prázdné).
			cesty.put(s, JINE_PROGRAMY.contains(s) ? existujici(koren, "Nová ěščřž " + s) : new File(koren, "Kešky č. 1/" + s));
		}
		final JPrehledSouboru panel = panel(cesty, Collections.emptySet());
		panel.uloz();
		Assert.assertEquals(Arrays.asList("gsak", "složky", "render", "mapy"), volani);
		for (final File f : cesty.values()) {
			Assert.assertTrue(f.toString(), f.isDirectory());
		}
		Assert.assertEquals(cesty.get("kese"), ulozeneKesoid.getKesDir().getEffectiveFile());
		Assert.assertEquals(cesty.get("geoget"), ulozeneKesoid.getGeogetDataDir().getEffectiveFile());
		Assert.assertEquals(cesty.get("gsak"), ulozeneKesoid.getGsakDataDir().getEffectiveFile());
		Assert.assertEquals(cesty.get("opensak"), ulozeneKesoid.getOpensakDataDir().getEffectiveFile());
		Assert.assertEquals(cesty.get("ozi"), ulozeneRender.getOziDir().getEffectiveFile());
		Assert.assertEquals(cesty.get("kmz"), ulozeneRender.getKmzDir().getEffectiveFile());
		Assert.assertEquals(cesty.get("obrazky"), ulozeneRender.getPictureDir().getEffectiveFile());
		Assert.assertEquals(cesty.get("mapy"), ulozeneKachle.getOfflineMapyDir().getEffectiveFile());
	}

	@Test
	public void vypnuteNeexistujiciSlozkyJinychProgramuSeUlozi() throws Exception {
		final File koren = tmp.newFolder("bez-programu");
		final Map<String, File> cesty = new LinkedHashMap<>();
		for (final String s : SLOZKY) {
			cesty.put(s, JINE_PROGRAMY.contains(s) ? new File(koren, "neni/" + s) : existujici(koren, s));
		}
		final JPrehledSouboru panel = panel(cesty, JINE_PROGRAMY);
		panel.uloz();
		Assert.assertEquals(Arrays.asList("gsak", "složky", "render", "mapy"), volani);
		Assert.assertFalse("vypnuté složky se nezakládají", new File(koren, "neni").exists());
		Assert.assertFalse(ulozeneKesoid.getGeogetDataDir().isActive());
		Assert.assertFalse(ulozeneKesoid.getGsakDataDir().isActive());
		Assert.assertFalse(ulozeneKesoid.getOpensakDataDir().isActive());
	}

	private static File existujici(final File koren, final String jmeno) {
		final File f = new File(koren, jmeno);
		Assert.assertTrue(f.mkdirs());
		return f;
	}

	private JPrehledSouboru panel(final Map<String, File> cesty, final Set<String> vypnute) {
		final JPrehledSouboru panel = new JPrehledSouboru(null);
		panel.inject(new KesoidModel() {
			@Override
			public void setGsakParametryNacitani(final GsakParametryNacitani g) {
				volani.add("gsak");
			}

			@Override
			public void setUmisteniSouboru(final KesoidUmisteniSouboru u) {
				ulozeneKesoid = u;
				volani.add("složky");
			}
		});
		panel.inject(new RenderModel() {
			@Override
			public void setUmisteniSouboru(final RenderUmisteniSouboru u) {
				ulozeneRender = u;
				volani.add("render");
			}
		});
		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		u.setKesDir(new Filex(cesty.get("kese"), false, true));
		u.setGeogetDataDir(new Filex(cesty.get("geoget"), false, !vypnute.contains("geoget")));
		u.setGsakDataDir(new Filex(cesty.get("gsak"), false, !vypnute.contains("gsak")));
		u.setOpensakDataDir(new Filex(cesty.get("opensak"), false, !vypnute.contains("opensak")));
		panel.onEvent(new KesoidUmisteniSouboruChangedEvent(u));
		final RenderUmisteniSouboru r = new RenderUmisteniSouboru();
		r.setOziDir(new Filex(cesty.get("ozi"), false, true));
		r.setKmzDir(new Filex(cesty.get("kmz"), false, true));
		r.setPictureDir(new Filex(cesty.get("obrazky"), false, true));
		panel.onEvent(new RenderUmisteniSouboruChangedEvent(r));
		panel.inject(new KachleModel() {
			@Override
			public void setUmisteniSouboru(final KachleUmisteniSouboru u) {
				ulozeneKachle = u;
				volani.add("mapy");
			}
		});
		final KachleUmisteniSouboru k = new KachleUmisteniSouboru();
		k.setKachleCacheDir(KachleUmisteniSouboru.KACHLE_CACHE_DIR);
		k.setOfflineMapyDir(new Filex(cesty.get("mapy"), false, true));
		panel.onEvent(new KachleUmisteniSouboruChangedEvent(k));
		final GsakParametryNacitani g = new GsakParametryNacitani();
		g.setCasNalezu(Collections.emptySet());
		panel.onEvent(new GsakParametryNacitaniChangedEvent(g));
		return panel;
	}
}
