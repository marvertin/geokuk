package cz.geokuk.plugins.cesty.akce.soubor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.cesty.CestyModel;

public class ImportujAction extends Action0 {

	private static final long	serialVersionUID	= 1L;
	private CestyModel			cestyModel;
	private JFileChooser		fc;

	public ImportujAction() {
		super("Importovat cesty (gpx, ggt)");
		putValue(SHORT_DESCRIPTION, "Do otevřeného výletu importuje cesty z GPX souborů a GGT soubory.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_V);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
		// putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (fc == null) { // dlouho to trvá, tak vytvoříme vždy nový
			fc = new JFileChooser();
			fc.addChoosableFileFilter(new GpxFilter());
			fc.addChoosableFileFilter(new GgtFilter());
			fc.addChoosableFileFilter(new AllAkceptableFilter());
		}
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setMultiSelectionEnabled(true);
		// fc.setCurrentDirectory(new File(jtext.getText()));
		fc.setSelectedFile(cestyModel.defaultAktualnihoVyletuFile());
		int result = fc.showDialog(Dlg.parentFrame(), "Importovat");
		if (result == JFileChooser.APPROVE_OPTION) {
			File[] selectedFiles = fc.getSelectedFiles();
			cestyModel.importuj(Arrays.asList(selectedFiles));
			System.out.println("Nactena cesta z: " + Arrays.asList(selectedFiles));
		}
	}

	public void inject(CestyModel cestyModel) {
		this.cestyModel = cestyModel;
	}

	private class AllAkceptableFilter extends FileFilter {

		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory())
				return true;
			if (pathname.getName().toLowerCase().endsWith(".gpx"))
				return true;
			if (pathname.getName().toLowerCase().endsWith(".ggt"))
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "Vše co umíme načíst (*.gpx, *.ggt)";
		}

	}

}
