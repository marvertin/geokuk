package cz.geokuk.plugins.mapy.stahovac;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JTextPane;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.VyrezChangedEvent;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.program.ZobrazServisniOknoAction;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.framework.MySwingWorker0;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.*;
import cz.geokuk.plugins.mapy.kachle.Priority;

public class JKachleOflinerDialog extends JMyDialog0 implements AfterEventReceiverRegistrationInit {

  private static final long serialVersionUID = 7180968190465321695L;

  private static final int LIMIT_DLAZDIC = 100000;

  private JTextPane uvod;
  private JButton spustit;

  private Coord moord;


  private TotoSeTaha totoSeTaha;

  private KachleOflinerSwingWorker kosw;

  private KachleModel kachleModel;

  private KaSet xkaSet;

  private int xminmoumer;

  private ZobrazServisniOknoAction zobrazServisniOknoAction;

  public JKachleOflinerDialog() {
    setTitle("Hromadné dotažení mapových dlaždic");
    init();
  }

  private void registerEvents() {

    spustit.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (kosw != null) kosw.cancel(true);
        zobrazServisniOknoAction.actionPerformed(null);
        dispose();
        kosw = new KachleOflinerSwingWorker(true);
        kosw.execute();
      }
    });


  }

  public void onEvent(VyrezChangedEvent event) {
    moord = event.getMoord();
    prepocetKachli();
  }

  private void prepocetKachli() {
    if (totoSeTaha != null) {
      if (kosw != null) kosw.cancel(true);
      kosw = new KachleOflinerSwingWorker(false);
      nastavCudl(0);
      kosw.execute();
    }
  }


  @Override
  protected void initComponents() {
    // Napřed registrovat, aby při inicializaci už byl výsledek tady
    Box box = Box.createVerticalBox();
    add(box);

    uvod = new JTextPane();
    uvod.setContentType("text/html");
    uvod.setText("Tady bude kecání");
    uvod.setPreferredSize(new Dimension(400, 200));
    uvod.setAlignmentX(CENTER_ALIGNMENT);

    spustit = new JButton("Vyberte nějaký výřez");
    spustit.setEnabled(false);
    spustit.setAlignmentX(CENTER_ALIGNMENT);

    box.add(uvod);
    box.add(Box.createVerticalStrut(10));
    box.add(spustit);

  }

  /**
   * @return
   */
  private String pokecani() {
    return String.format(
        "<html>Budou stahovány dlaždice mapových pokladů <b>%s</b> v rozmění měřítek " +
        " <b>&lt;%d,%d&gt;</b>" +
        " nyní natavte v hlavním okně výřez mapy který chcete stáhnout. Výřez můžete" +
        " nastavit v libovolném měřítku a v na libovolném mapovém podkladu. " +
        " Pak spusťte stahování tlačítkem. Stahování poběží na pozadí. V servisním okně lze sledovat," +
        " jak se zkracují frony. Stahování nelze zastavit jinak než ukončením programu.",
        totoSeTaha.kaSet.getKts(),
        totoSeTaha.minmoumer,
        totoSeTaha.maxmoumer);
  }

  private void nastavCudl(int pocetDlazdic)  {
    boolean b = pocetDlazdic > 0 && pocetDlazdic <= LIMIT_DLAZDIC;
    spustit.setEnabled(b);
    if (b) {
      spustit.setText(String.format("Stáhnout %d dlaždic do cache",  pocetDlazdic));
    } else {
      if (pocetDlazdic == 0) {
        spustit.setText("Počítáme dlaždice");
      } else {
        spustit.setText("Spočítáno " + pocetDlazdic + ", limit je " + LIMIT_DLAZDIC);
      }

    }
  }

  public void onEvent(ZmenaMapNastalaEvent event) {
    xkaSet = event.getKaSet();
    xminmoumer = xkaSet.getPodklad().getMinMoumer();
    prepocetKachli();
  }

  public class KachleOflinerSwingWorker extends MySwingWorker0<Integer, Kachle> {

    private final boolean zafrontovat;

    KachleOflinerSwingWorker(boolean zafrontovat) {
      this.zafrontovat = zafrontovat;
    }

    @Override
    protected Integer doInBackground() throws Exception {
      int moumer = moord.getMoumer(); // aktuální měřítko
      int w = moord.getWidth();
      int h = moord.getHeight();
      // nastavit na najvětší měřítko
      while (moumer <= totoSeTaha.maxmoumer) {
        w *= 2;
        h *= 2;
        moumer++;
      }

      // a teď projet směrem nahoru
      int pocetKachli = 0;
      while (moumer > totoSeTaha.minmoumer) {
        w /= 2;
        h /= 2;
        moumer--;
        Coord coco = new Coord(moumer, moord.getMoustred(), new Dimension(w,h), 0.0);

        int xn = w;
        //int yn = h;
        int moumaska = ~ (coco.getMoukrok()-1);
        Mou mou0 = new Mou(coco.getMouJZ().xx & moumaska, coco.getMouJZ().yy & moumaska);
        Mou mou = new Mou(mou0);
        while (coco.transform(mou).x < xn) {
          if (isCancelled()) return -1;
          while (coco.transform(mou).y > 0) {
            KaLoc kaloc = KaLoc.ofSZ(new Mou(mou), coco.getMoumer());
            if (zafrontovat) {
              Kachle kachle = new Kachle(new KaAll(kaloc, totoSeTaha.kaSet), kachleModel, false, null); // když se nepoužije, musí se stvořit nová
              
              kachle.setVzdalenostOdStredu(coco.getVzdalenostKachleOdStredu(kaloc.getMouSZ()));
              publish(kachle);
            }
            pocetKachli += totoSeTaha.kaSet.getKts().size(); // po každý podklad jedna kachle
            mou = mou.add(0, coco.getMoukrok());
          }
          mou = mou.add(coco.getMoukrok(), 0);
        }
      }
      return pocetKachli;
    }

    @Override
    protected void process(List<Kachle> chunks) {
      for (Kachle kachle : chunks) {
        kachle.ziskejObsah(Priority.STAHOVANI);
      }
    }
    @Override
    protected void donex() throws Exception {
      if (! isCancelled()) {
        Integer pocet = get();
        nastavCudl(pocet);
      }
    }

  }

  public void inject(KachleModel kachleModel) {
    this.kachleModel = kachleModel;
  }

  private class TotoSeTaha {
    private int minmoumer;
    private int maxmoumer;
    private KaSet kaSet;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
   */
  @Override
  public void initAfterEventReceiverRegistration() {
    totoSeTaha = new TotoSeTaha();
    totoSeTaha.maxmoumer = moord.getMoumer(); // zapamatovat si měřítko, do něj budeme kachlovat
    totoSeTaha.minmoumer = xminmoumer;
    totoSeTaha.kaSet = xkaSet;
    registerEvents();
    prepocetKachli();
    uvod.setText(pokecani());
  }

  public void inject(ZobrazServisniOknoAction zobrazServisniOknoAction) {
    this.zobrazServisniOknoAction = zobrazServisniOknoAction;
  }

  @Override
  protected String getTemaNapovedyDialogu() {
    return "StahovaniMapovychDlazdic";
  }


}
