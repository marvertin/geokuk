package cz.geokuk.plugins.challenge;

import cz.geokuk.framework.ToggleAction0;

/** Přepínač kreslení na mapě: Štítky u kandidátů. */
public class ChallengeStitkyAction extends ToggleAction0 {

	private static final long serialVersionUID = 1L;
	private transient ChallengeModel model;

	public ChallengeStitkyAction() {
		super("Štítky u kandidátů");
		putValue(SHORT_DESCRIPTION, "Kreslí u kandidátů štítek s číslem sektoru nebo políčkem D/T.");
		setSelected(true);
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		final ChallengeModel m = event.getModel();
		setEnabled(m.jeZapnuto());
		setSelected(m.isKresliStitky());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		if (model != null) {
			model.setKresliStitky(nastaveno);
		}
	}
}
