package fi.seco.semweb.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	protected final int maxSize;

	public LRUCache(int maxSize) {
		super(maxSize+1,0.75F, true);
		this.maxSize=maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
		if (size()> maxSize) return true;
		return false;
	}
}
