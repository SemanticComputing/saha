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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.sun.xml.internal.ws.developer.JAXWSProperties;
import com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe;

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

@SuppressWarnings("restriction")
public class OnkiWebService {

	public class OnkiProperty implements ISahaProperty {
		private Statement s;
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
		public String getLabel() {
			return getString(s.getPredicateLabel());
		}
		public String getUri() {
			return getString(s.getPredicateUri());
		}
		public String getValueLabel() {
			return label;
		}
		public String getValueLang() {
			return getString(s.getLabel());
		}
		public String getValueShaHex() {
			return DigestUtils.shaHex(getValueLabel());
		}
		public String getValueUri() {
			return getString(s.getUri());
		}
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
		public boolean isLiteral() {
			return literal;
		}
		public int compareTo(ISahaProperty o) {
			int c = String.CASE_INSENSITIVE_ORDER.compare(getValueLabel(),o.getValueLabel());
			return c != 0 ? c : getValueUri().compareTo(o.getValueUri());
		}
		public String getValueTypeLabel() { return ""; }
		public String getValueTypeUri() { return ""; }
		public String getComment() { return ""; }
		public PropertyConfig getConfig() {	return new PropertyConfig(); }
		public Set<String> getRange() {	return Collections.emptySet(); }
		public List<UriLabel> getRangeUriLabel() { return Collections.emptyList(); }
		public Set<UITreeNode> getRangeTree() { return Collections.emptySet(); }
	}
	
	public class OnkiResults implements IResults {
		private Set<IResults.IResult> results = new LinkedHashSet<IResults.IResult>();
		private int size;
		private OnkiResults(List<OnkiQueryResult> onkiResults, int size) {
			for (OnkiQueryResult result : onkiResults)
				results.add(new IResults.Result(
						result.getUri().getValue(),
						result.getTitle().getValue(),
						result.getAltLabel().getValue()));
			this.size = size;
		}
        
		public int getSize() {
			return size;
		}
        
		public Iterator<IResults.IResult> iterator() {
			return results.iterator();
		}
	}
	
	public class OnkiRepository implements IRepository {
		private IOnkiQueryPortType port;
		private OnkiRepository(String ontology) {
			URL url = null;
			try {
				url = ontology.startsWith("http://") ? new URL(ontology) : 
					new URL("http://www.yso.fi/onkiwebservice/wsdl/?o=" + ontology + "&k=" + accessKey);
			} catch (MalformedURLException e) {
				log.error("",e);
			}
			
			IOnkiQuery oq = new IOnkiQuery(url,new QName("http://service.onki.seco.cs.helsinki.fi","IOnkiQuery"));
			
			port = oq.getOnkiQuery();
			((BindingProvider)port).getRequestContext().put(JAXWSProperties.CONNECT_TIMEOUT,5000);
		}
        
		public synchronized IResults search(String queryTerm, Collection<String> parentUris, Collection<String> typeUris, 
				Locale locale, int maxResults) 
		{
			OnkiQueryResults rs = port.search(queryTerm,locale.getLanguage(),maxResults,
					toArrayOfString(typeUris),
					toArrayOfString(parentUris),
					null);
			return new OnkiResults(rs.getResults().getValue().getOnkiQueryResult(),
					rs.getMetadata().getValue().getTotalHitsAmount());
		}
		private ArrayOfString toArrayOfString(Collection<String> list) {
			if (list == null || list.isEmpty()) return null;
			ArrayOfString a = new ArrayOfString();
			for (String s : list) a.getString().add(s);
			return a;
		}
		public synchronized String getLabel(String uri, Locale locale) {
			return getLabel(uri,locale.getLanguage());
		}
		public synchronized String getLabel(String uri, String lang) {
			OnkiQueryResult r = port.getLabel(uri,lang);
			String label = r.getTitle().getValue();
			return !label.contains("*") ? label : uri;
		}
		public synchronized Set<ISahaProperty> getProperties(String uri, Locale locale) {
			Set<ISahaProperty> properties = new TreeSet<ISahaProperty>();
			ArrayOfStatement statements = null;
			try {
				statements = port.getProperties(uri,null,locale.getLanguage());
			} catch(Exception e) {
				log.error(e.getMessage());
				return properties;
			}
			for (Statement s : statements.getStatement()) {
				OnkiProperty property = new OnkiProperty(s);
				if (!property.getValueLabel().isEmpty())
					properties.add(property);
			}
			return properties;
		}
		public synchronized Map<UriLabel,Set<ISahaProperty>> getPropertyMap(String uri, Locale locale) {
			Map<UriLabel,Set<ISahaProperty>> map = new TreeMap<UriLabel,Set<ISahaProperty>>();
			for (ISahaProperty property : getProperties(uri,locale)) {
				UriLabel p = new UriLabel(property.getUri(),property.getLabel());
				if (!map.containsKey(p)) map.put(p,new TreeSet<ISahaProperty>());
				map.get(p).add(property);
			}
			return map;
		}
		public synchronized Set<Entry<UriLabel,Set<ISahaProperty>>> getPropertyMapEntrySet(String uri, Locale locale) {
			return getPropertyMap(uri,locale).entrySet();
		}
	}
	
	private Logger log = Logger.getLogger(getClass());
	private String accessKey;
	
	private Map<String,OnkiRepository> onkiCache = new HashMap<String,OnkiRepository>();
	
	public OnkiWebService() {
		if (log.isDebugEnabled()) HttpTransportPipe.dump=true;
	}
	
	@Required
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	
	public synchronized OnkiRepository getOnkiRepository(String ontology) {
		if (!onkiCache.containsKey(ontology))
			onkiCache.put(ontology,new OnkiRepository(ontology));
		return onkiCache.get(ontology);
	}
	
}
