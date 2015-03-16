package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.framework.Progressor;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.Wpt.EZOrder;
import cz.geokuk.plugins.kesoid.mapicon.Alela;
import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.google.common.base.MoreObjects.firstNonNull;

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
  private final InformaceOZdrojich informaceOZdrojich = new InformaceOZdrojich();

  private final GccomNick gccomNick;
  private final ProgressModel progressModel;

  public KesoidImportBuilder(GccomNick gccomNick, ProgressModel progressModel) {
    this.gccomNick = gccomNick;
    this.progressModel = progressModel;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#addGpxWpt(cz.geokuk.plugins.kesoid.importek.GpxWpt)
   */
  @Override
  public void addGpxWpt(GpxWpt gpxwpt) {
    if (gpxwpt.wgs == null || gpxwpt.wgs.lat < 8 || gpxwpt.wgs.lat > 80 || gpxwpt.wgs.lon < 1 || gpxwpt.wgs.lon > 30) {
      log.warn("Souradnice jsou mimo povoleny rozsah: " + gpxwpt.wgs + " - " + gpxwpt);
      return;
    }

    // vygenerovat jméno, pokud ho ještě nemáme
    if (gpxwpt.name == null) {
      citacBezejmennychWaypintu++;
      gpxwpt.name = PREFIX_BEZEJMENNYCH_WAYPOINTU + citacBezejmennychWaypintu;
    }

    // Přeplácnout nějaký, který tam už je
    GpxWpt old = gpxwpts.put(gpxwpt.name, gpxwpt);

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

  /* (non-Javadoc)
   * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#init()
   */
  public void init() {
  }

  /* (non-Javadoc)
   * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#done(cz.geokuk.plugins.kesoid.mapicon.Genom)
   */
  public void done(Genom genom) {
    this.genom = genom;
    int delkaTasku = gpxwpts.size();
    Progressor progressor = progressModel.start(delkaTasku, "Vytvářím waypointy");

    // přesypeme do seznamu
    List<GpxWpt> list = new LinkedList<>(gpxwpts.values());
    Map<String, Kesoid> resultKesoidsByName = new HashMap<>();

    // Process Czech Geodetic Points.
    // TODO : mapaPredTeckou shouldn't leak outside CGP processing - it's tied to it
    Map<String, CzechGeodeticPoint> mapaPredTeckou =
        processCzechGeodeticPoints(list, resultKesoidsByName);
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
    for (ListIterator<GpxWpt> it = list.listIterator(); it.hasNext(); ) {
      GpxWpt gpxwpt = it.next();
      if (gpxwpt.name.length() < 2) {
        continue;
      }
      String potentialGcCode = "GC" + gpxwpt.name.substring(2);
      //TODO dodatečné waypointy též pro waymarky
      Kesoid kesoid = resultKesoidsByName.get(potentialGcCode);
      if (kesoid != null) {
        Wpt wpt = createAditionalWpt(gpxwpt);
        if (wpt.getType() == EKesWptType.FINAL_LOCATION) {
          wpt.setZorder(EZOrder.FINAL);
          if (kesoid instanceof Kes) {
            Kes kes = (Kes) kesoid;
            if (Wpt.TRADITIONAL_CACHE.equals(kes.getFirstWpt().getSym())
                && Math.abs(kes.getFirstWpt().lat - wpt.lat) < 0.001
                && Math.abs(kes.getFirstWpt().lon - wpt.lon) < 0.001
                ) {
              log.debug(
                  "Vypouštíme finální waypointy tradičních keší na úvodních souřadnicích: {} {} {} {} {}",
                  kes.getNazev(), kes.getFirstWpt().lat, wpt.lat, kes.getFirstWpt().lon, wpt.lon);
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
    for (ListIterator<GpxWpt> it = list.listIterator(); it.hasNext(); ) {
      GpxWpt gpxwpt = it.next();
      SimpleWaypoint simpleWaypoint = createSimpleWaypoint(gpxwpt);
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
      for (Kesoid kesoid : resultKesoidsByName.values()) {
        for (Wpt wpt : kesoid.getWpts()) {
          kesBag.add(wpt, null);
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

  private EKesType decodePseudoKesType(GpxWpt gpxwpt) {
    return gpxwpt.groundspeak.type != null
        ? EKesType.decode(gpxwpt.groundspeak.type)
        : EKesType.decode(gpxwpt.type.substring(9));
  }

  private void processPhotos(List<GpxWpt> gpxWpts, Map<String, Kesoid> resultMap) {
    for (ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext(); ) {
      GpxWpt gpxWpt = it.next();
      if (isPhoto(gpxWpt)) {
        resultMap.put(gpxWpt.name, createPhoto(gpxWpt));
        it.remove();
      }
    }
  }

  private boolean isPhoto(GpxWpt gpxWpt) {
    return PIC.equals(gpxWpt.type);
  }

  private void processGeocaches(List<GpxWpt> gpxWpts, Map<String, Kesoid> resultMap) {
    for (ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext(); ) {
      GpxWpt gpxWpt = it.next();
      if (isGeocache(gpxWpt)) {
        resultMap.put(gpxWpt.name, createKes(gpxWpt));
        it.remove();
      }
    }
  }

  private boolean isGeocache(GpxWpt gpxWpt) {
    return (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym))
        && gpxWpt.groundspeak != null
        && gpxWpt.name.startsWith(GC);
  }

  private void processWaymarks(
      List<GpxWpt> gpxWpts,
      Map<String, Kesoid> resultsMap,
      Map<String, CzechGeodeticPoint> mapaPredTeckou) {

    // Pokusíme se najít ostatní waymarky, to znamená ty, které nebyly speciální
    for (ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext(); ) {
      GpxWpt gpxwpt = it.next();

      Waymark wm;

      if (isWaymark(gpxwpt)) {
        wm = createWaymarkNormal(gpxwpt);
      } else if (isWaymarkGeoget(gpxwpt)) {
        wm = createWaymarkGeoget(gpxwpt);
      } else {
        continue;
      }

      boolean pripojeno = zkusPripojitKCgp(wm, mapaPredTeckou);
      if (!pripojeno) {
        resultsMap.put(gpxwpt.name, wm);
      }
      it.remove();
    }
  }

  private boolean isWaymark(GpxWpt gpxWpt) {
    return WAYMARK.equals(gpxWpt.sym) && gpxWpt.name.startsWith(WM);
  }

  private boolean isWaymarkGeoget(GpxWpt gpxWpt) {
    return gpxWpt.name.startsWith(WM)
        && (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym));
  }

  private void processMunzees(List<GpxWpt> gpxWpts, Map<String, Kesoid> resultMap) {
    for (ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext(); ) {
      GpxWpt gpxWpt = it.next();
      if (isMunzee(gpxWpt)) {
        resultMap.put(gpxWpt.name, createMunzee(gpxWpt));
        it.remove();
      }
    }
  }

  private boolean isMunzee(GpxWpt gpxWpt) {
    return (gpxWpt.name.startsWith(MZ) || gpxWpt.name.startsWith(MU))
        && (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym));
  }

  private Map<String, CzechGeodeticPoint>
      processCzechGeodeticPoints(List<GpxWpt> gpxWpts, Map<String, Kesoid> resultMap) {
    List<GpxWpt> listCgpPridruzene = new ArrayList<>();
    Map<String, CzechGeodeticPoint> mapaPredTeckou = new HashMap<>();

    // Procházíme a hledáme české geodetické body, ony jsou tam jako speciální keše,
    // tak je nejdříve vyzobeme
    for (ListIterator<GpxWpt> it = gpxWpts.listIterator(); it.hasNext(); ) {
      GpxWpt gpxWpt = it.next();

      if (isCzechGeodeticPoint(gpxWpt)) {
        EKesType kesType = decodePseudoKesType(gpxWpt);
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
            SimpleWaypoint simpleWaypoint = createSimpleWaypoint(gpxWpt);
            resultMap.put(gpxWpt.name, simpleWaypoint);
        }
        it.remove();
      }
    }

    // procházíme přidružeňáky a přidružujeme
    for (GpxWpt gpxwpt : listCgpPridruzene) {
      CzechGeodeticPoint cgp = mapaPredTeckou.get(extrahujPrefixPredTeckou(gpxwpt));
      if (cgp != null) {
        pridruz(cgp, gpxwpt);
      } else {
        resultMap.put(gpxwpt.name, createCgp(gpxwpt));
      }
    }

    return mapaPredTeckou;
  }

  private boolean isCzechGeodeticPoint(GpxWpt gpxWpt) {
    return gpxWpt.groundspeak != null && (
        (gpxWpt.name.startsWith(GC) && gpxWpt.name.length() == 8)
        || gpxWpt.name.matches("^(TrB_|ZhB_|BTP_|ZGS_).*$")
        || "DATAZ".equals(gpxWpt.groundspeak.owner));
  }

  private boolean zkusPripojitKCgp(Waymark wm, Map<String, CzechGeodeticPoint> mapaPredTeckou) {
    if (!wm.getMainWpt().getSym().equals("Czech Geodetic Points")) {
      return false;  // není to ani ta správná kategorie
    }
    String oznaceniBodu = extractOznaceniBodu(wm.getNazev());
    if (oznaceniBodu == null) {
      return false; // tak ve jméně není označení, zobrazíme jako normální waymark
    }
    CzechGeodeticPoint cgp = mapaPredTeckou.get(oznaceniBodu);
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

  private SimpleWaypoint createSimpleWaypoint(GpxWpt gpxwpt) {
    Wpt wpt = createWpt(gpxwpt);
    wpt.setSym(gpxwpt.sym == null ? DEFAULT_SYM : gpxwpt.sym);

    SimpleWaypoint simpleWaypoint = new SimpleWaypoint();
    simpleWaypoint.setIdentifier(gpxwpt.name);
    simpleWaypoint.setUrl(gpxwpt.link.href);
    simpleWaypoint.addWpt(wpt);
    simpleWaypoint.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
    return simpleWaypoint;
  }

  private Photo createPhoto(GpxWpt gpxwpt) {
    Wpt wpt = createWpt(gpxwpt);
    wpt.setSym(gpxwpt.sym);

    Photo photo = new Photo();
    photo.setIdentifier(gpxwpt.link.href);

    photo.addWpt(wpt);
    log.info(photo.getFirstWpt());
    photo.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
    return photo;
  }

  private String extrahujPrefixPredTeckou(GpxWpt gpxwpt) {
    String jmeno = gpxwpt.groundspeak.name;
    int poz = jmeno.indexOf('.');
    if (poz < 0) {
      poz = jmeno.length();
    }
    return jmeno.substring(0, poz);
  }


  /**
   * @param aCgp
   * @param aGpxwpt
   */
  private void pridruz(CzechGeodeticPoint cgp, GpxWpt gpxwpt) {
    Wpt wpt = createWpt(gpxwpt);
    wpt.setName(gpxwpt.groundspeak.name);
    urciNazevCgpZPseudoKese(cgp, wpt, gpxwpt);
    wpt.setSym(urciSymCgpZPseudoKese(gpxwpt));
    cgp.addWpt(wpt);
  }

  /**
   * @param aGpxwpt
   * @return
   */
  private CzechGeodeticPoint createCgp(GpxWpt gpxwpt) {
    CzechGeodeticPoint cgp = new CzechGeodeticPoint();
    String suroveCisloBodu = gpxwpt.groundspeak.name;
    String cisloBodu = suroveCisloBodu;
    if (cisloBodu.endsWith(" (ETRS)")) {
      cisloBodu = cisloBodu.substring(0, cisloBodu.length() - 7);
    }
    cgp.setIdentifier(cisloBodu);
    cgp.setVztahx(EKesVztah.NOT);

    //		System.out.println(gpxwpt.groundspeak.name);
    //		System.out.println(gpxwpt.groundspeak.shortDescription);
    if (gpxwpt.groundspeak.shortDescription != null) {
      if (gpxwpt.groundspeak.shortDescription.startsWith("http")) {
        cgp.setUrl(gpxwpt.groundspeak.shortDescription);
      }
    }

    Wpt wpt = createWpt(gpxwpt);
    wpt.setName(cisloBodu);
    urciNazevCgpZPseudoKese(cgp, wpt, gpxwpt);
    wpt.setSym(urciSymCgpZPseudoKese(gpxwpt));

    cgp.addWpt(wpt);
    cgp.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));

    return cgp;
  }

  private Waymark createWaymarkGeoget(GpxWpt gpxwpt) {
    Waymark wm = new Waymark();
    wm.setIdentifier(gpxwpt.name);
    if (gccomNick.name.equals(gpxwpt.groundspeak.placedBy)) {
      wm.setVztahx(EKesVztah.OWN);
    } else {
      wm.setVztahx(EKesVztah.NORMAL);
    }
    wm.setUrl(gpxwpt.link.href);
    wm.setAuthor(gpxwpt.groundspeak.placedBy);
    wm.setHidden(gpxwpt.time);


    Wpt wpt = createWpt(gpxwpt);
    wpt.setNazev(gpxwpt.groundspeak.name);
    wpt.setSym(odstranNadbytecneMezery(gpxwpt.groundspeak.type));

    wm.addWpt(wpt);
    wm.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
    return wm;
  }


  private Waymark createWaymarkNormal(GpxWpt gpxwpt) {
    Waymark wm = new Waymark();
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

    Wpt wpt = createWpt(gpxwpt);
    wpt.setNazev(gpxwpt.link.text);
    wpt.setSym(odstranNadbytecneMezery(gpxwpt.type));

    wm.addWpt(wpt);
    wm.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));

    return wm;
  }

  private Munzee createMunzee(GpxWpt gpxwpt) {
    Munzee mz = new Munzee();
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


    Wpt wpt = createWpt(gpxwpt);
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

  /**
   * @param aType
   * @return
   */
  private String odstranNadbytecneMezery(String s) {
    return s == null ? null : s.replaceAll(" +", " ");
  }

  /**
   * @param aGpxwpt
   * @return
   */
  private String urciSymCgpZPseudoKese(GpxWpt gpxwpt) {
    EKesType pseudoKesType = decodePseudoKesType(gpxwpt);
    if (pseudoKesType == EKesType.TRADITIONAL)
      return (gpxwpt.groundspeak.name.contains("ETRS")) ? "TrB (ETRS)" : "TrB";
    if (pseudoKesType == EKesType.LETTERBOX_HYBRID) return "TrB-p";
    if (pseudoKesType == EKesType.MULTI) return "ZhB";
    if (pseudoKesType == EKesType.UNKNOWN) return "ZhB-p";
    if (pseudoKesType == EKesType.EARTHCACHE) return "BTP";
    if (pseudoKesType == EKesType.WHERIGO) return "ZGS";

    if (pseudoKesType == EKesType.EVENT) return "ZVBP";
    if (pseudoKesType == EKesType.CACHE_IN_TRASH_OUT_EVENT) return "PVBP";
    if (pseudoKesType == EKesType.MEGA_EVENT) return "ZNB";

    return "Unknown Cgp";
  }

  private void urciNazevCgpZPseudoKese(CzechGeodeticPoint cgp, Wpt wpt, GpxWpt gpxwpt) {
    String name = firstNonNull(gpxwpt.groundspeak.name, "Unknown CGP");

    EKesType pseudoKesType = decodePseudoKesType(gpxwpt);

    String nameCandidate = null;

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

    int poz = name.indexOf("http://");
    if (poz >= 0) {
      name = name.substring(0, poz);
    }
    JtskSouradnice jtsk = extrahujJtsk(name);
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

  private JtskSouradnice extrahujJtsk(String celyretez) {
    if (patExtrakceSouradnicJtsk == null) {
      patExtrakceSouradnicJtsk = Pattern.compile("(.*?)(\\d+)'(\\d+.\\d+) +(\\d+)'(\\d+\\.\\d+) (\\d+\\.\\d+)(.*)");
    }
    Matcher mat = patExtrakceSouradnicJtsk.matcher(celyretez);
    if (mat.matches()) {
      JtskSouradnice sou = new JtskSouradnice();
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

  private Kesoid createKes(GpxWpt gpxwpt) {
    Kes kes = new Kes();
    kes.setIdentifier(gpxwpt.name);
    kes.setAuthor(gpxwpt.groundspeak.placedBy);
    //    kes.setState(gpxwpt.groundspeak.state);
    //    kes.setCountry(gpxwpt.groundspeak.country);
    kes.setHidden(gpxwpt.time);
    kes.setHint(gpxwpt.groundspeak.encodedHints);


    kes.setTerrain(EKesDiffTerRating.parse(gpxwpt.groundspeak.terrain));
    kes.setDifficulty(EKesDiffTerRating.parse(gpxwpt.groundspeak.difficulty));
    kes.setSize(EKesSize.decode(gpxwpt.groundspeak.container));
    //		kes.set(gpxwpt.groundspeak.);
    //		kes.set(gpxwpt.groundspeak.);
    kes.setStatus(urciStatus(gpxwpt.groundspeak.archived, gpxwpt.groundspeak.availaible));
    kes.setVztahx(urciVztah(gpxwpt));
    kes.setUrl(gpxwpt.link.href);

    kes.setHodnoceni(gpxwpt.gpxg.hodnoceni);
    kes.setHodnoceniPocet(gpxwpt.gpxg.hodnoceniPocet);
    kes.setZnamka(gpxwpt.gpxg.znamka);
    kes.setBestOf(gpxwpt.gpxg.bestOf);
    kes.setFavorit(gpxwpt.gpxg.favorites);
    kes.setFoundTime(gpxwpt.gpxg.found);

    Wpt wpt = createWpt(gpxwpt);
    wpt.setSym(gpxwpt.groundspeak.type);
    wpt.setNazev(gpxwpt.groundspeak.name);  // název hlavního waypointu shodný s názvem keše
    wpt.setZorder(EZOrder.FIRST);

    kes.addWpt(wpt);
    kes.setMainWpt(wpt);

    kes.setUserDefinedAlelas(definujUzivatslskeAlely(gpxwpt));
    return kes;
  }

  private Wpt createAditionalWpt(GpxWpt gpxwpt) {
    Wpt wpt = createWpt(gpxwpt);
    wpt.setSym(gpxwpt.sym == null ? DEFAULT_SYM : gpxwpt.sym);
    boolean rucnePridany = (gpxwpt.gpxg.flag & 1) == 0;
    wpt.setRucnePridany(rucnePridany);
    wpt.setZorder(EZOrder.KESWPT);
    return wpt;
  }


  private EKesVztah urciVztah(GpxWpt gpxwpt) {
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

  protected EKesStatus urciStatus(boolean archived, boolean availaible) {
    if (archived) {
      return EKesStatus.ARCHIVED;
    } else if (!availaible) {
      return EKesStatus.DISABLED;
    } else {
      return EKesStatus.ACTIVE;
    }
  }

  private String vytvorNazev(GpxWpt gpxwpt) {
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

  private String extractOznaceniBodu(String celeJmeno) {
    if (celeJmeno == null) return null;
    if (patExtrakceCislaCgp == null) {
      patExtrakceCislaCgp = Pattern.compile(".*?([0-9]+-[0-9]+).*");
    }
    Matcher mat = patExtrakceCislaCgp.matcher(celeJmeno);
    if (mat.matches()) {
      return mat.group(1);
    } else {
      return null;
    }
  }

  private int urciElevation(GpxWpt gpxwpt) {
    if (gpxwpt.ele != 0)
      return (int) gpxwpt.ele;
    else {
      if (gpxwpt.gpxg != null) {
        return gpxwpt.gpxg.elevation;
      } else {
        return 0;
      }
    }
  }

  private Wpt createWpt(GpxWpt gpxwpt) {
    Wpt wpt = new Wpt();
    wpt.setWgs(gpxwpt.wgs);
    wpt.setElevation(urciElevation(gpxwpt));
    wpt.setName(gpxwpt.name);
    wpt.setNazev(vytvorNazev(gpxwpt));
    return wpt;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.plugins.kesoid.importek.IImportBuilder#getKesBag()
   */
  public KesBag getKesBag() {
    return kesBag;
  }


  private Set<Alela> definujUzivatslskeAlely(GpxWpt gpxwpt) {
    Set<Alela> alely = new HashSet<>();

    for (Map.Entry<String, String> entry : gpxwpt.gpxg.userTags.entrySet()) {
      String alelaName = entry.getValue();
      String genName = entry.getKey();
      Alela alela = genom.alela(alelaName, genName);
      if (alela == null) {
        continue;
      }
      alely.add(alela);
    }

    return alely;
  }

  public synchronized void setCurrentlyLoaded(File aJmenoZdroje, boolean nacteno) {
    infoOCurrentnimZdroji = informaceOZdrojich.add(aJmenoZdroje, nacteno);
  }

  @Override
  public void addTrackWpt(GpxWpt wpt) {
  }

  @Override
  public void begTrackSegment() {
  }

  @Override
  public void endTrackSegment() {
  }

  @Override
  public void begTrack() {
  }

  @Override
  public void endTrack() {
  }

  @Override
  public void setTrackName(String aTrackName) {
  }
}
