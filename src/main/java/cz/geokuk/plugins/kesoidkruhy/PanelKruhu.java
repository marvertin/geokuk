package cz.geokuk.plugins.kesoidkruhy;

import java.awt.Color;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeListener;

/** Ovládací prvky nastavení zvýrazňovacích kruhů: jednotkovost, velikost, průhlednost a barva. Zapnutí kruhů panel nemění. */
final class PanelKruhu extends Box {

	private static final long serialVersionUID = 1L;

	final JLabel pruhlednostLabel = new JLabel();
	final JSlider velikostSlider = new JSlider();
	final JSlider pruhlednostSlider = new JSlider(0, 100, 0);
	/** Po každém vytvoření panelů (i po přepnutí vzhledu) se z nich odstraní ovládání průhlednosti. */
	final JColorChooser barvaChooser = new JColorChooser(Color.WHITE) {
		private static final long serialVersionUID = 1L;

		@Override
		public void updateUI() {
			super.updateUI();
			skryjPruhlednostVChooseru(this);
		}
	};
	final JCheckBox jednotkoveKruhy = new JCheckBox("Jednotkové kruhy");

	private KruhyModel kruhyModel;
	/** Ovládací prvky se právě nastavují z modelu, jejich změny se do modelu nevracejí. */
	private boolean nacitaSe;

	PanelKruhu() {
		super(BoxLayout.Y_AXIS);
		pruhlednostLabel.setText(textPruhlednosti(0));
		velikostSlider.setToolTipText("Nastavení velikosti zvýrazňovacího kruhu.");
		pruhlednostSlider.setToolTipText("Průhlednost zvýrazňovacího kruhu: 0 % je plně krycí, 100 % neviditelný.");
		barvaChooser.setToolTipText("Nastavení barvy zvýrazňovacího kruhu");
		jednotkoveKruhy.setToolTipText("Nastavení jednotkovosti kruhů");
		add(jednotkoveKruhy);
		add(new JLabel("Velikost kruhů"));
		add(velikostSlider);
		add(Box.createVerticalStrut(10));
		add(pruhlednostLabel);
		add(pruhlednostSlider);
		add(Box.createVerticalStrut(10));
		add(barvaChooser);
	}

	/** Průhlednost nastavuje jen posuvník Průhlednost, výběr barvy ji nenabízí. */
	static void skryjPruhlednostVChooseru(final JColorChooser chooser) {
		for (final AbstractColorChooserPanel panel : chooser.getChooserPanels()) {
			try {
				// Od Javy 9; starší výběr barvy ovládání průhlednosti nemá.
				AbstractColorChooserPanel.class.getMethod("setColorTransparencySelectionEnabled", boolean.class).invoke(panel, false);
			} catch (final ReflectiveOperationException | RuntimeException e) {
				// nic k vypnutí
			}
		}
	}

	void inject(final KruhyModel kruhyModel) {
		this.kruhyModel = kruhyModel;
	}

	void onEvent(final KruhyPreferencesChangeEvent event) {
		nacitaSe = true;
		try {
			jednotkoveKruhy.getModel().setSelected(event.kruhy.isJednotkovaVelikost());
			velikostSlider.getModel().setValue(event.kruhy.getVelikost());
			final Color barva = event.kruhy.getBarva();
			barvaChooser.getSelectionModel().setSelectedColor(new Color(barva.getRGB()));
			final int pruhlednost = KruhyZDialogu.pruhlednostProcent(barva.getAlpha());
			pruhlednostSlider.getModel().setValue(pruhlednost);
			pruhlednostLabel.setText(textPruhlednosti(pruhlednost));
		} finally {
			nacitaSe = false;
		}
	}

	void registerEvents() {
		final ChangeListener chlist = e -> {
			if (nacitaSe) {
				return;
			}
			final int pruhlednost = pruhlednostSlider.getValue();
			pruhlednostLabel.setText(textPruhlednosti(pruhlednost));
			kruhyModel.setData(KruhyZDialogu.zmen(kruhyModel.getData(), barvaChooser.getSelectionModel().getSelectedColor(), pruhlednost, velikostSlider.getValue(),
			        jednotkoveKruhy.isSelected()));
		};
		velikostSlider.getModel().addChangeListener(chlist);
		pruhlednostSlider.getModel().addChangeListener(chlist);
		barvaChooser.getSelectionModel().addChangeListener(chlist);
		jednotkoveKruhy.getModel().addChangeListener(chlist);
	}

	private static String textPruhlednosti(final int procent) {
		return "Průhlednost kruhů: " + procent + " %";
	}
}
