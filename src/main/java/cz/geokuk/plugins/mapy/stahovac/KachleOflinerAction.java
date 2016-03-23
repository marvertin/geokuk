package cz.geokuk.plugins.mapy.stahovac;

import java.awt.event.ActionEvent;

import cz.geokuk.core.onoffline.OnofflineModelChangeEvent;
import cz.geokuk.framework.Action0;

public class KachleOflinerAction extends Action0 {

	private static final long serialVersionUID = -5465641756515262340L;

	public KachleOflinerAction() {
		super("Postahovat dlaždice...");
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final JKachleOflinerDialog jKachleOflinerDialog = factory.init(new JKachleOflinerDialog());
		jKachleOflinerDialog.setVisible(true);
	}

	public void onEvent(final OnofflineModelChangeEvent event) {
		setEnabled(event.isOnlineMOde());
	}
}
