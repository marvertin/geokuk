package cz.geokuk.plugins.mapy.kachle;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.*;
import com.google.common.util.concurrent.ForwardingFuture;

import cz.geokuk.core.onoffline.OnofflineModel;
import cz.geokuk.plugins.mapy.kachle.KachleManager.ItemToSave;
import cz.geokuk.plugins.mapy.kachle.KachloDownloader.EPraznyObrazek;
import cz.geokuk.plugins.mapy.kachle.KachloStav.EFaze;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.pocitadla.*;

/**
 * V novém pojetí zodpovídá za získávání kachlí z disku, paměti i downloadování.
 * 
 * @author veverka
 *
 */
public class KachleZiskavac {

	private static final Logger									log													= LogManager.getLogger(KachleZiskavac.class.getSimpleName() + "_diskWrite");

	private final Pocitadlo										pocitSubmitJednaDlazdice							= new PocitadloRoste("ka01 Počet požadavků na jednu dlaždici",
			"Kolikrát byl nakonec zadán požadavek na získání jedné dlaždice z jedné vrstvy. Tedy pokud zobrazujeme mapu s turistickými trasami a cyklotrasami, je dlaždice na podklad, na turistickou i na cyklo počítána zvlášť.");

	private final Pocitadlo										pocitZasahPametoveKese								= new PocitadloRoste("ka11 MEM cache #zásahů",
			"Kolikrát se podařilo hledanou dlaždici zasáhnout v paměti. Číslo stále roste a mělo by být ve srovnání s ostatními zásahy co největší.");

	private final Pocitadlo										pocitMinutiPametoveKese								= new PocitadloRoste("ka12 MEM cache #minutí",
			"Kolikrát se nepodařilo hledanou dlaždici v paměťové keši nalézt a co se dělo dál není tímto atributem určeno..");

	private final Pocitadlo										pocitZametenePametoveKese							= new PocitadloRoste("ka13 MEM cache #zametených",
			"Počet obrázků, které garbage collector zametrl pryč a my díky tomu odstranili referenci z keše.");

	private final PocitadloRoste								pocitSubmitDiskoveKese								= new PocitadloRoste("ka21 DISK cache #požadovaných",
			"Kolikrát bylo požadováno číst dlaždici z disku. Obsahuje všechny zásahy, minutí, chyby a také skutečně zknclované čtení");

	private final PocitadloRoste								pocitZasahDiskoveKese								= new PocitadloRoste("ka22 DISK cache #zásahů",
			"Kolikrát se podařilo hledanou dlaždici zasáhnout na disku, tedy naloadovat. Číslo stále roste a mělo by být ve srovnání s ostatními zásahy co největší.");

	private final PocitadloRoste								pocitMinutiDiskoveKese								= new PocitadloRoste("ka23 DISK cache #minutí",
			"Kolikrát se nepodařilo hledanou dlaždici v paměťové keši minout, co se dělo dál není tímto atributem určeno..");

	private final PocitadloRoste								pociChybDiskoveKese									= new PocitadloRoste("ka24 DISK cache #chyb čtení",
			"Kolikrát selhalo čtení dlaždich z disku.");

	private final PocitadloRoste								pocitSubmitWeb										= new PocitadloRoste("ka31 WEB #požadovaných",
			"Kolikrát bylo požadováno číst dlaždici z webu. Vždy poté, co se nenašly na disku.disku. Obsahuje všechny úspěšně i neúspěšně načtené a také zkanclované");

	private final PocitadloRoste								pocitDownloadWeb									= new PocitadloRoste("ka32 WEB #načtených",
			"Kolikrát se muselo hledanou flaždici stáhnout z webu a to úspěšně.");

	private final PocitadloRoste								pocitChybDownloadWeb								= new PocitadloRoste("ka33 WEB #chyb",
			"Kolikrát downloadování dlaždice zahlásilo chybu.");

	private final PocitadloRoste								pocitZapsanoChunkuNaDisk							= new PocitadloRoste("ka41 disk write #bloků",
			"V kolika diskovžch operacích byl prováděn zápis na disk. Z důvodu optimalizace se zápisy na disk združují do větších bloků.");

	private final PocitadloRoste								pocitZapsanoNaDisk									= new PocitadloRoste("ka42 disk write #dlaždic",
			"Kolik dlaždic bylo úspěšně zapsáno na disk asynchronním zapisovačem.");

	private final PocitadloRoste								pocitCancelCelkem									= new PocitadloRoste("ka51 #cancel celkem",
			"Celkový počet vydaných poždavaků na kanclování");

	private final PocitadloRoste								pocitCancelOdebranListener							= new PocitadloRoste("ka52 #cancel jen odebrán listener",
			"Z celového počtu cancelů ty, kde byl jen odebrán listener a dál se nic nemusí řešit, protože ještě někdo jiný čeká.");

	private final PocitadloRoste								pocitCancelProvedenePokusy							= new PocitadloRoste("ka53 #cance provedené pokusy",
			"Počet skutečně provedených pokusů o kanclování, tedy zavoláníá future.cancel");

	private final PocitadloRoste								pocitCancelNehotovePozadavky						= new PocitadloRoste("ka54 #cance skutečné cancely ještě nedokončených požadavků",
			"Počet SKUTEČNÝCH cancelů ,tedy cancelů proti nehotovým požadavkům.");

	private final PocitadloRoste								pocitCancelPozde									= new PocitadloRoste("ka55 #cance pozdní, když už je kachle získána",
			"Počet tancelů, které přišly pozdě, kdy už jsou data načtena.");

	private static Pocitadlo									pocitPlneniImageDoZnicenychKachli					= new PocitadloRoste("ka56 #duplicitně přijaté obrázky",
			"Počítá, kolikrát se stáhla kachle duplicitně, tedy když už díky plnění jiného poždavku byla naplněna a požadavek nebyl všas zkanclován.");

	private static Pocitadlo									pocitBrzdeniOfflineKdyzJeOnline						= new PocitadloRoste("ka61 #počet brzdení dávkového downloadu",
			"Počítá, kolikrát se o 100 ms zbrzdilo dávkové dotahování, protože běžel online.");

	private final Pocitadlo										pocitVelikostPametoveKese							= new PocitadloMalo("Počet dlaždic v memcache: ",
			"Počet závisí na velikosti pro Javu dostupné paměti (-Xmx) a měl by se ustálit na určité hodnotě, občas možná snížit, nikdy však nemůže být menší než počet dlaždic na mapě.");

	private final PocitadloNula									pocitVelikostDiskFrontyOnline						= new PocitadloNula("ka1 velikost fronty čtení z disku (online)",
			"Kolik je ve frontě požadavků na čtení dlaždic z webu.");
	private final PocitadloNula									pocitVelikostDiskFrontyBatch						= new PocitadloNula("ka2 velikost fronty čtení z disku (batch)",
			"Kolik je ve frontě požadavků na čtení dlaždic z webu.");

	private final PocitadloNula									pocitVelikostWebFrontyOnline						= new PocitadloNula("ka3 veliksot fronty čtení z webu (online)",
			"Kolik je ve frontě požadavků na čtení dlaždic z webu.");

	private final PocitadloNula									pocitVelikostWebFrontyBatch							= new PocitadloNula("ka3 veliksot fronty čtení z webu (batch)",
			"Kolik je ve frontě požadavků na čtení dlaždic z webu.");

	private final PocitadloNula									pocitVelikostZapisoveFronty							= new PocitadloNula("ka3 veliksot fronty zápisu na disk",
			"Kolik bloků dlaždic je ještě ve frontě a čeká na zápis na disk.");

	private final PocitadloMalo									pocitKachlice										= new PocitadloMalo("#kachlic",
			"Počet tzv. kachlic, což je objekt určený k řízení získávání kachle z disku, z webu a pak ukládání. Kachlice také reprezentují MEM cache");

	private static final int									MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU	= 300;
	private static final int									NTHREADS_WEB_CORE_ONLINE							= 5;
	private static final int									NTHREADS_WEB_CORE_BATCH								= 5;
	private static final int									NTHREADS_WEB_MAXIMUM_BACTH							= 10;

	private static final int									WEB_QUEUE_SIZE										= 100;
	private static final int									BATCH_DISK_QUEUE_SIZE								= 100;

	private static final int									NTHREADS_DISK										= 2;

	private final EnumMap<Priority, DvojiceExekucnichSluzeb>	exekucniSluzby										= new EnumMap<>(Priority.class);

	private final ExecutorService								execDiskWrite										= Executors.newFixedThreadPool(1);
	private final ScheduledExecutorService						tikacVelikostiFront									= Executors.newSingleThreadScheduledExecutor();

	private final KachloDownloader								downloader											= new KachloDownloader();

	private final ConcurrentMap<KaOne, Kachlice>				pozadavky											= new MapMaker().weakValues().makeMap();

	private KachleManager										kachleManager;

	private final KachleUkladac									ukladac												= new KachleUkladac();
	private OnofflineModel										onofflineModel;

	private KachleModel											kachleModel;

	public KachleZiskavac() {

		final DvojiceExekucnichSluzeb dvojiceOnline = new DvojiceExekucnichSluzeb();
		// Fronta je pro oonline přístup neomezená, protože nemůžeme nijak blokovat rsponsivnost UI */
		dvojiceOnline.priority = Priority.KACHLE;
		dvojiceOnline.disk = Executors.newFixedThreadPool(NTHREADS_DISK);
		dvojiceOnline.web = Executors.newFixedThreadPool(NTHREADS_WEB_CORE_ONLINE);
		dvojiceOnline.log = LogManager.getLogger(KachleZiskavac.class.getSimpleName() + "_online");

		final DvojiceExekucnichSluzeb dvojiceBatch = new DvojiceExekucnichSluzeb();
		dvojiceBatch.priority = Priority.STAHOVANI;
		// Pro background stahování. Jen jedno vlákno pro disk, krátká fronta a po přetížení nechat na volajícím */
		dvojiceBatch.disk = new ThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(BATCH_DISK_QUEUE_SIZE), new ThreadPoolExecutor.CallerRunsPolicy());
		// Pokud bude fornta na web přetížená, vykonává stahování vlákno pro dotahování z disku, čímž se to zpomalí
		dvojiceBatch.web = new ThreadPoolExecutor(NTHREADS_WEB_CORE_BATCH, NTHREADS_WEB_MAXIMUM_BACTH, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(WEB_QUEUE_SIZE),
				new ThreadPoolExecutor.CallerRunsPolicy());
		dvojiceBatch.log = LogManager.getLogger(KachleZiskavac.class.getSimpleName() + "_batch");

		exekucniSluzby.put(Priority.KACHLE, dvojiceOnline);
		exekucniSluzby.put(Priority.STAHOVANI, dvojiceBatch);

		tikacVelikostiFront.scheduleAtFixedRate(() -> {
			try {
				pocitVelikostDiskFrontyOnline.set(getVelikostFronty(dvojiceOnline.disk));
				pocitVelikostDiskFrontyBatch.set(getVelikostFronty(dvojiceBatch.disk));
				pocitVelikostWebFrontyOnline.set(getVelikostFronty(dvojiceOnline.web));
				pocitVelikostWebFrontyBatch.set(getVelikostFronty(dvojiceBatch.web));
				pocitVelikostZapisoveFronty.set(getVelikostFronty(execDiskWrite));
				pocitVelikostPametoveKese.set(pozadavky.size());
			} catch (final Exception e) {
				log.error("Chyba při tikání a zjišťování velikosti fornt: {}", e);
			}

		} , 1l, 1l, TimeUnit.SECONDS);
	}

	private int getVelikostFronty(final ExecutorService executorService) {
		return ((ThreadPoolExecutor) executorService).getQueue().size();
	}

	/**
	 * Zadá požadavekl na získání jedné kachle.
	 * 
	 * @param kaoneReq
	 * @param imageReceiver
	 * @param aDiagnosticsData
	 * @return
	 */
	private Kanceler ziskejObsah(final DvojiceExekucnichSluzeb dvojiceExekucnichSluzeb, final KaOneReq kaoneReq, final ImageReceiver imageReceiver, final DiagnosticsData aDiagnosticsData) {
		pocitSubmitJednaDlazdice.inc();
		final KaOne kaone = kaoneReq.getKa();
		if (kaone.getType() == EKaType._BEZ_PODKLADU) { // hned posílám bezpodkladovce
			final Image img = downloader.prazdnyObrazekBezDat(EPraznyObrazek.BEZ_PODKLADU).img;
			aDiagnosticsData.with(null, "bezpokladu").send("Prázdný obrázek");
			imageReceiver.send(new KachloStav(EFaze.RESULT_ONE, img)); // tváříme se, že je to normální obrázek průběžně dodaný
			return Kanceler.EMPTY; // opravdu není co kanclovat, když ani nepřidávám receiver
		}
		final DiagnosticsData diagnosticsData = aDiagnosticsData.with("kaOne", kaoneReq);

		final Kachlice kachlice = pozadavky.computeIfAbsent(kaone, k -> new Kachlice(k, dvojiceExekucnichSluzeb.log));
		final KachloStav kachloStav;
		boolean submitnoutDoFronty;
		synchronized (kachlice) {
			if (kachlice.image != null) {
				kachloStav = new KachloStav(EFaze.RESULT_ONE, kachlice.image);
				submitnoutDoFronty = false;
			} else { // obrázek ještě nemáme
				kachloStav = null;
				kachlice.receivers.add(imageReceiver); // pokud ješte nebyl, je přidán receiver, protože získáváme
				submitnoutDoFronty = !kachlice.uzSeNacita;
				kachlice.uzSeNacita = true; // a poznačíme, že se načítá ještě v synchronizaci, je nevyhnutlené, že se to stane
			}
		}
		if (kachloStav != null) {
			diagnosticsData.send("Z paměti");
			pocitZasahPametoveKese.inc();
			imageReceiver.send(new KachloStav(EFaze.RESULT_ONE, kachlice.image));
			return Kanceler.EMPTY; // opravdu není co kanclovat, když ani nepřidávám receiver
		} else {
			pocitMinutiPametoveKese.inc();
		}

		if (submitnoutDoFronty) { // když se ještě nenačítá, nutno načíst
			final MyFuture<Void> future = new MyFuture<Void>();
			future.delegate = submitDiskLoad(dvojiceExekucnichSluzeb, kaone, kachlice, diagnosticsData);
			kachlice.future = future; // tato futura bude delegovat, protože později se to vystřídá
			diagnosticsData.send("Submitnuto do fronty");
		} else {
			diagnosticsData.send("Načítáno dřívějším požadavkem");
		}
		// A nyní máme future nebo nemáme, podle toho zda jsme submitující. Pokud se dříve vrátí nesubmitujíci a cancelne, sice není future, ale nekancluje se
		// protože už je mezi receivery ten, který future má, ale ten nemůže být kanclován, protože ještě svoji future nevrátil
		// Teoreticky se může kanclování minout, pokud na stejný receiver dáme více požadavků. Pak ten nesubmitující může být zkanclován dříve, nemaje future nekancluje,
		// takže se načtení provede. To je však tak ojedinělý případ, že by to nemělo vadit.
		return () -> {
			kachlice.kancluj(imageReceiver, diagnosticsData);
		};
	}

	/**
	 * Kachlice drží obrázek jedné kachle. Je zodpovědná za uchování stavu během získávání kachle.
	 *
	 */
	private class Kachlice implements ImageReceiver {
		private final KaOne		kaone;

		/**
		 * Příznak, že se už načítá, takže další požadavek nezahajuje načítání. K testu nelze použít future, protože future je k dispozcici až po submitu a protože je možné, že zpracování proběhne v aktuálním vlákně, nechceme submitnutí synchronizovat
		 */
		boolean					uzSeNacita;
		MyFuture<Void>			future;										// pokud není null, probíhá získáváí a je možné kanclovat
		Image					image;										// pokud už máme obrázek, je vše hotovo a je zde
		Set<ImageReceiver>		receivers	= Sets.newIdentityHashSet();	// tyto je nutné všechny informovat

		private byte[]			imageData;

		private final Logger	log;

		public Kachlice(final KaOne kaone, final Logger log) {
			this.kaone = kaone;
			this.log = log;
			pocitKachlice.inc();
		}

		@Override
		protected void finalize() throws Throwable {
			if (image != null) {
				pocitZametenePametoveKese.inc();
			}
			pocitKachlice.dec();
		}

		synchronized void kancluj(final ImageReceiver imageReceiver, final DiagnosticsData diagnosticsData) {
			pocitCancelCelkem.inc();
			// To co kancluju, dám pryč
			log.debug("POŽADEVK NA CANCEL 1: {} - {}", kaone, receivers.size());
			receivers.remove(imageReceiver);
			log.debug("POŽADEVK NA CANCEL 2: {} - {}", kaone, receivers.size());
			if (receivers.size() == 0) { // kanclovat můžeme jen když už nikdo nečeká
				if (future != null) { // a jen když máme čím kanclovat, může to být už načteno a tedy kancloadlo vymazáno
					pocitCancelProvedenePokusy.inc();
					if (!future.isDone()) {
						pocitCancelNehotovePozadavky.inc();
						log.debug("KANCLUJU SKUTEČNĚ: {} - {}", kaone, diagnosticsData);
					} else {
						pocitCancelPozde.inc();
					}
					future.cancel(true);
					future = null;
				}
			} else {
				pocitCancelOdebranListener.inc();
			}
		}

		@Override
		public void send(final KachloStav kachloStav) {
			boolean zaplanovatUlozeni = false;
			synchronized (this) {
				receivers.forEach(rcv -> rcv.send(kachloStav)); // rozesíláme vždy dál
				final Image img = kachloStav.img;
				if (img != null) {
					if (image != null) { // kdyý už tam bylo nenull, bylo to zbytečné
						pocitPlneniImageDoZnicenychKachli.inc();
					}
					receivers.clear(); // už nebude potřeba posílat.
					image = img; // obrázek si samozřejmě schováme
					future = null; // už dojelo, tak není co kanclovat
					uzSeNacita = false;
					if (kachloStav.imageData != null) {
						this.imageData = kachloStav.imageData;
						zaplanovatUlozeni = true;
					}
				}
			}
			if (zaplanovatUlozeni) {
				ukladac.zaplanujUlozeni(this);
			}
		}

		@Override
		public String toString() {
			return kaone.toString();
		}

	}

	private Future<Void> submitDiskLoad(final DvojiceExekucnichSluzeb dvojiceExekucnichSluzeb, final KaOne kaone, final Kachlice kachlice, final DiagnosticsData diagnosticsData) {
		final Logger log = dvojiceExekucnichSluzeb.log;
		log.debug("SUBMITTING DISK LOAD  \"{}\" | {}", kaone, diagnosticsData);
		pocitSubmitDiskoveKese.inc();
		final Future<Void> future = dvojiceExekucnichSluzeb.disk.submit(() -> {

			log.debug("DISK LOAD START: {} | {}", kaone, diagnosticsData);
			diagnosticsData.send("Disk load - start");

			Image image;
			try {
				image = kachleManager.load(kaone);
			} catch (final Exception e) {
				pociChybDiskoveKese.inc();
				pocitMinutiDiskoveKese.dec(); // aby se epočíalo do minutých, neb bude zvětšeno
				FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Chyba při čtení kachle z databáze");
				e.printStackTrace();
				image = null; // jako by se nic nestalo
			}
			if (image != null) {
				diagnosticsData.send("Disk load - end success");
				pocitZasahDiskoveKese.inc();
				kachlice.send(new KachloStav(EFaze.RESULT_ONE, image));
				return null; // a máme to za sebou
			} else {
				diagnosticsData.send("Disk load - not in disk cache");
				pocitMinutiDiskoveKese.inc();
				if (!onofflineModel.isOnlineMode()) {
					kachlice.send(new KachloStav(EFaze.RESULT_ONE, downloader.prazdnyObrazekBezDat(EPraznyObrazek.OFFLINE).img));
					return null; // a konec nemůžeme downloadovat v offline módu
				}
			}

			// připravíme se na submitnutí
			final URL url = buildUrl(kaone);
			final DiagnosticsData diagnosticsData2 = diagnosticsData.with("url", url);

			// Kcyž někdo přerušil, tak hned končíme
			if (Thread.interrupted()) { // nebudeme pokračovat, pokud byl interaptnut
				log.debug("DISK LOAD INTERRUPTED-2  {}", kaone, diagnosticsData);
				return null;
			}
			// Hned submitneme, nikdo nepřerušil
			final Future<Void> newFuture = submitDownload(dvojiceExekucnichSluzeb, url, kachlice, diagnosticsData2);

			synchronized (kachlice) {
				// Ale on po submitnutí před vstupem do synchronizované sekce někdo mohl interaptovat a to ještě stále tento task, protože jsme nepřepli
				if (Thread.interrupted()) { // nebudeme pokračovat, pokud byl interaptnut
					newFuture.cancel(true); // musíme to co jsem submitnuli hned zabít
					log.debug("DISK LOAD INTERRUPTED-3  {}", kaone, diagnosticsData);
					return null;
				}
				// A přepneme to, co se bude kanclovat, když dojde ke cancelu až teď, kancluje se doěnload
				kachlice.future.delegate = newFuture;
			}
			return null;
		});
		return future;
	}

	private Future<Void> submitDownload(final DvojiceExekucnichSluzeb dvojiceExekucnichSluzeb, final URL url, final Kachlice kachlice, final DiagnosticsData diagnosticsData) {
		final Logger log = dvojiceExekucnichSluzeb.log;
		log.debug("SUBMITTING WEB DOWNLOAD  \"{}\" | {}", url, diagnosticsData);
		pocitSubmitWeb.inc();
		final Future<Void> future = dvojiceExekucnichSluzeb.web.submit(() -> {

			try {
				// Pokud stahujeme backgroundově a jsou nějaké požadavny na online kachle, tak ty online mají absolutní přednost,
				// backgroundy brzíme.
				while (dvojiceExekucnichSluzeb.priority == Priority.STAHOVANI && getVelikostFronty(exekucniSluzby.get(Priority.KACHLE).web) > 0) {
					pocitBrzdeniOfflineKdyzJeOnline.inc();
					Thread.sleep(100);
				}
				diagnosticsData.send("Web download - start");
				log.debug("DOWNLOAD START: \"{}\" | {}", url, diagnosticsData);
				final ImageWithData imageWithData = downloader.downloadImage(url);
				log.debug("DOWNLOAD END  : \"{}\" | {}", url, diagnosticsData);
				diagnosticsData.send("Web download - end success");
				pocitDownloadWeb.inc();
				kachlice.send(new KachloStav(EFaze.RESULT_ONE, imageWithData.img, imageWithData.data));
			} catch (final Exception e) {
				final DiagnosticsData diagnosticsDataWithException = diagnosticsData.with("EXCEPTION", e);
				diagnosticsDataWithException.send("Web download - end ERROR");
				pocitChybDownloadWeb.inc();
				kachlice.send(new KachloStav(EFaze.RESULT_ONE, e));
				log.debug("DOWNLOAD ERROR : \"{}\" | {}", url, diagnosticsDataWithException);
			}
			return null;
		});
		return future;
	}

	/**
	 * Získá obsah, získává ho postupně a plní ho do ImageReceveru v rekvesti
	 * 
	 * @param req
	 */
	public Kanceler ziskejObsah(final KaAllReq req, final DiagnosticsData diagnosticsDatax) {
		final DiagnosticsData diagnosticsData = diagnosticsDatax.with("priorita", req.getPriorita());

		final DlazebniKombiner kombiner = new DlazebniKombiner(req.getKa().kaSet.getKts());

		final DvojiceExekucnichSluzeb dvojiceExekucnichSluzeb = this.exekucniSluzby.get(req.getPriorita());
		final Logger log = dvojiceExekucnichSluzeb.log;
		log.debug("ziskejObsah: {}", diagnosticsData);
		final List<Kanceler> kanceleri = req.getKa().getKaOnes().stream().map(kaone -> {

			// Získání obsahu jednotlivých kachlí. Instance převezmou dlažebního kombinéra budou kombinovat.
			return ziskejObsah(dvojiceExekucnichSluzeb, new KaOneReq(kaone, req.getPriorita()), kachloStav -> {
				if (kachloStav.img != null) {
					kombiner.add(kaone.getType(), kachloStav.img);
				} else if (kachloStav.thr != null) {
					kombiner.add(kaone.getType(), kachloStav.thr);
				} else {
					assert false; // jedno to být musí
				}
				final EFaze posilanaFaze = kombiner.isHotovo() ? EFaze.RESULT_ALL_POSLEDNI : EFaze.RESULT_ALL_PRUBEH;
				final BufferedImage kombinedImage = kombiner.getKombinedImage();
				final Throwable firstException = kombiner.getFirstException();
				final KachloStav kachloStav2 = new KachloStav(posilanaFaze, kombinedImage, firstException);
				log.debug("ziskanObsah: {}", diagnosticsData.with("faze", kachloStav2.faze));
				req.getImageReceiver().send(kachloStav2);

			} , diagnosticsData);

		}).collect(Collectors.toList());

		// Vracený kanceler provolá jednotlivé kancelery
		return () -> {
			kanceleri.forEach(kanceler -> kanceler.cancel());
		};

	}

	private URL buildUrl(final KaOne kaone) {
		try {
			final URL url = kaone.getType().getUrlBuilder().buildUrl(kaone);
			return url;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class MyFuture<T> extends ForwardingFuture<T> {

		private Future<T> delegate;

		@Override
		protected Future<T> delegate() {
			return delegate;
		}

	}

	/**
	 * Řízení ukládání achlí na disk po čancích
	 * 
	 * @author veverka
	 *
	 */
	public class KachleUkladac {
		private static final int				CAS_PO_KTEREM_SE_ZAHAJI_UKLADANI_NA_DISK	= 5;
		private final BlockingQueue<Kachlice>	queue										= Queues.newLinkedBlockingQueue();

		ScheduledExecutorService				planovacUkladani							= Executors.newSingleThreadScheduledExecutor();

		private ScheduledFuture<?>				future;

		/**
		 * Zaplánuje uložení kachlice.
		 * 
		 * @param kachlice
		 */
		synchronized void zaplanujUlozeni(final Kachlice kachlice) {
			if (future != null) {
				future.cancel(false); // pokud je něco naplánováno, zabiju to, tím odložím ukládání
			}
			queue.add(kachlice);
			if (queue.size() >= MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU) { // jakmile je toho dost, ukládá se hned a není nutno plánovat
				submitChunks();
			} else { // plánuji zahájit ukládání pro případ, že nová kachle nepřijde.
				future = planovacUkladani.schedule(() -> {
					submitChunks();
				} , CAS_PO_KTEREM_SE_ZAHAJI_UKLADANI_NA_DISK, TimeUnit.SECONDS);
			}
		}

		synchronized private void submitChunks() {
			for (;;) {
				final ArrayList<Kachlice> list = new ArrayList<>(MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU);
				queue.drainTo(list, MAXIMALNI_POCET_UKLADANYCH_KACHLI_V_JEDNOM_CHUNKU);
				if (list.size() == 0) {
					return;
				}
				submitDiskSave(list);
			}
		}

		private void submitDiskSave(final Collection<Kachlice> kachlicky) {
			if (!kachleModel.isUkladatMapyNaDisk()) {
				return; // zakázáno ukládat
			}
			log.debug("Submitujeme ukladani kachle na disk #{}: {}", kachlicky.size(), kachlicky);
			execDiskWrite.submit(() -> {
				log.debug("Ukladani kachle na disk #{}: {}", kachlicky.size(), kachlicky);
				final List<ItemToSave> list = kachlicky.stream().map(kachlice -> new ItemToSave(kachlice.kaone, kachlice.imageData)).collect(Collectors.toList());
				kachleManager.save(list);
				pocitZapsanoChunkuNaDisk.inc();
				pocitZapsanoNaDisk.add(list.size());
				return null;

			});
		}
	}

	/** Smazat se musí třeba při zapnutí online módi */
	public void clearMemoryCache() {
		log.debug("Cache cleared");
		pozadavky.clear();
	}

	public void setKachleManager(final KachleManager kachleManager) {
		this.kachleManager = kachleManager;
	}

	public void inject(final OnofflineModel onofflineModel) {
		this.onofflineModel = onofflineModel;
	}

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
	}

	private static class DvojiceExekucnichSluzeb {
		Priority		priority;
		ExecutorService	disk;
		ExecutorService	web;
		Logger			log;
	}
}
