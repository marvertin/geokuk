package cz.geokuk.util.lang;

/**Předek všech turbokonzultích objektů.
 * Jeho cílem je obejít některé těžko odhalitelné chyby.
 *
 * @author  <a href="mailto:?????.?????@turboconsult.cz">????? ?????</a>
 * @version $Revision: 10 $
 * @see     "TW####??????.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/TwObject.java 10    12/04/00 3:39p Polakm $"
 */
public class Object0
 {
        /**Metoda se použije v metodě equals následníka pro určení shodnosti typů
         */
        protected final Object checkCompare(Object aObject)
        {
                //S null lze porovnávat vše
                if (aObject == null)
                        throw new NullPointerException("Pokus o porovnávání '"
                                   + this.getClass() + "' s hodnotou null, to se nesmi!");
                else
                        if (getClass() != aObject.getClass())
                        {
                                throw new ClassCastException("Pokus o porovnávání '"
                                   + this.getClass() + "' s '" + aObject.getClass().getName()
                                   + "', je povoleno porovnávat pouze instance stejné třídy");
                        } else
                                return aObject;
        }

        /**Metoda se použije v metodě equals následníka pro určení shodnosti typů
         * @deprecated To byla špatná myšlenka
         */
        protected final Object checkEquals(Object aObject)
        {
          return aObject;
          /*
                //S null lze porovnávat vše
                if (aObject == null)
                        return null;
                else
                        if (getClass() != aObject.getClass())
                        {
                                throw new ClassCastException("Pokus o porovnávání '"
                                   + this.getClass() + "' s '" + aObject.getClass().getName()
                                   + "', je povoleno porovnávat pouze instance stejné třídy");
                        } else
                                return aObject;
                                */
        }

        /**Metoda se použije v metodě equals následníka, pokud se požaduje možnost porovnání potomků.
         * @deprecated To byla špatná myšlenka
         */
        protected final Object checkEquals(Object aObject, Class<?> aStartFrom)
        {
          return aObject;
          /*
                //S null lze porovnávat vše
                if (aObject == null)
                        return null;
                else
                        if (! (aStartFrom.isInstance(this) && aStartFrom.isInstance(aObject)))
                        {
                                throw new ClassCastException("Pokus o porovnávání '"
                                   + this.getClass().getName() + "' s '" + aObject.getClass().getName()
                                   + "', porovnávané třídy musí být následníkem '" + aStartFrom.getName() + "'");
                        } else
                                return aObject;
          */
        }


}
