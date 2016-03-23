package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.BeanSubtype;

public class MrizkaUtmAction extends MrizkaAction0 {
	private static final long serialVersionUID = -3631232428454275961L;

	public MrizkaUtmAction() {
		super("Mřížka UTM");
		putValue(SHORT_DESCRIPTION, "Zobrazí na mapě souřadnicovou mřížku.");
	}

	@BeanSubtype("Utm")
	public void inject(MrizkaModel mrizkaModel) {
		this.mrizkaModel = mrizkaModel;
	}

}
