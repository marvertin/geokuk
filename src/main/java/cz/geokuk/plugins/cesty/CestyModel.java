/**
 *
 */
package cz.geokuk.plugins.cesty;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.cesty.data.*;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.lang.FUtil;

/**
 * @author veverka
 *
 */
public class CestyModel extends Model0 {

	private static final Logger		log				= LogManager.getLogger(CestyModel.class.getSimpleName());

	private static final String		MUJ_VYLET		= "Můj výlet";
	public static final String		VYLET_EXTENSION	= "gpx";
	private Doc						doc				= new Doc();

	private final Updator			updator			= new Updator();
	/** Aktivní cesta, vždy je nějaká aktivní, když ne platí bod číslo 1 */
	private Cesta					curta;

	private CestyZperzistentnovac	cestyZperzistentnovac;

	private Worker					worker;
	private KesBag					kesBag;

	private KesoidModel				kesoidModel;

	private PoziceModel				poziceModel;
	private Coord					moord;
	private boolean					probihaPridavani;

	public void addToVylet(Mouable mouable) {
		pridejBodNaMisto(mouable);
	}

	public boolean isOnVylet(Mouable mouable) {
		if (mouable == null)
			return false;
		if (mouable instanceof Wpt) {
			Wpt wpt = (Wpt) mouable;
			return doc.hasWpt(wpt);
		}
		if (mouable instanceof Bod)
			return true;
		return false;
	}

	// public EVylet get(Wpt wpt) {
	// boolean onIgnoreList = vylet.isOnIgnoreList(wpt.getKesoid());
	// if (onIgnoreList) return EVylet.NE;
	// boolean jeVCeste = cestyModel.getCesta().hasWpt(wpt);
	// if (jeVCeste) return EVylet.ANO;
	// // Není tam ani tam, tak nevím, kam na výlet
	// return EVylet.NEVIM;
	// }

	public int getPocetWaypointuVeVyletu() {
		return doc.getPocetWaypointu();
	}

	public void inject(CestyZperzistentnovac cestyZperzistentnovac) {
		this.cestyZperzistentnovac = cestyZperzistentnovac;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.geokuk.program.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
		boolean mameOtevritVylet = currPrefe().node(FPref.VYLET_node).getBoolean(FPref.JE_OTEVRENY_VYLET_value, false);
		File file = currPrefe().node(FPref.VYLET_node).getFile(FPref.AKTUALNI_SOUBOR_value, null);
		if (mameOtevritVylet && file != null && file.canRead()) {
			otevri(file);
		}
		fireCesta();
		fire(new PridavaniBoduEvent(false)); // přidávání neprobíhá, tak aŤ se provede příslušný event
	}

	public File defaultExportuDoGgt() {
		return new File(kesoidModel.getUmisteniSouboru().getGeogetDataDir().getEffectiveFile(), "lovim.ggt");
	}

	public File defaultAktualnihoVyletuFile() {
		File defaultFile = new File(kesoidModel.getUmisteniSouboru().getCestyDir().getEffectiveFile(), "Můj výlet.gpx");
		return currPrefe().node(FPref.VYLET_node).getFile(FPref.AKTUALNI_SOUBOR_value, defaultFile);
	}

	private class Worker extends SwingWorker<Void, Void> {

		private final List<Kesoid> kese;

		public Worker(List<Kesoid> kese) {
			this.kese = kese;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected Void doInBackground() throws Exception {
			Clipboard scl = getSystemClipboard();
			for (Kesoid kes : kese) {
				if (isCancelled()) {
					break;
				}
				scl.setContents(new StringSelection(kes.getUrlShow().toExternalForm()), null);
				Thread.sleep(100);
			}
			return null;
		}

	}

	public void nasypVyletDoGeogetu() {
		if (worker != null) {
			worker.cancel(true);
		}
		List<Kesoid> kesoidy = new ArrayList<>();
		for (Wpt wpt : doc.getWpts()) {
			kesoidy.add(wpt.getKesoid());
		}
		worker = new Worker(kesoidy);
		worker.execute();
	}

	public void pridejBodNaMisto(Mouable mouable) {
		if (curta == null) {
			Cesta cesta = doc.findNejblizsiCesta(mouable.getMou());
			if (cesta == null) {
				cesta = Cesta.create();
			}
			updator.xadd(doc, cesta);
			curta = cesta;
		}
		updator.pridejNaMisto(getCurta(), mouable);
		fireCesta();
	}

	public Bod pridejBodNaKonec(Mouable mouable) {
		if (curta == null) {
			Cesta cesta = Cesta.create();
			updator.xadd(doc, cesta);
			curta = cesta;
		}
		Bod bod = updator.pridejNaKonec(getCurta(), mouable);
		fireCesta();
		return bod;
	}

	public void odeberBod(Mouable mouable) {
		Bod bod = doc.findBod(mouable);
		if (bod == null)
			return;
		removeBod(bod);
		fireCesta();
	}

	public Usek removeBod(Bod bb) {
		Usek usek = updator.removeBod(bb);
		fireCesta();
		return usek;
	}

	public Bod rozdelUsekNaDvaNove(Usek usek, Mou mouNovy) {
		Bod bod = updator.rozdelUsekNaDvaNove(usek, mouNovy);
		fireCesta();
		return bod;
	}

	public void presunBod(Bod bb, Mouable mouable) {
		// if (mouable instanceof Wpt) {
		// Wpt wpt = (Wpt) mouable;
		// wpt.invalidate();
		// }
		// if (bb.getMouable() instanceof Wpt) {
		// Wpt wpt = (Wpt) bb.getMouable();
		// wpt.invalidate();
		// }
		// musime odstranit reference bodi na bod
		while (mouable instanceof Bod) {
			Bod bod = (Bod) mouable;
			mouable = bod.getMouable();
		}
		invalidate(mouable);
		invalidate(bb);
		if (bb.isStartocil()) {
			updator.setMouable(bb.getCesta().getStart(), mouable);
			updator.setMouable(bb.getCesta().getCil(), mouable);
		} else {
			updator.setMouable(bb, mouable);
		}
		fireCesta();
	}

	private void invalidate(Mouable mouable) {
		if (mouable instanceof Wpt) {
			Wpt wpt = (Wpt) mouable;
			wpt.invalidate();
		} else if (mouable instanceof Bod) {
			Bod bod = (Bod) mouable;
			invalidate(bod.getMouable());
		}
	}

	private void invalidate(Doc doc) {
		if (doc == null)
			return;
		for (Bod bod : doc.getBody()) {
			invalidate(bod);
		}
	}

	private void cleanCurta() {
		if (curta == null)
			return;
		for (Cesta cesta : doc.getCesty()) {
			if (cesta == curta)
				return;
		}
		curta = null;
	}

	private void fireCesta() {
		cleanCurta();
		poziceModel.refreshPozice();
		if (curta != null) {
			curta.kontrolaKonzistence();
		}
		fire(new CestyChangedEvent(doc, curta));
	}

	void prevezmiNoveOtevrenyDokument(Doc doc) {
		setDefaultProAktualniVyletFile(doc.getFile());
		doc.resetChanged();
		invalidate(doc);
		invalidate(this.doc);
		this.doc = doc;
		fireCesta();
	}

	void prevezmiImportovaneCesty(List<Cesta> cesty) {
		for (Cesta cesta : cesty) {
			doc.xadd(cesta);
		}
		invalidate(doc);
		fireCesta();
	}

	public void otevri(File file) {
		Doc doc = new Doc();
		doc.setFile(file);
		CestyOtevriSwingWorker wrk = new CestyOtevriSwingWorker(cestyZperzistentnovac, kesBag, this, file);
		wrk.execute();
	}

	public void importuj(List<File> files) {
		CestyImportSwingWorker wrk = new CestyImportSwingWorker(cestyZperzistentnovac, kesBag, this, files);
		wrk.execute();
	}

	public Doc getDoc() {
		return doc;
	}

	public void onEvent(KeskyNactenyEvent aEvent) {
		kesBag = aEvent.getVsechny();
	}

	public void onEvent(VyrezChangedEvent aEvent) {
		moord = aEvent.getMoord();
	}

	public Cesta getCurta() {
		return curta;
	}

	void setCurta(Cesta curta) {
		if (this.curta == curta)
			return;
		this.curta = curta;
		fireCesta();
	}

	public void prepniVzdusnostUseku(Usek usek, boolean vzdusny) {
		if (usek.isVzdusny() == vzdusny)
			return;
		updator.setVzdusny(usek, vzdusny);
		fireCesta();
	}

	public void uloz(File file, Doc doc, boolean statSeImplicitniProDokument) {
		new Ukladac().uloz(file, doc);
		if (statSeImplicitniProDokument) {
			doc.setFile(file);
			doc.resetChanged();
			setDefaultProAktualniVyletFile(file);
		}
		fireCesta();
	}

	public void exportujDoGgt(File file, Doc doc2) {
		cestyZperzistentnovac.zapisGgt(doc, file);
	}

	private void setDefaultProAktualniVyletFile(File file) {
		currPrefe().node(FPref.VYLET_node).putFile(FPref.AKTUALNI_SOUBOR_value, file);
		currPrefe().node(FPref.VYLET_node).putBoolean(FPref.JE_OTEVRENY_VYLET_value, true);
	}

	public void inject(PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public void inject(KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public File getImplicitniVyletNovyFile() {
		File dir = defaultAktualnihoVyletuFile().getParentFile();
		File result = najdiNeexistujiciSoubor(new File(dir, MUJ_VYLET + "." + VYLET_EXTENSION));
		return result;
	}

	public File getImplicitniVyletSaveAsNovyFile() {
		File file = doc.getFile();
		if (file == null) {
			return getImplicitniVyletNovyFile();
		} else {
			File result = najdiNeexistujiciSoubor(file);
			return result;
		}
	}

	public File getImplicitniVyletSaveCopyNovyFile() {
		File file = doc.getFile();
		if (file == null) {
			return getImplicitniVyletNovyFile();
		} else {
			File result = najdiNeexistujiciSoubor(new File(file.getParentFile(), "Kopie " + file.getName()));
			return result;
		}
	}

	private File najdiNeexistujiciSoubor(File aFile) {
		File dir = aFile.getParentFile();
		String pureName = aFile.getName();
		Pattern pat = Pattern.compile("^(.*?)(?: *\\(\\d+\\))?(\\.[^.]+)?$");
		Matcher mat = pat.matcher(pureName);
		log.debug(pureName + ": " + mat.matches() + " -- " + pat);
		log.debug(mat.group(1) + " * " + mat.group(2));
		// String baseName = poz < 0 ? pureName : pureName.substring(0, poz);
		// String extension = poz < 0 ? "" : pureName.substring(poz);
		String baseName = mat.group(1);
		String extension = mat.group(2);
		for (int i = 0;; i++) {
			File file = new File(dir, baseName + (i == 0 ? "" : " (" + i + ")") + extension);
			if (!file.exists())
				return file;
		}

	}

	public void znovuVsechnoPripni() {
		cestyZperzistentnovac.pripniNaWayponty(doc.getCesty(), kesBag);
		fireCesta();
	}

	public void zavri() {
		invalidate(doc);
		doc = new Doc();
		currPrefe().node(FPref.VYLET_node).putBoolean(FPref.JE_OTEVRENY_VYLET_value, false);
		fireCesta();
	}

	public void removeCestu(Cesta cesta) {
		for (Bod bod : cesta.getBody()) {
			invalidate(bod);
		}
		updator.remove(cesta);
		fireCesta();
	}

	public void reverseCestu(Cesta cesta) {
		updator.reverse(cesta);
		fireCesta();
	}

	public void onEvent(PoziceSeMaMenitEvent event) {
		for (Bod bod : doc.getBody()) {
			if (event.mou.equals(bod.getMou())) {
				int priorita = 20;
				if (bod.isStart()) {
					priorita = 21;
				}
				if (bod.isCil()) {
					priorita = 22;
				}
				event.add(bod, priorita);
			}
		}
	}

	public void bezNaZacatekCesty(Cesta cesta) {
		if (cesta == null) {
			cesta = doc.findNejblizsiCesta(moord.getMoustred());
		}
		if (cesta == null)
			return;
		curta = cesta;
		poziceModel.setPozice(cesta.getStart());
	}

	public void bezNaKonecCesty(Cesta cesta) {
		if (cesta == null) {
			cesta = doc.findNejblizsiCesta(moord.getMoustred());
		}
		if (cesta == null)
			return;
		curta = cesta;
		poziceModel.setPozice(cesta.getCil());
	}

	public void bezNaBod(Bod bod) {
		poziceModel.setPozice(bod);
	}

	public void bezNaBodVpred() {
		Bod bod = doc.findBod(poziceModel.getPoziceq().getPoziceMouable());
		if (bod != null) {
			if (!bod.isCil()) {
				poziceModel.setPozice(bod.getBvpred());
			}
		} else {
			bezNaZacatekCesty(curta);
		}
	}

	public void bezNaBodVzad() {
		Bod bod = doc.findBod(poziceModel.getPoziceq().getPoziceMouable());
		if (bod != null) {
			if (!bod.isStart()) {
				poziceModel.setPozice(bod.getBvzad());
			}
		} else {
			bezNaKonecCesty(curta);
		}
	}

	public void rozdelCestuVBode(Bod bod, boolean aSmazOdriznutyKus) {
		invalidate(doc);
		Cesta novaCesta = updator.rozdelCestuVBode(bod);
		if (aSmazOdriznutyKus) {
			InvalidacniPesek invalidacniPesek = invalidacniPesek();
			updator.remove(novaCesta);
			invalidacniPesek.invaliduj();
		}
		fireCesta();
	}

	public void rozdelCestuVUseku(Usek usek, Mou mou, boolean aSmazOdriznutyKus) {
		invalidate(doc);
		Cesta novaCesta = updator.rozdelCestuVUseku(usek, mou);
		if (aSmazOdriznutyKus) {
			InvalidacniPesek invalidacniPesek = invalidacniPesek();
			updator.remove(novaCesta);
			invalidacniPesek.invaliduj();
		}
		fireCesta();
	}

	public void uzavriCestu(Cesta cesta) {
		if (cesta.isKruh())
			return;
		updator.pridejNaKonec(cesta, cesta.getStart().getMouable());
		fireCesta();
	}

	public void pripojitCestuZa(Cesta cesta1, Cesta cesta2) {
		doc.kontrolaKonzistence();
		cesta1.kontrolaKonzistence();
		cesta2.kontrolaKonzistence();
		updator.pipojitCestuZa(cesta1, cesta2);
		curta = cesta1;
		curta.kontrolaKonzistence();
		doc.kontrolaKonzistence();
		fireCesta();
	}

	public void pripojitCestuPred(Cesta cesta1, Cesta cesta2) {
		doc.kontrolaKonzistence();
		cesta1.kontrolaKonzistence();
		cesta2.kontrolaKonzistence();
		updator.pipojitCestuPred(cesta1, cesta2);
		curta = cesta1;
		curta.kontrolaKonzistence();
		doc.kontrolaKonzistence();
		fireCesta();
	}

	public void spojCestyVPrekryvnemBode(Bod bod) {
		Bod druhyBod = bod.getKoncovyBodDruheCestyVhodnyProSpojeni();
		if (druhyBod == null)
			return;
		if (bod.isCil()) {
			updator.pipojitCestuZa(bod.getCesta(), druhyBod.getCesta());
		} else if (bod.isStart()) {
			updator.pipojitCestuPred(bod.getCesta(), druhyBod.getCesta());
		}
		fireCesta();
	}

	public void pospojujVzdusneUseky(Cesta cesta) {
		updator.pospojujVzdusneUseky(cesta);
		fireCesta();
	}

	public void smazatUsekAOtevritNeboRozdelitCestu(Usek usek) {
		updator.smazatUsekAOtevritNeboRozdelitCestu(usek);
		fireCesta();

	}

	public void presunoutVyhoziBod(Bod bod) {
		Cesta cesta = bod.getCesta();
		Cesta novaCesta = updator.rozdelCestuVBode(bod);
		updator.pipojitCestuPred(cesta, novaCesta);
		fireCesta();
	}

	private InvalidacniPesek invalidacniPesek() {
		InvalidacniPesek pesek = new InvalidacniPesek(doc.getWpts());
		return pesek;
	}

	private class InvalidacniPesek {
		private Set<Wpt> puvodnici = new HashSet<>();

		public InvalidacniPesek(Iterable<Wpt> wpts) {
			FUtil.addAll(puvodnici, wpts);
		}

		void invaliduj() {
			assert puvodnici != null : "Pokus o duplicitní použití invalidačního peška";
			for (Wpt wpt : doc.getWpts()) {
				boolean removedNow = puvodnici.remove(wpt);
				if (!removedNow) { // to znamená, že v čase vytváření peška tam nebyl
					wpt.invalidate();
				}
			}
			// Vše, co tam zbyle dnes nemáme a tedy invalidujeme
			for (Wpt wpt : puvodnici) {
				wpt.invalidate();
			}
			// znemožníme invalidačního peška
			puvodnici = null;

		}

	}

	public void setPridavaniBodu(boolean probihaPridavani) {
		if (this.probihaPridavani == probihaPridavani)
			return;
		this.probihaPridavani = probihaPridavani;
		fire(new PridavaniBoduEvent(probihaPridavani));
	}
}
