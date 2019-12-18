package cz.geokuk.plugins.kesoid.importek;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.framework.Progressor;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.GpxToWptContext;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.util.file.KeFile;
import cz.geokuk.util.procak.ProcakDispatcher;

public class KesoidImportBuilder implements IImportBuilder, GpxToWptContext {

	private static final Logger log = LogManager.getLogger(KesoidImportBuilder.class.getSimpleName());

	private static final String PREFIX_BEZEJMENNYCH_WAYPOINTU = "Geokuk";
	static final String GEOCACHE = "Geocache";
	static final String GEOCACHE_FOUND = "Geocache Found";


	private final Genom genom;
	private KesBag kesBag;

	private int citacBezejmennychWaypintu;
	private InformaceOZdroji infoOCurrentnimZdroji;
	private final InformaceOZdrojich.Builder informaceOZdrojichBuilder = InformaceOZdrojich.builder();

	private final GccomNick gccomNick;
	private final ProgressModel progressModel;

	private final KesoidPluginManager kesoidPluginManager;

	private ProcakDispatcher<GpxWpt> gpxWptDispatcher;

	private List<Wpt> wpts;

	private final Map<String, GpxWpt> gpxwpts = new HashMap<String, GpxWpt>(1023);

	public KesoidImportBuilder(final Genom genom, final GccomNick gccomNick, final ProgressModel progressModel, final KesoidPluginManager kesoidPluginManager) {
		this.genom = genom;
		this.gccomNick = gccomNick;
		this.progressModel = progressModel;
		this.kesoidPluginManager = kesoidPluginManager;
	}

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

	@Override
	public void addTrackWpt(final GpxWpt wpt) {}

	@Override
	public void begTrack() {}

	@Override
	public void begTrackSegment() {}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#init()
	 */
	@Override
	public void init() {
		wpts = new LinkedList<Wpt>();
		gpxWptDispatcher = kesoidPluginManager.createGpxWptProcakDispatcher(this, wpts::add);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#done(cz.geokuk.plugins.kesoid.mapicon.Genom)
	 */
	@Override
	public void done() {
		gpxWptDispatcher.done();
		final InformaceOZdrojich informaceOZdrojich = informaceOZdrojichBuilder.done();
//		Progressor progressor = progressModel.start(delkaTasku, "Vytvářím waypointy");

		// přesypeme do seznamu

		// FIXME vyřešit jednoduché waypointy
//		// A všechno, co zbylo jsou obyčejné jednoduché waypointy
//		for (final ListIterator<GpxWpt> it = list.listIterator(); it.hasNext();) {
//			final GpxWpt gpxwpt = it.next();
//			final SimpleWaypoint simpleWaypoint = createSimpleWaypoint(gpxwpt);
//			resultKesoidsByName.put(gpxwpt.name, simpleWaypoint);
//			it.remove();
//		}
//
//		progressor.finish();

		//////////////////////////////////////
		log.debug("Indexuji waypointy: " + wpts.size());

		kesBag = new KesBag(genom);
		final Progressor progressor = progressModel.start(wpts.size(), "Indexování");
		int citac = 0;
		try {
			for (final Wpt wpt : wpts) {
				kesBag.add(wpt);
			}
			if (citac++ % 1000 == 0) {
				progressor.setProgress(citac);
			}
			kesBag.setInformaceOZdrojich(informaceOZdrojich);
			kesBag.done();
		} finally {
			progressor.finish();
		}
		log.debug("Konec zpracování: " + wpts.size());
	}

	@Override
	public void endTrack() {}

	@Override
	public void endTrackSegment() {}

//	@Override
//	public GpxWpt get(final String aName) {
//		return gpxwpts.get(aName);
//	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#getKesBag()
	 */
	public KesBag getKesBag() {
		return kesBag;
	}



	public synchronized void setCurrentlyLoading(final KeFile aJmenoZdroje, final boolean nacteno) {
		infoOCurrentnimZdroji = informaceOZdrojichBuilder.add(aJmenoZdroje, nacteno);
	}

	@Override
	public void setTrackName(final String aTrackName) {}

	protected EKesStatus urciStatus(final boolean archived, final boolean availaible) {
		if (archived) {
			return EKesStatus.ARCHIVED;
		} else if (!availaible) {
			return EKesStatus.DISABLED;
		} else {
			return EKesStatus.ACTIVE;
		}
	}


	@Override
	public Wpt createWpt(final GpxWpt gpxwpt) {
		final Wpt wpt = new Wpt();
		wpt.setWgs(gpxwpt.wgs);
		wpt.setElevation(urciElevation(gpxwpt));
		wpt.setName(gpxwpt.name);
		wpt.setNazev(vytvorNazev(gpxwpt));
		return wpt;
	}

	@Override
	public Set<Alela> definujUzivatslskeAlely(final GpxWpt gpxwpt) {
		final Set<Alela> alely = new HashSet<>();

		for (final Map.Entry<String, String> entry : gpxwpt.gpxg.userTags.entrySet()) {
			final String alelaName = entry.getValue();
			final String genName = entry.getKey();
			final Alela alela = genom.gen(genName).alela(alelaName);
			if (alela == null) {
				continue;
			}
			alely.add(alela);
			genom.UNIVERZALNI_DRUH.addGen(alela.getGen());
		}

		return alely;
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
	public Genom getGenom() {
		return genom;
	}

	@Override
	public GccomNick getGccomNick() {
		return gccomNick;
	}

	@Override
	public GpxWpt get(final String name) {
		return gpxwpts.get(name);
	}



}
