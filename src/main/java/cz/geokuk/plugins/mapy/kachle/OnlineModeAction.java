package cz.geokuk.plugins.mapy.kachle;

import java.awt.event.KeyEvent;

import cz.geokuk.core.onoffline.OnofflineModel;
import cz.geokuk.core.onoffline.OnofflineModelChangeEvent;
import cz.geokuk.framework.ToggleAction0;


public class OnlineModeAction extends ToggleAction0 {
	private static final long serialVersionUID = -3631232428454275961L;

	private OnofflineModel onofflineModel;


	public OnlineModeAction() {
		super("Online");
		putValue(SHORT_DESCRIPTION, "Režim online, kdy se stahují mapy z webu, v offlinu se berou mapy pouze z diskové keše.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_O);

	}

	public void onEvent(final OnofflineModelChangeEvent event) {
		setSelected(event.isOnlineMOde());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		onofflineModel.setOnlineMode(nastaveno);
	}

	public void inject(final OnofflineModel onofflineModel) {
		this.onofflineModel = onofflineModel;
	}




}
