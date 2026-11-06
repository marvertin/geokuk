package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Definice challenge jako data: typ, střed, oblast a zdroj pravidel. Nenese žádný pevný případ; šablony jsou jen předvyplněné hodnoty, viz {@link Sablony}.
 * Do plochého textového tvaru a zpět jde převést přes {@link #naMapu()} a {@link #zMapy(Map)}.
 */
public final class Challenge {

	public enum Typ {
		/** Úhlové sektory po stupni okolo středu: každý ze 360 sektorů má mít nález. */
		SEKTORY_360,
		/** Matice obtížnost × terén: každé z 81 políček (po půlkách 1 až 5) má mít nález. */
		DT_MATICE,
		/** Kalendář: každý den v roce má mít nález. */
		KALENDAR
	}

	/** Odkud se měří azimut. */
	public enum DruhStredu {
		/** Domovské souřadnice uživatele. */
		DOMOV,
		/** Zadané souřadnice. */
		SOURADNICE,
		/** Souřadnice keše podle kódu (výchozí bod listingu). */
		LISTING
	}

	public enum DruhOblasti {
		/** Bez omezení oblasti. */
		ZADNA,
		/** Stát; {@link Oblast#idOblasti} má jedno označení. */
		STAT,
		/** Sjednocení oblastí (okresy, polygony); bod patří, leží-li v kterékoli. */
		SADA
	}

	/** Které nálezy se počítají. */
	public enum ZdrojeNalezu {
		/** Nálezy ze zapnutých zdrojů dat, tedy to, co uživatel vidí. */
		ZAPNUTE,
		/** Nálezy ze všech načtených zdrojů, i vypnutých; zatím bez účinku, protože načtená data vypnuté zdroje neobsahují. */
		VSECHNY
	}

	/** Pravidla, podle kterých se počítá. */
	public enum ZdrojPravidel {
		/** Project-GC: koule, bez Lab a Locationless, jen nálezy s logem na gc.com. */
		PGC,
		/** Stator: elipsoid WGS84, včetně Lab, bez Locationless a ignorovaných. */
		STATOR
	}

	/** Střed: druh a podle něj souřadnice nebo kód keše. */
	public static final class Stred {
		private final DruhStredu druh;
		private final Double lat;
		private final Double lon;
		private final String kodListingu;

		private Stred(final DruhStredu druh, final Double lat, final Double lon, final String kodListingu) {
			this.druh = druh;
			this.lat = lat;
			this.lon = lon;
			this.kodListingu = kodListingu;
		}

		public static Stred domov() {
			return new Stred(DruhStredu.DOMOV, null, null, null);
		}

		public static Stred souradnice(final double lat, final double lon) {
			if (!VypocetSektoru.jePlatnyStred(lat, lon)) {
				throw new IllegalArgumentException("Střed mimo rozsah souřadnic: " + lat + ", " + lon);
			}
			return new Stred(DruhStredu.SOURADNICE, lat, lon, null);
		}

		public static Stred listing(final String kod) {
			return new Stred(DruhStredu.LISTING, null, null, Objects.requireNonNull(kod));
		}

		public DruhStredu druh() {
			return druh;
		}

		public double lat() {
			return lat;
		}

		public double lon() {
			return lon;
		}

		public String kodListingu() {
			return kodListingu;
		}

		@Override
		public boolean equals(final Object o) {
			if (!(o instanceof Stred)) {
				return false;
			}
			final Stred s = (Stred) o;
			return druh == s.druh && Objects.equals(lat, s.lat) && Objects.equals(lon, s.lon) && Objects.equals(kodListingu, s.kodListingu);
		}

		@Override
		public int hashCode() {
			return Objects.hash(druh, lat, lon, kodListingu);
		}
	}

	/** Oblast: druh a označení oblastí, které ji tvoří, viz {@link PoskytovatelOblasti}. */
	public static final class OblastDef {
		private final DruhOblasti druh;
		private final List<String> idOblasti;

		private OblastDef(final DruhOblasti druh, final List<String> idOblasti) {
			this.druh = druh;
			this.idOblasti = Collections.unmodifiableList(new ArrayList<>(idOblasti));
		}

		public static OblastDef zadna() {
			return new OblastDef(DruhOblasti.ZADNA, Collections.<String> emptyList());
		}

		public static OblastDef stat(final String id) {
			return new OblastDef(DruhOblasti.STAT, Collections.singletonList(id));
		}

		public static OblastDef sada(final String... id) {
			if (id.length == 0) {
				throw new IllegalArgumentException("Sada oblastí je prázdná");
			}
			return new OblastDef(DruhOblasti.SADA, java.util.Arrays.asList(id));
		}

		public DruhOblasti druh() {
			return druh;
		}

		public List<String> idOblasti() {
			return idOblasti;
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof OblastDef && druh == ((OblastDef) o).druh && idOblasti.equals(((OblastDef) o).idOblasti);
		}

		@Override
		public int hashCode() {
			return Objects.hash(druh, idOblasti);
		}
	}

	private final String nazev;
	private final Typ typ;
	private final Stred stred;
	private final OblastDef oblast;
	private final ZdrojPravidel zdroj;
	private final ZdrojeNalezu nalezy;

	public Challenge(final String nazev, final Typ typ, final Stred stred, final OblastDef oblast, final ZdrojPravidel zdroj) {
		this(nazev, typ, stred, oblast, zdroj, ZdrojeNalezu.ZAPNUTE);
	}

	public Challenge(final String nazev, final Typ typ, final Stred stred, final OblastDef oblast, final ZdrojPravidel zdroj, final ZdrojeNalezu nalezy) {
		this.nalezy = Objects.requireNonNull(nalezy);
		this.nazev = Objects.requireNonNull(nazev);
		this.typ = Objects.requireNonNull(typ);
		this.stred = Objects.requireNonNull(stred);
		this.oblast = Objects.requireNonNull(oblast);
		this.zdroj = Objects.requireNonNull(zdroj);
	}

	public String nazev() {
		return nazev;
	}

	public Typ typ() {
		return typ;
	}

	public Stred stred() {
		return stred;
	}

	public OblastDef oblast() {
		return oblast;
	}

	public ZdrojPravidel zdroj() {
		return zdroj;
	}

	public ZdrojeNalezu nalezy() {
		return nalezy;
	}

	/** Stejná definice pod jiným názvem. */
	public Challenge sNazvem(final String novyNazev) {
		return new Challenge(novyNazev, typ, stred, oblast, zdroj, nalezy);
	}

	/** Ploché vyjádření definice (klíč → text) pro uložení; pořadí klíčů je stálé. */
	public Map<String, String> naMapu() {
		final Map<String, String> m = new LinkedHashMap<>();
		m.put("nazev", nazev);
		m.put("typ", typ.name());
		m.put("zdroj", zdroj.name());
		m.put("nalezy", nalezy.name());
		m.put("stred", stred.druh.name());
		if (stred.druh == DruhStredu.SOURADNICE) {
			m.put("stred.lat", Double.toString(stred.lat));
			m.put("stred.lon", Double.toString(stred.lon));
		} else if (stred.druh == DruhStredu.LISTING) {
			m.put("stred.kod", stred.kodListingu);
		}
		m.put("oblast", oblast.druh.name());
		if (!oblast.idOblasti.isEmpty()) {
			m.put("oblast.id", String.join(",", oblast.idOblasti));
		}
		return m;
	}

	/** Opak {@link #naMapu()}; neúplná nebo nesmyslná data jsou {@link IllegalArgumentException}. */
	public static Challenge zMapy(final Map<String, String> m) {
		try {
			final Typ typ = Typ.valueOf(povinne(m, "typ"));
			final ZdrojPravidel zdroj = ZdrojPravidel.valueOf(povinne(m, "zdroj"));
			final Stred stred;
			switch (DruhStredu.valueOf(povinne(m, "stred"))) {
			case DOMOV:
				stred = Stred.domov();
				break;
			case SOURADNICE:
				stred = Stred.souradnice(Double.parseDouble(povinne(m, "stred.lat")), Double.parseDouble(povinne(m, "stred.lon")));
				break;
			default:
				stred = Stred.listing(povinne(m, "stred.kod"));
				break;
			}
			final OblastDef oblast;
			switch (DruhOblasti.valueOf(povinne(m, "oblast"))) {
			case ZADNA:
				oblast = OblastDef.zadna();
				break;
			case STAT:
				oblast = OblastDef.stat(povinne(m, "oblast.id"));
				break;
			default:
				oblast = OblastDef.sada(povinne(m, "oblast.id").split(","));
				break;
			}
			final String nalezy = m.get("nalezy");
			return new Challenge(povinne(m, "nazev"), typ, stred, oblast, zdroj, nalezy == null || nalezy.isEmpty() ? ZdrojeNalezu.ZAPNUTE : ZdrojeNalezu.valueOf(nalezy));
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Neplatné souřadnice středu: " + e.getMessage(), e);
		}
	}

	private static String povinne(final Map<String, String> m, final String klic) {
		final String v = m.get(klic);
		if (v == null || v.isEmpty()) {
			throw new IllegalArgumentException("Chybí údaj " + klic);
		}
		return v;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Challenge)) {
			return false;
		}
		final Challenge c = (Challenge) o;
		return nazev.equals(c.nazev) && typ == c.typ && stred.equals(c.stred) && oblast.equals(c.oblast) && zdroj == c.zdroj && nalezy == c.nalezy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nazev, typ, stred, oblast, zdroj, nalezy);
	}

	@Override
	public String toString() {
		return nazev + " " + naMapu();
	}
}
