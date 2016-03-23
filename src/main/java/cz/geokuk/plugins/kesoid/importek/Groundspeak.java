/**
 *
 */
package cz.geokuk.plugins.kesoid.importek;

public class Groundspeak {

	public String name;
	public String type;
	public int ownerid;
	public String owner;
	public String placedBy;
	public boolean archived;
	public boolean availaible;
	public String encodedHints;
	public String state;
	public String country;
	public String terrain;
	public String difficulty;
	public String container;
	public String shortDescription;

	@Override
	public String toString() {
		return "Groundspeak [name=" + name + ", type=" + type + ", ownerid="
				+ ownerid + ", owner=" + owner + ", placedBy=" + placedBy
				+ ", archived=" + archived + ", availaible=" + availaible
				+ ", encodedHints=" + encodedHints + ", state=" + state + ", country="
				+ country + ", terrain=" + terrain + ", difficulty=" + difficulty
				+ ", container=" + container + ", shortDescription=" + shortDescription
				+ "]";
	}
}