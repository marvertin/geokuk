package cz.geokuk.util.lang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;



/** Třístavová Boolean hodnota: TRUE, FALSE, NOT_AVAILABLE.<BR>
 * Při konstrukci z Boolean dosáhneme NOT_AVAILABLE pomocí null.<BR>
 * Při konstrukci z String používáme tyto texty: "yes", "no", "n/a".<P>
 * Následuje příklad:<PRE>
 * A3Boolean b1 = A3Boolean.from("yes");
 * A3Boolean b2 = A3Boolean.from((Boolean)null);
 * A3Boolean b3 = A3Boolean.from(false);
 *
 * A3Boolean b = b1.and(b2);
 * if (b.isTrue()) { ... };
 * if (b.isNotAvailable()) { ... };
 * </PRE>
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Michal Polák
 * @version 1.0
 */
public final class A3Boolean extends AObject0 implements IElement, Serializable {
  private  static final long serialVersionUID = -9099084640819425965L;

  public static final A3Boolean TRUE = new A3Boolean(true);
  public static final A3Boolean FALSE = new A3Boolean(false);
  public static final A3Boolean NOT_AVAILABLE = new A3Boolean(null);

  private static final String _TRUE = "yes";
  private static final String _FALSE = "no";
  private static final String _NOT_AVAILABLE = "n/a";

  private Boolean _value;
  private A3Boolean(Boolean aValue) {

    _value = aValue;
  }

  public static A3Boolean from(boolean aValue) {

    return (aValue) ? TRUE : FALSE;
  }

  public static A3Boolean from(Boolean aValue) {

      return (aValue == null) ? NOT_AVAILABLE : (aValue.booleanValue()) ? TRUE : FALSE;
  }

  public static A3Boolean from(String aValue) {

    A3Boolean result = (_TRUE.equals(aValue)) ? TRUE : (_FALSE.equals(aValue)) ? FALSE : (_NOT_AVAILABLE.equals(aValue)) ? NOT_AVAILABLE : null;
    if (result == null) {

      throw new XCreateElement("Bad string value '" + aValue +
                               "' ! Valid values: " + _TRUE + ", " + _FALSE + ", " + _NOT_AVAILABLE);
    }
    return result;
  }

  public static boolean canFrom(boolean aValue) {

      try {

        from(aValue);
        return true;
      }
      catch (XCreateElement e) {

        return false;
      }
  }
  public static boolean canFrom(Boolean aValue) {

        try {

          from(aValue);
          return true;
        }
        catch (XCreateElement e) {

          return false;
        }
  }
  public static boolean canFrom(String aValue) {

    try {

      from(aValue);
      return true;
    }
    catch (XCreateElement e) {

      return false;
    }
  }

  public boolean isTrue() {return (_value != null && _value.booleanValue());}
  public boolean isFalse() {return (_value != null && !_value.booleanValue());}
  public boolean isNotAvailable() {return _value == null;}

  @Override
  public String asString() {

    return (_value == null) ? _NOT_AVAILABLE : (_value.booleanValue()) ? _TRUE : _FALSE;
  }

  @Override
  public String toString() {return asString();}

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(_value);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    _value = (Boolean)in.readObject();
  }

  // MV: Musí být nejméně protected, ačkoli vůbec nechápu proč
  protected Object readResolve() throws ObjectStreamException {

    return from(_value);
  }

  @Override
  public boolean equals(Object aValue) {

    if (!(aValue instanceof A3Boolean)) return false;
    Boolean objb = ((A3Boolean)aValue)._value;
    if (_value == null) {

      return objb == null;
    }
    else {

      return _value.equals(objb);
    }
  }

  @Override
  public int hashCode() {
    return (_value == null) ? 0 : _value.hashCode();
  }

  public A3Boolean not() {

    return (_value == null) ? NOT_AVAILABLE : (_value.booleanValue()) ? FALSE : TRUE;
  }

  public A3Boolean and(A3Boolean aValue) {

    if (aValue == null) throw new RuntimeException ("Null value prohibited !");
    if (_value == null) {

      if (aValue._value != null && !aValue._value) {
          return FALSE;
      } else {
          return NOT_AVAILABLE;
      }
    }
    else {
      return _value ? aValue : FALSE;
    }
  }

  public A3Boolean or(A3Boolean aValue) {

    if (aValue == null) throw new RuntimeException ("Null value prohibited !");
    if (_value == null) {

      if (aValue._value != null && aValue._value) {
          return TRUE;
      } else {
          return NOT_AVAILABLE;
      }
    }
    else {

      return _value ? TRUE : aValue;
    }
  }

  public A3Boolean xor(A3Boolean aValue) {

    if (aValue == null) throw new RuntimeException ("Null value prohibited !");
    if (_value == null || aValue._value == null) return NOT_AVAILABLE;
    return (equals(aValue)) ? FALSE : TRUE;
  }
}
