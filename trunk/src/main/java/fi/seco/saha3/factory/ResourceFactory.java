package fi.seco.saha3.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import fi.seco.saha3.index.ResourceIndexSearcher;
import fi.seco.saha3.model.ISahaProperty;
import fi.seco.saha3.model.ISahaResource;
import fi.seco.saha3.model.UriLabel;
import fi.seco.saha3.model.configuration.ConfigService;

public class ResourceFactory {

	private ResourceIndexSearcher searcher;
	private PropertyFactory propertyFactory;
	private ConfigService config;
	
	@Autowired
	public void setSearcher(ResourceIndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	@Autowired
	public void setPropertyFactory(PropertyFactory propertyFactory) {
		this.propertyFactory = propertyFactory;
	}
	
	@Autowired
	public void setConfig(ConfigService config) {
		this.config = config;
	}

	public ISahaResource getResource(final String uri, final Locale locale) {
		return new ISahaResource() {
			private List<ISahaProperty> cachedInverseProperties;
            
			public String getUri() {
				return uri;
			}
            
			public String getLabel() {
				return searcher.getLabel(uri,locale);
			}
            
			public List<UriLabel> getTypes() {
				List<UriLabel> types = new ArrayList<UriLabel>();
				for (String typeUri : searcher.getTypeUris(uri))
					types.add(new UriLabel(typeUri,searcher.getLabel(typeUri,locale)));
				return types;
			}
            
			public List<ISahaProperty> getProperties() {
				return propertyFactory.getProperties(uri,locale);
			}
            
			public Map<UriLabel,Set<ISahaProperty>> getPropertyMap() {
				return mapToProperties(getProperties(),getPropertyOrderMap(uri));
			}
            
			public Set<Entry<UriLabel,Set<ISahaProperty>>> getPropertyMapEntrySet() {
				return getPropertyMap().entrySet();
			}
            
			public Set<ISahaProperty> getEditorProperties() {
				return propertyFactory.getAllProperties(uri,searcher.getTransitiveTypeUris(uri),locale);
			}
            
			public Map<UriLabel,Set<ISahaProperty>> getEditorPropertyMap() {
				return mapToProperties(getEditorProperties(),getPropertyOrderMap(uri));
			}
            
			public Set<Entry<UriLabel,Set<ISahaProperty>>> getEditorPropertyMapEntrySet() {
				return getEditorPropertyMap().entrySet();
			}
            
			public List<ISahaProperty> getInverseProperties() {
				if (cachedInverseProperties == null)
					cachedInverseProperties = propertyFactory.getInverseProperties(uri,locale);
				return cachedInverseProperties;
			}
            
			public Iterator<ISahaProperty> getSortedInverseProperties() {
				return new fi.seco.semweb.util.BinaryHeap<ISahaProperty>(getInverseProperties(),PropertyFactory.getPropertyComparator()).iterator();
			}
		};
	}
	
	public final static Comparator<ISahaResource> getResourceComparator() {
		return new Comparator<ISahaResource>() {
            
			public int compare(ISahaResource o1, ISahaResource o2) {
				int c = String.CASE_INSENSITIVE_ORDER.compare(o1.getLabel(),o2.getLabel());
				return c != 0 ? c : o1.getUri().compareTo(o2.getUri());
			}
		};
	}
	
	public Map<String,Integer> getPropertyOrderMap(String uri) {
		Map<String,Integer> map = getPropertyOrderMap(searcher.getTypeUris(uri));
		if (map.isEmpty()) map = getPropertyOrderMap(searcher.getTransitiveTypeUris(uri));
		return map;
	}
	
	public Map<String,Integer> getPropertyOrderMap(String[] typeUris) {
		for (String typeUri : typeUris) {
			Collection<String> propertyOrder = config.getPropertyOrder(typeUri);
			if (!propertyOrder.isEmpty())
				return buildPositionMap(propertyOrder);
		}
		return Collections.emptyMap();
	}
	
	private Map<String,Integer> buildPositionMap(Collection<String> propertyOrder) {
		Map<String,Integer> map = new HashMap<String,Integer>();
		int i=0;
		for (String poperty : propertyOrder)
			map.put(poperty,i++);
		return map;
	}
	
	private static Map<UriLabel,Set<ISahaProperty>> mapToProperties(Collection<ISahaProperty> c, Map<String,Integer> propertyOrderMap) {
		Map<UriLabel,Set<ISahaProperty>> map = new TreeMap<UriLabel,Set<ISahaProperty>>(propertyOrderOverridingComparator(propertyOrderMap));
		for (ISahaProperty property : c) {
			UriLabel p = new UriLabel(property.getUri(),property.getLabel());
			if (!map.containsKey(p)) map.put(p,new TreeSet<ISahaProperty>());
			map.get(p).add(property);
		}
		return map;
	}

	private static Comparator<UriLabel> propertyOrderOverridingComparator(final Map<String,Integer> propertyOrderMap) {
		return new Comparator<UriLabel>() {
            
			public int compare(UriLabel u0, UriLabel u1) {
				boolean firstKnown = propertyOrderMap.containsKey(u0.getUri());
				boolean secondKnown = propertyOrderMap.containsKey(u1.getUri());
				if (firstKnown && !secondKnown) return -1;
				if (!firstKnown && secondKnown) return 1;
				if (firstKnown && secondKnown)
					return propertyOrderMap.get(u0.getUri()).compareTo(propertyOrderMap.get(u1.getUri()));
				return u0.compareTo(u1);
			}
		};
	}
	
}
