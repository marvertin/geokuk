package cz.geokuk.util.lang;

/*import java.util.Stack;
import java.lang.reflect.*;
*/
import java.net.InetAddress;
import java.net.UnknownHostException;


/**Dost hlavní TW třída.
 * @author Michal Polák
 * @version $Revision: 28 $
 * @see     "TW0139Util.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/TwSystem.java 28    14.06.01 15:04 Roztocil $"
 */
public final class FSystem {

	/**
         * Oddělovač řádků.
	 */
	public static final String LINE_SEPARATOR;

	/**
         * Uživatelův login.
	 */
	public static final String USER_NAME;

	/**
         * IP tohoto počítače ve formátu %d.%d.%d.%d, null pokud není aktivována síťová vrstva.
	 */
	public static final String LOCAL_HOST_IP;

	/**
         * Doménové jméno tohoto počítače ve tvaru: orel&#46;tconsult&#46;cz,  null pokud není aktivována síťová vrstva.
	 */
	public static final String LOCAL_HOST_NAME;

	/**
         * Jméno tohoto počítače, null pokud není aktivována síťová vrstva.
	 */
	public static final String COMPUTER_NAME;

        /**
         * Defaultní TEMP adresář ve tvaru: C:\DOCUME~1\polakm\MSTNNA~1\Temp\.
         */
        public static final String TEMP_DIR;

	static {

		LINE_SEPARATOR = System.getProperty("line.separator");
		USER_NAME = System.getProperty ("user.name");
                TEMP_DIR = System.getProperty ("java.io.tmpdir");

		InetAddress addr;
		try {

			addr = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {

			addr = null;
		}

		if (addr != null) {

			LOCAL_HOST_IP = addr.getHostAddress();
			LOCAL_HOST_NAME = addr.getHostName();

			int dotPos = LOCAL_HOST_NAME.indexOf('.');
			if (dotPos > 0) {
                COMPUTER_NAME = LOCAL_HOST_NAME.substring(0, dotPos);
            }
			else {
                COMPUTER_NAME = LOCAL_HOST_NAME;
            }
		}
		else {
            LOCAL_HOST_IP = null;
			LOCAL_HOST_NAME = null;
			COMPUTER_NAME = null;
		}


	}
}
