/**
 *
 */
package cz.geokuk.plugins.mapy.kachle.podklady;

import cz.geokuk.util.file.Filex;

/**
 * @author Martin Veverka
 *
 */
public class KachleCacheFolderHolder {

	private Filex kachleCacheDir;

	public Filex getKachleCacheFolder() {
		return kachleCacheDir;
	}

	/**
	 * @param kachleCacheDir
	 */
	public void setKachleCacheDir(final Filex kachleCacheDir) {
		this.kachleCacheDir = kachleCacheDir;
	}
}
