package cz.geokuk.core.coord;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.framework.Action0;

/**
 * @author veverka
 *
 */
public class SouradniceDoClipboarduAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;
	private Poziceq poziceq = new Poziceq();
	private final Mouable mouable;
	private PoziceModel poziceModel;

	/**
	 *
	 */
	public SouradniceDoClipboarduAction(final Mouable mouable) {
		super(null);
		this.mouable = mouable;
		putValue(NAME, "Souřadnice do clipboardu");
		putValue(SHORT_DESCRIPTION, "Do systémového clipboardu vloží souřadnice záměrného kříže.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		Mouable m = mouable;
		if (m == null) {
			m = poziceq.getPoziceMouable();
			if (m == null) {
				return;
			}
		}
		poziceModel.souradniceDoClipboardu(m.getMou());
	}

	@Override
	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void onEvent(final PoziceChangedEvent event) {
		poziceq = event.poziceq;
		setEnabled(mouable != null || !poziceq.isNoPosition());
	}
}
