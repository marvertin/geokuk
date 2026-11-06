package cz.geokuk.plugins.challenge;

import java.util.List;

import cz.geokuk.plugins.challenge.VysledekSektoru.KandidatKes;

/**
 * Skok na nejbližšího kandidáta ve směru: z dané polohy (vybraný kandidát, jinak střed mapy) na nejbližšího kandidáta v kuželu ±{@value #POLOVINA_KUZELE} stupňů kolem
 * azimutu směru (sever 0, východ 90, jih 180, západ 270). Hranice kuželu patří dovnitř. Při shodě vzdálenosti vyhraje kandidát s nižším sektorem (u D/T bez sektoru
 * rozhoduje kód keše), takže výsledek nezávisí na pořadí kandidátů. V cyklu se nealokuje, jen jednou na volání.
 */
public final class SkokVeSmeru {

	/** Polovina úhlu kuželu ve stupních. */
	public static final double POLOVINA_KUZELE = 45;

	private SkokVeSmeru() {}

	/** Nejlepší dosud nalezený kandidát. */
	private static final class Nejlepsi {
		final double lat;
		final double lon;
		final double smer;
		final String vynechat;
		String id;
		int sektor;
		double lat0;
		double lon0;
		String stitek;
		double vzdalenost = Double.MAX_VALUE;

		Nejlepsi(final double lat, final double lon, final double smer, final String vynechat) {
			this.lat = lat;
			this.lon = lon;
			this.smer = smer;
			this.vynechat = vynechat;
		}

		void zvaz(final String idKese, final int sektorKese, final double latKese, final double lonKese, final String stitekKese) {
			if (idKese.equals(vynechat)) {
				return;
			}
			final double d = Geodezie.vzdalenostKoule(lat, lon, latKese, lonKese);
			if (d < Geodezie.STEJNY_BOD_M || d > vzdalenost) {
				return;
			}
			if (d == vzdalenost && !jeLepsiPriShode(idKese, sektorKese)) {
				return;
			}
			if (!jeVKuzelu(Geodezie.azimutKoule(lat, lon, latKese, lonKese), smer)) {
				return;
			}
			id = idKese;
			sektor = sektorKese;
			lat0 = latKese;
			lon0 = lonKese;
			stitek = stitekKese;
			vzdalenost = d;
		}

		VybranyKandidat vysledek() {
			return id == null ? null : new VybranyKandidat(id, lat0, lon0, sektor, vzdalenost, stitek);
		}

		private boolean jeLepsiPriShode(final String idKese, final int sektorKese) {
			return sektorKese < sektor || sektorKese == sektor && idKese.compareTo(id) < 0;
		}
	}

	/** Leží azimut keše v kuželu kolem směru? Hranice patří dovnitř. */
	static boolean jeVKuzelu(final double azimutKese, final double smer) {
		return rozdilUhlu(azimutKese, smer) <= POLOVINA_KUZELE;
	}

	/** Úhlový rozdíl dvou azimutů ve stupních, 0 až 180. */
	static double rozdilUhlu(final double a, final double b) {
		final double r = Math.abs(a - b) % 360;
		return r > 180 ? 360 - r : r;
	}

	private static void zkontroluj(final double lat, final double lon, final double azimutSmeru) {
		if (!VypocetSektoru.jePlatnyStred(lat, lon)) {
			throw new IllegalArgumentException("Poloha mimo rozsah souřadnic: " + lat + ", " + lon);
		}
		if (Double.isNaN(azimutSmeru) || Double.isInfinite(azimutSmeru)) {
			throw new IllegalArgumentException("Směr není konečné číslo: " + azimutSmeru);
		}
	}

	/**
	 * Kandidát chybějícího sektoru 360° ve směru.
	 *
	 * @param vynechat
	 *            kód vybraného kandidáta, na kterém se stojí (nevybere se znovu); null = nic
	 * @return nejbližší kandidát (vzdálenost od zadané polohy), nebo null, když v kuželu nikdo není
	 */
	public static VybranyKandidat proSektory(final VysledekSektoru vysledek, final double lat, final double lon, final double azimutSmeru, final String vynechat) {
		zkontroluj(lat, lon, azimutSmeru);
		final Nejlepsi n = new Nejlepsi(lat, lon, azimutSmeru, vynechat);
		final List<KandidatKes> kandidati = vysledek.vsichniKandidati();
		for (int i = 0; i < kandidati.size(); i++) {
			final KandidatKes k = kandidati.get(i);
			n.zvaz(k.id(), k.sektor(), k.lat(), k.lon(), null);
		}
		return n.vysledek();
	}

	/** Kandidát chybějícího políčka D/T (nebo dne kalendáře) ve směru; viz {@link #proSektory}. */
	public static VybranyKandidat proPolicka(final VysledekPolicek vysledek, final double lat, final double lon, final double azimutSmeru, final String vynechat) {
		zkontroluj(lat, lon, azimutSmeru);
		final Nejlepsi n = new Nejlepsi(lat, lon, azimutSmeru, vynechat);
		final List<KandidatPolicka> kandidati = vysledek.kandidati();
		for (int i = 0; i < kandidati.size(); i++) {
			final KandidatPolicka k = kandidati.get(i);
			n.zvaz(k.id(), -1, k.lat(), k.lon(), k.popis());
		}
		return n.vysledek();
	}
}
