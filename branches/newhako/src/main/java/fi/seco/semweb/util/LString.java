package fi.seco.semweb.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class LString implements Serializable {

	/**
	 * Literal label language support
	 */
	private static final long serialVersionUID = 1L;
	private LinkedHashMultimap<Locale,String> strings = LinkedHashMultimap.create();

	public LString() {

	}

	public LString(String string) {
		strings.put(null,string);
	}

	public LString(Locale lang, String string) {
		strings.put(lang,string);
	}

	public LString(StmtIterator i) {
		addAll(i);
	}
	
	public LString(Iterator<Statement> i) {
		addAll(i);
	}

	public LString(Resource r, Property p) {
		addAll(r,p);
	}

	public LString(Resource r, Iterable<Property> props) {
		for (Property p: props) addAll(r,p);
	}

	public LString(Resource r, List<String> props) {
		Model m = r.getModel();
		for (String p: props) addAll(r,m.createProperty(p));
	}

	public LString(Resource r) {
		addAllLabels(r);
	}

	public String get(Locale lang) {
		Collection<String> c = strings.get(lang);
		if (c==null || c.isEmpty()) return null;
		return c.iterator().next();
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
		if (!strings.isEmpty()) return strings.values().iterator().next();
		return null;
	}

	public Collection<String> getAll() {
		return strings.values();
	}

	public LinkedHashMultimap<Locale,String> getAllAsMap() {
		return strings;
	}

	public LString add(String string) {
		add(null,string);
		return this;
	}

	public LString add(Statement s) {
		strings.put(new Locale(s.getLanguage()),s.getString());
		return this;
	}

	public LString addAll(Map<Locale,String> map) {
		for (Entry<Locale,String> e : map.entrySet()) strings.put(e.getKey(),e.getValue());
		return this;
	}

	public LString addAll(SetMultimap<Locale,String> map) {
		strings.putAll(map);
		return this;
	}

	public LString addAll(Iterator<Statement> i) {
		for (;i.hasNext();)
			add(i.next());
		return this;
	}
	
	public LString addAll(StmtIterator i) {
		for (;i.hasNext();)
			add(i.nextStatement());
		return this;
	}

	public LString addAll(Resource r, Property p) {
		for (StmtIterator t = r.listProperties(p);t.hasNext();)
			add(t.nextStatement());
		return this;
	}

	public LString addAll(Resource r, Iterable<Property> props) {
		for (Property p : props) addAll(r,p);
		return this;
	}

	public LString addAllLabels(Resource r) {
		addAll(r,RDFS.label);
		return this;
	}

	public LString add(Locale lang, String string) {
		strings.put(lang,string);
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
		return strings.equals(((LString)other).strings);
	}
}
