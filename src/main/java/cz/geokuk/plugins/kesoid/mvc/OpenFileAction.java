package cz.geokuk.plugins.kesoid.mvc;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import cz.geokuk.framework.Action0;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenFileAction extends Action0 {

	private static final long serialVersionUID = 9155605907779458631L;

	private final File fileToOpen;

	public OpenFileAction(final File file) {
		super("Otevřít v defaultním programu");
		fileToOpen = file;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (!Desktop.isDesktopSupported()) {
			log.error("Unable to retrieve Desktop environment. not supported on current platform!");
		}
		try {
			Desktop.getDesktop().open(fileToOpen);
		} catch (final IOException exception) {
			log.error("Unable to open file " + fileToOpen, exception);
		}
	}

	@Override
	public boolean shouldBeVisible() {
		return fileToOpen != null;
	}
}
