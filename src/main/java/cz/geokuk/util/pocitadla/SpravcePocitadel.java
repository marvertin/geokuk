/**
 *
 */
package cz.geokuk.util.pocitadla;

import java.util.*;

/**
 * @author veverka
 *
 */
public class SpravcePocitadel {

	private static final Map<Pocitadlo, Integer> pocitadla = Collections.synchronizedMap(new WeakHashMap<Pocitadlo, Integer>());

	public static Collection<Pocitadlo> getPocitadla() {
		return pocitadla.keySet();
		// return new ArrayList<Pocitadlo>();
	}

	public static void register(final Pocitadlo pocitadlo) {
		pocitadla.put(pocitadlo, 0);
	}
}
