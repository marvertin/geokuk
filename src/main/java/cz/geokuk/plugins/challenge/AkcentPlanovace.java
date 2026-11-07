package cz.geokuk.plugins.challenge;

import java.awt.Color;

import javax.swing.UIManager;

/** Barva značek plánovače na mapě: akcent aktuálního tématu, ztmavený a sytější, aby byl čitelný na světlé mapě. */
final class AkcentPlanovace {

	private static final Color ZALOHA = new Color(0x2F4F7F);

	private AkcentPlanovace() {
	}

	static Color barva() {
		final Color t = UIManager.getColor("Slider.foreground");
		if (t == null) {
			return ZALOHA;
		}
		final float[] h = Color.RGBtoHSB(t.getRed(), t.getGreen(), t.getBlue(), null);
		return Color.getHSBColor(h[0], Math.min(1f, h[1] * 1.6f + 0.2f), h[2] * 0.45f);
	}

	static Color pozadiStitku() {
		final Color c = UIManager.getColor("ToolTip.background");
		return c == null ? Color.WHITE : c;
	}

	static Color textStitku() {
		final Color c = UIManager.getColor("ToolTip.foreground");
		return c == null ? Color.BLACK : c;
	}
}
