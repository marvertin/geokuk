/**
 *
 */
package cz.geokuk.util.file;

import java.util.*;

/**
 * Jménovací uzel podle vzoru composite.
 *
 * @author veverka
 */
public class KeyNode<K, D> {

	private D									data;
	private final SortedMap<K, KeyNode<K, D>>	items	= new TreeMap<>();

	public KeyNode<K, D> locate(final List<K> keys) {
		if (keys.isEmpty()) {
			return this;
		} else {
			final K key = keys.get(0);
			final KeyNode<K, D> node = items.get(key);
			if (node == null) {
				return null;
			}
			return node.locate(keys.subList(1, keys.size())); // a s o jedno míň zařaď
		}
	}

	public KeyNode<K, D> locate(final K key) {
		return locate(Collections.singletonList(key));
	}

	public SortedMap<K, KeyNode<K, D>> getItems() {
		return items;
	}

	public List<KeyNode<K, D>> getSubNodes() {
		return new ArrayList<>(items.values());

	}

	public Set<K> getKeys() {
		return items.keySet();
	}

	void add(final D data, final List<K> keys) {
		if (keys.isEmpty()) {
			this.data = data;
		} else {
			final K key = keys.get(0);
			KeyNode<K, D> node = items.get(key);
			if (node == null) {
				node = new KeyNode<>();
				items.put(key, node);
			}
			node.add(data, keys.subList(1, keys.size())); // a s o jedno míň zařaď
		}
	}

	void remove(final List<K> keys) {
		if (keys.isEmpty()) {
			this.data = null;
			items.clear();
		} else {
			final K key = keys.get(0);
			final KeyNode<K, D> node = items.get(key);
			if (node == null) {
				return; // není tam
			}
			node.remove(keys.subList(1, keys.size()));
			if (node.data == null && node.items.isEmpty()) {
				items.remove(key);
			}
		}
	}

	void print(final String prefix) {
		System.out.println(data);
		for (final K key : items.keySet()) {
			System.out.print(prefix + key + ": ");
			items.get(key).print(prefix + "     ");
		}
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
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

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
		final KeyNode other = (KeyNode) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		if (items == null) {
			if (other.items != null) {
				return false;
			}
		} else if (!items.equals(other.items)) {
			return false;
		}
		return true;
	}

	public D getData() {
		return data;
	}

	@Override
	public String toString() {
		return "KeyNode [data=" + data + "] " + getKeys();
	}

}
