package cz.geokuk.plugins.challenge;

import java.util.Arrays;

/** Výběr prvků s nejmenší hodnotou (vzdáleností), bez alokace na prvek. */
final class NejblizsiPrvky {

	private NejblizsiPrvky() {
	}

	/** Indexy {@code kolik} prvků z prvních {@code n} hodnot s nejmenší hodnotou, seřazené podle hodnoty vzestupně. */
	static int[] vyber(final double[] hodnoty, final int n, final int kolik) {
		final int[] poradi = new int[kolik];
		if (kolik == 0) {
			return poradi;
		}
		final double[] serazene = Arrays.copyOf(hodnoty, n);
		Arrays.sort(serazene);
		final double prah = serazene[kolik - 1];
		int m = 0;
		for (int i = 0; i < n && m < kolik; i++) {
			if (hodnoty[i] <= prah) {
				int j = m++;
				while (j > 0 && hodnoty[poradi[j - 1]] > hodnoty[i]) {
					poradi[j] = poradi[j - 1];
					j--;
				}
				poradi[j] = i;
			}
		}
		return poradi;
	}
}
