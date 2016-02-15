/**
 * 
 */
package cz.geokuk.plugins.kesoid.importek;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InformaceOZdrojich {
  private final Map<String, InformaceOZdroji> map = new LinkedHashMap<String, InformaceOZdroji>();
  private final List<InformaceOZdroji> list= new ArrayList<InformaceOZdroji>();

  public int getSourceCount() {
    return map.size();
  }

  /**
   * Vrátíá počet načtených nebo naopak nenačtených potovr.
   * @param nactenych
   * @return
   */
  public int getSourceCount(boolean nactenych) {
    int pocet = 0;
    for (InformaceOZdroji info : list) {
      if (info.nacteno == nactenych) {
        pocet ++;
      }
    }
    return pocet;
  }

  public int getNenactenychSourceCount() {
    return map.size();
  }



  public InformaceOZdroji add(String aJmenoZdroje, long aLastModified, boolean nacteno) {
    InformaceOZdroji ioz = map.get(aJmenoZdroje);
    if (ioz == null) {
      ioz = new InformaceOZdroji();
      ioz.jmenoZDroje = aJmenoZdroje;
      ioz.lastModified = aLastModified;
      ioz.nacteno = nacteno;
      map.put(aJmenoZdroje, ioz);
      list.add(ioz);
    }
    return ioz;
  }

  public long getYungest() {
    long x = 0;
    for (InformaceOZdroji info : map.values()) {
      if (info.nacteno) {
        x = Math.max(x, info.lastModified);
      }
    }
    return x;
  }

  public InformaceOZdroji get(int index) {
    return list.get(index);
  }

  /**
   * @return
   */
  public Set<String> getJmenaZdroju() {
    return map.keySet();
  }
}