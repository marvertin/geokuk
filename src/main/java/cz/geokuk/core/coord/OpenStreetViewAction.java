package cz.geokuk.core.coord;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Action0;

public class OpenStreetViewAction extends Action0 {
	private static final Logger	log						= LogManager.getLogger(OpenStreetViewAction.class.getSimpleName());

	private static final long	serialVersionUID		= -8054017274338240706L;

	private static final String	STREET_VIEW_TEMPLATE	= "http://maps.google.com/maps?layer=c&cbll=%.6f,%.6f";

	private Poziceq				poziceq					= new Poziceq();
	private final Mouable		mouable;

	public OpenStreetViewAction(Mouable mouable) {
		super(null);
		this.mouable = mouable;
		putValue(NAME, "Otevřít StreetView");
		putValue(SHORT_DESCRIPTION, "Otevře defaultní prohlížeč se StreetView v daném místě.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(ActionEvent e) {
		Mouable m = mouable;
		if (m == null) {
			m = poziceq.getPoziceMouable();
			if (m == null) {
				return;
			}
		}
		Wgs wgs = mouable.getMou().toWgs();
		openBrowserWithStreetView(wgs);
	}

	private static URI getStreetViewUri(Wgs wgs) {
		try {
			return new URI(String.format(Locale.US, STREET_VIEW_TEMPLATE, wgs.lat, wgs.lon));
		} catch (URISyntaxException e) {
			throw new AssertionError("Someone has messed up the URI!", e);
		}
	}

	private static void openBrowserWithStreetView(Wgs coordinates) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					desktop.browse(getStreetViewUri(coordinates));
				} catch (IOException e) {
					log.error("Desktop environment doesn't support browsing actions!", e);
				}
			} else {
				log.error("Desktop environment doesn't support browsing actions!");
			}
		} else {
			log.error("Desktop environment isn't supported!");
		}
	}

	public void onEvent(PoziceChangedEvent event) {
		poziceq = event.poziceq;
		setEnabled(mouable != null || !poziceq.isNoPosition());
	}
}
