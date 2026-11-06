package cz.geokuk.plugins.challenge;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import cz.geokuk.plugins.kesoid.EKesStatus;
import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.kes.EKesDiffTerRating;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;

/** Přizpůsobení kešoidu z modelu programu pro výpočet challenge. */
public final class KesVstup implements VstupKes, VstupDT, VstupKalendar {

	private final Kesoid kesoid;
	private final Wpt hlavni;
	private final ZoneId pasmo;
	private String typ;

	private KesVstup(final Kesoid kesoid, final Wpt hlavni, final ZoneId pasmo) {
		this.kesoid = kesoid;
		this.hlavni = hlavni;
		this.pasmo = pasmo;
	}

	/** Pro challenge se počítají jen keše z gc.com (kód GC…); jiné kešoidy (OC, waymarky, munzee) a kešoidy bez souřadnic dávají null. */
	public static KesVstup z(final Kesoid kesoid) {
		return z(kesoid, ZoneId.systemDefault());
	}

	/** Jako {@link #z(Kesoid)}; datum nálezu se zapsané s časovou zónou převádí do pásma uživatele. */
	public static KesVstup z(final Kesoid kesoid, final ZoneId pasmo) {
		return jeVhodny(kesoid) ? new KesVstup(kesoid, kesoid.getMainWpt(), Objects.requireNonNull(pasmo, "pasmo")) : null;
	}

	private static boolean jeVhodny(final Kesoid kesoid) {
		if (!(kesoid instanceof Kes) || kesoid.getIdentifier() == null || !kesoid.getIdentifier().startsWith("GC")) {
			return false;
		}
		final Wpt w = kesoid.getMainWpt();
		return w != null && !w.hasEmptyCoords();
	}

	/** Keše z gc.com ze seznamu kešoidů, vytvářené až při průchodu; kešoidy, které se nepočítají, se přeskočí. */
	public static java.util.Collection<KesVstup> zKesoidu(final java.util.List<? extends Kesoid> kesoidy) {
		return zKesoidu(kesoidy, ZoneId.systemDefault());
	}

	/** Jako {@link #zKesoidu(java.util.List)} s pásmem pro datum nálezu. */
	public static java.util.Collection<KesVstup> zKesoidu(final java.util.List<? extends Kesoid> kesoidy, final ZoneId pasmo) {
		Objects.requireNonNull(pasmo, "pasmo");
		return new java.util.AbstractCollection<KesVstup>() {
			@Override
			public java.util.Iterator<KesVstup> iterator() {
				final java.util.Iterator<? extends Kesoid> it = kesoidy.iterator();
				return new java.util.Iterator<KesVstup>() {
					private KesVstup dalsi;

					@Override
					public boolean hasNext() {
						while (dalsi == null && it.hasNext()) {
							dalsi = z(it.next(), pasmo);
						}
						return dalsi != null;
					}

					@Override
					public KesVstup next() {
						if (!hasNext()) {
							throw new java.util.NoSuchElementException();
						}
						final KesVstup v = dalsi;
						dalsi = null;
						return v;
					}
				};
			}

			private int velikost = -1;

			/** Počet keší, které se projdou; spočítá se jednou při prvním dotazu (seznam se za dobu předpočtu nemění). */
			@Override
			public synchronized int size() {
				if (velikost < 0) {
					int n = 0;
					for (final Kesoid k : kesoidy) {
						if (jeVhodny(k)) {
							n++;
						}
					}
					velikost = n;
				}
				return velikost;
			}
		};
	}

	@Override
	public String id() {
		return kesoid.getIdentifier();
	}

	@Override
	public double lat() {
		return hlavni.getWgs().lat;
	}

	@Override
	public double lon() {
		return hlavni.getWgs().lon;
	}

	@Override
	public boolean nalezena() {
		return kesoid.getVztah() == EKesVztah.FOUND;
	}

	@Override
	public boolean vlastni() {
		return kesoid.getVztah() == EKesVztah.OWN;
	}

	@Override
	public boolean neaktivni() {
		return kesoid.getStatus() != EKesStatus.ACTIVE;
	}

	/** Typy, které se zatím nenabízejí jako kandidáti na chybějící sektor; nálezy se počítají normálně. */
	private static final java.util.Set<String> MIMO_KANDIDATY = new java.util.HashSet<>(java.util.Arrays.asList("gps adventures exhibit", "community celebration event",
			"lost and found event caches", "giga-event cache"));

	/** Typ keše podle gc.com (hlavní waypoint ho nese v symbolu, i u nalezených keší). */
	private String typ() {
		if (typ == null) {
			final String sym = hlavni.getSym();
			typ = sym == null ? "" : sym.trim().toLowerCase(java.util.Locale.ROOT);
		}
		return typ;
	}

	@Override
	public boolean lab() {
		return "lab cache".equals(typ());
	}

	@Override
	public boolean locationless() {
		return typ().startsWith("locationless");
	}

	@Override
	public boolean mimoKandidaty() {
		return MIMO_KANDIDATY.contains(typ());
	}

	@Override
	public StavLogu logNaGc() {
		return StavLogu.NEZNAMY;
	}

	@Override
	public int obtiznost() {
		final EKesDiffTerRating r = ((Kes) kesoid).getDifficulty();
		return r == null ? -1 : r.ordinal();
	}

	@Override
	public int teren() {
		final EKesDiffTerRating r = ((Kes) kesoid).getTerrain();
		return r == null ? -1 : r.ordinal();
	}

	/** Datum nálezu, jen u nalezené keše. */
	@Override
	public LocalDate datumNalezu() {
		return nalezena() ? DatumyKese.datum(((Kes) kesoid).getFountTime(), pasmo) : null;
	}

	@Override
	public LocalDate datum() {
		return DatumyKese.datum(kesoid.getHidden(), null);
	}

	@Override
	public boolean event() {
		final String t = typ();
		return t.contains("event") || t.startsWith("geocaching hq");
	}
}
