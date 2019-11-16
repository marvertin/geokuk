package cz.geokuk.core.render;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;

public class JNastavovecMeritka extends JSpinner implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = -484273090975902036L;

	public final SpinnerNumberModel iModel = new SpinnerNumberModel(0, 0, 100, 1);

	private RenderModel renderModel;

	/**
	 *
	 */
	public JNastavovecMeritka() {
		setModel(iModel);
		// setMaximumSize(new Dimension(35, 22));
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
	}

	@Override
	public void initAfterEventReceiverRegistration() {
		iModel.addChangeListener(e -> {
			final RenderSettings settings = renderModel.getRenderSettings();
			settings.setRenderedMoumer((Integer) iModel.getNumber());
			renderModel.setRenderSettings(settings);
		});
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

}