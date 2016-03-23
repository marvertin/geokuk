package cz.geokuk.framework;

public class ProgressEvent extends Event0<ProgressModel> {

	private final boolean		visible;
	private final int			progress;
	private final int			max;
	private final String		text;
	private final String		tooltip;
	private final Progressor	progressor;

	/**
	 * @param visible
	 * @param progress
	 * @param max
	 * @param text
	 * @param tooltip
	 */
	public ProgressEvent(final Progressor progressor, final boolean visible, final int progress, final int max, final String text, final String tooltip) {
		super();
		this.progressor = progressor;
		this.visible = visible;
		this.progress = progress;
		this.max = max;
		this.text = text;
		this.tooltip = tooltip;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @return the progress
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * @return the max
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	@Override
	public String toString() {
		return "ProgressEvent " + System.identityHashCode(progressor) + "[visible=" + visible + ", progress=" + progress + ", max=" + max + ", text=" + text + ", tooltip=" + tooltip + "]";
	}

	/**
	 * @return the progressor
	 */
	public Progressor getProgressor() {
		return progressor;
	}

}
