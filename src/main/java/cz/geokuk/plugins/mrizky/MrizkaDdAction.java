package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.BeanSubtype;

public class MrizkaDdAction extends MrizkaAction0 {
	private static final long serialVersionUID = -3631232428454275961L;

	public MrizkaDdAction() {
		super("Mřížka DD°");
		putValue(SHORT_DESCRIPTION, "Zobrazovat popisky keší na mapě.");
	}

	@BeanSubtype("Dd")
	public void inject(final MrizkaModel mrizkaModel) {
		this.mrizkaModel = mrizkaModel;
	}

}
