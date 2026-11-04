package cz.geokuk.plugins.kesoidkruhy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.StavVrstevModel;
import cz.geokuk.plugins.kesoid.ToastModel;
import cz.geokuk.plugins.kesoid.ZpravyZkratek;

public class JednotkoveKruhyAction extends ToggleAction0 {

	private static final long serialVersionUID = -7547868179813232769L;
	private KruhyModel model;
	private StavVrstevModel stavVrstev;
	private ToastModel toast;

	public JednotkoveKruhyAction() {
		super(nazev(PolomerKruhu.VYCHOZI_METRU));
		putValue(SHORT_DESCRIPTION, popis(PolomerKruhu.VYCHOZI_METRU));
		putValue(MNEMONIC_KEY, KeyEvent.VK_R);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F8"));
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
		putValue(NAME, nazev(event.kruhy.getPolomerMetru()));
		putValue(SHORT_DESCRIPTION, popis(event.kruhy.getPolomerMetru()));
		setSelected(event.kruhy.isJednotkovaVelikost());
	}

	/** Název v menu podle poloměru, třeba „Kruhy 1 km“. */
	static String nazev(final int polomerMetru) {
		return "Kruhy " + PolomerKruhu.format(polomerMetru);
	}

	static String popis(final int polomerMetru) {
		return "Kruhy o poloměru " + PolomerKruhu.format(polomerMetru) + ", jehož velikost na mapě se mění s přiblížením.";
	}

	@Override
	protected void onSlectedChange(final boolean onoff) {
		final KruhySettings data = model.getData();
		final boolean zvyrazneniBylyZapnute = data.isOnoff();
		data.setJednotkovaVelikost(onoff);
		if (onoff) {
			data.setOnoff(true);
		}
		model.setData(data);
		if (toast != null && stavVrstev != null) {
			toast.ukaz(ZpravyZkratek.jednotkoveKruhy(onoff, zvyrazneniBylyZapnute, stavVrstev.getStav().getSkrytyKruh()));
		}
	}

}
