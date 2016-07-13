/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.io.File;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.program.FConst;
import cz.geokuk.core.program.UmisteniSouboru0;
import cz.geokuk.util.file.Filex;

/**
 * @author Martin Veverka
 *
 */
public class KesoidUmisteniSouboru extends UmisteniSouboru0 {

	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(KesoidUmisteniSouboru.class.getSimpleName());

	public static final Filex GEOKUK_DATA_DIR = new Filex(new File(FConst.HOME_DIR, "geokuk"), false, true);
	public static final Filex CESTY_DIR = new Filex(new File(FConst.HOME_DIR, "geokuk/cesty"), false, true);
	public static final Filex GEOGET_DATA_DIR = new Filex(new File("C:\\geoget\\data"), false, true);
	public static final Filex GSAK_DATA_DIR = new Filex(new File(FConst.HOME_DIR, "AppData/Roaming/gsak/data"), false, true);

	public static final Filex IMAGE_3RDPARTY_DIR = new Filex(new File(GEOKUK_DATA_DIR.getFile(), "images3rdParty"), false, false);
	public static final Filex IMAGE_MY_DIR = new Filex(new File(GEOKUK_DATA_DIR.getFile(), "imagesMy"), false, false);

	// public static final Filex KESOID_CACHE_DIR = new Filex(new File(PRCHAVE_DIR, "kesoids"), false, true);

	public static final Filex ANO_GGT = new Filex(new File(GEOKUK_DATA_DIR.getFile(), "lovim.ggt"), false, true);
	public static final Filex NE_GGT = new Filex(new File(GEOKUK_DATA_DIR.getFile(), "tedne.ggt"), false, true);

	private Filex kesDir;
	private Filex cestyDir;

	private Filex geogetDataDir;
	private Filex gsakDataDir;
	private Filex image3rdPartyDir;
	private Filex imageMyDir;

	private Filex neGgtFile;
	private Filex anoGgtFile;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final KesoidUmisteniSouboru that = (KesoidUmisteniSouboru) o;
		return equalsDataLocations(that) && equalsImageLocations(that) && equalsGgtLocations(that) && equalsPathLocations(that);
	}

	/** Porovná, zda se shodují umístění datových složek. */
	public boolean equalsDataLocations(final KesoidUmisteniSouboru that) {
		if (!Objects.equals(kesDir, that == null ? null : that.kesDir)) {
			return false;
		}
		if (!Objects.equals(geogetDataDir, that == null ? null : that.geogetDataDir)) {
			return false;
		}
		if (!Objects.equals(gsakDataDir, that == null ? null : that.gsakDataDir)) {
			return false;
		}
		return true;
	}

	/** Porovná, zda se shodují umístění složek s obrázky (ikonami). */
	public boolean equalsImageLocations(final KesoidUmisteniSouboru that) {
		if (!Objects.equals(image3rdPartyDir, that == null ? null : that.image3rdPartyDir)) {
			return false;
		}
		if (!Objects.equals(imageMyDir, that == null ? null : that.imageMyDir)) {
			return false;
		}
		return true;
	}

	/** Porovná, zda se shodují umístění dat výletů. */
	public boolean equalsGgtLocations(final KesoidUmisteniSouboru that) {
		if (!Objects.equals(anoGgtFile, that == null ? null : that.anoGgtFile)) {
			return false;
		}
		if (!Objects.equals(neGgtFile, that == null ? null : that.neGgtFile)) {
			return false;
		}
		return true;
	}

	/** Porovná, zda se shodují umístění složek s cestami. */
	public boolean equalsPathLocations(final KesoidUmisteniSouboru that) {
		if (!Objects.equals(cestyDir, that == null ? null : that.cestyDir)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the neGgtFile
	 */
	public Filex getAnoGgtFile() {
		check(anoGgtFile);
		return anoGgtFile;
	}

	public Filex getCestyDir() {
		check(cestyDir);
		return cestyDir;
	}

	public Filex getGeogetDataDir() {
		return geogetDataDir;
	}

	public Filex getGsakDataDir() {
		return gsakDataDir;
	}

	public Filex getImage3rdPartyDir() {
		return image3rdPartyDir;
	}

	public Filex getImageMyDir() {
		return imageMyDir;
	}

	/**
	 * @return the kesDir
	 */
	public Filex getKesDir() {
		check(kesDir);
		return kesDir;
	}

	/**
	 * @return the neGgtFile
	 */
	public Filex getNeGgtFile() {
		check(neGgtFile);
		return neGgtFile;
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
		result = prime * result + (geogetDataDir == null ? 0 : geogetDataDir.hashCode());
		result = prime * result + (image3rdPartyDir == null ? 0 : image3rdPartyDir.hashCode());
		result = prime * result + (imageMyDir == null ? 0 : imageMyDir.hashCode());
		result = prime * result + (kesDir == null ? 0 : kesDir.hashCode());
		result = prime * result + (cestyDir == null ? 0 : cestyDir.hashCode());
		result = prime * result + (neGgtFile == null ? 0 : neGgtFile.hashCode());
		result = prime * result + (anoGgtFile == null ? 0 : anoGgtFile.hashCode());
		return result;
	}

	/**
	 * @param neGgtFile
	 *            the neGgtFile to set
	 */
	public void setAnoGgtFile(final Filex anoGgtFile) {
		this.anoGgtFile = anoGgtFile;
	}

	public void setCestyDir(final Filex cestyDir) {
		this.cestyDir = cestyDir;
	}

	public void setGeogetDataDir(final Filex geogetDir) {
		geogetDataDir = geogetDir;
	}

	public void setGsakDataDir(final Filex gsakDir) {
		gsakDataDir = gsakDir;
	}

	public void setImage3rdPartyDir(final Filex image3rdPartyDir) {
		this.image3rdPartyDir = image3rdPartyDir;
	}

	public void setImageMyDir(final Filex imageMyDir) {
		this.imageMyDir = imageMyDir;
	}

	/**
	 * @param kesDir
	 *            the kesDir to set
	 */
	public void setKesDir(final Filex kesDir) {
		this.kesDir = kesDir;
	}

	/**
	 * @param neGgtFile
	 *            the neGgtFile to set
	 */
	public void setNeGgtFile(final Filex neGgtFile) {
		this.neGgtFile = neGgtFile;
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
