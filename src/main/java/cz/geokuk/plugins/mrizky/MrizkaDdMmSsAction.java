package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.BeanSubtype;

public class MrizkaDdMmSsAction extends MrizkaAction0 {
	private static final long serialVersionUID = -3631232428454275961L;

	public MrizkaDdMmSsAction() {
		super("Mřížka DD°MM'SS");
		putValue(SHORT_DESCRIPTION, "Zobrazí na mapě souřadnicovou mřížku.");
	}

	@BeanSubtype("DdMmSs")
	public void inject(final MrizkaModel mrizkaModel) {
		this.mrizkaModel = mrizkaModel;
	}

}
