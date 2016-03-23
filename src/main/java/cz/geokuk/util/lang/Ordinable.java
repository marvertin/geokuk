package cz.geokuk.util.lang;

/**
 * Interface for TC ordinal data types.<BR>
 *
 * A.getAnother (n) == B.getAnother (n) <=> A == B<BR>
 * A.getAnother (n) == B <=> B.getAnother (-n) == A<BR>
 *
 * A.getDistance(B) == 0 <=> A == B<BR>
 * A.getAnother (n) == B <=> A.getDistance (B) == n<BR>
 *
 * Notice: Sometimes is A.getAnother(1) known as "successor" and A.getAnother(-1) as "ancestor".
 *
 * Title: Description: Copyright: Copyright (c) 2001 Company: TurboConsult s.r.o.
 * 
 * @author Michal Polák
 * @version 1.0
 */
public interface Ordinable<T> {

	/**
	 * Get another ordinal object of me.
	 */
	public T getAnother(long aNthObject);

	/**
	 * Get distance from me to given object.<BR>
	 */
	public long getDistance(T aObject);
}
