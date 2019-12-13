package cz.geokuk.plugins.kesoid.importek;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.framework.Progressor;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.Wpt.EZOrder;
import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.util.file.KeFile;

public class KesoidImportBuilder implements IImportBuilder {

	private static final Logger log = LogManager.getLogger(KesoidImportBuilder.class.getSimpleName());

	private static final String PREFIX_BEZEJMENNYCH_WAYPOINTU = "Geokuk";
	private static final String DEFAULT_SYM = "Waypoint";
	private static final String WAYMARK = "Waymark";
	private static final String WM = "WM";
	private static final String GC = "GC";
	private static final String MZ = "MZ";
	private static final String MU = "MU";
	private static final String PIC = "pic";
	static final String GEOCACHE = "Geocache";
	static final String GEOCACHE_FOUND = "Geocache Found";

	private static Pattern patExtrakceCislaCgp;
	private static Pattern patExtrakceSouradnicJtsk;

	private final Map<String, GpxWpt> gpxwpts = new HashMap<>(1023);

	private Genom genom;
	private KesBag kesBag;

	private int citacBezejmennychWaypintu;
	private InformaceOZdroji infoOCurrentnimZdroji;
	private final InformaceOZdrojich.Builder informaceOZdrojichBuilder = InformaceOZdrojich.builder();

	private final GccomNick gccomNick;
	private final ProgressModel progressModel;

	public KesoidImportBuilder(final GccomNick gccomNick, final ProgressModel progressModel) {
		this.gccomNick = gccomNick;
		this.progressModel = progressModel;
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

		// A pokud byl původní nalezen a tento ne, tak je tento už také nalezen.
		// ale až po výstupu do keše samozřejmě, aby při smazání souboru se z keše nebraly nesmysly
		if (old != null && GEOCACHE_FOUND.equals(old.sym) && GEOCACHE.equals(gpxwpt.sym)) {
			gpxwpt.sym = GEOCACHE_FOUND;
		}

		// a teď výpočty počtů
		gpxwpt.iInformaceOZdroji = infoOCurrentnimZdroji; // aby si pamatoval, ze kterého je zdroje
		infoOCurrentnimZdroji.pocetWaypointuCelkem++; // tak samozřejmě, že celkem je tam
		infoOCurrentnimZdroji.pocetWaypointuBranych++; // tak samozřejmě, že těch braných je také tam
		if (old != null) {
			old.iInformaceOZdroji.pocetWaypointuBranych--; // a u té staré potvory musíme snížit brané, protože jsou přepsané
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
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#done(cz.geokuk.plugins.kesoid.mapicon.Genom)
	 */
	public void done(final Genom genom) {
		this.genom = genom;
		final InformaceOZdrojich informaceOZdrojich = informaceOZdrojichBuilder.done();
		final int delkaTasku = gpxwpts.size();
		Progressor progressor = progressModel.start(delkaTasku, "Vytvářím waypointy");

		// přesypeme do seznamu
		final List<GpxWpt> list = new LinkedList<>(gpxwpts.values());
		final Map<String, Kesoid> resultKesoidsByName = new HashMap<>();

		// Process Czech Geodetic Points.
		// TODO : mapaPredTeckou shouldn't leak outside CGP processing - it's tied to it
		final Map<String, CzechGeodeticPoint> mapaPredTeckou = processCzechGeodeticPoints(list, resultKesoidsByName);
		progressor.setProgress(delkaTasku - list.size());

		// Process geocaches.
		processGeocaches(list, resultKesoidsByName);
		progressor.setProgress(delkaTasku - list.size());

		// Process waymarks.
		processWaymarks(list, resultKesoidsByName, mapaPredTeckou);
		progressor.setProgress(delkaTasku - list.size());

		// Process munzees.
		processMunzees(list, resultKesoidsByName);
		progressor.setProgress(delkaTasku - list.size());

		processPhotos(list, resultKesoidsByName);

		// Teď znovu procházíme a hledáme dodatečné waypointy kešoidů
		for (final ListIterator<GpxWpt> it = list.listIterator(); it.hasNext();) {
			final GpxWpt gpxwpt = it.next();
			if (gpxwpt.name.length() < 2) {
				continue;
			}
			final String potentialGcCode = "GC" + gpxwpt.name.substring(2);
			// TODO dodatečné waypointy též pro waymarky
			final Kesoid kesoid = resultKesoidsByName.get(potentialGcCode);
			if (kesoid != null) {
				final Wpt wpt = createAditionalWpt(gpxwpt);
				if (wpt.getType() == EKesWptType.FINAL_LOCATION) {
					wpt.setZorder(EZOrder.FINAL);
					if (kesoid instanceof Kes) {
						final Kes kes = (Kes) kesoid;
						if (Wpt.TRADITIONAL_CACHE.equals(kes.getFirstWpt().getSym()) && Math.abs(kes.getFirstWpt().lat - wpt.lat) < 0.001 && Math.abs(kes.getFirstWpt().lon - wpt.lon) < 0.001) {
							log.debug("Vypouštíme finální waypointy tradičních keší na úvodních souřadnicích: {} {} {} {} {}", kes.getNazev(), kes.getFirstWpt().lat, wpt.lat, kes.getFirstWpt().lon,
									wpt.lon);
						} else {
							kes.setMainWpt(wpt);
							kesoid.addWpt(wpt);
						}
					} else {
						kesoid.addWpt(wpt);
					}
				} else {
					kesoid.addWpt(wpt);
				}
				it.remove();
			}
		}

		progressor.setProgress(delkaTasku - list.size());

		// A všechno, co zbylo jsou obyčejné jednoduché waypointy
		for (final ListIterator<GpxWpt> it = list.listIterator(); it.hasNext();) {
			final GpxWpt gpxwpt = it.next();
			final SimpleWaypoint simpleWaypoint = createSimpleWaypoint(gpxwpt);
			resultKesoidsByName.put(gpxwpt.name, simpleWaypoint);
			it.remove();
		}

		progressor.finish();

		//////////////////////////////////////
		log.debug("Indexuji waypointy: " + delkaTasku);

		kesBag = new KesBag(genom);
		progressor = progressModel.start(resultKesoidsByName.size(), "Indexování");
		int citac = 0;
		try {
			for (final Kesoid kesoid : resultKesoidsByName.values()) {
				for (final Wpt wpt : kesoid.getWpts()) {
					kesBag.add(wpt);
				}
				if (citac++ % 1000 == 0) {
					progressor.setProgress(citac);
				}
			}
			kesBag.setInformaceOZdrojich(informaceOZdrojich);
			kesBag.done();
		} finally {
			progressor.finish();
		}
		log.debug("Konec zpracování: " + delkaTasku);
	}

	@Override
	public void endTrack() {}

	@Override
	public void endTrackSegment() {}

	@Override
	public GpxWpt get(final String aName) {
		return gpxwpts.get(aName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#getKesBag()
	 */
	public KesBag getKesBag() {
		return kesBag;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#init()
	 */
	public void init() {}

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

	private Wpt createAditionalWpt(final GpxWpt gpxwpt) {
		final Wpt wpt = createWpt(gpxwpt);
		wpt.setSym(gpxwpt.sym == null ? DEFAULT_SYM : gpxwpt.sym);
		final boolean rucnePridany = (gpxwpt.gpxg.flag & 1) == 0;
		wpt.setRucnePridany(rucnePridany);
		wpt.setZorder(EZOrder.KESWPT);
		return wpt;
	}

	/**
	 * @param aGpxwpt
	 * @return
	 */
	private CzechGeodeticPoint createCgp(final GpxWpt gpxwpt) {
		final CzechGeodeticPoint cgp = new CzechGeodeticPoint();
		final String suroveCisloBodu = gpxwpt.groundspeak.name;
		String cisloBodu = suroveCisloBodu;
		if (cisloBodu.endsWith(" (ETRS)")) {
			cisloBodu = cisloBodu.substring(0, cisloBodu.length() - 7);
		}
		cgp.setIdentifier(cisloBodu);
		cgp.setVztahx(EKesVztah.NOT);

		// System.out.println(gpxwpt.groundspeak.name);
		// System.out.println(gpxwpt.groundspeak.shortDescription);
		if (gpxwpt.groundspeak.shortDescription != null) {
			if (gpxwpt.groundspeak.shortDescription.startsWith("http")) {
				cgp.setUrl(gpxwpt.groundspeak.shortDescription);
			}
		}

		final Wpt wpt = createWpt(gpxwpt);
		wpt.setName(cisloBodu);
		urciNazevCgpZPseudoKese(cgp, wpt, gpxwpt);
		wpt.setSym(urciSymCgpZPseudoKese(gpxwpt));

		cgp.addWpt(wpt);
		cgp.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));

		return cgp;
	}

	private Kesoid createKes(final GpxWpt gpxwpt) {
		final Kes kes = new Kes();
		kes.setIdentifier(gpxwpt.name);
		kes.setAuthor(gpxwpt.groundspeak.placedBy);
		// kes.setState(gpxwpt.groundspeak.state);
		// kes.setCountry(gpxwpt.groundspeak.country);
		kes.setHidden(gpxwpt.time);
		kes.setHint(gpxwpt.groundspeak.encodedHints);

		kes.setTerrain(EKesDiffTerRating.parse(gpxwpt.groundspeak.terrain));
		kes.setDifficulty(EKesDiffTerRating.parse(gpxwpt.groundspeak.difficulty));
		kes.setSize(EKesSize.decode(gpxwpt.groundspeak.container));
		// kes.set(gpxwpt.groundspeak.);
		// kes.set(gpxwpt.groundspeak.);
		kes.setStatus(urciStatus(gpxwpt.groundspeak.archived, gpxwpt.groundspeak.availaible));
		kes.setVztahx(urciVztah(gpxwpt));
		kes.setUrl(gpxwpt.link.href);

		kes.setHodnoceni(gpxwpt.gpxg.hodnoceni);
		kes.setHodnoceniPocet(gpxwpt.gpxg.hodnoceniPocet);
		kes.setZnamka(gpxwpt.gpxg.znamka);
		kes.setBestOf(gpxwpt.gpxg.bestOf);
		kes.setFavorit(gpxwpt.gpxg.favorites);
		kes.setFoundTime(gpxwpt.gpxg.found);

		final Wpt wpt = createWpt(gpxwpt);
		wpt.setSym(gpxwpt.groundspeak.type);
		wpt.setNazev(gpxwpt.groundspeak.name); // název hlavního waypointu shodný s názvem keše
		wpt.setZorder(EZOrder.FIRST);

		kes.addWpt(wpt);
		kes.setMainWpt(wpt);

		kes.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
		return kes;
	}

	private Munzee createMunzee(final GpxWpt gpxwpt) {
		final Munzee mz = new Munzee();
		mz.setIdentifier(gpxwpt.name);
		if (gccomNick.name.equals(gpxwpt.groundspeak.placedBy)) {
			mz.setVztahx(EKesVztah.OWN);
		} else if (GEOCACHE_FOUND.equals(gpxwpt.sym)) {
			mz.setVztahx(EKesVztah.FOUND);
		} else {
			mz.setVztahx(EKesVztah.NORMAL);
		}
		mz.setUrl(gpxwpt.link.href);
		mz.setAuthor(gpxwpt.groundspeak.placedBy);
		mz.setHidden(gpxwpt.time);

		final Wpt wpt = createWpt(gpxwpt);
		wpt.setNazev(gpxwpt.groundspeak.name);
		if (gpxwpt.name.startsWith(MZ)) {
			wpt.setSym("MZ " + odstranNadbytecneMezery(gpxwpt.groundspeak.type));
		} else {
			wpt.setSym(odstranNadbytecneMezery(gpxwpt.groundspeak.type));
		}

		mz.addWpt(wpt);
		mz.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
		return mz;
	}

	private Photo createPhoto(final GpxWpt gpxwpt) {
		final Wpt wpt = createWpt(gpxwpt);
		wpt.setSym(gpxwpt.sym);

		final Photo photo = new Photo();
		photo.setIdentifier(gpxwpt.link.href);

		photo.addWpt(wpt);
		log.debug("photo: " + photo.getFirstWpt());
		photo.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
		return photo;
	}

	private SimpleWaypoint createSimpleWaypoint(final GpxWpt gpxwpt) {
		final Wpt wpt = createWpt(gpxwpt);
		wpt.setSym(gpxwpt.sym == null ? DEFAULT_SYM : gpxwpt.sym);

		final SimpleWaypoint simpleWaypoint = new SimpleWaypoint();
		simpleWaypoint.setIdentifier(gpxwpt.name);
		simpleWaypoint.setUrl(gpxwpt.link.href);
		simpleWaypoint.addWpt(wpt);
		simpleWaypoint.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
		return simpleWaypoint;
	}

	private Waymark createWaymarkGeoget(final GpxWpt gpxwpt) {
		final Waymark wm = new Waymark();
		wm.setIdentifier(gpxwpt.name);
		if (gccomNick.name.equals(gpxwpt.groundspeak.placedBy)) {
			wm.setVztahx(EKesVztah.OWN);
		} else {
			wm.setVztahx(EKesVztah.NORMAL);
		}
		wm.setUrl(gpxwpt.link.href);
		wm.setAuthor(gpxwpt.groundspeak.placedBy);
		wm.setHidden(gpxwpt.time);

		final Wpt wpt = createWpt(gpxwpt);
		wpt.setNazev(gpxwpt.groundspeak.name);
		wpt.setSym(odstranNadbytecneMezery(gpxwpt.groundspeak.type));

		wm.addWpt(wpt);
		wm.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
		return wm;
	}

	private Waymark createWaymarkNormal(final GpxWpt gpxwpt) {
		final Waymark wm = new Waymark();
		wm.setIdentifier(gpxwpt.name);
		if (gpxwpt.groundspeak != null) {
			if (gccomNick.name.equals(gpxwpt.groundspeak.placedBy)) {
				wm.setVztahx(EKesVztah.OWN);
			} else {
				wm.setVztahx(EKesVztah.NORMAL);
			}
			wm.setAuthor(gpxwpt.groundspeak.placedBy);
		} else {
			wm.setVztahx(EKesVztah.NORMAL);
		}
		wm.setUrl(gpxwpt.link.href);

		final Wpt wpt = createWpt(gpxwpt);
		wpt.setNazev(gpxwpt.link.text);
		wpt.setSym(odstranNadbytecneMezery(gpxwpt.type));

		wm.addWpt(wpt);
		wm.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));

		return wm;
	}

	private Wpt createWpt(final GpxWpt gpxwpt) {
		final Wpt wpt = new Wpt();
		wpt.setWgs(gpxwpt.wgs);
		wpt.setElevation(urciElevation(gpxwpt));
		wpt.setName(gpxwpt.name);
		wpt.setNazev(vytvorNazev(gpxwpt));
		return wpt;
	}

	private EKesType decodePseudoKesType(final GpxWpt gpxwpt) {
		return gpxwpt.groundspeak.type != null ? EKesType.decode(gpxwpt.groundspeak.type) : EKesType.decode(gpxwpt.type.substring(9));
	}

	private Set<Alela> definujUzivatslskeAlely(final GpxWpt gpxwpt) {
		final Set<Alela> alely = new HashSet<>();

		for (final Map.Entry<String, String> entry : gpxwpt.gpxg.userTags.entrySet()) {
			final String alelaName = entry.getValue();
			final String genName = entry.getKey();
			final Alela alela = genom.gen(genName).alela(alelaName);
			if (alela == null) {
				continue;
			}
			alely.add(alela);
		}

		return alely;
	}

	private String extractOznaceniBodu(final String celeJmeno) {
		if (celeJmeno == null) {
			return null;
		}
		if (patExtrakceCislaCgp == null) {
			patExtrakceCislaCgp = Pattern.compile(".*?([0-9]+-[0-9]+).*");
		}
		final Matcher mat = patExtrakceCislaCgp.matcher(celeJmeno);
		if (mat.matches()) {
			return mat.group(1);
		} else {
			return null;
		}
	}

	private JtskSouradnice extrahujJtsk(final String celyretez) {
		if (patExtrakceSouradnicJtsk == null) {
			patExtrakceSouradnicJtsk = Pattern.compile("(.*?)(\\d+)'(\\d+.\\d+) +(\\d+)'(\\d+\\.\\d+) (\\d+\\.\\d+)(.*)");
		}
		final Matcher mat = patExtrakceSouradnicJtsk.matcher(celyretez);
		if (mat.matches()) {
			final JtskSouradnice sou = new JtskSouradnice();
			sou.pred = mat.group(1);
			sou.y = Double.parseDouble(mat.group(2) + mat.group(3));
			sou.x = Double.parseDouble(mat.group(4) + mat.group(5));
			sou.z = Double.parseDouble(mat.group(6));
			sou.po = mat.group(7);
			return sou;
		} else {
			return null;
		}
	}

	private String extrahujPrefixPredTeckou(final GpxWpt gpxwpt) {
		final String jmeno = gpxwpt.groundspeak.name;
		int poz = jmeno.indexOf('.');
		if (poz < 0) {
			poz = jmeno.length();
		}
		return jmeno.substring(0, poz);
	}

	private boolean isCzechGeodeticPoint(final GpxWpt gpxWpt) {
		// TODO: Je špatné rozpoznávat geokeše podle prefixu GC - jsou systémy (Geocaching.su, OpenCaching, ...) které tento prefix nemají a přitom to jsou keše! [2016-04-09, Bohusz]
		return gpxWpt.groundspeak != null && (gpxWpt.name.startsWith(GC) && gpxWpt.name.length() == 8 || gpxWpt.name.matches("^(TrB_|ZhB_|BTP_|ZGS_).*$") || "DATAZ".equals(gpxWpt.groundspeak.owner));
	}

	private boolean isGeocache(final GpxWpt gpxWpt) {
		// TODO: Je špatné rozpoznávat geokeše podle prefixu GC - jsou systémy (Geocaching.su, OpenCaching, ...) které tento prefix nemají a přitom to jsou keše! [2016-04-09, Bohusz]
		return (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym)) && gpxWpt.groundspeak != null && gpxWpt.name.startsWith(GC);
	}

	private boolean isMunzee(final GpxWpt gpxWpt) {
		return (gpxWpt.name.startsWith(MZ) || gpxWpt.name.startsWith(MU)) && (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym));
	}

	private boolean isPhoto(final GpxWpt gpxWpt) {
		return PIC.equals(gpxWpt.type);
	}

	private boolean isWaymark(final GpxWpt gpxWpt) {
		return WAYMARK.equals(gpxWpt.sym) && gpxWpt.name.startsWith(WM);
	}

	private boolean isWaymarkGeoget(final GpxWpt gpxWpt) {
		return gpxWpt.name.startsWith(WM) && (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym));
	}

	/**
	 * @param aType
	 * @return
	 */
	private String odstranNadbytecneMezery(final String s) {
		return s == null ? null : s.replaceAll(" +", " ");
	}

	/**
	 * @param aCgp
	 * @param aGpxwpt
	 */
	private void pridruz(final CzechGeodeticPoint cgp, final GpxWpt gpxwpt) {
		final Wpt wpt = createWpt(gpxwpt);
		wpt.setName(gpxwpt.groundspeak.name);
		urciNazevCgpZPseudoKese(cgp, wpt, gpxwpt);
		wpt.setSym(urciSymCgpZPseudoKese(gpxwpt));
		cgp.addWpt(wpt);
	}

	private Map<String, CzechGeodeticPoint> processCzechGeodeticPoints(final List<GpxWpt> gpxWpts, final Map<String, Kesoid> resultMap) {
		final List<GpxWpt> listCgpPridruzene = new ArrayList<>();
		final Map<String, CzechGeodeticPoint> mapaPredTeckou = new HashMap<>();

		// Procházíme a hledáme české geodetické body, ony jsou tam jako speciální keše,
		// tak je nejdříve vyzobeme
		for (final ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext();) {
			final GpxWpt gpxWpt = it.next();

			if (isCzechGeodeticPoint(gpxWpt)) {
				final EKesType kesType = decodePseudoKesType(gpxWpt);
				switch (kesType) {
				case TRADITIONAL:
				case MULTI:
				case EARTHCACHE:
				case WHERIGO:
					CzechGeodeticPoint cgp = createCgp(gpxWpt);
					resultMap.put(gpxWpt.name, cgp);
					mapaPredTeckou.put(extrahujPrefixPredTeckou(gpxWpt), cgp);
					break;
				case EVENT:
				case CACHE_IN_TRASH_OUT_EVENT:
				case MEGA_EVENT:
					cgp = createCgp(gpxWpt);
					resultMap.put(gpxWpt.name, cgp);
					break;
				case LETTERBOX_HYBRID:
				case UNKNOWN:
					listCgpPridruzene.add(gpxWpt);
					break;
				default:
					final SimpleWaypoint simpleWaypoint = createSimpleWaypoint(gpxWpt);
					resultMap.put(gpxWpt.name, simpleWaypoint);
				}
				it.remove();
			}
		}

		// procházíme přidružeňáky a přidružujeme
		for (final GpxWpt gpxwpt : listCgpPridruzene) {
			final CzechGeodeticPoint cgp = mapaPredTeckou.get(extrahujPrefixPredTeckou(gpxwpt));
			if (cgp != null) {
				pridruz(cgp, gpxwpt);
			} else {
				resultMap.put(gpxwpt.name, createCgp(gpxwpt));
			}
		}

		return mapaPredTeckou;
	}

	private void processGeocaches(final List<GpxWpt> gpxWpts, final Map<String, Kesoid> resultMap) {
		for (final ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext();) {
			final GpxWpt gpxWpt = it.next();
			if (isGeocache(gpxWpt)) {
				resultMap.put(gpxWpt.name, createKes(gpxWpt));
				it.remove();
			}
		}
	}

	private void processMunzees(final List<GpxWpt> gpxWpts, final Map<String, Kesoid> resultMap) {
		for (final ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext();) {
			final GpxWpt gpxWpt = it.next();
			if (isMunzee(gpxWpt)) {
				resultMap.put(gpxWpt.name, createMunzee(gpxWpt));
				it.remove();
			}
		}
	}

	private void processPhotos(final List<GpxWpt> gpxWpts, final Map<String, Kesoid> resultMap) {
		for (final ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext();) {
			final GpxWpt gpxWpt = it.next();
			if (isPhoto(gpxWpt)) {
				resultMap.put(gpxWpt.name, createPhoto(gpxWpt));
				it.remove();
			}
		}
	}

	private void processWaymarks(final List<GpxWpt> gpxWpts, final Map<String, Kesoid> resultsMap, final Map<String, CzechGeodeticPoint> mapaPredTeckou) {

		// Pokusíme se najít ostatní waymarky, to znamená ty, které nebyly speciální
		for (final ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext();) {
			final GpxWpt gpxwpt = it.next();

			Waymark wm;

			if (isWaymark(gpxwpt)) {
				wm = createWaymarkNormal(gpxwpt);
			} else if (isWaymarkGeoget(gpxwpt)) {
				wm = createWaymarkGeoget(gpxwpt);
			} else {
				continue;
			}

			final boolean pripojeno = zkusPripojitKCgp(wm, mapaPredTeckou);
			if (!pripojeno) {
				resultsMap.put(gpxwpt.name, wm);
			}
			it.remove();
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

	private void urciNazevCgpZPseudoKese(final CzechGeodeticPoint cgp, final Wpt wpt, final GpxWpt gpxwpt) {
		String name = firstNonNull(gpxwpt.groundspeak.name, "Unknown CGP");

		final EKesType pseudoKesType = decodePseudoKesType(gpxwpt);

		final String nameCandidate = null;

		switch (pseudoKesType) {
		case TRADITIONAL:
		case LETTERBOX_HYBRID:
		case MULTI:
		case UNKNOWN:
		case CACHE_IN_TRASH_OUT_EVENT:
		case EVENT:
		case MEGA_EVENT:
			name = firstNonNull(gpxwpt.groundspeak.encodedHints, name);
			break;
		case EARTHCACHE:
		case WHERIGO:
			name = firstNonNull(gpxwpt.groundspeak.shortDescription, name);
			break;
		default:
			// Just fall through.
		}

		name = firstNonNull(nameCandidate, name);

		final int poz = name.indexOf("http://");
		if (poz >= 0) {
			name = name.substring(0, poz);
		}
		final JtskSouradnice jtsk = extrahujJtsk(name);
		if (jtsk != null) {
			name = jtsk.pred + jtsk.po;
			cgp.setXjtsk(jtsk.x);
			cgp.setYjtsk(jtsk.y);
			wpt.setElevation((int) jtsk.z);
		}
		name = name.trim();
		if (name.length() == 0) {
			name = "Geodetický bod";
		}
		wpt.setNazev(name);
	}

	/**
	 * @param aGpxwpt
	 * @return
	 */
	private String urciSymCgpZPseudoKese(final GpxWpt gpxwpt) {
		final EKesType pseudoKesType = decodePseudoKesType(gpxwpt);
		if (pseudoKesType == EKesType.TRADITIONAL) {
			return gpxwpt.groundspeak.name.contains("ETRS") ? "TrB (ETRS)" : "TrB";
		}
		if (pseudoKesType == EKesType.LETTERBOX_HYBRID) {
			return "TrB-p";
		}
		if (pseudoKesType == EKesType.MULTI) {
			return "ZhB";
		}
		if (pseudoKesType == EKesType.UNKNOWN) {
			return "ZhB-p";
		}
		if (pseudoKesType == EKesType.EARTHCACHE) {
			return "BTP";
		}
		if (pseudoKesType == EKesType.WHERIGO) {
			return "ZGS";
		}

		if (pseudoKesType == EKesType.EVENT) {
			return "ZVBP";
		}
		if (pseudoKesType == EKesType.CACHE_IN_TRASH_OUT_EVENT) {
			return "PVBP";
		}
		if (pseudoKesType == EKesType.MEGA_EVENT) {
			return "ZNB";
		}

		return "Unknown Cgp";
	}

	private EKesVztah urciVztah(final GpxWpt gpxwpt) {
		if (gpxwpt.explicitneUrcenoVlastnictvi) {
			return EKesVztah.OWN;
		}
		if (gccomNick.name.equals(gpxwpt.groundspeak.owner)) {
			return EKesVztah.OWN;
		}
		if (gccomNick.id == gpxwpt.groundspeak.ownerid) {
			return EKesVztah.OWN;
		}
		if (gccomNick.name.equals(gpxwpt.groundspeak.placedBy)) {
			return EKesVztah.OWN;
		} else if (GEOCACHE_FOUND.equals(gpxwpt.sym)) {
			return EKesVztah.FOUND;
		} else {
			return EKesVztah.NORMAL;
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

	private boolean zkusPripojitKCgp(final Waymark wm, final Map<String, CzechGeodeticPoint> mapaPredTeckou) {
		if (!wm.getMainWpt().getSym().equals("Czech Geodetic Points")) {
			return false; // není to ani ta správná kategorie
		}
		final String oznaceniBodu = extractOznaceniBodu(wm.getNazev());
		if (oznaceniBodu == null) {
			return false; // tak ve jméně není označení, zobrazíme jako normální waymark
		}
		final CzechGeodeticPoint cgp = mapaPredTeckou.get(oznaceniBodu);
		if (cgp == null) {
			return false; // nenašli jsme již existující
		}
		// tak a tady musíme do cgp narvat vše, co víme
		cgp.setVztahx(wm.getVztah());
		cgp.getFirstWpt().setNazev(wm.getNazev());
		cgp.setUrl(wm.getUrl());
		cgp.setAuthor(wm.getAuthor());
		cgp.setHidden(wm.getHidden());
		return true;
	}
}
