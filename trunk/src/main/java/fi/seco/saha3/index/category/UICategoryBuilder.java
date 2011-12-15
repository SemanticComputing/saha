package fi.seco.saha3.index.category;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.TermQuery;
import org.springframework.beans.factory.annotation.Required;

import fi.seco.saha3.index.QueryParser;
import fi.seco.saha3.index.ResourceIndex;
import fi.seco.saha3.index.ResourceIndexSearcher;

/**
 * A builder that gives all needed user interface information about
 * HAKO categories (facets). This information is combined from the two
 * underlying data sources - the raw RDF graph model (through 
 * <code>CategoryBuilder</code>) and the lucene index (for efficient text 
 * and amount queries)
 * 
 */
public class UICategoryBuilder {
	
	private CategoryBuilder categoryBuilder;
	private ResourceIndexSearcher searcher;
	
	@Required
	public void setCategoryBuilder(CategoryBuilder categoryBuilder) {
		this.categoryBuilder = categoryBuilder;
	}
	
	@Required
	public void setSearcher(ResourceIndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	/**
	 * Returns a representation of the categories that has all necessary
	 * data for rendering the categories in the UI.
	 * 
	 * @param types Types of the searched target instances
	 * @param properties The category (facet) properties
	 * @param queryMap A map of constraints, mapping property URIs to their
	 * required values. If the key is <code>UBER_FIELD_NAME</code>, the values
	 * are text searches and must match to the label of the searched instances. 
	 * @param locale Preferred locale for the labels of the results
	 * @return Renderable representation of the search results
	 */
	public UICategories getUICategories(
			List<String> types, List<String> properties, Map<String,List<String>> queryMap, Locale locale) 
	{
		Filter filter = new CachingWrapperFilter(new QueryWrapperFilter(parseQuery(queryMap,types)));
		
		Map<String,SortedSet<UICategoryNode>> renderableCategory = 
			new LinkedHashMap<String,SortedSet<UICategoryNode>>();
		
		Map<String,UICategoryNode> allNodes = new HashMap<String,UICategoryNode>();
		
		for (String property : properties)
			renderableCategory.put(searcher.getLabel(property,locale),
					buildUICategories(property,categoryBuilder.getCategoryNodes(property),filter,locale,queryMap,allNodes));
		
		return new UICategories(renderableCategory,allNodes);
	}
	
	private Query parseQuery(Map<String,List<String>> queryMap, List<String> types) {
		BooleanQuery query = new BooleanQuery();
		QueryParser parser = searcher.getParser();
		
		String queryString = null;
		List<String> terms = queryMap.get(ResourceIndex.UBER_FIELD_NAME);
		if (terms != null && !terms.isEmpty())
			queryString = terms.get(0);
		
		parser.addUberQuery(query,queryString);
		parser.addTypeQuery(query,types);
		parser.addFacetQuery(query,queryMap);
		
		return query;
	}

	private SortedSet<UICategoryNode> buildUICategories(String propertyUri, CategoryNode[] values, Filter filter, 
			Locale locale, Map<String,List<String>> queryMap, Map<String,UICategoryNode> allNodes) 
	{
		SortedSet<UICategoryNode> renderableValues = new TreeSet<UICategoryNode>();
		
		for (CategoryNode value : values) {
			UICategoryNode renderableValue = buildUICategory(propertyUri,value,filter,locale,queryMap);
			renderableValue.setChildren(buildUICategories(propertyUri,value.getChildren(),filter,locale,queryMap,allNodes));
			if (renderableValue.getRecursiveItemCount() > 0)			
				renderableValues.add(renderableValue);
			
			allNodes.put(value.getUri(),renderableValue);
		}
		
		return renderableValues;
	}
	
	private UICategoryNode buildUICategory(String propertyUri, CategoryNode item, Filter filter, 
			Locale locale, Map<String,List<String>> queryMap) 
	{
		return new UICategoryNode(
				propertyUri,
				item.getUri(),
				searcher.getLabel(item.getUri(),locale),
				countResults(propertyUri,item.getUri(),filter),
				formatSelectQuery(propertyUri,item,queryMap),
				formatBackQuery(propertyUri,item,queryMap));
	}
	
	private String formatSelectQuery(String propertyUri, CategoryNode item, Map<String,List<String>> queryMap) {
		if (queryMap.isEmpty()) return e(propertyUri) + "=" + e(item.getUri());
		return toQueryString(queryMap) + "&" + e(propertyUri) + "=" + e(item.getUri());
	}
	
	private String formatBackQuery(String propertyUri, CategoryNode item, Map<String,List<String>> queryMap) {
		return toQueryStringWithoutKeyValue(queryMap,propertyUri,item.getUri());
	}
	
	private int countResults(String propertyUri, String valueUri, Filter filter) {
		return searcher.getCount(new FilteredQuery(new TermQuery(
				new Term(propertyUri+ResourceIndex.URI_FIELD,valueUri)),filter));
	}
	
	private String toQueryString(Map<String,List<String>> queryMap) {
		StringBuilder buffer = new StringBuilder();
		boolean isFirst = true;
		for (String key : queryMap.keySet()) {
			for (String value : queryMap.get(key)) {
				if (!isFirst) buffer.append("&");
				isFirst = false;
				buffer.append(e(key) + "=" + e(value));
			}
		}
		return buffer.toString();
	}

	private String toQueryStringWithoutKeyValue(Map<String,List<String>> queryMap, String notKey, String notValue) {
		StringBuilder buffer = new StringBuilder();
		boolean isFirst = true;
		for (String key : queryMap.keySet()) {
			for (String value : queryMap.get(key)) {
				if (!(key.equals(notKey) && value.equals(notValue))) {
					if (!isFirst) buffer.append("&");
					isFirst = false;
					buffer.append(e(key) + "=" + e(value));
				}
			}
		}
		return buffer.toString();
	}
	
	private String e(String s) {
		try {
			return URLEncoder.encode(s,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return s;
		}
		
	}
}
