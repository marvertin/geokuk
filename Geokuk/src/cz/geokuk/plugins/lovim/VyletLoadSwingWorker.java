/**
 * 
 */
package cz.geokuk.plugins.lovim;


import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.kesoid.KesBag;



/**
 * @author veverka
 *
 */
public class VyletLoadSwingWorker extends MySwingWorker0<Vylet, Void> {


  private final VyletovyZperzistentnovac vyletovyZperzistentnovac;
  private final KesBag vsechny;
  private final VyletModel vyletModel;


  public VyletLoadSwingWorker(VyletovyZperzistentnovac vyletovyZperzistentnovac2, KesBag vsechny, VyletModel vyletModel) {
    this.vyletovyZperzistentnovac = vyletovyZperzistentnovac2;
    this.vsechny = vsechny;
    this.vyletModel = vyletModel;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public Vylet doInBackground() throws Exception {
    return vyletovyZperzistentnovac.immediatlyNactiVylet(vsechny);
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void donex() throws InterruptedException, ExecutionException {
    Vylet result = get();
    if (result == null) return; // asi zkanclváno
    System.out.printf("Nahran vylet, %d lovenych a %d ignorovanych: \n",
        result.get(EVylet.ANO).size(), 
        result.get(EVylet.NE).size());
    vyletModel.setNewVylet(result);
  }


}

