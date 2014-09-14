package cz.geokuk.core.hledani;


import java.util.concurrent.ExecutionException;
import java.util.regex.PatternSyntaxException;

import cz.geokuk.core.hledani.HledaciSluzba.Finishor;
import cz.geokuk.framework.MySwingWorker0;


class HledaciRunnableSwingWorker<T extends Nalezenec0> extends MySwingWorker0<VysledekHledani<T>, Void> {


  private final HledaciPodminka0 podminka;
  private final Hledac0<T> hledac;
  private final Finishor<T> finishor;


  public HledaciRunnableSwingWorker(Finishor<T> finishor,
      HledaciPodminka0 podminka, Hledac0<T> hledac) {
    super();
    this.finishor = finishor;
    this.podminka = podminka;
    this.hledac = hledac;
  }

  @Override
  protected VysledekHledani<T> doInBackground() throws Exception {
    VysledekHledani<T> result = new VysledekHledani<>();
    hledac.setFuture(this);
    try {
      result.nalezenci = hledac.najdiASerad(podminka);
    } catch (PatternSyntaxException e) {
      result.exception = e;
    }
    if (isCancelled()) return null;
    return result;
  }

  @Override
  protected void donex() throws InterruptedException, ExecutionException  {
    VysledekHledani<T> vysledekHledani = null;
    if (! isCancelled()) vysledekHledani = get();
    finishor.finish(vysledekHledani);
  }







}
