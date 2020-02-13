package cz.geokuk.plugins.kesoid.kind.kes;

import javax.swing.SwingUtilities;

import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.WptSumarizer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KesWptSumarizer extends Model0  implements WptSumarizer {

	private final KesPlugin kesPlugin;

	@Getter
	private int maximalniBestOf;

	@Getter
	private int maximalniHodnoceni;

	@Getter
	private int maximalniFavorit;


	@Override
	public void initLoading() {
		maximalniBestOf = 0;
		maximalniHodnoceni = 0;
		maximalniFavorit = 0;
	}

	@Override
	public void afterLoaded(final Wpt wpt) {
		final Kesoid kesoid = wpt.getKoid();
		final Kes kes = (Kes) kesoid;
		maximalniBestOf = Math.max(maximalniBestOf, kes.getBestOf());
		maximalniHodnoceni = Math.max(maximalniHodnoceni, kes.getHodnoceni());
		maximalniFavorit = Math.max(maximalniFavorit, kes.getFavorit());
	}

	@Override
	public void doneLoading() {
		SwingUtilities.invokeLater(() -> fire(new KesWptSumarizeEvent()));
	}

	@Override
	public void initFiltering() {
		// TODO Auto-generated method stub

	}

	@Override
	public void filtered(final Wpt wpt, final boolean shown) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doneFiltering() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initAndFire() {
	}

}
