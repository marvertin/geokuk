package cz.geokuk.api.mapicon;


import java.awt.Color;
import java.net.URL;
import java.util.Deque;
import java.util.Properties;

import cz.geokuk.plugins.kesoid.mapicon.IkonDrawingProperties;

public abstract class Drawer0 {

	private IkonDrawingProperties idp;

	/**
	 * @param aIdp the idp to set
	 */
	public void setIdp(IkonDrawingProperties aIdp) {
		idp = aIdp;
	}

	/**
	 * Vykreslí zdrojový obrázek do dané vrstvy.
	 * Obrázek přidá do seznamu,
	 * @param src
	 * @param dest
	 */
	public abstract void draw(Deque<Imagant> imaganti);

	/**
	 * @return
	 */
	protected final int getYoffset() {
		return idp.yoffset;
	}

	/**
	 * @return
	 */
	protected final int getXoffset() {
		return idp.xoffset;
	}

	/**
	 * @return
	 */
	protected final URL getUrl() {
		return idp.url;
	}

	protected final Properties getProperties() {
		return idp.properties;
	}

	protected final String getString(String propname, String def) {
		String s = (String) idp.properties.get(propname);
		return s == null ? def : s;
	}

	protected final int getInt(String propname, int def) {
		return Integer.parseInt(getString(propname, def + "").trim());
	}

	protected final float getFloat(String propname, float def) {
		return Float.parseFloat(getString(propname, def + ""));
	}

	protected final String[] getStrings(String propname, String[] def) {
		String s = getString(propname, null);
		if (s == null) return def;
		String[] ss = s.split(" *, *");
		return ss;
	}

	protected final int[] getInts(String propname, int[] def) {
		String[] ss = getStrings(propname, null);
		if (ss == null) return def;
		int[] ii = new int[ss.length];
		for (int i=0; i<ss.length; i++) {
			ii[i] = Integer.parseInt(ss[i]);
		}
		return ii;
	}

	protected final float[] getFloats(String propname, float[] def) {
		String[] ss = getStrings(propname, null);
		if (ss == null) return def;
		float[] ff = new float[ss.length];
		for (int i=0; i<ss.length; i++) {
			ff[i] = Float.parseFloat(ss[i]);
		}
		return ff;
	}

	protected final Color getColor(String propname, Color def) {
		int[] ii = getInts(propname, null);
		if (ii == null || ii.length == 0) return def;
		if (ii.length < 3) {
			return new Color(ii[0], ii[0], ii[0]);
		} else if (ii.length == 3) {
			return new Color(ii[0], ii[1], ii[2]);
		} else {
			return new Color(ii[0], ii[1], ii[2], ii[3]);
		}
	}





}
