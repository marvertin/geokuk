package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.Dimension;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.genetika.*;

public class JDebugVyberIkon extends JVyberIkon0 {

	private static final long serialVersionUID = -6496737194139718970L;
	private final JComponent jskelneikony;

	private IkonBag bag;
	private Set<String> jmenaVybranychAlel;
	private boolean zobrazovatVse;

	/**
	 * @param aJskelneikony
	 */
	public JDebugVyberIkon(final JComponent aJskelneikony) {
		super(true, false);
		jskelneikony = aJskelneikony;
	}

	public void onEvent(final PoziceChangedEvent event) {
		final Wpt wpt = event.poziceq.getWpt();
		if (wpt == null) {
			return;
		}
		if (bag == null) {
			return;
		}
		jmenaVybranychAlel = Alela.alelyToNames(wpt.getGenotyp(bag.getGenom()).getAlely());
		zobrazovatVse = true;
		refresh(bag, jmenaVybranychAlel, null);
	}

	public void resetBag(final IkonBag aBag) {
		bag = aBag;
		if (jmenaVybranychAlel == null) {
			final Jedinec genotypVychozi = bag.getGenom().getGenotypVychozi();
			jmenaVybranychAlel = Alela.alelyToNames(genotypVychozi.getAlely());
		}
		refresh(aBag, jmenaVybranychAlel, null);
	}

	@Override
	protected boolean shouldEnable(final Alela alela) {
		return alela.isVychozi() || bag.getSada().getPouziteAlely().contains(alela);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see geokuk.mapicon.JVyberIkon0#shouldRender(geokuk.mapicon.Alela)
	 */
	@Override
	protected boolean shouldRender(final Alela alela) {
		return alela.isVychozi() || bag.getSada().getPouziteAlely().contains(alela) || zobrazovatVse;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see geokuk.mapicon.JVyberIkon0#shouldRender(geokuk.mapicon.Gen)
	 */
	@Override
	protected boolean shouldRender(final Gen gen) {
		return bag.getSada().getPouziteGeny().contains(gen) || zobrazovatVse;
	}

	@Override
	protected void zmenaVyberu(final Set<Alela> aAlelyx) {
		jmenaVybranychAlel = Alela.alelyToNames(aAlelyx);
		final Jedinec genotyp = bag.getGenom().jakysiNovyJedinec(aAlelyx);
		final Sklivec sklivec = bag.getSklivec(genotyp);
		jskelneikony.removeAll();
		// BoundingRect br = Imagant.sjednoceni(sklivec.imaganti);
		{
			jskelneikony.add(Box.createVerticalStrut(20));
			final JButton jLabel = new JButton();
			jLabel.setAlignmentX(CENTER_ALIGNMENT);
			// jLabel.setText("všechna skla přes sebe");
			final Imagant imagant = Imagant.prekresliNaSebe(sklivec.imaganti);
			if (imagant != null) {
				jLabel.setIcon(new ImageIcon(imagant.getImage()));
			}
			jskelneikony.add(jLabel);
		}
		jskelneikony.add(Box.createVerticalStrut(50));

		final Iterator<SkloAplikant> iterator = bag.getSada().getSkloAplikanti().iterator();

		for (final Imagant imagant : sklivec.imaganti) {
			final SkloAplikant skloAplikant = iterator.next();
			final Box panel = Box.createHorizontalBox();
			final TitledBorder border = BorderFactory.createTitledBorder(skloAplikant.sklo.getName());
			border.setTitleJustification(TitledBorder.CENTER);
			panel.setBorder(border);
			final JLabel jLabel = new JLabel();
			// jLabel.setText("sklo");
			if (imagant != null) {
				jLabel.setIcon(new ImageIcon(imagant.getImage()));
			}
			jLabel.setAlignmentX(CENTER_ALIGNMENT);

			panel.add(Box.createHorizontalGlue());
			panel.add(jLabel);
			panel.add(Box.createHorizontalGlue());

			panel.setMinimumSize(new Dimension(150, 100));
			panel.setPreferredSize(new Dimension(150, 100));

			// JLabel jJmenoSady = new JLabel(skloAplikant.sklo.getName());
			// jJmenoSady.setAlignmentX(JComponent.CENTER_ALIGNMENT);
			// jskelneikony.add(jJmenoSady);
			jskelneikony.add(panel);
			jskelneikony.add(Box.createVerticalStrut(10));
		}
		jskelneikony.add(Box.createVerticalGlue());

		final JCheckBox jZobrazovaniVseho = new JCheckBox("Zobrazit vše");
		jZobrazovaniVseho.setSelected(zobrazovatVse);
		jskelneikony.add(jZobrazovaniVseho);

		jZobrazovaniVseho.addItemListener(e -> {
			zobrazovatVse = jZobrazovaniVseho.isSelected();
			resetBag(bag);
		});

		// a teď vyrendrovat vše přes sebe

		System.out.println(genotyp);
		jskelneikony.revalidate();
		// pack();
	}

}
