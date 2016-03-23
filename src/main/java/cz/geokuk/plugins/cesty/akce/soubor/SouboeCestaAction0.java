package cz.geokuk.plugins.cesty.akce.soubor;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.Dlg;
import cz.geokuk.plugins.cesty.CestyModel;
import cz.geokuk.plugins.cesty.data.Doc;

public abstract class SouboeCestaAction0 extends Action0 {

	private static final long	serialVersionUID	= -2637836928166450446L;

	protected CestyModel		cestyModel;

	public SouboeCestaAction0(final String string) {
		super(string);
	}

	public void inject(final CestyModel cestyModel) {
		this.cestyModel = cestyModel;

	}

	public boolean ulozitSDotazem() {
		if (!cestyModel.getDoc().isChanged()) {
			return true; // nezměna znamená uloženo
		}
		final Object[] options = { "Uložit změny", "Zahodit změny", "Zrušit" };
		final String hlaska = cestyModel.getDoc().getFile() != null ? "<html>Soubor s výletem byl změněn <b>" + cestyModel.getDoc().getFile() + "</b> "
				: "Byl vytvořen nový výlet, ale nebyl doposud uložen do souboru." + ".";
		final int n = JOptionPane.showOptionDialog(Dlg.parentFrame(), hlaska, "Uložení změn ve výletu", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
		System.out.println(n);
		if (n == 0) {
			return ulozit();
		} else {
			return n == 1;
		}

	}

	protected boolean ulozit() {
		final Doc xdoc = cestyModel.getDoc();
		if (xdoc.getFile() == null) { // ještě nebyl určen soubor, musíme se zeptat
			final JFileChooser fc = new JFileChooser();
			fc.addChoosableFileFilter(new GpxFilter());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setSelectedFile(cestyModel.getImplicitniVyletNovyFile());
			final int result = fc.showDialog(Dlg.parentFrame(), "Uložit");
			if (result == JFileChooser.APPROVE_OPTION) {
				final File selectedFile = doplnGpxPriponuProUkladani(fc.getSelectedFile());
				if (selectedFile.exists()) { // dtaz na přepsání
					if (!Dlg.prepsatSoubor(selectedFile)) {
						return false;
					}
				}
				xdoc.setFile(selectedFile);
			} else {
				return false;
			}
		}
		// TODO ukládat na pozadí a také mít jinde ukládací dialog
		cestyModel.uloz(xdoc.getFile(), xdoc, true);
		System.out.println("Uložena cesta do: " + xdoc.getFile());
		return true;
	}

	File doplnGgtPriponuProUkladani(final File file) {
		if (file == null) {
			return null;
		}
		if (file.getName().toLowerCase().endsWith(".ggt")) {
			return file;
		}
		return new File(file.getPath() + ".ggt");
	}

	File doplnGpxPriponuProUkladani(final File file) {
		if (file == null) {
			return null;
		}
		if (file.getName().toLowerCase().endsWith(".gpx")) {
			return file;
		}
		return new File(file.getPath() + ".gpx");
	}

}
