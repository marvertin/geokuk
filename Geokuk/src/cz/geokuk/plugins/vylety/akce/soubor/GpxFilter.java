package cz.geokuk.plugins.vylety.akce.soubor;

import java.io.File;

import javax.swing.filechooser.FileFilter;

class GpxFilter extends FileFilter {

  @Override
  public boolean accept(File pathname) {
    if (pathname.isDirectory()) return true;
    if (pathname.getName().toLowerCase().endsWith(".gpx")) return true;
    return false;
  }

  @Override
  public String getDescription() {
    return "Soubory s cestami (*.gpx)";
  }

}