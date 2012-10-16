package fi.seco.semweb.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

public class LString implements Serializable {

	/**
	 * Literal label language support
	 */
	private static final long serialVersionUID = 1L;
	private final LinkedHashMultimap<Locale, String> strings = LinkedHashMultimap.create();

	public LString() {

	}

	public LString(String string) {
		strings.put(null, string);
	}

	public LString(Locale lang, String string) {
		strings.put(lang, string);
	}

	public String get(Locale lang) {
		Collection<String> c = strings.get(lang);
		if (c == null || c.isEmpty()) return null;
		return c.iterator().next();
	}

	public String getMatch(Locale lang) {
		if (get(lang) != null) return get(lang);
		if (lang == null) return null;
		String tmp = get(new Locale(lang.getLanguage(), lang.getCountry()));
		if (tmp != null) return tmp;
		return get(new Locale(lang.getLanguage()));
	}

	public String getMatchAny(Locale lang) {
		String ret = getMatch(lang);
		if (ret != null) return ret;
		ret = get(null);
		if (ret != null) return ret;
		if (!strings.isEmpty()) return strings.values().iterator().next();
		return null;
	}

	public Collection<String> getAll() {
		return strings.values();
	}

	public LinkedHashMultimap<Locale, String> getAllAsMap() {
		return strings;
	}

	public LString add(String string) {
		add(null, string);
		return this;
	}

	public LString addAll(Map<Locale, String> map) {
		for (Entry<Locale, String> e : map.entrySet())
			strings.put(e.getKey(), e.getValue());
		return this;
	}

	public LString addAll(SetMultimap<Locale, String> map) {
		strings.putAll(map);
		return this;
	}

	public LString add(Locale lang, String string) {
		strings.put(lang, string);
		return this;
	}

	public LString remove(Locale lang, String string) {
		strings.remove(lang, string);
		return this;
	}

	@Override
	public String toString() {
		return strings.values().toString();
	}

	public int size() {
		return strings.size();
	}

	public LString addAll(LString other) {
		strings.putAll(other.getAllAsMap());
		return this;
	}

	@Override
	public int hashCode() {
		return strings.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LString)) return false;
		return strings.equals(((LString) other).strings);
	}
}
