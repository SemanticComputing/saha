package fi.seco.saha3.infrastructure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.vocabulary.RDFS;

import fi.helsinki.cs.seco.onki.service.ArrayOfStatement;
import fi.helsinki.cs.seco.onki.service.ArrayOfString;
import fi.helsinki.cs.seco.onki.service.IOnkiQuery;
import fi.helsinki.cs.seco.onki.service.IOnkiQueryPortType;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResult;
import fi.helsinki.cs.seco.onki.service.OnkiQueryResults;
import fi.helsinki.cs.seco.onki.service.Statement;
import fi.seco.saha3.model.IResults;
import fi.seco.saha3.model.IResults.IResult;
import fi.seco.saha3.model.IResults.Result;
import fi.seco.saha3.model.IResults.Results;
import fi.seco.saha3.model.ISahaProperty;
import fi.seco.saha3.model.UITreeNode;
import fi.seco.saha3.model.UriLabel;
import fi.seco.saha3.model.configuration.PropertyConfig;
import fi.seco.saha3.util.SAHA3;

/**
 * Mediator class between ONKI and SAHA ontology searches. Encodes and decodes
 * the requests between SAHA data models and the SOAP interface used for the
 * remote queries.
 * 
 */
public class ExternalRepositoryService {

	private static class ExternalProperty implements ISahaProperty {

		private final String predicateURI;
		private final String predicateLabel;
		private final String valueTypeURI;
		private final String valueTypeLabel;
		private final UriLabel value;

		public ExternalProperty(String predicateURI, String predicateLabel, UriLabel value, String valueTypeURI,
				String valueTypeLabel) {
			this.predicateURI = predicateURI;
			this.predicateLabel = predicateLabel;
			this.value = value;
			this.valueTypeURI = valueTypeURI;
			this.valueTypeLabel = valueTypeLabel;
		}

		@Override
		public String getLabel() {
			return predicateLabel;
		}

		@Override
		public String getUri() {
			return predicateURI;
		}

		@Override
		public String getValueLabel() {
			return value.getLabel();
		}

		@Override
		public String getValueLang() {
			return value.getLang();
		}

		@Override
		public String getValueShaHex() {
			return value.getShaHex();
		}

		@Override
		public String getValueUri() {
			return value.getUri();
		}

		@Override
		public String getValueDatatypeUri() {
			return value.getDatatype();
		}

		@Override
		public boolean isLiteral() {
			return "".equals(value.getUri());
		}

		@Override
		public int compareTo(ISahaProperty o) {
			int c = String.CASE_INSENSITIVE_ORDER.compare(getValueLabel(), o.getValueLabel());
			return c != 0 ? c : getValueUri().compareTo(o.getValueUri());
		}

		@Override
		public String getValueTypeLabel() {
			return valueTypeLabel;
		}

		@Override
		public String getValueTypeUri() {
			return valueTypeURI;
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

	private static String getString(JAXBElement<String> e) {
		if (e != null) {
			String s = e.getValue();
			if (s != null) return s;
		}
		return "";
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

	public class OnkiRepository implements IExternalRepository {
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

		@Override
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

		@Override
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
				String label = getString(s.getLabel());
				String predicateURI = getString(s.getPredicateUri());
				String predicateLabel = getString(s.getPredicateLabel());
				String lang = getString(s.getLang());
				String suri = getString(s.getUri());
				if (label.isEmpty()) label = suri;
				ExternalProperty property = new ExternalProperty(predicateURI, predicateLabel, new UriLabel(suri, lang, label), "", "");
				properties.add(property);
			}
			return properties;
		}

	}

	private final Logger log = LoggerFactory.getLogger(getClass());
	private String accessKey;

	private final Map<String, OnkiRepository> onkiCache = new HashMap<String, OnkiRepository>();

	public ExternalRepositoryService() {

	}

	@Required
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	private final IExternalRepository geonamesRepository = new IExternalRepository() {

		@Override
		public IResults search(String queryTerm, Collection<String> parentUris, Collection<String> typeUris,
				Locale locale, int maxResults) {
			final List<IResult> ret = new ArrayList<IResult>();
			WebService.setUserName("jiemakel");
			ToponymSearchCriteria tsc = new ToponymSearchCriteria();
			tsc.setNameStartsWith(queryTerm);
			ToponymSearchResult tsr;
			try {
				tsr = WebService.search(tsc);
				for (Toponym t : tsr.getToponyms())
					ret.add(new Result(toUri(t.getGeoNameId()), t.getName() + ", " + t.getCountryName()));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return new Results(ret, tsr.getTotalResultsCount());
		}

		private String toUri(int id) {
			return "http://sws.geonames.org/" + id + "/";
		}

		private int toId(String uri) {
			return Integer.valueOf(uri.substring("http://sws.geonames.org/".length(), uri.length() - 1));
		}

		@Override
		public Set<ISahaProperty> getProperties(String uri, Locale locale) {
			try {
				String lstring = locale.toString();
				Toponym t = WebService.get(toId(uri), lstring, "medium");
				Set<ISahaProperty> ret = new HashSet<ISahaProperty>();
				ret.add(new ExternalProperty(RDFS.label.getURI(), lstring.equals("fi") ? "nimi" : lstring.equals("sv") ? "namn" : "name", new UriLabel("", lstring, t.getName()), "", ""));
				ret.add(new ExternalProperty(SAHA3.WGS84_LAT, "lat", new UriLabel("", lstring, "" + t.getLatitude()), "", ""));
				ret.add(new ExternalProperty(SAHA3.WGS84_LONG, "lon", new UriLabel("", lstring, "" + t.getLongitude()), "", ""));
				return ret;
			} catch (NumberFormatException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getLabel(String uri, String lang) {
			try {
				Toponym t = WebService.get(toId(uri), lang, "medium");
				return t.getName();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	};

	public IExternalRepository getExternalRepository(String ontology) {
		if (ontology.equals("geonames")) return geonamesRepository;
		if (!onkiCache.containsKey(ontology)) onkiCache.put(ontology, new OnkiRepository(ontology));
		return onkiCache.get(ontology);
	}

}
