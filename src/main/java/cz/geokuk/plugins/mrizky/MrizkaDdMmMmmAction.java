package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.BeanSubtype;


public class MrizkaDdMmMmmAction extends MrizkaAction0 {
	private static final long serialVersionUID = -3631232428454275961L;

	public MrizkaDdMmMmmAction() {
		super("Mřížka DD°MM.MMM");
		putValue(SHORT_DESCRIPTION, "Zobrazovat popisky keší na mapě." );
	}


	@BeanSubtype("DdMmMmm")
	public void inject(MrizkaModel mrizkaModel) {
		this.mrizkaModel = mrizkaModel;
	}

}
