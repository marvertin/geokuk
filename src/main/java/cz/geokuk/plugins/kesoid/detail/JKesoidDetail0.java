package cz.geokuk.plugins.kesoid.detail;

import javax.swing.JPanel;

import cz.geokuk.plugins.kesoid.Wpt;

public abstract class JKesoidDetail0 extends JPanel {

	private static final long serialVersionUID = 1L;

	public abstract void napln(Wpt wpt);

	public static String formatujDatum(String s) {
		if (s == null) {
			return s;
		}
		s = s.trim();
		if (s.length() == 8) {
			final String cc = s.substring(6, 8) + "." + s.substring(4, 6) + "." + s.substring(0, 4);
			return cc;
		}
		if (s.length() < 10) {
			return s;
		}
		final String x = s.substring(8, 10) + "." + s.substring(5, 7) + "." + s.substring(0, 4);
		return x;
	}

}
