package cz.geokuk.util.lang;

import java.util.Map;

/**
 * Rozhraní objektu, které funguje jako mapa čítačů. Lze si to představit jako mapa čítačů. Jako klíč může být použit objekt libovolného typu, který má korektně definovánu metodu equals, protože tato metoa se použie pro určení shodnosti klíčů. Typicky budou výčty řetězce, výčty, obálky primitivních
 * typů, může to být ale cokoli. Uživatel čítače musí mít na paměti, že některý z čítaných objektů bude mapou čítačů držen do té doby, než čítač klesne k nule. Většina metod vrací hodnotu čítače před provedením operace. Žádná operace nepřivede čítač do záporných hodnot. Pokud by operace k tomu
 * vedla, zastaví se čítač na nule.
 *
 * Title: Evidence exemplářů a dodávek Description: V první fázi zde bude implementace přidání dodávky a jejích exemplářů Copyright: Copyright (c) 2001 Company: TurboConsult s.r.o.
 *
 * @author
 * @version 1.0
 */

public interface CounterMap<T> {
	/**
	 * Inkrtementuje o jedničku čítač specifikovaný zadaným klíčem
	 *
	 * @param aKey
	 *            Objekt, který má být počítán.
	 * @return Hodnota čítače před provedením operace.
	 */
	public int inc(T aKey);

	/**
	 * Dekrementuje o jedničku čítač specifikovaný zadaným klíčem
	 *
	 * @param aKey
	 *            Objekt, který má být počítán.
	 * @return Hodnota čítače před provedením operace.
	 */
	public int dec(T aKey);

	/**
	 * Vrátí hodnotu čítače pro daný objekt.
	 *
	 * @parame aKey(Object aKey)
	 * @return Hodnota čítače pro daný objekt. Vrací nulu, pokud objekt není v mapě evidován. Nikdy nevrátí zápornou hodnotu. Je to vlastně také hodnota před provedením operace.
	 */
	public int count(T aKey);

	/**
	 * Přičte zadanou hodnotu k čítači pro daný objekt.
	 *
	 * @param aKey
	 *            Objekt, který má být čítán.
	 * @param aOKolik
	 *            O jkolik má být čítač změněn. Může být kladné, záporné nebo 0.
	 * @return Hodnota čítače před provedením operace.
	 */
	public int add(T aKey, int aOKolik);

	/**
	 * Přičte zadanou hodnotu k čítači pro daný objekt.
	 *
	 * @param aKey
	 *            Objekt, který má být čítán.
	 * @param aOKolik
	 *            O jkolik má být čítač změněn. Může být kladné, záporné nebo 0. Pokud je hodnota záporná a snížení čítače by vedlo k záporným hodnotám, je čítač nastaven na nulu.
	 * @return Hodnota čítače před provedením operace.
	 */
	public int set(T aKey, int aNaKolik);

	/**
	 * Resetuje čítač daného objektu
	 *
	 * @param aKey
	 *            Objekt, jehož čítač má být resetován.
	 * @return Hodnota čítače před provedením operace. Pokud je záporná, je považována za nulu.
	 */
	public int reset(T aKey);

	/**
	 * Vrátí nemodifikovatelnou mapu objektů na objekty Integer
	 *
	 * @return Naplněný objekt implementující rozhraní Map jako mapa na čítače. Objekt není
	 */
	public Map<T, Integer> getMap();

	/**
	 * Vrátí sumu všech čítačů
	 */
	public int count();

	/**
	 * Vynuluje všechny čítače
	 */
	public int reset();

	/**
	 * Přičte odpovídající hodnoty čítačů ze zadané mapy.
	 *
	 * @param aMap
	 *            mapa, která se má přičíst.
	 * @return Celková suma čítačů teéto mapy před přičtěním.
	 */
	public int add(CounterMap<T> aMap);

	/**
	 * Odečte odpovídající hodnoty čítačů ze zadané mapy.
	 *
	 * @param aMap
	 *            mapa, která se má přičíst.
	 * @return Celková suma čítačů teéto mapy před přičtěním.
	 */
	public int sub(CounterMap<T> aMap);
}
