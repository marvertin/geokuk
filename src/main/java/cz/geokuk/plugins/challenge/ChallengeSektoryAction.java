package cz.geokuk.plugins.challenge;

import cz.geokuk.framework.ToggleAction0;

/** Přepínač kreslení na mapě: sektory na mapě. */
public class ChallengeSektoryAction extends ToggleAction0 {

	private static final long serialVersionUID = 1L;
	private transient ChallengeModel model;

	public ChallengeSektoryAction() {
		super("Sektory na mapě");
		putValue(SHORT_DESCRIPTION, "Kreslí u zobrazené challenge sektory: prstenec, klín sektoru vybraného kandidáta a čáru k němu.");
		setSelected(true);
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		final ChallengeModel m = event.getModel();
		final Challenge c = m.zobrazena();
		setEnabled(m.jeZapnuto() && c != null && JPruhPlanovace.maSektory(c.typ()));
		setSelected(m.isKresliSektory());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		if (model != null) {
			model.setKresliSektory(nastaveno);
		}
	}
}
