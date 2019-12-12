package cz.geokuk.plugins.kesoid.mvc;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Collections2;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;
import cz.geokuk.plugins.kesoid.genetika.QualAlelaNames;
import cz.geokuk.plugins.kesoid.importek.InformaceOZdrojich;
import cz.geokuk.plugins.kesoid.importek.MultiNacitacLoaderManager;
import cz.geokuk.plugins.kesoid.mapicon.*;
import cz.geokuk.plugins.vylety.EVylet;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.KeFile;

/**
 * @author Martin Veverka
 *
 */
public class KesoidModel extends Model0 {

	// Datamodelu
	private KesFilter filter;
	private QualAlelaNames jmenaAlelNaToolbaru;
	private QualAlelaNames jmenaNefenotypovanychAlel;
	private String jmenoSady = "neznama-sada";
	private IkonBag ikonBag;
	private KesBag vsechny;
	private GccomNick gccomNick;
	private ASada jmenoAktualniSadyIkon;
	private KesoidUmisteniSouboru umisteniSouboru;
	private Set<File> blokovaneZdroje;
	private GsakParametryNacitani gsakParametryNacitani;

	// injektovanci
	private final MultiNacitacLoaderManager multiNacitacLoaderManager = new MultiNacitacLoaderManager(this);
	private final IkonNacitacManager ikonNacitacLoaderManager = new IkonNacitacManager(this);
	private KesFilteringSwingWorker filteringSwingWorker;
	private ProgressModel progressModel;
	private Boolean onoff;

	public void filtrujDleAlely(final String alelaName, final boolean zobrazit) {
		final Set<String> jmena = new HashSet<>(filter.getJmenaNechtenychAlel().getQualNames());
		boolean zmena;
		if (zobrazit) {
			zmena = jmena.remove(alelaName);
		} else {
			zmena = jmena.add(alelaName);
		}
		if (!zmena) {
			return; // není změna
		}
		setJmenaNechtenychAlel(new QualAlelaNames(jmena));
	}

	public FilterDefinition getDefinition() {
		return filter.getFilterDefinition().copy();
	}

	public KesFilter getFilter() {
		return filter;
	}

	public GccomNick getGccomNick() {
		return gccomNick;
	}

	public ASada getJmenoAktualniSadyIkon() {
		return jmenoAktualniSadyIkon;
	}

	public ProgressModel getProgressModel() {
		return progressModel;
	}

	/**
	 * @return the umisteniSouboru
	 */
	public KesoidUmisteniSouboru getUmisteniSouboru() {
		return umisteniSouboru;
	}

	public GsakParametryNacitani getGsakParametryNacitani() {
		return gsakParametryNacitani;
	}

	public void inject(final KesFilter filter) {
		this.filter = filter;
	}

	public void inject(final ProgressModel progressModel) {
		this.progressModel = progressModel;
	}

	public boolean maSeNacist(final KeFile jmenoZdroje) {
		return !blokovaneZdroje.contains(jmenoZdroje.getFile());
	}

	public void onEvent(final IkonyNactenyEvent event) {
		jmenoSady = event.getBag().getSada().getName();
		setJmenaNefenotypovanychAlel(currPrefe().node(FPref.MAPICON_FENOTYP_node).getQualAlelaNames(jmenoSady, QualAlelaNames.EMPTY));
	}

	public void onEvent(final KeskyNactenyEvent aEvent) {
		vycistiBlokovaneZdroje(aEvent.getVsechny().getInformaceOZdrojich());
		startIkonLoad(false);
	}

	public void otevriListingVGeogetu(final Kesoid kes) {
		if (kes == null) {
			return;
		}
		final Clipboard scl = getSystemClipboard();
		final StringSelection ss = new StringSelection(kes.getUrlPrint().toExternalForm());
		try {
			scl.setContents(ss, null);
		} catch (final IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Do clipboardu to nejde dáti.");
		}
	}

	public void pridejDoSeznamuVGeogetu(final Kesoid kes) {
		if (kes == null) {
			return;
		}
		final Clipboard scl = getSystemClipboard();
		final StringSelection ss = new StringSelection(kes.getUrlShow().toExternalForm());
		try {
			scl.setContents(ss, null);
		} catch (final IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Do clipboardu to nejde dáti.");
		}
	}

	public void pridejKodKesoiduDoClipboardu(final Kesoid kes) {
		if (kes == null) {
			return;
		}
		final Clipboard scl = getSystemClipboard();
		final StringSelection ss = new StringSelection(kes.getIdentifier());
		try {
			scl.setContents(ss, null);
		} catch (final IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Do clipboardu to nejde dáti.");
		}
	}

	public void setDefinition(final FilterDefinition filterDefinition) {
		if (filterDefinition.equals(filter.getFilterDefinition())) {
			return;
		}
		filter.setFilterDefinition(filterDefinition);
		currPrefe().putStructure(FPref.KESFILTER_structure_node, filterDefinition);
		currPrefe().node(FPref.KESFILTER_structure_node).putInt("prahVyletu", filterDefinition.getPrahVyletu().ordinal());
		fajruj();
	}

	public void setGccomNick(final GccomNick gccomNick) {
		if (gccomNick.equals(this.gccomNick)) {
			return;
		}
		this.gccomNick = gccomNick;
		currPrefe().node(FPref.NASTAVENI_node).put(FPref.GEOCACHING_COM_NICK_value, gccomNick.name);
		currPrefe().node(FPref.NASTAVENI_node).putInt(FPref.GEOCACHING_COM_NICK_ID_value, gccomNick.id);
		fire(new GccomNickChangedEvent(gccomNick));
		startKesLoading();
	}

	public void setIkonBag(final IkonBag ikonBag) {
		this.ikonBag = ikonBag;
		fire(new IkonyNactenyEvent(ikonBag, getJmenoAktualniSadyIkon()));
		startKesLoading();
	}

	public void setJmenaAlelNaToolbaru(final QualAlelaNames jmenaAlelNaToolbaru) {
		if (jmenaAlelNaToolbaru.equals(filter.getJmenaNechtenychAlel())) {
			return;
		}
		this.jmenaAlelNaToolbaru = jmenaAlelNaToolbaru;
		currPrefe().node(FPref.KESOID_FILTR_node).putQualAlelaNames(FPref.KESOID_FILTER_NATOOLBARU_value, jmenaAlelNaToolbaru);
		fajruj();
	}

	public void setJmenaNefenotypovanychAlel(final QualAlelaNames jmenaNefenotypovanychAlel) {
		if (jmenaNefenotypovanychAlel.equals(this.jmenaNefenotypovanychAlel)) {
			return;
		}
		this.jmenaNefenotypovanychAlel = jmenaNefenotypovanychAlel;
		currPrefe().node(FPref.MAPICON_FENOTYP_node).putQualAlelaNames(jmenoSady, jmenaNefenotypovanychAlel);
		fire(new FenotypPreferencesChangedEvent(jmenaNefenotypovanychAlel));
	}

	public void setJmenaNechtenychAlel(final QualAlelaNames jmenaNechtenychAlel) {
		if (jmenaNechtenychAlel.equals(filter.getJmenaNechtenychAlel())) {
			return;
		}
		filter.setJmenaNechtenychAlel(jmenaNechtenychAlel);
		currPrefe().node(FPref.KESOID_FILTR_node).putQualAlelaNames(FPref.KESOID_FILTER_ALELY_value, jmenaNechtenychAlel);
		fajruj();
	}

	public void setJmenoAktualniSadyIkon(final ASada jmenoAktualniSadyIkon) {
		if (jmenoAktualniSadyIkon.equals(jmenaAlelNaToolbaru)) {
			return;
		}
		this.jmenoAktualniSadyIkon = jmenoAktualniSadyIkon;
		currPrefe().node(FPref.JMENO_VYBRANE_SADY_IKON_node).putAtom(FPref.JMENO_VYBRANE_SADY_IKON_value, jmenoAktualniSadyIkon);
		fire(new JmenoAktualniSadyIkonChangeEvent(jmenoAktualniSadyIkon));
		startIkonLoad(true);
	}

	public void setNacitatSoubor(final KeFile jmenoZdroje, final boolean nacitat) {
		// TODO : speed up
		final Collection<File> changedFiles = Collections2.transform(vsechny.getInformaceOZdrojich().getSubtree(jmenoZdroje), informaceOZdroji -> informaceOZdroji.jmenoZdroje.getFile());
		System.out.println((nacitat ? "++++" : "----") + "XNASTAVENI " + changedFiles);
		final boolean zmena = nacitat ? blokovaneZdroje.removeAll(changedFiles) : blokovaneZdroje.addAll(changedFiles);
		if (zmena) {
			currPrefe().node(FPref.KESOID_node).putFileCollection(FPref.BLOKOVANE_ZDROJE_value, blokovaneZdroje);
			startKesLoading();
		}
	}

	public void setOnoff(final boolean onoff) {
		if (this.onoff != null && this.onoff == onoff) {
			return;
		}
		this.onoff = onoff;
		currPrefe().node(FPref.KESOID_node).putBoolean(FPref.KESOID_VISIBLE_value, onoff);
		fire(new KesoidOnoffEvent(onoff));
	}

	public void setPrekrocenLimitWaypointuVeVyrezu(final boolean prekrocenLimit) {
		fire(new PrekrocenLimitWaypointuVeVyrezuEvent(prekrocenLimit));
	}

	public void setGsakParametryNacitani(final GsakParametryNacitani aGsakParametryNacitani) {
		gsakParametryNacitani = aGsakParametryNacitani;
		final MyPreferences pref = currPrefe().node(FPref.GSAK_node);
		pref.putStringSet(FPref.GSAK_CAS_NALEZU_value, aGsakParametryNacitani.getCasNalezu());
		pref.putStringSet(FPref.GSAK_CAS_NENALEZU_value, aGsakParametryNacitani.getCasNenalezu());
		pref.putBoolean(FPref.GSAK_NACITAT_VSECHNO, aGsakParametryNacitani.isNacistVsechnyDatabaze());
		fire(new GsakParametryNacitaniChangedEvent(gsakParametryNacitani));
	}

	/**
	 * @param aUmisteniSouboru
	 *            the umisteniSouboru to set
	 */
	public void setUmisteniSouboru(final KesoidUmisteniSouboru aUmisteniSouboru) {
		if (aUmisteniSouboru.equals(this.umisteniSouboru)) {
			return;
		}
		final boolean nacistIkony = !aUmisteniSouboru.equalsImageLocations(umisteniSouboru);
		final boolean nacistKese = !aUmisteniSouboru.equalsDataLocations(umisteniSouboru);
		final Set<File> zakázat = gsakSouboryKZakazání(umisteniSouboru, aUmisteniSouboru);
		umisteniSouboru = aUmisteniSouboru;

		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		pref.putFilex(FPref.KES_DIR_value, aUmisteniSouboru.getKesDir());
		pref.putFilex(FPref.CESTY_DIR_value, aUmisteniSouboru.getCestyDir());
		pref.putFilex(FPref.GEOGET_DATA_DIR_value, aUmisteniSouboru.getGeogetDataDir());
		pref.putFilex(FPref.GSAK_DATA_DIR_value, aUmisteniSouboru.getGsakDataDir());
		pref.putFilex(FPref.IMAGE_3RD_PARTY_DIR_value, aUmisteniSouboru.getImage3rdPartyDir());
		pref.putFilex(FPref.IMAGE_MY_DIR_value, aUmisteniSouboru.getImageMyDir());
		pref.putFilex(FPref.ANO_GGT_FILE_value, aUmisteniSouboru.getAnoGgtFile());
		pref.putFilex(FPref.NE_GGT_FILE_value, aUmisteniSouboru.getNeGgtFile());
		pref.remove("vyjimkyDir"); // mazat ze starých verzí
		blokovaneZdroje = new HashSet<>(currPrefe().node(FPref.KESOID_node).getFileCollection(FPref.BLOKOVANE_ZDROJE_value, new HashSet<File>()));
		blokovaneZdroje.addAll(zakázat);
		fire(new KesoidUmisteniSouboruChangedEvent(aUmisteniSouboru));
		if (nacistIkony) {
			startIkonLoad(true);
		} else { // když se načítají ikony, tak se vždy potom čtou keše
			if (nacistKese) {
				startKesLoading();
			}
		}
	}

	private Set<File> gsakSouboryKZakazání(final KesoidUmisteniSouboru aAktuální, final KesoidUmisteniSouboru aNové) {
		if (gsakParametryNacitani.isNacistVsechnyDatabaze() || /* Tohle se stává jen při startu programu. */ aAktuální == null) {
			return Collections.emptySet();
		}
		final Set<File> nové = multiNacitacLoaderManager.gsakSoubory(aNové.getGsakDataDir()).stream().map(f -> f.getFile()).collect(Collectors.toSet());
		final Set<File> stávající = multiNacitacLoaderManager.gsakSoubory(aAktuální.getGsakDataDir()).stream().map(f -> f.getFile()).collect(Collectors.toSet());
		// Pokud totiž některé stávající v seznamu blokovaných nejsou, tak je někdo předtím povolil. A tak je nebudeme znovu zakazovat, že.
		nové.removeAll(stávající);
		return nové;
	}

	public void setVsechnyKesoidy(final KesBag vsechnyKesoidy) {
		vsechny = vsechnyKesoidy;
		spustFiltrovani();
		fire(new KeskyNactenyEvent(vsechnyKesoidy));
	}

	public void spustFiltrovani() {
		if (vsechny == null) {
			return;
		}
		if (filteringSwingWorker != null) {
			filteringSwingWorker.cancel(true);
		}
		filteringSwingWorker = new KesFilteringSwingWorker(vsechny, vsechny.getGenom(), filter, this, getProgressModel());
		filteringSwingWorker.execute();
	}

	public void startIkonLoad(final boolean prenacti) {
		ikonNacitacLoaderManager.startLoad(prenacti);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
		final String gccomNickName = currPrefe().node(FPref.NASTAVENI_node).get(FPref.GEOCACHING_COM_NICK_value, "sem napis svuj nick na GC.COM");
		final int gccomNickId = currPrefe().node(FPref.NASTAVENI_node).getInt(FPref.GEOCACHING_COM_NICK_ID_value, -1);

		setGccomNick(new GccomNick(gccomNickName, gccomNickId));
		final FilterDefinition filterDefinition = currPrefe().getStructure(FPref.KESFILTER_structure_node, new FilterDefinition());
		final int prahVyletuOrdinal = currPrefe().node(FPref.KESFILTER_structure_node).getInt("prahVyletu", filterDefinition.getPrahVyletu().ordinal());
		for (final EVylet vylet : EVylet.values()) {
			if (prahVyletuOrdinal == vylet.ordinal()) {
				filterDefinition.setPrahVyletu(vylet);
			}
		}
		filter.setFilterDefinition(filterDefinition);

		// FIXME tady nemohou být takovéto konstant, mohou se změnit
		final QualAlelaNames defval = new QualAlelaNames("fnd:vztah", "dsbl:stav", "arch:stav");

		filter.setJmenaNechtenychAlel(currPrefe().node(FPref.KESOID_FILTR_node).getQualAlelaNames(FPref.KESOID_FILTER_ALELY_value, defval));
		jmenaAlelNaToolbaru = currPrefe().node(FPref.KESOID_FILTR_node).getQualAlelaNames(FPref.KESOID_FILTER_NATOOLBARU_value, defval);

		final ASada jmenoAktualniSadyIkon = currPrefe().node(FPref.JMENO_VYBRANE_SADY_IKON_node).getAtom(FPref.JMENO_VYBRANE_SADY_IKON_value, ASada.STANDARD, ASada.class);
		this.jmenoAktualniSadyIkon = jmenoAktualniSadyIkon;
		fire(new JmenoAktualniSadyIkonChangeEvent(jmenoAktualniSadyIkon));
		// fire(new GccomNickChangedEvent(gccomNick));
		// loadUmisteniSouboru();
		setGsakParametryNacitani(loadGsakParametryNacitani());
		setUmisteniSouboru(loadUmisteniSouboru());

		setOnoff(currPrefe().node(FPref.KESOID_node).getBoolean(FPref.KESOID_VISIBLE_value, true));
		fajruj();
	}

	private void fajruj() {
		fire(new FilterDefinitionChangedEvent(filter.getFilterDefinition(), filter.getJmenaNechtenychAlel(), jmenaAlelNaToolbaru));
		spustFiltrovani();
	}

	private KesoidUmisteniSouboru loadUmisteniSouboru() {
		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		u.setKesDir(pref.getFilex("kesDir", KesoidUmisteniSouboru.GEOKUK_DATA_DIR));
		u.setCestyDir(pref.getFilex(FPref.CESTY_DIR_value, KesoidUmisteniSouboru.CESTY_DIR));
		u.setGeogetDataDir(pref.getFilex("geogetDataDir", KesoidUmisteniSouboru.GEOGET_DATA_DIR));
		u.setGsakDataDir(pref.getFilex("gsakDataDir", KesoidUmisteniSouboru.GSAK_DATA_DIR));
		u.setImage3rdPartyDir(pref.getFilex("image3rdPartyDir", KesoidUmisteniSouboru.IMAGE_3RDPARTY_DIR));
		u.setImageMyDir(pref.getFilex("imageMyDir", KesoidUmisteniSouboru.IMAGE_MY_DIR));
		u.setAnoGgtFile(pref.getFilex(FPref.ANO_GGT_FILE_value, KesoidUmisteniSouboru.ANO_GGT));
		u.setNeGgtFile(pref.getFilex(FPref.NE_GGT_FILE_value, KesoidUmisteniSouboru.NE_GGT));
		return u;
	}

	private GsakParametryNacitani loadGsakParametryNacitani() {
		final GsakParametryNacitani g = new GsakParametryNacitani();
		final MyPreferences pref = currPrefe().node(FPref.GSAK_node);
		g.setCasNalezu(pref.getStringList(FPref.GSAK_CAS_NALEZU_value, Arrays.asList("UserData")));
		g.setCasNenalezu(pref.getStringList(FPref.GSAK_CAS_NENALEZU_value, Arrays.asList("UserData")));
		g.setNacistVsechnyDatabaze(pref.getBoolean(FPref.GSAK_NACITAT_VSECHNO, true));
		return g;
	}

	private void startKesLoading() {
		if (ikonBag != null && gccomNick != null) {
			multiNacitacLoaderManager.startLoad(true, ikonBag.getGenom());
		}
	}

	private void vycistiBlokovaneZdroje(final InformaceOZdrojich informaceOZdrojich) {
		if (blokovaneZdroje.retainAll(informaceOZdrojich.getJmenaZdroju())) {
			currPrefe().node(FPref.KESOID_node).putFileCollection(FPref.BLOKOVANE_ZDROJE_value, blokovaneZdroje);
		}
	}

}
