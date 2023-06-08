package cz.geokuk.plugins.mapy.kachle.data;

import java.net.MalformedURLException;
import java.net.URL;

import lombok.Data;

/**
 * Nese kompletní informaci o kachli, lokaci, měřítko i typ.
 *
 * @author veverka
 *
 */
@Data
public class Ka {

	// private int DOPLNKOVAC = 1<<28;

	private final KaLoc loc;
	private final KaType type;

	@Override
	public String toString() {
		return getLoc().toString() + "*" + type.getId();
	}

	public String typToId() {
		return type.getId();
	}

	/** Zbuilduje URL ke kachli 256*256 mapy */
	public URL getUrl() {
		try {
			return type.getUrlBuilder().buildUrl(this);
		} catch (final MalformedURLException e) {
			// nemůže nastat
			throw new RuntimeException(e);
		}

	}

}
