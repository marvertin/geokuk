package cz.geokuk.plugins.vylety;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;

public class VyletNevimAction extends VyletActionIndividual0 {

	private static final long serialVersionUID = 1L;

	public VyletNevimAction(final Wpt wpt) {
		super("Nevím", wpt);
		putValue(SHORT_DESCRIPTION, "Ještě nevím, zda tuto keš budu lovit nebo ignorovat.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_N);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('*'));
		putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletNevim.png"));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		vyletModel.add(EVylet.NEVIM, kesoid());
	}

	@Override
	protected void enablujPokudMaSmysl() {
		setEnabled(vyletModel.get(kesoid()) != EVylet.NEVIM);
	}
}
