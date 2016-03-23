package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.*;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyVyfiltrovanyEvent;

public class JFenotypIkonyDialog extends JMyDialog0 implements AfterInjectInit {

	private static final long serialVersionUID = -6496737194139718970L;
	private JComponent jvse;

	private JFenotypVyberIkon fenotypVyberIkon;

	private IkonBag ikonBag;
	private Set<String> jmenaVybranychAlel;
	private KesBag filtrovaneKesBag;

	public JFenotypIkonyDialog() {
		setTitle("Výběr fenotypu");
		init();
	}

	@Override
	public void initAfterInject() {
		factory.init(fenotypVyberIkon);
	}

	public void onEvent(final FenotypPreferencesChangedEvent event) {
		if (event.getJmenaNefenotypovanychAlel().equals(jmenaVybranychAlel)) {
			return;
		}
		jmenaVybranychAlel = event.getJmenaNefenotypovanychAlel();
		resetIfVse();
	}

	public void onEvent(final IkonyNactenyEvent event) {
		ikonBag = event.getBag();
		resetIfVse();
	}

	public void onEvent(final KeskyVyfiltrovanyEvent event) {
		filtrovaneKesBag = event.getFiltrovane();
		resetIfVse();
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "VyberFenotypu";
	}

	@Override
	protected void initComponents() {
		jvse = Box.createHorizontalBox();
		jvse.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(jvse);

		fenotypVyberIkon = new JFenotypVyberIkon();
		jvse.add(fenotypVyberIkon);
		jvse.setPreferredSize(new Dimension(500, 600));
	}

	private void resetIfVse() {
		if (jmenaVybranychAlel == null) {
			return;
		}
		if (ikonBag == null) {
			return;
		}
		if (filtrovaneKesBag == null) {
			return;
		}
		fenotypVyberIkon.resetBag(ikonBag, filtrovaneKesBag, jmenaVybranychAlel);
		jvse.revalidate();
	}

}
