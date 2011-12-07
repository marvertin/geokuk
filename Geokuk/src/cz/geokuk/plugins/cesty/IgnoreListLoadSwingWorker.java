/**
 * 
 */
package cz.geokuk.plugins.cesty;


import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.kesoid.KesBag;



/**
 * @author veverka
 *
 */
public class IgnoreListLoadSwingWorker extends MySwingWorker0<IgnoreList, Void> {


  private final CestyZperzistentnovac cestyZperzistentnovac;
  private final KesBag vsechny;
  private final CestyModel cestyModel;


  public IgnoreListLoadSwingWorker(CestyZperzistentnovac cestyZperzistentnovac2, KesBag vsechny, CestyModel cestyModel) {
    cestyZperzistentnovac = cestyZperzistentnovac2;
    this.vsechny = vsechny;
    this.cestyModel = cestyModel;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public IgnoreList doInBackground() throws Exception {
    return cestyZperzistentnovac.immediatlyNactiIgnoreList(vsechny);
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void donex() throws InterruptedException, ExecutionException {
    IgnoreList result = get();
    if (result == null) return; // asi zkanclváno
    System.out.printf("Nahran ignorelist %d ignorovanych: \n",
        result.getIgnoreList().size());
    cestyModel.setNewlyLoadedIgnoreList(result);
  }


}

