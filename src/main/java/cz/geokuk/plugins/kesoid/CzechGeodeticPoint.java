package cz.geokuk.plugins.kesoid;

import javax.swing.Icon;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

public class CzechGeodeticPoint extends Kesoid {

	private double xjtsk;
	private double yjtsk;

	@Override
	public void buildGenotyp(Genom genom, Genotyp g) {
		GenotypBuilderCgp genotypBuilder = new GenotypBuilderCgp(genom, g);
		genotypBuilder.build(this);
	}

	@Override
	public EKesoidKind getKesoidKind() {
		return EKesoidKind.CGP;
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.Kesoid#prispejDoTooltipu(java.lang.StringBuilder)
	 */
	@Override
	public void prispejDoTooltipu(StringBuilder sb, Wpt wpt) {
		sb.append("<b>");
		sb.append(wpt.getName());
		sb.append("</b>  - ");
		sb.append(wpt.getNazev());
		if (! wpt.getNazev().contains(wpt.getSym())) {
			sb.append("<small>");
			sb.append(" - ");
			sb.append(wpt.getSym());
			sb.append("</small>");
		}
	}

	public double getXjtsk() {
		return xjtsk;
	}

	public void setXjtsk(double xjtsk) {
		this.xjtsk = xjtsk;
	}

	public double getYjtsk() {
		return yjtsk;
	}

	public void setYjtsk(double yjtsk) {
		this.yjtsk = yjtsk;
	}

	@Override
	public Icon getUrlIcon() {
		return ImageLoader.seekResIcon("dataz.png");
	}

}
