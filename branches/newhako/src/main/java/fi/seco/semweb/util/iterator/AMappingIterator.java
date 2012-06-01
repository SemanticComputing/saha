package fi.seco.semweb.util.iterator;

import java.util.Iterator;

public abstract class AMappingIterator<O,E> extends AIterableIterator<E> {

	protected Iterator<? extends O> iter;

	public AMappingIterator(IIterableIterator<? extends O> iter) {
		this.iter=iter;
	}

	public AMappingIterator(Iterator<? extends O> iter) {
		this.iter=iter;
	}

	public AMappingIterator(Iterable<? extends O> col) {
  	  this(col.iterator());
	}

	@Override
	public final boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public final E next() {
		return map(iter.next());
	}

	@Override
	public final void remove() {
		iter.remove();
	}

	public abstract E map(O src);

}
