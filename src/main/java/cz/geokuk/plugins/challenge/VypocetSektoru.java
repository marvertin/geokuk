package cz.geokuk.plugins.challenge;

import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.VysledekSektoru.Kandidati;

/** Výpočet challenge 360°: z nálezů a středu zjistí obsazené sektory, a z nenalezených keší v oblasti kandidáty pro chybějící. */
public final class VypocetSektoru {

	private VypocetSektoru() {}

	/**
	 * @param zdroj
	 *            pravidla, podle kterých se keše vybírají a azimut počítá
	 * @param lat
	 *            střed, ze kterého se měří
	 * @param oblast
	 *            keše musí ležet v oblasti; null = bez omezení
	 * @param ignorovane
	 *            kódy keší, které Stator nepočítá ({@link ZdrojPravidel#STATOR}); u PGC se nepoužijí
	 */
	public static VysledekSektoru spocti(final ZdrojPravidel zdroj, final double lat, final double lon, final Oblast oblast, final Set<String> ignorovane,
			final Iterable<? extends VstupKes> kese) {
		return spocti(zdroj, lat, lon, oblast, ignorovane, kese, () -> false);
	}

	/** Viz {@link #spocti(ZdrojPravidel, double, double, Oblast, Set, Iterable)}; po zrušení vrací null. */
	public static VysledekSektoru spocti(final ZdrojPravidel zdroj, final double lat, final double lon, final Oblast oblast, final Set<String> ignorovane,
			final Iterable<? extends VstupKes> kese, final java.util.function.BooleanSupplier zruseno) {
		if (!jePlatnyStred(lat, lon)) {
			throw new IllegalArgumentException("Střed mimo rozsah souřadnic: " + lat + ", " + lon);
		}
		return spoctiSKlasifikaci(zdroj, lat, lon, primy(zdroj, lat, lon, oblast), ignorovane, kese, zruseno);
	}

	/**
	 * Poloha keše vůči středu a oblasti: leží v oblasti, v kterém sektoru a zda je sektor nejistý. Záleží jen na souřadnicích keše, středu, oblasti a pravidlech, proto
	 * se dá předpočítat a uložit, viz {@link Predpocet}.
	 */
	public interface Klasifikator {
		Klasifikace klasifikuj(VstupKes k);
	}

	/** Výsledek {@link Klasifikator}u zakódovaný do jednoho čísla: sektor (9 bitů), nejistota (2 bity) a příznak, že keš leží v oblasti. */
	public static final class Klasifikace {
		static final int BEZ_NEJISTOTY = 0;
		static final int NEJISTY_DOLU = 1;
		static final int NEJISTY_NAHORU = 2;

		private final short kod;

		Klasifikace(final short kod) {
			this.kod = kod;
		}

		static Klasifikace mimoOblast() {
			return new Klasifikace((short) 0);
		}

		static Klasifikace vOblasti(final int sektor, final int nejistota) {
			return new Klasifikace((short) (sektor | nejistota << 9 | 1 << 11));
		}

		static Klasifikace zKodu(final short kod) {
			return new Klasifikace(kod);
		}

		short kod() {
			return kod;
		}

		public boolean jeVOblasti() {
			return (kod >> 11 & 1) != 0;
		}

		public int sektor() {
			return kod & 0x1FF;
		}

		/** Nejistota: {@link #NEJISTY_DOLU} = soused je sektor pod, {@link #NEJISTY_NAHORU} = nad. */
		int nejistota() {
			return kod >> 9 & 3;
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof Klasifikace && ((Klasifikace) o).kod == kod;
		}

		@Override
		public int hashCode() {
			return kod;
		}
	}

	/** Klasifikace přímým výpočtem z nálezu: leží v oblasti (null = bez omezení), sektor a nejistota podle pravidel. */
	public static Klasifikator primy(final ZdrojPravidel zdroj, final double lat, final double lon, final Oblast oblast) {
		return k -> {
			if (oblast != null && !oblast.obsahuje(k.lat(), k.lon())) {
				return Klasifikace.mimoOblast();
			}
			final double az = azimut(zdroj, lat, lon, k);
			final int s = Geodezie.sektor(az);
			if (Geodezie.jeNejisty(az) || nahrazenoKouli(zdroj, lat, lon, k)) {
				final double zlomek = az - Math.floor(az);
				return Klasifikace.vOblasti(s, zlomek < 0.5 ? Klasifikace.NEJISTY_DOLU : Klasifikace.NEJISTY_NAHORU);
			}
			return Klasifikace.vOblasti(s, Klasifikace.BEZ_NEJISTOTY);
		};
	}

	/** Výpočet s danou klasifikací keší (přímou nebo předpočítanou); střed je potřeba jen pro vzdálenost kandidátů. */
	public static VysledekSektoru spoctiSKlasifikaci(final ZdrojPravidel zdroj, final double lat, final double lon, final Klasifikator klasifikator, final Set<String> ignorovane,
			final Iterable<? extends VstupKes> kese, final java.util.function.BooleanSupplier zruseno) {
		final Set<String> ign = zdroj == ZdrojPravidel.STATOR ? ignorovane : Collections.<String> emptySet();
		final BitSet obsazeneJiste = new BitSet(360);
		final BitSet obsazeneJakkoli = new BitSet(360);
		final BitSet soused = new BitSet(360);
		final Kandidati[] kandidati = new Kandidati[360];
		final java.util.List<VstupKes> nenalezene = new java.util.ArrayList<>();
		final java.util.List<Klasifikace> nenalezeneKlasifikace = new java.util.ArrayList<>();
		int nalezu = 0;
		int poradi = 0;
		for (final VstupKes k : kese) {
			if ((++poradi & 0x3FFF) == 0 && zruseno.getAsBoolean()) {
				return null;
			}
			if (!lzePocitat(zdroj, k, ign)) {
				continue;
			}
			if (k.nalezena()) {
				if (zdroj == ZdrojPravidel.PGC && k.logNaGc() == VstupKes.StavLogu.NE) {
					continue;
				}
				final Klasifikace c = klasifikator.klasifikuj(k);
				if (!c.jeVOblasti()) {
					continue;
				}
				nalezu++;
				final int s = c.sektor();
				obsazeneJakkoli.set(s);
				if (c.nejistota() == Klasifikace.BEZ_NEJISTOTY) {
					obsazeneJiste.set(s);
				} else {
					soused.set(c.nejistota() == Klasifikace.NEJISTY_DOLU ? (s + 359) % 360 : (s + 1) % 360);
				}
			} else if (!k.vlastni() && !k.neaktivni()) {
				final Klasifikace c = klasifikator.klasifikuj(k);
				if (c.jeVOblasti()) {
					nenalezene.add(k);
					nenalezeneKlasifikace.add(c);
				}
			}
		}
		final java.util.List<VysledekSektoru.KandidatKes> vsichni = new java.util.ArrayList<>();
		for (int i = 0; i < nenalezene.size(); i++) {
			final VstupKes k = nenalezene.get(i);
			final int s = nenalezeneKlasifikace.get(i).sektor();
			if (!obsazeneJakkoli.get(s)) {
				vsichni.add(new VysledekSektoru.KandidatKes(k.id(), k.lat(), k.lon(), s));
				if (kandidati[s] == null) {
					kandidati[s] = new Kandidati(s);
				}
				kandidati[s].pridej(k.id(), Geodezie.vzdalenostKoule(lat, lon, k.lat(), k.lon()));
			}
		}
		final BitSet nejiste = new BitSet(360);
		for (int s = 0; s < 360; s++) {
			final boolean jenNejistymi = obsazeneJakkoli.get(s) && !obsazeneJiste.get(s);
			final boolean chybiVedleNejisteho = !obsazeneJakkoli.get(s) && soused.get(s);
			if (jenNejistymi || chybiVedleNejisteho) {
				nejiste.set(s);
			}
		}
		return new VysledekSektoru(obsazeneJakkoli, nejiste, kandidati, nalezu, vsichni);
	}

	/** Střed musí být konečné číslo ve rozsahu souřadnic; jinak by sektory vycházely nesmyslně. */
	static boolean jePlatnyStred(final double lat, final double lon) {
		return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
	}

	/** Elipsoid nekonvergoval (téměř protinožec), azimut je z koule a sektor je proto nejistý. */
	private static boolean nahrazenoKouli(final ZdrojPravidel zdroj, final double lat, final double lon, final VstupKes k) {
		return zdroj == ZdrojPravidel.STATOR && Double.isNaN(Geodezie.azimutElipsoid(lat, lon, k.lat(), k.lon()));
	}

	private static boolean lzePocitat(final ZdrojPravidel zdroj, final VstupKes k, final Set<String> ignorovane) {
		if (k.locationless()) {
			return false;
		}
		if (zdroj == ZdrojPravidel.PGC) {
			return !k.lab();
		}
		return !ignorovane.contains(k.id());
	}

	private static double azimut(final ZdrojPravidel zdroj, final double lat, final double lon, final VstupKes k) {
		if (zdroj == ZdrojPravidel.STATOR) {
			final double e = Geodezie.azimutElipsoid(lat, lon, k.lat(), k.lon());
			if (!Double.isNaN(e)) {
				return e;
			}
		}
		return Geodezie.azimutKoule(lat, lon, k.lat(), k.lon());
	}
}
