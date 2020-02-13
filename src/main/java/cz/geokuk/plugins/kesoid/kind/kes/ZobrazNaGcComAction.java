/**
 *
 */
package cz.geokuk.plugins.kesoid.kind.kes;

import java.net.MalformedURLException;

import cz.geokuk.framework.OpenUrlAction0;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;
import lombok.SneakyThrows;

/**
 * @author Martin Veverka
 *
 */
public class ZobrazNaGcComAction extends OpenUrlAction0 {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 * @throws MalformedURLException
	 *
	 */
	@SneakyThrows
	public ZobrazNaGcComAction(final Wpt wpt)  {
		super(url("https://coord.info/" + wpt.getKoid().getIdentifier())); // sem se nesmí dát aditional waypoint
		putValue(NAME, "Zobrazení na gc.com");
		putValue(SMALL_ICON, ImageLoader.seekResIcon("gccom-green-16.png"));
		putValue(SHORT_DESCRIPTION, "Zobrazí listing keše na oficiálním webu webu \"geocaching.com\".");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
	}



}
