/**
 * 
 */
package cz.geokuk.core.coord;


import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coordinates.ESmer;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.Action0;


/**
 * @author veverka
 *
 */
public class PosouvejSipkamiAction extends Action0 {


  private static final long serialVersionUID = -8054017274338240706L;
  private final boolean daleko;
  private final int dx;
  private final int dy;

  private static final int KROK_MALY = 10;

  /**
   * 
   */
  public PosouvejSipkamiAction(ESmer smer, boolean daleko) {
    this.daleko = daleko;

    String zrychlovac = daleko ? "ctrl " : "";
    switch(smer) {
    case SEVER:
      putValue(NAME, "Severně");
      putValue(SHORT_DESCRIPTION, "Posun mapy severně.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(zrychlovac +"UP"));
      dx = 0;
      dy = 1;
      break;
    case VYCHOD:
      putValue(NAME, "Východně");
      putValue(SHORT_DESCRIPTION, "Posun mapy východně.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(zrychlovac +"RIGHT"));
      dx = 1;
      dy = 0;
      break;
    case JIH:
      putValue(NAME, "Jižně");
      putValue(SHORT_DESCRIPTION, "Posun mapy jižně.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(zrychlovac + "DOWN"));
      dx = 0;
      dy = -1;
      break;
    case ZAPAD:
      putValue(NAME, "Západně");
      putValue(SHORT_DESCRIPTION, "Posun mapy západně.");
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(zrychlovac +"LEFT"));
      dx = -1;
      dy = 0;
      break;

    default : throw new RuntimeException();
    }

  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */

  @Override
  public void actionPerformed(ActionEvent e) {
    Coord moord = vyrezModel.getMoord();
    Mou mou = moord.getMoustred();
    int dxx = daleko ?  dx * moord.getMouSize().dxx / 2 :  dx * moord.getPomer() * KROK_MALY;
    int dyy = daleko ?  dy * moord.getMouSize().dyy / 2 :  dy * moord.getPomer() * KROK_MALY;

    moord.getMouSize();
    Mou moustred = new Mou(mou.xx + dxx, mou.yy + dyy);
    vyrezModel.presunMapuNaMoustred(moustred);
  }

  public void onEvent(VyrezChangedEvent event) {
    //    setEnabled (! event.getModel().jeNejvzdaLenejsiMeritko());
  }

}



