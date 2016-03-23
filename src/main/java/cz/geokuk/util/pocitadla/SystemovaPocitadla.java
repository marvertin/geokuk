package cz.geokuk.util.pocitadla;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;


public class SystemovaPocitadla {

	private static Timer tm;

	public static Pocitadlo volnaPamet = new PocitadloSystem("Volná paměť [KiB]", "Hodnota Runtime.freeMemory()");
	public static Pocitadlo celkovaPamet = new PocitadloSystem("Celková paměť [KiB]", "Hodnota Runtime.totalMemoty()");
	public static Pocitadlo dostupnychProcesoru = new PocitadloSystem("Počet dostupných procesorů", "Hodnota Runtime.availaibleProcesors()");

	public static Pocitadlo pocetVzorkuSytemovych = new PocitadloSystem("Počet vzorků", "Počet odebraných vzorků paměti a jiných systémových hodnot.");

	public synchronized static void spustPocitani() {
		//		final List<Object> zroutPameti = new ArrayList<Object>();
		if (tm != null) return;
		tm = new Timer(100, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Runtime runtime = Runtime.getRuntime();
				volnaPamet.set((int) (runtime.freeMemory() / 1024));
				celkovaPamet.set((int) (runtime.totalMemory() / 1024));
				dostupnychProcesoru.set(runtime.availableProcessors());
				pocetVzorkuSytemovych.inc();

				//				System.gc();
				//				if (runtime.freeMemory() > 2048 * 1000) {
				//					zroutPameti.add(new byte[1024*1000]);
				//					System.out.println("Odežráno paměti: " + (zroutPameti.size() * 1000) + " KiB");
				//				}
			}
		});
		tm.setRepeats(true);
		tm.start();
	}

	public synchronized static void zastavPocitani() {
		if (tm != null) {
			tm.stop();
			tm = null;
		}
	}
}
