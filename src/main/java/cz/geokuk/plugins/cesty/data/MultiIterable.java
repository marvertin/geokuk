package cz.geokuk.plugins.cesty.data;

import java.util.Iterator;

import cz.geokuk.util.lang.EmptyIterator;

public abstract class MultiIterable<T, M> implements Iterable<T> {

	private final Iterable<M> list;

	MultiIterable(final Iterable<M> list) {
		this.list = list;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator<M>	ceit	= list.iterator();
			Iterator<T>	boit	= new EmptyIterator<>();

			{
				najezd();
			}

			@Override
			public void remove() {
				throw new RuntimeException("Not implemented");
			}

			@Override
			public T next() {
				final T bod = boit.next();
				najezd();
				return bod;
			}

			private void najezd() {
				while (!boit.hasNext() && ceit.hasNext()) {
					final M cesta = ceit.next();
					boit = prepareIterable(cesta).iterator();
				}
			}

			@Override
			public boolean hasNext() {
				return boit.hasNext();
			}
		};
	}

	// boit = ceit.next().getBody().iterator();
	protected abstract Iterable<T> prepareIterable(M m);
}
