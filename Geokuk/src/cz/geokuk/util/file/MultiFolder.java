/**
 * 
 */
package cz.geokuk.util.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



/**
 * Nese informace z několika složek,
 * ať resourcových nebo normálních. Informace se překrývají.
 * @author veverka
 *
 */
public class MultiFolder {
  /**
   * 
   */
  private static final String REMOVE_SUFFIX = ".remove";
  private static final String CONTENT_TXT = "content.txt";

  private KeyTree<String, LamUrl> tree = new KeyTree<String, LamUrl>();

  public KeyNode<String,LamUrl> getNode(String key) {
  	String[] ss = key.split("/");
  	return tree.locate(Arrays.asList(ss));
  }
  
  public void addFolderTree(File dir) {
    List<String> emptyList = Collections.emptyList();
    addOneFord(dir, emptyList);
  }

  private void addOneFord(File ford, List<String> names) {
    LamUrl lamUrl = new LamUrl();
    lamUrl.url = fileToUrl(ford);
    lamUrl.lastModified = ford.lastModified();
    lamUrl.name = ford.getName();
    tree.add(lamUrl, names); 
    if (ford.isDirectory()) {
      for (String s : ford.list()) {
        List<String> list = new ArrayList<String>(names.size() + 1);
        list.addAll(names);
        if (s.endsWith(REMOVE_SUFFIX)) {
          list.add(s.substring(0, s.length() - REMOVE_SUFFIX.length()));
          tree.remove(list);
        } else {
          list.add(s);
          addOneFord(new File(ford, s), list);
        }
      }
    }
  }

  /**
   * @param ford
   * @return
   * @throws MalformedURLException
   */
  private URL fileToUrl(File ford)  {
    try {
      return ford.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Přidá strom resourců
   * @param rootresource
   */
  public void addResourceTree(String rootresource) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    BufferedReader bis = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(rootresource + "/" + CONTENT_TXT)));
    String line;
    try {
      while((line = bis.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0) continue;
        String s = rootresource + "/" + line;
        URL url = classLoader.getResource(s);
        String[] jmena = line.split("/");
        if (jmena[jmena.length-1].endsWith(REMOVE_SUFFIX)) {
          jmena[jmena.length-1] = jmena[jmena.length-1].substring(0, jmena[jmena.length-1].length() - REMOVE_SUFFIX.length());
          tree.remove(jmena);
        } else {
          LamUrl lamUrl = new LamUrl();
          lamUrl.url = url;
          lamUrl.name = jmena[jmena.length - 1];
          tree.add(lamUrl, jmena);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tree == null) ? 0 : tree.hashCode());
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
    MultiFolder other = (MultiFolder) obj;
    if (tree == null) {
      if (other.tree != null)
        return false;
    } else if (!tree.equals(other.tree))
      return false;
    return true;
  }

  public static void main(String[] args) {

    MultiFolder mf = new MultiFolder();
    mf.addResourceTree("geokuk/image");
    mf.addFolderTree(new File("img2"));
    mf.addFolderTree(new File("img3"));
    mf.print();
  }

  public void print() {
    tree.print();
  }
}
