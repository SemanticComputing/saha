package fi.seco.saha3.index.category;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import fi.seco.saha3.index.PropertyRangeCache;
import fi.seco.saha3.index.ResourceIndexSearcher;

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
    
    
    
    public CategoryNode[] getCategoryNodes(String propertyUri) {
        if (caching)
        {
            if (!categories.containsKey(propertyUri))
                categories.put(propertyUri,buildNodes(propertyUri));
            return categories.get(propertyUri);
        }
        
        return buildNodes(propertyUri);
    }
    
    private CategoryNode[] buildNodes(String propertyUri) {
        Set<CategoryNode> rootNodes = new HashSet<CategoryNode>();
        Map<String,CategoryNode> nodeMap = new HashMap<String,CategoryNode>();
        
        for (String valueUri : rangeCache.getPropertyRange(propertyUri))
            nodeMap.put(valueUri,new CategoryNode(valueUri));
        
        rootNodes.addAll(nodeMap.values());
        
        for (CategoryNode node : nodeMap.values())
            for (String object : searcher.getAncestors(node.getUri()))
                if (nodeMap.containsKey(object)) {
                    nodeMap.get(object).addChild(node);
                    rootNodes.remove(node);
                }
        
        return rootNodes.toArray(new CategoryNode[rootNodes.size()]);
    }
    
}
