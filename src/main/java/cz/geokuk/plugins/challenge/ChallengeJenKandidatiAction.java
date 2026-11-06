package cz.geokuk.plugins.challenge;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.ToastModel;
import cz.geokuk.plugins.kesoid.ZpravyZkratek;

/** Zobrazí jen kandidáty, kteří zaplní chybějící sektor aktivní challenge. */
public class ChallengeJenKandidatiAction extends ToggleAction0 {

	private static final long serialVersionUID = 1L;
	private transient ChallengeModel model;
	private transient ToastModel toast;

	public ChallengeJenKandidatiAction() {
		super("Zobrazit jen kandidáty");
		putValue(SHORT_DESCRIPTION, "Na mapě nechá jen nenalezené keše, které doplní chybějící sektor, políčko nebo den.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		setEnabled(false);
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void inject(final ToastModel toast) {
		this.toast = toast;
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
			return;
		}
		if (toast != null) {
			toast.ukaz(ZpravyZkratek.challengeJenKandidati(nastaveno));
		}
	}
}
