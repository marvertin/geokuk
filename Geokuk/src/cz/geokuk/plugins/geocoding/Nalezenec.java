package cz.geokuk.plugins.geocoding;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.Nalezenec0;


public class Nalezenec extends Nalezenec0 {

  public String accurancy;
  public String adresa;
  public String administrativeArea; // Jihomoravský Kraj, vYSOČINA
  public String subAdministrativeArea; // Brno, Jihlava, Maršov; Veverská Bítýška
  public String locality; // Černá pole; Henčov; -; -; Pazderna
  public String thoroughfare; // Lidická 1880/50 ; 4079; 163
  public Wgs wgs;

  @Override
  public Wgs getWgs() {
    return wgs;
  }

}
