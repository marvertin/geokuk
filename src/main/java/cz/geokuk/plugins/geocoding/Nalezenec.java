package cz.geokuk.plugins.geocoding;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.Nalezenec0;


public class Nalezenec extends Nalezenec0 {


	public String adresa;
	public String administrativeArea; // Jihomoravský Kraj, vYSOČINA
	public String subAdministrativeArea; // Brno, Jihlava, Maršov; Veverská Bítýška
	public String locality; // Černá pole; Henčov; -; -; Pazderna
	public String thoroughfare; // Lidická 1880/50 ; 4079; 163


	//	  "ROOFTOP" indicates that the returned result is a precise geocode for which we have location information accurate down to street address precision.
	//	  "RANGE_INTERPOLATED" indicates that the returned result reflects an approximation (usually on a road) interpolated between two precise points (such as intersections). Interpolated results are generally returned when rooftop geocodes are unavailable for a street address.
	//	  "GEOMETRIC_CENTER" indicates that the returned result is the geometric center of a result such as a polyline (for example, a street) or polygon (region).
	//	  "APPROXIMATE" indicates that the returned result is approximate.
	public String locationType;

	public Wgs wgs;

	@Override
	public Wgs getWgs() {
		return wgs;
	}

}
