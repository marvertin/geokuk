/**
 *
 */
package cz.geokuk.core.coord;

import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import cz.geokuk.core.coordinates.*;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.Model0;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;

/**
 * @author veverka
 *
 */
public class PoziceModel extends Model0 implements AfterInjectInit {

	private Poziceq poziceq = new Poziceq();

	// /**
	// * @param pozice the pozice to set
	// */
	// public void setPozice(Mouable mouable) {
	// if (mouable == null) {
	// if (poziceq.isNoPosition())
	// return;
	// else { // pozice se ruší
	// poziceq = new Poziceq();
	// }
	// } else {
	// if (poziceq.isNoPosition()) { // pozice nově vzniká
	// poziceq = new Poziceq(new Pozice(mouable));
	// } else { // pozice se mění
	// if (mouable == poziceq.getPozice().toMouable()) return; // úplně stejný objekt jako minule
	// if (! (mouable instanceof Uchopenec) && !(poziceq.getPozice().toMouable() instanceof Uchopenec) && mouable.getMou().equals(poziceq.getPozice().getMou()))
	// return; // není to uchopenec, takž žádný konkrétní a přitom se jedná o stejné souřadky
	// poziceq = new Poziceq(new Pozice(mouable));
	// }
	// }
	// fire(new PoziceChangedEvent(poziceq));
	// }

	/**
	 * @param pozice
	 *            the pozice to set
	 */
	public void setPozice(Mouable mouable) {
		if (mouable == null) {
			poziceq = new Poziceq();
			fire(new PoziceChangedEvent(poziceq));
		} else { // jdeme na nějakou pozici
			novaPozice(mouable.getMou());
		}
	}

	/**
	 * Pozice je doopravdy nová a není prázdná
	 * 
	 * @param mou
	 */
	private void novaPozice(Mou mou) {
		PoziceSeMaMenitEvent event = new PoziceSeMaMenitEvent(mou);
		fire(event); // proženeme event přes všechny
		Mouable mouable = event.mou;
		if (event.getUchopenec() != null) {
			mouable = event.getUchopenec();
		}
		if (poziceq.isNoPosition() || mouable.getClass() != poziceq.getPoziceMouable().getClass() || !poziceq.getPoziceMou().equals(mou)) {
			poziceq = new Poziceq(mouable);
			fire(new PoziceChangedEvent(poziceq));
		}
	}

	public void souradniceDoClipboardu(Mouable mouable) {
		if (mouable == null)
			return;
		Clipboard scl = getSystemClipboard();
		Wgs wgs = mouable.getMou().toWgs();
		StringSelection ss = new StringSelection(wgs.toString());
		try {
			scl.setContents(ss, null);
		} catch (IllegalStateException e2) {
			FExceptionDumper.dump(e2, EExceptionSeverity.WORKARROUND, "Do clipboardu to nejde dáti.");
		}
	}

	public void refreshPozice() {
		if (!poziceq.isNoPosition()) {
			novaPozice(poziceq.getPoziceMou().getMou());
		}
	}

	public void clearPozice() {
		setPozice(null);
	}

	public void onEvent(PoziceChangedEvent event) {
		poziceq = event.poziceq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.geokuk.program.AfterInjectInit#initAfterInject()
	 */
	@Override
	public void initAndFire() {
		clearPozice();
	}

	public void setMys(Point cur, Mou mouCur, Mouable mouable) {
		fire(new ZmenaSouradnicMysiEvent(cur, mouCur, mouable));
	}

	public Poziceq getPoziceq() {
		return poziceq;
	}

}
