package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.BeanSubtypable;
import cz.geokuk.framework.BeanSubtype;
import cz.geokuk.framework.ToggleAction0;

public abstract class MrizkaAction0 extends ToggleAction0 implements BeanSubtypable {

	private static final long serialVersionUID = -8064505014609316205L;

	protected MrizkaModel mrizkaModel;

	public MrizkaAction0(String name) {
		super(name);
	}

	@Override
	protected void onSlectedChange(boolean nastaveno) {
		mrizkaModel.setOnoff(nastaveno);
	}

	private String kterouMamMrizku() {
		String kn = getClass().getName();
		int poz1 = kn.indexOf(".Mrizka");
		int poz2 = kn.indexOf("Action");
		String result = kn.substring(poz1 + ".Mrizka".length(), poz2);
		return result;
	}

	@BeanSubtype
	public void onEvent(MrizkaEvent event) {
		setSelected(event.onoff);
	}

	@Override
	public String getSubType() {
		return kterouMamMrizku();
	}

}
