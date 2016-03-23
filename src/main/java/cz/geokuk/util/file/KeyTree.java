/**
 *
 */
package cz.geokuk.util.file;

import java.util.Arrays;
import java.util.List;

/**
 * Jménovací uzel podle vzoru composite.
 * 
 * @author veverka
 */
public class KeyTree<K, D> {

	private final KeyNode<K, D> root = new KeyNode<>();

	public KeyNode<K, D> locate(List<K> keys) {
		return root.locate(keys);
	}

	void add(D data, List<K> keys) {
		root.add(data, keys);
	}

	void add(D data, @SuppressWarnings("unchecked") K... keys) {
		add(data, Arrays.asList(keys));
	}

	void remove(List<K> keys) {
		root.remove(keys);
	}

	void remove(@SuppressWarnings("unchecked") K... keys) {
		remove(Arrays.asList(keys));
	}

	public KeyNode<K, D> getRoot() {
		return root;
	}

	public void print() {
		root.print("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((root == null) ? 0 : root.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings({ "rawtypes" })
		KeyTree other = (KeyTree) obj;
		if (root == null) {
			if (other.root != null)
				return false;
		} else if (!root.equals(other.root))
			return false;
		return true;
	}

}
