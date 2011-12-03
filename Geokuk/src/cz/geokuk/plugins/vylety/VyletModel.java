/**
 * 
 */
package cz.geokuk.plugins.vylety;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.core.coord.PoziceSeMaMenitEvent;
import cz.geokuk.core.coord.VyrezChangedEvent;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Dlg;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.vylety.cesty.Bod;
import cz.geokuk.plugins.vylety.cesty.Cesta;
import cz.geokuk.plugins.vylety.cesty.Doc;
import cz.geokuk.plugins.vylety.cesty.Updator;
import cz.geokuk.plugins.vylety.cesty.Usek;
import cz.geokuk.util.file.Filex;
import cz.geokuk.util.lang.FUtil;
import cz.geokuk.util.process.BrowserOpener;

/**
 * @author veverka
 *
 */
public class VyletModel extends Model0 {

  private static final String MUJ_VYLET = "Můj výlet";
  public static final String VYLET_EXTENSION = "gpx";
  private IgnoreList ignoreList;
  private Doc doc = new Doc();

  private final Updator updator = new Updator();
  /** Aktivní cesta, vždy je nějaká aktivní, když ne platí bod číslo 1 */
  private Cesta curta;


  private VyletovyZperzistentnovac vyletovyZperzistentnovac;

  private Worker worker;
  private KesBag kesBag;


  private KesoidModel kesoidModel;

  private boolean zastaralyAnoGgtFileBylNahran;
  private boolean probihaNahravaniZastaralehoAnoGgtFile;
  private File zastaralySouborSVyletem;
  private boolean priPrvnimUlozeniSeZapiseZeZastaralySouborAnoGgtUzNebudeNikdyNacitan;
  private PoziceModel poziceModel;
  private Coord moord;
  private boolean probihaPridavani;

  public void addToIgnoreList(Mouable mouable) {
    if (mouable == null) return;
    if (mouable instanceof Wpt) {
      Wpt wpt = (Wpt) mouable;
      for (Wpt w : wpt.getKesoid().getWpts()) {
        odeberBod(w);
      }
      boolean zmenaIgnoreListu = ignoreList.addToIgnoreList(wpt);
      if (zmenaIgnoreListu) {
        ulozIgnoreListAFiruj(wpt);
      }
    }
    odeberBod(mouable);
  }

  public void addToVylet(Mouable mouable) {
    pridejBodNaMisto(mouable);
  }

  public void removeFromBoth(Mouable mouable) {
    boolean zmenaIgnoreListu = false;
    if (mouable instanceof Wpt) {
      Wpt wpt = (Wpt) mouable;
      zmenaIgnoreListu = ignoreList.removeFromIgnoreList(wpt);
      if (zmenaIgnoreListu) {
        ulozIgnoreListAFiruj(wpt);
      }
    }
    odeberBod(mouable);
  }

  private void removeFromIgnorList(Mouable mouable) {
    if (mouable instanceof Wpt) {
      Wpt wpt = (Wpt) mouable;
      wpt.invalidate();
      boolean zmenaIgnoreListu = ignoreList.removeFromIgnoreList(wpt);
      if (zmenaIgnoreListu) {
        ulozIgnoreListAFiruj(wpt);
      }
    }
  }

  public boolean isOnIgnoreList(Mouable mouable) {
    if (mouable instanceof Wpt) {
      Wpt wpt = (Wpt) mouable;
      return ignoreList.isOnIgnoreList(wpt);
    } else
      return false;
  }

  public boolean isOnVylet(Mouable mouable) {
    if (mouable == null) return false;
    if (mouable instanceof Wpt) {
      Wpt wpt = (Wpt) mouable;
      return doc.hasWpt(wpt);
    }
    if (mouable instanceof Bod)
      return true;
    return false;
  }


  private void ulozIgnoreListAFiruj(Wpt wptx) {
    IgnoreListSaveSwingWorker worker = new IgnoreListSaveSwingWorker(vyletovyZperzistentnovac, ignoreList);
    worker.execute();
    for (Wpt w : wptx.getKesoid().getWpts()) {
      onChangeIgnoreList(w);
    }
  }

  //  public EVylet get(Wpt wpt) {
  //    boolean onIgnoreList = vylet.isOnIgnoreList(wpt.getKesoid());
  //    if (onIgnoreList) return EVylet.NE;
  //    boolean jeVCeste = cestyModel.getCesta().hasWpt(wpt);
  //    if (jeVCeste) return EVylet.ANO;
  //    // Není tam ani tam, tak nevím, kam na výlet
  //    return EVylet.NEVIM;
  //  }


  public void removeAllFromCesta() {
    InvalidacniPesek invalidacniPesek = invalidacniPesek();
    for (Wpt wpt : doc.getWpts()) {
      removeFromBoth(wpt);
    }
    invalidacniPesek.invaliduj();
  }

  public void clearIgnoreList() {
    boolean zmena = ignoreList.clear();
    if (zmena) {
      // cokoli se mohlo změnit
      onChangeIgnoreList(null);
    }
  }


  public int getPocetIgnorovanychKesoidu() {
    int pocet = ignoreList.getIgnoreList().size();
    return pocet;

  }

  public int getPocetWaypointuVeVyletu() {
    return doc.getPocetWaypointu();
  }


  private void onChangeIgnoreList(Wpt wpt) {
    if (!SwingUtilities.isEventDispatchThread()) return;
    fire(new IgnoreListChangedEvent(wpt));
  }

  /**
   * @param vsechny
   */
  public void startLogingIgnoreList(KesBag vsechny) {
    IgnoreListLoadSwingWorker worker = new IgnoreListLoadSwingWorker(vyletovyZperzistentnovac, vsechny, this);
    worker.execute();
  }

  void setNewlyLoadedIgnoreList(IgnoreList newIgnoreList) {
    ignoreList = newIgnoreList;
    fire(new IgnoreListChangedEvent(null));
  }


  public void inject(VyletovyZperzistentnovac vyletovyZperzistentnovac) {
    this.vyletovyZperzistentnovac = vyletovyZperzistentnovac;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.program.Model0#initAndFire()
   */
  @Override
  protected void initAndFire() {
    setNewlyLoadedIgnoreList(new IgnoreList());
    boolean mameOtevritVylet = currPrefe().node(FPref.VYLET_node).getBoolean(FPref.JE_OTEVRENY_VYLET_value, false);
    File file = currPrefe().node(FPref.VYLET_node).getFile(FPref.AKTUALNI_SOUBOR_value, null);
    if (mameOtevritVylet && file != null && file.canRead()) {
      otevri(file);
    }
    fireCesta();
    fire(new PridavaniBoduEvent(false)); // přidávání neprobíhá, tak aŤ se provede příslušný event
  }

  public File defaultExportuDoGgt() {
    File file = new File(kesoidModel.getUmisteniSouboru().getGeogetDataDir().getEffectiveFile(), "lovim.ggt");
    return file;
  }


  public File defaultAktualnihoVyletuFile() {
    File defaultFile = new File(kesoidModel.getUmisteniSouboru().getKesDir().getEffectiveFile(), "Můj výlet.gpx");
    File file = currPrefe().node(FPref.VYLET_node).getFile(FPref.AKTUALNI_SOUBOR_value, defaultFile);
    return file;
  }

  private class Worker extends SwingWorker<Void, Void> {

    private final List<Kesoid> kese;



    public Worker(List<Kesoid> kese) {
      this.kese = kese;
    }

    /* (non-Javadoc)
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
    List<Kesoid> kesoidy = new ArrayList<Kesoid>();
    for (Wpt wpt : doc.getWpts()){
      kesoidy.add(wpt.getKesoid());
    }
    worker = new Worker(kesoidy);
    worker.execute();
  }


  public void pridejBodNaMisto(Mouable mouable) {
    removeFromIgnorList(mouable);
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
    removeFromIgnorList(mouable);
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
    if (bod == null) return;
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
    //    if (mouable instanceof Wpt) {
    //      Wpt wpt = (Wpt) mouable;
    //      wpt.invalidate();
    //    }
    //    if (bb.getMouable() instanceof Wpt) {
    //      Wpt wpt = (Wpt) bb.getMouable();
    //      wpt.invalidate();
    //    }
    // musime odstranit reference bodi na bod
    while (mouable instanceof Bod) {
      Bod bod = (Bod) mouable;
      mouable = bod.getMouable();
    }
    removeFromIgnorList(mouable);
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
    if (doc == null) return;
    for (Bod bod : doc.getBody()) {
      invalidate(bod);
    }
  }

  private void cleanCurta() {
    if (curta == null) return;
    for (Cesta cesta : doc.getCesty()) {
      if (cesta == curta) return;
    }
    curta = null;
  }

  private void fireCesta() {
    cleanCurta();
    poziceModel.refreshPozice();
    if (curta != null) {
      curta.kontrolaKonzistence();
    }
    fire(new VyletChangedEvent(doc, curta));
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
    osetriPrevzeninahranehoVyletuNaZStaralyAnoGgtFile();
  }


  public void otevri(File file ) {
    Doc doc = new Doc();
    doc.setFile(file);
    VyletOtevriSwingWorker wrk = new VyletOtevriSwingWorker(vyletovyZperzistentnovac, kesBag, this, file);
    wrk.execute();
  }

  public void importuj(List<File> files) {
    VyletImportSwingWorker wrk = new VyletImportSwingWorker(vyletovyZperzistentnovac, kesBag, this, files);
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
    if (this.curta == curta) return;
    this.curta = curta;
    fireCesta();
  }

  public void prepniVzdusnostUseku(Usek usek, boolean vzdusny) {
    if (usek.isVzdusny() == vzdusny) return;
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
    vyletovyZperzistentnovac.zapisGgt(doc, file);
  }

  private void setDefaultProAktualniVyletFile(File file) {
    currPrefe().node(FPref.VYLET_node).putFile(FPref.AKTUALNI_SOUBOR_value, file);
    currPrefe().node(FPref.VYLET_node).putBoolean(FPref.JE_OTEVRENY_VYLET_value, true);
    oznacZeUzSeNikdyNebudeNacitatAnoGgtFile();
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
    if (file == null)
      return getImplicitniVyletNovyFile();
    else {
      File result = najdiNeexistujiciSoubor(file);
      return result;
    }
  }

  public File getImplicitniVyletSaveCopyNovyFile() {
    File file = doc.getFile();
    if (file == null)
      return getImplicitniVyletNovyFile();
    else {
      File result = najdiNeexistujiciSoubor(new File(file.getParentFile(), "Kopie " + file.getName()));
      return result;
    }
  }

  private File najdiNeexistujiciSoubor(File aFile) {
    File dir = aFile.getParentFile();
    String pureName = aFile.getName();
    Pattern pat = Pattern.compile("^(.*?)(?: *\\(\\d+\\))?(\\.[^.]+)?$");
    Matcher mat = pat.matcher(pureName);
    System.out.println(pureName + ": " + mat.matches() + " -- " + pat);
    System.out.println(mat.group(1) + " * " + mat.group(2));
    //    String baseName = poz < 0 ? pureName : pureName.substring(0, poz);
    //    String extension = poz < 0 ? "" : pureName.substring(poz);
    String baseName = mat.group(1);
    String extension = mat.group(2);
    for (int i=0;;i++) {
      File file = new File(dir, baseName + (i==0 ? "" : " (" + i + ")") + extension);
      if (! file.exists()) return file;
    }

  }

  public void znovuVsechnoPripni() {
    vyletovyZperzistentnovac.pripniNaWayponty(doc.getCesty(), kesBag);
    fireCesta();
  }


  public void zavri() {
    invalidate(doc);
    doc = new Doc();
    currPrefe().node(FPref.VYLET_node).putBoolean(FPref.JE_OTEVRENY_VYLET_value, false);
    fireCesta();
  }

  private void oznacZeUzSeNikdyNebudeNacitatAnoGgtFile() {
    if (priPrvnimUlozeniSeZapiseZeZastaralySouborAnoGgtUzNebudeNikdyNacitan) {
      currPrefe().node(FPref.UMISTENI_SOUBORU_node).putBoolean(FPref.ZASTARALE_ANO_GGT_FILE_UZ_NENACITAT, true);
    }
  }

  public void vyresPripadneNahraniZastaralychVyletu() {
    if (probihaNahravaniZastaralehoAnoGgtFile || zastaralyAnoGgtFileBylNahran) return; // načtení proběhlo nebo probíhá
    boolean anoGgtFileUzNenacitat = currPrefe().node(FPref.UMISTENI_SOUBORU_node).getBoolean(FPref.ZASTARALE_ANO_GGT_FILE_UZ_NENACITAT, false);
    if (anoGgtFileUzNenacitat) return; // je řečeno, že se načítat nebude, protože uživatel soubor asi uložil
    Filex filex = currPrefe().node(FPref.UMISTENI_SOUBORU_node).getFilex(FPref.ZASTARALE_ANO_GGT_FILE_value, null);
    if (filex == null) return; // asi se jedná o novou instalaci, kde nebyla předchozí verze a tudíš není důvod nic načítat

    if (! filex.getEffectiveFile().canRead()) return; // soubor je sice nastaven, ale nelze číst, tak to zkusíme asi někdy jindy
    if (! doc.isEmpty()) return; // něco je načteno, tak nebudeme donačítat další soubor

    probihaNahravaniZastaralehoAnoGgtFile = true;
    zastaralySouborSVyletem = filex.getEffectiveFile();
    importuj(Collections.singletonList(filex.getEffectiveFile()));

  }

  private void osetriPrevzeninahranehoVyletuNaZStaralyAnoGgtFile() {
    if (!probihaNahravaniZastaralehoAnoGgtFile) return;
    zastaralyAnoGgtFileBylNahran = true;
    probihaNahravaniZastaralehoAnoGgtFile = false;
    if (doc.isEmpty()) return;
    Object[] options = {"Beru na vědomí",
    "Zobrazit nápovědu"};
    int n = JOptionPane.showOptionDialog(Dlg.parentFrame(),
        "<html>Byl nalezen výlet v souboru <b>\"" + zastaralySouborSVyletem + "\"</b><br> a byl nahrán, ulož si jej do GPX souboru.",
        "Převedení výletu",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);

    if (n == 1) {
      try {
        BrowserOpener.displayURL(new URL("http://wiki.geocaching.cz/wiki/Geokuk_-_pl%C3%A1nov%C3%A1n%C3%AD_v%C3%BDlet%C5%AF"));
      } catch (MalformedURLException e) { // no tak co
      }
    }

    //    Dlg.info("<html>Byl nalezen výlet v souboru <b>\"" + zastaralySouborSVyletem + "\"</b><br> a byl nahrán, ulož si jej do GPX souboru." +
    //        "<br><a href=\"http://wiki.geocaching.cz/wiki/Geokuk_-_pl%C3%A1nov%C3%A1n%C3%AD_v%C3%BDlet%C5%AF\">doku</a>", "Převedení výletu");
    // http://wiki.geocaching.cz/wiki/Geokuk_-_pl%C3%A1nov%C3%A1n%C3%AD_v%C3%BDlet%C5%AF
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
    if (cesta == null) return;
    curta = cesta;
    poziceModel.setPozice(cesta.getStart());
  }

  public void bezNaKonecCesty(Cesta cesta) {
    if (cesta == null) {
      cesta = doc.findNejblizsiCesta(moord.getMoustred());
    }
    if (cesta == null) return;
    curta = cesta;
    poziceModel.setPozice(cesta.getCil());
  }

  public void bezNaBod(Bod bod) {
    poziceModel.setPozice(bod);
  }


  public void bezNaBodVpred() {
    Bod bod = doc.findBod(poziceModel.getPoziceq().getPoziceMouable());
    if (bod != null) {
      if (! bod.isCil()) {
        poziceModel.setPozice(bod.getBvpred());
      }
    } else {
      bezNaZacatekCesty(curta);
    }
  }

  public void bezNaBodVzad() {
    Bod bod = doc.findBod(poziceModel.getPoziceq().getPoziceMouable());
    if (bod != null) {
      if (! bod.isStart()) {
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
    if (cesta.isKruh()) return;
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
    if (druhyBod == null) return;
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
    private Set<Wpt> puvodnici = new HashSet<Wpt>();

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
    if (this.probihaPridavani == probihaPridavani) return;
    this.probihaPridavani = probihaPridavani;
    fire(new PridavaniBoduEvent(probihaPridavani));
  }
}
