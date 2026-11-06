package cz.geokuk.plugins.kesoidkruhy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.StavVrstevModel;
import cz.geokuk.plugins.kesoid.ToastModel;
import cz.geokuk.plugins.kesoid.ZpravyZkratek;

public class KruhyOnoffAction extends ToggleAction0 {

	private static final long serialVersionUID = -7547868179813232769L;
	private KruhyModel model;
	private StavVrstevModel stavVrstev;
	private ToastModel toast;

	public KruhyOnoffAction() {

		super("Zvýrazňovací kruhy");
		putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
		putValue(SHORT_DESCRIPTION, "Zobrazí na mapě zvýrazňovací kruhy.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));

	}

	public void inject(final KruhyModel model) {
		this.model = model;
	}

	public void inject(final StavVrstevModel stavVrstev) {
		this.stavVrstev = stavVrstev;
	}

	public void inject(final ToastModel toast) {
		this.toast = toast;
	}

	public void onEvent(final KruhyPreferencesChangeEvent event) {
		setSelected(event.kruhy.isOnoff());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		final KruhySettings data = model.getData();
		data.setOnoff(nastaveno);
		model.setData(data);
		if (toast != null && stavVrstev != null) {
			toast.ukaz(ZpravyZkratek.zvyraznovaciKruhy(nastaveno, velikost(data), stavVrstev.getStav().getSkrytyKruh()));
		}
	}

	/** Velikost kruhu pro zprávu: poloměr, když je velikost v metrech, jinak v pixelech. */
	static String velikost(final KruhySettings data) {
		return data.isJednotkovaVelikost() ? PolomerKruhu.format(data.getPolomerMetru()) : PolomerKruhu.formatPixely(data.getVelikost());
	}

}
