package cz.geokuk.plugins.kesoid.filtr;

import java.awt.Dimension;

import javax.swing.*;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.genetika.QualAlelaNames;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;

public class JFiltrIkonyDialog extends JMyDialog0 implements AfterInjectInit {

	private static final long serialVersionUID = -6496737194139718970L;
	private JComponent jvse;

	private JFiltrVyberIkon filtrVyberIkon;

	private IkonBag bag;
	private QualAlelaNames jmenaAlelFiltr;
	private KesBag kesBag;

	public JFiltrIkonyDialog() {
		super();
		setTitle("Výběr Filtru");
		init();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.AfterInjectInit#initAfterInject()
	 */
	@Override
	public void initAfterInject() {
		factory.init(filtrVyberIkon);
	}

	public void onEvent(final FilterDefinitionChangedEvent event) {
		if (event.getJmenaNechtenychAlel().equals(jmenaAlelFiltr)) {
			return;
		}
		jmenaAlelFiltr = event.getJmenaNechtenychAlel();
		resetIfVse();
	}

	public void onEvent(final IkonyNactenyEvent event) {
		bag = event.getBag();
		resetIfVse();
	}

	public void onEvent(final KeskyNactenyEvent event) {
		kesBag = event.getVsechny();
		resetIfVse();
	}

	@Override
	protected String getTemaNapovedyDialogu() {
		return "FiltrKesoidu";
	}

	@Override
	protected void initComponents() {
		jvse = Box.createHorizontalBox();
		jvse.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(jvse);

		filtrVyberIkon = new JFiltrVyberIkon();
		jvse.add(filtrVyberIkon);
		jvse.setPreferredSize(new Dimension(500, 600));
	}

	private void resetIfVse() {
		if (jmenaAlelFiltr == null) {
			return;
		}
		if (bag == null) {
			return;
		}
		if (kesBag == null) {
			return;
		}
		filtrVyberIkon.resetBag(bag, kesBag, jmenaAlelFiltr);
		jvse.revalidate();
	}

}
