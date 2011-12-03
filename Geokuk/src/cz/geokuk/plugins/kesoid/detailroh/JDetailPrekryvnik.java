/**
 * 
 */
package cz.geokuk.plugins.kesoid.detailroh;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import cz.geokuk.core.coord.JCoordPrekryvnik0;
import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.Poziceq;
import cz.geokuk.core.coord.ZmenaSouradnicMysiEvent;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.vylety.PridavaniBoduEvent;

/**
 * @author veverka
 *
 */
public class JDetailPrekryvnik extends JCoordPrekryvnik0 {
  private static final int SPOZDENI_ZOBRAZENI_DETAILU = 100;
  private static final int DETAIL_MOUMER = 17;
  private static final long serialVersionUID = -5996655830197513951L;
  private Poziceq poziceq;
  private boolean probihaPridavani;
  private Mou moucur;

  // Timer, který spožďuje vykreslení detailu,k dyž jen hejbeme myší se stisknutým controlem
  private Timer zpozdovaciTimer;

  /**
   * 
   */
  public JDetailPrekryvnik() {
  }

  public void onEvent(ZmenaSouradnicMysiEvent aEvent) {
    moucur = aEvent.upravenaMys == null ? aEvent.moucur : aEvent.upravenaMys.getMou();
    nastav();
  }

  public void onEvent(PridavaniBoduEvent aEvent) {
    probihaPridavani = aEvent.probihaPridavani;
    nastav();
  }

  public void onEvent(PoziceChangedEvent aEvent) {
    poziceq = aEvent.poziceq;
    nastav();
  }

  private void nastav() {
    if (probihaPridavani) {
      if (moucur != null) {
        nastavSeSpozdenim();
        repaint(); // musíme překreslit při změně středu
        setVisible(true);
      } else {
        nastavNaPozici();
      }
    } else {
      nastavNaPozici();
    }

  }

  private void nastavSeSpozdenim() {

    if (zpozdovaciTimer != null) {
      zpozdovaciTimer.stop();
    }
    zpozdovaciTimer = new Timer(SPOZDENI_ZOBRAZENI_DETAILU, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        zpozdovaciTimer = null;
        if (moucur != null) {
          setSoord(getSoord().derive(DETAIL_MOUMER, moucur));
        }
      }
    });
    zpozdovaciTimer.setRepeats(false);
    zpozdovaciTimer.start();
  }

  private void nastavNaPozici() {
    if (poziceq == null || poziceq.isNoPosition()) {
      setVisible(false);
    } else {
      setSoord(getSoord().derive(DETAIL_MOUMER, poziceq.getWgs().toMou()));
      repaint(); // musíme překreslit při změně středu
      setVisible(true);
    }
  }



}
