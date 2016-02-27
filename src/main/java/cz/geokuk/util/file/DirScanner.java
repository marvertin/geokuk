package cz.geokuk.util.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Třída je zodpověd na projití zadaných rootu
 * a držení informací o tom, zda nedošlo ke změně.
 */
public  class DirScanner {

  // case insensitive, TODO : other image formats than JPG, raw and tif

  // TODO : Use file watchers
  private List<Root> roots;
  private List<KeFile> lastScaned = null;

  public void seRootDirs(boolean prenacti, Root... roots) {
    List<Root> newRoots = Arrays.asList(roots);
    if (!prenacti && newRoots.equals(this.roots)) {
      return;
    }
    this.roots = newRoots;
    nulujLastScaned();
  }

  public synchronized void nulujLastScaned() {
    lastScaned = null;
  }
  /**
   * Vrátí null, pokud není co načítat, protože nedošlo ke změně.
   * Prázdný seznam je něco jiného, to ke změně došlo takové, že
   * zmizely všechny soubory.
   * Když se změní byť jediný soubor, je to změna a načítá se.
   * @return
   */
  public synchronized List<KeFile> coMamNacist() {
    Set<KeFile> set = new TreeSet<>();
    for (Root dir : roots) {
      List<KeFile> li = scanDir(dir);
      set.addAll(li);
    }
    List<KeFile> list = new ArrayList<>(set);
    if (list.equals(lastScaned)) return null; // nezměnilo se nic
    lastScaned = list;
    return list;
  }

  private List<KeFile> scanDir(final Root root) {
    List<KeFile> list = new ArrayList<>();
    if (! root.dir.isDirectory()) return list;
    Deque<File> deque = new ArrayDeque<>();
    deque.add(root.dir);
    while (!deque.isEmpty()) {
      File f = deque.pop();
      if (f.isDirectory()) {
        deque.addAll(Arrays.asList(f.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isDirectory() || root.pattern.matcher(pathname.getName()).matches();
          }
        })));
      } else {
        list.add(new KeFile(new FileAndTime(f), root));
      }
    }

    Collections.sort(list); // podle času
    return list;
  }
}
