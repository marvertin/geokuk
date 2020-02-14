package cz.geokuk.plugins.kesoid.importek;

import java.util.HashMap;
import java.util.Map;

import cz.geokuk.plugins.kesoid.EWptStatus;
import cz.geokuk.plugins.kesoid.Wpti;
import cz.geokuk.plugins.kesoid.kind.GpxToWptContext;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.util.file.KeFile;
import cz.geokuk.util.procak.ProcakDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Builder, který přijímá GpxWpt přímo ze zdrojů, ze kterých se načítají.
 * Toto propasírovává přes pluginy "Procáky" jenž zajišťují vytvoření Wpt.
 * Vyrobené Wpt se posílají dál.
 * @author Martin
 *
 */
@Slf4j
@RequiredArgsConstructor
public class GpxWptmportBuilder implements IImportBuilder {



	private static final String PREFIX_BEZEJMENNYCH_WAYPOINTU = "Geokuk";
	static final String GEOCACHE = "Geocache";
	static final String GEOCACHE_FOUND = "Geocache Found";

	private final KesoidPluginManager kesoidPluginManager;
	private final GpxToWptContext gpxToWptContext;
	private final WptReceiver wptReceiver;


	private int citacBezejmennychWaypintu;
	private InformaceOZdroji infoOCurrentnimZdroji;
	private final InformaceOZdrojich.Builder informaceOZdrojichBuilder = InformaceOZdrojich.builder();


	private ProcakDispatcher<GpxWpt> gpxWptDispatcher;

	private final Map<String, GpxWpt> gpxwpts = new HashMap<String, GpxWpt>(1023);
	// Až všechno doběhne, budou informace o zdrohjícz
	private InformaceOZdrojich informaceOZdrojich;


	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#addGpxWpt(cz.geokuk.plugins.kesoid.importek.GpxWpt)
	 */
	@Override
	public void addGpxWpt(final GpxWpt gpxwpt) {
		if (gpxwpt.wgs == null || gpxwpt.wgs.lat < -85 || gpxwpt.wgs.lat > 85) {
			log.debug("Souradnice jsou mimo povoleny rozsah: {} - {}", gpxwpt.wgs, gpxwpt);
			return;
		}

		// vygenerovat jméno, pokud ho ještě nemáme
		if (gpxwpt.name == null) {
			citacBezejmennychWaypintu++;
			gpxwpt.name = PREFIX_BEZEJMENNYCH_WAYPOINTU + citacBezejmennychWaypintu;
		}

		// Přeplácnout nějaký, který tam už je
		final GpxWpt old = gpxwpts.put(gpxwpt.name, gpxwpt);

		gpxwpt.iInformaceOZdroji = infoOCurrentnimZdroji; // aby si pamatoval, ze kterého je zdroje
		// a teď výpočty počtů
		infoOCurrentnimZdroji.pocetWaypointuCelkem++; // tak samozřejmě, že celkem je tam
		// TODO [veverka] Těžká kešovina to je, to musí býti jinde. -- 19. 12. 2019 17:43:13 veverka
		if (old != null) { // už tam byl
			// A pokud byl původní nalezen a tento ne, tak je tento už také nalezen.
			// ale až po výstupu do keše samozřejmě, aby při smazání souboru se z keše nebraly nesmysly
			if (old != null && GEOCACHE_FOUND.equals(old.sym) && GEOCACHE.equals(gpxwpt.sym)) {
				gpxwpt.sym = GEOCACHE_FOUND;
			}

		} else {
			infoOCurrentnimZdroji.pocetWaypointuBranych++; // tak samozřejmě, že těch braných je také tam
			gpxWptDispatcher.dispatch(gpxwpt);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#init()
	 */
	@Override
	public void init() {
		kesoidPluginManager.initLoading();
		gpxWptDispatcher = kesoidPluginManager.createGpxWptProcakDispatcher(gpxToWptContext,
				(gpxwpt, kepodr) -> {
					final Wpti wpt = new Wpti();
					wpt.setKepodr(kepodr);
					wpt.setWgs(gpxwpt.wgs);
					wpt.setElevation(urciElevation(gpxwpt));
					wpt.setIdentifier(gpxwpt.name);
					wpt.setNazev(vytvorNazev(gpxwpt));
					return wpt;
				}, wptReceiver);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#done(cz.geokuk.plugins.kesoid.mapicon.Genom)
	 */
	@Override
	public void done() {
		gpxWptDispatcher.done(); // ještě dojíždějí nezpracovanci
		informaceOZdrojich = informaceOZdrojichBuilder.done();
		kesoidPluginManager.doneLoading();
	}

	public synchronized void setCurrentlyLoading(final KeFile aJmenoZdroje, final boolean nacteno) {
		infoOCurrentnimZdroji = informaceOZdrojichBuilder.add(aJmenoZdroje, nacteno);
	}

	public InformaceOZdrojich getInformaceOZdrojich() {
		if (informaceOZdrojich == null) {
			throw new IllegalStateException("Ještě nedoběhlo načítání, nejsou informace o zdrojích");
		}
		return informaceOZdrojich;
	}

	protected EWptStatus urciStatus(final boolean archived, final boolean availaible) {
		if (archived) {
			return EWptStatus.ARCHIVED;
		} else if (!availaible) {
			return EWptStatus.DISABLED;
		} else {
			return EWptStatus.ACTIVE;
		}
	}

	private int urciElevation(final GpxWpt gpxwpt) {
		if (gpxwpt.ele != 0) {
			return (int) gpxwpt.ele;
		} else {
			if (gpxwpt.gpxg != null) {
				return gpxwpt.gpxg.elevation;
			} else {
				return 0;
			}
		}
	}

	private String vytvorNazev(final GpxWpt gpxwpt) {
		String s;
		if (gpxwpt.desc == null) {
			if (gpxwpt.cmt == null) {
				s = "?";
			} else {
				s = gpxwpt.cmt;
			}
		} else {
			if (gpxwpt.cmt == null) {
				s = gpxwpt.desc;
			} else {
				if (gpxwpt.cmt.toLowerCase().contains(gpxwpt.desc.toLowerCase())) {
					s = gpxwpt.desc;
				} else {
					s = gpxwpt.desc + ", " + gpxwpt.cmt;
				}
			}
		}
		return s;
	}


	@Override
	public GpxWpt get(final String name) {
		return gpxwpts.get(name);
	}

	// Tracky tady v této instanci nezpracováváme
	@Override
	public void addTrackWpt(final GpxWpt wpt) {}

	@Override
	public void begTrack() {}

	@Override
	public void endTrack() {}

	@Override
	public void endTrackSegment() {}

	@Override
	public void begTrackSegment() {}

	@Override
	public void setTrackName(final String aTrackName) {}


}
