package cz.geokuk.plugins.mrizky;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.MultiInjection;

/**
 * @author veverka
 *
 */
public class ZhasniVsechnyMrizkyAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;
	private final List<MrizkaModel> mrizkaModels = new ArrayList<>();

	/**
	 *
	 */
	public ZhasniVsechnyMrizkyAction() {
		super("Zhasni mřížky");
		putValue(SHORT_DESCRIPTION, "Zhasni všechny rozsvícené mřížky.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		for (final MrizkaModel mrizkaModel : mrizkaModels) {
			mrizkaModel.setOnoff(false);
		}
	}

	@MultiInjection
	public void inject(final MrizkaModel mrizkaModel) {
		if (!"Meritkovnik".equals(mrizkaModel.getSubType())) {
			mrizkaModels.add(mrizkaModel); // jenom přížky se zhasínají
		}
	}

}
