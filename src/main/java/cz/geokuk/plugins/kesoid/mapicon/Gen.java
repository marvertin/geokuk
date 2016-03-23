package cz.geokuk.plugins.kesoid.mapicon;

import java.util.*;

import cz.geokuk.util.lang.FString;

/**
 * Determines a property of a {@link cz.geokuk.plugins.kesoid.Kesoid}.
 *
 * <p>
 * For example, gen can be {@code size of the cache} or {@code type of the waypoint}.
 */
public class Gen {

	private final String				displayName;
	private final Set<Alela>			alely				= new LinkedHashSet<>();
	private final Map<String, Grupa>	grupy				= new HashMap<>();

	private Alela						vychoziAlela;
	private boolean						locked;
	private final Genom					genom;

	private final Grupa					IMPLICITNI_GRUPA	= grupa(Grupa.IMPLICITNI_GRUPA_NAME);
	private final boolean				vypsatelnyVeZhasinaci;

	/**
	 * @return the grupy
	 */
	public Map<String, Grupa> getGrupy() {
		return grupy;
	}

	public Gen(final String displayName, final Genom genom, final boolean vypsatelnyVeZhasinaci) {
		this.displayName = displayName;
		this.genom = genom;
		this.vypsatelnyVeZhasinaci = vypsatelnyVeZhasinaci;
	}

	public String getDisplayName() {
		return displayName;
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
		final Grupa grupa = grupa(jmenoGrupy);
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

	public Alela getVychoziAlela() {
		assert vychoziAlela != null : "Na genu: " + displayName;
		return vychoziAlela;
	}

	public synchronized Grupa grupa(final String grupaName) {
		if (FString.isEmpty(grupaName)) {
			return IMPLICITNI_GRUPA;
		}
		Grupa grupa = grupy.get(grupaName);
		if (grupa == null) {
			grupa = new Grupa(grupaName);
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

	/**
	 *
	 */
	public void lock() {
		locked = true;
	}

	public Genom getGenom() {
		return genom;
	}

	@Override
	public String toString() {
		return "Gen [displayName=" + displayName + ", alely=" + alely + ", vychoziAlela=" + vychoziAlela + "]";
	}

	public boolean isVypsatelnyVeZhasinaci() {
		return vypsatelnyVeZhasinaci;
	}
}
