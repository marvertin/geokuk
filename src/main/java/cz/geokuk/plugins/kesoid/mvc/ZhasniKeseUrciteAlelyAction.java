/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;

import javax.swing.Icon;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;

/**
 * @author Martin Veverka
 *
 */
public class ZhasniKeseUrciteAlelyAction extends Action0 implements AfterEventReceiverRegistrationInit {

	private class PreskrtnutaIkona implements Icon {

		private final Icon icon;

		/**
		 * @param image
		 */
		public PreskrtnutaIkona(final Icon icon) {
			this.icon = icon;
		}

		/**
		 * @return
		 * @see javax.swing.Icon#getIconHeight()
		 */
		@Override
		public int getIconHeight() {
			return icon != null ? icon.getIconHeight() : 0;
		}

		/**
		 * @return
		 * @see javax.swing.Icon#getIconWidth()
		 */
		@Override
		public int getIconWidth() {
			return icon != null ? icon.getIconWidth() : 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see javax.swing.ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
		 */
		@Override
		public synchronized void paintIcon(final Component c, final Graphics g, final int x, final int y) {
			if (icon != null) {
				icon.paintIcon(c, g, x, y);
			}
			g.drawLine(0, 0, getIconWidth(), getIconHeight());
			g.drawLine(0, getIconHeight(), getIconWidth(), 0);
		}
	}

	private static final long serialVersionUID = -8054017274338240706L;
	private IkonBag ikonBag;
	private final Alela alela;
	private KesoidModel kesoidModel;

	private KesBag vsechny;

	/**
	 *
	 */
	public ZhasniKeseUrciteAlelyAction(final Alela alela) {
		this.alela = alela;
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		kesoidModel.filtrujDleAlely(alela.qualName(), false);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
	 */
	@Override
	public void initAfterEventReceiverRegistration() {
		super.putValue(NAME, sestavJmeno());
		super.putValue(SMALL_ICON, new PreskrtnutaIkona(ikonBag.seekIkon(ikonBag.getGenom().UNIVERZALNI_DRUH.genotypVychozi().with(alela))));
		super.putValue(SHORT_DESCRIPTION, "Zhasne waypointy daného typu.");

	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void onEvent(final IkonyNactenyEvent event) {
		ikonBag = event.getBag();
	}

	public void onEvent(final KeskyNactenyEvent event) {
		vsechny = event.getVsechny();
	}

	private String sestavJmeno() {
		return String.format("<html>%s: <b>%s</b> <i>(%d)</i>", alela.getGen().getDisplayName(), alela.getDisplayName(), vsechny.getPoctyAlel().count(alela));
	}

}
