/**
 *
 */
package cz.geokuk.plugins.refbody;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Action0;

/**
 *
 * @author veverka
 *
 */
public class NaKonkretniBodAction extends Action0 {

	private static int citac;
	private static final long serialVersionUID = -2882817111560336824L;
	private final Wgs wgs;

	/**
	 * @param aBoard
	 */
	public NaKonkretniBodAction(final String kam, final Wgs wgs) {
		super(kam);
		this.wgs = wgs;
		putValue(SHORT_DESCRIPTION, "Přesune mapu na referenční bod: \" " + kam + "\"");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt " + ++citac));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {
		poziceModel.setPozice(wgs);
		vyrezModel.vystredovatNaPozici();
	}

}
