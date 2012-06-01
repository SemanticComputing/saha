package fi.seco.saha3.index.category;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import fi.seco.saha3.index.PropertyRangeCache;
import fi.seco.saha3.index.ResourceIndexSearcher;

/**
 * A builder for the categories on HAKO, i.e., the different facets with
 * which the user can constrain the search based on object property values.
 * Contains the actual data model information - for rendering in the interface,
 * a builder is wrapped in <code>UICategoryBuilder</code>.
 * 
 */
public class CategoryBuilder {
    
    private boolean caching = true;
    
    private ResourceIndexSearcher searcher;
    private PropertyRangeCache rangeCache;
    
    // root categories, keys are properties
    private Map<String,CategoryNode[]> categories = new HashMap<String,CategoryNode[]>();
    
    @Autowired
    public void setSearcher(ResourceIndexSearcher searcher) {
        this.searcher = searcher;
    }
    
    @Autowired
    public void setRangeCache(PropertyRangeCache rangeCache) {
        this.rangeCache = rangeCache;
    }
    
    @Required
    public void setCaching(boolean caching) {
        this.caching = caching;
    }
    
    /**
     * A caching way to query a single category for a specific property URI.
     * 
     * @param propertyUri The URI of the facet
     * @return An array containing the root elements of the facet (which in
     * turn contain child elements)
     */
    public CategoryNode[] getCategoryNodes(String propertyUri) {
        if (caching)
        {
            if (!categories.containsKey(propertyUri))
                categories.put(propertyUri,buildNodes(propertyUri));
            return categories.get(propertyUri);
        }
        
        return buildNodes(propertyUri);
    }
    
    /**
     * Builds the actual category trees for the property based on the
     * <i>class hierarchy of the values that the triples using this property
     * have</i>, denoted by <code>rdfs:subClassOf</code> (hard-coded in
     * <code>fi.seco.saha3.index.ResourceIndex</code>).
     * 
     * @param propertyUri The URI of the facet
     * @return An array containing the root elements of the facet (which in
     * turn contain child elements)
     */
    private CategoryNode[] buildNodes(String propertyUri) {
        Set<CategoryNode> rootNodes = new HashSet<CategoryNode>();
        Map<String,CategoryNode> nodeMap = new HashMap<String,CategoryNode>();
        
        for (String valueUri : rangeCache.getPropertyRange(propertyUri))
            nodeMap.put(valueUri,new CategoryNode(valueUri));
        for (String valueUri : rangeCache.getPropertyRange(propertyUri))
            for (String object : searcher.getAllAncestors(valueUri))
                nodeMap.put(object, new CategoryNode(object));
        
        rootNodes.addAll(nodeMap.values());
        
        for (CategoryNode node : nodeMap.values())
            for (String object : searcher.getAncestors(node.getUri()))
            {
                if (nodeMap.containsKey(object)) {
                    nodeMap.get(object).addChild(node);
                    rootNodes.remove(node);
                }
            }
        return rootNodes.toArray(new CategoryNode[rootNodes.size()]);
    }
    
}
