package fi.seco.semweb.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ImmutableLString implements Serializable {

	private static final long serialVersionUID = 1L;
	private Map<Locale,String[]> strings = new HashMap<Locale,String[]>();

	public ImmutableLString(LString lstring) {
		for (Entry<Locale,Collection<String>> e : lstring.getAllAsMap().asMap().entrySet()) if (!e.getValue().isEmpty()) this.strings.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
	}

	public ImmutableLString(Map<Locale,Set<String>> strings) {
		for (Entry<Locale,Set<String>> e : strings.entrySet()) if (!e.getValue().isEmpty()) this.strings.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
	}

	public String get(Locale lang) {
		String[] c = strings.get(lang);
		if (c==null) return null;
		return c[0];
	}

	public String getMatch(Locale lang) {
		if (get(lang)!=null) return get(lang);
		if (lang==null) return null;
		String tmp = get(new Locale(lang.getLanguage(),lang.getCountry()));
		if (tmp!=null) return tmp;
		return get(new Locale(lang.getLanguage()));
	}

	public String getMatchAny(Locale lang) {
		String ret = getMatch(lang);
		if (ret!=null) return ret;
		ret = get(null);
		if (ret!=null) return ret;
		if (!strings.isEmpty()) return strings.values().iterator().next()[0];
		return null;
	}

	public Set<String> getAll() {
		Set<String> ret = new HashSet<String>();
		for (String[] s : strings.values()) for (String ss : s) ret.add(ss);
		return ret;
	}

	public Map<Locale,String[]> getAllAsMap() {
		return strings;
	}

	@Override
	public String toString() {
		return strings.values().toString();
	}

	public void serializeToRDFAsValuesOfProperty(Resource r, Property p) {
		for (Locale lang : strings.keySet())
			if (lang == null) r.addProperty(p,get(null));
			else r.addProperty(p,r.getModel().createLiteral(get(lang),lang.toString()));
	}

	public void serializeToRDFAsLabelsOf(Resource r) {
		serializeToRDFAsValuesOfProperty(r,RDFS.label);
	}

	public int size() {
		return strings.size();
	}

	public ImmutableLString addAll(ImmutableLString other) {
		strings.putAll(other.getAllAsMap());
		return this;
	}

	@Override
	public int hashCode() {
		return strings.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ImmutableLString)) return false;
		return strings.equals(((ImmutableLString)other).strings);
	}

}
