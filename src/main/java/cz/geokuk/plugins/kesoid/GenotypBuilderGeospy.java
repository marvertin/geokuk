/**
 * 
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

class GenotypBuilderGeospy {

  /**
   * 
   */
	private final Genotyp g;
  private final Genom genom;

  /**
   * 
   */
  public GenotypBuilderGeospy(Genom genom, Genotyp g) {
		this.genom = genom;
    this.g = g;
  }
  
  
  public void build(Geospy geospy) {
    g.put(genom.ALELA_gs);
    switch (geospy.getVztah()) {
    case NORMAL: g.put(genom.ALELA_hnf); break;
    case FOUND:  g.put(genom.ALELA_gssec); break;
    case OWN:    g.put(genom.ALELA_gsown); break;
    case NOT:    g.put(genom.ALELA_not); break;
    }
  }




}