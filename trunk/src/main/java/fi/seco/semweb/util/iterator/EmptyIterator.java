package fi.seco.semweb.util.iterator;

import java.util.NoSuchElementException;

public final class EmptyIterator<E> extends AIterableIterator<E> implements ISerializableIterableIterator<E> {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unchecked")
	public static final EmptyIterator SHARED_INSTANCE = new EmptyIterator<Object>();

	@Override
	public final boolean hasNext() {
		return false;
	}

	@Override
	public final E next() {
		throw new NoSuchElementException();
	}

	@Override
	public final void remove() {
		throw new UnsupportedOperationException("How can you remove something from an empty iterator?");
	}

	@SuppressWarnings("unchecked")
	public final static <T> EmptyIterator<T> getInstance() {
		return SHARED_INSTANCE;
	}

}
