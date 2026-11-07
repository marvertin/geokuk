package cz.geokuk.plugins.challenge;

import cz.geokuk.framework.ToggleAction0;

/** Zapíná a vypíná plánovač challenge: lišta na mapě, při vypnutí zůstane jen úchyt. */
public class ChallengeZapnoutAction extends ToggleAction0 {

	private static final long serialVersionUID = 1L;
	private transient ChallengeModel model;

	public ChallengeZapnoutAction() {
		super("Zapnout plánovač");
		putValue(SHORT_DESCRIPTION, "Na mapě vlevo nahoře ukáže plánovač; vypnutý zůstane jen jeho ikona ◎.");
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		setSelected(event.getModel().jeZapnuto());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		model.setZapnuto(nastaveno);
	}
}
