package fi.seco.semweb.util.iterator;

import java.util.Iterator;

public class IteratorToIIterableIterator<E> implements IIterableIterator<E> {

	private Iterator<E> iter;

	public IteratorToIIterableIterator(Iterator<E> iter) {
		this.iter=iter;
	}

	public final Iterator<E> iterator() {
		return iter;
	}

	public final boolean hasNext() {
		return iter.hasNext();
	}

	public final E next() {
		return iter.next();
	}

	public final void remove() {
		iter.remove();
	}

}
