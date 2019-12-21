/**
 *
 */
package cz.geokuk.plugins.kesoidpopisky;

import java.util.*;

import cz.geokuk.plugins.kesoid.Wpt;

/**
 * Třída, která sestavuje popisky. Její stvoření může trvat, ale její provádění nikoli
 *
 * @author Martin Veverka
 *
 *
 *
 *         {wpt} - waypoint {typ1} - označení typu (jedno písmeno) {velikost} - označení velikosti {velikost1} - označení velikosti (jedno písmeno) {obtiznost} - označení obtížnosti {obtiznost1} - označení obtížnosti (jedno písmeno) {teren} - označení terenu {teren1} - označení terenu (jedno
 *         písmeno) {autor} - autor kešky {nazev} - název kešky {zalozeno} - datum založení {nbsp} - mezera (neprovádí se na ní zalomení), jen popisek {br} - odřádkování, jen popisek
 */
public class SestavovacPopisku {


	/**
	 * Oddělovač řádků. Lze ho najít
	 *
	 * @author Martin Veverka
	 *
	 */
	private static class NahrBr implements PopiskyNahrazovac {

		@Override
		public void pridej(final StringBuilder sb, final Wpt wpt) {}

	}

	public static class NahrKonstantni implements PopiskyNahrazovac {

		private final String konstatna;

		/**
		 *
		 */
		public NahrKonstantni(final String konstatna) {
			this.konstatna = konstatna;
		}

		@Override
		public void pridej(final StringBuilder sb, final Wpt wpt) {
			sb.append(konstatna);
		}

	}


	public static final NahrBr NAHRBR = new NahrBr();


	private final PopiskyNahrazovac[] nahrazky;

	private final int pocetRadku;

	private final Map<String, PopiskyNahrazovac> iNahrazovace;

	/**
	 *
	 */
	public SestavovacPopisku(final String pattern, final Map<String, PopiskyNahrazovac> aNahrazovace) {
		this.iNahrazovace = aNahrazovace;
		final List<PopiskyNahrazovac> nahrazovace = new ArrayList<>();
		vytvorNahrazovace(nahrazovace, pattern);
		int n = 1;
		for (final PopiskyNahrazovac nahr : nahrazovace) {
			if (nahr == NAHRBR) {
				n++;
			}
		}
		pocetRadku = n;
		nahrazky = nahrazovace.toArray(new PopiskyNahrazovac[nahrazovace.size()]);
	}

	public String[] sestavPopisek(final Wpt wpt) {
		int n = 0;
		final String[] popisky = new String[pocetRadku];
		final StringBuilder sb = new StringBuilder();
		for (final PopiskyNahrazovac nahr : nahrazky) {
			nahr.pridej(sb, wpt);
			if (nahr == NAHRBR) {
				popisky[n] = sb.toString();
				n++;
				sb.setLength(0);
			}
		}
		popisky[n] = sb.toString();
		return popisky;
	}

	private void vytvorNahrazovace(final List<PopiskyNahrazovac> nahrazovace, final String vzorek) {
		if (vzorek.length() == 0) {
			return;
		}
		for (final Map.Entry<String, PopiskyNahrazovac> entry : iNahrazovace.entrySet()) {
			final int delka = entry.getKey().length();
			final int poz = vzorek.indexOf(entry.getKey());
			if (poz >= 0) { // našli jsme některý
				vytvorNahrazovace(nahrazovace, vzorek.substring(0, poz));
				nahrazovace.add(entry.getValue());
				vytvorNahrazovace(nahrazovace, vzorek.substring(poz + delka));
				return; // jednu jsme našli a vyřešili, jiné se najdou jinde
			}
		}
		nahrazovace.add(new NahrKonstantni(vzorek));
	}
}