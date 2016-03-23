/**
 *
 */
package cz.geokuk.core.render;

import java.io.File;

import cz.geokuk.core.program.UmisteniSouboru0;
import cz.geokuk.plugins.kesoid.mvc.KesoidUmisteniSouboru;
import cz.geokuk.util.file.Filex;

/**
 * @author veverka
 *
 */
public class RenderUmisteniSouboru extends UmisteniSouboru0 {

	public static final Filex KMZ_DIR = new Filex(new File(KesoidUmisteniSouboru.GEOKUK_DATA_DIR.getFile(), "kmz"), false, true);

	public static final Filex OZI_DIR = new Filex(new File(KesoidUmisteniSouboru.GEOKUK_DATA_DIR.getFile(), "ozi"), false, true);

	public static final Filex PICURE_DIR = new Filex(new File(KesoidUmisteniSouboru.GEOKUK_DATA_DIR.getFile(), "picture"), false, true);

	private Filex kmzDir;

	private Filex oziDir;

	private Filex pictureDir;

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
		final RenderUmisteniSouboru other = (RenderUmisteniSouboru) obj;
		if (kmzDir == null) {
			if (other.kmzDir != null) {
				return false;
			}
		} else if (!kmzDir.equals(other.kmzDir)) {
			return false;
		}
		if (oziDir == null) {
			if (other.oziDir != null) {
				return false;
			}
		} else if (!oziDir.equals(other.oziDir)) {
			return false;
		}
		if (pictureDir == null) {
			if (other.pictureDir != null) {
				return false;
			}
		} else if (!pictureDir.equals(other.pictureDir)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the kmzDir
	 */
	public Filex getKmzDir() {
		return kmzDir;
	}

	/**
	 * @return the oziDir
	 */
	public Filex getOziDir() {
		return oziDir;
	}

	public Filex getPictureDir() {
		return pictureDir;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (kmzDir == null ? 0 : kmzDir.hashCode());
		result = prime * result + (oziDir == null ? 0 : oziDir.hashCode());
		result = prime * result + (pictureDir == null ? 0 : pictureDir.hashCode());
		return result;
	}

	/**
	 * @param kmzDir
	 *            the kmzDir to set
	 */
	public void setKmzDir(final Filex kmzDir) {
		check(kmzDir);
		this.kmzDir = kmzDir;
	}

	/**
	 * @param oziDir
	 *            the oziDir to set
	 */
	public void setOziDir(final Filex oziDir) {
		check(oziDir);
		this.oziDir = oziDir;
	}

	public void setPictureDir(final Filex pictureDir) {
		check(pictureDir);
		this.pictureDir = pictureDir;
	}

	/**
	 * @param aKesDir
	 */
	private void check(final Filex file) {
		if (file == null) {
			throw new RuntimeException("Jmena souboru jeste nebyla inicializovana.");
		}
	}

}
