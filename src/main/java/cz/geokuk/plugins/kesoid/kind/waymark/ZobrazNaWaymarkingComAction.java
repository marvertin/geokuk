/**
 *
 */
package cz.geokuk.plugins.kesoid.kind.waymark;

import java.net.MalformedURLException;

import cz.geokuk.framework.OpenUrlAction0;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.Wpti;
import lombok.SneakyThrows;

/**
 * @author Martin Veverka
 *
 */
public class ZobrazNaWaymarkingComAction extends OpenUrlAction0 {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 * @throws MalformedURLException
	 *
	 */
	@SneakyThrows
	public ZobrazNaWaymarkingComAction(final Wpt wpt)  {
		super(((Wpti)wpt).getUrl());
		putValue(NAME, "Zobrazení na waymarking.com");
		putValue(SMALL_ICON, ImageLoader.seekResIcon("waymarking.png"));
		putValue(SHORT_DESCRIPTION, "Zobrazí listing waymarku na oficialním webu \"waymarking.com\".");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
	}



}
