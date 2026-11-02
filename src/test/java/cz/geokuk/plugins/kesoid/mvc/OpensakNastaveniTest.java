package cz.geokuk.plugins.kesoid.mvc;

import java.io.File;
import java.util.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.MyPreferences;
import cz.geokuk.util.file.FileAndTime;
import cz.geokuk.util.file.KeFile;
import cz.geokuk.util.file.Root;

/** Výchozí datová složka OpenSAKu a „Načítat až po vybrání“ nezávisle na GSAKu. */
public class OpensakNastaveniTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final MyPreferences pref = MyPreferences.current().node("test-opensak-nastaveni");
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
	};

	private final File a = new File("/tmp/opensak/Default.db").getAbsoluteFile();
	private final File b = new File("/tmp/opensak/Nova.db").getAbsoluteFile();

	@After
	public void uklid() throws Exception {
		pref.removeNode();
	}

	@Test
	public void vychoziSlozkaPodlePlatformy() throws Exception {
		final File home = tmp.getRoot();
		Assert.assertEquals(new File(home, ".local/share/opensak"), KesoidUmisteniSouboru.vychoziSlozkaOpensaku("Linux", home, null));
		Assert.assertEquals(new File(home, "Library/Application Support/opensak"), KesoidUmisteniSouboru.vychoziSlozkaOpensaku("Mac OS X", home, null));
		final File appData = new File(home, "AppData/Roaming");
		Assert.assertEquals(new File(appData, "opensak"), KesoidUmisteniSouboru.vychoziSlozkaOpensaku("Windows 11", home, appData.getPath()));
		final File dokumenty = new File(home, "Documents/opensak");
		Assert.assertTrue(dokumenty.mkdirs());
		Assert.assertEquals("instalace z Microsoft Store", dokumenty, KesoidUmisteniSouboru.vychoziSlozkaOpensaku("Windows 11", home, appData.getPath()));
	}

	@Test
	public void vychoziSlozkaJeNeaktivni() {
		Assert.assertFalse(KesoidUmisteniSouboru.OPENSAK_DATA_DIR.isActive());
	}

	@Test
	public void novaDatabazeOpensakuSeNenacte() {
		parametry.setNoveDatabazeHned(true);
		parametry.setNoveDatabazeOpensakuHned(false);
		model.zaradOpensakDatabaze(set(a));
		Assert.assertTrue("první prohledání nic nezakáže", nacte(a));
		model.zaradOpensakDatabaze(set(a, b));
		Assert.assertTrue(nacte(a));
		Assert.assertFalse(nacte(b));
	}

	@Test
	public void volbaGsakuOpensakNeblokuje() {
		parametry.setNoveDatabazeHned(false);
		parametry.setNoveDatabazeOpensakuHned(true);
		model.zaradGsakDatabaze(set());
		model.zaradOpensakDatabaze(set(a));
		model.zaradOpensakDatabaze(set(a, b));
		Assert.assertTrue(nacte(b));
	}

	@Test
	public void seznamyZnamychDatabaziJsouOddelene() {
		parametry.setNoveDatabazeOpensakuHned(false);
		model.zaradGsakDatabaze(set(a));
		model.zaradOpensakDatabaze(set(b));
		Assert.assertTrue("první prohledání OpenSAKu nic nezakáže, i když GSAK už prohledaný byl", nacte(b));
	}

	private boolean nacte(final File f) {
		return model.maSeNacist(new KeFile(new FileAndTime(f, 0), new Root(f.getParentFile(), new Root.Def(1, null, null))));
	}

	private static Set<File> set(final File... f) {
		return new HashSet<>(Arrays.asList(f));
	}
}
