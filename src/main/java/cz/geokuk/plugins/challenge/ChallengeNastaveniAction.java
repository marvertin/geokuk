package cz.geokuk.plugins.challenge;

import java.awt.event.KeyEvent;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;

/** Otevře dialog plánovače challenge. */
public class ChallengeNastaveniAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = 1L;

	public ChallengeNastaveniAction() {
		super("Nastavení plánovače…");
		putValue(SHORT_DESCRIPTION, "Seznam challenge, výběr aktivní, kopie šablon a úprava vlastních.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_P);
	}

	@Override
	public JMyDialog0 createDialog() {
		return new JChallengeDialog();
	}
}
