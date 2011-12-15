package fi.seco.saha3.model;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.rdf.model.Model;

import fi.seco.saha3.factory.ReferenceTracker;
import fi.seco.saha3.factory.ResourceFactory;
import fi.seco.saha3.factory.TreeBuilder;
import fi.seco.saha3.factory.TreeBuilder.UITreeNode;
import fi.seco.saha3.index.PropertyRangeCache;
import fi.seco.saha3.index.ResourceIndexSearcher;
import fi.seco.saha3.index.category.UICategories;
import fi.seco.saha3.index.category.UICategoryBuilder;
import fi.seco.saha3.model.configuration.ConfigService;
import fi.seco.saha3.model.configuration.HakoConfig;

/**
 * A single SAHA project. Contains the data of the model (in both the RDF
 * graph and Lucene index) and the relevant configuration, both for SAHA and
 * HAKO.
 * 
 */
public class SahaProject implements IRepository, IModelEditor {
	
    private ResourceIndexSearcher searcher;
    private TreeBuilder treeBuilder;
    private ResourceFactory resourceFactory;
    private ReferenceTracker referenceTracker;
    private PropertyRangeCache propertyRangeCache;
    private ModelEditor modelEditor;
    private ConfigService config;
    private UICategoryBuilder categoryBuilder;
    
    @Autowired
    public void setConfig(ConfigService config) {
		this.config = config;
	}
    
    @Autowired
    public void setSearcher(ResourceIndexSearcher searcher) {
		this.searcher = searcher;
	}
    
    @Autowired
    public void setPropertyRangeCache(PropertyRangeCache propertyRangeCache) {
		this.propertyRangeCache = propertyRangeCache;
	}
    
    @Autowired
    public void setModelEditor(ModelEditor modelEditor) {
		this.modelEditor = modelEditor;
	}
    
    @Autowired
    public void setCategoryBuilder(UICategoryBuilder categoryBuilder) {
		this.categoryBuilder = categoryBuilder;
	}
    
    @Autowired
    public void setTreeBuilder(TreeBuilder treeBuilder) {
		this.treeBuilder = treeBuilder;
	}
    
    @Autowired
    public void setReferenceTracker(ReferenceTracker referenceTracker) {
		this.referenceTracker = referenceTracker;
	}
    
    @Autowired
    public void setResourceFactory(ResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}

    
    public Set<UITreeNode> getLocalizedRootClasses(Locale locale) {
        return treeBuilder.getLocalizedRootNodes(locale);
    }
    
    public ISahaResource getResource(String uri, Locale locale) {
        return resourceFactory.getResource(uri,locale);
    }
    
    public List<ISahaResource> getPropertyMissingResources(String propertyUri, Locale locale) {
    	return referenceTracker.getPropertyMissingResources(propertyUri,locale);
    }
    
    public UICategories getUICategories(Map<String,List<String>> queryMap, Locale locale) {
    	if (!isHakoConfig()) return new UICategories();
    	
    	HakoConfig hakoConfig = this.config.getHakoConfig();
    	return categoryBuilder.getUICategories(hakoConfig.getTypes(),hakoConfig.getProperties(),queryMap,locale);
    }
    
    // for debug
    public UICategories getUICategories(List<String> types, List<String> properties, Locale locale) {
    	return categoryBuilder.getUICategories(types,properties,new HashMap<String,List<String>>(),locale);
    }
    
    public IResults getSortedInstances(Map<String,List<String>> queryMap, List<String> types, Locale locale, int from, int to, boolean sort) {
        return searcher.search(queryMap,types,locale,from,to,sort);
    }
    
    // for debug
    public IResults getSortedInstances(Query query, Locale locale, int from, int to) {
    	return searcher.search(query,locale,from,to,true);
    }
    
    public IResults getSortedInstances(String label, String type, Locale locale, int from, int to) {
        return searcher.searchLabel(label,type,locale,from,to,true);
    }
    
    // parent restrictions not supported
    public IResults search(String query, Collection<String> parents, Collection<String> types, Locale locale, int max) {
        return searcher.search(query,types,locale,0,max);
    }
    
    public IResults search(String query, List<String> types, Locale locale, int max) {
        return searcher.search(query,types,locale,0,max);
    }
	
	public ConfigService getConfig() {
		return config;
	}
	
	public void reindex()
    {
        this.searcher.reindex();
    }
	
	// HAKO methods
	
    public boolean isHakoConfig() {
    	return config.getHakoConfig() != null;
    }
    
    public void setHakoConfig(List<String> types, List<String> properties) {
    	HakoConfig hakoConfig = new HakoConfig();
    	hakoConfig.setTypes(types);
    	hakoConfig.setProperties(properties);
    	
    	config.setHakoConfig(hakoConfig);
    }
    
	public void destroyHako() {
		if (config.getHakoConfig() == null) propertyRangeCache.clear();
		config.setHakoConfig(null);
	}
    
    public HakoConfig getHakoConfig() {
    	if (!isHakoConfig()) config.setHakoConfig(new HakoConfig());
    	return config.getHakoConfig();
    }
    
    public List<String> getHakoTypes() {
    	if (!isHakoConfig()) return Collections.emptyList();
    	return config.getHakoConfig().getTypes();
    }
    
	// delegate methods for ModelEditor
	
    public boolean readModel(InputStream in, String lang) {
		return modelEditor.readModel(in, lang);
	}
    
	public UriLabel addLiteralProperty(String s, String p, String l, String lang) {
		return modelEditor.addLiteralProperty(s, p, l, lang);
	}

	public UriLabel addLiteralProperty(String s, String p, String l) {
		return modelEditor.addLiteralProperty(s, p, l);
	}

	public boolean addModel(Model m) {
		return modelEditor.addModel(m);
	}

	public UriLabel addObjectProperty(String s, String p, String o, Locale locale) {
		return modelEditor.addObjectProperty(s, p, o, locale);
	}

	public boolean clear() {
		return modelEditor.clear();
	}

	public String createResource(String uri, String type, String label) {
		return modelEditor.createResource(uri, type, label);
	}

	public String createResource(String type, String label) {
		return modelEditor.createResource(type, label);
	}

	public boolean removeLiteralProperty(String s, String p, String valueShaHex) {
		return modelEditor.removeLiteralProperty(s, p, valueShaHex);
	}

	public boolean removeObjectProperty(String s, String p, String o) {
		return modelEditor.removeObjectProperty(s, p, o);
	}

	public boolean removeProperty(String s, String p) {
		return modelEditor.removeProperty(s, p);
	}

	public boolean removeResource(String uri) {
		return modelEditor.removeResource(uri);
	}
	
	public void reindexFromModel()
	{
	    this.modelEditor.reindexFromModel();
	}

    public boolean setMapProperty(String s, String fc, String value)
    {
        return modelEditor.setMapProperty(s, fc, value);
    }

}
