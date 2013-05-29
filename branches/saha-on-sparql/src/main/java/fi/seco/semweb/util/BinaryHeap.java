package fi.seco.semweb.util;

import java.util.Collection;
import java.util.Comparator;

public class BinaryHeap<T> extends ABinaryHeap<T> {

	private final Comparator<T> c;

	@Override
	protected boolean lessThan(T a, T b) {
		return c.compare(a, b) < 0;
	}

	public BinaryHeap(Collection<T> a, Comparator<T> c) {
		super(a, true);
		this.c = c;
		heapify();
	}

	public BinaryHeap(int maxSize, Comparator<T> c) {
		super(maxSize);
		this.c = c;
	}

	public BinaryHeap(T[] a, Comparator<T> c) {
		super(a, true);
		this.c = c;
		heapify();
	}

}
