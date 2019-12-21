package cz.geokuk.plugins.kesoid.kind.cgp;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.kind.*;
import cz.geokuk.plugins.kesoid.kind.kes.EKesType;
import cz.geokuk.util.procak.EProcakResult;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CgpGpxWptProcak implements GpxWptProcak {

	private static Pattern patExtrakceCislaCgp;
	private static Pattern patExtrakceSouradnicJtsk;

	private static final String GC = "GC";
	private static final String WM = "WM";
	private static final String GEOCACHE = "Geocache";
	private static final String GEOCACHE_FOUND = "Geocache Found";
	private static final String WAYMARK = "Waymark";


	private final GpxToWptContext ctx;
	private final GpxToWptBuilder builder;

	final Map<String, CzechGeodeticPoint> mapaPredTeckou = new HashMap<>();

	private boolean druheKolo;

	@Override
	public EProcakResult process(final GpxWpt gpxwpt) {
		if (isCzechGeodeticPoint(gpxwpt)) {
			final EKesType kesType = decodePseudoKesType(gpxwpt);
			switch (kesType) {
			case TRADITIONAL:
			case MULTI:
			case EARTHCACHE:
			case WHERIGO:
			{
				final CzechGeodeticPoint cgp = createCgp(gpxwpt);
				mapaPredTeckou.put(extrahujPrefixPredTeckou(gpxwpt), cgp);
				ctx.expose(cgp.getMainWpt());
				return EProcakResult.DONE;
			}
			case EVENT:
			case CACHE_IN_TRASH_OUT_EVENT:
			case MEGA_EVENT:
			{
				ctx.expose(createCgp(gpxwpt).getMainWpt());
				return EProcakResult.DONE;
			}
			case LETTERBOX_HYBRID:
			case UNKNOWN:
				if (druheKolo) {
					final CzechGeodeticPoint cgp2 = mapaPredTeckou.get(extrahujPrefixPredTeckou(gpxwpt));
					if (cgp2 != null) {
						final Wpt wpt = pridruz(cgp2, gpxwpt);
						ctx.expose(wpt);
					} else {
						ctx.expose(createCgp(gpxwpt).getMainWpt());
					}
					return EProcakResult.DONE;
				} else {
					return EProcakResult.PROBABLY_MY_BUT_NEXT_ROUND;
				}
			default:
				return EProcakResult.NEVER;
			}
		} else if (isWaymark(gpxwpt)) {
			final Wmdata wm = createWaymark(gpxwpt);
			// FIXME [veverka] V předchnozí verzi to nebylo null -- 19. 12. 2019 17:56:26 veverka
			if (wm.getSym() == null || !wm.getSym().equals("Czech Geodetic Points")) {
				return EProcakResult.NEVER; // není to ani ta správná kategorie, nikdy nás to zajímat nebude
			}
			final String oznaceniBodu = extractOznaceniBodu(wm.getNazev());
			if (oznaceniBodu == null) {
				return EProcakResult.NEVER; // tak ve jméně není označení, zobrazíme jako normální waymark
			}
			final CzechGeodeticPoint cgp = mapaPredTeckou.get(oznaceniBodu);
			if (cgp == null) {
				if (druheKolo) {
					return EProcakResult.NEVER; // nenašli jsme, není to kam připojit, je to neexistující CGP, tak to bude normálně asi waymark
				} else {
					return EProcakResult.PROBABLY_MY_BUT_NEXT_ROUND; // neumíme zatím připojit
				}
			}
			prepisInformaceVCgpZWaymarku(wm, cgp);
			return EProcakResult.DONE;

		} else {
			return EProcakResult.NEVER;
		}

	}

	@Override
	public void roundDone() {
		// V druhém kole řešíme přidužeňáky a waymarky.
		druheKolo = true;
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

		final Wpt wpt = builder.createWpt(gpxwpt, CgpPlugin.CGP);
		wpt.setName(cisloBodu);
		urciNazevCgpZPseudoKese(cgp, wpt, gpxwpt);
		wpt.setSym(urciSymCgpZPseudoKese(gpxwpt));

		cgp.addWpt(wpt);
		cgp.setUserDefinedAlelas(ctx.definujUzivatslskeAlely(gpxwpt));

		return cgp;
	}

	/**
	 * @param aCgp
	 * @param aGpxwpt
	 */
	private Wpt pridruz(final CzechGeodeticPoint cgp, final GpxWpt gpxwpt) {
		final Wpt wpt = builder.createWpt(gpxwpt, CgpPlugin.CGP);
		wpt.setName(gpxwpt.groundspeak.name);
		urciNazevCgpZPseudoKese(cgp, wpt, gpxwpt);
		wpt.setSym(urciSymCgpZPseudoKese(gpxwpt));
		cgp.addWpt(wpt);
		return wpt;
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

	private EKesType decodePseudoKesType(final GpxWpt gpxwpt) {
		return gpxwpt.groundspeak.type != null ? EKesType.decode(gpxwpt.groundspeak.type) : EKesType.decode(gpxwpt.type.substring(9));
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


	private boolean prepisInformaceVCgpZWaymarku(final Wmdata wm, final CzechGeodeticPoint cgp) {
		// tak a tady musíme do cgp narvat vše, co víme
		cgp.setVztahx(wm.getVztahx());
		cgp.getFirstWpt().setNazev(wm.getNazev());
		cgp.setUrl(wm.getUrl());
		cgp.setAuthor(wm.getAuthor());
		cgp.setHidden(wm.getHidden());
		return true;
	}

	private boolean isCzechGeodeticPoint(final GpxWpt gpxWpt) {
		// TODO: Je špatné rozpoznávat geokeše podle prefixu GC - jsou systémy (Geocaching.su, OpenCaching, ...) které tento prefix nemají a přitom to jsou keše! [2016-04-09, Bohusz]
		return gpxWpt.groundspeak != null && (gpxWpt.name.startsWith(GC) && gpxWpt.name.length() == 8 || gpxWpt.name.matches("^(TrB_|ZhB_|BTP_|ZGS_).*$") || "DATAZ".equals(gpxWpt.groundspeak.owner));
	}


	private boolean isWaymarkNormal(final GpxWpt gpxWpt) {
		//wpt.setNazev(gpxwpt.link.text);
		return gpxWpt.name.startsWith(WM) && WAYMARK.equals(gpxWpt.sym);
	}

	private boolean isWaymarkGeoget(final GpxWpt gpxWpt) {
		//wpt.setNazev(gpxwpt.groundspeak.name);
		return gpxWpt.name.startsWith(WM) && (GEOCACHE.equals(gpxWpt.sym) || GEOCACHE_FOUND.equals(gpxWpt.sym));
	}

	private boolean isWaymark(final GpxWpt gpxWpt) {
		return isWaymarkNormal(gpxWpt) ||isWaymarkGeoget(gpxWpt);
	}

	private Wmdata createWaymark(final GpxWpt gpxwpt) {
		if (isWaymarkNormal(gpxwpt)) {
			return createWaymarkNormal(gpxwpt);
		} else if (isWaymarkGeoget(gpxwpt)) {
			return createWaymarkGeoget(gpxwpt);
		} else {
			throw new IllegalArgumentException("To už musíš vědět, že je to waymar, když ho tvoříšk");
		}
	}
	private Wmdata createWaymarkGeoget(final GpxWpt gpxwpt) {
		final Wmdata wm = new Wmdata();
		if (ctx.getGccomNick().name.equals(gpxwpt.groundspeak.placedBy)) {
			wm.setVztahx(EKesVztah.OWN);
		} else {
			wm.setVztahx(EKesVztah.NORMAL);
		}
		wm.setUrl(gpxwpt.link.href);
		wm.setAuthor(gpxwpt.groundspeak.placedBy);
		wm.setHidden(gpxwpt.time);

		wm.setNazev(gpxwpt.groundspeak.name);
		wm.setSym(odstranNadbytecneMezery(gpxwpt.groundspeak.type));

		return wm;
	}

	private Wmdata createWaymarkNormal(final GpxWpt gpxwpt) {
		final Wmdata wm = new Wmdata();
		if (gpxwpt.groundspeak != null) {
			if (ctx.getGccomNick().name.equals(gpxwpt.groundspeak.placedBy)) {
				wm.setVztahx(EKesVztah.OWN);
			} else {
				wm.setVztahx(EKesVztah.NORMAL);
			}
			wm.setAuthor(gpxwpt.groundspeak.placedBy);
		} else {
			wm.setVztahx(EKesVztah.NORMAL);
		}
		wm.setUrl(gpxwpt.link.href);

		wm.setNazev(gpxwpt.link.text);
		wm.setSym(odstranNadbytecneMezery(gpxwpt.type));

		return wm;
	}

	@Data
	private static class Wmdata {
		String sym;
		String url;
		String hidden;
		String author;
		String nazev;
		EKesVztah vztahx;
	}
	/**
	 * @param aType
	 * @return
	 */
	private String odstranNadbytecneMezery(final String s) {
		return s == null ? null : s.replaceAll(" +", " ");
	}

	@Override
	public void allDone() {
		// TODO Auto-generated method stub

	}



}
