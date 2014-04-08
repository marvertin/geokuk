/**
 * 
 */
package cz.geokuk.plugins.cesty;


import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.cesty.data.Doc;
import cz.geokuk.plugins.kesoid.KesBag;



/**
 * @author veverka
 *
 */
public class CestyOtevriSwingWorker extends MySwingWorker0<Doc, Void> {


  private final CestyZperzistentnovac cestyZperzistentnovac;
  private final KesBag kesBag;
  private final CestyModel cestyModel;
  private final File file;


  public CestyOtevriSwingWorker(CestyZperzistentnovac cestyZperzistentnovac, KesBag vsechny, CestyModel cestyModel, File file) {
    this.cestyZperzistentnovac = cestyZperzistentnovac;
    kesBag = vsechny;
    this.cestyModel = cestyModel;
    this.file = file;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Doc doInBackground() throws Exception {
    Doc doc = new Doc();
    doc.setFile(file);
    List<Cesta> cesty = cestyZperzistentnovac.nacti(Collections.singletonList(file), kesBag);
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
    cestyModel.prevezmiNoveOtevrenyDokument(doc);
  }


}

