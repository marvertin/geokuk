package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Determines a property of a {@link cz.geokuk.plugins.kesoid.Kesoid}.
 *
 * <p>
 * For example, gen can be {@code size of the cache} or {@code type of the waypoint}.
 */
public class Gen {

	private final String nazev;
	private final Set<Alela> alely = new LinkedHashSet<>();
	private Alela vychoziAlela;
	private boolean locked;
	private final Genom genom;
	private boolean vypsatelnyVeZhasinaci;
	final int poradiVGenomu;
	private String displayName;

	public Gen(final String nazev, final int poradiVGenomu, final Genom genom) {
		this.nazev = nazev;
		this.poradiVGenomu = poradiVGenomu;
		this.genom = genom;
	}

	public synchronized void add(final Alela alela) {
		if (locked) {
			throw new RuntimeException("Nemozne pridavat alely " + alela + " k zamcenemu genu " + this);
		}
		final Gen puvodniGen = alela.hasGen() ? alela.getGen() : null;
		if (puvodniGen != this) {
			if (puvodniGen != null) {
				//puvodniGen.alely.remove(alela);
				throw new RuntimeException("Alela už měla gen, nelze přehazovat alely" + alela + " " + puvodniGen);
			}
			alely.add(alela);
			alela.setGen(this);
		}
		if (vychoziAlela == null) {
			// první alela, která přijde se stává výchozí alelou a už se to nezmění
			vychoziAlela = alela;
		}
	}

	public Alela alela(final String nazev) {
		final Alela alela = genom.alela(nazev);
		add(alela);
		return alela;
	}

	public Gen displayName(final String displayName) {
		this.displayName = displayName;
		return this;
	}

	public Set<Alela> getAlely() {
		return alely;
	}

	public String getDisplayName() {
		return displayName == null ? nazev : displayName;
	}

	public Genom getGenom() {
		return genom;
	}

	public Alela getVychoziAlela() {
		assert vychoziAlela != null : "Na genu: " + nazev;
		return vychoziAlela;
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
		return "Gen [displayName=" + nazev + ", alely=" + alely + ", vychoziAlela=" + vychoziAlela + "]";
	}

}
