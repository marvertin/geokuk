package cz.geokuk.framework;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.Icon;

import cz.geokuk.util.process.BrowserOpener;
import lombok.SneakyThrows;

public class OpenUrlAction0 extends Action0 {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private final URL url;

	public OpenUrlAction0(final URL url) {
		this.url = url;
	}

	public OpenUrlAction0(final URL url, final String string) {
		super(string);
		this.url = url;
	}

	public OpenUrlAction0(final URL url, final String aString, final Icon aIcon) {
		super(aString, aIcon);
		this.url = url;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		BrowserOpener.displayURL(url);
	}

	@Override
	public boolean shouldBeVisible() {
		return url != null;
	}

	@SneakyThrows
	protected static URL url(final String url) {
		return new URL(url);
	}
}
