package cz.geokuk.util.lang;

public class FObject {
  
  public static boolean isEqual(Object o1, Object o2) {
    if (o1 == null) {
      if (o2 == null) {
        return true;
      } else {
        return false;
      }
    } else {
      if (o2 == null) {
        return false;
      } else {
        return o1.equals(o2);
      }
    }
  }

}
