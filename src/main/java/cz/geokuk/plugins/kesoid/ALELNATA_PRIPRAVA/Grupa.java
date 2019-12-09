package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import java.util.Set;

public interface Grupa {

	/**
	 * @return the alely
	 */
	Set<Alela> getAlely();

	String getDisplayName();

	// Vždy je to false, zajištěno jinak
	default boolean isOther() {
		return false;
	}

}