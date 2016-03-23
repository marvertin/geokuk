package cz.geokuk.framework;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.*;

public abstract class ToggleAction0 extends Action0 {
	private static final long	serialVersionUID	= 3747754572841745541L;

	private ButtonModel			bm;											// = new DefaultButtonModel();
	private boolean				iOnoff;

	public ToggleAction0(final String name) {
		super(name);
	}

	/**
	 * Přidá tuto modelo akci nějakému buttonu. Z prvního buttonu bere model, do dalšího buttonu dává.
	 *
	 * @param ab
	 */
	public void join(final AbstractButton ab) {
		if (bm == null) {
			bm = ab.getModel();
			nastavDoButtonu();
			registerEvents();
		} else {
			ab.setModel(bm);
		}
		ab.setAction(this);
		final Icon icon = getIcon();
		if (icon != null) {
			ab.setIcon(icon);
		}
	}

	private void registerEvents() {
		bm.addItemListener(event -> {
			final boolean onoff = event.getStateChange() == ItemEvent.SELECTED;
			if (iOnoff != onoff) {
				iOnoff = onoff;
				onSlectedChange(iOnoff);
			}
		});
	}

	public void setSelected(final boolean onoff) {
		this.iOnoff = onoff;
		nastavDoButtonu();
	}

	private void nastavDoButtonu() {
		if (bm != null) {
			bm.setSelected(iOnoff);
		}
	}

	public boolean isSelected() {
		return iOnoff;
	}

	protected abstract void onSlectedChange(boolean nastaveno);

	@Override
	public void actionPerformed(final ActionEvent e) {
		// Neni potreba definovat, dela se to pres model
	}

}
