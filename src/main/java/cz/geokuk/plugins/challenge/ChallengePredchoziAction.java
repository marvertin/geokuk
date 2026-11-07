package cz.geokuk.plugins.challenge;

import java.awt.event.ActionEvent;

import cz.geokuk.framework.Action0;

/** Předchozí sektor nebo políčko zobrazené challenge (◀ ▶); název a popis podle typu challenge. */
public class ChallengePredchoziAction extends Action0 {

	private static final long serialVersionUID = 1L;
	private transient ChallengeModel model;

	public ChallengePredchoziAction() {
		super(ChallengeAkce.nazevPosunu(Challenge.Typ.SEKTORY_360, false));
		putValue(SHORT_DESCRIPTION, ChallengeAkce.tipPosunu(Challenge.Typ.SEKTORY_360, false));
		setEnabled(false);
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		final ChallengeModel m = event.getModel();
		setEnabled(ChallengeAkce.dostupne(m));
		final Challenge c = m == null ? null : m.zobrazena();
		if (c != null) {
			putValue(NAME, ChallengeAkce.nazevPosunu(c.typ(), false));
			putValue(SHORT_DESCRIPTION, ChallengeAkce.tipPosunu(c.typ(), false));
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		ChallengeAkce.posun(model, false);
	}
}
