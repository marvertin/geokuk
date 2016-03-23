package cz.geokuk.plugins.mrizky;

import java.awt.event.KeyEvent;

import cz.geokuk.framework.BeanSubtype;
import cz.geokuk.framework.ToggleAction0;

public class MeritkovnikAction extends ToggleAction0 {
	private static final long	serialVersionUID	= -3631232428454275961L;

	public MrizkaModel			mrizkaModel;

	public MeritkovnikAction() {
		super("Zobrazit měřítko");
		putValue(SHORT_DESCRIPTION, "Zobrazí měřítko mapy uprostřed její dolní části.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_M);
	}

	@BeanSubtype("Meritkovnik")
	public void inject(final MrizkaModel mrizkaModel) {
		this.mrizkaModel = mrizkaModel;
	}

	@BeanSubtype("Meritkovnik")
	public void onEvent(final MrizkaEvent event) {
		setSelected(event.onoff);
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		mrizkaModel.setOnoff(nastaveno);
	}

}
