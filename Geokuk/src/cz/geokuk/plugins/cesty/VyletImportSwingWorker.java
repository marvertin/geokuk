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
public class VyletImportSwingWorker extends MySwingWorker0<List<Cesta>, Void> {


  private final VyletovyZperzistentnovac vyletovyZperzistentnovac;
  private final KesBag kesBag;
  private final VyletModel vyletModel;
  private final List<File> files;


  public VyletImportSwingWorker(VyletovyZperzistentnovac vyletovyZperzistentnovac, KesBag vsechny, VyletModel vyletModel, List<File> files) {
    this.vyletovyZperzistentnovac = vyletovyZperzistentnovac;
    kesBag = vsechny;
    this.vyletModel = vyletModel;
    this.files = files;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public List<Cesta> doInBackground() throws Exception {
    List<Cesta> cesty = vyletovyZperzistentnovac.nacti(files, kesBag);
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
    vyletModel.prevezmiImportovaneCesty(cesty);
  }


}

