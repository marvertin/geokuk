/**
 *
 */
package cz.geokuk.framework;

/**
 * @author Martin Veverka
 *
 */
public interface Progressor {

	public void addProgress(int progress);

	public void finish();

	/**
	 * @return
	 */
	public int getProgress();

	public void incProgress();

	public void setMax(int max);

	public void setProgress(int progress);

	public void setText(String text);

	public void setTooltip(String text);

}
