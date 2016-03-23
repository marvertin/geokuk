package cz.geokuk.plugins.kesoid.filtr;

import java.awt.Dimension;
import java.util.Set;

import javax.swing.*;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;

public class JFiltrIkonyDialog extends JMyDialog0 implements AfterInjectInit {

	private static final long	serialVersionUID	= -6496737194139718970L;
	private JComponent			jvse;

	private JFiltrVyberIkon		filtrVyberIkon;

	private IkonBag				bag;
	private Set<String>			jmenaAlelFiltr;
	private KesBag				kesBag;

	public JFiltrIkonyDialog() {
		super();
		setTitle("Výběr Filtru");
		init();
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

	public void onEvent(KeskyNactenyEvent event) {
		kesBag = event.getVsechny();
		resetIfVse();
	}

	public void onEvent(IkonyNactenyEvent event) {
		bag = event.getBag();
		resetIfVse();
	}

	public void onEvent(FilterDefinitionChangedEvent event) {
		if (event.getJmenaNechtenychAlel().equals(jmenaAlelFiltr))
			return;
		jmenaAlelFiltr = event.getJmenaNechtenychAlel();
		resetIfVse();
	}

	private void resetIfVse() {
		if (jmenaAlelFiltr == null)
			return;
		if (bag == null)
			return;
		if (kesBag == null)
			return;
		filtrVyberIkon.resetBag(bag, kesBag, jmenaAlelFiltr);
		jvse.revalidate();
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

	@Override
	protected String getTemaNapovedyDialogu() {
		return "FiltrKesoidu";
	}

}
