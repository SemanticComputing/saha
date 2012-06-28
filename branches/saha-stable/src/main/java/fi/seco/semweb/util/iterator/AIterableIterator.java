package fi.seco.semweb.util.iterator;

import java.util.Iterator;

public abstract class AIterableIterator<E> implements IIterableIterator<E> {

	public final Iterator<E> iterator() {
		return this;
	}

	public abstract boolean hasNext();

	public abstract E next();

	public void remove() {

	}

}
