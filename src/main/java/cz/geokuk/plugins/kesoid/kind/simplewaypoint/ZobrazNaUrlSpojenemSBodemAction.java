/**
 *
 */
package cz.geokuk.plugins.kesoid.kind.simplewaypoint;

import java.net.MalformedURLException;

import cz.geokuk.framework.OpenUrlAction0;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;
import lombok.SneakyThrows;

/**
 * @author Martin Veverka
 *
 */
public class ZobrazNaUrlSpojenemSBodemAction extends OpenUrlAction0 {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 * @throws MalformedURLException
	 *
	 */
	@SneakyThrows
	public ZobrazNaUrlSpojenemSBodemAction(final Wpt wpt)  {
		super(wpt.getKesoid().getUrl());
		putValue(NAME, "Zobrazení na webu");
		putValue(SMALL_ICON, ImageLoader.seekResIcon("internet.png"));
		putValue(SHORT_DESCRIPTION, "Zobrazí nawebu spojeném s bodem.");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
	}



}