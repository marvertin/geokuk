package cz.geokuk.plugins.cesty.akce.soubor;

import java.io.File;

import javax.swing.filechooser.FileFilter;

class GgtFilter extends FileFilter {

	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory())
			return true;
		if (pathname.getName().toLowerCase().endsWith(".ggt"))
			return true;
		return false;
	}

	@Override
	public String getDescription() {
		return "Seznamy Geoetu (*.ggt)";
	}

}