package fi.seco.saha3.index;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import fi.seco.semweb.util.LRUCache;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

public class SubjectObjectCache {
    
    private Model model;
    private Set<Property> hierarchyProperties;
    
    private Map<String,String[]> objectCache = new LRUCache<String,String[]>(10000);
    private Map<String,String[]> subjectCache = new LRUCache<String,String[]>(10000);
    
    public SubjectObjectCache(Model model, String propertyUri) {
        this.model = model;
        this.hierarchyProperties = new HashSet<Property>();
        this.hierarchyProperties.add(ResourceFactory.createProperty(propertyUri));
    }
    
    public SubjectObjectCache(Model model, Set<Property> properties) {
        this.model = model;
        this.hierarchyProperties = properties;
    }
    
    public SubjectObjectCache(Model model, Property... properties) {
        this.model = model;
        this.hierarchyProperties = new HashSet<Property>();
        for (Property p : properties)
            this.hierarchyProperties.add(p);
    }
    
    public Set<String[]> getAllTransitiveObjectPaths(String resourceUri) {
        Set<String[]> allPaths = new HashSet<String[]>();
        getAllTransitiveObjectPaths(resourceUri,new LinkedList<String>(),allPaths);
        return allPaths;
    }
    
    @SuppressWarnings("unchecked")
    private void getAllTransitiveObjectPaths(String resourceUri, LinkedList<String> path, Set<String[]> resultSet) {
        path.addFirst(resourceUri);
        if (hasObjects(resourceUri)) {
            for (String parent : getObjects(resourceUri))
                if (!path.contains(parent)) 
                	getAllTransitiveObjectPaths(parent,(LinkedList<String>)path.clone(),resultSet);
        } else
            resultSet.add(path.toArray(new String[path.size()]));
    }
    
    public Set<String> getTransitiveObjects(String resourceUri) {
        Set<String> objects = new HashSet<String>();
        getTransitiveObjects(resourceUri,objects);
        return objects;
    }
    
    private void getTransitiveObjects(String resourceUri, Set<String> objects) {
        objects.add(resourceUri);
        for (String parent : getObjects(resourceUri))
            if (!objects.contains(parent)) getTransitiveObjects(parent,objects);
    }
    
    public boolean hasObjects(String resourceUri) {
        return getObjects(resourceUri).length > 0;
    }
    
    public String[] getObjects(String resourceUri) {
        if (!objectCache.containsKey(resourceUri)) {
        	Set<String> objects = new HashSet<String>();
            for (Property p : hierarchyProperties) {
                for (Statement s : new IteratorToIIterableIterator<Statement>(
                		model.listStatements(ResourceFactory.createResource(resourceUri),p,(RDFNode)null))) 
                {
                    if (s.getObject().isURIResource())                        
                        objects.add(s.getResource().getURI());
                }
            }
            objectCache.put(resourceUri,objects.toArray(new String[objects.size()]));
        }
        return objectCache.get(resourceUri);
    }
    
    public Set<String> getTransitiveSubjects(String resourceUri) {
        Set<String> subjects = new HashSet<String>();
        getTransitiveSubjects(resourceUri,subjects);
        return subjects;
    }
    
    private void getTransitiveSubjects(String resourceUri, Set<String> subjects) {
    	subjects.add(resourceUri);
        for (String parent : getSubjects(resourceUri))
            if (!subjects.contains(parent)) getTransitiveSubjects(parent,subjects);
    }
    
    public boolean hasSubjects(String resourceUri) {
        return getSubjects(resourceUri).length > 0;
    }
    
    public String[] getSubjects(String resourceUri) {
        if (!subjectCache.containsKey(resourceUri)) {
            Set<String> subjects = new HashSet<String>();
            for (Property p : hierarchyProperties) {
                for (Statement s : new IteratorToIIterableIterator<Statement>(
                		model.listStatements(null,p,ResourceFactory.createResource(resourceUri)))) 
                {
                    if (s.getSubject().isURIResource())                        
                        subjects.add(s.getSubject().getURI());
                }
            }
            subjectCache.put(resourceUri,subjects.toArray(new String[subjects.size()]));
        }
        return subjectCache.get(resourceUri);
    }
    
    public void clear() {
    	objectCache.clear();
    	subjectCache.clear();
    }
    
}
