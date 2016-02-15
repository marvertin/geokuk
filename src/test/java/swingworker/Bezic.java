package swingworker;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

/**
 * 
 */

/**
 * @author veverka
 *
 */
public class Bezic extends SwingWorker<String, String> {


  public boolean vyhodisx;

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected String doInBackground() throws Exception {
    for (int i = 0; i < 100; i++) {
      publish(i + "");
      System.out.println("Jedeme: " + i);
      if (vyhodisx) throw new IllegalArgumentException("Vyhozeno uvnitr");
      Thread.sleep(50);
    }
    return "Hotovo";
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#process(java.util.List)
   */
  @Override
  protected void process(List<String> aChunks) {
    System.out.println(aChunks.size());
    for (String s : aChunks) {
      JHlavni.l.setText("Bezime: " +s);
    }

  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void done() {
    try {
      System.out.println("KONCIME1: ");
      System.out.println("KON: " + get());
      System.out.println("KONCIME2: ");

      JHlavni.l.setText(get());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
