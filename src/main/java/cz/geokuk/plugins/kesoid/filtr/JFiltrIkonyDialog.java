package cz.geokuk.plugins.kesoid.filtr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.*;

import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.genetika.QualAlelaNames;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;
import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;

public class JFiltrIkonyDialog extends JMyDialog0 implements AfterInjectInit {

	private static final long serialVersionUID = -6496737194139718970L;
	private JComponent jvse;

	private JFiltrVyberIkon filtrVyberIkon;

	/** Pruh nahoře, který říká, že kešoidy jsou skryté (F5) a filtr se proto na mapě neuplatní. */
	JPanel skryteKesoidy;
	private KesoidModel kesoidModel;

	private IkonBag bag;
	private QualAlelaNames jmenaAlelFiltr;
	private KesBag kesBag;

	public JFiltrIkonyDialog() {
		super();
		setTitle("Výběr filtru");
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

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
		skryteKesoidy.setVisible(!kesoidModel.isOnoff());
	}

	public void onEvent(final KesoidOnoffEvent event) {
		skryteKesoidy.setVisible(!event.isOnoff());
		skryteKesoidy.revalidate();
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
		skryteKesoidy = new JPanel(new BorderLayout(8, 0));
		skryteKesoidy.setBackground(new Color(255, 205, 80));
		skryteKesoidy.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		final JLabel textSkrytych = new JLabel("Kešoidy jsou skryté (F5) – filtr se teď na mapě neuplatní.");
		textSkrytych.setForeground(Color.BLACK);
		skryteKesoidy.add(textSkrytych, BorderLayout.CENTER);
		final JButton zobrazit = new JButton("Zobrazit kešoidy");
		zobrazit.addActionListener(e -> {
			if (kesoidModel != null) {
				kesoidModel.setOnoff(true);
			}
		});
		skryteKesoidy.add(zobrazit, BorderLayout.EAST);
		skryteKesoidy.setVisible(false);
		add(skryteKesoidy, BorderLayout.NORTH);

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
