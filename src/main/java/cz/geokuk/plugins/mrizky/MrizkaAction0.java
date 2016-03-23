package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.*;

public abstract class MrizkaAction0 extends ToggleAction0 implements BeanSubtypable {

	private static final long	serialVersionUID	= -8064505014609316205L;

	protected MrizkaModel		mrizkaModel;

	public MrizkaAction0(final String name) {
		super(name);
	}

	@Override
	public String getSubType() {
		return kterouMamMrizku();
	}

	@BeanSubtype
	public void onEvent(final MrizkaEvent event) {
		setSelected(event.onoff);
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		mrizkaModel.setOnoff(nastaveno);
	}

	private String kterouMamMrizku() {
		final String kn = getClass().getName();
		final int poz1 = kn.indexOf(".Mrizka");
		final int poz2 = kn.indexOf("Action");
		final String result = kn.substring(poz1 + ".Mrizka".length(), poz2);
		return result;
	}

}
