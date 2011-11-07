/**
 * 
 */
package cz.geokuk.plugins.vylety;


import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.kesoid.KesBag;



/**
 * @author veverka
 *
 */
public class IgnoreListLoadSwingWorker extends MySwingWorker0<IgnoreList, Void> {


  private final VyletovyZperzistentnovac vyletovyZperzistentnovac;
  private final KesBag vsechny;
  private final VyletModel vyletModel;


  public IgnoreListLoadSwingWorker(VyletovyZperzistentnovac vyletovyZperzistentnovac2, KesBag vsechny, VyletModel vyletModel) {
    vyletovyZperzistentnovac = vyletovyZperzistentnovac2;
    this.vsechny = vsechny;
    this.vyletModel = vyletModel;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public IgnoreList doInBackground() throws Exception {
    return vyletovyZperzistentnovac.immediatlyNactiIgnoreList(vsechny);
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
    vyletModel.setNewlyLoadedIgnoreList(result);
  }


}

