package cz.geokuk.plugins.kesoid.mapicon;


import java.util.Set;

import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

public class JFenotypVyberIkon extends JVyberIkon0 {

	private static final long serialVersionUID = -6496737194139718970L;

	private IkonBag bag;

	private Set<Alela> pouziteAlely;

	private KesoidModel kesoidModel;

	/**
	 * @param aJskelneikony
	 */
	public JFenotypVyberIkon() {
		super(false, true);
	}


	public void resetBag(IkonBag bag, KesBag kesBag, Set<String> aJmenaVybranychAlel) {
		this.bag = bag;
		pouziteAlely = kesBag.getPouziteAlely();
		refresh(bag, aJmenaVybranychAlel, kesBag.getPoctyAlel());
	}

	@Override
	protected void zmenaVyberu(Set<Alela> aAlely) {
		System.out.println("Vyber alel, které se nefenotypují: " + aAlely);
		kesoidModel.setJmenaNefenotypovanychAlel(Alela.alelyToNames(aAlely));
	}


	/* (non-Javadoc)
	 * @see geokuk.mapicon.JVyberIkon0#shouldRender(geokuk.mapicon.Alela)
	 */
	@Override
	protected boolean shouldRender(Alela alela) {
		return ! alela.isVychozi() && bag.getSada().getPouziteAlely().contains(alela)
				&& (pouziteAlely == null || pouziteAlely.contains(alela));
	}


	/* (non-Javadoc)
	 * @see geokuk.mapicon.JVyberIkon0#shouldRender(geokuk.mapicon.Gen)
	 */
	@Override
	protected boolean shouldRender(Gen gen) {
		return bag.getSada().getPouziteGeny().contains(gen);
	}


	@Override
	protected boolean shouldEnable(Alela alela) {
		return true;
	}

	public void inject(KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

}
