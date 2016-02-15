package cz.geokuk.util.lang;

import java.math.BigDecimal;


/**
 * @author  <a href="mailto:?????.?????@turboconsult.cz">????? ?????</a>
 * @version $Revision: 3 $
 * @see     "TW####??????.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/ATwLongBase.java 3     26.05.00 16:00 Veverka $"
 */
public abstract class ALong0 extends AObject0 implements IAtomLong, IAtomBigDecimal, Comparable<ALong0>
{
  private static final long serialVersionUID = -4485641313971435401L;

  /**Hodnota
   */
  private long iValue;

  /**Kontrola rozsahu.
   */
  protected ALong0(long aValue)
  {
    iValue = aValue;
    checkRange();
    validate();
  }

  /**
   * @param aValue
   */
  protected ALong0(String aValue) {
    try {
      iValue = Long.parseLong(aValue.trim());
      checkRange();
      validate();
    } catch (Exception e) {
      throw new RuntimeException("Illegal value \"" + aValue + "\" for type \"" + getClass().getName() + "\"") ;
    }
  }

  /**
   * @param aValue
   */
  protected ALong0(BigDecimal aValue) {
    try {
    if (aValue.scale() > 0) throw new RuntimeException("Only number without decimal numbers can be converted to " + getClass().getName());
      iValue = aValue.longValueExact();
      checkRange();
      validate();
    } catch (Exception e) {
      throw new RuntimeException("Illegal value \"" + aValue + "\" for type \"" + getClass().getName() + "\"") ;
    }
  }
  
  /**Vrací se jako integer.
   */
  public long toLong()
  {
    return iValue;
  }
  
  /**
   * Vrací se jako BigDecimal
   * @return
   */
  public BigDecimal toBigDecimal() {
    return new BigDecimal(iValue);
  }

  /**Vrací se jako string.
   */
  public String toString()
  {
    return Long.toString(iValue);
  }


  /**Minimální povolená hodnota. Následník může přepsat, pokud má být minimální hodnota jiná.
   * Předefinovaná metoda by měla pokud možno vracet konstantu.
   */
  protected long minValue()
  {
    return Long.MIN_VALUE;
  }

  /**Maximální povolená hodnota. Následník může přepsat, pokud má být maximální hodnota jiná.
   * Předefinovaná metoda by měla pokud možno vracet konstantu.
   */
  protected long maxValue()
  {
    return Long.MAX_VALUE;
  }

  /**Kontrola rozsahu. Metoda se volá z konstruktoru a kontroluje rozsah.
   */
  protected final void checkRange()
  {
    if (! (minValue() <= iValue && iValue <= maxValue()))
      throw new XCreateElement("Hodnota " + iValue + " není v požadovaném intervalu <"
          + minValue() + "," + maxValue() + ">");
  }

  /**Porovnání dvou elementárních typů.
   */
  public boolean equals(Object aObject)
  {
    if (!(aObject instanceof ALong0)) return false;
    ALong0 obj = (ALong0)aObject;
    return iValue == obj.iValue;
  }


  /**Heš kód.
   */
  public int hashCode()
  {
    return (int)(iValue ^ (iValue >>> 32));
  }


  /** Kontrola platnosti typu. Metoda je volána z kosntruktoru poté, co byla provedena validace rozsahu dély řetězce.
   * V okamžiku volání je zřejmé, že hodnota iValue je nastavena a je ve stanoveném rozsahu.
   * Metoda v případě nevalidity vyhazuje nečekovanou výjimku.
   * Neplést s metodou isValid(), to je jiná validita (menší váha).
   */
  protected void validate() {
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(ALong0 anotherALong) {
    long thisVal = this.iValue;
    long anotherVal = anotherALong.iValue;
    return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
  }


}
