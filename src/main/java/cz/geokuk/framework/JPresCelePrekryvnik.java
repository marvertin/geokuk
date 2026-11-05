/**
 *
 */
package cz.geokuk.framework;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.napoveda.UkazatelVykonu;
import cz.geokuk.core.napoveda.Vykon;

/**
 * @author Martin Veverka
 *
 */
public class JPresCelePrekryvnik extends JCoordPrekryvnik0 implements AfterEventReceiverRegistrationInit {
	private static final long serialVersionUID = -5996655830197513951L;
	private VyrezModel vyrezModel;

	@Override
	public void initAfterEventReceiverRegistration() {
		UkazatelVykonu.sleduj(this);
		// Listener zajístí, že se změna šířky a výšky pošle všem zájemcům
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				vyrezModel.setVelikost(getWidth(), getHeight());
			}
		});
	}

	public void inject(final VyrezModel vyrezModel) {
		this.vyrezModel = vyrezModel;
	}

	@Override
	public void paint(final Graphics g) {
		final long zacatek = System.nanoTime();
		super.paint(g);
		final Rectangle vyrez = g.getClipBounds();
		// Překreslení jen štítku ukazatele se do doby kreslení mapy nepočítá.
		if (vyrez == null || !UkazatelVykonu.oblast().contains(vyrez)) {
			Vykon.zaznamenej(Vykon.Velicina.PREKRESLENI, System.nanoTime() - zacatek);
		}
		UkazatelVykonu.kresli(g);
	}

	public void onEvent(final VyrezChangedEvent event) {
		// V hlavním překryvníku sledujeme pohyb výřezu a podle toho posouváme pohled
		setSoord(event.getMoord());
	}

}
