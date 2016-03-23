package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;

public class JenFinalUNalezenychAction extends ToggleAction0 {

	private static final long	serialVersionUID	= -7547868179813232769L;
	private KesoidModel			model;

	public JenFinalUNalezenychAction() {
		super("Jen final u nalezených");
		putValue(SHORT_DESCRIPTION, "U nalezenýc a mých keší skrýt všechny waypointy kromě kromě finálního smailíka.");
	}

	public void onEvent(FilterDefinitionChangedEvent event) {
		setSelected(event.getFilterDefinition().isJenFinalUNalezenych());
	}

	@Override
	protected void onSlectedChange(boolean onoff) {
		FilterDefinition definition = model.getDefinition();
		definition.setJenFinalUNalezenych(onoff);
		model.setDefinition(definition);
	}

	public void inject(KesoidModel model) {
		this.model = model;
	}

}
