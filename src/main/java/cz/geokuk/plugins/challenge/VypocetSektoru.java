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
		final Set<String> ign = zdroj == ZdrojPravidel.STATOR ? ignorovane : Collections.<String> emptySet();
		final BitSet obsazeneJiste = new BitSet(360);
		final BitSet obsazeneJakkoli = new BitSet(360);
		final BitSet soused = new BitSet(360);
		final Kandidati[] kandidati = new Kandidati[360];
		final java.util.List<VstupKes> nenalezene = new java.util.ArrayList<>();
		int nalezu = 0;
		int poradi = 0;
		for (final VstupKes k : kese) {
			if ((++poradi & 0x3FFF) == 0 && zruseno.getAsBoolean()) {
				return null;
			}
			if (!lzePocitat(zdroj, k, ign)) {
				continue;
			}
			if (oblast != null && !oblast.obsahuje(k.lat(), k.lon())) {
				continue;
			}
			if (k.nalezena()) {
				if (zdroj == ZdrojPravidel.PGC && k.logNaGc() == VstupKes.StavLogu.NE) {
					continue;
				}
				nalezu++;
				final double az = azimut(zdroj, lat, lon, k);
				final int s = Geodezie.sektor(az);
				obsazeneJakkoli.set(s);
				if (Geodezie.jeNejisty(az) || nahrazenoKouli(zdroj, lat, lon, k)) {
					final double zlomek = az - Math.floor(az);
					soused.set(zlomek < 0.5 ? (s + 359) % 360 : (s + 1) % 360);
				} else {
					obsazeneJiste.set(s);
				}
			} else {
				if (!k.vlastni() && !k.neaktivni()) {
					nenalezene.add(k);
				}
			}
		}
		for (final VstupKes k : nenalezene) {
			final int s = Geodezie.sektor(azimut(zdroj, lat, lon, k));
			if (!obsazeneJakkoli.get(s)) {
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
		return new VysledekSektoru(obsazeneJakkoli, nejiste, kandidati, nalezu);
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
