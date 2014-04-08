/**
 * 
 */
package cz.geokuk.plugins.cesty;


import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.kesoid.KesBag;



/**
 * @author veverka
 *
 */
public class CestyImportSwingWorker extends MySwingWorker0<List<Cesta>, Void> {


  private final CestyZperzistentnovac cestyZperzistentnovac;
  private final KesBag kesBag;
  private final CestyModel cestyModel;
  private final List<File> files;


  public CestyImportSwingWorker(CestyZperzistentnovac cestyZperzistentnovac, KesBag vsechny, CestyModel cestyModel, List<File> files) {
    this.cestyZperzistentnovac = cestyZperzistentnovac;
    kesBag = vsechny;
    this.cestyModel = cestyModel;
    this.files = files;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public List<Cesta> doInBackground() throws Exception {
    List<Cesta> cesty = cestyZperzistentnovac.nacti(files, kesBag);
    return cesty;
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void donex() throws InterruptedException, ExecutionException {
    List<Cesta> cesty = get();
    if (cesty == null) return; // asi zkanclváno
    System.out.printf("Načteny cesty %d: \n",  cesty.size());
    cestyModel.prevezmiImportovaneCesty(cesty);
  }


}

