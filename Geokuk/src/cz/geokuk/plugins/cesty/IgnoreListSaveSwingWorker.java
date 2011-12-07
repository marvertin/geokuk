/**
 * 
 */
package cz.geokuk.plugins.cesty;


import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;

/**
 * @author veverka
 *
 */
public class IgnoreListSaveSwingWorker extends MySwingWorker0<IgnoreList, Void> {


  private final CestyZperzistentnovac cestyZperzistentnovac;
  private final IgnoreList vylet;


  public IgnoreListSaveSwingWorker(CestyZperzistentnovac cestyZperzistentnovac, IgnoreList vylet) {
    this.cestyZperzistentnovac = cestyZperzistentnovac;
    this.vylet = vylet;
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected IgnoreList doInBackground() throws Exception {
    cestyZperzistentnovac.immediatlyZapisIgnoreList(vylet);
    return null;
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void donex() throws InterruptedException, ExecutionException {
    if (isCancelled()) return;
    IgnoreList result = get();
    if (result == null) return; // asi zkanclváno
    System.out.printf("Uložen ignorelist %d ignorovanych: \n",
        result.getIgnoreList().size());
    //Board.eveman.fire(new VyletChangeEvent(result, null, null, null));
  }

}

