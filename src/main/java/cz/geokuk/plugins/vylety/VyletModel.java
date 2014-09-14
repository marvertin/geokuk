/**
 * 
 */
package cz.geokuk.plugins.vylety;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;

/**
 * @author veverka
 *
 */
public class VyletModel extends Model0 {

  private Vylet vylet;

  private VyletovyZperzistentnovac vyletovyZperzistentnovac;

  private Worker worker;

  public void add(EVylet evyl, Kesoid kes) {
    EVylet evylPuvodni = vylet.add(evyl, kes);
    if (evyl != evylPuvodni) {
      VyletSaveSwingWorker worker = new VyletSaveSwingWorker(vyletovyZperzistentnovac, vylet);
      worker.execute();
      onChange(kes, evylPuvodni, evyl);
    }

  }

  public EVylet get(Kesoid kes) {
    return vylet.get(kes);
  }


  public void removeAll(EVylet evyl) {
    vylet.removeAll(evyl);
    VyletSaveSwingWorker worker = new VyletSaveSwingWorker(vyletovyZperzistentnovac, vylet);
    worker.execute();
    onChange(null, null, null);
  }

  public Set<Kesoid> get(EVylet evyl) {
    return vylet.get(evyl);
  }


  private void onChange(Kesoid kes, EVylet evylPuvodni, EVylet evyl) {
    if (!SwingUtilities.isEventDispatchThread()) return;
    fire(new VyletChangeEvent(this, kes, evyl, evylPuvodni));
  }

  /**
   * @param vsechny
   */
  public void startLoadingVylet(KesBag vsechny) {
    VyletLoadSwingWorker worker = new VyletLoadSwingWorker(vyletovyZperzistentnovac, vsechny, this);
    worker.execute();
  }

  public void setNewVylet(Vylet newvylet) {
    vylet = newvylet;
    fire(new VyletChangeEvent(this, null, null, null));
  }


  public void inject(VyletovyZperzistentnovac vyletovyZperzistentnovac) {
    this.vyletovyZperzistentnovac = vyletovyZperzistentnovac;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.program.Model0#initAndFire()
   */
  @Override
  protected void initAndFire() {
    setNewVylet(new Vylet());
  }

  public void nasypVypetDoGeogetu() {
    if (worker != null) {
      worker.cancel(true);
    }
    worker = new Worker(new ArrayList<>(get(EVylet.ANO)));
    worker.execute();
  }

  private class Worker extends SwingWorker<Void, Void> {

    private final List<Kesoid> kese;



    public Worker(List<Kesoid> kese) {
      this.kese = kese;
    }

    /* (non-Javadoc)
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground() throws Exception {
      Clipboard scl = getSystemClipboard();
      for (Kesoid kes : kese) {
        if (isCancelled()) {
          break;
        }
        scl.setContents(new StringSelection(kes.getUrlShow().toExternalForm()), null);
        Thread.sleep(100);
      }
      return null;
    }

  }

}
