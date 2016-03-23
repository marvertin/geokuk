package cz.geokuk.plugins.vylety;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import cz.geokuk.framework.Dlg;

public class VyletSmazAnoAction extends VyletAction0 {

	private static final long serialVersionUID = -7547868179813232769L;

	public VyletSmazAnoAction() {
		super("Žádné nelovím");
		putValue(SHORT_DESCRIPTION, "Odstraní příznak lovení u všech keší.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_N);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (Dlg.anone("Opravdu odstranit z výletu " + vyletModel.get(EVylet.ANO).size() + " keší, které chcete lovit?")) {
			vyletModel.removeAll(EVylet.ANO);
		}
	}

	@Override
	protected void vyletChanged() {
		super.vyletChanged();
		setEnabled(vyletModel.get(EVylet.ANO).size() > 0);
	}

}
