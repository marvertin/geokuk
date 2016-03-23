package cz.geokuk.plugins.geocoding;

import java.awt.event.ActionEvent;
import java.net.URL;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Action0;
import cz.geokuk.util.process.BrowserOpener;

public class GeocodingBorowserXmlAction extends Action0 {

	private static final long serialVersionUID = -5194259213320265512L;
	private final Wgs wgs;

	public GeocodingBorowserXmlAction(final Wgs wgs) {
		super("Geocoding ...");
		putValue(SHORT_DESCRIPTION, "Zobrazí Internetový prohlížeč s detetailními informacemi o vybraném místě. Dokument je ve formátu XML, tak jak je vracen příslušnou službou.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_A);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('M', InputEvent.CTRL_DOWN_MASK));

		this.wgs = wgs;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final HledaciPodminka podm = new HledaciPodminka();
		podm.setStredHledani(wgs);
		podm.setVzorek(wgs.lat + "," + wgs.lon);
		final URL url = podm.computeUrl();
		BrowserOpener.displayURL(url);

	}

}
