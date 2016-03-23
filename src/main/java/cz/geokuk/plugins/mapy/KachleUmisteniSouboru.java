/**
 *
 */
package cz.geokuk.plugins.mapy;

import java.io.File;

import cz.geokuk.core.program.UmisteniSouboru0;
import cz.geokuk.plugins.kesoid.mvc.KesoidUmisteniSouboru;
import cz.geokuk.util.file.Filex;

/**
 * @author veverka
 *
 */
public class KachleUmisteniSouboru extends UmisteniSouboru0 {

	public static final File	PRCHAVE_DIR			= new File(KesoidUmisteniSouboru.GEOKUK_DATA_DIR.getFile(), "prchave");	// keš, kterou je vždymožno smazat

	public static final Filex	KACHLE_CACHE_DIR	= new Filex(new File(PRCHAVE_DIR, "kachle"), false, true);

	private Filex				kachleCacheDir;

	/**
	 * @return the kachleCacheDir
	 */
	public Filex getKachleCacheDir() {
		return kachleCacheDir;
	}

	/**
	 * @param aKesDir
	 */
	private void check(final Filex file) {
		if (file == null) {
			throw new RuntimeException("Jmena souboru jeste nebyla inicializovana.");
		}
	}

	/**
	 * @param kachleCacheDir
	 *            the kachleCacheDir to set
	 */
	public void setKachleCacheDir(final Filex kachleCacheDir) {
		check(kachleCacheDir);
		this.kachleCacheDir = kachleCacheDir;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kachleCacheDir == null) ? 0 : kachleCacheDir.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final KachleUmisteniSouboru other = (KachleUmisteniSouboru) obj;
		if (kachleCacheDir == null) {
			if (other.kachleCacheDir != null) {
				return false;
			}
		} else if (!kachleCacheDir.equals(other.kachleCacheDir)) {
			return false;
		}
		return true;
	}

}
