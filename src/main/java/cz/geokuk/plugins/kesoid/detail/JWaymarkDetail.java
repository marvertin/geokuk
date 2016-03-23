package cz.geokuk.plugins.kesoid.detail;

import javax.swing.Box;

import cz.geokuk.plugins.kesoid.Waymark;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.refbody.DomaciSouradniceSeZmenilyEvent;

/**
 * Detailní informace o vybrané keši.
 *
 * @author Spikodrob
 *
 */
public class JWaymarkDetail extends JKesoidDetail0 {

	/**
	 *
	 */
	private static final long	serialVersionUID	= -3323887260932949747L;

	private Waymark				waym;

	public JWaymarkDetail() {
		initComponents();
	}

	@Override
	public void napln(final Wpt wpt) {
		waym = (Waymark) wpt.getKesoid();
		napln();
	}

	public void onEvent(final DomaciSouradniceSeZmenilyEvent aEvent) {
		if (isVisible() && waym != null) {
			napln();
		}
	}

	private void initComponents() {

		final Box hlav = Box.createVerticalBox();
		add(hlav);

		final Box box2 = Box.createHorizontalBox();

		final Box pan4b = Box.createVerticalBox();

		box2.add(Box.createHorizontalGlue());

		// pan4.setAlignmentX(RIGHT_ALIGNMENT);
		box2.add(pan4b);

		hlav.add(box2);

		final Box box3 = Box.createHorizontalBox();
		box3.add(Box.createGlue());
		hlav.add(box3);

	}

	private void napln() {
	}

}
