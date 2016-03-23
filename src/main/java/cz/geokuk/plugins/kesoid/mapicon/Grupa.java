package cz.geokuk.plugins.kesoid.mapicon;

import java.util.*;

import cz.geokuk.util.lang.FString;

public class Grupa {
	static final String		IMPLICITNI_GRUPA_NAME	= "other!";

	private final String	grupaName;
	private String			displayName;

	private Set<Alela>		alely					= new LinkedHashSet<>();

	/**
	 * @return the alely
	 */
	public Set<Alela> getAlely() {
		return Collections.unmodifiableSet(alely);
	}

	public synchronized void add(Alela alela) {
		Grupa puvodniGrupa = alela.getGrupa();
		if (puvodniGrupa != this) {
			// System.out.printf("Prerazovani alely mezi grupami %s: %s -> %s\n", alela, puvodniGrupa, this);
			// new RuntimeException().printStackTrace();
			if (puvodniGrupa != null) {
				puvodniGrupa.alely.remove(alela);
			}
			alely.add(alela);
			alela.setGrupa(this);
		}
	}

	public Grupa(String grupaName) {
		this.grupaName = grupaName;
		this.displayName = grupaName;
	}

	public String getDisplayName() {
		return FString.isEmpty(displayName) ? grupaName : displayName;
	}

	public boolean isOther() {
		return IMPLICITNI_GRUPA_NAME.equals(grupaName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return grupaName;
	}

	public String name() {
		return grupaName;
	}

}
