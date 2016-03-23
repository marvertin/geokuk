/**
 *
 */
package cz.geokuk.util.file;

import java.util.Arrays;
import java.util.List;

/**
 * Jménovací uzel podle vzoru composite.
 *
 * @author Martin Veverka
 */
public class KeyTree<K, D> {

	private final KeyNode<K, D> root = new KeyNode<>();

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings({ "rawtypes" })
		final KeyTree other = (KeyTree) obj;
		if (root == null) {
			if (other.root != null) {
				return false;
			}
		} else if (!root.equals(other.root)) {
			return false;
		}
		return true;
	}

	public KeyNode<K, D> getRoot() {
		return root;
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
		result = prime * result + (root == null ? 0 : root.hashCode());
		return result;
	}

	public KeyNode<K, D> locate(final List<K> keys) {
		return root.locate(keys);
	}

	public void print() {
		root.print("");
	}

	void add(final D data, @SuppressWarnings("unchecked") final K... keys) {
		add(data, Arrays.asList(keys));
	}

	void add(final D data, final List<K> keys) {
		root.add(data, keys);
	}

	void remove(@SuppressWarnings("unchecked") final K... keys) {
		remove(Arrays.asList(keys));
	}

	void remove(final List<K> keys) {
		root.remove(keys);
	}

}
