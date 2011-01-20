/**
 * 
 */
package cz.geokuk.util.index2d;


import java.util.ArrayList;
import java.util.List;

import cz.geokuk.util.index2d.Ctverecnik.DuplikHlidac;


/**
 * Drží celý index všech objektů na mapě, tedy se dají  přes něj dostat i ty objekty.
 * @author veverka
 *
 */
public class Indexator<T> {

  /**
   * 
   */
  Ctverecnik<T> root;
  
  public Indexator(BoundingRect br) {
    root = new Ctverecnik<T>(br.xx1, br.yy1, br.xx2, br.yy2);
  }

  public Indexator() {
    this(new BoundingRect(0,0,Integer.MAX_VALUE / 2, Integer.MAX_VALUE/ 2));
  }

  public void vloz(int xx, int yy, T mapobj) {
    if (! checkRozsah(xx, yy) ) {
      throw new RuntimeException("Hodnoty " + xx + " " + yy + " jsou mimo rozsah " + root);
    }
      
    Sheet<T> sheet = new Sheet<T>(xx, yy, mapobj);
    DuplikHlidac duplikHlidac = new Ctverecnik.DuplikHlidac();
    root.vloz(sheet, duplikHlidac);
    while (duplikHlidac.duplicita) {
      //throw new RuntimeException("Duplicita");
      sheet = new Sheet<T>(sheet.xx + 3, sheet.yy + 7, mapobj);
      duplikHlidac = new Ctverecnik.DuplikHlidac();
      root.vloz(sheet, duplikHlidac);
      //System.out.println("Duplicita resena " + mapobj);
    }
  }

	public boolean checkRozsah(int xx, int yy) {
	  return ! (xx < root.getXx1() || xx >= root.getXx2() || yy < root.getYy1() || yy >= root.getYy2());
  }

  public int count(BoundingRect boundingRect) {
    final int[] counta = new int[1];
    root.visit(boundingRect, new SloucenyVisitor<T>() {
      @Override
      protected void visitNod(Node0<T> aNode) {
        counta[0] += aNode.count;
      }
    });
    return counta[0];
  }
  
  public List<Node0<T>> shallowList(BoundingRect boundingRect) {
    final List<Node0<T>> list = new ArrayList<Node0<T>>(100);
    root.visit(boundingRect, new SloucenyVisitor<T>() {
      @Override
      protected void visitNod(Node0<T> aNode) {
        list.add(aNode);
      }
    });
    return list;
  }
  
  public List<Sheet<T>> deepList(BoundingRect boundingRect) {
    final List<Sheet<T>> list = new ArrayList<Sheet<T>>(100);
    root.visit(boundingRect, new FlatVisitor<T>() {
      public void visit(Sheet<T> aSheet) {
        list.add(aSheet);
      }
    });
    return list;
  }
  
  public void visit(BoundingRect boundingRect, Visitor<T> visitor) {
    root.visit(boundingRect, visitor);
  }
  
  public void vypis() {
    root.vypis("root", 1);
  }
  
}
