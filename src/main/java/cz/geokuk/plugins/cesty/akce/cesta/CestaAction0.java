package cz.geokuk.plugins.cesty.akce.cesta;

import java.awt.event.ActionEvent;

import cz.geokuk.plugins.cesty.akce.PositionedAction0;
import cz.geokuk.plugins.cesty.data.Cesta;

public abstract class CestaAction0 extends PositionedAction0 {

	private static final long serialVersionUID = 1L;
	private final Cesta kontextovaCesta;

	public CestaAction0(Cesta kontextovaCesta) {
		//System.out.println(System.identityHashCode(kontextovaCesta) + ": CestaAction0-konstruktor " + getClass());
		this.kontextovaCesta = kontextovaCesta;
		if (kontextovaCesta != null) {
			kontextovaCesta.kontrolaKonzistence();
		}
	}

	@Override
	protected final void vyletChanged() {
		vyhodnotitPovolenost();
	}


	private void vyhodnotitPovolenost() {
		Cesta cesta = getCesta();
		if (cesta == null) {
			setEnabled(false);
		} else {
			if (mamPovolitProCestu(cesta)) {
				setEnabled(true);
				nastavJmenoAkce(cesta, kontextovaCesta != null);
			} else {
				setEnabled(false);
			}
		}
	}

	@Override
	public final void actionPerformed(ActionEvent aE) {
		Cesta cesta = getCesta();
		if (cesta != null) {
			provedProCestu(cesta);
		}
	}

	/**
	 * Pokud je daný Cesta, tak se nechá natavit jméno. Natavovací metoda také
	 * ví, zda je akce z kontexotvého menu nebo z hlavního
	 * @param Cesta
	 * @param aZKontextovehoMenu
	 */
	protected abstract void nastavJmenoAkce(Cesta cesta, boolean aZKontextovehoMenu);

	/**
	 * Zda se pro dany Cesta ma akce povolit.
	 * @param Cesta Nikdy nebude predano null
	 * @return
	 */
	protected abstract boolean mamPovolitProCestu(Cesta cesta);

	/**
	 * Provedení akce pro daný Cesta.
	 * @param Cesta
	 */
	protected abstract void provedProCestu(Cesta cesta);

	private Cesta getCesta() {
		if (curta() != null) {
			curta().kontrolaKonzistence();
		}
		if (kontextovaCesta != null) {
			//System.out.println(System.identityHashCode(kontextovaCesta) + ": CestaAction0-getClass " + getClass());
			kontextovaCesta.kontrolaKonzistence();
			return kontextovaCesta;
		} else {
			return curta();
		}
	}



}

