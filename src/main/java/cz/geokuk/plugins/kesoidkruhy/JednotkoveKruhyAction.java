package cz.geokuk.plugins.kesoidkruhy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;

public class JednotkoveKruhyAction extends ToggleAction0 {

	private static final long serialVersionUID = -7547868179813232769L;
	private KruhyModel model;

	public JednotkoveKruhyAction() {
		super("Jednotkové kruhy");
		putValue(SHORT_DESCRIPTION, popis(PolomerKruhu.VYCHOZI_METRU));
		putValue(MNEMONIC_KEY, KeyEvent.VK_J);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F8"));
	}

	public void inject(final KruhyModel model) {
		this.model = model;
	}

	public void onEvent(final KruhyPreferencesChangeEvent event) {
		putValue(SHORT_DESCRIPTION, popis(event.kruhy.getPolomerMetru()));
		setSelected(event.kruhy.isJednotkovaVelikost());
	}

	static String popis(final int polomerMetru) {
		return "Jednotkové kruhy, poloměr " + PolomerKruhu.format(polomerMetru);
	}

	@Override
	protected void onSlectedChange(final boolean onoff) {
		final KruhySettings data = model.getData();
		data.setJednotkovaVelikost(onoff);
		if (onoff) {
			data.setOnoff(true);
		}
		model.setData(data);
	}

}
