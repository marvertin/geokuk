package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.framework.ToggleAction0;

public class JenFinalUNalezenychAction extends ToggleAction0 {

	private static final long serialVersionUID = -7547868179813232769L;
	private KesFilterModel model;

	public JenFinalUNalezenychAction() {
		super("Jen final u nalezených");
		putValue(SHORT_DESCRIPTION, "U nalezenýc a mých keší skrýt všechny waypointy kromě kromě finálního smailíka.");
	}

	public void inject(final KesFilterModel model) {
		this.model = model;
	}

	public void onEvent(final KesFilterDefinitionChangedEvent event) {
		setSelected(event.getKesFilterDefinition().isJenFinalUNalezenych());
	}

	@Override
	protected void onSlectedChange(final boolean onoff) {
		model.modify(definition -> definition.withJenFinalUNalezenych(onoff));
	}

}
