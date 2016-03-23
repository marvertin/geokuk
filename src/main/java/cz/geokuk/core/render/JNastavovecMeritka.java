package cz.geokuk.core.render;

import java.awt.Dimension;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.EKaType;

public class JNastavovecMeritka extends JSpinner implements AfterEventReceiverRegistrationInit {

	private static final long		serialVersionUID	= -484273090975902036L;

	public final SpinnerNumberModel	iModel				= new SpinnerNumberModel(0, 0, 100, 1);

	private RenderModel				renderModel;

	/**
	 *
	 */
	public JNastavovecMeritka() {
		setModel(iModel);
		// setMaximumSize(new Dimension(35, 22));
	}

	public void onEvent(ZmenaMapNastalaEvent event) {
		EKaType podklad = event.getKaSet().getPodklad();
		iModel.setMinimum(podklad.getMinMoumer());
		iModel.setMaximum(podklad.getMaxMoumer());
		iModel.setValue(podklad.fitMoumer((Integer) iModel.getValue()));
	}

	public void onEvent(PripravaRendrovaniEvent event) {
		iModel.setValue(event.getRenderSettings().getRenderedMoumer());
	}

	@Override
	public void initAfterEventReceiverRegistration() {
		iModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				RenderSettings settings = renderModel.getRenderSettings();
				settings.setRenderedMoumer((Integer) iModel.getNumber());
				renderModel.setRenderSettings(settings);
			}
		});
	}

	public void inject(RenderModel renderModel) {
		this.renderModel = renderModel;

	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
	}

}