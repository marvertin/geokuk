package cz.geokuk.plugins.cesty;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;



public class IgnoreList {

  private final Set<Kesoid> ignoreList  = new HashSet<Kesoid>();


  boolean addToIgnoreList(Wpt wpt) {
    return ignoreList.add(wpt.getKesoid());
  }

  boolean removeFromIgnoreList(Wpt wpt) {
    return ignoreList.remove(wpt.getKesoid());
  }

  boolean isOnIgnoreList(Wpt wpt) {
    return ignoreList.contains(wpt.getKesoid());
  }


  Set<Kesoid> getIgnoreList() {
    return Collections.unmodifiableSet(ignoreList);
  }

  public boolean clear() {
    boolean zmena = ignoreList.size() > 0;
    ignoreList.clear();
    return zmena;
  }


}
