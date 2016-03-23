package cz.geokuk.plugins.vylety;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

import cz.geokuk.core.coordinates.MouRect;
import cz.geokuk.plugins.kesoid.Kesoid;


public class VyletZoomAction extends VyletAction0 {

	private static final long serialVersionUID = -7547868179813232769L;

	public VyletZoomAction() {
		super("Zoom výlet");
		putValue(SHORT_DESCRIPTION, "Zobrazí měřítko a výřez mapy tak, aby na mapě byly všechny keše vybrané do výletu.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Set<Kesoid> set = vyletModel.get(EVylet.ANO);
		MouRect mourect = new MouRect();
		for (Kesoid kes : set) {
			mourect.add(kes.getMainWpt().getWgs().toMou());
		}
		mourect.resize(1.2);
		vyrezModel.zoomTo(mourect);
	}

	@Override
	protected void vyletChanged() {
		super.vyletChanged();
		setEnabled(vyletModel.get(EVylet.ANO).size() > 0);
	}


}
