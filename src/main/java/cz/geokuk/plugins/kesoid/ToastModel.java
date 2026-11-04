package cz.geokuk.plugins.kesoid;

import cz.geokuk.framework.Model0;

/** Rozesílá krátké zprávy po zkratkách; nová zpráva nahradí starou. */
public class ToastModel extends Model0 {

	/** Zprávu null nebo prázdnou nikdo nezobrazí. */
	public void ukaz(final String text) {
		if (text == null || text.isEmpty()) {
			return;
		}
		fire(new ToastEvent(text));
	}

	@Override
	protected void initAndFire() {
		// Bez počátečního stavu: zpráva je jednorázová.
	}
}
