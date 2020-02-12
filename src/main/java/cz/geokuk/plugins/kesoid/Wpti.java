package cz.geokuk.plugins.kesoid;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cz.geokuk.core.coordinates.*;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;
import cz.geokuk.plugins.kesoid.kind.KesoidPlugin;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;
import lombok.Getter;
import lombok.Setter;

public class Wpti extends Weikoid0 implements Uchopenec, Wpt {


	public static final String TRADITIONAL_CACHE = "Traditional Cache";

	private static Map<String, String> wptMapping = new HashMap<>();

	static {
		wptMapping.put("Virtual Stage", "Question to Answer");
		wptMapping.put("Physical Stage", "Stages of a Multicache");
	}

	private static int currentSklivecValidityCode;

	/**
	 * Jmené vejpointu, z GPS tag name
	 */
	private String identifier;

	/**
	 * symbol waypointu, zároveˇje jeho typem, určuje, co se zobrazí na napě, není to přímo <sym>
	 */
	private String sym;

	/**
	 * Sožadnice
	 */
	private int elevation;

	// private String prefix;

	private boolean rucnePridany;
	/**
	 * Název waypointu, je to nějaký delší název z GPS to bud <cmt> nebo <desc>, podle toho, co tam je
	 */
	private String nazev;

	// Podpora vykreslování
	private Sklivec sklivec;

	private int sklivecValidityCode;
	public double lat;

	public double lon;
	private int xx = -1;

	private int yy = -1;

	private EZOrder zorder = EZOrder.OTHER;

	private Genotyp genotyp;

	/** Plugin pomocí něhož byl waypoint stvořen. Použije se při
	 * pro polymorfismus chování různých kešoidů */
	@Setter
	private KesoidPlugin kesoidPlugin;

	/** Poddruh kešoidu */
	@Getter @Setter
	private Kepodr kepodr;

	public static void invalidateAllSklivec() {
		currentSklivecValidityCode++;
	}

	/**
	 *
	 */
	public Wpti() {}

	/**
	 * @return the elevation
	 */
	@Override
	public int getElevation() {
		return elevation;
	}

	// public void setPrefix(String prefix) {
	// this.prefix = prefix.intern();
	// }

	@Override
	public Genotyp getGenotyp() {
		if (genotyp == null) {
			throw new IllegalStateException("Prázdný genotyp waypointu: " + this);
		}
		return genotyp;
	}

	public void recomputeGenotypIfExists() {
		if (genotyp != null) {
			computeGenotyp(genotyp.getGenom());
		}

	}

	/**
	 * Pokud neexistuje gentotyp vypočte ho a schová.
	 * @param genom
	 */
	private void computeGenotypIfNotExists(final Genom genom) {
		if (genotyp == null) {
			computeGenotyp(genom);
		}
	}

	private void computeGenotyp(final Genom genom) {
		final GenotypBuilderWpt genotypBuilder = new GenotypBuilderWpt(genom);
		final Genotyp g = genom.UNIVERZALNI_DRUH.genotypVychozi();
		genotyp = getKesoid().doBuildGenotyp(genotypBuilder.build(this, g));
		assert genotyp != null;
	}

	/**
	 * Pokud neexistuje gentotyp vypočte ho a schová.
	 * @param genom
	 */
	@Override
	public void computeGenotypIfNotExistsForAllRing(final Genom genom) {
		Weikoid0 weikoid = this;
		do {
			if (weikoid instanceof Wpti) {
				final Wpti wpti = (Wpti) weikoid;
				wpti.computeGenotypIfNotExists(genom);
			}
			weikoid = weikoid.next;
		} while (weikoid != this);
	}

	// public String getPrefix() {
	// return prefix;
	// }

	@Override
	public Kesoid0 getKesoid() {
		for (Weikoid0 weik = next;; weik = weik.next) {
			if (weik instanceof Kesoid) {
				return (Kesoid0) weik;
			}
		}
	}

	@Override
	public Mou getMou() {
		if (yy == -1) { // testovat yy, protože se nastavuje později
			final Mou mou = getWgs().toMou();
			xx = mou.xx;
			yy = mou.yy;
			return mou;
		} else {
			// System.out.println("kesnuto " + xx + " " + yy);
			return new Mou(xx, yy);
		}
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getNazev() {
		return nazev;
	}

	@Override
	public Sklivec getSklivec() {
		if (sklivecValidityCode != currentSklivecValidityCode) {
			sklivec = null;
		}
		return sklivec;
	}

	@Override
	public String getSym() {
		return sym;
	}

	/**
	 * @return the wgs
	 */
	@Override
	public Wgs getWgs() {
		return new Wgs(lat, lon);
	}

	/**
	 * @return the zorder
	 */
	@Override
	public EZOrder getZorder() {
		return zorder;
	}

	@Override
	public boolean hasEmptyCoords() {
		return getMou().xx == 0 && getMou().yy == 0;
	}

	/**
	 *
	 */
	@Override
	public void invalidate() {
		setSklivec(null);
	}

	@Override
	public boolean isMainWpt() {
		return getKesoid().getMainWpt() == this;
	}

	/**
	 * @return the rucnePridany
	 */
	@Override
	public boolean isRucnePridany() {
		return rucnePridany;
	}

	/**
	 * @param aElevation
	 *            the elevation to set
	 */
	public void setElevation(final int aElevation) {
		elevation = aElevation;
	}

	public void setIdentifier(final String identifier) {
		this.identifier = identifier;
	}

	public void setNazev(final String aNazev) {
		// Je tam strašne moc krátkých názvů jako TrB nebo ZhB
		nazev = aNazev.length() >= 5 ? aNazev : aNazev.intern();
	}

	/**
	 * @param aRucnePridany
	 *            the rucnePridany to set
	 */
	public void setRucnePridany(final boolean aRucnePridany) {
		rucnePridany = aRucnePridany;
	}

	@Override
	public void setSklivec(final Sklivec sklivec) {
		this.sklivec = sklivec;
		sklivecValidityCode = currentSklivecValidityCode;
	}

	public void setSym(final String sym) {
		String adjustedSym = wptMapping.get(sym);
		if (adjustedSym == null) {
			adjustedSym = sym;
		}
		this.sym = adjustedSym == null ? "unknown_sym" : adjustedSym.intern();
	}

	/**
	 * @param aWgs
	 *            the wgs to set
	 */
	public void setWgs(final Wgs aWgs) {
		lat = aWgs.lat;
		lon = aWgs.lon;
	}

	/**
	 * @param zorder
	 *            the zorder to set
	 */
	public void setZorder(final EZOrder zorder) {
		this.zorder = zorder;
	}

	@Override
	public String textToolTipu() {
		final Wpt wpt = this;
		final StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		// TODO Zpbrazení tooltipu nutno dořešit
		// if (wpt.getType() != EKesWptType.CACHE && wpt.getType() != EKesWptType.FINAL_LOCATION) {
		// sb.append("<i>" + wpt.getName() + ": " + wpt.getNazev() + "</i><br>");
		// }
		// sb.append("<b>");
		// sb.append(wpt.getKesoid().getNazev());
		// sb.append("</b>");
		// sb.append("<small>");
		// sb.append(" - ");
		// sb.append(sym);
		// sb.append(" (" + wpt.getKesoid().getIdentifier() + ")");
		// sb.append("</small>");
		// sb.append("<br>");

		getKesoid().prispejDoTooltipu(sb, wpt);

		// sb.append("<br>");
		// sb.append("<br>");
		// sb.append("<b>");
		// sb.append(wpt.getNazev());
		// sb.append("</b>");
		// sb.append("<small>");
		// sb.append(" - ");
		// sb.append(sym);
		// sb.append(" (" + wpt.getName() + ")");
		// sb.append("</small>");

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Wpt [name=" + nazev + ", type="  + ", wgs=" + getWgs() + "] " + (getKesoid() == null ? "" : getKesoid().getIdentifier());
	}

	/**
	 * Odstranění seve z kruhu waipointů. Je to divná metoda, použije se pokud má waypoint prázdné souřadnice.
	 * Vůbewc by neměl takový waypoint v kruhu být.
	 */
	@Override
	public void removeMeFromRing() {
		Weikoid0 weikoid = this;
		while (weikoid.next != this) { // najít v kruhu ten ukazující na mě
			weikoid = weikoid.next;
		}
		// vynechat mě, když vím, že ukazuje na mě. Pokud jsme byl v kruhu sám, nestalo se nic.
		weikoid.next = weikoid.next.next;
	}

	@Override
	public KesoidPlugin getKesoidPlugin() {
		if (kesoidPlugin == null) {
			throw new NullPointerException(this.getNazev() + " nemá kesoidPlugin");
		}
		return kesoidPlugin;
	}

	@Override
	public File getSourceFile() {
		return getKesoid().getSourceFile();
	}


}
