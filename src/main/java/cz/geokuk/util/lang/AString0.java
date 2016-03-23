package cz.geokuk.util.lang;

import java.io.Serializable;

/**
 * Předek všech elementárních typů založených na stringu. Tento předek již deklaruje protektovanou stringovou instanční proměnnou iValue, proto ji ve svých potomcích nemusíte (a neměli byste) deklarovat. Neměli byste deklarovat žádné instanční proměnné. Pokud to potřebujete, nepoužívejte tuto třídu
 * jako předka.
 *
 * V následníku musíte udělat vždy konsteruktor, který naplní proměnnou iValue a v případě potřeby provede patřičné testy správnosti. Vstupní parametr může upravovat do kanonického tvaru. Ostatní metody předefinováváte pouze v případě potřeby.
 *
 * @author <a href="mailto:?????.?????@turboconsult.cz">????? ?????</a>
 * @version $Revision: 6 $
 * @see "TW####??????.vjp"
 * @see "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/ATwStringBase.java 6     27.07.00 6:14 Veverka $"
 */
public abstract class AString0 extends AObject0 implements Serializable, Comparable<AString0> {
	private static final long	serialVersionUID	= -3816615837981907372L;
	protected final String		iValue;

	protected AString0(final String aValue) {
		iValue = aValue.trim();
		if (iValue == null) {
			throw new NullPointerException("Cannot use null value as constructor parameter of tw elementary types " + getClass());
		}
		checkRange();
		validate();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final AString0 aObject) {
		if (aObject == null) {
			throw new IllegalArgumentException("Cannot compare with null");
		}
		return iValue.compareTo(aObject.iValue);
	}

	/**
	 * Porovnání dvou elementárních typů.
	 */
	@Override
	public boolean equals(final Object aObject) {
		if (aObject == this) {
			return true; // pokud porovnávám se sebou, tak se rovnají
		}
		if (!(aObject instanceof AString0)) {
			return false; // s jiným typem se nerovnají
		}
		final AString0 obj = (AString0) aObject;
		return iValue.equals(obj.iValue); // porovnat přímo řetězce
	}

	/**
	 * Heškód je vracen podle zastrčeného řetězce.
	 */
	@Override
	public int hashCode() {
		return iValue.hashCode();
	}

	/**
	 * Standardní toString je implementován voláním asString. Může být pochopitelně přepsán.
	 */
	@Override
	public String toString() {
		return iValue;
	}

	/**
	 * Kontrola rozsahu. Metoda se volá z konstruktoru a kontroluje rozsah.
	 */
	protected final void checkRange() {
		if (!(minLength() <= iValue.length() && iValue.length() <= maxLength())) {
			throw new XCreateElement("Hodnota \"" + iValue + "\" není v požadovaném intervalu <" + minLength() + "," + maxLength() + ">");
		}
	}

	/**
	 * Maximální délka Stringu. Následník může přepsat, pokud má být maximální hodnota jiná. Předefinovaná metoda by měla pokud možno vracet konstantu.
	 */
	protected int maxLength() {
		return Integer.MAX_VALUE;
	}

	/**
	 * Minimální délka Stringu. Následník může přepsat, pokud má být minimální hodnota jiná. Předefinovaná metoda by měla pokud možno vracet konstantu.
	 */
	protected int minLength() {
		return 0;
	}

	/**
	 * Kontrola platnosti typu. Metoda je volána z kosntruktoru poté, co byla provedena validace rozsahu dély řetězce. V okamžiku volání je zřejmé, že hodnota iValue není null, neobsahuje koncové mezery a je ve stanoveném rozsahu. Metoda v případě nevalidity vyhazuje nečekovanou výjimku. Neplést s
	 * metodou isValid(), to je jiná validita (menší váha).
	 */
	protected void validate() {}

}
