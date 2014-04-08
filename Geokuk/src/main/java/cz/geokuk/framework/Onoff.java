/**
 * 
 */
package cz.geokuk.framework;


/**
 * @author veverka
 *
 */
public class Onoff<T extends Model0> {
  
  private final Class<? extends OnoffEvent0<T>> clazz;
  private final T model;

  private Boolean onoff;
  
  public void setOnoff(boolean onoff) {
    try {
      if (this.onoff != null && this.onoff == onoff) return;
      this.onoff = onoff;
      onSetOnOff(onoff); // nastavit do modelu, pokud model toto požaduje, například za čelem zperzostentnění
      if (clazz != null) {
        OnoffEvent0<T> event = clazz.newInstance();
        event.setModel(model);
        event.onoff = onoff;
        model.fire(event);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  
  }

  /**
   * @param model
   * @param clazz
   */
  public Onoff(T model, Class<? extends OnoffEvent0<T>> clazz) {
    super();
    this.model = model;
    this.clazz = clazz;
  }

  protected void onSetOnOff(boolean onoff) {
    
  }

  /**
   * @return
   */
  public boolean isOnoff() {
    return onoff;
  }

}
