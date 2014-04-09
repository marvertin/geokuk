package cz.geokuk.util.lang;

import java.math.BigDecimal;


/**
 * @author  <a href="mailto:?????.?????@turboconsult.cz">????? ?????</a>
 * @version $Revision: 3 $
 * @see     "TW####??????.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/ATwLongBase.java 3     26.05.00 16:00 Veverka $"
 */
public  class ABigDecimal0 extends AObject0 implements  IAtomBigDecimal, IAtomDouble, Comparable<ABigDecimal0>
{
  private static final long serialVersionUID = -4485641313971435401L;

  /**Hodnota
   */
  private BigDecimal iValue;

  /**Kontrola rozsahu.
   */
  protected ABigDecimal0(BigDecimal aValue)
  {
    iValue = aValue;
    validate();
  }

  /**Kontrola rozsahu.
   */
  protected ABigDecimal0(double aValue)
  {
    iValue = BigDecimal.valueOf(aValue);
    validate();
  }
  
  /**
   * @param aValue
   */
  protected ABigDecimal0(String aValue) {
    try {
      iValue = new BigDecimal(aValue);
      validate();
    } catch (Exception e) {
      throw new RuntimeException("Illegal value \"" + aValue + "\" for type \"" + getClass().getName() + "\"", e) ;
    }
  }


  /**
   * Vrací se jako BigDecimal
   * @return
   */
  public BigDecimal toBigDecimal() {
    return iValue;
  }

  /**Vrací se jako string.
   */
  public String toString()
  {
    return iValue.toString();
  }

  /**Porovnání dvou elementárních typů.
   */
  public boolean equals(Object aObject)
  {
    if (!(aObject instanceof ABigDecimal0)) return false;
    ABigDecimal0 obj = (ABigDecimal0)aObject;
    return iValue.equals(obj.iValue);
  }


  /**Heš kód.
   */
  public int hashCode()
  {
    return iValue.hashCode();
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
  public int compareTo(ABigDecimal0 aABigDecimal) {
    
    return iValue.compareTo(aABigDecimal.iValue);
  }

  /* (non-Javadoc)
   * @see cz.tconsult.tw.lang.IAtomDouble#toDouble()
   */
  public double toDouble() {
    return iValue.doubleValue();
  }


}
