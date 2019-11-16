package cz.geokuk.plugins.mapy.kachle.podklady;

import java.awt.Image;

import lombok.Data;

@Data
public class KachloStav {
	private final Image img;
	private final Throwable thr;

	public KachloStav(final Image img) {
		this.img = img;
		this.thr = null;
	}

	public KachloStav(final Throwable thr) {
		this.img = null;
		this.thr = thr;
	}

}