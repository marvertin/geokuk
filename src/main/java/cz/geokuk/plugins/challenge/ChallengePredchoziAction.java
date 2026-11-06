package cz.geokuk.plugins.challenge;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

import cz.geokuk.framework.Action0;

/** Předchozí chybějící. */
public class ChallengePredchoziAction extends Action0 {

	private static final long serialVersionUID = 1L;
	private transient ChallengeModel model;
	private transient Supplier<PlanovacChallenge> planovac = () -> null;

	public ChallengePredchoziAction() {
		super("Předchozí chybějící");
		putValue(SHORT_DESCRIPTION, "Vrátí se k předchozí vybrané keši.");
		setEnabled(false);
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void inject(final PlanovacChallengeSluzba sluzba) {
		planovac = sluzba::planovac;
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		setEnabled(ChallengeAkce.dostupne(event.getModel()));
	}

	/** Plánovač pro testy, kde není celý program. */
	void setPlanovac(final Supplier<PlanovacChallenge> planovac) {
		this.planovac = planovac;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		ChallengeAkce.prejdi(model, planovac.get(), false);
	}
}
