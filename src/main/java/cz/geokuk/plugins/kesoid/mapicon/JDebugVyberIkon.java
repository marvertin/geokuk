package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.plugins.kesoid.Wpt;

public class JDebugVyberIkon extends JVyberIkon0 {

	private static final long	serialVersionUID	= -6496737194139718970L;
	private final JComponent	jskelneikony;

	private IkonBag				bag;
	private Set<String>			jmenaVybranychAlel;
	private boolean				zobrazovatVse;

	/**
	 * @param aJskelneikony
	 */
	public JDebugVyberIkon(JComponent aJskelneikony) {
		super(true, false);
		jskelneikony = aJskelneikony;
	}

	public void resetBag(IkonBag aBag) {
		bag = aBag;
		if (jmenaVybranychAlel == null) {
			Genotyp genotypVychozi = bag.getGenom().getGenotypVychozi();
			jmenaVybranychAlel = Alela.alelyToNames(genotypVychozi.getAlely());
		}
		refresh(aBag, jmenaVybranychAlel, null);
	}

	@Override
	protected void zmenaVyberu(Set<Alela> aAlelyx) {
		jmenaVybranychAlel = Alela.alelyToNames(aAlelyx);
		Genotyp genotyp = new Genotyp(aAlelyx, bag.getGenom());
		Sklivec sklivec = bag.getSklivec(genotyp);
		jskelneikony.removeAll();
		// BoundingRect br = Imagant.sjednoceni(sklivec.imaganti);
		{
			jskelneikony.add(Box.createVerticalStrut(20));
			JButton jLabel = new JButton();
			jLabel.setAlignmentX(CENTER_ALIGNMENT);
			// jLabel.setText("všechna skla přes sebe");
			Imagant imagant = Sklo.prekresliNaSebe(sklivec.imaganti);
			if (imagant != null) {
				jLabel.setIcon(new ImageIcon(imagant.getImage()));
			}
			jskelneikony.add(jLabel);
		}
		jskelneikony.add(Box.createVerticalStrut(50));

		Iterator<SkloAplikant> iterator = bag.getSada().getSkloAplikanti().iterator();

		for (Imagant imagant : sklivec.imaganti) {
			SkloAplikant skloAplikant = iterator.next();
			Box panel = Box.createHorizontalBox();
			TitledBorder border = BorderFactory.createTitledBorder(skloAplikant.sklo.getName());
			border.setTitleJustification(TitledBorder.CENTER);
			panel.setBorder(border);
			JLabel jLabel = new JLabel();
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

		jZobrazovaniVseho.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				zobrazovatVse = jZobrazovaniVseho.isSelected();
				resetBag(bag);
			}
		});

		// a teď vyrendrovat vše přes sebe

		System.out.println(genotyp);
		jskelneikony.revalidate();
		// pack();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see geokuk.mapicon.JVyberIkon0#shouldRender(geokuk.mapicon.Alela)
	 */
	@Override
	protected boolean shouldRender(Alela alela) {
		return alela.isVychozi() || bag.getSada().getPouziteAlely().contains(alela) || zobrazovatVse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see geokuk.mapicon.JVyberIkon0#shouldRender(geokuk.mapicon.Gen)
	 */
	@Override
	protected boolean shouldRender(Gen gen) {
		return bag.getSada().getPouziteGeny().contains(gen) || zobrazovatVse;
	}

	@Override
	protected boolean shouldEnable(Alela alela) {
		return alela.isVychozi() || bag.getSada().getPouziteAlely().contains(alela);
	}

	public void onEvent(PoziceChangedEvent event) {
		Wpt wpt = event.poziceq.getWpt();
		if (wpt == null)
			return;
		if (bag == null)
			return;
		jmenaVybranychAlel = Alela.alelyToNames(wpt.getGenotyp(bag.getGenom()).getAlely());
		zobrazovatVse = true;
		refresh(bag, jmenaVybranychAlel, null);
	}

}
