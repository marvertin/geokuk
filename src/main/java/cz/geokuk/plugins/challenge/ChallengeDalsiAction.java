package cz.geokuk.plugins.challenge;

import java.awt.event.ActionEvent;

import cz.geokuk.framework.Action0;

/** Další sektor nebo políčko zobrazené challenge (◀ ▶); název a popis podle typu challenge. */
public class ChallengeDalsiAction extends Action0 {

	private static final long serialVersionUID = 1L;
	private transient ChallengeModel model;

	public ChallengeDalsiAction() {
		super(ChallengeAkce.nazevPosunu(Challenge.Typ.SEKTORY_360, true));
		putValue(SHORT_DESCRIPTION, ChallengeAkce.tipPosunu(Challenge.Typ.SEKTORY_360, true));
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
			putValue(NAME, ChallengeAkce.nazevPosunu(c.typ(), true));
			putValue(SHORT_DESCRIPTION, ChallengeAkce.tipPosunu(c.typ(), true));
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		ChallengeAkce.posun(model, true);
	}
}
