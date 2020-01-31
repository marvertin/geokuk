package cz.geokuk.plugins.kesoid.kind.kes;

import java.util.function.UnaryOperator;

import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import lombok.Getter;

public class KesFilterModel extends Model0 {

	private static final String KES_FILTER_node = "kesFilter";

	@Getter
	private KesFilterDefinition kesFilterDefinition;

	@Getter
	private KesFilter kesFilter;

	private KesoidModel kesoidModel;

	public void onEvent(final KesFilterDefinitionChangedEvent event) {
		kesFilter = new KesFilter(event.getKesFilterDefinition());
		kesoidModel.spustFiltrovani();
	}

	private void setKesFilterDefinition(final KesFilterDefinition kesFilterDefinition) {
		if (! kesFilterDefinition.equals(this.kesFilterDefinition)) {
			currPrefe()
			.node(KesPlugin.PREF_KES_PLUGIN_node)
			.putStructure(KES_FILTER_node, kesFilterDefinition);
			this.kesFilterDefinition = kesFilterDefinition;
			fire(new KesFilterDefinitionChangedEvent(kesFilterDefinition));
		}
	}

	/**
	 * Provede mofifikaci modelu zadanou funkcí.
	 * @param fce Fukce, kerá dostane stávající strukturu a vrátí novou.
	 */
	public void modify(final UnaryOperator<KesFilterDefinition> fce) {
		setKesFilterDefinition(fce.apply(kesFilterDefinition));
	}

	@Override
	protected void initAndFire() {
		kesFilterDefinition = currPrefe()
				.node(KesPlugin.PREF_KES_PLUGIN_node)
				.getStructure(KES_FILTER_node, new KesFilterDefinition());
		fire(new KesFilterDefinitionChangedEvent(kesFilterDefinition));
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel =  kesoidModel;
	}
}
