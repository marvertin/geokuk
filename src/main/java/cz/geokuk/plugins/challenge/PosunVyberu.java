package cz.geokuk.plugins.challenge;

import java.util.List;

/**
 * Posun výběru o jednu položku (◀ ▶): u 360° další chybějící sektor s kandidáty po kružnici 0 až 359, u matice D/T další chybějící políčko s kandidáty po řádcích
 * (D1/T1, D1/T1,5 … D1/T5, D1,5/T1 …). Za posledním následuje první a naopak. Výběr se nepřeskakuje na keše, jen mezi sektory a políčky.
 */
public final class PosunVyberu {

	/** Nic nevybráno. */
	public static final int ZADNY_SEKTOR = -1;

	private PosunVyberu() {}

	/**
	 * @param aktualni
	 *            sektor 0 až 359 nebo {@link #ZADNY_SEKTOR}; sektor bez kandidátů (třeba už zaplněný) se bere jako výchozí bod stejně
	 * @param dopredu
	 *            true = vzestupně, false = sestupně
	 * @return sousední sektor s kandidáty po kružnici, bez výběru první (vpřed) nebo poslední (zpět); při jediném sektoru s kandidáty tentýž; -1, když žádný není
	 */
	public static int dalsiSektor(final VysledekSektoru vysledek, final int aktualni, final boolean dopredu) {
		if (aktualni < ZADNY_SEKTOR || aktualni > 359) {
			throw new IllegalArgumentException("Sektor mimo 0 až 359: " + aktualni);
		}
		final int krok = dopredu ? 1 : -1;
		final int zacatek = aktualni == ZADNY_SEKTOR ? (dopredu ? 359 : 0) : aktualni;
		for (int i = 1; i <= 360; i++) {
			final int s = Math.floorMod(zacatek + krok * i, 360);
			final VysledekSektoru.Kandidati k = vysledek.kandidati(s);
			if (k != null && k.pocet() > 0) {
				return s;
			}
		}
		return ZADNY_SEKTOR;
	}

	/**
	 * @param aktualni
	 *            štítek vybraného políčka nebo null; neznámý štítek se bere jako výchozí bod před prvním políčkem
	 * @return sousední chybějící políčko s kandidáty v pořadí {@link VysledekPolicek#stitkyChybejicich()}; při jediném tomtéž; null, když žádné není
	 */
	public static String dalsiPolicko(final VysledekPolicek vysledek, final String aktualni, final boolean dopredu) {
		final List<String> stitky = vysledek.stitkyChybejicich();
		final int n = stitky.size();
		if (n == 0) {
			return null;
		}
		final int index = aktualni == null ? -1 : stitky.indexOf(aktualni);
		final int krok = dopredu ? 1 : -1;
		final int zacatek = index >= 0 ? index : (dopredu ? n - 1 : 0);
		for (int i = 1; i <= n; i++) {
			final String s = stitky.get(Math.floorMod(zacatek + krok * i, n));
			if (vysledek.pocetKandidatu(s) > 0) {
				return s;
			}
		}
		return null;
	}
}
