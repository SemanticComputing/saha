package fi.seco.saha3.model;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.index.ResourceIndex;
import fi.seco.saha3.index.ResourceIndexSearcher;
import fi.seco.saha3.infrastructure.OnkiWebService;
import fi.seco.saha3.infrastructure.OnkiWebService.OnkiRepository;
import fi.seco.saha3.model.configuration.ConfigService;
import fi.seco.saha3.model.configuration.RepositoryConfig;
import fi.seco.saha3.util.SAHA3;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

/**
 * Class used for write operations performed in SAHA. Keeps both the RDF
 * data model and the Lucene index consistent with each other.
 * 
 */
public class ModelEditor implements IModelEditor {
	
	private Logger log = Logger.getLogger(getClass());
	
    private Model model;
    private ConfigService config;
    private ResourceIndex index;
    private ResourceIndexSearcher searcher;
    private OnkiWebService onkiWebService;
    private boolean allowEditing = true;

	private Set<String> knownLanguages = new HashSet<String>(Arrays.asList(Locale.getISOLanguages()));

	public static final String WGS84_LAT = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
	public static final String WGS84_LONG = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
	public static final String POLYGON_URI = "http://www.yso.fi/onto/sapo/hasPolygon";
	public static final String ROUTE_URI = "http://www.yso.fi/onto/sapo/hasRoute";
	
	@Required
	public void setModel(Model model) {
		this.model = model;
	}

	@Required
	public void setConfig(ConfigService config) {
		this.config = config;
	}

	@Required
	public void setIndex(ResourceIndex index) {
		this.index = index;
	}

	@Required
	public void setSearcher(ResourceIndexSearcher searcher) {
		this.searcher = searcher;
	}

	@Required
	public void setOnkiWebService(OnkiWebService onkiWebService) {
		this.onkiWebService = onkiWebService;
	}

	public void setAllowEditing(boolean allowEditing) {
		this.allowEditing = allowEditing;
	}

	public synchronized UriLabel addObjectProperty(String s, String p, String o, Locale locale) {
        if (allowEditing) {
        	checkAndAddExternalResources(p,o,locale);
            model.add(model.createResource(s),model.createProperty(p),model.createResource(o));
            index.indexResource(s);
            updateModifiedTimestamp(s);
        }
        return new UriLabel(o,searcher.getLabel(o,locale));
    }
    
    private void checkAndAddExternalResources(String propertyUri, String objectUri, Locale locale) {
    	if (onkiWebService != null && !model.containsResource(model.createResource(objectUri))) {
    		for (RepositoryConfig repositoryConfig : 
    			config.getPropertyConfig(propertyUri).getRepositoryConfigs()) 
    		{
    			OnkiRepository onkiRepository = 
    				onkiWebService.getOnkiRepository(repositoryConfig.getSourceName());
    			createResource(objectUri,null,null);
    			
    			for (ISahaProperty property : onkiRepository.getProperties(objectUri, locale))
    			{
    			    if ((property.getUri().equals(WGS84_LAT) || property.getUri().equals(WGS84_LONG)
    			        || property.getUri().equals(POLYGON_URI) || property.getUri().equals(ROUTE_URI))
    			        && property.isLiteral())
    			    {
    			        addLiteralProperty(objectUri, property.getUri(), property.getValueLabel());
    			    }
    			}
    			
    			Set<Locale> locales = new HashSet<Locale>(index.getAcceptedLocales());
    			locales.add(locale);
    			for (Locale l : locales) {
					String label = onkiRepository.getLabel(objectUri,l);
					if (!label.equals(objectUri))
						addLiteralProperty(objectUri,config.getLabelProperty(),label,l.getLanguage());
    			}
    		}
    	}
	}

	public synchronized boolean removeObjectProperty(String s, String p, String o) {
        if (allowEditing) {
            model.remove(model.createResource(s),model.createProperty(p),model.createResource(o));
            index.indexResource(s);
            updateModifiedTimestamp(s);
            return true;
        }
        return false;
    }
    
    public UriLabel addLiteralProperty(String s, String p, String l) {
        return addLiteralProperty(s,p,model.createLiteral(l));
    }
    
    public UriLabel addLiteralProperty(String s, String p, String l, String lang) {
    	lang = lang.trim();
    	return knownLanguages.contains(lang) ? 
    			addLiteralProperty(s,p,model.createLiteral(l,lang)) : addLiteralProperty(s,p,model.createLiteral(l));
    }
    
    private UriLabel addLiteralProperty(String s, String p, Literal l) {
        if (allowEditing) {
        	Resource subject = model.createResource(s);
        	Property predicate = model.createProperty(p);
            model.add(subject,predicate,l);
            index.indexResource(s);
            updateAffectedResources(predicate,subject);
            updateModifiedTimestamp(s);
        }
        return new UriLabel("",l.getLanguage(),l.getString());
    }
    
    public synchronized boolean addModel(Model m) {
    	if (allowEditing) {
    		clearSubClassOfCache();
    		Set<String> modified = new HashSet<String>();
    		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements())) {
    			model.add(s);
                modified.add(s.getSubject().getURI());
                modified.addAll(getAffecterdResources(s.getPredicate(),s.getSubject()));
    		}
    		index.indexResource(modified);
    		return true;
    	}
    	return false;
    }
    
    public synchronized boolean readModel(InputStream in, String lang) {
    	if (allowEditing) {    		
    		model.read(in,"",lang);
    		
    		// Re-add internal configuration from model and remove the handled triples
    		config.addConfigFromModel(model);
    		
    		clearSubClassOfCache();
    		index.clear();
    		index.reindex();
    		return true;
    	}
    	return false;
    }
    
    public synchronized boolean removeProperty(String s, String p) {
        if (allowEditing) {
        	Resource subject = model.createResource(s);
        	Property predicate = model.createProperty(p);
            model.removeAll(subject,predicate,null);
            index.indexResource(s);
            updateAffectedResources(predicate,subject);
            updateModifiedTimestamp(s);
            return true;
        }
        return false;
    }
    
    public boolean removeLiteralProperty(String s, String p, String valueShaHex) {
        for (Statement statement : new IteratorToIIterableIterator<Statement>(model.createResource(s).listProperties(model.createProperty(p))))
            if (statement.getObject().isLiteral())
                if (DigestUtils.shaHex(statement.getObject().toString()).equals(valueShaHex))
                    return removeLiteralProperty(s,p,statement.getLiteral());
        log.warn("Failed to remove literal property. s: " + s + " p: " + p + " valueShaHex: " + valueShaHex);
        return false;
    }
    
    private synchronized boolean removeLiteralProperty(String s, String p, Literal l) {
        if (allowEditing) {
        	Resource subject = model.createResource(s);
        	Property predicate = model.createProperty(p);
            model.remove(subject,predicate,l);
            index.indexResource(s);
            updateAffectedResources(predicate,subject);
            updateModifiedTimestamp(s);
            return true;
        }
        return false;
    }
    
    private void updateAffectedResources(Property p, Resource r) {
        index.indexResource(getAffecterdResources(p,r));
        // if subClassOf was modified, clear ancestor and tree caches
        if (p.equals(RDFS.subClassOf)) 
        	clearSubClassOfCache();
    }
    
    private Set<String> getAffecterdResources(Property p, Resource r) { 
    	Set<String> affected = new HashSet<String>();
    	// if label was modified, return referencing resources
	    if (index.getAcceptedLabels().contains(p))
	    	for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null,null,r)))
	    		if (s.getSubject().isURIResource())
	    			affected.add(s.getSubject().getURI());
	    return affected;
    }
    
    public String createResource(String type, String label) {
        return createResource(generateRandomUri(config.getNamespace()),type,label);
    }
    
    public synchronized String createResource(String uri, String type, String label) {
        if (allowEditing) {
            Resource resource = model.createResource(uri);
            if (type != null && !type.isEmpty()) 
            	model.add(resource,RDF.type,model.createResource(type));
            if (label != null && !label.isEmpty()) 
            	model.add(resource,model.createProperty(config.getLabelProperty()),label);
            index.indexResource(uri);
            addCreatedTimestamp(uri);
        }
        return uri;
    }
    
    public static String generateRandomUri(String namespace) {
        return namespace + "u" + UUID.randomUUID().toString();
    }
    
    public synchronized boolean removeResource(String uri) {
        if (allowEditing) {
            List<Statement> trash = new ArrayList<Statement>();
            Set<String> modified = new HashSet<String>();
            for (Statement s : new IteratorToIIterableIterator<Statement>(model.createResource(uri).listProperties()))
                trash.add(s);
            for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null,null,model.createResource(uri)))) {
                if (s.getSubject().isURIResource())
                    modified.add(s.getSubject().getURI());
                trash.add(s);
            }
            model.remove(trash);
            index.removeResource(uri);
            for (String m : modified) 
            	index.indexResource(m);
            return true;
        }
        return false;
    }
    
    public boolean setMapProperty(String s, String fc, String value)
    {
        boolean success = false;
        
        removeProperty(s, WGS84_LAT);
        removeProperty(s, WGS84_LONG);
        removeProperty(s, POLYGON_URI);
        removeProperty(s, ROUTE_URI);
        
        // Remove existing
        if (fc == null || value == null || fc.isEmpty() || value.isEmpty())
            return true;
        
        if (fc.equals("singlepoint"))
        {
            String[] parts = value.split(","); 
            addLiteralProperty(s, WGS84_LAT, parts[0]);
            addLiteralProperty(s, WGS84_LONG, parts[1]);
            success = true;
        }
        else if (fc.equals("polygon"))
        {
            addLiteralProperty(s, POLYGON_URI, value);
            success = true;
        }
        else if (fc.equals("route"))
        {
            addLiteralProperty(s, ROUTE_URI, value);
            success = true;
        }
        
        return success;
    }
    
    private void addCreatedTimestamp(String uri) {
    	model.add(model.createLiteralStatement(
    			model.createResource(uri),
    			SAHA3.dateCreated,
    			model.createTypedLiteral(Calendar.getInstance())));
    }
    
    private void updateModifiedTimestamp(String uri) {
    	Resource r = model.createResource(uri);
    	model.removeAll(r,SAHA3.dateModified,null);
    	model.add(r,SAHA3.dateModified,model.createTypedLiteral(Calendar.getInstance()));
    }
    
    private void clearSubClassOfCache() {
    	index.clearSubClassOfCache();
    }
    
	public synchronized boolean clear() {
		if (allowEditing) {
			model.removeAll();
			index.clear();
			index.reindex(); // init empty index
			return true;
		}
		return false;
	}

    public boolean reindexFromModel()
    {
        if (allowEditing) {
            index.clear();
            index.reindex();
            return true;
        }
        return false;
    }
}
