package cz.geokuk.plugins.kesoid.kind.waymark;

import java.net.URL;

import cz.geokuk.plugins.kesoid.Kesoid0;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;
import lombok.SneakyThrows;

public class Waymark extends Kesoid0 {

	@Override
	public Genotyp buildGenotyp(final Genotyp g) {
		final GenotypBuilderWaymark genotypBuilder = new GenotypBuilderWaymark(g.getGenom());
		return genotypBuilder.build(this, g);
	}

	@Override
	public EKesoidKind getKesoidKind() {
		return EKesoidKind.WAYMARK;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.Kesoid#prispejDoTooltipu(java.lang.StringBuilder)
	 */
	@Override
	public void prispejDoTooltipu(final StringBuilder sb, final Wpt wpt) {
		sb.append("<b>");
		sb.append(getNazev());
		sb.append("</b>");
		sb.append("<small>");
		sb.append(" - ");
		sb.append(getFirstWpt().getSym());
		sb.append("  (").append(getIdentifier()).append(")");
		sb.append("</small>");
		sb.append("<br>");
		if (wpt != getFirstWpt()) {
			if (!getNazev().contains(wpt.getNazev())) {
				sb.append(wpt.isRucnePridany() ? "+ " : "");
				sb.append("<i>");
				sb.append(wpt.getIdentifier().substring(0, 2));
				sb.append(": ");
				sb.append(wpt.getNazev());
				sb.append("</i>");
			}
			// if (! getSym().equals(wpt.getSym())) {
			sb.append("<small>");
			sb.append(" - ");
			sb.append(wpt.getSym());
			sb.append("  (").append(wpt.getIdentifier()).append(")");
			sb.append("</small>");
		}
	}


	@Override
	@SneakyThrows
	public URL getUrlProPridaniDoSeznamuVGeogetu() {
		return new URL("https://www.waymarking.com/waymarks/" + getIdentifier());
	}


}
