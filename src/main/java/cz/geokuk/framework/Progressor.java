/**
 * 
 */
package cz.geokuk.framework;

/**
 * @author veverka
 *
 */
public interface Progressor {

  public void setProgress(int progress);

  public void incProgress();

  public void addProgress(int progress);

  public void setMax(int max);

  public void setText(String text);

  public void setTooltip(String text);

  public void finish();

  /**
   * @return
   */
  public int getProgress();

}
