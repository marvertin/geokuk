package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InformaceOZdrojich {
  private static final Logger log = LogManager.getLogger(GeogetLoader.class.getSimpleName());
  
  private InformaceOZdroji root;
  
  private final Map<File, InformaceOZdroji> map = new LinkedHashMap<>();

  public int getSourceCount(boolean loaded) {
    int loadedCount = 0;
    int notLoadedCount = 0;
    for (InformaceOZdroji ioz : map.values()) {
      if (ioz.getChildren().isEmpty()) {
        if (ioz.nacteno) {
          ++loadedCount;
        } else {
          ++notLoadedCount;
        }
      }
    }
    return loaded ? loadedCount : notLoadedCount;
  }

  public InformaceOZdroji add(File aJmenoZdroje, boolean nacteno) {
    // TODO : perhaps it's not needed to bother with the whole path to root...
    // Maybe canonical path and longest
    // common prefix will do?
    InformaceOZdroji ioz = map.get(aJmenoZdroje);
    if (ioz == null) {
      ioz = new InformaceOZdroji(aJmenoZdroje, nacteno);
      map.put(aJmenoZdroje, ioz);
      InformaceOZdroji last = ioz;
      File parent = aJmenoZdroje.getParentFile();
      while (parent != null) {
        InformaceOZdroji parentIoz = map.get(parent);
        if (parentIoz != null) {
          parentIoz.addChild(last);
          last.parent = parentIoz;
          break;
        }
        parentIoz = new InformaceOZdroji(parent, false);
        parentIoz.addChild(last);
        last.parent = parentIoz;
        map.put(parent, parentIoz);
        last = parentIoz;
        parent = parent.getParentFile();
      }
      if (parent == null) {
        root = last;
      }
    }
    return map.get(aJmenoZdroje);
  }

  public InformaceOZdroji getRoot() {
    if (root == null) {
      return null;
    }

    while (root.getChildren().size() == 1) {
      root = root.getChildren().get(0);
    }
    return root.parent;
  }

  public InformaceOZdroji get(File key) {
    return map.get(key);
  }

  public Collection<InformaceOZdroji> getSubtree(File subTreeRoot) {
    List<InformaceOZdroji> toReturn = new ArrayList<>();
    return getSubtree(get(subTreeRoot), toReturn);
  }

  private Collection<InformaceOZdroji> getSubtree(InformaceOZdroji subTreeRoot, Collection<InformaceOZdroji> buffer) {
    List<InformaceOZdroji> children = subTreeRoot.getChildren();
    buffer.add(subTreeRoot);
    for (InformaceOZdroji child : children) {
      getSubtree(child, buffer);
    }
    return buffer;
  }

  public Set<File> getJmenaZdroju() {
    return map.keySet();
  }

  public long getYungest() {
    long x = 0;
    for (InformaceOZdroji info : map.values()) {
      if (info.nacteno) {
        x = Math.max(x, info.getLastModified());
      }
    }
    log.info("Spocitano nejmladsi datum: {}", x);
    return x;
  }

}