package cz.geokuk.plugins.kesoid.kind.kes;

import java.net.URL;

import cz.geokuk.framework.OpenUrlAction0;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;
import lombok.SneakyThrows;

/**
 * @author Martin Veverka
 *
 */
public class TiskniNaGcComAction extends OpenUrlAction0 {

	private static final long serialVersionUID = -8054017274338240706L;

	private static String URL_PREFIX_PRINT = "https://www.geocaching.com/seek/cdpf.aspx?guid=";
	private static String URL_PREFIX_SHOW = "https://www.geocaching.com/seek/cache_details.aspx?guid=";

	/**
	 *
	 */
	public TiskniNaGcComAction(final Wpt wpt) {
		super(getUrlPrint(wpt), "Tisk na gc.com", ImageLoader.seekResIcon("gccom-green-16.png"));
		putValue(SHORT_DESCRIPTION, "Zobrazí listing keše na oficiál ním webu \"geocachin.com\" v tisknutelné podobě.");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

	}


	@SneakyThrows
	private static URL getUrlPrint(final Wpt wpt) {
		final String urls = wpt.getKesoid().getUrl().toExternalForm();
		System.out.println(urls);
		if (urls.startsWith(URL_PREFIX_SHOW)) {
			return new URL(URL_PREFIX_PRINT + urls.substring(URL_PREFIX_SHOW.length()));
		} else {
			return null;
		}
	}
}
