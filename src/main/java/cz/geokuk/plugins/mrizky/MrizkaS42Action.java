package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.BeanSubtype;

public class MrizkaS42Action extends MrizkaAction0 {
	private static final long serialVersionUID = -3631232428454275961L;

	public MrizkaS42Action() {
		super("Mřížka S42");
		putValue(SHORT_DESCRIPTION, "Zobrazí na mapě souřadnicovou mřížku.");
	}

	@BeanSubtype("S42")
	public void inject(final MrizkaModel mrizkaModel) {
		this.mrizkaModel = mrizkaModel;
	}

}
