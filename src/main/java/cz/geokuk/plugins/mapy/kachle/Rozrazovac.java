package cz.geokuk.plugins.mapy.kachle;


import java.awt.Image;

import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloRoste;




public class Rozrazovac implements Runnable {

  
  private final Pocitadlo pocitRozrazeniNaMensi = new PocitadloRoste("Rozřazení požadavku na celou kachli na jednotlivé",
  "");
  
  private KachleModel kachleModel;
  

  @Override
  public void run() {
    for (;;) { // toto vlákno neskončí
      try {
        kachleModel.pocitRozrazovaciQueue.set(kachleModel.rozrazovaciQueue.size());
        Ka0Req ka0req = kachleModel.rozrazovaciQueue.take();
        if (!ka0req.jesteToChceme()) continue; // když to nehceme, přeskakujeme
        kachleModel.pocitRozrazovaciQueue.set(kachleModel.rozrazovaciQueue.size());
        if (ka0req instanceof KaOneReq) {
          KaOneReq klr = (KaOneReq) ka0req;
          if (kachleModel.cache.isOnDiskOrMemory(klr.getKa())) {
            kachleModel.fromFileQueue.add(klr);
          } else {
            kachleModel.downLoadQueue.add(klr);
          }
        } else if (ka0req instanceof KaAllReq) {
          KaAllReq kaAllReq = (KaAllReq)ka0req;
          KaAll kaAll = kaAllReq.getKa();
          ImageReceiver imgrcv = kaAllReq.getImageReceiver();
          // rozhodit na jednotlivosti
          pocitRozrazeniNaMensi.inc();
          DlazebniKombiner dk = new DlazebniKombiner(kaAll.kaSet.getKts());
          DlazebniPosilac dp = new DlazebniPosilac(dk, imgrcv);
          for (EKaType type : kaAll.kaSet.getKts()) {
            KaOne kaOne = new KaOne(kaAll.getLoc(), type);
            Image img = kachleModel.cache.memoryCachedImage(kaOne);
            if (imgrcv != null) {
              if (img != null) {
                dp.add(type, img);
              } else {
                KaOneReq kaOneReq = new KaOneReq(kaOne, kaAllReq, dp, kaAllReq.getPriorita());
                kaOneReq.setVzdalenostOdStredu(kaAllReq.getVzdalenostOdStredu()); // musíme přenést kvůli řazení
                kachleModel.rozrazovaciQueue.add(kaOneReq);
              }
            }
          }
        } else {
          throw new RuntimeException("Toto jeste neumime");
        }
        //Thread.sleep(30);
      } catch (InterruptedException e) { // tak přerušil a já jedu dál
        FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Přerušení metodou interrupt(), taková výjimka asi nikdy nenastane.");
      }
    }

  }

  public void inject(KachleModel kachleModel) {
    this.kachleModel = kachleModel;
  }
  
}
