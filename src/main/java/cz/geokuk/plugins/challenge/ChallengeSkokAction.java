package cz.geokuk.plugins.challenge;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

import javax.swing.KeyStroke;

import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.ToastModel;
import cz.geokuk.plugins.kesoid.ZpravyZkratek;

/** Skok na kandidáta zobrazené challenge ve směru šipky (Shift+šipka); když tam žádný není, řekne to zpráva po zkratce. */
public class ChallengeSkokAction extends Action0 {

	private static final long serialVersionUID = 1L;
	private final SmerSkoku smer;
	private transient ChallengeModel model;
	private transient ToastModel toast;
	private transient Supplier<PlanovacChallenge> planovac = () -> null;

	public ChallengeSkokAction(final SmerSkoku smer) {
		super("Skok na kandidáta " + smer.text());
		this.smer = smer;
		putValue(SHORT_DESCRIPTION, "Vybere nejbližšího kandidáta zobrazené challenge " + smer.text() + " a vystředí na něj mapu.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(klavesa(smer), InputEvent.SHIFT_DOWN_MASK));
		setEnabled(false);
	}

	static int klavesa(final SmerSkoku smer) {
		switch (smer) {
		case SEVER:
			return KeyEvent.VK_UP;
		case VYCHOD:
			return KeyEvent.VK_RIGHT;
		case JIH:
			return KeyEvent.VK_DOWN;
		default:
			return KeyEvent.VK_LEFT;
		}
	}

	public void inject(final ChallengeModel model) {
		this.model = model;
	}

	public void inject(final PlanovacChallengeSluzba sluzba) {
		planovac = sluzba::planovac;
	}

	public void inject(final ToastModel toast) {
		this.toast = toast;
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
		if (ChallengeAkce.skoc(model, planovac.get(), smer) == null && toast != null) {
			toast.ukaz(ZpravyZkratek.challengeZadnyKandidat(smer.text()));
		}
	}
}
