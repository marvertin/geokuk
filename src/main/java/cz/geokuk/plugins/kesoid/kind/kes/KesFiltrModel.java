package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.Model0;

public class KesFiltrModel extends Model0 {

	private static final String KES_FILTER_node = "kesFilter";

	private KesFilterDefinition kesFilterDefinition;

	public void onEvent(final KesFilterDefinition event) {
//		onlineMode = event.isOnlineMOde();
	}

	public void setKesFilderDefinition(final KesFilterDefinition kesFilterDefinition) {
		if (! kesFilterDefinition.equals(this.kesFilterDefinition)) {
			currPrefe()
			.node(KesPlugin.PREF_KES_PLUGIN_node)
			.putStructure(KES_FILTER_node, kesFilterDefinition);
			this.kesFilterDefinition = kesFilterDefinition;
		}
	}

	@Override
	protected void initAndFire() {
		kesFilterDefinition = currPrefe()
				.node(KesPlugin.PREF_KES_PLUGIN_node)
				.getStructure(KES_FILTER_node, new KesFilterDefinition());
		fire(new KesFilterChangeEvent(kesFilterDefinition));
	}
}
