package cz.geokuk.plugins.kesoidkruhy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;

public class KruhyOnoffAction extends ToggleAction0 {

	private static final long	serialVersionUID	= -7547868179813232769L;
	private KruhyModel			model;

	public KruhyOnoffAction() {

		super("Zvýrazňovací kruhy");
		putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
		putValue(SHORT_DESCRIPTION, "Zobrazí na mapě zvýrazňovací kruhy.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

	}

	public void inject(final KruhyModel model) {
		this.model = model;
	}

	public void onEvent(final KruhyPreferencesChangeEvent event) {
		setSelected(event.kruhy.isOnoff());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		final KruhySettings data = model.getData();
		data.setOnoff(nastaveno);
		model.setData(data);
	}

}
