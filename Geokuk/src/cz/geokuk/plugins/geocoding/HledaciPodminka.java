/**
 * 
 */
package cz.geokuk.plugins.geocoding;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import cz.geokuk.core.hledani.HledaciPodminka0;

/**
 * @author veverka
 *
 */
public class HledaciPodminka extends HledaciPodminka0 {

  private static final String URL_PREFIX = "http://maps.google.com/maps/geo?output=xml&sensor=false&key=geokuk&gl=CZ";

  URL computeUrl() {
    URL url;
    try {
      StringBuilder sb = new StringBuilder();
      sb.append(URL_PREFIX);
      sb.append("&q=");
      sb.append(URLEncoder.encode(getVzorek(), "utf8"));
      sb.append("&ll=");
      sb.append(getStredHledani().lat);
      sb.append(",");
      sb.append(getStredHledani().lon);
      sb.append("&spn=");
      sb.append("1,1");
      url = new URL(sb.toString());
    } catch (MalformedURLException e1) {
      throw new RuntimeException(e1);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    return url;
  }

}
