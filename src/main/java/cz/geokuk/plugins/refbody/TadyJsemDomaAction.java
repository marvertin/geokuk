/**
 *
 */
package cz.geokuk.plugins.refbody;

import java.awt.event.ActionEvent;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.VyrezModel;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Action0;
import cz.geokuk.framework.Dlg;

/**
 * @author veverka
 *
 */
public class TadyJsemDomaAction extends Action0 {

	private static final long	serialVersionUID	= -2882817111560336824L;

	private Wgs					hc;

	private RefbodyModel		refbodyModel;

	/**
	 * @param aBoard
	 */
	public TadyJsemDomaAction() {
		super("Jsem doma...");
		putValue(SHORT_DESCRIPTION,
				"Nastaví aktuální pozici jako domácí souřadnice." + " Na takto uložená nastavení se dostanete volbou \"Domů\", od těchto souřadnic se počítá vzdálenost a pozice keší.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_D);

	}

	public void onEvent(final PoziceChangedEvent aEvent) {
		if (aEvent.poziceq.isNoPosition()) {
			setEnabled(false);
		} else {
			setEnabled(true);
			hc = aEvent.poziceq.getWgs();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {
		final Wgs stareGc = refbodyModel.getHc();
		boolean zapsat = true;
		if (!VyrezModel.DEFAULTNI_DOMACI_SOURADNICE.equals(stareGc)) {
			// ještě neukládal, tak se nemusíme ptát
			zapsat = Dlg.anone("Uložit " + hc + " jako domací souřadnice a přepsat tak původní " + stareGc + "?");
		}
		if (zapsat) {
			refbodyModel.setHc(hc);
		}
	}

	public void inject(final RefbodyModel refbodyModel) {
		this.refbodyModel = refbodyModel;
	}

}
