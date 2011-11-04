package fi.seco.saha3.infrastructure;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import fi.helsinki.cs.seco.onki.service.ArrayOfStatement;
import fi.helsinki.cs.seco.onki.service.ArrayOfString;
import fi.helsinki.cs.seco.onki.service.IOnkiQuery;
import fi.helsinki.cs.seco.onki.service.IOnkiQueryPortType;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResult;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResults;
import fi.helsinki.cs.seco.onki.service.Statement;
import fi.seco.saha3.factory.TreeBuilder.UITreeNode;
import fi.seco.saha3.model.IRepository;
import fi.seco.saha3.model.IResults;
import fi.seco.saha3.model.ISahaProperty;
import fi.seco.saha3.model.UriLabel;
import fi.seco.saha3.model.configuration.PropertyConfig;

public class OnkiWebService {

	public class OnkiProperty implements ISahaProperty {
		private final Statement s;
		private boolean literal;
		private String label;

		public OnkiProperty(Statement s) {
			this.s = s;
			this.label = getString(s.getLabel());
			if (label.isEmpty()) {
				label = getString(s.getValue());
				literal = true;
			}
		}

		@Override
		public String getLabel() {
			return getString(s.getPredicateLabel());
		}

		@Override
		public String getUri() {
			return getString(s.getPredicateUri());
		}

		@Override
		public String getValueLabel() {
			return label;
		}

		@Override
		public String getValueLang() {
			return getString(s.getLabel());
		}

		@Override
		public String getValueShaHex() {
			return DigestUtils.shaHex(getValueLabel());
		}

		@Override
		public String getValueUri() {
			return getString(s.getUri());
		}

		@Override
		public String getValueDatatypeUri() {
			return "";
		}

		private String getString(JAXBElement<String> e) {
			if (e != null) {
				String s = e.getValue();
				if (s != null) return s;
			}
			return "";
		}

		@Override
		public boolean isLiteral() {
			return literal;
		}

		@Override
		public int compareTo(ISahaProperty o) {
			int c = String.CASE_INSENSITIVE_ORDER.compare(getValueLabel(), o.getValueLabel());
			return c != 0 ? c : getValueUri().compareTo(o.getValueUri());
		}

		@Override
		public String getValueTypeLabel() {
			return "";
		}

		@Override
		public String getValueTypeUri() {
			return "";
		}

		@Override
		public String getComment() {
			return "";
		}

		@Override
		public PropertyConfig getConfig() {
			return new PropertyConfig();
		}

		@Override
		public Set<String> getRange() {
			return Collections.emptySet();
		}

		@Override
		public List<UriLabel> getRangeUriLabel() {
			return Collections.emptyList();
		}

		@Override
		public Set<UITreeNode> getRangeTree() {
			return Collections.emptySet();
		}
	}

	public class OnkiResults implements IResults {
		private final Set<IResults.IResult> results = new LinkedHashSet<IResults.IResult>();
		private final int size;

		private OnkiResults(List<OnkiQueryResult> onkiResults, int size) {
			for (OnkiQueryResult result : onkiResults)
				results.add(new IResults.Result(result.getUri().getValue(), result.getTitle().getValue(), result.getAltLabel().getValue()));
			this.size = size;
		}

		@Override
		public int getSize() {
			return size;
		}

		@Override
		public Iterator<IResults.IResult> iterator() {
			return results.iterator();
		}
	}

	public class OnkiRepository implements IRepository {
		private final IOnkiQueryPortType port;
		private final String ontology;

		private OnkiRepository(String ontology) {
			this.ontology = ontology;

			URL url = null;
			try {
				url = ontology.startsWith("http://") ? new URL(ontology) : new URL("http://www.yso.fi/onkiwebservice/wsdl/?o=" + ontology + "&k=" + accessKey);
			} catch (MalformedURLException e) {
				log.error("", e);
			}
			IOnkiQueryPortType tport = null;
			try {
				IOnkiQuery oq = new IOnkiQuery(url, new QName("http://service.onki.seco.cs.helsinki.fi", "IOnkiQuery"));
				tport = oq.getOnkiQuery();
				((BindingProvider) tport).getRequestContext().put("com.sun.xml.ws.connect.timeout", 5000);
			} catch (Exception e) {
				log.error("Couldn't initialize web service to ONKI ontology " + ontology, e);
			}
			port = tport;
		}

		@Override
		public IResults search(String queryTerm, Collection<String> parentUris, Collection<String> typeUris,
				Locale locale, int maxResults) {
			try {
				OnkiQueryResults rs = port.search(queryTerm, locale.getLanguage(), maxResults, toArrayOfString(typeUris), toArrayOfString(parentUris), null);
				return new OnkiResults(rs.getResults().getValue().getOnkiQueryResult(), rs.getMetadata().getValue().getTotalHitsAmount());
			} catch (Exception e) {
				log.error("search(" + queryTerm + ", " + parentUris + ", " + typeUris + ", " + locale + ", " + maxResults + ") web service call to ONKI ontology " + ontology + " failed", e);
				return null;
			}
		}

		private ArrayOfString toArrayOfString(Collection<String> list) {
			if (list == null || list.isEmpty()) return null;
			ArrayOfString a = new ArrayOfString();
			for (String s : list)
				a.getString().add(s);
			return a;
		}

		public String getLabel(String uri, Locale locale) {
			return getLabel(uri, locale.getLanguage());
		}

		public String getLabel(String uri, String lang) {
			try {
				OnkiQueryResult r = port.getLabel(uri, lang);
				String label = r.getTitle().getValue();
				return !label.contains("*") ? label : uri;
			} catch (Exception e) {
				log.error("getLabel(" + uri + "," + lang + ") web service call to ONKI ontology " + ontology + " failed", e);
				return null;
			}
		}

		public Set<ISahaProperty> getProperties(String uri, Locale locale) {
			Set<ISahaProperty> properties = new TreeSet<ISahaProperty>();
			ArrayOfStatement statements = null;
			try {
				statements = port.getProperties(uri, null, locale.getLanguage());
			} catch (Exception e) {
				log.error("getProperties(" + uri + ", " + locale + ") web service call to ONKI ontology " + ontology + " failed", e);
				return properties;
			}
			for (Statement s : statements.getStatement()) {
				OnkiProperty property = new OnkiProperty(s);
				if (!property.getValueLabel().isEmpty()) properties.add(property);
			}
			return properties;
		}

		public Map<UriLabel, Set<ISahaProperty>> getPropertyMap(String uri, Locale locale) {
			Map<UriLabel, Set<ISahaProperty>> map = new TreeMap<UriLabel, Set<ISahaProperty>>();
			for (ISahaProperty property : getProperties(uri, locale)) {
				UriLabel p = new UriLabel(property.getUri(), property.getLabel());
				if (!map.containsKey(p)) map.put(p, new TreeSet<ISahaProperty>());
				map.get(p).add(property);
			}
			return map;
		}

		public Set<Entry<UriLabel, Set<ISahaProperty>>> getPropertyMapEntrySet(String uri, Locale locale) {
			return getPropertyMap(uri, locale).entrySet();
		}
	}

	private final Logger log = Logger.getLogger(getClass());
	private String accessKey;

	private final Map<String, OnkiRepository> onkiCache = new HashMap<String, OnkiRepository>();

	public OnkiWebService() {

	}

	@Required
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public OnkiRepository getOnkiRepository(String ontology) {
		if (!onkiCache.containsKey(ontology)) onkiCache.put(ontology, new OnkiRepository(ontology));
		return onkiCache.get(ontology);
	}

}
