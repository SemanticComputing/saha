package fi.seco.saha3.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;

import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

/**
 * A class for caching the ranges of properties. 
 * 
 */
public class PropertyRangeCache {
    
    private boolean caching = true;
    
    private Model model;
    private Map<String,String[]> propertyRangeCache = new HashMap<String,String[]>();
    
    @Autowired
    public void setModel(Model model) {
        this.model = model;
    }
    
    @Required
    public void setCaching(boolean caching) {
        this.caching = caching;
    }
    
    public String[] getPropertyRange(String propertyUri) {
        if (caching)
        {
            if (!propertyRangeCache.containsKey(propertyUri))
                propertyRangeCache.put(propertyUri,enumeratePropertyValues(propertyUri));
            return propertyRangeCache.get(propertyUri);
        }
        
        return enumeratePropertyValues(propertyUri);
    }
    
    private String[] enumeratePropertyValues(String propertyUri) {
        Set<String> values = new HashSet<String>();
        for (Statement s : new IteratorToIIterableIterator<Statement>(
            model.listStatements(null,model.createProperty(propertyUri),(RDFNode)null))) 
        {
            if (s.getObject().isURIResource())
                values.add(s.getResource().getURI());
        }
        return values.toArray(new String[values.size()]);
    }
    
    public void clear() {
        propertyRangeCache.clear();
    }
    
}
