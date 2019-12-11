package cz.geokuk.plugins.kesoid.filtr;

import java.util.HashSet;
import java.util.Set;

import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.genetika.*;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;
import cz.geokuk.plugins.kesoid.mapicon.JVyberIkon0;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

public class JFiltrVyberIkon extends JVyberIkon0 {

	private static final long serialVersionUID = -6496737194139718970L;

	private KesBag kesBag;

	private KesoidModel kesoidModel;

	/**
	 * @param aJskelneikony
	 */
	public JFiltrVyberIkon() {
		super(false, true);
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void resetBag(final IkonBag bag, final KesBag kesBag, final QualAlelaNames aJmenaVybranychAlel) {
		this.kesBag = kesBag;
		refresh(bag, aJmenaVybranychAlel, kesBag.getPoctyAlel());
	}

	@Override
	protected boolean shouldEnable(final Alela alela) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see geokuk.mapicon.JVyberIkon0#shouldRender(geokuk.mapicon.Alela)
	 */
	@Override
	protected boolean shouldRender(final Alela alela) {
		return kesBag.getPouziteAlely().contains(alela);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see geokuk.mapicon.JVyberIkon0#shouldRender(geokuk.mapicon.Gen)
	 */
	@Override
	protected boolean shouldRender(final Gen gen) {
		final Set<Alela> alely = new HashSet<>(gen.getAlely());
		alely.retainAll(kesBag.getPouziteAlely());
		return alely.size() > 1;
	}

	@Override
	protected void zmenaVyberu(final Set<Alela> aAlely) {
		kesoidModel.setJmenaNechtenychAlel(Alela.alelyToQualNames(aAlely));
	}

}
