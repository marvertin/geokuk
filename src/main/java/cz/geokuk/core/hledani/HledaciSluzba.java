/**
 *
 */
package cz.geokuk.core.hledani;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Martin Veverka
 *
 */
public class HledaciSluzba {

	class Finishor<T extends Nalezenec0> {
		private final RefreshorVysledkuHledani<T> refreshor;

		/**
		 * @param refreshor
		 */
		public Finishor(final RefreshorVysledkuHledani<T> refreshor) {
			this.refreshor = refreshor;
		}

		void finish(final VysledekHledani<T> vysledekHledani) {
			map.remove(refreshor);
			if (vysledekHledani != null) { // poslat dál, jen když nebyl kancel
				refreshor.refreshVysledekHledani(vysledekHledani);
			}
		}

	}

	private final Map<RefreshorVysledkuHledani<? extends Nalezenec0>, HledaciRunnableSwingWorker<? extends Nalezenec0>> map = new IdentityHashMap<>();

	/**
	 * Spustí vyhledávání zadaným hledačem, zaanou hledací podmínkou a hledá se do zadaného refreshoru. Pokud pro daný refreshoir již běží nějaké hledání, je nejdříve zkanclováno a pak zahájeno nové hledání.
	 *
	 * @param hledac
	 * @param podm
	 * @param refreshor
	 */
	public synchronized <T extends Nalezenec0> void spustHledani(final Hledac0<T> hledac, final HledaciPodminka0 podm, final RefreshorVysledkuHledani<T> refreshor) {

		{
			final HledaciRunnableSwingWorker<?> hledaciRunnableNaKancl = map.remove(refreshor); // zrušit a když to tam je tak zkanclovat
			if (hledaciRunnableNaKancl != null) {
				hledaciRunnableNaKancl.cancel(true); // zkanclovat, když se hledalo
				// System.out.println("HLEDACISLUZBA " + System.identityHashCode(hledaciRunnableNaKancl) + ": CANCEL");
			}
		}
		final HledaciRunnableSwingWorker<T> hledaciRunnable = new HledaciRunnableSwingWorker<>(new Finishor<>(refreshor), podm, hledac);
		map.put(refreshor, hledaciRunnable);
		// System.out.println("HLEDACISLUZBA " + System.identityHashCode(hledaciRunnable) + ": EXECUTE");
		hledaciRunnable.execute();
	}

}
