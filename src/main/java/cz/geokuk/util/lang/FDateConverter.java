/*
 * Created on 5.12.2005
 *
 * 
 */
package cz.geokuk.util.lang;

import java.util.Calendar;

/**
 * @author andrysek
 *
 * Konverze datumů
 */
public class FDateConverter {
  public static ADate toADate(java.util.Date aDate) {
    ADate result = toADate(aDate, java.util.TimeZone.getDefault(), java.util.Locale.getDefault());
    return result;
  }

  public static ADate toADate(java.util.Date aDate, java.util.TimeZone aTimeZone, java.util.Locale aLocale) {
    if (aDate == null) {
      return null;
    } else {      
      Calendar cal = Calendar.getInstance(aTimeZone, aLocale);
      cal.setTime(aDate);      
      ADate result = ADate.from(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
      return result;
    }
  }
}
