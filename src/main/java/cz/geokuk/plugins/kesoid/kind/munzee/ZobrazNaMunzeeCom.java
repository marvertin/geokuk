/**
 *
 */
package cz.geokuk.plugins.kesoid.kind.munzee;

import java.net.MalformedURLException;

import cz.geokuk.framework.OpenUrlAction0;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;
import lombok.SneakyThrows;

/**
 * @author Martin Veverka
 *
 */
public class ZobrazNaMunzeeCom extends OpenUrlAction0 {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 * @throws MalformedURLException
	 *
	 */
	@SneakyThrows
	public ZobrazNaMunzeeCom(final Wpt wpt)  {
		super(wpt.getKesoid().getUrl());
		putValue(NAME, "Zobrazení na munzee.com");
		putValue(SMALL_ICON, ImageLoader.seekResIcon("munzee.png"));
		putValue(SHORT_DESCRIPTION, "Zobrazí listing munzee na oficiálním webu munzee.com.");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
	}



}
