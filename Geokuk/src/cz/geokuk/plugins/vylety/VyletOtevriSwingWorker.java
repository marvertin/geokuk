/**
 * 
 */
package cz.geokuk.plugins.vylety;


import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.vylety.cesty.Cesta;
import cz.geokuk.plugins.vylety.cesty.Doc;



/**
 * @author veverka
 *
 */
public class VyletOtevriSwingWorker extends MySwingWorker0<Doc, Void> {


  private final VyletovyZperzistentnovac vyletovyZperzistentnovac;
  private final KesBag kesBag;
  private final VyletModel vyletModel;
  private final File file;


  public VyletOtevriSwingWorker(VyletovyZperzistentnovac vyletovyZperzistentnovac, KesBag vsechny, VyletModel vyletModel, File file) {
    this.vyletovyZperzistentnovac = vyletovyZperzistentnovac;
    kesBag = vsechny;
    this.vyletModel = vyletModel;
    this.file = file;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Doc doInBackground() throws Exception {
    Doc doc = new Doc();
    doc.setFile(file);
    List<Cesta> cesty = vyletovyZperzistentnovac.nacti(Collections.singletonList(file), kesBag);
    for (Cesta cesta : cesty) {
      doc.xadd(cesta);
    }
    return doc;
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void donex() throws InterruptedException, ExecutionException {
    Doc doc = get();
    if (doc == null) return; // asi zkanclváno
    System.out.printf("Načten dokument %s: \n",  doc.getFile());
    vyletModel.prevezmiNoveOtevrenyDokument(doc);
  }


}

