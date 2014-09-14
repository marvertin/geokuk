package cz.geokuk.util.lang;

public class FObject {
  
  public static boolean isEqual(Object o1, Object o2) {
    if (o1 == null) {
        return o2 == null;
    } else {
        return o2 != null && o1.equals(o2);
    }
  }

}
