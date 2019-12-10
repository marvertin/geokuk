package cz.geokuk.plugins.kesoid;

import javax.swing.Icon;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

public class CzechGeodeticPoint extends Kesoid {

	private double xjtsk;
	private double yjtsk;

	@Override
	public Genotyp buildGenotyp(final Genotyp g) {
		final GenotypBuilderCgp genotypBuilder = new GenotypBuilderCgp(g.getGenom());
		return genotypBuilder.build(this, g);
	}

	@Override
	public EKesoidKind getKesoidKind() {
		return EKesoidKind.CGP;
	}

	@Override
	public Icon getUrlIcon() {
		return ImageLoader.seekResIcon("dataz.png");
	}

	public double getXjtsk() {
		return xjtsk;
	}

	public double getYjtsk() {
		return yjtsk;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.Kesoid#prispejDoTooltipu(java.lang.StringBuilder)
	 */
	@Override
	public void prispejDoTooltipu(final StringBuilder sb, final Wpt wpt) {
		sb.append("<b>");
		sb.append(wpt.getName());
		sb.append("</b>  - ");
		sb.append(wpt.getNazev());
		if (!wpt.getNazev().contains(wpt.getSym())) {
			sb.append("<small>");
			sb.append(" - ");
			sb.append(wpt.getSym());
			sb.append("</small>");
		}
	}

	public void setXjtsk(final double xjtsk) {
		this.xjtsk = xjtsk;
	}

	public void setYjtsk(final double yjtsk) {
		this.yjtsk = yjtsk;
	}

}
