package fi.seco.semweb.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public abstract class ABinaryHeap<T> extends AbstractCollection<T> {
	private final Object[] heap;
	private int lastIndex;
	private final int maxIndex;

	protected abstract boolean lessThan(T a, T b);

	public ABinaryHeap(int maxSize) {
		lastIndex = -1;
		heap = new Object[maxSize];
		maxIndex = maxSize - 1;
	}

	public ABinaryHeap(Object[] a) {
		lastIndex = a.length - 1;
		maxIndex = lastIndex;
		heap = a;
		heapify();
	}

	protected ABinaryHeap(Object[] a, boolean mark) {
		lastIndex = a.length - 1;
		maxIndex = lastIndex;
		heap = a;
	}

	protected ABinaryHeap(Collection<T> a, boolean mark) {
		this(a.toArray(), mark);
	}

	public ABinaryHeap(Collection<T> a) {
		this(a.toArray());
	}

	@Override
	public final boolean add(T element) {
		lastIndex++;
		heap[lastIndex] = element;
		upHeap();
		return true;
	}

	public final T putIfSpace(T element) {
		if (lastIndex < maxIndex) {
			add(element);
			return null;
		} else if (!lessThan(element, peekFirst())) {
			@SuppressWarnings("unchecked") T last = (T) heap[0];
			heap[0] = element;
			downHeap(0);
			return last;
		} else return element;
	}

	@SuppressWarnings("unchecked")
	public final T peekFirst() {
		return (T) heap[0];
	}

	@SuppressWarnings("unchecked")
	public final T removeFirst() {
		T result = (T) heap[0];
		heap[0] = heap[lastIndex];
		lastIndex--;
		downHeap(0);
		return result;
	}

	@Override
	public final int size() {
		return lastIndex + 1;
	}

	@Override
	public final void clear() {
		lastIndex = -1;
	}

	@SuppressWarnings("unchecked")
	private final void upHeap() {
		int i = lastIndex;
		T node = (T) heap[i];
		int parent = (i - 1) >> 1;
		while (parent >= 0 && lessThan(node, (T) heap[parent])) {
			heap[i] = heap[parent];
			i = parent;
			parent = (parent - 1) >> 1;
		}
		heap[i] = node;
	}

	protected void heapify() {
		for (int i = (lastIndex >> 1); i >= 0; i--)
			downHeap(i);
	}

	@SuppressWarnings("unchecked")
	private final void downHeap(int index) {
		T node = (T) heap[index];
		int childIndex = (index << 1) + 1;
		int otherChildIndex = childIndex + 1;
		if (otherChildIndex <= lastIndex && lessThan((T) heap[otherChildIndex], (T) heap[childIndex]))
			childIndex = otherChildIndex;
		while (childIndex <= lastIndex && lessThan((T) heap[childIndex], node)) {
			heap[index] = heap[childIndex];
			index = childIndex;
			childIndex = (index << 1) + 1;
			otherChildIndex = childIndex + 1;
			if (otherChildIndex <= lastIndex && lessThan((T) heap[otherChildIndex], (T) heap[childIndex]))
				childIndex = otherChildIndex;
		}
		heap[index] = node;
	}

	@Override
	public final boolean isEmpty() {
		return lastIndex == -1;
	}

	public void sort() {
		for (int i = 1; i < lastIndex; i++)
			downHeap(i);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			int ci = 0;

			@Override
			public boolean hasNext() {
				return ci <= lastIndex;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			@SuppressWarnings("unchecked")
			public T next() {
				return (T) heap[ci++];
			}

		};
	}

	public Iterator<T> reverseIterator() {
		return new Iterator<T>() {

			int ci = lastIndex;

			@Override
			public boolean hasNext() {
				return ci >= 0;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@SuppressWarnings("unchecked")
			@Override
			public T next() {
				return (T) heap[ci--];
			}

		};
	}

}