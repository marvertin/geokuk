package cz.geokuk.plugins.kesoidobsazenost;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.StavVrstevModel;
import cz.geokuk.plugins.kesoid.ToastModel;
import cz.geokuk.plugins.kesoid.VrstvaNadMapou;
import cz.geokuk.plugins.kesoid.ZpravyZkratek;

public class ObsazenostOnoffAction extends ToggleAction0 {

	private static final long serialVersionUID = -7547868179813232769L;
	private ObsazenostModel popiskyModel;
	private StavVrstevModel stavVrstev;
	private ToastModel toast;

	public ObsazenostOnoffAction() {
		super("Obsazenost 161 m");
		putValue(SHORT_DESCRIPTION, "Zobrazí kruhy o poloměru 161 m kolem fyzických částí keší, i skrytých filtrem. Nová keš nesmí ležet uvnitř cizího kruhu, překryv kruhů pravidlo neporušuje.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_O);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));

	}

	public void inject(final ObsazenostModel popiskyModel) {
		this.popiskyModel = popiskyModel;
	}

	public void inject(final StavVrstevModel stavVrstev) {
		this.stavVrstev = stavVrstev;
	}

	public void inject(final ToastModel toast) {
		this.toast = toast;
	}

	public void onEvent(final ObsazenostOnoffEvent event) {
		setSelected(event.isOnoff());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		popiskyModel.visible.setOnoff(nastaveno);
		if (toast != null && stavVrstev != null) {
			toast.ukaz(ZpravyZkratek.obsazenost(nastaveno, JObsazenost.POLOMER_OBSAZENOSTI + " m", stavVrstev.getStav().get(VrstvaNadMapou.OBSAZENOST)));
		}
	}

}
