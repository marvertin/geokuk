package cz.geokuk.core.render;

import javax.swing.*;
import javax.swing.event.ChangeListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;

public class JNastavovacVelikostiDlazdic extends JPanel implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = -484273090975902036L;

	public final SpinnerNumberModel iModel = new SpinnerNumberModel(10, 10, 10000, 1);

	private RenderModel renderModel;

	private JSpinner jMaximalniVelikost;
	private JTextField jSkutecnaVelikost;
	private JTextField jPocetDlazdic;

	/**
	 *
	 */
	public JNastavovacVelikostiDlazdic(final String smer) {
		initComponents(smer);

	}

	public void addChangeListener(final ChangeListener listener) {
		jMaximalniVelikost.addChangeListener(listener);
	}

	/**
	 * @return
	 */
	public Integer getMaximalniVelikost() {
		return (Integer) iModel.getValue();
	}

	@Override
	public void initAfterEventReceiverRegistration() {
		registerEvents();
	}

	public void inject(final RenderModel renderModel) {
		this.renderModel = renderModel;

	}

	public void onEvent(final PripravaRendrovaniEvent event) {
		iModel.setValue(event.getRenderSettings().getRenderedMoumer());
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		final EKaType podklad = event.getKatype();
		iModel.setMinimum(podklad.getMinMoumer());
		iModel.setMaximum(podklad.getMaxMoumer());
		iModel.setValue(podklad.fitMoumer((Integer) iModel.getValue()));
	}

	/**
	 * @param kmzMaxDlazdiceX
	 */
	public void setMaximalniVelikost(final int kmzMaxDlazdice) {
		iModel.setValue(kmzMaxDlazdice);
	}

	public void setMetrika(final DlazdicovaMetrika metrika) {
		jSkutecnaVelikost.setText(metrika.dlaSize + "");
		jPocetDlazdic.setText(metrika.dlaPocet + "");
	}

	/**
	 *
	 */
	private void initComponents(final String smer) {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		jMaximalniVelikost = new JSpinner();
		jMaximalniVelikost.setModel(iModel);
		jSkutecnaVelikost = new JTextField();
		jSkutecnaVelikost.setColumns(5);
		jSkutecnaVelikost.setEditable(false);
		jPocetDlazdic = new JTextField();
		jPocetDlazdic.setColumns(5);
		jPocetDlazdic.setEditable(false);

		add(jMaximalniVelikost);
		add(Box.createHorizontalStrut(5));
		add(jSkutecnaVelikost);
		add(Box.createHorizontalStrut(5));
		add(jPocetDlazdic);
		add(Box.createHorizontalStrut(5));
		add(new JLabel("(maximum / aktuálně / počet)"));

		jMaximalniVelikost.setToolTipText("Maximální veliksot dlaždice v pixlech ve směru " + smer + ".");
		jSkutecnaVelikost.setToolTipText("Skutečně rendrovaná velikost dlaždice ve směru " + smer + ", není větší než velikost aktuální.");
		jPocetDlazdic.setToolTipText("Počet rendrovaných dlaždic ve směru " + smer + ".");

	}

	/**
	 *
	 */
	private void registerEvents() {
		iModel.addChangeListener(e -> {
			final RenderSettings settings = renderModel.getRenderSettings();
			settings.setKmzMaxDlazdiceX((Integer) iModel.getNumber());
			renderModel.setRenderSettings(settings);
		});
	}

}