package cz.geokuk.plugins.mapy.kachle.podklady;

import java.awt.AWTEvent;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.*;

import cz.geokuk.core.coord.PrvniNacitaniMapy;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.napoveda.Diagnostika;
import cz.geokuk.core.napoveda.Vykon;
import cz.geokuk.core.onoffline.OnofflineModel;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.mapy.kachle.data.DiagnosticsData;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.data.Ka;
import cz.geokuk.plugins.mapy.kachle.data.KaLoc;
import cz.geokuk.plugins.mapy.kachle.podklady.KachleManager.ItemToSave;
import cz.geokuk.plugins.mapy.kachle.podklady.KachloDownloader.EPraznyObrazek;
import cz.geokuk.util.exception.*;
import cz.geokuk.util.pocitadla.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * V novém pojetí zodpovídá za získávání kachlí z disku, paměti i downloadování.
 *
 * @author Martin Veverka
 *
 */
@Slf4j
public class KachleZiskavac {

	/**
	 * Objekt zodpovědný za držení jedné kachle a její získání. Je zároveň v mapě jako keš.
	 *
	 * @author Martin
	 *
	 */

	class Kachlice {
		private final Ka ka;
		private final DvojiceExekucnichSluzeb des;
		private final DiagnosticsData diagnosticsData;

		// Dohromady stav kachlice.
		//    ! image & ! futura & irs = 0 ........ počáteční stav po vytvoření, stav po zkanclování dotažení
		//    ! image & futura   & irc > 0 ........ probíhá získávání
		//    image   & ! futura & irc = 0 ........ mám obrázek
		// jiné stavy nejsou možné nebo jinak:
		//   image / futura    ano     ne
		//              ano     -      irc=0
		//               ne    irc>0   irc=0
		// čili, když je něco v sezanmu pro výslede, je futura, kterou se získává a není image
		private Image image;
		private Future<?> futura;
		/** Stahování už posílá požadavek na server: při zrušení doběhne, aby se tatáž dlaždice nestahovala dvakrát. */
		private volatile boolean stahujeSe;
		private final List<ImageReceiver> irs = new ArrayList<ImageReceiver>();
		/** Kopie {@link #irs} pro výpočet pořadí ve frontách, který nesmí brát zámek dlaždice. */
		private volatile ImageReceiver[] prijemciProRazeni = new ImageReceiver[0];

		public Kachlice(final Ka ka, final DvojiceExekucnichSluzeb des, final DiagnosticsData diagnosticsData) {
			super();
			this.ka = ka;
			this.des = des;
			this.diagnosticsData = diagnosticsData;
			pocitKachlice.inc();
			//System.out.println("Vytvořena kachlice pro: " + ka);
		}

		private void obnovPrijemceProRazeni() {
			prijemciProRazeni = irs.toArray(new ImageReceiver[0]);
		}

		/** Naléhavost nejnaléhavějšího příjemce, {@link ImageReceiver#ZASTARALA}, když dlaždici nikdo nechce. Bez zámků, volá ji fronta úloh. */
		long dulezitost() {
			long nejlepsi = ImageReceiver.ZASTARALA;
			for (final ImageReceiver ir : prijemciProRazeni) {
				nejlepsi = Math.min(nejlepsi, ir.dulezitost());
			}
			return nejlepsi;
		}

		/** Úloha vyřazená z fronty jako zastaralá: příjemci, které už nikdo nechce, se odeberou; kdo se mezitím přihlásil, dostane úlohu znovu. */
		void zahodUlohu(final Future<?> uloha) {
			final Runnable spust;
			synchronized (this) {
				uloha.cancel(false);
				irs.removeIf(ir -> ir.dulezitost() == ImageReceiver.ZASTARALA);
				obnovPrijemceProRazeni();
				if (futura != uloha) {
					return;
				}
				futura = null;
				spust = irs.isEmpty() ? null : zahajNacteni();
			}
			if (spust != null) {
				spust.run();
			}
		}

		/** Úloha čekající ve frontě disku nebo vykreslování; pořadí se vyhodnotí až při výběru podle toho, co uživatel vidí. */
		final class Uloha implements PrioritniFronta.Razena {
			private final Runnable telo;
			private final Future<?> budouci;
			private final long zadano = System.nanoTime();

			<F extends Runnable & Future<?>> Uloha(final F telo) {
				this.telo = telo;
				this.budouci = telo;
			}

			@Override
			public void run() {
				telo.run();
			}

			@Override
			public long poradi(final long tedNs) {
				final long zakladni = dulezitost();
				if (zakladni == ImageReceiver.ZASTARALA) {
					return ImageReceiver.ZASTARALA;
				}
				final long cekani = tedNs - zadano;
				if (zakladni >= ImageReceiver.NA_POZADI || cekani <= starnutiNs) {
					return zakladni;
				}
				// Co čeká příliš dlouho, jde před ostatní, nejstarší první; dlaždice mimo výřez tím předbíhají jen ostatní mimo výřez ve své třídě a dlaždice na pozadí se neposouvají.
				final boolean mimoVyrez = zakladni % ImageReceiver.TRIDA >= ImageReceiver.MIMO_VYREZ;
				return mimoVyrez ? zakladni - cekani : -cekani;
			}

			@Override
			public void zahod() {
				zahodUlohu(budouci);
			}
		}

		synchronized Image getImage() {
			return image;
		}

		void ziskej(final ImageReceiver ir) {
			final Runnable spust;
			synchronized (this) {
				spust = zaregistruj(ir);
			}
			if (spust != null) {
				spust.run();
			}
		}

		/** Úlohu vrací ke spuštění mimo zámek: při plné frontě poběží ve volajícím vlákně a nesmí držet zámek dlaždice, který chce i obrazovka. */
		private Runnable zaregistruj(final ImageReceiver ir) {
			if (image != null) {
				// Když už ho máme, tak nás nic nezajímá, ani ho nemusíme zaregistrovávat do seznamu
				ir.send(new KachloStav(image));
				return null;
			}
			irs.add(ir);
			obnovPrijemceProRazeni();
			return zahajNacteni();
		}

		/** Když se dlaždice právě nezískává, zadá čtení z disku; úlohu vrací ke spuštění mimo zámek. */
		private Runnable zahajNacteni() {
			if (futura == null) { // nic se nezískává, začíná nutnost získat tu potvoru
				pocitDiskLoadSubmit.inc();
				final ListenableFutureTask<Image> future = ListenableFutureTask.create(this::nactiZDisku);
				futura = future;

				Futures.addCallback(future, new FutureCallback<Image>() {

					@Override
					public void onSuccess(final Image img) { // čtení u disku
						if (img != null) {
							pocitDiskLoadZasah.inc();
							onImageLoaded(img);
						} else {
							pocitDiskLoadMinuti.inc();
							pocitDownloadWebSubmit.inc();
							submitDownload();
						}

					}

					@Override
					public void onFailure(final Throwable t) { // čtení z disku
						if (!(t instanceof CancellationException)) { // když nebylo kanclováno, při kanclování se nespoští download
							pocitDiskLoadError.inc();
							submitDownload();
						}
					}

					private void submitDownload() {
						final Runnable spust;
						synchronized (Kachlice.this) {
							if (futura != future) {
								return; // čtení z disku bylo mezitím zrušeno
							}
							futura = null;
							if (irs.isEmpty()) {
								return;
							}
							spust = spustDownload();
						}
						if (spust != null) {
							spust.run();
						}
					}

					private Runnable spustDownload() {
						if (ka.getType().isOffline()) {
							return spustRender();
						} else if (onofflineModel.isOnlineMode()) {
							final AtomicReference<Future<?>> moje = new AtomicReference<>();
							final ListenableFutureTask<ImageWithData> future = pripravDownload(ka, des, diagnosticsData, Kachlice.this, moje);
							moje.set(future);
							futura = future;
							Futures.addCallback(future, new FutureCallback<ImageWithData>() {

								@Override
								public void onSuccess(final ImageWithData imageWithData) { // čtení z webu
									pocitDownloadWebOk.inc();
									onImageLoaded(imageWithData.getImg());
								}

								@Override
								public void onFailure(final Throwable t) { // čtení z webu
									if (t instanceof CancellationException) {
										return; // zrušilo se, protože dlaždici už nikdo nechce; příjemci nového požadavku čekají na jiné získání
									}
									pocitDownloadWebError.inc();
									onImageFailure(t);
								}
							}, MoreExecutors.directExecutor());
							// Až po zaregistrování: při plné frontě poběží úloha hned ve volajícím vlákně a musí už být „moje“.
							return () -> des.web.execute(new Uloha(future));
						} else { // v offline režimu nesmíme downloadovat a musíme poslat prázdný obrázek
							onImageLoaded(downloader.fejkovyObrazek(EPraznyObrazek.OFFLINE));
							return null;
						}
					}

					private Runnable spustRender() {
						final AtomicReference<Future<?>> moje = new AtomicReference<>();
						final ListenableFutureTask<BufferedImage> future = pripravRender(ka, des, diagnosticsData, Kachlice.this, moje);
						moje.set(future);
						futura = future;
						Futures.addCallback(future, new FutureCallback<BufferedImage>() {

							@Override
							public void onSuccess(final BufferedImage obrazek) {
								onImageLoaded(obrazek);
							}

							@Override
							public void onFailure(final Throwable t) {
								if (t instanceof CancellationException) {
									return;
								}
								onImageFailure(t);
							}
						}, MoreExecutors.directExecutor());
						return () -> des.render.execute(new Uloha(future));
					}

				}, MoreExecutors.directExecutor());
				return () -> des.disk.execute(new Uloha(future));
			}
			return null;
		}

		private Image nactiZDisku() throws IOException {
			return nactiZCache(ka);
		}

		/** Stahování začne posílat požadavek na server, jen když je pořád aktuální a někdo dlaždici chce. */
		synchronized boolean zahajStahovani(final AtomicReference<Future<?>> moje) {
			if (futura == null || futura != moje.get() || irs.isEmpty()) {
				if (futura != null && futura == moje.get()) {
					futura = null;
				}
				return false;
			}
			stahujeSe = true;
			return true;
		}

		public synchronized void onImageLoaded(final Image image) {
			futura = null;
			stahujeSe = false;
			this.image = image;
			for (final ImageReceiver ir : odeberPrijemce()) {
				ir.send(new KachloStav(image));
			}
		}

		public synchronized void onImageFailure(final Throwable t) {
			futura = null;
			stahujeSe = false;
			for (final ImageReceiver ir : odeberPrijemce()) {
				ir.send(new KachloStav(t));
			}
		}

		/** Příjemce se během rozesílání může odhlásit (rendr), proto se rozesílá z kopie. */
		private List<ImageReceiver> odeberPrijemce() {
			final List<ImageReceiver> prijemci = new ArrayList<>(irs);
			irs.clear();
			obnovPrijemceProRazeni();
			return prijemci;
		}

		public synchronized void kancluj(final ImageReceiver imageReceiver) {
			pocitCancelCelkem.inc();
			if (image != null) {
				pocitCancelPozde.inc();
			}
			irs.remove(imageReceiver); // už nechce, tak mu nebudeme nic říkat
			obnovPrijemceProRazeni();
			if (irs.isEmpty() && futura != null && !stahujeSe) {
				// Bez přerušení vlákna: přerušení zavře kanál souboru diskové cache.
				futura.cancel(false);
				futura = null;
			}
		}
	}

	/**
	 * Řízení ukládání achlí na disk po čancích
	 *
	 * @author Martin Veverka
	 *
	 */
	class KachleUkladac {
		/** Nejdéle za tuto dobu od první čekající dlaždice se začne ukládat, když se mapou nehýbe. */
		long casPoKteremSeZahajiUkladaniMs = TimeUnit.SECONDS.toMillis(5);

		/** Při pohybu mapou (nové požadavky na dlaždice) se zápis odkládá, nejdéle takto dlouho od první čekající dlaždice. */
		long nejdelsiOdlozeniUkladaniMs = TimeUnit.SECONDS.toMillis(15);

		/** Mapou se nehýbe, když nepřišel požadavek na dlaždici tuto dobu. */
		long necinnostMs = 1000;

		/** Čekajících dlaždic je tolik, že se ukládá i při pohybu mapou; offline dlaždice čekají nezkomprimované, proto strop podle velikosti haldy. */
		int nejviceCekajicich = (int) Math.max(MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU, Math.min(100, Runtime.getRuntime().maxMemory() / (8L << 20)));

		/** Hodiny v ms; test je nahrazuje. */
		LongSupplier hodiny = System::currentTimeMillis;

		private final BlockingQueue<Ukladanec> queue = Queues.newLinkedBlockingQueue();

		/** Kdy přišla první z čekajících dlaždic. */
		private long prvniCekajici;

		ScheduledExecutorService planovacUkladani = Executors.newSingleThreadScheduledExecutor();

		private ScheduledFuture<?> future;

		/**
		 * Zaplánuje uložení kachlice.
		 *
		 * @param ukladanec
		 */
		synchronized void zaplanujUlozeni(final Ukladanec ukladanec) {
			if (queue.isEmpty()) {
				prvniCekajici = hodiny.getAsLong();
			}
			queue.add(ukladanec);
			if (queue.size() >= MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU && !lzeOdlozit()) { // jakmile je toho dost, ukládá se hned a není nutno plánovat
				submitChunks();
			} else if (future == null) { // časovač běží od první čekající dlaždice, další ho neodkládají
				naplanuj(casPoKteremSeZahajiUkladaniMs);
			}
		}

		private void naplanuj(final long zaMs) {
			future = planovacUkladani.schedule(() -> {
				try {
					casovacVyprsel();
				} catch (final RuntimeException e) {
					log.error("Naplánované ukládání dlaždic na disk selhalo", e);
				}
			}, zaMs, TimeUnit.MILLISECONDS);
		}

		/** Při pohybu mapou se zápis (commit pod zámkem cache) odkládá, ale ne déle než do horního limitu a ne při mnoha čekajících dlaždicích. */
		private boolean lzeOdlozit() {
			final long ted = hodiny.getAsLong();
			return ted - posledniPozadavek < necinnostMs && ted - prvniCekajici < nejdelsiOdlozeniUkladaniMs && queue.size() < nejviceCekajicich;
		}

		private synchronized void casovacVyprsel() {
			future = null;
			if (!queue.isEmpty() && lzeOdlozit()) {
				naplanuj(Math.min(necinnostMs, nejdelsiOdlozeniUkladaniMs));
				return;
			}
			submitChunks();
		}

		private void submitDiskSave(final Collection<Ukladanec> ukladanci) {
			if (!kachleModel.isUkladatMapyNaDisk()) {
				return; // zakázáno ukládat
			}
			log.debug("Submitujeme ukladani kachle na disk #{}", ukladanci.size());
			execDiskWrite.execute(() -> {
				try {
					log.info("Ukladani kachle na disk #{}:", ukladanci.size());
					final List<ItemToSave> list = new ArrayList<>(ukladanci.size());
					for (final Ukladanec ukladanec : ukladanci) {
						try {
							list.add(new ItemToSave(ukladanec.getKa(), ukladanec.getTyp(), ukladanec.dataKUlozeni()));
						} catch (final IOException e) {
							log.warn("Dlaždici {} nejde převést pro uložení: {}", ukladanec.getKa(), e.toString());
						}
					}
					if (kachleManager.save(list)) {
						pocitZapsanoChunkuNaDisk.inc();
						pocitZapsanoNaDisk.add(list.size());
					}
				} catch (final RuntimeException e) {
					log.error("Ukládání dlaždic na disk selhalo", e);
				}
			});
		}

		synchronized private void submitChunks() {
			if (future != null) {
				future.cancel(false);
				future = null;
			}
			for (;;) {
				final ArrayList<Ukladanec> list = new ArrayList<>(MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU);
				queue.drainTo(list, MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU);
				if (list.size() == 0) {
					return;
				}
				submitDiskSave(list);
			}
		}
	}

	@Data
	private static class DvojiceExekucnichSluzeb {
		private final Priority priority;
		private final ListeningExecutorService disk;
		private final ListeningExecutorService web;
		/** Vykreslování offline mapy. */
		private final ListeningExecutorService render;
		private final org.slf4j.Logger log;

		private BlockingQueue<Runnable> diskQueue;
		private BlockingQueue<Runnable> webQueue;
		private BlockingQueue<Runnable> renderQueue;
		private final ThreadPoolExecutor[] vlakna;

		public DvojiceExekucnichSluzeb(final Priority priority, final ExecutorService disk, final ExecutorService web, final ExecutorService render, final Logger log) {
			this.priority = priority;
			this.disk = MoreExecutors.listeningDecorator(disk);
			this.web = MoreExecutors.listeningDecorator(web);
			this.render = MoreExecutors.listeningDecorator(render);
			this.log = log;

			diskQueue = ((ThreadPoolExecutor) disk).getQueue();
			webQueue = ((ThreadPoolExecutor) web).getQueue();
			renderQueue = ((ThreadPoolExecutor) render).getQueue();
			vlakna = new ThreadPoolExecutor[] { (ThreadPoolExecutor) disk, (ThreadPoolExecutor) web, (ThreadPoolExecutor) render };
		}

		/** Nic nečeká ani neběží. */
		boolean jeNecinna() {
			for (final ThreadPoolExecutor v : vlakna) {
				if (v.getActiveCount() > 0 || !v.getQueue().isEmpty()) {
					return false;
				}
			}
			return true;
		}

		// ExecutorService nedekorovanyServisProZjisteni
	}

	private static Pocitadlo pocitBrzdeniOfflineKdyzJeOnline = new PocitadloRoste("ka61 #počet brzdení dávkového downloadu",
			"Počítá, kolikrát dávkové dotahování čekalo, protože běžel online.");

	private static final int MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU = 50;

	/** Jak dlouho smí dlaždice čekat ve frontě na řadu, než předběhne ostatní; jinak by ji nepřetržitý posun mohl odsouvat. */
	static volatile long starnutiNs = TimeUnit.SECONDS.toNanos(2);

	static final int NTHREADS_WEB_CORE_ONLINE = 5;

	private static final int NTHREADS_WEB_CORE_BATCH = 5;

	static final int NTHREADS_WEB_MAXIMUM_BACTH = 10;

	private static final int WEB_QUEUE_SIZE = 100;

	private static final int BATCH_DISK_QUEUE_SIZE = 100;

	/** Dávka (i export mapy) smí jít na server s omezením jen ve 2 spojeních, i když má 10 vláken. */
	volatile SoubehNaHostitele soubehDavky = new SoubehNaHostitele();

	void setSoubehDavky(final SoubehNaHostitele soubehDavky) {
		this.soubehDavky = soubehDavky;
	}

	/** Jedno jádro zůstane oknu, nad 6 vláken vykreslování skoro nezrychlí. */
	static final int NTHREADS_RENDER = pocetVlakenRenderu(Runtime.getRuntime().availableProcessors());

	static int pocetVlakenRenderu(final int jader) {
		return Math.min(jader, Math.min(6, Math.max(2, jader - 1)));
	}

	private static ThreadFactory vlaknaRenderu(final String jmeno) {
		final ThreadFactory vychozi = Executors.defaultThreadFactory();
		return r -> {
			final Thread t = vychozi.newThread(r);
			t.setName(jmeno + " " + t.getName());
			t.setDaemon(true);
			return t;
		};
	}

	/** Čtení z cache je hlavně dekódování obrázků, víc vláken pomůže na víc jádrech. */
	static final int NTHREADS_DISK = Math.max(2, Math.min(4, Runtime.getRuntime().availableProcessors()));

	private final Pocitadlo pocitSubmitJednaDlazdice = new PocitadloRoste("ka01 Počet požadavků na jednu dlaždici",
			"Kolikrát byl nakonec zadán požadavek na získání jedné dlaždice z jedné vrstvy. Tedy pokud zobrazujeme mapu s turistickými trasami a cyklotrasami, je dlaždice na podklad, na turistickou i na cyklo počítána zvlášť.");

	private final Pocitadlo pocitMemZasah = new PocitadloRoste("ka11 MEM cache #zásahů",
			"Kolikrát se podařilo hledanou dlaždici zasáhnout v paměti. Číslo stále roste a mělo by být ve srovnání s ostatními zásahy co největší.");

	private final Pocitadlo pocitMemMinuti = new PocitadloRoste("ka12 MEM cache #minutí",
			"Kolikrát se nepodařilo hledanou dlaždici v paměťové keši nalézt a co se dělo dál není tímto atributem určeno..");

	private final Pocitadlo pocitMemZameteni = new PocitadloRoste("ka13 MEM cache #zametených", "Počet kachlic, které garbage collector zametl pryč a my díky tomu odstranili referenci z keše.");

	private final PocitadloRoste pocitDiskLoadSubmit = new PocitadloRoste("ka21 DISK cache #požadovaných",
			"Kolikrát bylo požadováno číst dlaždici z disku. Obsahuje všechny zásahy, minutí, chyby a také skutečně zknclované čtení");

	private final PocitadloRoste pocitDiskLoadZasah = new PocitadloRoste("ka22 DISK cache #zásahů",
			"Kolikrát se podařilo hledanou dlaždici zasáhnout na disku, tedy naloadovat. Číslo stále roste a mělo by být ve srovnání s ostatními zásahy co největší.");

	private final PocitadloRoste pocitDiskLoadMinuti = new PocitadloRoste("ka23 DISK cache #minutí",
			"Kolikrát se nepodařilo hledanou dlaždici v paměťové keši minout, co se dělo dál není tímto atributem určeno..");

	final PocitadloRoste pocitDiskLoadError = new PocitadloRoste("ka24 DISK cache #chyb čtení", "Kolikrát selhalo čtení dlaždich z disku.");

	private final PocitadloRoste pocitDownloadWebSubmit = new PocitadloRoste("ka31 WEB #požadovaných",
			"Kolikrát bylo požadováno číst dlaždici z webu. Vždy poté, co se nenašly na disku.disku. Obsahuje všechny úspěšně i neúspěšně načtené a také zkanclované");

	private final PocitadloRoste pocitDownloadWebOk = new PocitadloRoste("ka32 WEB #načtených", "Kolikrát se muselo hledanou kachli stáhnout z webu a to úspěšně.");

	private final PocitadloRoste pocitDownloadWebError = new PocitadloRoste("ka33 WEB #chyb", "Kolikrát downloadování dlaždice zahlásilo chybu.");

	private final PocitadloRoste pocitRenderOk = new PocitadloRoste("ka36 RENDER #vykreslených", "Kolik dlaždic offline mapy se vykreslilo.");

	final PocitadloRoste pocitRenderError = new PocitadloRoste("ka37 RENDER #chyb", "Kolikrát vykreslení dlaždice offline mapy selhalo.");

	private final PocitadloRoste pocitZapsanoChunkuNaDisk = new PocitadloRoste("ka41 disk write #bloků",
			"V kolika diskovžch operacích byl prováděn zápis na disk. Z důvodu optimalizace se zápisy na disk združují do větších bloků.");

	private final PocitadloRoste pocitZapsanoNaDisk = new PocitadloRoste("ka42 disk write #dlaždic", "Kolik dlaždic bylo úspěšně zapsáno na disk asynchronním zapisovačem.");

	private final PocitadloRoste pocitCancelCelkem = new PocitadloRoste("ka51 #cancel celkem", "Celkový počet vydaných poždavaků na kanclování");

	private final PocitadloRoste pocitCancelPozde = new PocitadloRoste("ka55 #cance pozdní, když už je kachle získána", "Počet tancelů, které přišly pozdě, kdy už jsou data načtena.");

	private final Pocitadlo pocitVelikostPametoveKese = new PocitadloMalo("Počet dlaždic v memcache: ",
			"Počet závisí na velikosti pro Javu dostupné paměti (-Xmx) a měl by se ustálit na určité hodnotě, občas možná snížit, nikdy však nemůže být menší než počet dlaždic na mapě.");
	private final PocitadloNula pocitVelikostDiskFrontyOnline = new PocitadloNula("ka1 velikost fronty čtení z disku (online)", "Kolik je ve frontě požadavků na čtení dlaždic z webu.");

	private final PocitadloNula pocitVelikostDiskFrontyBatch = new PocitadloNula("ka2 velikost fronty čtení z disku (batch)", "Kolik je ve frontě požadavků na čtení dlaždic z webu.");

	private final PocitadloNula pocitVelikostWebFrontyOnline = new PocitadloNula("ka3 veliksot fronty čtení z webu (online)", "Kolik je ve frontě požadavků na čtení dlaždic z webu.");

	private final PocitadloNula pocitVelikostWebFrontyBatch = new PocitadloNula("ka3 veliksot fronty čtení z webu (batch)", "Kolik je ve frontě požadavků na čtení dlaždic z webu.");
	private final PocitadloNula pocitVelikostZapisoveFronty = new PocitadloNula("ka3 veliksot fronty zápisu na disk", "Kolik bloků dlaždic je ještě ve frontě a čeká na zápis na disk.");

	final PocitadloMalo pocitKachlice = new PocitadloMalo("#kachlic",
			"Počet tzv. kachlic, což je objekt určený k řízení získávání kachle z disku, z webu a pak ukládání. Kachlice také reprezentují MEM cache");

	private final EnumMap<Priority, DvojiceExekucnichSluzeb> exekucniSluzby = new EnumMap<>(Priority.class);

	private final ThreadPoolExecutor execDiskWrite = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

	private final ScheduledExecutorService tikacVelikostiFront = Executors.newSingleThreadScheduledExecutor();
	private final KachloDownloader downloader = new KachloDownloader();

	/** Nejméně dlaždic v paměti i při malé haldě. */
	static final int MIN_KACHLI_V_PAMETI = 300;
	/** Podíl haldy, který smí zabrat dlaždice v paměti, a jeho horní mez. */
	private static final double PODIL_HALDY_PRO_KACHLE = 0.25;
	private static final long MAX_BAJTU_KACHLI_V_PAMETI = 1L << 30;
	/** Dlaždice 256 × 256 bodů po 4 bajtech. */
	private static final long BAJTU_NA_KACHLI = 256L * 256 * 4;

	/** Kolik dlaždic se drží v paměti: čtvrtina haldy, nejvýš 1 GB, nejméně {@link #MIN_KACHLI_V_PAMETI}. */
	static int maxKachliVPameti(final long maxHaldaBajtu) {
		final long bajtu = Math.min((long) (maxHaldaBajtu * PODIL_HALDY_PRO_KACHLE), MAX_BAJTU_KACHLI_V_PAMETI);
		return (int) Math.max(MIN_KACHLI_V_PAMETI, bajtu / BAJTU_NA_KACHLI);
	}

	private final int maxKachliVPameti = maxKachliVPameti(Runtime.getRuntime().maxMemory());

	private final Cache<Ka, Kachlice> kachlmap = CacheBuilder.newBuilder().maximumSize(maxKachliVPameti).softValues().<Ka, Kachlice>removalListener(oznameni -> {
		pocitKachlice.dec();
		if (oznameni.getCause() == RemovalCause.COLLECTED) {
			pocitMemZameteni.inc();
		}
	}).build();

	private KachleManager kachleManager;

	final KachleUkladac ukladac = new KachleUkladac();

	/** Pohyb mapou je požadavek na viditelnou dlaždici; předvykreslení okolí a stahování na pozadí zápis neodkládají. */
	static boolean jePohybMapou(final KaOneReq req) {
		final ImageReceiver prijemce = req.getImageReceiver();
		return req.getPriorita() == Priority.KACHLE && prijemce != null && prijemce.dulezitost() < ImageReceiver.NA_POZADI;
	}

	/** Kdy (ms) přišel poslední požadavek na dlaždici; zápis dlaždic se při pohybu mapou odkládá. */
	private volatile long posledniPozadavek;

	/** Bez sítě selže každá dlaždice, hlásí se proto souhrnně podle druhu chyby. */
	private final Map<String, OpakovaneChyby> chybyStahovani = new ConcurrentHashMap<>();
	/** Dlaždice mimo území podkladu (server vrátil 404); kreslí se prázdné a znovu se nestahují. */
	private final Cache<String, Boolean> mimoUzemi = CacheBuilder.newBuilder().maximumSize(10_000).build();
	static final Image PRAZDNA_MIMO_UZEMI = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
	static final BufferedImage PRAZDNY_PREKRYV = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
	/** Dlaždice, které se nedávno nepodařilo stáhnout; jinak by se při každém překreslení stahovaly znovu. */
	private final Cache<String, Boolean> nedavnoNestazene = CacheBuilder.newBuilder().expireAfterWrite(java.time.Duration.ofSeconds(30)).maximumSize(10_000).build();

	private OnofflineModel onofflineModel;

	private volatile Runnable priZmeneOfflineMapy = () -> {};

	private final OfflineMapy offlineMapy = new OfflineMapy(this::zmenaOfflineMapy);

	/** Turistické a cyklotrasy přes podklad: stejné mapy, vlastní téma a klíče v cache. */
	private final OfflineMapy offlineTrasy = new OfflineMapy(this::zmenaOfflineMapy, true);

	private KachleModel kachleModel;

	private final PredvykresleniPrehledu predvykresleniPrehledu = new PredvykresleniPrehledu(new PrehledOfflineMap(), this::jeVolnoProPredvykresleni, System::nanoTime,
			KachleZiskavac::jePoPrvnimPohledu);

	/** Úklid cache offline mapy běží v jednom vlákně na pozadí, dlaždice na obrazovce nezdržuje. */
	private final ExecutorService uklidOfflineCache = Executors.newSingleThreadExecutor(vlaknaRenderu("Úklid cache offline mapy"));

	public KachleZiskavac() {
		final BiConsumer<OblastniKlic, File> uklid = (klic, symboly) -> {
			if (symboly != null) {
				uklidOfflineCache.execute(() -> uklidOfflineCache(klic, symboly));
			}
		};
		offlineMapy.setPriOtevreni(uklid);
		offlineTrasy.setPriOtevreni(uklid);

		final DvojiceExekucnichSluzeb dvojiceOnline = new DvojiceExekucnichSluzeb(
				// Fronta je pro oonline přístup neomezená, protože nemůžeme nijak blokovat rsponsivnost UI */
				Priority.KACHLE, new ThreadPoolExecutor(NTHREADS_DISK, NTHREADS_DISK, 0L, TimeUnit.MILLISECONDS, new PrioritniFronta()),
					new ThreadPoolExecutor(NTHREADS_WEB_CORE_ONLINE, NTHREADS_WEB_CORE_ONLINE, 0L, TimeUnit.MILLISECONDS, new PrioritniFronta()),
					new ThreadPoolExecutor(NTHREADS_RENDER, NTHREADS_RENDER, 0L, TimeUnit.MILLISECONDS, new PrioritniFronta(), vlaknaRenderu("Offline mapa")),
				LoggerFactory.getLogger(KachleZiskavac.class.getSimpleName() + "_online"));

		final DvojiceExekucnichSluzeb dvojiceBatch = new DvojiceExekucnichSluzeb(Priority.STAHOVANI,
				// Pro background stahování. Jen jedno vlákno pro disk, krátká fronta a po přetížení nechat na volajícím */
				new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(BATCH_DISK_QUEUE_SIZE), new ThreadPoolExecutor.CallerRunsPolicy()),
				// Pokud bude fornta na web přetížená, vykonává stahování vlákno pro dotahování z disku, čímž se to zpomalí
				new ThreadPoolExecutor(NTHREADS_WEB_CORE_BATCH, NTHREADS_WEB_MAXIMUM_BACTH, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(WEB_QUEUE_SIZE), new ThreadPoolExecutor.CallerRunsPolicy()),
				// Export vykresluje jedním vláknem, obrazovka má přednost.
				Executors.newFixedThreadPool(1, vlaknaRenderu("Offline mapa export")),
				LoggerFactory.getLogger(KachleZiskavac.class.getSimpleName() + "_batch"));

		exekucniSluzby.put(Priority.KACHLE, dvojiceOnline);
		exekucniSluzby.put(Priority.STAHOVANI, dvojiceBatch);

		tikacVelikostiFront.scheduleAtFixedRate(() -> {
			try {
				pocitVelikostDiskFrontyOnline.set(dvojiceOnline.diskQueue.size());
				pocitVelikostDiskFrontyBatch.set(dvojiceBatch.diskQueue.size());
				pocitVelikostWebFrontyOnline.set(dvojiceOnline.webQueue.size());
				pocitVelikostWebFrontyBatch.set(dvojiceBatch.webQueue.size());
				pocitVelikostZapisoveFronty.set(execDiskWrite.getQueue().size());
				pocitVelikostPametoveKese.set(kachlmap.asMap().size());
			} catch (final Exception e) {
				log.error("Chyba při tikání a zjišťování velikosti fornt: {}", e);
			}

		}, 1l, 1l, TimeUnit.SECONDS);
		// Konec programu volá System.exit, dlaždice čekající na zápis by se jinak ztratily.
		Runtime.getRuntime().addShutdownHook(new Thread(this::dopisNaDisk, "Dopsání dlaždic na disk"));
	}

	void dopisNaDisk() {
		// Náhledy z cache při ukončení nikdo nepotřebuje a nesmí číst z cache, kterou se dopisuje.
		nahledyZCache.shutdownNow();
		ukladac.submitChunks();
		execDiskWrite.shutdown();
		try {
			if (!pockejNaZapis(execDiskWrite, 10_000, 60_000)) {
				log.warn("Při ukončení se nestihly zapsat všechny dlaždice do cache");
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/** Čeká na dokončení zápisu, dokud postupuje: pomalý disk dostane čas, zaseknutý zápis konec programu nezdrží o víc než jeden krok. */
	static boolean pockejNaZapis(final ThreadPoolExecutor zapis, final long krokMs, final long nejvicMs) throws InterruptedException {
		final long konec = System.currentTimeMillis() + nejvicMs;
		long hotovo = zapis.getCompletedTaskCount();
		while (!zapis.awaitTermination(krokMs, TimeUnit.MILLISECONDS)) {
			final long ted = zapis.getCompletedTaskCount();
			if (ted == hotovo || System.currentTimeMillis() >= konec) {
				return false;
			}
			hotovo = ted;
		}
		return true;
	}

	/**
	 * Zapíše klíč do souboru klíčů a smaže soubory symbolů, které už nikdo nepoužívá. Dlaždice klíčů, které v souboru nezůstaly, se z cache automaticky nemažou:
	 * průchod a mazání ve velké cache by na dlouho zastavily čtení dlaždic.
	 */
	void uklidOfflineCache(final OblastniKlic klic, final File symboly) {
		try {
			final File slozka = symboly.getParentFile();
			final UklidOfflineCache.Plan plan = UklidOfflineCache.naplanuj(slozka, klic.getGlobalni(), symboly, klic.getSestava(), klic.getIdentita());
			final Set<String> ponechat = plan.uloz();
			log.info("Cache offline mapy: zůstávají klíče {}", ponechat);
			if (!plan.zastarale().isEmpty()) {
				log.info("Cache offline mapy: zastaralé dlaždice zůstávají do optimalizace: klíče {}", plan.zastarale());
			}
		} catch (final IOException | RuntimeException e) {
			log.warn("Soubor klíčů cache offline mapy nejde zapsat: {}", e.toString());
		}
	}

	/** Mapy pro předvykreslení přehledu, pro testy. */
	PredvykresleniPrehledu.Mapy mapyProPredvykresleniPrehledu() {
		return new PrehledOfflineMap();
	}

	/**
	 * Spustí předvykreslování přehledových zoomů offline map v nečinnosti; jakýkoli vstup myší nebo klávesnicí ho přeruší. Na jednojádrovém počítači nic
	 * nedělá.
	 */
	public void spustPredvykresleniPrehledu() {
		if (!PredvykresleniPrehledu.jeVhodne(Runtime.getRuntime().availableProcessors())) {
			return;
		}
		Toolkit.getDefaultToolkit().addAWTEventListener(e -> predvykresleniPrehledu.aktivita(),
				AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
		final ScheduledExecutorService vlakno = Executors.newSingleThreadScheduledExecutor(r -> {
			final Thread t = vlaknaRenderu("Předvykreslení přehledu").newThread(r);
			t.setPriority(Thread.MIN_PRIORITY);
			return t;
		});
		vlakno.scheduleWithFixedDelay(() -> {
			try {
				final int n = predvykresleniPrehledu.pracuj();
				if (n > 0) {
					log.debug("Předvykresleno {} dlaždic přehledu offline map, zbývá {}", n, predvykresleniPrehledu.zbyva());
				}
			} catch (final RuntimeException e) {
				log.warn("Předvykreslení přehledu offline map selhalo: {}", e.toString());
			}
		}, 1, 1, TimeUnit.SECONDS);
	}

	/** Místo, kolem kterého se v nečinnosti předvykreslí z10–12. */
	public void setPoziceProPredvykresleni(final Wgs pozice) {
		predvykresleniPrehledu.setPozice(pozice);
	}

	/** První načítání mapy skončilo a od spuštění programu uplynula doba, po kterou se hlídá start. */
	static boolean jePoPrvnimPohledu() {
		return PrvniNacitaniMapy.jeUkonceno() && Diagnostika.dobaBehuMs() >= PrvniNacitaniMapy.LIMIT_MS;
	}

	/** Mapy se nestahují, nic jiného se nekreslí ani nečte z cache a dlaždice se ukládají na disk. */
	boolean jeVolnoProPredvykresleni() {
		final KachleModel km = kachleModel;
		return km != null && km.isUkladatMapyNaDisk() && kachleManager != null && offlineMapy.getSlozka() != null && !offlineMapy.isDavka()
				&& exekucniSluzby.get(Priority.KACHLE).jeNecinna() && exekucniSluzby.get(Priority.STAHOVANI).jeNecinna();
	}

	/** Podkladová offline mapa; dlaždice jdou do cache stejně jako při kreslení na obrazovku. */
	private final class PrehledOfflineMap implements PredvykresleniPrehledu.Mapy {

		@Override
		public String klic() throws IOException {
			final OfflineRenderer r = offlineMapy.pouzij();
			try {
				return r.getKlic() + '.' + r.getOblastniKlic().getSestava();
			} finally {
				r.skonci();
			}
		}

		@Override
		public List<KaLoc> dlazdice(final int zMin, final int zMax) throws IOException {
			final OfflineRenderer r = offlineMapy.pouzij();
			try {
				return r.dlazdicePokryti(zMin, zMax);
			} finally {
				r.skonci();
			}
		}

		@Override
		public List<KaLoc> zapamatovanyVzorek(final String klic) {
			final File slozka = slozkaKlicu();
			return slozka == null ? null : HotovePredvykresleni.vzorek(slozka, klic);
		}

		@Override
		public void zapamatujHotovo(final String klic, final List<KaLoc> vzorek) {
			final File slozka = slozkaKlicu();
			if (slozka == null || vzorek.isEmpty()) {
				return;
			}
			try {
				HotovePredvykresleni.zapis(slozka, klic, vzorek);
			} catch (final IOException | RuntimeException e) {
				log.debug("Předvykreslení přehledu nejde zapamatovat: {}", e.toString());
			}
		}

		/** Složka se souborem klíčů cache offline mapy; null, když se symboly a klíče neukládají. */
		private File slozkaKlicu() {
			try {
				final OfflineRenderer r = offlineMapy.pouzij();
				try {
					final File symboly = r.getSouborSymbolu();
					return symboly == null ? null : symboly.getParentFile();
				} finally {
					r.skonci();
				}
			} catch (final IOException | RuntimeException e) {
				return null;
			}
		}

		@Override
		public boolean jeVCache(final KaLoc loc, final String klic) {
			final KachleManager km = kachleManager;
			if (km == null) {
				return true;
			}
			final OfflineRenderer r;
			try {
				r = offlineMapy.pouzij();
			} catch (final IOException e) {
				return true;
			}
			try {
				return km.exists(new Ka(loc, EKaType.OFFLINE_MF), r.getKlicDlazdice(loc));
			} finally {
				r.skonci();
			}
		}

		@Override
		public boolean vykresliChybejici(final KaLoc loc, final String klic) throws IOException {
			final KachleManager km = kachleManager;
			if (km == null) {
				return false;
			}
			final OfflineRenderer r = offlineMapy.pouzij();
			try {
				if (!klic.equals(r.getKlic() + '.' + r.getOblastniKlic().getSestava()) || !r.pokryva(loc) || km.exists(new Ka(loc, EKaType.OFFLINE_MF), r.getKlicDlazdice(loc))) {
					return false;
				}
				final long zacatek = System.nanoTime();
				final BufferedImage obrazek = r.vyrendruj(loc);
				Vykon.zaznamenej(Vykon.Velicina.VYKRESLENI_OFFLINE, System.nanoTime() - zacatek);
				pocitRenderOk.inc();
				ukladac.zaplanujUlozeni(new Ukladanec(new Ka(loc, EKaType.OFFLINE_MF), r.getKlicDlazdice(loc), obrazek, null));
				return true;
			} finally {
				r.skonci();
			}
		}

		@Override
		public boolean vykresli(final KaLoc loc, final String klic) throws IOException {
			final OfflineRenderer r = offlineMapy.pouzij();
			try {
				if (!klic.equals(r.getKlic() + '.' + r.getOblastniKlic().getSestava()) || !r.pokryva(loc)) {
					return false;
				}
				final long zacatek = System.nanoTime();
				final BufferedImage obrazek = r.vyrendruj(loc);
				// Přehledové dlaždice jsou velké a zkreslily by p95 a max vykreslení dlaždic na obrazovce.
				Vykon.zaznamenej(Vykon.Velicina.VYKRESLENI_PREHLEDU, System.nanoTime() - zacatek);
				pocitRenderOk.inc();
				ukladac.zaplanujUlozeni(new Ukladanec(new Ka(loc, EKaType.OFFLINE_MF), r.getKlicDlazdice(loc), obrazek, null));
				return true;
			} finally {
				r.skonci();
			}
		}
	}

	/** Smazat se musí třeba při zapnutí online módi */
	public void clearMemoryCache() {
		log.debug("Cache cleared");
		kachlmap.asMap().clear();
	}

	private void zmenaOfflineMapy() {
		clearMemoryCache();
		priZmeneOfflineMapy.run();
	}

	public OfflineMapy getOfflineMapy() {
		return offlineMapy;
	}

	public OfflineMapy getOfflineTrasy() {
		return offlineTrasy;
	}

	/** Offline mapy, ze kterých se kreslí dlaždice daného typu. */
	OfflineMapy offlineMapy(final EKaType typ) {
		return typ == EKaType.TRASY ? offlineTrasy : offlineMapy;
	}

	/** Zavolá se (z vlákna vykreslování), když se změní soubory offline mapy nebo téma. */
	public void setPriZmeneOfflineMapy(final Runnable priZmene) {
		priZmeneOfflineMapy = priZmene;
	}

	/**
	 * Otevření spojení s cache stojí stovky ms a drží zámek cache; proto se dělá hned po nastavení složky cache na pozadí ve všech vláknech, která z cache čtou nebo do ní
	 * zapisují.
	 */
	public void predotevriCache() {
		final KachleManager km = kachleManager;
		if (km == null) {
			return;
		}
		final DvojiceExekucnichSluzeb online = exekucniSluzby.get(Priority.KACHLE);
		// Pool zakládá vlákno pro každou z prvních úloh, i když jiné vlákno čeká, takže každé vlákno čtení dostane jednu.
		for (int i = 0; i < NTHREADS_DISK; i++) {
			online.disk.execute(km::predotevri);
		}
		execDiskWrite.execute(km::predotevri);
	}

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	public void inject(final OnofflineModel onofflineModel) {
		this.onofflineModel = onofflineModel;
	}

	public void setKachleManager(final KachleManager kachleManager) {
		this.kachleManager = kachleManager;
	}

	/**
	 * Toto je hlavní metoda celého procesu.
	 *
	 *
	 * Získá obsah, získává ho postupně a plní ho do ImageReceveru v rekvesti
	 *
	 * @param req
	 */
	public Kanceler ziskejObsah(final KaOneReq req, final DiagnosticsData diagnosticsDatax) {
		final DiagnosticsData diagnosticsData = diagnosticsDatax.with("priorita", req.getPriorita());

		final DvojiceExekucnichSluzeb dvojiceExekucnichSluzeb = exekucniSluzby.get(req.getPriorita());
		final Logger log = dvojiceExekucnichSluzeb.log;
		log.debug("ziskejObsah: {}", diagnosticsData);
		final Kanceler kanceler = ziskejObsah(dvojiceExekucnichSluzeb, req, diagnosticsData);
		return kanceler;

	}

	/** Omezení souběhu platí jen pro samotné stahování; semafor se nedrží při čekání na zápis do cache. */
	private ImageWithData stahni(final URL url, final Ka ka, final boolean davka) throws IOException, InterruptedException {
		final Semaphore soubeh = davka ? soubehDavky.pro(url.getHost()) : null;
		if (soubeh != null) {
			soubeh.acquire();
		}
		try {
			return downloader.downloadImage(url, ka.getType().getHlavicky());
		} finally {
			if (soubeh != null) {
				soubeh.release();
			}
		}
	}

	/** Dávka počká, dokud čekají dlaždice, které uživatel právě prohlíží. */
	static void pockejNaOnline(final BlockingQueue<Runnable> online) throws InterruptedException {
		if (online.isEmpty()) {
			return;
		}
		if (!(online instanceof PrioritniFronta)) {
			throw new IllegalStateException("Fronta online dlaždic není PrioritniFronta: " + online.getClass().getName());
		}
		pocitBrzdeniOfflineKdyzJeOnline.inc();
		((PrioritniFronta) online).pockejNaPrazdnou();
	}

	/** Úloha stažení dlaždice; spouští ji volající, až ji zaregistruje. */
	private ListenableFutureTask<ImageWithData> pripravDownload(final Ka ka, final DvojiceExekucnichSluzeb dvojiceExekucnichSluzeb, final DiagnosticsData diagnosticsData,
			final Kachlice kachlice, final AtomicReference<Future<?>> moje) {
		final Logger log = dvojiceExekucnichSluzeb.log;
		final URL url = ka.getUrl();
		log.debug("SUBMITTING WEB DOWNLOAD  \"{}\" | {}", url, diagnosticsData);
		final ListenableFutureTask<ImageWithData> future = ListenableFutureTask.create(() -> {

			// Pokud stahujeme backgroundově a jsou nějaké požadavny na online kachle, tak ty online mají absolutní přednost,
			// backgroundy brzíme.
			if (dvojiceExekucnichSluzeb.priority == Priority.STAHOVANI) {
				pockejNaOnline(exekucniSluzby.get(Priority.KACHLE).webQueue);
			}
			final String klic = url.toExternalForm();
			if (mimoUzemi.getIfPresent(klic) != null) {
				return new ImageWithData(PRAZDNA_MIMO_UZEMI, new byte[0]);
			}
			if (nedavnoNestazene.getIfPresent(klic) != null) {
				throw new IOException("Dlaždici se nedávno nepodařilo stáhnout: " + url);
			}
			if (!kachlice.zahajStahovani(moje)) {
				throw new CancellationException("Dlaždici už nikdo nechce: " + url);
			}
			diagnosticsData.send("Web download - start");
			log.debug("DOWNLOAD START: \"{}\" | {}", url, diagnosticsData);
			ImageWithData imageWithData;
			try {
				imageWithData = stahni(url, ka, dvojiceExekucnichSluzeb.priority == Priority.STAHOVANI);
				// Ukládá se hned, i když byl požadavek mezitím zrušen: stažená data by se jinak zahodila a stahovala znovu.
				ukladac.zaplanujUlozeni(new Ukladanec(ka, imageWithData.getData(), kachlice));
				log.debug("DOWNLOAD END  : \"{}\" | {}", url, diagnosticsData);
				diagnosticsData.send("Web download - end success");
				return imageWithData;
			} catch (final KachloDownloader.ChybaServeru e) {
				if (!ka.getType().jeMimoUzemi(e.getKod(), ka.getLoc())) {
					throw zaznamenejChybu(e, url, klic, diagnosticsData, log);
				}
				mimoUzemi.put(klic, Boolean.TRUE);
				diagnosticsData.send("Web download - mimo území podkladu");
				return new ImageWithData(PRAZDNA_MIMO_UZEMI, new byte[0]);
			} catch (final Exception e) {
				throw zaznamenejChybu(e, url, klic, diagnosticsData, log);
			}
		});
		return future;
	}

	/** Úloha vykreslení dlaždice offline mapy; spouští ji volající, až ji zaregistruje. */
	private ListenableFutureTask<BufferedImage> pripravRender(final Ka ka, final DvojiceExekucnichSluzeb dvojiceExekucnichSluzeb, final DiagnosticsData diagnosticsData,
			final Kachlice kachlice, final AtomicReference<Future<?>> moje) {
		return ListenableFutureTask.create(() -> {
			if (dvojiceExekucnichSluzeb.priority == Priority.STAHOVANI) {
				pockejNaOnline(exekucniSluzby.get(Priority.KACHLE).renderQueue);
			}
			if (!kachlice.zahajStahovani(moje)) {
				throw new CancellationException("Dlaždici už nikdo nechce: " + ka);
			}
			try {
				final OfflineRenderer renderer;
				try {
					renderer = offlineMapy(ka.getType()).pouzij();
				} catch (final OfflineMapaChyba e) {
					if (ka.getType().isPrekryv()) {
						return PRAZDNY_PREKRYV; // bez map překryv nic nekreslí, chybu ukazuje jen podklad
					}
					throw e;
				}
				try {
					final long zacatek = System.nanoTime();
					final BufferedImage obrazek = renderer.vyrendruj(ka.getLoc());
					Vykon.zaznamenej(Vykon.Velicina.VYKRESLENI_OFFLINE, System.nanoTime() - zacatek);
					pocitRenderOk.inc();
					if (renderer.pokryva(ka.getLoc())) {
						ukladac.zaplanujUlozeni(new Ukladanec(ka, renderer.getKlicDlazdice(ka.getLoc()), obrazek, kachlice));
					}
					return obrazek;
				} finally {
					renderer.skonci();
				}
			} catch (final Exception e) {
				pocitRenderError.inc();
				// Chybějící nebo nečitelné mapy ukazuje dlaždice i s radou, kde nastavit složku; chybou programu nejsou.
				if (!(e instanceof OfflineMapaChyba)) {
					chybyStahovani.computeIfAbsent("offline " + e.getClass().getName(), k -> new OpakovaneChyby("Chyba při vykreslení dlaždice offline mapy (" + e.getClass().getName() + ")")).ohlas(e);
				}
				diagnosticsData.send(e.toString());
				throw e;
			}
		});
	}

	private Exception zaznamenejChybu(final Exception e, final URL url, final String klic, final DiagnosticsData diagnosticsData, final Logger log) {
		log.debug("DOWNLOAD ERROR  : \"{}\" | {}", url, e);
		if (!(e instanceof InterruptedException || e instanceof InterruptedIOException || Thread.currentThread().isInterrupted())) {
			nedavnoNestazene.put(klic, Boolean.TRUE);
		}
		chybyStahovani.computeIfAbsent(e.getClass().getName(), k -> new OpakovaneChyby("Chyba při stahování dlaždice (" + k + ")")).ohlas(e);
		diagnosticsData.send(e.toString());
		return e;
	}

	/** Dlaždice offline mapy se hledá v cache pod klíčem aktuálních map a tématu. */
	private Image nactiZCache(final Ka ka) throws IOException {
		if (!ka.getType().isOffline()) {
			return kachleManager.load(ka);
		}
		final OfflineRenderer renderer;
		try {
			renderer = offlineMapy(ka.getType()).pouzij();
		} catch (final IOException e) {
			return null; // mapy nejdou otevřít; chybu ukáže a započítá vykreslení
		}
		try {
			if (!renderer.pokryva(ka.getLoc())) {
				return null; // dlaždice mimo mapy se jen vybarví a neukládá
			}
			return kachleManager.load(ka, renderer.getKlicDlazdice(ka.getLoc()));
		} finally {
			renderer.skonci();
		}
	}

	/** Rodičovské dlaždice dekódované z cache pro náhledy: sourozenci sdílejí jedno dekódování a i neúspěšné hledání se krátce pamatuje. */
	private final Cache<Ka, Optional<Image>> nahledyRodicu = CacheBuilder.newBuilder().maximumSize(NAHLEDY_RODICU).expireAfterWrite(java.time.Duration.ofSeconds(10)).softValues().build();

	private static final int NAHLEDY_RODICU = 32;

	/** Náhled z cache na disku jen pro online mapy; u offline map se dlaždice vykreslí rychle a čtení a dekódování by soutěžilo s vykreslováním o procesor. */
	public boolean lzeNahledZCache(final Ka ka) {
		return !ka.getType().isOffline();
	}

	/**
	 * Hotový obrázek dlaždice z paměti, u online map jinak z cache na disku (nevykresluje ani nestahuje). Čte z disku, volat jen z vlákna na pozadí, viz
	 * {@link #naPozadiProNahled}. Rozhodnutí, odkud se náhled bere, je jen tady.
	 */
	public Image nahledZPametiNeboCache(final Ka ka) {
		final Image zPameti = nahledZPameti(ka);
		if (zPameti != null) {
			return zPameti;
		}
		if (!lzeNahledZCache(ka) || kachleManager == null) {
			return null;
		}
		try {
			return nahledyRodicu.get(ka, () -> Optional.ofNullable(nactiZCache(ka))).orElse(null);
		} catch (final java.util.concurrent.ExecutionException | RuntimeException e) {
			log.debug("Náhled dlaždice {} z cache nelze načíst: {}", ka, e.toString());
			return null;
		}
	}

	/** Jedno vlákno pro náhledy z cache; při nahromadění se zahazují nejstarší úlohy, platí poslední zobrazení. */
	private final ThreadPoolExecutor nahledyZCache = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(NAHLEDY_ZE_CACHE_FRONTA),
			vlaknaRenderu("Náhled z cache"), new ThreadPoolExecutor.DiscardOldestPolicy());

	private static final int NAHLEDY_ZE_CACHE_FRONTA = 64;

	/** Spustí na pozadí hledání náhledu; nestihnuté úlohy se zahodí. */
	public void naPozadiProNahled(final Runnable uloha) {
		nahledyZCache.execute(uloha);
	}

	/** Vlákna stahování online dlaždic, pro testy pořadí ve frontě. */
	ExecutorService vlaknaWebOnline() {
		return exekucniSluzby.get(Priority.KACHLE).web;
	}

	/** Úlohy čekající na stažení online dlaždic, pro testy. */
	BlockingQueue<Runnable> frontaWebOnline() {
		return exekucniSluzby.get(Priority.KACHLE).webQueue;
	}

	/** Hotový obrázek dlaždice, když je už v paměťové cache; jinak null. Nic nespouští. */
	public Image nahledZPameti(final Ka ka) {
		final Kachlice kachlice = kachlmap.getIfPresent(ka);
		return kachlice == null ? null : kachlice.getImage();
	}

	/**
	 * Zadá požadavekl na získání jedné kachle.
	 *
	 * @param kaoneReq
	 * @param aDiagnosticsData
	 * @return
	 */
	private Kanceler ziskejObsah(final DvojiceExekucnichSluzeb dvojiceExekucnichSluzeb, final KaOneReq kaoneReq, final DiagnosticsData aDiagnosticsData) {
		final ImageReceiver imageReceiver = kaoneReq.getImageReceiver();
		pocitSubmitJednaDlazdice.inc();
		final Ka kaone = kaoneReq.getKa();
		if (jePohybMapou(kaoneReq)) {
			posledniPozadavek = ukladac.hodiny.getAsLong();
		}
		final DiagnosticsData diagnosticsData = aDiagnosticsData.with("kaOne", kaoneReq);

		final Kachlice kachlice = kachlmap.asMap().computeIfAbsent(kaone, k -> new Kachlice(k, dvojiceExekucnichSluzeb, diagnosticsData));
		(kachlice.image == null ? pocitMemMinuti : pocitMemZasah).inc();
		kachlice.ziskej(imageReceiver);
		return () -> {
			kachlice.kancluj(imageReceiver);
		};
	}
}
