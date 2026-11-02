package cz.geokuk.plugins.kesoid.mvc;

import java.io.File;
import java.util.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.MyPreferences;
import cz.geokuk.util.file.FileAndTime;
import cz.geokuk.util.file.Filex;
import cz.geokuk.util.file.KeFile;
import cz.geokuk.util.file.Root;

/** „Načítat až po vybrání“: databázi GSAKu, kterou GeoKuk ještě neviděl, načte až po vybrání uživatelem. */
public class GsakNacitatPoVybraniTest {

	private final MyPreferences pref = MyPreferences.current().node("test-gsak-po-vybrani");
	private final GsakParametryNacitani parametry = new GsakParametryNacitani();
	private final KesoidModel model = new KesoidModel() {
		@Override
		protected MyPreferences currPrefe() {
			return pref;
		}

		@Override
		public GsakParametryNacitani getGsakParametryNacitani() {
			return parametry;
		}

		@Override
		public KesoidUmisteniSouboru getUmisteniSouboru() {
			dotazuNaUmisteni++;
			return umisteni;
		}
	};
	private KesoidUmisteniSouboru umisteni;
	private int dotazuNaUmisteni;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final File a = new File("/tmp/gsak/data/Default/sqlite.db3").getAbsoluteFile();
	private final File b = new File("/tmp/gsak/data/Nova/sqlite.db3").getAbsoluteFile();

	@After
	public void uklid() throws Exception {
		pref.removeNode();
	}

	private boolean nacte(final File f) {
		return model.maSeNacist(new KeFile(new FileAndTime(f, 0), new Root(f.getParentFile().getParentFile(), new Root.Def(2, null, null))));
	}

	@Test
	public void prvniProhledaniNicNezakaze() {
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(a));
		Assert.assertTrue(nacte(a));
	}

	@Test
	public void novaDatabazeVGsakuSeNenacte() {
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(a));
		model.zaradGsakDatabaze(set(a, b));
		Assert.assertTrue(nacte(a));
		Assert.assertFalse(nacte(b));
	}

	@Test
	public void poZapnutiGsakuSeDatabazeNenactou() {
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(a, b));
		model.zaradGsakDatabaze(set()); // GSAK neaktivní
		model.zaradGsakDatabaze(set(a, b));
		Assert.assertFalse(nacte(a));
		Assert.assertFalse(nacte(b));
	}

	@Test
	public void priNacitaniVsehoSeNicNezakaze() {
		parametry.setNoveDatabazeHned(true);
		model.zaradGsakDatabaze(set(a));
		model.zaradGsakDatabaze(set(a, b));
		Assert.assertTrue(nacte(b));
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(a, b));
		Assert.assertTrue(nacte(b));
	}

	/** Zablokovaná databáze zůstane zablokovaná, i když byla složka GSAKu chvíli nedostupná. */
	@Test
	public void blokovanaZustanePoNedostupneSlozce() throws Exception {
		final File slozka = new File(tmp.getRoot(), "gsak");
		final File stara = new File(slozka, "Default/sqlite.db3");
		final File treti = new File(slozka, "Treti/sqlite.db3");
		umisteni = new KesoidUmisteniSouboru();
		umisteni.setKesDir(new Filex(tmp.newFolder("gpx"), false, true));
		umisteni.setGeogetDataDir(new Filex(new File(tmp.getRoot(), "geoget"), false, false));
		umisteni.setGsakDataDir(new Filex(slozka, false, true));
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(stara));
		model.zaradGsakDatabaze(set(stara, treti));
		Assert.assertFalse(nacte(treti));

		model.setNedostupnePriNacitani(set(slozka)); // sken složku nepřečetl
		model.vycistiBlokovaneZdroje(set()); // složka nedostupná, nic se nenačetlo
		Assert.assertFalse(nacte(treti));

		Assert.assertTrue(new File(slozka, "Treti").mkdirs());
		model.setNedostupnePriNacitani(set());
		model.vycistiBlokovaneZdroje(set(stara, treti));
		Assert.assertFalse(nacte(treti));

		model.vycistiBlokovaneZdroje(set(stara)); // databáze ve složce, která je k dispozici, zmizela
		Assert.assertTrue(nacte(treti));
	}

	/** Databáze ve složce GSAKu zadané přes symbolický odkaz se při dalším prohledání znovu nezablokuje. */
	@Test
	public void znamaDatabazePresOdkazSeNezablokuje() throws Exception {
		final File skutecna = tmp.newFolder("skutecna");
		final File odkaz = new File(tmp.getRoot(), "odkaz");
		try {
			java.nio.file.Files.createSymbolicLink(odkaz.toPath(), skutecna.toPath());
		} catch (final UnsupportedOperationException | java.io.IOException e) {
			Assume.assumeNoException("symbolický odkaz nejde vytvořit", e);
		}
		final File db = new File(odkaz, "Default/sqlite.db3").getAbsoluteFile();
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(db));
		model.zaradGsakDatabaze(set(db));
		Assert.assertTrue(nacte(db));
	}

	/** Neplatná cesta k aktivní složce (ručně upravené nastavení) nesmí shodit úklid zablokovaných zdrojů. */
	@Test
	public void neplatnaSlozkaNevadi() throws Exception {
		umisteni = new KesoidUmisteniSouboru();
		umisteni.setKesDir(new Filex(new File(tmp.getRoot(), "a\0b"), false, true));
		umisteni.setGeogetDataDir(new Filex(new File(tmp.getRoot(), "geoget"), false, false));
		umisteni.setGsakDataDir(new Filex(new File(tmp.getRoot(), "gsak"), false, false));
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(a));
		model.zaradGsakDatabaze(set(a, b)); // b je zablokovaná, úklid má co dělat
		model.setNedostupnePriNacitani(set(new File(tmp.getRoot(), "a\0b")));
		model.vycistiBlokovaneZdroje(set());
	}

	/** Úklid zablokovaných zdrojů po načtení běží na EDT, dostupnost složek bere ze skenu; neodpovídající síťový disk by EDT zablokoval. */
	@Test
	public void sBlokovanymiSeSlozkyNaEdtNekontroluji() throws Exception {
		umisteni = new KesoidUmisteniSouboru();
		umisteni.setKesDir(new Filex(tmp.newFolder("gpx"), false, true));
		umisteni.setGeogetDataDir(new Filex(new File(tmp.getRoot(), "geoget"), false, true));
		umisteni.setGsakDataDir(new Filex(new File(tmp.getRoot(), "gsak"), false, true));
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(a));
		model.zaradGsakDatabaze(set(a, b));
		final int predUklidem = dotazuNaUmisteni;
		model.vycistiBlokovaneZdroje(set(a));
		Assert.assertEquals(predUklidem, dotazuNaUmisteni);
		Assert.assertTrue("b zmizela z dostupné složky", nacte(b));
	}

	/** Bez zablokovaných zdrojů se po načtení nezjišťuje dostupnost složek (souborové operace na EDT). */
	@Test
	public void bezBlokovanychSeSlozkyNekontroluji() throws Exception {
		umisteni = new KesoidUmisteniSouboru();
		umisteni.setKesDir(new Filex(tmp.newFolder("gpx"), false, true));
		umisteni.setGeogetDataDir(new Filex(new File(tmp.getRoot(), "geoget"), false, false));
		umisteni.setGsakDataDir(new Filex(new File(tmp.getRoot(), "gsak"), false, true));
		model.vycistiBlokovaneZdroje(set());
		Assert.assertEquals(0, dotazuNaUmisteni);
	}

	/** Podsložky byly při prohledání nečitelné a pak se vrátily: vybraná databáze zůstane vybraná, zablokovaná zablokovaná. */
	@Test
	public void necitelnaPodslozkaPriSkenuNicNezapomene() throws Exception {
		final File slozka = tmp.newFolder("gsak").getCanonicalFile(); // nastavení ukládá kanonické cesty (Windows: RUNNER~1)
		final File vybrana = new File(slozka, "Default/sqlite.db3");
		final File treti = new File(slozka, "Treti/sqlite.db3");
		umisteni = new KesoidUmisteniSouboru();
		umisteni.setKesDir(new Filex(tmp.newFolder("gpx"), false, true));
		umisteni.setGeogetDataDir(new Filex(new File(tmp.getRoot(), "geoget"), false, false));
		umisteni.setGsakDataDir(new Filex(slozka, false, true));
		parametry.setNoveDatabazeHned(false);
		model.zaradGsakDatabaze(set(vybrana));
		model.zaradGsakDatabaze(set(vybrana, treti));
		Assert.assertTrue(nacte(vybrana));
		Assert.assertFalse(nacte(treti));

		final Set<File> nedostupne = set(vybrana.getParentFile(), treti.getParentFile());
		model.setNedostupnePriNacitani(nedostupne);
		model.zaradGsakDatabaze(set(), nedostupne);
		Assert.assertTrue(new File(slozka, "Treti").mkdirs()); // složka je zpátky dřív, než se uklízí
		model.vycistiBlokovaneZdroje(set());
		Assert.assertFalse(nacte(treti));

		model.setNedostupnePriNacitani(set());
		model.zaradGsakDatabaze(set(vybrana, treti), set());
		model.vycistiBlokovaneZdroje(set(vybrana, treti));
		Assert.assertTrue(nacte(vybrana));
		Assert.assertFalse(nacte(treti));
	}

	private static Set<File> set(final File... f) {
		return new HashSet<>(Arrays.asList(f));
	}
}
