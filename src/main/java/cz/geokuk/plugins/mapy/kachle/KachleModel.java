/**
 *
 */
package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.core.onoffline.OnofflineModelChangeEvent;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboru;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboruChangedEvent;
import cz.geokuk.plugins.mapy.kachle.podklady.*;

/**
 * @author Martin Veverka
 *
 */
public class KachleModel extends Model0 {

	private final KachleCacheFolderHolder kachleCacheFolderHolder = new KachleCacheFolderHolder();

	public KachloDownloader kachloDownloader;

	private Object umisteniSouboru;

	private KachleZiskavac ziskavac;

	public final KachleManager kachleManager;

	/**
	 * @param bb
	 */
	public KachleModel() {
		kachleManager = KachleManagerFactory.getInstance(getKachleCacheFolderHolder());
		assert kachleCacheFolderHolder != null;
	}

	/**
	 * @return the kachleCacheFolderHolder
	 */
	public KachleCacheFolderHolder getKachleCacheFolderHolder() {
		assert kachleCacheFolderHolder != null;
		return kachleCacheFolderHolder;
	}

	public KachleZiskavac getZiskavac() {
		return ziskavac;
	}

	public void inject(final KachleZiskavac kachleZiskavac) {
		ziskavac = kachleZiskavac;
		ziskavac.setKachleManager(kachleManager);
	}

	public void inject(final KachloDownloader kachloDownloader) {
		this.kachloDownloader = kachloDownloader;
	}

	/**
	 * @return the ukladatMapyNaDisk
	 */
	public boolean isUkladatMapyNaDisk() {
		// return Settings.vseobecne.ukladatMapyNaDisk.isSelected();
		final boolean b = currPrefe().getBoolean("ukladatMapyNaDisk", true);
		return b;
	}

	public void onEvent(final OnofflineModelChangeEvent eve) {
		if (eve.isOnlineMOde()) {
			ziskavac.clearMemoryCache();
		}
	}

	/**
	 * @param ukladatMapyNaDisk
	 *            the ukladatMapyNaDisk to set
	 */
	public void setUkladatMapyNaDisk(final boolean ukladatMapyNaDisk) {
		if (ukladatMapyNaDisk == isUkladatMapyNaDisk()) {
			return;
		}
		currPrefe().putBoolean("ukladatMapyNaDisk", ukladatMapyNaDisk);
		fire(new KachleModelChangeEvent());
	}

	/**
	 * @param umisteniSouboru
	 *            the umisteniSouboru to set
	 */
	public void setUmisteniSouboru(final KachleUmisteniSouboru umisteniSouboru) {
		if (umisteniSouboru.equals(this.umisteniSouboru)) {
			return;
		}
		this.umisteniSouboru = umisteniSouboru;
		kachleCacheFolderHolder.setKachleCacheDir(umisteniSouboru.getKachleCacheDir());
		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		pref.putFilex(FPref.KACHLE_CACHE_DIR_value, umisteniSouboru.getKachleCacheDir());
		pref.remove("vyjimkyDir"); // mazat ze starych verzi
		fire(new KachleUmisteniSouboruChangedEvent(umisteniSouboru));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
		setUmisteniSouboru(loadUmisteniSouboru());
		fire(new KachleModelChangeEvent());
	}

	private KachleUmisteniSouboru loadUmisteniSouboru() {
		final KachleUmisteniSouboru u = new KachleUmisteniSouboru();
		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		u.setKachleCacheDir(pref.getFilex("kachleCacheDir", KachleUmisteniSouboru.KACHLE_CACHE_DIR));
		return u;
	}
}
