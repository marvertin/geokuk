package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.BeanSubtype;

public class MrizkaJTSKAction extends MrizkaAction0 {
	private static final long serialVersionUID = -3631232428454275961L;

	public MrizkaJTSKAction() {
		super("Mřížka JTSK");
		putValue(SHORT_DESCRIPTION, "Zobrazí na mapě souřadnicovou mřížku.");
	}

	@BeanSubtype("JTSK")
	public void inject(MrizkaModel mrizkaModel) {
		this.mrizkaModel = mrizkaModel;
	}

}
