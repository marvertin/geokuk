package cz.geokuk.plugins.kesoidkruhy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeListener;

/** Ovládací prvky nastavení zvýrazňovacích kruhů: velikost v pixelech nebo v metrech, průhlednost a barva. Zapnutí kruhů panel nemění. */
final class PanelKruhu extends Box {

	private static final long serialVersionUID = 1L;

	final JRadioButton vPixelech = new JRadioButton("v pixelech");
	final JRadioButton vMetrech = new JRadioButton("v metrech");
	final JSlider velikostSlider = new JSlider(PolomerKruhu.MIN_PIXLU_VELIKOST, PolomerKruhu.MAX_PIXLU_VELIKOST, 20);
	final JTextField velikostPole = new JTextField(9);
	final JSlider polomerSlider = new JSlider(0, PolomerKruhu.PRESETY.length - 1, PolomerKruhu.pozice(PolomerKruhu.VYCHOZI_METRU));
	final JTextField polomerPole = new JTextField(9);
	final JLabel pruhlednostLabel = new JLabel();
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

	private KruhyModel kruhyModel;
	/** Ovládací prvky se právě nastavují z modelu, jejich změny se do modelu nevracejí. */
	private boolean nacitaSe;
	/** Poslední platný poloměr jednotkových kruhů v metrech. */
	private int polomer = PolomerKruhu.VYCHOZI_METRU;

	PanelKruhu() {
		super(BoxLayout.Y_AXIS);
		final ButtonGroup skupina = new ButtonGroup();
		skupina.add(vPixelech);
		skupina.add(vMetrech);
		vPixelech.setSelected(true);
		vPixelech.setToolTipText("Kruh má při každém přiblížení mapy stejnou velikost na obrazovce.");
		vMetrech.setToolTipText("Kruh má skutečnou velikost na zemi, při přiblížení mapy se jeho velikost na obrazovce mění.");
		velikostSlider.setToolTipText("Velikost kruhu v pixelech.");
		velikostPole.setToolTipText("Velikost kruhu v pixelech od 0 do 100, například 40 nebo 40 px. Potvrdí se Enterem.");
		velikostPole.setText(PolomerKruhu.formatPixely(velikostSlider.getValue()));
		polomerSlider.setToolTipText("Poloměr kruhu na zemi; posuvník se přichytává k běžným hodnotám, jinou hodnotu zadejte do pole.");
		polomerPole.setToolTipText("Poloměr kruhu na zemi od 1 m do 50 km, například 161, 333 m nebo 1,5 km. Potvrdí se Enterem.");
		polomerPole.setText(PolomerKruhu.format(polomer));
		polomerSlider.setSnapToTicks(true);
		polomerSlider.setMajorTickSpacing(1);
		polomerSlider.setPaintTicks(true);
		polomerSlider.setLabelTable(popiskyPosuvnikuPolomeru());
		polomerSlider.setPaintLabels(true);
		pruhlednostLabel.setText(textPruhlednosti(0));
		pruhlednostSlider.setToolTipText("Průhlednost zvýrazňovacího kruhu: 0 % je plně krycí, 100 % neviditelný.");
		barvaChooser.setToolTipText("Nastavení barvy zvýrazňovacího kruhu");

		final JPanel volba = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
		volba.add(new JLabel("Velikost kruhu:"));
		volba.add(vPixelech);
		volba.add(vMetrech);
		add(volba);
		add(Box.createVerticalStrut(6));
		add(new JLabel("Velikost kruhu v pixelech"));
		add(radek(velikostSlider, velikostPole));
		add(Box.createVerticalStrut(10));
		add(new JLabel("Poloměr kruhu v metrech"));
		add(radek(polomerSlider, polomerPole));
		add(Box.createVerticalStrut(10));
		add(pruhlednostLabel);
		add(pruhlednostSlider);
		add(Box.createVerticalStrut(10));
		add(barvaChooser);
		uplatniJednotkovost();
	}

	private static JPanel radek(final JSlider posuvnik, final JTextField pole) {
		final JPanel radek = new JPanel(new BorderLayout(8, 0));
		radek.add(posuvnik, BorderLayout.CENTER);
		final JPanel poleVyska = new JPanel(new BorderLayout());
		poleVyska.add(pole, BorderLayout.NORTH);
		radek.add(poleVyska, BorderLayout.EAST);
		return radek;
	}

	private static Hashtable<Integer, JLabel> popiskyPosuvnikuPolomeru() {
		final Hashtable<Integer, JLabel> popisky = new Hashtable<>();
		for (int i = 0; i < PolomerKruhu.PRESETY.length; i++) {
			final int m = PolomerKruhu.PRESETY[i];
			final JLabel l = new JLabel(m >= 1000 ? m / 1000 + " km" : String.valueOf(m));
			l.setFont(l.getFont().deriveFont(10f));
			popisky.put(i, l);
		}
		return popisky;
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

	boolean isVMetrech() {
		return vMetrech.isSelected();
	}

	void onEvent(final KruhyPreferencesChangeEvent event) {
		nacitaSe = true;
		try {
			vMetrech.setSelected(event.kruhy.isJednotkovaVelikost());
			vPixelech.setSelected(!event.kruhy.isJednotkovaVelikost());
			velikostSlider.getModel().setValue(event.kruhy.getVelikost());
			velikostPole.setText(PolomerKruhu.formatPixely(event.kruhy.getVelikost()));
			polomer = event.kruhy.getPolomerMetru();
			polomerSlider.getModel().setValue(PolomerKruhu.pozice(polomer));
			polomerPole.setText(PolomerKruhu.format(polomer));
			final Color barva = event.kruhy.getBarva();
			barvaChooser.getSelectionModel().setSelectedColor(new Color(barva.getRGB()));
			final int pruhlednost = KruhyZDialogu.pruhlednostProcent(barva.getAlpha());
			pruhlednostSlider.getModel().setValue(pruhlednost);
			pruhlednostLabel.setText(textPruhlednosti(pruhlednost));
			uplatniJednotkovost();
		} finally {
			nacitaSe = false;
		}
	}

	void registerEvents() {
		final ChangeListener chlist = e -> {
			if (nacitaSe) {
				return;
			}
			uplatniJednotkovost();
			uloz();
		};
		pruhlednostSlider.getModel().addChangeListener(chlist);
		barvaChooser.getSelectionModel().addChangeListener(chlist);
		vMetrech.getModel().addChangeListener(chlist);
		velikostSlider.getModel().addChangeListener(e -> {
			if (nacitaSe) {
				return;
			}
			velikostPole.setText(PolomerKruhu.formatPixely(velikostSlider.getValue()));
			uloz();
		});
		polomerSlider.getModel().addChangeListener(e -> {
			if (nacitaSe) {
				return;
			}
			polomer = PolomerKruhu.metru(polomerSlider.getValue());
			polomerPole.setText(PolomerKruhu.format(polomer));
			uloz();
		});
		polomerPole.addActionListener(e -> potvrdPolomerZPole());
		polomerPole.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e) {
				potvrdPolomerZPole();
			}
		});
		velikostPole.addActionListener(e -> potvrdPixelyZPole());
		velikostPole.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(final FocusEvent e) {
				potvrdPixelyZPole();
			}
		});
	}

	/** Neplatná hodnota se vrátí na poslední platnou. */
	void potvrdPixelyZPole() {
		final int px = PolomerKruhu.parsujPixely(velikostPole.getText());
		if (px < 0) {
			velikostPole.setText(PolomerKruhu.formatPixely(velikostSlider.getValue()));
			return;
		}
		velikostPole.setText(PolomerKruhu.formatPixely(px));
		if (px != velikostSlider.getValue()) {
			// Změna posuvníku sama zapíše hodnotu do modelu.
			velikostSlider.setValue(px);
		}
	}

	/** Neplatná hodnota se vrátí na poslední platnou, platná nastaví posuvník na nejbližší pozici bez přichycení hodnoty. */
	void potvrdPolomerZPole() {
		final int metru = PolomerKruhu.parsuj(polomerPole.getText());
		if (metru == 0) {
			polomerPole.setText(PolomerKruhu.format(polomer));
			return;
		}
		polomerPole.setText(PolomerKruhu.format(metru));
		if (metru == polomer) {
			return;
		}
		polomer = metru;
		nacitaSe = true;
		try {
			polomerSlider.setValue(PolomerKruhu.pozice(metru));
		} finally {
			nacitaSe = false;
		}
		uloz();
	}

	/** Rozhoduje přepínač: při velikosti v metrech se používá posuvník poloměru, jinak posuvník pixelů. */
	private void uplatniJednotkovost() {
		final boolean metry = isVMetrech();
		velikostSlider.setEnabled(!metry);
		velikostPole.setEnabled(!metry);
		polomerSlider.setEnabled(metry);
		polomerPole.setEnabled(metry);
	}

	private void uloz() {
		final int pruhlednost = pruhlednostSlider.getValue();
		pruhlednostLabel.setText(textPruhlednosti(pruhlednost));
		kruhyModel.setData(KruhyZDialogu.zmen(kruhyModel.getData(), barvaChooser.getSelectionModel().getSelectedColor(), pruhlednost, velikostSlider.getValue(), isVMetrech(), polomer));
	}

	private static String textPruhlednosti(final int procent) {
		return "Průhlednost kruhů: " + procent + " %";
	}
}
