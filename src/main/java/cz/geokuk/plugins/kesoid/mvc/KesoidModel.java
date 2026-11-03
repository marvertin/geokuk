package cz.geokuk.plugins.kesoid.mvc;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import com.google.common.collect.Collections2;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.filtr.FilterDefinitionChangedEvent;
import cz.geokuk.plugins.kesoid.genetika.QualAlelaNames;
import cz.geokuk.plugins.kesoid.importek.InformaceOZdrojich;
import cz.geokuk.plugins.kesoid.importek.RegistrStavuZdroju;
import cz.geokuk.plugins.kesoid.importek.StavPolozky;
import cz.geokuk.plugins.kesoid.importek.StavZdroju;
import cz.geokuk.plugins.kesoid.importek.TypZdroje;
import cz.geokuk.plugins.kesoid.importek.MultiNacitac;
import cz.geokuk.plugins.kesoid.importek.MultiNacitacLoaderManager;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mapicon.*;
import cz.geokuk.plugins.vylety.EVylet;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.Filex;
import cz.geokuk.util.file.KeFile;
import lombok.Getter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Martin Veverka
 *
 */
@Slf4j
public class KesoidModel extends Model0 implements OvladaniZdroju {

	// FIXME tady nemohou být takovéto konstant, mohou se změnit
	private static final QualAlelaNames VYCHOZI_NECHTENE_ALELY = new QualAlelaNames("fnd:vztah", "dsbl:stav", "arch:stav");

	// Datamodelu
	private KesoidFilterModel filter;
	private QualAlelaNames jmenaAlelNaToolbaru;
	private QualAlelaNames jmenaNefenotypovanychAlel;
	private String jmenoSady = "neznama-sada";
	private IkonBag ikonBag;
	private KesBag vsechny;
	private GccomNick gccomNick;
	private ASada jmenoAktualniSadyIkon;
	private KesoidUmisteniSouboru umisteniSouboru;
	/** Mění se z EDT i z vlákna načítání: jen celou novou kopií v upravBlokovaneZdroje. */
	private volatile Set<File> blokovaneZdroje = Collections.emptySet();
	/** Vypnuté celé typy zdrojů; položky si zachovávají vlastní volbu v blokovaných zdrojích. */
	private volatile Set<TypZdroje> vypnuteTypy = Collections.emptySet();
	private volatile Set<String> casyDatZdroju = Collections.emptySet();
	private boolean casyDatZNastaveni;
	/** Složky, které se při posledním prohledání nepodařilo přečíst. */
	private volatile Set<File> nedostupnePriNacitani = Collections.emptySet();
	private GsakParametryNacitani gsakParametryNacitani;

	// injektovanci
	private final MultiNacitacLoaderManager multiNacitacLoaderManager = new MultiNacitacLoaderManager(this);
	private final IkonNacitacManager ikonNacitacLoaderManager = new IkonNacitacManager(this);
	private KesFilteringSwingWorker filteringSwingWorker;
	private ProgressModel progressModel;
	private Boolean onoff;
	private boolean sledovatSchranku;
	private EZobrazeniKesi zobrazeniKesi;
	private int zvetseniIkonProcent = ZvetseniIkonEvent.VYCHOZI;
	private volatile List<String> zamceneDatabaze = Collections.emptyList();
	/** Zdroje rozběhnutého prvního načítání, dokud žádné keše načtené nejsou. */
	private volatile InformaceOZdrojich nacitaneZdroje;
	private LimityKresleni limityKresleni = LimityKresleni.VYCHOZI;
	/** Čte ho načítání ikon na pozadí. */
	private volatile Zvyrazneni zvyrazneni = Zvyrazneni.VYCHOZI;
	private List<Zvyrazneni> predvolbyZvyrazneni = Collections.emptyList();
	private final AtomicBoolean udalostStavuZdrojuCeka = new AtomicBoolean();
	private volatile StavZdroju stavZdroju = StavZdroju.PRAZDNY;

	{
		multiNacitacLoaderManager.getRegistr().setPosluchac(this::naplanujUdalostStavuZdroju);
	}

	@Getter
	private KesoidPluginManager kesopidPluginManager;

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

	public KesoidFilterModel getFilter() {
		return filter;
	}

	/** Všechny načtené kešoidy, nebo null, dokud se nenačetly. */
	public KesBag getVsechnyKesoidy() {
		return vsechny;
	}

	public void prenactiKese() {
		startKesLoading();
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

	/** Umístění souborů; dokud je model nenačetl, přímo z nastavení. */
	public KesoidUmisteniSouboru getUmisteniSouboruNeboZNastaveni() {
		final KesoidUmisteniSouboru u = umisteniSouboru;
		return u != null ? u : loadUmisteniSouboru();
	}

	public GsakParametryNacitani getGsakParametryNacitani() {
		return gsakParametryNacitani;
	}

	public void inject(final KesoidFilterModel filter) {
		this.filter = filter;
	}

	public void inject(final ProgressModel progressModel) {
		this.progressModel = progressModel;
	}

	public void inject(final KesoidPluginManager kesopidPluginManager) {
		this.kesopidPluginManager = kesopidPluginManager;
	}

	/**
	 * Databáze GSAKu z aktuálního prohledání. Při volbě „Nové databáze načíst až po vybrání“ se databáze, kterou GeoKuk při minulém prohledání neviděl, nenačte, dokud ji uživatel nevybere.
	 */
	public void zaradGsakDatabaze(final Set<File> databaze) {
		zaradGsakDatabaze(databaze, Collections.emptySet());
	}

	/** Známé databáze v nedostupných složkách zůstanou známé. */
	public void zaradGsakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {
		zaradDatabaze(FPref.ZNAME_GSAK_DATABAZE_value, () -> getGsakParametryNacitani().isNoveDatabazeHned(), databaze, nedostupne);
	}

	/** Databáze OpenSAKu z aktuálního prohledání, „Načítat až po vybrání“ stejně jako u GSAKu. */
	public void zaradOpensakDatabaze(final Set<File> databaze) {
		zaradOpensakDatabaze(databaze, Collections.emptySet());
	}

	public void zaradOpensakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {
		zaradDatabaze(FPref.ZNAME_OPENSAK_DATABAZE_value, () -> getGsakParametryNacitani().isNoveDatabazeOpensakuHned(), databaze, nedostupne);
	}

	/** Databáze GeoGetu (soubory .db3) z aktuálního prohledání, „Načítat až po vybrání“ stejně jako u GSAKu. */
	public void zaradGeogetDatabaze(final Set<File> databaze) {
		zaradGeogetDatabaze(databaze, Collections.emptySet());
	}

	public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {
		zaradDatabaze(FPref.ZNAME_GEOGET_DATABAZE_value, () -> getGsakParametryNacitani().isNoveDatabazeGeogetuHned(), databaze, nedostupne);
	}

	private synchronized void zaradDatabaze(final String klicZnamych, final java.util.function.BooleanSupplier nacistVsechny, final Set<File> databaze, final Set<File> nedostupne) {
		final MyPreferences pref = currPrefe().node(FPref.KESOID_node);
		final Collection<File> zname = pref.getFileCollection(klicZnamych, null);
		if (zname != null) {
			final Set<File> nove = new HashSet<>(databaze);
			nove.removeAll(zname);
			if (!nove.isEmpty() && !nacistVsechny.getAsBoolean()) {
				upravBlokovaneZdroje(b -> b.addAll(nove));
			}
		}
		final Set<File> noveZname = new HashSet<>(databaze);
		if (zname != null) {
			for (final File f : zname) {
				if (jePod(f, nedostupne)) {
					noveZname.add(f);
				}
			}
		}
		if (zname == null || !noveZname.equals(new HashSet<>(zname))) {
			pref.putFileCollection(klicZnamych, noveZname);
		}
	}

	public void setNedostupnePriNacitani(final Set<File> nedostupne) {
		nedostupnePriNacitani = nedostupne;
	}

	private static boolean jePod(final File f, final Collection<File> slozky) {
		final Path cesta = f.toPath();
		return slozky.stream().anyMatch(s -> {
			try {
				return cesta.startsWith(s.toPath());
			} catch (final InvalidPathException e) {
				return false; // neplatná cesta (ručně upravené nastavení) žádnou složku neoznačuje
			}
		});
	}

	/** Databáze, do které jiný program právě zapisuje; načte se, až zápis skončí. */
	public boolean jeZamcena(final KeFile jmenoZdroje) {
		return multiNacitacLoaderManager.jeZamcena(jmenoZdroje.getFile());
	}

	public boolean maSeNacist(final KeFile jmenoZdroje) {
		return maSeNacist(jmenoZdroje.getFile());
	}

	/** Zdroj se načítá, když ho uživatel nevypnul a není vypnutý celý jeho typ. */
	public boolean maSeNacist(final File zdroj) {
		return jeZdrojZapnut(zdroj) && !isTypVypnut(getRegistrStavuZdroju().getTyp(zdroj));
	}

	/** Vlastní volba položky, bez ohledu na vypnutý typ. */
	public boolean jeZdrojZapnut(final File zdroj) {
		return !blokovaneZdroje.contains(zdroj);
	}

	public boolean isTypVypnut(final TypZdroje typ) {
		return typ != null && vypnuteTypy.contains(typ);
	}

	/** Registr stavu, který model rozesílá událostmi; zapisuje do něj načítání. */
	public RegistrStavuZdroju getRegistrStavuZdroju() {
		return multiNacitacLoaderManager.getRegistr();
	}

	/** Poslední stav všech položek zdrojů; pro změny viz {@link StavZdrojuEvent}. */
	public StavZdroju getStavZdroju() {
		return stavZdroju;
	}

	/** Změny stavu z vlákna načítání i z EDT se slučují do jedné události v EDT. */
	private void naplanujUdalostStavuZdroju() {
		if (udalostStavuZdrojuCeka.compareAndSet(false, true)) {
			SwingUtilities.invokeLater(() -> {
				udalostStavuZdrojuCeka.set(false);
				final StavZdroju stav = multiNacitacLoaderManager.getRegistr().getSnimek();
				stavZdroju = stav;
				fire(new StavZdrojuEvent(stav));
			});
		}
	}

	/** Zapne nebo vypne celý typ; položky ani jejich volba se nemění, po zapnutí je stejný výběr jako před vypnutím. */
	public void setNacitatTyp(final TypZdroje typ, final boolean nacitat) {
		zmenZapnute(Collections.<File>emptyList(), Collections.<File>emptyList(), Collections.singletonMap(typ, !nacitat));
	}

	/** Výslovné „zapnout vše“ / „vypnout vše“: přepíše volbu všech položek i všech typů. */
	public void setNacitatVse(final boolean nacitat) {
		final List<File> soubory = new ArrayList<>();
		for (final StavPolozky p : getRegistrStavuZdroju().getSnimek().getPolozky()) {
			soubory.add(p.getSoubor());
		}
		final Map<TypZdroje, Boolean> typy = new EnumMap<>(TypZdroje.class);
		for (final TypZdroje t : TypZdroje.values()) {
			typy.put(t, !nacitat);
		}
		zmenZapnute(nacitat ? soubory : Collections.<File>emptyList(), nacitat ? Collections.<File>emptyList() : soubory, typy);
	}

	/** Výslovné „zapnout vše“ / „vypnout vše“ jen pro jeden typ: přepíše volbu jeho položek i typu. */
	public void setNacitatVseVTypu(final TypZdroje typ, final boolean nacitat) {
		final List<File> soubory = getRegistrStavuZdroju().getSoubory(typ);
		zmenZapnute(nacitat ? soubory : Collections.<File>emptyList(), nacitat ? Collections.<File>emptyList() : soubory, Collections.singletonMap(typ, !nacitat));
	}

	/** Jedna změna zapnutých zdrojů a typů: jeden zápis nastavení, okamžitá změna stavu pro GUI a jedno spuštění načítání. */
	private void zmenZapnute(final Collection<File> zapnout, final Collection<File> vypnout, final Map<TypZdroje, Boolean> typyVypnute) {
		final boolean zdroje = upravBlokovaneZdroje(b -> {
			final boolean zapnuto = b.removeAll(zapnout);
			return b.addAll(vypnout) | zapnuto;
		});
		final boolean typy = upravVypnuteTypy(typyVypnute);
		if (zdroje || typy) {
			getRegistrStavuZdroju().prepisZapnuti(this::jeZdrojZapnut, this::isTypVypnut);
			startKesLoading();
		}
	}

	private synchronized boolean upravVypnuteTypy(final Map<TypZdroje, Boolean> zmeny) {
		final Set<TypZdroje> nove = EnumSet.noneOf(TypZdroje.class);
		nove.addAll(vypnuteTypy);
		for (final Map.Entry<TypZdroje, Boolean> e : zmeny.entrySet()) {
			if (e.getValue()) {
				nove.add(e.getKey());
			} else {
				nove.remove(e.getKey());
			}
		}
		if (nove.equals(vypnuteTypy)) {
			return false;
		}
		vypnuteTypy = nove;
		final Set<String> jmena = new TreeSet<>();
		for (final TypZdroje t : nove) {
			jmena.add(t.name());
		}
		currPrefe().node(FPref.KESOID_node).putStringSet(FPref.VYPNUTE_TYPY_ZDROJU_value, jmena);
		return true;
	}


	public void onEvent(final IkonyNactenyEvent event) {
		jmenoSady = event.getBag().getSada().getName();
		if (!jmenoSady.equals(jmenoAktualniSadyIkon.name())) {
			// Vybraná sada chybí a načetla se náhradní, ať se příště nezkouší znovu.
			jmenoAktualniSadyIkon = Atom.valueOf(ASada.class, jmenoSady);
			currPrefe().node(FPref.JMENO_VYBRANE_SADY_IKON_node).putAtom(FPref.JMENO_VYBRANE_SADY_IKON_value, jmenoAktualniSadyIkon);
			fire(new JmenoAktualniSadyIkonChangeEvent(jmenoAktualniSadyIkon));
		}
		setJmenaNefenotypovanychAlel(currPrefe().node(FPref.MAPICON_FENOTYP_node).getQualAlelaNames(jmenoSady, QualAlelaNames.EMPTY));
	}

	public void onEvent(final KeskyNactenyEvent aEvent) {
		vycistiBlokovaneZdroje(aEvent.getVsechny().getInformaceOZdrojich().getJmenaZdroju());
		startIkonLoad(false);
	}

	public void otevriListingVGeogetu(final Kesoid kes) {
		if (kes == null) {
			return;
		}
		// Tisková URL existuje jen u starých odkazů s guid, jinak stačí běžná URL listingu.
		URL url = kes.getUrlPrint();
		if (url == null) {
			url = kes.getUrlShow();
		}
		if (url == null) {
			return;
		}
		final Clipboard scl = getSystemClipboard();
		final StringSelection ss = new StringSelection(url.toExternalForm());
		try {
			scl.setContents(ss, null);
		} catch (final IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Kopírování do schránky");
		}
	}

	public void pridejDoSeznamuVGeogetu(final Kesoid kes) {
		if (kes == null) {
			return;
		}
		final URL url = kes.getUrlShow();
		if (url == null) {
			return;
		}
		final Clipboard scl = getSystemClipboard();
		final StringSelection ss = new StringSelection(url.toExternalForm());
		try {
			scl.setContents(ss, null);
		} catch (final IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Kopírování do schránky");
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
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Kopírování do schránky");
		}
	}

	/** Vrátí filtr i skryté typy keší na výchozí hodnoty. */
	public void nastavImplicitniFiltr() {
		setDefinition(new FilterDefinition());
		setJmenaNechtenychAlel(VYCHOZI_NECHTENE_ALELY);
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
		if (jmenoAktualniSadyIkon.equals(this.jmenoAktualniSadyIkon)) {
			return;
		}
		this.jmenoAktualniSadyIkon = jmenoAktualniSadyIkon;
		currPrefe().node(FPref.JMENO_VYBRANE_SADY_IKON_node).putAtom(FPref.JMENO_VYBRANE_SADY_IKON_value, jmenoAktualniSadyIkon);
		fire(new JmenoAktualniSadyIkonChangeEvent(jmenoAktualniSadyIkon));
		startIkonLoad(true);
	}

	public void setNacitatSoubor(final KeFile jmenoZdroje, final boolean nacitat) {
		// TODO : speed up
		final InformaceOZdrojich zdroje = vsechny != null ? vsechny.getInformaceOZdrojich() : nacitaneZdroje;
		if (zdroje == null) {
			return;
		}
		final Collection<File> changedFiles = Collections2.transform(zdroje.getSubtree(jmenoZdroje), informaceOZdroji -> informaceOZdroji.jmenoZdroje.getFile());
		log.debug("Změna nastavení načítání ({}): {}", nacitat, changedFiles);
		zmenZapnute(nacitat ? changedFiles : Collections.<File>emptyList(), nacitat ? Collections.<File>emptyList() : changedFiles, Collections.<TypZdroje, Boolean>emptyMap());
	}

	/** Zapne nebo vypne jednu položku zdroje (soubor či databázi), jak ji ukazuje {@link StavZdroju}. */
	public void setNacitatPolozku(final File soubor, final boolean nacitat) {
		final Collection<File> jedna = Collections.singletonList(soubor);
		zmenZapnute(nacitat ? jedna : Collections.<File>emptyList(), nacitat ? Collections.<File>emptyList() : jedna, Collections.<TypZdroje, Boolean>emptyMap());
	}

	/** Zapne typ a z jeho položek nechá vybranou jen tuto. */
	public void setNacitatJenPolozku(final TypZdroje typ, final File soubor) {
		final List<File> ostatni = new ArrayList<>(getRegistrStavuZdroju().getSoubory(typ));
		ostatni.remove(soubor);
		zmenZapnute(Collections.singletonList(soubor), ostatni, Collections.singletonMap(typ, false));
	}

	/** Zapne typ (bez výběru se všemi položkami) a ostatní typy vypne; jejich výběr položek zůstane. */
	public void setNacitatJenTyp(final TypZdroje typ) {
		final Map<TypZdroje, Boolean> typy = new EnumMap<>(TypZdroje.class);
		for (final TypZdroje t : TypZdroje.values()) {
			typy.put(t, t != typ);
		}
		final List<File> soubory = getRegistrStavuZdroju().getSoubory(typ);
		final boolean nicVybrano = soubory.stream().noneMatch(this::jeZdrojZapnut);
		zmenZapnute(nicVybrano ? soubory : Collections.<File>emptyList(), Collections.<File>emptyList(), typy);
	}

	public void setOnoff(final boolean onoff) {
		if (this.onoff != null && this.onoff == onoff) {
			return;
		}
		this.onoff = onoff;
		currPrefe().node(FPref.KESOID_node).putBoolean(FPref.KESOID_VISIBLE_value, onoff);
		fire(new KesoidOnoffEvent(onoff));
	}

	/** Přejít na kešoid, jehož kód nebo odkaz se objeví ve schránce, když je okno programu neaktivní; výchozí je vypnuto. */
	public boolean isSledovatSchranku() {
		return sledovatSchranku;
	}

	public void setSledovatSchranku(final boolean sledovat) {
		if (sledovatSchranku == sledovat) {
			return;
		}
		sledovatSchranku = sledovat;
		currPrefe().node(FPref.KESOID_node).putBoolean(FPref.SLEDOVAT_SCHRANKU_value, sledovat);
		fire(new SledovaniSchrankyEvent(sledovat));
	}

	public void setZobrazeniKesi(final EZobrazeniKesi zobrazeniKesi) {
		if (this.zobrazeniKesi == zobrazeniKesi) {
			return;
		}
		this.zobrazeniKesi = zobrazeniKesi;
		currPrefe().node(FPref.KESOID_node).putEnum(FPref.ZOBRAZENI_KESI_value, zobrazeniKesi);
		fire(new ZobrazeniKesiEvent(zobrazeniKesi));
	}

	/** Zvětšení ikon keší v procentech. */
	public int getZvetseniIkonProcent() {
		return zvetseniIkonProcent;
	}

	public void setZvetseniIkonProcent(final int procent) {
		final int p = ZvetseniIkonEvent.oriznout(procent);
		if (p == zvetseniIkonProcent) {
			return;
		}
		zvetseniIkonProcent = p;
		currPrefe().node(FPref.KESOID_node).putInt(FPref.ZVETSENI_IKON_value, p);
		fire(new ZvetseniIkonEvent(p));
	}

	public void setPrekrocenLimitWaypointuVeVyrezu(final boolean prekrocenLimit, final boolean tecky, final int limit) {
		fire(new PrekrocenLimitWaypointuVeVyrezuEvent(prekrocenLimit, tecky, limit));
	}

	public LimityKresleni getLimityKresleni() {
		return limityKresleni;
	}

	public void setLimityKresleni(final LimityKresleni limity) {
		if (limity.equals(limityKresleni)) {
			return;
		}
		limityKresleni = limity;
		currPrefe().node(FPref.KESOID_node).putInt(FPref.LIMIT_IKON_value, limity.getIkon());
		currPrefe().node(FPref.KESOID_node).putInt(FPref.LIMIT_TECEK_value, limity.getTecek());
		fire(new LimityKresleniEvent(limity));
	}

	public Zvyrazneni getZvyrazneni() {
		return zvyrazneni;
	}

	/** Vlastní předvolby zvýraznění, vestavěné nejsou mezi nimi. */
	public List<Zvyrazneni> getPredvolbyZvyrazneni() {
		return predvolbyZvyrazneni;
	}

	public void setZvyrazneni(final Zvyrazneni zvyrazneni) {
		if (zvyrazneni.equals(this.zvyrazneni)) {
			return;
		}
		this.zvyrazneni = zvyrazneni;
		currPrefe().node(FPref.ZVYRAZNENI_node).put(FPref.ZVYRAZNENI_value, zvyrazneni.naText());
		fire(new ZvyrazneniEvent(zvyrazneni, predvolbyZvyrazneni));
		startIkonLoad(true);
	}

	public void setPredvolbyZvyrazneni(final List<Zvyrazneni> predvolby) {
		if (predvolby.equals(predvolbyZvyrazneni)) {
			return;
		}
		predvolbyZvyrazneni = Collections.unmodifiableList(new ArrayList<>(predvolby));
		final List<String> texty = new ArrayList<>();
		for (final Zvyrazneni z : predvolbyZvyrazneni) {
			texty.add(z.naText());
		}
		currPrefe().node(FPref.ZVYRAZNENI_node).putStringList(FPref.ZVYRAZNENI_PREDVOLBY_value, texty);
		fire(new ZvyrazneniEvent(zvyrazneni, predvolbyZvyrazneni));
	}

	private void nactiZvyrazneni() {
		final MyPreferences pref = currPrefe().node(FPref.ZVYRAZNENI_node);
		final Zvyrazneni ulozene = Zvyrazneni.zTextu(pref.get(FPref.ZVYRAZNENI_value, null));
		zvyrazneni = ulozene == null ? Zvyrazneni.VYCHOZI : ulozene;
		final List<Zvyrazneni> predvolby = new ArrayList<>();
		for (final String text : pref.getStringList(FPref.ZVYRAZNENI_PREDVOLBY_value, Collections.emptyList())) {
			final Zvyrazneni z = Zvyrazneni.zTextu(text);
			if (z != null) {
				predvolby.add(z);
			}
		}
		predvolbyZvyrazneni = Collections.unmodifiableList(predvolby);
		fire(new ZvyrazneniEvent(zvyrazneni, predvolbyZvyrazneni));
	}

	void nactiLimityKresleni(final MyPreferences kesoid) {
		limityKresleni = LimityKresleni.of(kesoid.getInt(FPref.LIMIT_IKON_value, LimityKresleni.VYCHOZI_IKON), kesoid.getInt(FPref.LIMIT_TECEK_value, LimityKresleni.VYCHOZI_TECEK))
				.sVlastnostmi();
		fire(new LimityKresleniEvent(limityKresleni));
	}

	/** Voláno z vlákna načítání před čtením zdrojů; událost doručená v EDT. */
	public void setNacitaneZdroje(final InformaceOZdrojich zdroje) {
		nacitaneZdroje = zdroje;
		SwingUtilities.invokeLater(() -> fire(new NacitaneZdrojeEvent(zdroje)));
	}

	/** Voláno po každém načtení; událost jen při změně, doručená v EDT. */
	public void setZamceneDatabaze(final List<String> jmena) {
		if (jmena.equals(zamceneDatabaze)) {
			return;
		}
		zamceneDatabaze = jmena;
		SwingUtilities.invokeLater(() -> fire(new ZamceneDatabazeEvent(jmena)));
	}

	public void setGsakParametryNacitani(final GsakParametryNacitani aGsakParametryNacitani) {
		gsakParametryNacitani = aGsakParametryNacitani;
		final MyPreferences pref = currPrefe().node(FPref.GSAK_node);
		pref.putStringSet(FPref.GSAK_CAS_NALEZU_value, aGsakParametryNacitani.getCasNalezu());
		pref.remove(FPref.GSAK_CAS_NENALEZU_value);
		pref.putBoolean(FPref.NOVE_DATABAZE_HNED_value, aGsakParametryNacitani.isNoveDatabazeHned());
		currPrefe().node(FPref.OPENSAK_node).putBoolean(FPref.NOVE_DATABAZE_HNED_value, aGsakParametryNacitani.isNoveDatabazeOpensakuHned());
		currPrefe().node(FPref.GEOGET_node).putBoolean(FPref.NOVE_DATABAZE_HNED_value, aGsakParametryNacitani.isNoveDatabazeGeogetuHned());
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
		umisteniSouboru = aUmisteniSouboru;

		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		pref.putFilex(FPref.KES_DIR_value, aUmisteniSouboru.getKesDir());
		pref.putFilex(FPref.GEOGET_DATA_DIR_value, aUmisteniSouboru.getGeogetDataDir());
		pref.putFilex(FPref.GSAK_DATA_DIR_value, aUmisteniSouboru.getGsakDataDir());
		pref.putFilex(FPref.OPENSAK_DATA_DIR_value, aUmisteniSouboru.getOpensakDataDir());
		pref.remove("vyjimkyDir"); // mazat ze starých verzí
		nactiVyberZdroju();
		fire(new KesoidUmisteniSouboruChangedEvent(aUmisteniSouboru));
		if (nacistIkony) {
			startIkonLoad(true);
		} else { // když se načítají ikony, tak se vždy potom čtou keše
			if (nacistKese) {
				startKesLoading();
			}
		}
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
		filteringSwingWorker = new KesFilteringSwingWorker(vsechny, filter, this, getProgressModel());
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
		final String gccomNickName = currPrefe().node(FPref.NASTAVENI_node).get(FPref.GEOCACHING_COM_NICK_value, "sem napište svůj nick na geocaching.com");
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

		filter.setJmenaNechtenychAlel(currPrefe().node(FPref.KESOID_FILTR_node).getQualAlelaNames(FPref.KESOID_FILTER_ALELY_value, VYCHOZI_NECHTENE_ALELY));
		jmenaAlelNaToolbaru = currPrefe().node(FPref.KESOID_FILTR_node).getQualAlelaNames(FPref.KESOID_FILTER_NATOOLBARU_value, VYCHOZI_NECHTENE_ALELY);

		final ASada jmenoAktualniSadyIkon = currPrefe().node(FPref.JMENO_VYBRANE_SADY_IKON_node).getAtom(FPref.JMENO_VYBRANE_SADY_IKON_value, ASada.STANDARD, ASada.class);
		this.jmenoAktualniSadyIkon = jmenoAktualniSadyIkon;
		fire(new JmenoAktualniSadyIkonChangeEvent(jmenoAktualniSadyIkon));
		nactiZvyrazneni();
		// fire(new GccomNickChangedEvent(gccomNick));
		// loadUmisteniSouboru();
		setGsakParametryNacitani(loadGsakParametryNacitani());
		setUmisteniSouboru(loadUmisteniSouboru());

		final MyPreferences kesoid = currPrefe().node(FPref.KESOID_node);
		setOnoff(kesoid.getBoolean(FPref.KESOID_VISIBLE_value, true));
		setSledovatSchranku(kesoid.getBoolean(FPref.SLEDOVAT_SCHRANKU_value, false));
		fire(new SledovaniSchrankyEvent(sledovatSchranku));
		setZobrazeniKesi(kesoid.getEnum(FPref.ZOBRAZENI_KESI_value, EZobrazeniKesi.AUTOMATICKY, EZobrazeniKesi.class));
		zvetseniIkonProcent = ZvetseniIkonEvent.oriznout(kesoid.getInt(FPref.ZVETSENI_IKON_value, ZvetseniIkonEvent.VYCHOZI));
		fire(new ZvetseniIkonEvent(zvetseniIkonProcent));
		nactiLimityKresleni(kesoid);
		fajruj();
	}

	private void fajruj() {
		fire(new FilterDefinitionChangedEvent(filter.getFilterDefinition(), filter.getJmenaNechtenychAlel(), jmenaAlelNaToolbaru));
		spustFiltrovani();
	}

	private KesoidUmisteniSouboru loadUmisteniSouboru() {
		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		final MyPreferences pref = currPrefe().node(FPref.UMISTENI_SOUBORU_node);
		u.setKesDir(pref.getFilex("kesDir", KesoidUmisteniSouboru.KES_DIR));
		u.setCestyDir(KesoidUmisteniSouboru.CESTY_DIR);
		u.setGeogetDataDir(pref.getFilex("geogetDataDir", KesoidUmisteniSouboru.GEOGET_DATA_DIR));
		u.setGsakDataDir(pref.getFilex("gsakDataDir", KesoidUmisteniSouboru.GSAK_DATA_DIR));
		u.setOpensakDataDir(pref.getFilex(FPref.OPENSAK_DATA_DIR_value, KesoidUmisteniSouboru.OPENSAK_DATA_DIR));
		u.setImage3rdPartyDir(KesoidUmisteniSouboru.IMAGE_3RDPARTY_DIR);
		u.setImageMyDir(KesoidUmisteniSouboru.IMAGE_MY_DIR);
		u.setAnoGgtFile(KesoidUmisteniSouboru.ANO_GGT);
		u.setNeGgtFile(KesoidUmisteniSouboru.NE_GGT);
		return u;
	}

	GsakParametryNacitani loadGsakParametryNacitani() {
		final GsakParametryNacitani g = new GsakParametryNacitani();
		final MyPreferences pref = currPrefe().node(FPref.GSAK_node);
		g.setCasNalezu(pref.getStringList(FPref.GSAK_CAS_NALEZU_value, Arrays.asList("UserData")));
		g.setNoveDatabazeHned(pref.getBoolean(FPref.NOVE_DATABAZE_HNED_value, true));
		g.setNoveDatabazeOpensakuHned(currPrefe().node(FPref.OPENSAK_node).getBoolean(FPref.NOVE_DATABAZE_HNED_value, true));
		g.setNoveDatabazeGeogetuHned(currPrefe().node(FPref.GEOGET_node).getBoolean(FPref.NOVE_DATABAZE_HNED_value, true));
		return g;
	}

	private void startKesLoading() {
		if (ikonBag != null && gccomNick != null) {
			multiNacitacLoaderManager.startLoad(true, ikonBag.getGenom());
		}
	}

	/** Čas dat zdrojů z minulého běhu programu, záznamy ve tvaru, kterému rozumí načítání; chybějící nastavení = žádné záznamy. */
	public synchronized Set<String> getCasyDatZdroju() {
		return casyDatZdroju;
	}

	public synchronized void setCasyDatZdroju(final Set<String> zaznamy) {
		casyDatZdroju = Collections.unmodifiableSet(new LinkedHashSet<>(zaznamy));
		if (casyDatZNastaveni) {
			currPrefe().node(FPref.KESOID_node).putStringSet(FPref.CASY_DAT_ZDROJU_value, casyDatZdroju);
		}
	}

	/** Načte z nastavení, které zdroje a typy zdrojů uživatel vypnul, a časy dat zdrojů. */
	public synchronized void nactiVyberZdroju() {
		casyDatZdroju = Collections.unmodifiableSet(currPrefe().node(FPref.KESOID_node).getStringSet(FPref.CASY_DAT_ZDROJU_value, Collections.<String>emptySet()));
		casyDatZNastaveni = true;
		blokovaneZdroje = new HashSet<>(currPrefe().node(FPref.KESOID_node).getFileCollection(FPref.BLOKOVANE_ZDROJE_value, new HashSet<File>()));
		vypnuteTypy = nactiVypnuteTypy();
	}

	private Set<TypZdroje> nactiVypnuteTypy() {
		final Set<TypZdroje> typy = EnumSet.noneOf(TypZdroje.class);
		for (final String jmeno : currPrefe().node(FPref.KESOID_node).getStringSet(FPref.VYPNUTE_TYPY_ZDROJU_value, Collections.<String>emptySet())) {
			try {
				typy.add(TypZdroje.valueOf(jmeno));
			} catch (final IllegalArgumentException e) {
				// neznámý typ z novější verze
			}
		}
		return typy;
	}

	/** Zapomene blokované zdroje, které už nejsou; zdroje v dočasně nedostupné složce (síť, USB) zůstanou blokované. */
	void vycistiBlokovaneZdroje(final Set<File> zdroje) {
		// Volá se na EDT po každém načtení; dostupnost složek zjistil sken ve vlákně načítání, neodpovídající síťový disk by EDT zablokoval.
		if (blokovaneZdroje.isEmpty()) {
			return;
		}
		final Set<File> priNacitani = nedostupnePriNacitani;
		upravBlokovaneZdroje(b -> b.removeIf(f -> !zdroje.contains(f) && !jePod(f, priNacitani)));
	}

	/** Upraví kopii blokovaných zdrojů; když se změnila, uloží ji. */
	private synchronized boolean upravBlokovaneZdroje(final java.util.function.Predicate<Set<File>> uprava) {
		final Set<File> nove = new HashSet<>(blokovaneZdroje);
		if (!uprava.test(nove)) {
			return false;
		}
		blokovaneZdroje = nove;
		currPrefe().node(FPref.KESOID_node).putFileCollection(FPref.BLOKOVANE_ZDROJE_value, nove);
		return true;
	}

}
