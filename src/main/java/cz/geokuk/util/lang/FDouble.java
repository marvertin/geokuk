package cz.geokuk.util.lang;

/**Doplňuje do Double užitečné metody, které by se daly čekat, ale nejsou tam.
 * Všude se používají otevřené intervaly, tedy ty, kde body definující interval
 * už do tohoto intervalu nepatří (neboli ostrá nerovnost). Kdekoliv se v této
 * třídě hovoří o vzdálenosti, myslí se tím rozdíl v absolutní hodnotě.
 * @author  <a href="mailto:michal.polak@turboconsult.cz">Michal Polák</a>
 * @version $Revision: 2 $
 * @see     "TW0139Util.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/util/TwDouble.java 2     21.09.00 9:26 Polakm $"
 */
public class FDouble {

	/**Defaultní epsilon dosazované do isSimilar(a,b).
	 */
	public static double DEFAULTEPSILON = 0.000000000001;

	/**Vrací true, pokud vzdálenost je menší než defaultní epsilon.
	 * Pokud je aspoň jedno číslo null, pak je výsledek vždy false.
	 */
	public static boolean isSimilar(Double a, Double b) {

		return isSimilar(a, b, DEFAULTEPSILON);
	}

	/**Vrací true, pokud vzdálenost je menší než defaultní epsilon.
	 */
	public static boolean isSimilar(double a, double b) {

		return isSimilar(a, b, DEFAULTEPSILON);
	}

	/**Vrací true, pokud vzdálenost je menší než epsilon.
	 * Pokud je aspoň jedno číslo null, pak je výsledek vždy false.
	 */
	public static boolean isSimilar(Double a, Double b, double epsilon) {


		if (a == null || b == null) return false;

		return isSimilar (a.doubleValue(), b.doubleValue(), epsilon);
	}

	/**Vrací true, pokud vzdálenost je menší než epsilon.
	 */
	public static boolean isSimilar(double a, double b, double epsilon) {

		return Math.abs(a - b) < epsilon;
	}

	/**Vrací true, pokud je číslo z otevřeného intervalu.
	 * Pokud je aspoň jedno číslo null, pak je výsledek vždy false.
	 */
	public static boolean isFromInterval(Double a, Double min, Double max) {

		if (a == null || min == null || max == null) return false;

		return isFromInterval(a.doubleValue(), min.doubleValue(), max.doubleValue());
	}

	/**Vrací true, pokud je číslo z otevřeného intervalu.
	 */
	public static boolean isFromInterval(double a, double min, double max) {

		return min < a && a < max;
	}
}
