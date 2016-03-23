package cz.geokuk.plugins.mapy.kachle;

import java.awt.Image;

public class KachloStav {
	public final EFaze		faze;
	public final Image		img;
	public final Throwable	thr;
	public final byte[]		imageData;

	public KachloStav(final EFaze faze, final Image img) {
		this(faze, img, null, null);
	}

	public KachloStav(final EFaze faze, final Image img, final byte[] imageData) {
		this(faze, img, imageData, null);
	}

	public KachloStav(final EFaze faze, final Throwable thr) {
		this(faze, null, null, thr);
	}

	public KachloStav(final EFaze faze, final Image img, final Throwable thr) {
		this(faze, img, null, thr);
	}

	private KachloStav(final EFaze faze, final Image img, final byte[] imageData, final Throwable thr) {
		this.faze = faze;
		this.img = img;
		this.imageData = imageData;
		this.thr = thr;
	}

	public static enum EFaze {
		RESULT_ONE,

		RESULT_ALL_PRUBEH, RESULT_ALL_POSLEDNI,;
	}

	@Override
	public String toString() {
		return "KachloStav [faze=" + faze + ", img=" + img + ", thr=" + thr + "]";
	}
}