/**
 *
 */
package cz.geokuk.core.coord;

import java.awt.Dimension;

import cz.geokuk.core.coordinates.*;
import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.util.lang.FMath;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Martin Veverka
 */
@Slf4j
public class VyrezModel extends Model0 {



	public static final Wgs DEFAULTNI_DOMACI_SOURADNICE = new Wgs(49.8, 15.5);

	// private EventFirer ef;

	private PoziceModel poziceModel;

	private EKaType podkladMap;

	private Coord moord = Coord.prozatimniInicializacni();

	/**
	 * @return the moord
	 */
	public Coord getMoord() {
		return moord;
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	public boolean isPoziceUprostred() {
		final Mou mouPozice = poziceModel.getPoziceq().getPoziceMou();
		final Mou mouStred = moord.getMoustred();
		final boolean b = mouPozice.equals(mouStred);

		log.debug("Mou pozice: ", mouPozice);
		log.debug("Mou stred: ", mouStred);
		return b;
	}

	public boolean jeNejblizsiMeritko() {
		return nastavitelneMeritkoZChteneho(moord.getMoumer() + 1, false) == moord.getMoumer();
	}

	public boolean jeNejvzdaLenejsiMeritko() {
		return nastavitelneMeritkoZChteneho(moord.getMoumer() - 1, false) == moord.getMoumer();
	}

	public int nejblizsiMeritko() {
		return podkladMap.getMaxMoumer();
	}

	public int nejvzdalenejsiMeritko() {
		return podkladMap.getMinMoumer();
	}

	public int omezMeritko(final int moumer) {
		return nastavitelneMeritkoZChteneho(moumer, false);
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		podkladMap = event.getKatype();
		setMoumer(FMath.fit(moord.getMoumer(), podkladMap.getMinMoumer(), podkladMap.getMaxMoumer()));
	}

	public void presunMapuNaMoustred(final Mou moustred) {
		setMoord(moord.derive(moustred));
	}

	public void setMeritkoMapy(final int moumer) {
		setMoumer(nastavitelneMeritkoZChteneho(moumer, false));
	}

	/**
	 * Jen nedovolí turistické mapě úplné přiblížení
	 *
	 * @param moumer
	 */
	public void setMeritkoMapyAutomaticky(final int moumer) {
		setMoumer(nastavitelneMeritkoZChteneho(moumer, true));
	}

	public void setMoord(final Coord moord) {
		if (moord.equals(this.moord)) {
			return;
		}
		this.moord = moord;
		currPrefe().node(FPref.UVODNI_SOURADNICE_node).putInt(FPref.MOUMER_value, moord.getMoumer());
		currPrefe().node(FPref.UVODNI_SOURADNICE_node).putMou(FPref.MOUSTRED_value, moord.getMoustred());

		fire(new VyrezChangedEvent(moord));
	}

	public void setMoumer(final int moumer) {
		setMoord(moord.derive(moumer));
	}

	public void setVelikost(final int width, final int height) {
		setMoord(moord.derive(new Dimension(width, height)));
	}

	public void vystredovatNaPozici() {
		final Mou mou = poziceModel.getPoziceq().getPoziceMou();
		if (mou != null) {
			presunMapuNaMoustred(mou);
		}
	}

	public void zoomByGivenPoint(final int moumer, final Mou zoomMouStred) {
		final int skutecnyMoumer = nastavitelneMeritkoZChteneho(moumer, false);
		setMoord(moord.derive(skutecnyMoumer, moord.computeZoom(skutecnyMoumer, zoomMouStred)));
	}

	public void zoomTo(final MouRect mourect) {
		final Mou moustred = mourect.getStred();
		poziceModel.setPozice(moustred.toWgs());
		vystredovatNaPozici();
		int mer = 20;
		for (;; mer--) {
			// TODO [veverka] Ta konstanta 24 je vycucaná z prstu, funguje, ale kde se vzala? -- 11. 2. 2020 11:04:15 veverka
			final int pom = 1 << 24 - mer; // pomer pro toto meritko
			if (mourect.getMouWidth() / pom <= moord.getDim().getWidth()
					&& mourect.getMouHeight() / pom <= moord.getDim().getHeight()) {
				break; // hledáme nejbližší nejlepší
			}
		}
		setMeritkoMapyAutomaticky(mer);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.Model0#initAndFire()
	 */
	@Override
	protected void initAndFire() {
		final int moumer = currPrefe().node(FPref.UVODNI_SOURADNICE_node).getInt(FPref.MOUMER_value, 6);
		final Mou moustred = currPrefe().node(FPref.UVODNI_SOURADNICE_node).getMou(FPref.MOUSTRED_value, DEFAULTNI_DOMACI_SOURADNICE.toMou());
		setMoord(moord.derive(moumer, moustred));
	}

	private int nastavitelneMeritkoZChteneho(int moumer, final boolean autoMeritko) {
		moumer = FMath.fit(moumer, podkladMap.getMinMoumer(), autoMeritko ? podkladMap.getMaxAutoMoumer() : podkladMap.getMaxMoumer());
		return moumer;
	}
}
