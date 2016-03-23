/**
 *
 */
package cz.geokuk.plugins.kesoid.importek;

import java.util.HashMap;
import java.util.Map;

/**
 * @author veverka
 *
 */
public class Gpxg {

	public int elevation;
	public String found;
	public int flag;
	public int hodnoceni;
	public int hodnoceniPocet;
	public int bestOf;
	public int favorites;
	public int znamka;
	public String czkraj;
	public String czokres;
	public Map<String, String> userTags = new HashMap<>();

	public void putUserTag(final String genname, final String alelaname) {
		userTags.put(genname, alelaname);
	}

}
