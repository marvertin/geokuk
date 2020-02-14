package cz.geokuk.plugins.kesoid.kind.munzee;

import javax.swing.Box;

import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.Wpti;
import cz.geokuk.plugins.kesoid.detail.JKesoidDetail0;
import cz.geokuk.plugins.refbody.DomaciSouradniceSeZmenilyEvent;

/**
 * Detailní informace o vybrané keši.
 *
 * @author Spikodrob
 *
 */
public class JMunzeeDetail extends JKesoidDetail0 {

	/**
	 *
	 */
	private static final long serialVersionUID = -3323887260932949747L;

	private Munzee munzee;

	public JMunzeeDetail() {
		initComponents();
	}

	@Override
	public void napln(final Wpt wpt) {
		munzee = (Munzee) ((Wpti)wpt).getKoid();
		napln();
	}

	public void onEvent(final DomaciSouradniceSeZmenilyEvent aEvent) {
		if (isVisible() && munzee != null) {
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

	private void napln() {}

}
