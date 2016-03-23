/**
 *
 */
package cz.geokuk.plugins.geocoding;

import java.io.UnsupportedEncodingException;
import java.net.*;

import cz.geokuk.core.hledani.HledaciPodminka0;

/**
 * @author veverka
 *
 */
public class HledaciPodminka extends HledaciPodminka0 {

	// private static final String URL_PREFIX = "http://maps.google.com/maps/geo?output=xml&sensor=false&key=geokuk&gl=CZ";
	private static final String URL_PREFIX = "http://maps.googleapis.com/maps/api/geocode/xml?sensor=false";

	URL computeUrl() {
		URL url;
		try {

			// sb.append("&bounds=");
			// sb.append(String.format("%s,%s|%s,%s", getStredHledani().lat -1, getStredHledani().lon -1, getStredHledani().lat +1, getStredHledani().lon +1));

			// sb.append(URL_PREFIX);
			// sb.append("&q=");
			// sb.append(URLEncoder.encode(getVzorek(), "utf8"));
			// sb.append("&ll=");
			// sb.append(getStredHledani().lat);
			// sb.append(",");
			// sb.append(getStredHledani().lon);
			// sb.append("&spn=");
			// sb.append("1,1");
			url = new URL(URL_PREFIX + "&address=" + URLEncoder.encode(getVzorek(), "utf8") + "&language=cs");
			System.out.println("Hledaci URL: " + url);
		} catch (MalformedURLException | UnsupportedEncodingException e1) {
			throw new RuntimeException(e1);
		}
		return url;
	}

}
