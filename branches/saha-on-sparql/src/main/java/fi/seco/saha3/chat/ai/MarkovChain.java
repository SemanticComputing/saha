package fi.seco.saha3.chat.ai;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;

public class MarkovChain {
	
	private static class HashableArray<T> {
		private final T[] arr;
		public HashableArray(T[] arr) {
			this.arr = arr;
		}
		@SuppressWarnings("unchecked")
		@Override
		public final boolean equals(Object o) {
			if (o != null && o.getClass().equals(getClass()))
				return Arrays.equals(arr,((HashableArray<T>)o).arr);
			return false;
		}
		@Override
		public final int hashCode() {
			int hash = 0;
			for (T s : arr)
				hash = 31 * hash + s.hashCode();
			return hash;
		}
		public final T[] getArray() {
			return arr;
		}
	}
	
	private Map<HashableArray<String>,TObjectIntHashMap> matrix = 
		new HashMap<HashableArray<String>,TObjectIntHashMap>();
	private int order = 2;
	
	private CircularFifoBuffer<String> history = new org.apache.commons.collections15.buffer.CircularFifoBuffer<String>(order);
	private Random rnd = new Random();
    private IWordBias bias = null;
	
	public void setBias(IWordBias bias) {
	    this.bias = bias;
	}
	
	public void learn(String sample) {
		for (String word : sample.split("\\s")) {
			if (!word.isEmpty()) {
				if (history.size() == order)
					putResult(new HashableArray<String>(history.toArray(new String[history.size()])),word);
				history.add(word);
			}
		}
	}
	
	private void putResult(HashableArray<String> history, String word) {
		if (!matrix.containsKey(history))
			matrix.put(history,initMap(word));
		else
			incrementWordFreq(matrix.get(history),word);
	}

	private TObjectIntHashMap initMap(String word) {
		TObjectIntHashMap map = new TObjectIntHashMap();
		map.put(word,1);
		return map;
	}
	
	private void incrementWordFreq(TObjectIntHashMap map, String word) {
		if (!map.containsKey(word))
			map.put(word,1);
		else
			map.put(word,map.get(word)+1);
	}
	
	public String simulate(int length) {
		StringBuilder buffer = new StringBuilder();
		
		int r = rnd.nextInt(matrix.size());
		Iterator<HashableArray<String>> iter = matrix.keySet().iterator();
		HashableArray<String> start = iter.next();
		for (int i=0;i<r-1;i++) start = iter.next();
		
		history.clear();
		for (String s : start.getArray()) history.add(s);
		
		for (int i=0;i<length;i++) {
			String word = getWord(new HashableArray<String>(history.toArray(new String[history.size()])));
			buffer.append(word + " ");
			if (i % 10 == 0) buffer.append("\n");
			history.add(word);
		}
		
		return buffer.toString();
	}
	
	private TObjectIntHashMap filter(TObjectIntHashMap map) {
		String[] biased = bias.filterChoices((String[]) map.keys());
		if (biased != null) {
			map = (TObjectIntHashMap) map.clone();
			TObjectIntIterator it = map.iterator();
			while (it.hasNext()) {
			    it.advance();
			    for (String b : biased) if (!it.key().equals(b))
			    	it.remove();
			}
		}
		return map;
	}
	
	private String getWord(HashableArray<String> history) {
		TObjectIntHashMap map = matrix.get(history);
		
		if (bias != null) map = filter(map);
		
		int r = rnd.nextInt(getSum(map.getValues())); 
		
		TObjectIntIterator it = map.iterator();
		while (it.hasNext()) {
		    it.advance();
			r -= it.value();
			if (r <= 0) return (String) it.key();
		}
		return null;
	}
	
	private int getSum(int[] i) {
		int sum = 0;
		for (int x : i) sum += x;
		return sum;
	}
	
}
