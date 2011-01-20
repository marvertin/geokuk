/**
 * 
 */
package cz.geokuk.plugins.kesoid.mvc;


import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mapicon.Alela;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;


/**
 * @author veverka
 *
 */
public class SwitchKesoidUrciteAlelyAction extends ToggleAction0 implements AfterEventReceiverRegistrationInit {

  private static final long serialVersionUID = -8054017274338240706L;
  private IkonBag ikonBag;
  private final Alela alela;
  private KesoidModel kesoidModel;
  private KesBag vsechny;

  /**
   * 
   */
  public SwitchKesoidUrciteAlelyAction(Alela alela) {
    super("Zapnuti vypnuti alely");
    this.alela = alela;
    //putValue(MNEMONIC_KEY, InputEvent.)
    //   putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */

  public void onEvent(IkonyNactenyEvent event) {
    ikonBag = event.getBag();
  }

  public void onEvent(KeskyNactenyEvent event) {
    vsechny = event.getVsechny();
  }

  /* (non-Javadoc)
   * @see cz.geokuk.program.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    super.putValue(NAME, sestavJmeno());
    super.putValue(SMALL_ICON,  ikonBag.seekIkon(ikonBag.getGenom().getGenotypProAlelu(alela)));
    super.putValue(SHORT_DESCRIPTION, sestavJmeno());
  }

  private String sestavJmeno() {
    return String.format("<html>%s: <b>%s</b> <i>(%d)</i>",
        alela.getGen().getDisplayName(),
        alela.getDisplayName(),
        vsechny.getPoctyAlel().count(alela));
  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }

  public void onEvent(KeskyVyfiltrovanyEvent event) {
    boolean nechtena = event.getModel().getFilter().getJmenaNechtenychAlel().contains(alela.toString());
    setSelected(! nechtena);
    super.putValue(SHORT_DESCRIPTION, sestavJmeno());
  }

  @Override
  protected void onSlectedChange(boolean nastaveno) {
    kesoidModel.filtrujDleAlely(alela.toString(), nastaveno);
  }



}



