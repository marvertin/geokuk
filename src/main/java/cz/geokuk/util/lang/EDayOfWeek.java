package cz.geokuk.util.lang;

import java.util.Calendar;

/**
 * Dny v týdnu.
 *
 * @author <a href="mailto:Jiri.Polak@turboconsult.cz">Jiří Polák</a>
 * @version $Revision: 1 $
 * @see "TW0139Util.vjp"
 * @see "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/EDayOfWeek.java 1     13.12.00 15:07 Polak $"
 */
public enum EDayOfWeek {
	MONDAY(Calendar.MONDAY), TUESDAY(Calendar.TUESDAY), WEDNESDAY(Calendar.WEDNESDAY), THURSDAY(Calendar.THURSDAY), FRIDAY(Calendar.FRIDAY), SATURDAY(Calendar.SATURDAY), SUNDAY(Calendar.SUNDAY);

	private static EDayOfWeek[] sCalendarNaNas;

	private final int iDayOfWeakAsCalendar;

	/**
	 * Vrátí honodu dne v týdnu na základě kalendáře.
	 *
	 * @param aCalendarValue
	 * @return
	 */
	public static EDayOfWeek fromCalendarValue(final int aCalendarValue) {
		if (sCalendarNaNas == null) {
			// Zde se spoléháme trochu nesprávně na to, jak jsou konstanty
			// ve tříde Calendar implementovány.
			sCalendarNaNas = new EDayOfWeek[8];
			for (final EDayOfWeek dayOfWeek : EDayOfWeek.values()) {
				sCalendarNaNas[dayOfWeek.iDayOfWeakAsCalendar] = dayOfWeek;
			}
		}
		return sCalendarNaNas[aCalendarValue];
	}

	EDayOfWeek(final int aDayOfWeakAsCalendar) {
		iDayOfWeakAsCalendar = aDayOfWeakAsCalendar;
	}

	public int getDayOfWeakAsCalendar() {
		return iDayOfWeakAsCalendar;
	}
}
