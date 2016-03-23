package cz.geokuk.core.onoffline;

import cz.geokuk.framework.Model0;

public class OnofflineModel extends Model0 {

	private boolean onlineMode;

	@Override
	protected void initAndFire() {
		onlineMode = currPrefe().getBoolean("onlineMode", true);
		fire(new OnofflineModelChangeEvent(onlineMode));
	}


	/**
	 * @param onlineMode the onlineMode to set
	 */
	public void setOnlineMode(final boolean onlineMode) {
		if (onlineMode == this.onlineMode) {
			return;
		}
		this.onlineMode = onlineMode;
		currPrefe().putBoolean("onlineMode", onlineMode);
		fire(new OnofflineModelChangeEvent(onlineMode));
	}


	/**
	 * @return the onlineMode
	 */
	public boolean isOnlineMode() {
		return onlineMode;
	}

}
