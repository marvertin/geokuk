package cz.geokuk.util.file;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author veverka
 */
public  class DirScaner {

  // case insensitive, TODO : other image formats than JPG
  private static final Pattern FILE_NAME_REGEX = Pattern.compile("(?i).*\\.(geokuk|gpx|zip|jpg)");

  // TODO : Use file watchers
  private File dir;
  private List<FileAndTime> lastScaned = null;

  public void setDir(File dir, boolean prenacti) {
    if (!prenacti && dir.equals(this.dir)) {
      return;
    }
    this.dir = dir;
    nulujLastScaned();
  }

  public synchronized void nulujLastScaned() {
    lastScaned = null;
  }
  /**
   * Vrátí null, pokud není co načítat, protože nedošlo ke změně.
   * Prázdný seznam je něco jiného, to ke změně došlo takové, že
   * zmizely všechny soubory
   * @return
   */
  public synchronized List<File> coMamNacist() {
    List<FileAndTime> list = scanDir(dir);
    if (list.equals(lastScaned)) return null; // nezměnilo se nic
    lastScaned = list;
    List<File> files = new ArrayList<>();
    for (FileAndTime fat : list) {
      files.add(fat.file);
    }
    return files;
  }

  private List<FileAndTime> scanDir(File dir) {
    List<FileAndTime> list = new ArrayList<>();
    if (! dir.isDirectory()) return list;
    Deque<File> deque = new ArrayDeque<>();
    deque.add(dir);
    while (!deque.isEmpty()) {
      File f = deque.pop();
      if (f.isDirectory()) {
        deque.addAll(Arrays.asList(f.listFiles(new FileFilter() {
          @Override
          public boolean accept(File pathname) {
            return pathname.isDirectory() || FILE_NAME_REGEX.matcher(pathname.getName()).matches();
          }
        })));
      } else {
        list.add(new FileAndTime(f));
      }
    }

    Collections.sort(list); // podle času
    return list;
  }

  private static class FileAndTime implements Comparable<FileAndTime> {

    private final File file;
    private final long lastmodify;

    public FileAndTime(File aFile) {
      this(aFile, aFile.lastModified());
    }

    public FileAndTime(File aFile, long aLastmodify) {
      file = aFile;
      lastmodify = aLastmodify;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((file == null) ? 0 : file.hashCode());
      result = prime * result + (int) (lastmodify ^ (lastmodify >>> 32));
      return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FileAndTime other = (FileAndTime) obj;
      if (file == null) {
        if (other.file != null)
          return false;
      } else if (!file.equals(other.file))
        return false;
      if (lastmodify != other.lastmodify)
        return false;
      return true;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return "FileAndTime [file=" + file + ", lastmodify=" + lastmodify + "]";
    }
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(FileAndTime fat) {
      if (lastmodify == fat.lastmodify) {
          return 0;
      }
      if (lastmodify < fat.lastmodify) {
          return -1;
      } else {
          return 1;
      }
    }
  }
}
