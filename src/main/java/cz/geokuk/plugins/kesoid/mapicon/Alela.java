package cz.geokuk.plugins.kesoid.mapicon;

import java.util.HashSet;
import java.util.Set;


public class Alela {

  private final String alelaName;
  private String displayName;
  private Gen gen;
  private Grupa grupa;
	private final int celkovePoradi;

  /**
   * @return the grupa
   */
  public Grupa getGrupa() {
    return grupa;
  }

  /**
   * @param aGrupa the grupa to set
   */
  public void setGrupa(Grupa aGrupa) {
    grupa = aGrupa;
  }

  public Alela(String alelaName, int celkovePoradi) {
    this.alelaName = alelaName;
    this.displayName = alelaName;
		this.celkovePoradi = celkovePoradi;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName == null ? alelaName : displayName;
  }

  public boolean hasGen() {
  	return gen != null;
  }
  
  public Gen getGen() {
  	if (gen == null) {
  		throw new RuntimeException("Alela " + alelaName + " nemá gen!");
  	}
    return gen;
  }

  public void setGen(Gen gen) {
    this.gen = gen;
  }

  @Override
  public String toString() {
    return alelaName;
  }

  public boolean isVychozi() {
    return gen.getVychoziAlela() == this;
  }


  public String name() {
    return alelaName;
  }

  public static Set<String> alelyToNames(Set<Alela> alely) {
    Set<String> jmenaAlel = new HashSet<>(alely.size());
    for (Alela alela : alely) {
      jmenaAlel.add(alela.name());
    }
    return jmenaAlel;
  }

	public int getCelkovePoradi() {
  	return celkovePoradi;
  }
	
	
	public Genom getGenom() {
		return getGen().getGenom();
	}

	public boolean isSym() {
		return getGenom().isAlelaSym(this);
	}
}
