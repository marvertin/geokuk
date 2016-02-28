package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.geokuk.util.file.KeFile;

public class InformaceOZdroji {
  public final KeFile jmenoZdroje;
  public int pocetWaypointuCelkem;
  public int pocetWaypointuBranych;
  public int pocetWaypointuCelkemChildren;
  public int pocetWaypointuBranychChildren;
  public final boolean nacteno;
  private final List<InformaceOZdroji> children = new ArrayList<>();
  public InformaceOZdroji parent;

  public InformaceOZdroji(KeFile jmenoZdroje, boolean nacteno) {
    this.jmenoZdroje = jmenoZdroje;
    this.nacteno = nacteno;
  }

  public long getLastModified() {
    return jmenoZdroje.getLastModified();
  }
  
  public void addChild(InformaceOZdroji child) {
    // TODO : use a sane data structure for this
    children.add(child);
    Collections.sort(children, InformaceOZdrojiComparator.INSTANCE);
  }
  
  public InformaceOZdroji removeChild(int index) {
    InformaceOZdroji odebranec = children.remove(index);
    odebranec.parent = null;
    return odebranec;
  }

  public int remplaceChild(InformaceOZdroji iozOld, InformaceOZdroji iozNew) {
    int index = children.indexOf(iozOld);
    children.set(index, iozNew);
    return index;
  }

  public List<InformaceOZdroji> getChildren() {
    return Collections.unmodifiableList(children);
  }

  private static class InformaceOZdrojiComparator implements Comparator<InformaceOZdroji> {
    private static final InformaceOZdrojiComparator INSTANCE = new InformaceOZdrojiComparator();

    @Override
    public int compare(InformaceOZdroji o1, InformaceOZdroji o2) {
      return FileDirComparator.INSTANCE.compare(o1.jmenoZdroje.getFile(), o2.jmenoZdroje.getFile());
    }
  }

  private static class FileDirComparator implements Comparator<File> {
    private static final FileDirComparator INSTANCE = new FileDirComparator();

    @Override
    public int compare(File file1, File file2) {
      if (file1.isDirectory() && file2.isFile()) {
        return -1;
      } else if (file1.isFile() && file2.isDirectory()) {
        return 1;
      } else {
        return compareNames(file1, file2);
      }
    }

    private int compareNames(File file1, File file2) {
      return file1.getName().compareToIgnoreCase(file2.getName());
    }
  }
 
  
  /**
   * Rekuzivně vypíše intormace
   * @param maToBytParent
   */
  public void print(String odsazovac, InformaceOZdroji maToBytParent) {
    String parentProblem = parent == maToBytParent ? "" : "!!!  NESEDI PARENT: " + parent + " <> " + maToBytParent; 
    System.out.println(odsazovac + jmenoZdroje.getFile() + parentProblem);
    for (InformaceOZdroji info : getChildren()) {
      info.print(odsazovac + "  ", this);
    }
  }

  @Override
  public String toString() {
    return jmenoZdroje.toString();
  }

  public String getDisplayName() {
    if (parent.parent != null) {
      return parent.jmenoZdroje.getFile().toPath().relativize(jmenoZdroje.getFile().toPath()).toString();
    } else {
      return jmenoZdroje.getFile().getAbsolutePath();
    }
  }

  void spocitejSiPocetWaipointuChildren() {
    pocetWaypointuBranychChildren = 0;
    pocetWaypointuCelkemChildren = 0;
    for (InformaceOZdroji ioz : children) {
      ioz.spocitejSiPocetWaipointuChildren();
      pocetWaypointuBranychChildren += ioz.getPocetWaypointuBranychSDetmi();
      pocetWaypointuCelkemChildren += ioz.getPocetWaypointuCelkemSDetmi();
    }
    
  }

  public int getPocetWaypointuBranychSDetmi() {
    return pocetWaypointuBranych + pocetWaypointuBranychChildren;
  }

  public int getPocetWaypointuCelkemSDetmi() {
    return pocetWaypointuCelkem + pocetWaypointuCelkemChildren;
  }
}