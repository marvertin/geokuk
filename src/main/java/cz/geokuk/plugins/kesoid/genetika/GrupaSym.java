package cz.geokuk.plugins.kesoid.genetika;

import java.util.*;

import cz.geokuk.util.lang.FString;

public class GrupaSym implements Grupa {
	static final String IMPLICITNI_GRUPA_NAME = "other!";

	private final String grupaName;
	private String displayName;

	private final Set<Alela> alely = new LinkedHashSet<>();

	public GrupaSym(final String grupaName) {
		this.grupaName = grupaName;
		displayName = grupaName;
	}

	public synchronized void add(final Alela alela) {
//		final Grupa puvodniGrupa = alela.getGrupa();
//		if (puvodniGrupa != this) {
//			// System.out.printf("Prerazovani alely mezi grupami %s: %s -> %s\n", alela, puvodniGrupa, this);
//			// new RuntimeException().printStackTrace();
//			if (puvodniGrupa != null) {
//				puvodniGrupa.alely.remove(alela);
//			}
//			alely.add(alela);
//			alela.setGrupa(this);
//		}
		alely.add(alela);
	}

	/**
	 * @return the alely
	 */
	@Override
	public Set<Alela> getAlely() {
		return Collections.unmodifiableSet(alely);
	}

	@Override
	public String getDisplayName() {
		return FString.isEmpty(displayName) ? grupaName : displayName;
	}

	public String name() {
		return grupaName;
	}

	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return grupaName;
	}

}
