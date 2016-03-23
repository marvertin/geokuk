package cz.geokuk.plugins.kesoid.mvc;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;
import cz.geokuk.plugins.kesoid.importek.*;
import cz.geokuk.plugins.kesoid.mapicon.*;
import cz.geokuk.plugins.vylety.EVylet;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.KeFile;

/**
 * @author veverka
 *
 */
public class KesoidModel extends Model0 {

	// Datamodelu
	private KesFilter						filter;
	private Set<String>						jmenaAlelNaToolbaru;
	private Set<String>						jmenaNefenotypovanychAlel;
	private String							jmenoSady					= "neznama-sada";
	private IkonBag							ikonBag;
	private KesBag							vsechny;
	private GccomNick						gccomNick;
	private ASada							jmenoAktualniSadyIkon;
	private KesoidUmisteniSouboru			umisteniSouboru;
	private Set<File>						blokovaneZdroje;

	// injektovanci
	private final MultiNacitacLoaderManager	multiNacitacLoaderManager	= new MultiNacitacLoaderManager(this);
	private final IkonNacitacManager		ikonNacitacLoaderManager	= new IkonNacitacManager(this);
	private KesFilteringSwingWorker			filteringSwingWorker;
	private ProgressModel					progressModel;
	private Boolean							onoff;

	/**
	 * @return the umisteniSouboru
	 */
	public KesoidUmisteniSouboru getUmisteniSouboru() {
		return umisteniSouboru;
	}

	/**
	 * @param umisteniSouboru
	 *            the umisteniSouboru to set
	 */
	public void setUmisteniSouboru(final KesoidUmisteniSouboru umisteniSouboru) {
		if (umisteniSouboru.equals(this.umisteniSouboru))
			return;
		final boolean nacistIkony = this.umisteniSouboru == null || this.umisteniSouboru.getImageMyDir().equals(umisteniSouboru.getImageMyDir())
				|| this.umisteniSouboru.getImage3rdPartyDir().equals(umisteniSouboru.getImage3rdPartyDir());
		final boolean nacistKese = this.umisteniSouboru == null || this.umisteniSouboru.getKesDir().equals(umisteniSouboru.getKesDir());
		this.umisteniSouboru = umisteniSouboru;
		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		pref.putFilex(FPref.KES_DIR_value, umisteniSouboru.getKesDir());
		pref.putFilex(FPref.CESTY_DIR_value, umisteniSouboru.getCestyDir());
		pref.putFilex(FPref.GEOGET_DATA_DIR_value, umisteniSouboru.getGeogetDataDir());
		pref.putFilex(FPref.IMAGE_3RD_PARTY_DIR_value, umisteniSouboru.getImage3rdPartyDir());
		pref.putFilex(FPref.IMAGE_MY_DIR_value, umisteniSouboru.getImageMyDir());
		pref.putFilex(FPref.ANO_GGT_FILE_value, umisteniSouboru.getAnoGgtFile());
		pref.putFilex(FPref.NE_GGT_FILE_value, umisteniSouboru.getNeGgtFile());
		pref.remove("vyjimkyDir"); // mazat ze starych verzi
		blokovaneZdroje = new HashSet<>(currPrefe().node(FPref.KESOID_node).getFileCollection(FPref.BLOKOVANE_ZDROJE_value, new HashSet<File>()));
		fire(new KesoidUmisteniSouboruChangedEvent(umisteniSouboru));
		if (nacistIkony) {
			startIkonLoad(true);
		} else { // když se ančítají ikony, tak se vždy potom čtou keše
			if (nacistKese) {
				startKesLoading();
			}
		}
	}

	public void setJmenaNefenotypovanychAlel(final Set<String> jmenaNefenotypovanychAlel) {
		if (jmenaNefenotypovanychAlel.equals(this.jmenaNefenotypovanychAlel))
			return;
		this.jmenaNefenotypovanychAlel = jmenaNefenotypovanychAlel;
		currPrefe().node(FPref.MAPICON_FENOTYP_node).putStringSet(jmenoSady, jmenaNefenotypovanychAlel);
		fire(new FenotypPreferencesChangedEvent(jmenaNefenotypovanychAlel));
	}

	public KesFilter getFilter() {
		return filter;
	}

	public FilterDefinition getDefinition() {
		return filter.getFilterDefinition().copy();
	}

	public void setDefinition(final FilterDefinition filterDefinition) {
		if (filterDefinition.equals(filter.getFilterDefinition()))
			return;
		filter.setFilterDefinition(filterDefinition);
		currPrefe().putStructure(FPref.KESFILTER_structure_node, filterDefinition);
		currPrefe().node(FPref.KESFILTER_structure_node).putInt("prahVyletu", filterDefinition.getPrahVyletu().ordinal());
		fajruj();
	}

	private void fajruj() {
		fire(new FilterDefinitionChangedEvent(filter.getFilterDefinition(), filter.getJmenaNechtenychAlel(), jmenaAlelNaToolbaru));
		spustFiltrovani();
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

		final Set<String> defval = new HashSet<>();
		defval.add("fnd");
		defval.add("dsbl");
		defval.add("arch");
		filter.setJmenaNechtenychAlel(currPrefe().node(FPref.KESOID_FILTR_node).getStringSet(FPref.KESOID_FILTER_ALELY_value, defval));
		jmenaAlelNaToolbaru = currPrefe().node(FPref.KESOID_FILTR_node).getStringSet(FPref.KESOID_FILTER_NATOOLBARU_value, defval);

		final ASada jmenoAktualniSadyIkon = currPrefe().node(FPref.JMENO_VYBRANE_SADY_IKON_node).getAtom(FPref.JMENO_VYBRANE_SADY_IKON_value, ASada.STANDARD, ASada.class);
		this.jmenoAktualniSadyIkon = jmenoAktualniSadyIkon;
		fire(new JmenoAktualniSadyIkonChangeEvent(jmenoAktualniSadyIkon));
		// fire(new GccomNickChangedEvent(gccomNick));
		// loadUmisteniSouboru();
		setUmisteniSouboru(loadUmisteniSouboru());

		setOnoff(currPrefe().node(FPref.KESOID_node).getBoolean(FPref.KESOID_VISIBLE_value, true));
		fajruj();
	}

	public void inject(final KesFilter filter) {
		this.filter = filter;
	}

	private KesoidUmisteniSouboru loadUmisteniSouboru() {
		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		u.setKesDir(pref.getFilex("kesDir", KesoidUmisteniSouboru.GEOKUK_DATA_DIR));
		u.setCestyDir(pref.getFilex(FPref.CESTY_DIR_value, KesoidUmisteniSouboru.CESTY_DIR));
		u.setGeogetDataDir(pref.getFilex("geogetDataDir", KesoidUmisteniSouboru.GEOGET_DATA_DIR));
		u.setImage3rdPartyDir(pref.getFilex("image3rdPartyDir", KesoidUmisteniSouboru.IMAGE_3RDPARTY_DIR));
		u.setImageMyDir(pref.getFilex("imageMyDir", KesoidUmisteniSouboru.IMAGE_MY_DIR));
		u.setAnoGgtFile(pref.getFilex(FPref.ANO_GGT_FILE_value, KesoidUmisteniSouboru.ANO_GGT));
		u.setNeGgtFile(pref.getFilex(FPref.NE_GGT_FILE_value, KesoidUmisteniSouboru.NE_GGT));
		return u;
	}

	public void spustFiltrovani() {
		if (vsechny == null)
			return;
		if (filteringSwingWorker != null) {
			filteringSwingWorker.cancel(true);
		}
		filteringSwingWorker = new KesFilteringSwingWorker(vsechny, vsechny.getGenom(), filter, this, getProgressModel());
		filteringSwingWorker.execute();
	}

	public void filtrujDleAlely(final String alelaName, final boolean zobrazit) {
		final Set<String> jmena = new HashSet<>(filter.getJmenaNechtenychAlel());
		boolean zmena;
		if (zobrazit) {
			zmena = jmena.remove(alelaName);
		} else {
			zmena = jmena.add(alelaName);
		}
		if (!zmena)
			return; // není změna
		setJmenaNechtenychAlel(jmena);
	}

	public void setJmenaNechtenychAlel(final Set<String> jmenaNechtenychAlel) {
		if (jmenaNechtenychAlel.equals(filter.getJmenaNechtenychAlel())) {
			return;
		}
		filter.setJmenaNechtenychAlel(jmenaNechtenychAlel);
		currPrefe().node(FPref.KESOID_FILTR_node).putStringSet(FPref.KESOID_FILTER_ALELY_value, jmenaNechtenychAlel);
		fajruj();
	}

	public void setJmenaAlelNaToolbaru(final Set<String> jmenaAlelNaToolbaru) {
		if (jmenaAlelNaToolbaru.equals(filter.getJmenaNechtenychAlel()))
			return;
		this.jmenaAlelNaToolbaru = jmenaAlelNaToolbaru;
		currPrefe().node(FPref.KESOID_FILTR_node).putStringSet(FPref.KESOID_FILTER_NATOOLBARU_value, jmenaAlelNaToolbaru);
		fajruj();
	}

	public void onEvent(final IkonyNactenyEvent event) {
		jmenoSady = event.getBag().getSada().getName();
		setJmenaNefenotypovanychAlel(currPrefe().node(FPref.MAPICON_FENOTYP_node).getStringSet(jmenoSady, new HashSet<String>()));
	}

	public void onEvent(final KeskyNactenyEvent aEvent) {
		vycistiBlokovaneZdroje(aEvent.getVsechny().getInformaceOZdrojich());
		startIkonLoad(false);
	}

	private void vycistiBlokovaneZdroje(final InformaceOZdrojich informaceOZdrojich) {
		final boolean zmena = blokovaneZdroje.retainAll(informaceOZdrojich.getJmenaZdroju());
		if (!zmena)
			return;
		currPrefe().node(FPref.KESOID_node).putFileCollection(FPref.BLOKOVANE_ZDROJE_value, blokovaneZdroje);
	}

	public void setVsechnyKesoidy(final KesBag vsechnyKesoidy) {
		vsechny = vsechnyKesoidy;
		spustFiltrovani();
		fire(new KeskyNactenyEvent(vsechnyKesoidy));
	}

	public void setIkonBag(final IkonBag ikonBag) {
		this.ikonBag = ikonBag;
		fire(new IkonyNactenyEvent(ikonBag, getJmenoAktualniSadyIkon()));
		startKesLoading();
	}

	private void startKesLoading() {
		if (ikonBag != null && gccomNick != null) {
			multiNacitacLoaderManager.startLoad(true, ikonBag.getGenom());
		}
	}

	public void startIkonLoad(final boolean prenacti) {
		ikonNacitacLoaderManager.startLoad(prenacti);
	}

	public void setGccomNick(final GccomNick gccomNick) {
		if (gccomNick.equals(this.gccomNick))
			return;
		this.gccomNick = gccomNick;
		currPrefe().node(FPref.NASTAVENI_node).put(FPref.GEOCACHING_COM_NICK_value, gccomNick.name);
		currPrefe().node(FPref.NASTAVENI_node).putInt(FPref.GEOCACHING_COM_NICK_ID_value, gccomNick.id);
		fire(new GccomNickChangedEvent(gccomNick));
		startKesLoading();
	}

	public GccomNick getGccomNick() {
		return gccomNick;
	}

	public void setJmenoAktualniSadyIkon(final ASada jmenoAktualniSadyIkon) {
		if (jmenoAktualniSadyIkon.equals(jmenaAlelNaToolbaru))
			return;
		this.jmenoAktualniSadyIkon = jmenoAktualniSadyIkon;
		currPrefe().node(FPref.JMENO_VYBRANE_SADY_IKON_node).putAtom(FPref.JMENO_VYBRANE_SADY_IKON_value, jmenoAktualniSadyIkon);
		fire(new JmenoAktualniSadyIkonChangeEvent(jmenoAktualniSadyIkon));
		startIkonLoad(true);
	}

	public ASada getJmenoAktualniSadyIkon() {
		return jmenoAktualniSadyIkon;
	}

	public void otevriListingVGeogetu(final Kesoid kes) {
		if (kes == null)
			return;
		final Clipboard scl = getSystemClipboard();
		final StringSelection ss = new StringSelection(kes.getUrlPrint().toExternalForm());
		try {
			scl.setContents(ss, null);
		} catch (final IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Do clipboardu to nejde dáti.");
		}
	}

	public void pridejDoSeznamuVGeogetu(final Kesoid kes) {
		if (kes == null)
			return;
		final Clipboard scl = getSystemClipboard();
		final StringSelection ss = new StringSelection(kes.getUrlShow().toExternalForm());
		try {
			scl.setContents(ss, null);
		} catch (final IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Do clipboardu to nejde dáti.");
		}
	}

	public void pridejKodKesoiduDoClipboardu(final Kesoid kes) {
		if (kes == null)
			return;
		final Clipboard scl = getSystemClipboard();
		final StringSelection ss = new StringSelection(kes.getIdentifier());
		try {
			scl.setContents(ss, null);
		} catch (final IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Do clipboardu to nejde dáti.");
		}
	}

	public void inject(final ProgressModel progressModel) {
		this.progressModel = progressModel;
	}

	public ProgressModel getProgressModel() {
		return progressModel;
	}

	public void setPrekrocenLimitWaypointuVeVyrezu(final boolean prekrocenLimit) {
		fire(new PrekrocenLimitWaypointuVeVyrezuEvent(prekrocenLimit));
	}

	public boolean maSeNacist(final KeFile jmenoZdroje) {
		return !blokovaneZdroje.contains(jmenoZdroje.getFile());
	}

	public void setNacitatSoubor(final KeFile jmenoZDroje, final boolean nacitat) {
		// TODO : speed up
		boolean zmena;
		final Collection<File> changedFiles = Collections2.transform(vsechny.getInformaceOZdrojich().getSubtree(jmenoZDroje), new Function<InformaceOZdroji, File>() {
			@Override
			public File apply(final InformaceOZdroji informaceOZdroji) {
				return informaceOZdroji.jmenoZdroje.getFile();
			}
		});
		if (nacitat) {
			System.out.println("++++XNASTAVENI " + changedFiles);
			zmena = blokovaneZdroje.removeAll(changedFiles);
		} else {
			System.out.println("----XNASTAVENI " + changedFiles);
			zmena = blokovaneZdroje.addAll(changedFiles);
		}
		if (!zmena)
			return;
		currPrefe().node(FPref.KESOID_node).putFileCollection(FPref.BLOKOVANE_ZDROJE_value, blokovaneZdroje);
		startKesLoading();
	}

	public void setOnoff(final boolean onoff) {
		if (this.onoff != null && this.onoff == onoff)
			return;
		this.onoff = onoff;
		currPrefe().node(FPref.KESOID_node).putBoolean(FPref.KESOID_VISIBLE_value, onoff);
		fire(new KesoidOnoffEvent(onoff));
	}

}
