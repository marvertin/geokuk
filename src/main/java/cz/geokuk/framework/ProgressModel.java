/**
 *
 */
package cz.geokuk.framework;

import javax.swing.SwingUtilities;


/**
 * @author veverka
 *
 */
public class ProgressModel extends Model0 {


	/* (non-Javadoc)
	 * @see cz.geokuk.framework.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
	}

	public Progressor start(int max, String text) {
		SimpleNotParalelProgressor progressor = new SimpleNotParalelProgressor();
		progressor.max = max;
		progressor.text = text;
		return progressor;
	}


	private class SimpleNotParalelProgressor implements Progressor {

		private int progress;
		private int max;
		private String text = "";
		private String tooltip = "";
		private boolean visible;
		private final long startTime = System.currentTimeMillis();


		private static final long millisToDecideToPopup = 500;
		private static final long millisToPopup = 2000;

		/**
		 *
		 */
		private static final int MINIMALNI_DOBA_MEZI_DVEVA_POSUNY_PROGRESORU = 101;
		private long lastFireTime;

		/* (non-Javadoc)
		 * @see nove.Progressor#addProgress(int)
		 */
		@Override
		public void addProgress(int aProgress) {
			setProgress(progress + aProgress);
		}

		/* (non-Javadoc)
		 * @see nove.Progressor#incProgress()
		 */
		@Override
		public void incProgress() {
			addProgress(1);
		}

		/* (non-Javadoc)
		 * @see nove.Progressor#setMax(int)
		 */
		@Override
		public void setMax(int aMax) {
			if (max == aMax) return;
			max = aMax;
			firex(false);
		}

		/* (non-Javadoc)
		 * @see nove.Progressor#setProgress(int)
		 */
		@Override
		public void setProgress(int nv) {
			progress = nv;
			if (nv >= max) {
				finish();
			} else {
				if (visible) {
					firex(false);
				} else {
					long T = System.currentTimeMillis();
					long dT = T-startTime;
					if (dT >= millisToDecideToPopup) {
						long predictedCompletionTime;
						if (nv > 0) {
							predictedCompletionTime = (int)(dT *
									(max) /
									(nv));
						} else {
							predictedCompletionTime = millisToPopup;
						}
						if (predictedCompletionTime >= millisToPopup) {
							visible = true;
							firex(true);
						}
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see nove.Progressor#finish()
		 */
		@Override
		public void finish() {
			progress = 0;
			visible = false;
			lastFireTime = 0; // při ukončení musíme vždy poslat
			firex(true);
		}


		/* (non-Javadoc)
		 * @see nove.Progressor#setText(java.lang.String)
		 */
		@Override
		public void setText(String aText) {
			if (text.equals(aText)) return;
			text = aText;
			firex(true);
		}

		/* (non-Javadoc)
		 * @see nove.Progressor#setTooltip(java.lang.String)
		 */
		@Override
		public void setTooltip(String aTooltip) {
			if (tooltip.equals(aTooltip)) return;
			tooltip = aTooltip;
			firex(true);
		}

		/**
		 *
		 */
		void firex(boolean allways) {
			long T = System.currentTimeMillis();
			if (allways || T - lastFireTime > MINIMALNI_DOBA_MEZI_DVEVA_POSUNY_PROGRESORU) {
				final ProgressEvent event = new ProgressEvent(this, visible, progress, max, text, tooltip);
				lastFireTime = T;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						//System.out.println("PROEVE " + event);
						fire(event);
					}
				});
			}
		}

		/* (non-Javadoc)
		 * @see cz.geokuk.framework.Progressor#getProgress()
		 */
		@Override
		public int getProgress() {
			return progress;
		}

	}


}
