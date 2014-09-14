package cz.geokuk.util.index2d;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ctverecnik<T> extends Node0<T> {

    private static final Logger log =
            LogManager.getLogger(Ctverecnik.class.getSimpleName());

  private Node0<T> jz;
  private Node0<T> jv;
  private Node0<T> sz;
  private Node0<T> sv;

  private final int xx1;
  private final int yy1;
  private final int xx2;
  private final int yy2;


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Ctverecnik [xx1=" + xx1 + ", yy1=" + yy1 + ", xx2=" + xx2 + ", yy2=" + yy2 + "]";
  }

  /**
   * @return the xx1
   */
  public int getXx1() {
    return xx1;
  }

  /**
   * @return the yy1
   */
  public int getYy1() {
    return yy1;
  }

  /**
   * @return the xx2
   */
  public int getXx2() {
    return xx2;
  }

  /**
   * @return the yy2
   */
  public int getYy2() {
    return yy2;
  }


  /**
   * @param aXx1
   * @param aXx2
   * @param aYy1
   * @param aYy2
   */
  public Ctverecnik(int aXx1, int aYy1, int aXx2, int aYy2) {
    xx1 = aXx1;
    yy1 = aYy1;
    xx2 = aXx2;
    yy2 = aYy2;
  }
  
  @Override
  public void visit(BoundingRect rect, Visitor<T> visitor) {
    if (rect != null) {
      boolean jeToMimo 
          =  xx1 >= rect.xx2
         ||  xx2 <= rect.xx1
         ||  yy1 >= rect.yy2
         ||  yy2 <= rect.yy1;
      if (jeToMimo) return;
         
      boolean jeToKompletUvnitr
         = xx1 >= rect.xx1
        && xx2 <= rect.xx2
        && yy1 >= rect.yy1
        && yy2 <= rect.yy2;
        
      if (jeToKompletUvnitr) {
        visitor.visit(this);
      } else {
        visitPodrizene(rect, visitor);  
      }
    } else {  // nemáme zadaný obdélník, takže je nekonečný,
      // takže to nemůže být mimo ani kompletně uvnitř
      visitPodrizene(rect, visitor);  
    }
  }

  /**
   * @param rect
   * @param visitor
   */
  private void visitPodrizene(BoundingRect rect, Visitor<T> visitor) {
    if (jz != null) jz.visit(rect, visitor);  
    if (jv != null) jv.visit(rect, visitor);  
    if (sz != null) sz.visit(rect, visitor);  
    if (sv != null) sv.visit(rect, visitor);
  }

  public void vloz (Sheet<T> sheet, DuplikHlidac dh) {

    int xxs = xx1 + (xx2 - xx1) / 2;
    int yys = yy1 + (yy2 - yy1) / 2;

    int xx = sheet.xx;
    int yy = sheet.yy;
    
    if        (xx < xxs && yy < yys) {
      jz = vlozDoPodctverce(jz, sheet, xx1, yy1, xxs, yys, dh);
    } else if (xx >= xxs && yy < yys) {
      jv = vlozDoPodctverce(jv, sheet, xxs, yy1, xx2, yys, dh);
    } else if (xx < xxs && yy >= yys) {
      sz = vlozDoPodctverce(sz, sheet, xx1, yys, xxs, yy2, dh);
    } else if (xx >= xxs && yy >= yys) {
      sv = vlozDoPodctverce(sv, sheet, xxs, yys, xx2, yy2, dh);
    } else {
      throw new RuntimeException("Ani jedna podminka nezabrala, podivne: " + this);
    }
    if (! dh.duplicita) {
      count++;  // pokud jsem opravdu vložil
    }

  }

  private Node0<T> vlozDoPodctverce(Node0<T> node, Sheet<T> aSheet, int xx1, int yy1, int xx2, int yy2, DuplikHlidac dh) {
    //System.out.printf("xx1=%d xx2=%d yy1=%d yy2=%d\n", xx1, xx2, yy1, yy2);
    if (node == null) { // vlozit se tam
      return aSheet;
    } else if (node instanceof Ctverecnik) {
      Ctverecnik<T> ctver = (Ctverecnik<T>) node;
      ctver.vloz(aSheet, dh);
      return node;
    } else if (node instanceof Sheet) {
      Sheet<T> sheet = (Sheet<T>) node;
      if (sheet.xx == aSheet.xx && sheet.yy == aSheet.yy) {
        dh.duplicita = true; // takovy uz tam mame
        return node; // beze zmeny
      }
      Ctverecnik<T> ctver = new Ctverecnik<>(xx1, yy1, xx2, yy2);
      ctver.vloz(sheet, dh);
      ctver.vloz(aSheet, dh);
      return ctver;
    } else { // item je objektakem
      throw new RuntimeException("Podivny node: " + node.getClass().getName());
    }
  }

  /* (non-Javadoc)
   * @see objekty.Node0#isSheet()
   */
  @Override
  boolean isSheet() {
    return false;
  }
  
  static class DuplikHlidac {
    boolean duplicita = false;
  }

  /* (non-Javadoc)
   * @see objekty.Node0#vypis(int)
   */
  @Override
  void vypis(String prefix, int aLevel) {
    String mezery = String.format("%" + (aLevel * 2) + "s", " ");
    log.debug("{}{}: ({}) [{},{}] - [{},{}]", mezery, prefix, count, xx1, yy1, xx2, yy2);
    podvypis(jz, "jz", aLevel+1);
    podvypis(jv, "jv", aLevel+1);
    podvypis(sz, "sz", aLevel+1);
    podvypis(sv, "sv", aLevel+1);
  }

  /**
   * @param aJz
   * @param aString
   * @param aLevel
   */
  private void podvypis(Node0<T> aNode, String aPrefix, int aLevel) {
    if (aNode == null) {
      String mezery = String.format("%" + (aLevel * 2) + "s", " ");
      log.debug("{}{}: null", mezery, aPrefix);
    } else {
      aNode.vypis(aPrefix, aLevel);
    }
    
    
  }
}
