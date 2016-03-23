package cz.geokuk.plugins.vylety;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Kesoid;

public class VyletAnoAction extends VyletActionIndividual0 {

	private static final long serialVersionUID = 1L;

	public VyletAnoAction(Kesoid kes) {
		super("Lovím", kes);

		putValue(SHORT_DESCRIPTION, "Zařadí keš mezi favority, které chci lovit.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_L);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
		putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		vyletModel.add(EVylet.ANO, kesoid());
	}

	@Override
	protected void enablujPokudMaSmysl() {
		setEnabled(vyletModel.get(kesoid()) != EVylet.ANO);
	}
}
