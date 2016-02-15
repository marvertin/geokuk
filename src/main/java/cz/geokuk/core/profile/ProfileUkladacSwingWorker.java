/**
 * 
 */
package cz.geokuk.core.profile;


import java.io.File;
import java.util.concurrent.ExecutionException;

import cz.geokuk.framework.MySwingWorker0;



/**
 * @author veverka
 *
 */
public class ProfileUkladacSwingWorker extends MySwingWorker0<File, Void> {



  public ProfileUkladacSwingWorker() {
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  public File doInBackground() throws Exception {
    File file = FPreferencesInNearFile.saveNearToProgramAndSwitchOn();
    return file;
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void donex() throws InterruptedException, ExecutionException {
  }


}

