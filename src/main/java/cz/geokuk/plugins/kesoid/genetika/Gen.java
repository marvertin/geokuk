package cz.geokuk.plugins.kesoid.genetika;

import java.util.*;

import cz.geokuk.util.lang.FString;

/**
 * Determines a property of a {@link cz.geokuk.plugins.kesoid.Kesoid}.
 *
 * <p>
 * For example, gen can be {@code size of the cache} or {@code type of the waypoint}.
 */
public class Gen {

	private final String displayName;
	private final Set<Alela> alely = new LinkedHashSet<>();
	private final Map<String, GrupaImpl> grupy = new HashMap<>();

	private Alela vychoziAlela;
	private boolean locked;
	private final Genom genom;

	private final GrupaImpl IMPLICITNI_GRUPA = grupa(GrupaImpl.IMPLICITNI_GRUPA_NAME);
	private final boolean vypsatelnyVeZhasinaci;

	public Gen(final String displayName, final Genom genom, final boolean vypsatelnyVeZhasinaci) {
		this.displayName = displayName;
		this.genom = genom;
		this.vypsatelnyVeZhasinaci = vypsatelnyVeZhasinaci;
	}

	public synchronized void add(final Alela alela, final String jmenoGrupy) {
		if (locked) {
			throw new RuntimeException("Nemozne pridavat alely " + alela + " k zamcenemu genu " + this);
		}
		final Gen puvodniGen = alela.hasGen() ? alela.getGen() : null;
		if (puvodniGen != this) {
			if (puvodniGen != null) {
				puvodniGen.alely.remove(alela);
			}
			alely.add(alela);
			alela.setGen(this);
		}
		final GrupaImpl grupa = grupa(jmenoGrupy);
		if (alela.getGrupa() == null || grupa != IMPLICITNI_GRUPA) {
			grupa.add(alela);
		}
		if (vychoziAlela == null) {
			vychoziAlela = alela;
		}
	}

	public Set<Alela> getAlely() {
		return alely;
	}

	public String getDisplayName() {
		return displayName;
	}

	public Genom getGenom() {
		return genom;
	}

	/**
	 * @return the grupy
	 */
	public Map<String, GrupaImpl> getGrupy() {
		return grupy;
	}

	public Alela getVychoziAlela() {
		assert vychoziAlela != null : "Na genu: " + displayName;
		return vychoziAlela;
	}

	public synchronized GrupaImpl grupa(final String grupaName) {
		if (FString.isEmpty(grupaName)) {
			return IMPLICITNI_GRUPA;
		}
		GrupaImpl grupa = grupy.get(grupaName);
		if (grupa == null) {
			grupa = new GrupaImpl(grupaName);
			if ("gcawp!".equals(grupaName)) {
				genom.GRUPA_gcawp = grupa;
			}
			if ("gc!".equals(grupaName)) {
				genom.GRUPA_gc = grupa;
			}
			grupy.put(grupaName, grupa);
		}
		return grupa;
	}

	public boolean isVypsatelnyVeZhasinaci() {
		return vypsatelnyVeZhasinaci;
	}

	/**
	 *
	 */
	public void lock() {
		locked = true;
	}

	@Override
	public String toString() {
		return "Gen [displayName=" + displayName + ", alely=" + alely + ", vychoziAlela=" + vychoziAlela + "]";
	}
}
