package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.ToastModel;
import cz.geokuk.plugins.kesoid.ZpravyZkratek;

public class KesoidyOnoffAction extends ToggleAction0 {

	private static final long serialVersionUID = -7547868179813232769L;
	private KesoidModel kesoidModel;
	private ToastModel toast;

	public KesoidyOnoffAction() {
		super("Zobrazovat kešoidy");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_Y);
		putValue(MNEMONIC_KEY, KeyEvent.VK_B);
		putValue(SHORT_DESCRIPTION, "Zobrazovat kešoidy (F5): řídí, zda se vůbec budou zobrazovat kešoidy.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void inject(final ToastModel toast) {
		this.toast = toast;
	}

	public void onEvent(final KesoidOnoffEvent event) {
		setSelected(event.isOnoff());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		kesoidModel.setOnoff(nastaveno);
		if (toast != null) {
			toast.ukaz(ZpravyZkratek.kesoidy(nastaveno));
		}
	}

}
