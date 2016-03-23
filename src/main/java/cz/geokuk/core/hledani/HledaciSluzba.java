/**
 *
 */
package cz.geokuk.core.hledani;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author veverka
 *
 */
public class HledaciSluzba {

	private final Map<RefreshorVysledkuHledani<? extends Nalezenec0>, HledaciRunnableSwingWorker<? extends Nalezenec0>> map = new IdentityHashMap<>();

	/**
	 * Spustí vyhledávání zadaným hledačem, zaanou hledací podmínkou a hledá se do zadaného refreshoru. Pokud pro daný refreshoir již běží nějaké hledání, je nejdříve zkanclováno a pak zahájeno nové hledání.
	 * 
	 * @param hledac
	 * @param podm
	 * @param refreshor
	 */
	public synchronized <T extends Nalezenec0> void spustHledani(Hledac0<T> hledac, HledaciPodminka0 podm, RefreshorVysledkuHledani<T> refreshor) {

		{
			HledaciRunnableSwingWorker<?> hledaciRunnableNaKancl = map.remove(refreshor); // zrušit a když to tam je tak zkanclovat
			if (hledaciRunnableNaKancl != null)
				hledaciRunnableNaKancl.cancel(true); // zkanclovat, když se hledalo
			// System.out.println("HLEDACISLUZBA " + System.identityHashCode(hledaciRunnableNaKancl) + ": CANCEL");
		}
		HledaciRunnableSwingWorker<T> hledaciRunnable = new HledaciRunnableSwingWorker<>(new Finishor<>(refreshor), podm, hledac);
		map.put(refreshor, hledaciRunnable);
		// System.out.println("HLEDACISLUZBA " + System.identityHashCode(hledaciRunnable) + ": EXECUTE");
		hledaciRunnable.execute();
	}

	class Finishor<T extends Nalezenec0> {
		private final RefreshorVysledkuHledani<T> refreshor;

		/**
		 * @param refreshor
		 */
		public Finishor(RefreshorVysledkuHledani<T> refreshor) {
			this.refreshor = refreshor;
		}

		void finish(VysledekHledani<T> vysledekHledani) {
			map.remove(refreshor);
			if (vysledekHledani != null) { // poslat dál, jen když nebyl kancel
				refreshor.refreshVysledekHledani(vysledekHledani);
			}
		}

	}

}
