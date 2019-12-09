package cz.geokuk.plugins.kesoid.genetika;

import java.util.Set;

public interface Grupa {

	/**
	 * @return the alely
	 */
	Set<Alela> getAlely();

	String getDisplayName();

	boolean isOther();

}