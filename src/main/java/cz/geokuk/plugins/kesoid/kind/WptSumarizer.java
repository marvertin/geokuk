package cz.geokuk.plugins.kesoid.kind;

import cz.geokuk.plugins.kesoid.Wpt;

/**
 * Strkají se sem waypointy, aby bylo možné sumarizovat a podle toho
 * zhasínat a rožínat třeba filtry.
 * @author Martin
 *
 */
public interface WptSumarizer {

	public void initLoading();
	public void afterLoaded(Wpt wpt);
	public void doneLoading();

	public void initFiltering();
	public void filtered(Wpt wpt, boolean shown);
	public void doneFiltering();

	static final WptSumarizer EMPTY = new WptSumarizer() {


		@Override
		public void initLoading() {
		}

		@Override
		public void initFiltering() {
		}

		@Override
		public void filtered(final Wpt wpt, final boolean shown) {
		}

		@Override
		public void doneLoading() {
		}

		@Override
		public void doneFiltering() {
		}

		@Override
		public void afterLoaded(final Wpt wpt) {
		}
	};



}
