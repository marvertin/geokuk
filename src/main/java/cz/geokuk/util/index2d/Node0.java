package cz.geokuk.util.index2d;

public abstract class Node0<T> {

	int count;

	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}


	abstract boolean isSheet();


	abstract void visit(BoundingRect rect, Visitor<T> visitor);

	/**
	 * @param aPrefix
	 * @param aLevel
	 */
	abstract void vypis(String aPrefix, int aLevel);
}
