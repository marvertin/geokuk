package cz.geokuk.util.lang;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Title: Evidence exemplářů a dodávek Description: V první fázi zde bude implementace přidání dodávky a jejích exemplářů Copyright: Copyright (c) 2001 Company: TurboConsult s.r.o.
 *
 * @author
 * @version 1.0
 */

public class CCounterMap<T> implements CounterMap<T>, java.io.Serializable {
	private static final Logger	log					= LogManager.getLogger(CCounterMap.class.getSimpleName());

	static final long			serialVersionUID	= 4829239030839775643L;

	private final Map<T, Citac>	iCitace				= new LinkedHashMap<>();
	private int					iAllCount;																		// počet všech čítání

	/**
	 * Inkrtementuje o jedničku čítač specifikovaný zadaným klíčem
	 *
	 * @param aKey
	 *            Objekt, který má být počítán.
	 * @return Hodnota čítače před provedením operace.
	 */
	@Override
	public int inc(final T aKey) {
		return add(aKey, 1);
	}

	/**
	 * Dekrementuje o jedničku čítač specifikovaný zadaným klíčem
	 *
	 * @param aKey
	 *            Objekt, který má být počítán.
	 * @return Hodnota čítače před provedením operace.
	 */
	@Override
	public int dec(final T aKey) {
		return add(aKey, -1);
	}

	/**
	 * Vrátí hodnotu čítače pro daný objekt.
	 *
	 * @parame aKey(Object aKey)
	 * @return Hodnota čítače pro daný objekt. Vrací nulu, pokud objekt není v mapě evidován. Nikdy nevrátí zápornou hodnotu. Je to vlastně také hodnota před provedením operace.
	 */
	@Override
	public int count(final T aKey) {
		final Citac c = _citac(aKey);
		return c == null ? 0 : c.get();
	}

	/**
	 * Přičte zadanou hodnotu k čítači pro daný objekt.
	 *
	 * @param aKey
	 *            Objekt, který má být čítán.
	 * @param aOKolik
	 *            O jkolik má být čítač změněn. Může být kladné, záporné nebo 0.
	 * @return Hodnota čítače před provedením operace.
	 */
	@Override
	public int add(final T aKey, int aOKolik) {
		Citac c = _citac(aKey);
		int minule;
		if (c != null) // je něco evidováno
		{
			minule = c.get();
			if (minule + aOKolik < 0) {
				aOKolik = -minule; // aby nepodlezlo pod nulu
			}
			c.add(aOKolik); // důležité, i když se nastavuje na nula, aby se nastavil před zrušním
			if (c.get() == 0) {
				iCitace.remove(aKey); // zrušit, pokud docházíme k nule
			}
		} else // objekt není
		{
			minule = 0;
			if (aOKolik > 0) {
				c = new Citac(); // stvořit čítač
				c.add(aOKolik); // nastavit na hodnotu
				iCitace.put(aKey, c); // vložit do mapu
			}
		}
		return minule;
	}

	/**
	 * Přičte zadanou hodnotu k čítači pro daný objekt.
	 *
	 * @param aKey
	 *            Objekt, který má být čítán.
	 * @param aOKolik
	 *            O jkolik má být čítač změněn. Může být kladné, záporné nebo 0. Pokud je hodnota záporná a snížení čítače by vedlo k záporným hodnotám, je čítač nastaven na nulu.
	 * @return Hodnota čítače před provedením operace.
	 */
	@Override
	public int set(final T aKey, int aNaKolik) {
		if (aNaKolik < 0) {
			aNaKolik = 0;
		}
		Citac c = _citac(aKey);
		int minule;
		if (c != null) // je něco evidováno
		{
			minule = c.get();
			c.add(aNaKolik - minule); // důležité, i když se nastavuje na nula, aby se nastavil před zrušním
			if (c.get() == 0) {
				iCitace.remove(aKey); // zrušit, pokud docházíme k nule
			}
		} else // objekt není
		{
			minule = 0;
			if (aNaKolik != 0) {
				c = new Citac(); // stvořit čítač
				c.add(aNaKolik); // nastavit na hodnotu
				iCitace.put(aKey, c); // vložit do mapu
			}
		}
		return minule;
	}

	/**
	 * Resetuje čítač daného objektu
	 *
	 * @param aKey
	 *            Objekt, jehož čítač má být resetován.
	 * @return Hodnota čítače před provedením operace. Pokud je záporná, je považována za nulu.
	 */
	@Override
	public int reset(final T aKey) {
		return set(aKey, 0);
	}

	/**
	 * Vrátí nemodifikovatelnou mapu objektů na objekty Integer
	 *
	 * @return Naplněný objekt implementující rozhraní Map jako mapa na čítače. Objekt není
	 */
	@Override
	public Map<T, Integer> getMap() {
		final Map<T, Integer> m = new HashMap<>();
		for (final Map.Entry<T, Citac> entry : iCitace.entrySet()) {
			m.put(entry.getKey(), entry.getValue().get());
		}
		return m;
	}

	/**
	 * Vrátí sumu všech čítačů
	 */
	@Override
	public int count() {
		return iAllCount;
	}

	/**
	 * Vynuluje všechny čítače.
	 *
	 * @return Součet všech čítačů před vynulováním.
	 */
	@Override
	public int reset() {
		final int minule = iAllCount;
		iCitace.clear();
		iAllCount = 0;
		return minule;
	}

	@Override
	public String toString() {
		// zjistit velikost nejdelšího klíče i hodnoty
		int strlen = 0;
		int maxvalue = 0;
		for (final Map.Entry<T, Citac> entry : iCitace.entrySet()) {
			strlen = Math.max(strlen, (entry.getKey() + "").length());
			maxvalue = Math.max(maxvalue, entry.getValue().get());
		}
		// spočítat počet míst
		int pocetmist = 0;
		while (maxvalue > 0) {
			maxvalue /= 10;
			pocetmist++;
		}
		final StringBuilder sb = new StringBuilder();
		for (final Map.Entry<T, Citac> entry : iCitace.entrySet()) {
			// String klic = (String)entry.getKey();
			final int hodnota = entry.getValue().get();
			sb.append(FString.alignRight(entry.getKey() + " ", strlen + 4, '.'));
			sb.append(FString.truncateLeft("............................... " + hodnota, pocetmist + 1));

			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}

	private Citac _citac(final Object aKey) {
		return iCitace.get(aKey);
	}

	private class Citac implements java.io.Serializable {
		static final long	serialVersionUID	= 9077120911716375382L;
		private int			iPocet;

		void add(final int aPocet) {
			iPocet += aPocet; // posunout
			iAllCount += aPocet; // celkový posunout
			if (iPocet < 0) {
				iAllCount += -iPocet; // tak moc toho nemůžeme odečíst
				iPocet = 0;
			}
		}

		int get() {
			return iPocet;
		}
	}

	private int _addsub(final CounterMap<T> aMap, final int aZnamenko) {
		final int minule = iAllCount;
		for (final Map.Entry<T, Integer> me : aMap.getMap().entrySet()) {
			this.add(me.getKey(), me.getValue() * aZnamenko); // přičíst hodnotu ze zpracovávaného mapu
			// System.err.p rintln(" " + me.getKey() + ": " + me.getValue() + " " + aMap.count(me.getKey()));
		}
		return minule;
	}

	/**
	 * Přičte odpovídající hodnoty čítačů ze zadané mapy.
	 *
	 * @param aMap
	 *            mapa, která se má přičíst.
	 * @return Celková suma čítačů teéto mapy před přičtěním.
	 */
	@Override
	public int add(final CounterMap<T> aMap) {
		return _addsub(aMap, 1);
	}

	/**
	 * Odečte odpovídající hodnoty čítačů ze zadané mapy.
	 *
	 * @param aMap
	 *            mapa, která se má přičíst.
	 * @return Celková suma čítačů teéto mapy před přičtěním.
	 */
	@Override
	public int sub(final CounterMap<T> aMap) {
		return _addsub(aMap, -1);
	}

	private static <T> void _testVypis(final CounterMap<T> aMap) {
		final Map<T, Integer> m = aMap.getMap();
		log.debug("Vypis mavy citacu");
		for (final Map.Entry<T, Integer> me : m.entrySet()) {
			log.debug("  " + me.getKey() + ": " + me.getValue() + "  " + aMap.count(me.getKey()));
		}
		log.debug("     Celkovy pocet = " + aMap.count());
	}

	public static void main(final String args[]) {
		// TODO : test?
		final CounterMap<String> cm2 = new CCounterMap<>();
		cm2.set("martin", 1200);
		cm2.set("helena", 1400);
		cm2.set("aneta", 1500);

		final CounterMap<String> cm = new CCounterMap<>();
		_testVypis(cm);

		log.debug(cm.add("adam", 3));
		log.debug(cm.inc("adam"));
		log.debug(cm.dec("marketa"));
		log.debug(cm.add("marketa", 7));
		log.debug(cm.dec("marketa"));
		log.debug(cm.add("robert", 5));
		log.debug(cm.add("robert", 8));

		cm.add("robert", 264656);
		cm.add("aneta", 4477);
		cm.add("helena", 1);

		log.debug("");
		log.debug("");
		log.debug(cm);

		_testVypis(cm);
		log.debug(cm.add("marketa", 4));
		_testVypis(cm);
		log.debug(cm.add("marketa", -3));
		_testVypis(cm);
		log.debug(cm.add("marketa", -11));
		log.debug(cm.set("robert", 71));
		log.debug(cm.set("aneta", 18));

		_testVypis(cm);
		log.debug(cm.add(cm2));
		_testVypis(cm);
		log.debug(cm.dec("adam"));
		log.debug(cm.dec("adam"));

		_testVypis(cm);
		log.debug(cm.dec("adam"));
		_testVypis(cm);
		log.debug(cm.dec("adam"));
		log.debug(cm.add("aneta", -6));
		_testVypis(cm);
		log.debug(cm.dec("adam"));
		log.debug(cm.reset("aneta"));
		_testVypis(cm);
		log.debug("nikdo: " + cm.count("nikdo"));
		log.debug(cm.reset());
		_testVypis(cm);

	}
}