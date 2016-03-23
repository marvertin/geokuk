package cz.geokuk.plugins.cesty.akce.soubor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.cesty.CestyChangedEvent;
import cz.geokuk.plugins.cesty.data.Doc;

public class ExportujDoGgtAction extends SouboeCestaAction0 {

	private static final Logger	log					= LogManager.getLogger(ExportujDoGgtAction.class.getSimpleName());

	private static final long	serialVersionUID	= 1L;
	private Doc					doc;

	public ExportujDoGgtAction() {
		super("Exportovat cesty do GGT ...");
		putValue(SHORT_DESCRIPTION, "Uloží zadaný výlet do GGT soubor, vhodné pro Geoget");
		putValue(MNEMONIC_KEY, KeyEvent.VK_E);
		// putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new GgtFilter());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setSelectedFile(cestyModel.defaultExportuDoGgt());
		int result = fc.showDialog(Dlg.parentFrame(), "Exportovat GGT");
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = doplnGgtPriponuProUkladani(fc.getSelectedFile());
			if (selectedFile.exists()) { // dtaz na přepsání
				if (!Dlg.prepsatSoubor(selectedFile))
					return;
			}
			cestyModel.exportujDoGgt(selectedFile, doc);
			log.info("Uložena cesta do: " + doc.getFile());
		}
		// TODO ukládat na pozadí
	}

	public void onEvent(CestyChangedEvent event) {
		doc = event.getDoc();
		setEnabled(doc != null && (!doc.isEmpty()));
	}

}
