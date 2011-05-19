package fi.seco.saha3.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.index.ResourceIndexSearcher;
import fi.seco.saha3.model.ISahaProperty;
import fi.seco.saha3.model.UriLabel;
import fi.seco.saha3.model.configuration.ConfigService;
import fi.seco.saha3.model.configuration.PropertyConfig;
import fi.seco.semweb.util.JenaHelper;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

public class PropertyFactory {

	public class SahaProperty implements ISahaProperty {
		private String uri;
		private RDFNode object;
		private Locale locale;
		private String valueLabel;
		public SahaProperty(String uri, RDFNode object, Locale locale) {
			this.uri = uri;
			this.object = object;
			this.locale = locale;
		}
        
		public String getUri() {
			return uri;
		}
        
		public String getLabel() {
			return searcher.getLabel(getUri(),locale);
		}
        
		public String getComment() {
			Resource r = model.createResource(uri);
			String comment = JenaHelper.getLangProperty(r,RDFS.comment,locale.getLanguage());
			if (comment == null) comment = JenaHelper.getPropertyAsString(r,RDFS.comment);
			if (comment == null) comment = "";
			return comment;
		}
        
		public boolean isLiteral() {
			return object.isLiteral();
		}
		private boolean isUriResource() {
			return object.isURIResource();
		}
        
		public String getValueUri() {
			if (isUriResource()) return ((Resource)object).getURI();
			return "";
		}
        
		public String getValueLang() {
			if (isLiteral()) return ((Literal)object).getLanguage();
			return locale.getLanguage();
		}
        
		public String getValueLabel() {
			if (valueLabel == null) valueLabel = fetchValueLabel();
			return valueLabel;
		}
		private String fetchValueLabel() {
			if (isLiteral()) return ((Literal)object).getString();
			if (isUriResource()) return searcher.getLabel(getValueUri(),locale);
			return object.toString();
		}
        
		public String getValueShaHex() {
			if (isLiteral() && object != null)
				return DigestUtils.shaHex(object.asLiteral().toString());
			
			return DigestUtils.shaHex(getValueLabel());
		}
        
		public String getValueTypeUri() {
			return searcher.getTypeUri(getValueUri());
		}
        
		public String getValueTypeLabel() {
			return searcher.getLabel(getValueTypeUri(),locale);
		}
        
		public String getValueDatatypeUri() {
			if (isLiteral()) {
				String datatypeUri = ((Literal)object).getDatatypeURI();
				if (datatypeUri != null) return datatypeUri;
			}
			return "";
		}
        
		public int compareTo(ISahaProperty o) {
			return compareSahaProperties(this,o);
		}
        
		public Set<String> getRange() {
			return getPropertyRangeFromModel(getUri(),true);
		}
        
		public List<UriLabel> getRangeUriLabel() {
			List<UriLabel> uriLabels = new ArrayList<UriLabel>();
			for (String rangeUri : getRange())
				uriLabels.add(new UriLabel(rangeUri,locale,searcher.getLabel(rangeUri,locale)));
			Collections.sort(uriLabels);
			return uriLabels;
		}
        
		public Set<TreeBuilder.UITreeNode> getRangeTree() {
			return treeBuilder.getLocalizedTreeNodes(getPropertyRangeFromModel(getUri(),false),locale);
		}
        
		public PropertyConfig getConfig() {
			return config.getPropertyConfig(getUri());
		}
	}
	
	public final static Comparator<ISahaProperty> getPropertyComparator() {
		return new Comparator<ISahaProperty>() {
            
			public int compare(ISahaProperty o1, ISahaProperty o2) {
				return compareSahaProperties(o1,o2);
			}
		};
	}
	
	private final static int compareSahaProperties(ISahaProperty o1, ISahaProperty o2) {
		int c = String.CASE_INSENSITIVE_ORDER.compare(o1.getValueLabel(),o2.getValueLabel());
		if (c == 0) c = String.CASE_INSENSITIVE_ORDER.compare(o1.getValueShaHex(),o2.getValueShaHex());
		return c != 0 ? c : o1.getValueUri().compareTo(o2.getValueUri());
	}
	
	private Logger log = Logger.getLogger(getClass());
	
	private Model model;
	private ResourceIndexSearcher searcher;
	private TreeBuilder treeBuilder;
	private ConfigService config;
	
	@Autowired
	public void setModel(Model model) {
		this.model = model;
	}

	@Autowired
	public void setSearcher(ResourceIndexSearcher searcher) {
		this.searcher = searcher;
	}

	@Autowired
	public void setTreeBuilder(TreeBuilder treeBuilder) {
		this.treeBuilder = treeBuilder;
	}

	@Autowired
	public void setConfig(ConfigService config) {
		this.config = config;
	}
	
	public List<ISahaProperty> getProperties(String uri, Locale locale) {
		List<ISahaProperty> list = new ArrayList<ISahaProperty>();
		for (Statement s : new IteratorToIIterableIterator<Statement>(model.createResource(uri).listProperties()))
			list.add(getProperty(s,locale));
		return list;
	}
	
	private ISahaProperty getProperty(Statement s, Locale locale) {
		return new SahaProperty(s.getPredicate().getURI(),s.getObject(),locale);
	}

	public List<ISahaProperty> getInverseProperties(String uri, Locale locale) {
		List<ISahaProperty> list = new ArrayList<ISahaProperty>();
		for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null,null,model.createResource(uri))))
			list.add(getInverseProperty(s,locale));
		return list;
	}
	
	private ISahaProperty getInverseProperty(final Statement s, final Locale locale) {
		return new SahaProperty(s.getPredicate().getURI(),s.getSubject(),locale);
	}

	public Set<ISahaProperty> getAllProperties(String uri, String[] types, Locale locale) {
		Set<ISahaProperty> set = getDomainProperties(types,locale);
		set.addAll(getProperties(uri,locale));
		return set;
	}
	
	public Set<ISahaProperty> getDomainProperties(String[] types, Locale locale) {
		Set<ISahaProperty> set = new HashSet<ISahaProperty>();
		for (String type : types)
			for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null,RDFS.domain,model.createResource(type))))			
				set.add(getEmptyProperty(s.getSubject().getURI(),locale));			
		return set;
	}
	
	private ISahaProperty getEmptyProperty(final String uri, final Locale locale) {
		return new SahaProperty(uri,null,locale) {
			@Override
			public String getValueUri() {
				return "";
			}
			@Override
			public String getValueLang() {
				return "";
			}
			@Override
			public String getValueLabel() {
				return "";
			}
			@Override
			public String getValueDatatypeUri() {
				return "";
			}
			@Override
			public boolean isLiteral() {
				for (String type : searcher.getTypeUris(uri)) {
					if (type.equals(OWL.DatatypeProperty.getURI())) return true;
					if (type.equals(OWL.ObjectProperty.getURI())) return false;
				}
				log.debug("isLiteral is ambiguous for property: " + uri);
				return false;
			}
		};
	}
	
	private Set<String> getPropertyRangeFromModel(String propertyUri, boolean transitive) {
		Property property = model.createProperty(propertyUri);
		Set<String> range = new HashSet<String>();
		for (Statement s : new IteratorToIIterableIterator<Statement>(property.listProperties(RDFS.range)))
			if (s.getObject().isURIResource() && !s.getObject().equals(OWL.Thing)) {
				String uri = ((Resource)s.getObject()).getURI();
				range.add(uri);
				if (transitive) 
					range.addAll(searcher.getAllDescendants(uri));
			}
		return range;
	}

}
