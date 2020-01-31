package cz.geokuk.plugins.kesoid.kind.kes;

import javax.swing.SpinnerNumberModel;

public class JVybiracHodnoceni extends JVybiracCiselnyRuznychHodnoceni0 {

	private static final long serialVersionUID = 2417664157609045262L;

	public final SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 100, 1);

	public JVybiracHodnoceni() {
		super("Hodnocení:");
		setToolTipText("Filte dle prahu hodnocení keší na geocaching.cz, zobrazí se jen keše mající hodnocení větší nebo rovné prahu.");
	}

	@Override
	protected int getMaximum(final KesWptSumarizer sumarizer) {
		return sumarizer.getMaximalniHodnoceni();
	}

	@Override
	protected int getPrah(final KesFilterDefinition filterDefinition) {
		return filterDefinition.getPrahHodnoceni();
	}

	@Override
	protected KesFilterDefinition withPrah(final KesFilterDefinition filterDefinition, final int prah) {
		return filterDefinition.withPrahHodnoceni(prah);
	}

}