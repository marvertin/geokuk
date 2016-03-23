package cz.geokuk.plugins.cesty.akce.soubor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;

import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.cesty.CestyChangedEvent;
import cz.geokuk.plugins.cesty.data.Doc;

public class UlozJakoAction extends SouboeCestaAction0 {

	private static final long	serialVersionUID	= 1L;
	private Doc					doc;

	public UlozJakoAction() {
		super("Uložit cesty (gpx) jako ...");
		putValue(SHORT_DESCRIPTION, "Uloží zadaný výlet do jiného souboru GPX");
		putValue(MNEMONIC_KEY, KeyEvent.VK_V);
		// putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {

		final JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new GpxFilter());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setSelectedFile(cestyModel.getImplicitniVyletSaveAsNovyFile());
		final int result = fc.showDialog(Dlg.parentFrame(), "Uložit jako");
		if (result == JFileChooser.APPROVE_OPTION) {
			final File selectedFile = doplnGpxPriponuProUkladani(fc.getSelectedFile());
			if (selectedFile.exists()) { // dtaz na přepsání
				if (!Dlg.prepsatSoubor(selectedFile)) {
					return;
				}
			}
			doc.setFile(selectedFile);
			cestyModel.uloz(doc.getFile(), doc, true);
			System.out.println("Uložena cesta do: " + doc.getFile());
		}
		// TODO ukládat na pozadí
	}

	public void onEvent(final CestyChangedEvent event) {
		doc = event.getDoc();
		setEnabled(doc != null && !doc.isEmpty());
	}

}
