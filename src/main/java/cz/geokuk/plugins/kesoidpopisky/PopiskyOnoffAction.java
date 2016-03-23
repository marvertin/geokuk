package cz.geokuk.plugins.kesoidpopisky;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;

public class PopiskyOnoffAction extends ToggleAction0 {

	private static final long	serialVersionUID	= -7547868179813232769L;
	private PopiskyModel		popiskyModel;

	public PopiskyOnoffAction() {
		super("Popisky na mapě");
		putValue(MNEMONIC_KEY, KeyEvent.VK_Y);
		putValue(SHORT_DESCRIPTION, "Zobrazovat popisky keší na mapě.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
	}

	public void inject(PopiskyModel popiskyModel) {
		this.popiskyModel = popiskyModel;
	}

	public void onEvent(PopiskyOnoffEvent event) {
		setSelected(event.isOnoff());
	}

	@Override
	protected void onSlectedChange(boolean nastaveno) {
		popiskyModel.visible.setOnoff(nastaveno);
	}

}
