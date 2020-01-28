package cz.geokuk.plugins.kesoid.kind;

import java.util.Comparator;

public class KesoidPluginComparator implements Comparator<KesoidPlugin> {

	@Override
	public int compare(final KesoidPlugin o1, final KesoidPlugin o2) {

		return o1.getOrder() - o2.getOrder();
	}

}
