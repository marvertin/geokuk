package cz.geokuk.plugins.kesoid.detail;


import javax.swing.Box;

import cz.geokuk.plugins.kesoid.SimpleWaypoint;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.refbody.DomaciSouradniceSeZmenilyEvent;

/**
 * Detailní informace o vybrané keši.
 * @author Spikodrob
 *
 */
public class JSimpleWaypointDetail extends JKesoidDetail0 {

	private static final long serialVersionUID = -3323887260932949747L;

	private SimpleWaypoint simwpt;

	public JSimpleWaypointDetail() {
		initComponents();
	}

	private void initComponents() {

		Box hlav = Box.createVerticalBox();
		add(hlav);

		Box box2 = Box.createHorizontalBox();

		Box pan4b = Box.createVerticalBox();

		box2.add(Box.createHorizontalGlue());

		//  pan4.setAlignmentX(RIGHT_ALIGNMENT);
		box2.add(pan4b);

		hlav.add(box2);

		Box box3 = Box.createHorizontalBox();
		box3.add(Box.createGlue());
		hlav.add(box3);
	}

	@Override
	public void napln(Wpt wpt) {
		simwpt = (SimpleWaypoint)wpt.getKesoid();
		napln();
	}

	public void onEvent(DomaciSouradniceSeZmenilyEvent aEvent) {
		if (isVisible() && simwpt != null) {
			napln();
		}
	}

	private void napln() {
	}
}
