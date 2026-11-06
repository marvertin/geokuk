package cz.geokuk.plugins.challenge;

import cz.geokuk.framework.ToggleAction0;

/** Zobrazí jen kandidáty, kteří zaplní chybějící sektor aktivní challenge. */
public class ChallengeJenKandidatiAction extends ToggleAction0 {

	private static final long serialVersionUID = 1L;
	private transient ChallengeModel model;

	public ChallengeJenKandidatiAction() {
		super("Zobrazit jen kandidáty");
		putValue(SHORT_DESCRIPTION, "Na mapě nechá jen nenalezené keše, které doplní chybějící sektor, políčko nebo den.");
		setEnabled(false);
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void onEvent(final ChallengeZmenenyEvent event) {
		final ChallengeModel m = event.getModel();
		final Challenge c = m.zobrazena();
		setEnabled(ChallengeAkce.dostupne(m));
		setSelected(c != null && c.equals(m.jenKandidatiPro()));
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		final Challenge c = model.zobrazena();
		if (c == null) {
			setSelected(false);
			return;
		}
		try {
			model.setJenKandidatiPro(nastaveno ? c : null);
		} catch (final ChallengeChyba e) {
			setSelected(false);
		}
	}
}
