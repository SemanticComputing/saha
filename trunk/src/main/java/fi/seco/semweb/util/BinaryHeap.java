package fi.seco.semweb.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** 
 * For iterating an array in order.
 * <ul>
 *  <li>Constructing the heap is O(n) with a low constant factor</li>
 *  <li>deleteMin (or Iterator next()) is O(log n)</li>
 * </ul>
 * Iterator can be called only once (as it modifies the heap).
 * 
 */
public class BinaryHeap<T> implements Iterable<T> {
	
	private T[] tree;
	private Comparator<T> c;
	private int size;

	@SuppressWarnings("unchecked")
	public BinaryHeap(Collection<T> a, Comparator<T> c) {
		this((T[])a.toArray(),c);
	}
	
	public BinaryHeap(T[] a, Comparator<T> c) {
		this.c = c;
		this.tree = a;
		this.size = a.length;
		heapify();
	}

    public Iterator<T> iterator() {
    	return new Iterator<T>() {
			public boolean hasNext() {
				return !isEmpty();
			}
			public T next() {
				if (hasNext()) return deleteMin();
				throw new NoSuchElementException();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
    }

	public boolean isEmpty() {
		return size == 0;
	}
	
    public T deleteMin() {
        if (isEmpty())
            return null;

		size--;

        T min = tree[0];

        tree[0] = tree[size];
        percolateDown(0);

        return min;
    }
    
	private void heapify() {
		for (int i=(tree.length/2)-1; i>=0; i--)
			percolateDown(i);
	}
    
	private void percolateDown(int index) {
		T tmp = tree[index];
		while (2*index+1 < size) {
			int child = 2*index+1;

			if (child < size-1 && c.compare(tree[child],tree[child+1]) > 0)
				child++;

			if (c.compare(tmp,tree[child]) > 0)
				tree[index] = tree[child];
			else
				break;

			index=child;
		}
		tree[index] = tmp;
	}
}