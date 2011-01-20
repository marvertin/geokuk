/**
 * 
 */
package cz.geokuk.plugins.kesoid.importek;

public class Groundspeak {

  public boolean archived;
  public boolean availaible;
  public String encodedHints;
  public String state;
  public String country;
  public String terrain;
  public String difficulty;
  public String container;
  public String type;
  public String placedBy;
  public String owner;
  public String name;
  public String shortDescription;
  
  @Override
  public String toString() {
    return "Groundspeak [name=" + name + ", type=" + type + ", placedBy="
    + placedBy + ", archived=" + archived + ", availaible=" + availaible
    + ", container=" + container + ", terrain=" + terrain
    + ", difficulty=" + difficulty + ", country=" + country + ", state="
    + state + ", encodedHints=" + encodedHints + "]";
  }


}