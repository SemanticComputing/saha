package fi.seco.saha3.web.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.index.ResourceIndex;
import fi.seco.saha3.index.category.UICategories;
import fi.seco.saha3.index.category.UICategoryNode;
import fi.seco.saha3.model.IResults.IResult;
import fi.seco.saha3.model.SahaProject;

/**
 * Controller for the HAKO config screen and HAKO itself.
 * 
 */
public class HakoJSONController extends ASahaController {

	@SuppressWarnings("unchecked")
	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response, SahaProject project, Locale locale,
			ModelAndView mav) throws Exception {
	    
		if (!project.isHakoConfig()) {
			response.setContentType("application/json;charset=UTF-8");
	        response.getWriter().write(getJSONResponseError("Error: the hako instance is not configured"));
			return null;
		}
		JSONObject result = new JSONObject();
		Map<String,List<String>> parameterMap = toModifiableMap(request.getParameterMap());
		
		parameterMap.remove("lang");
		parameterMap.remove("model");
		
		boolean sort = true;
		List<String> searchStrings = parameterMap.get(ResourceIndex.UBER_FIELD_NAME);
		if (searchStrings != null) for (String searchString : searchStrings)
			if (!searchString.isEmpty())
				sort = false; // list items by relevance
		
		UICategories categories = project.getUICategories(parameterMap,locale);
			
		Map<String,UICategoryNode> selected = new HashMap<String,UICategoryNode>();
		Map<String,UICategoryNode> allNodes = categories.getAllNodes();
		result.put("selectedCategories", Collections.emptyList());
		for (List<String> values : parameterMap.values()) {
			for (String value : values) {
				if (allNodes.containsKey(value)) {
					JSONObject tmp = new JSONObject();
					UICategoryNode uinode = allNodes.get(value);					
					selected.put(value,uinode);
					tmp.put("uri", uinode.getUri());
					tmp.put("label", uinode.getLabel());
					tmp.put("backQuery", uinode.getBackQuery());
					tmp.put("propertyUri", uinode.getPropertyUri());
					result.append("selectedCategories", tmp);
				}
			}
		}
		
		Iterator<Entry<String, SortedSet<UICategoryNode>>> it = categories.getRootNodes().entrySet().iterator();
		while (it.hasNext()) {
			JSONObject facetCategory = new JSONObject();
			Entry<String, SortedSet<UICategoryNode>> category = it.next();
			facetCategory.put("label", category.getKey());
			for (UICategoryNode node : category.getValue()) {
				JSONObject tmp = new JSONObject(); // XXX: Fix recursion so that the this object is created in the first call for buildRecursiveHierarchys
				tmp.put("uri", node.getUri());
				tmp.put("itemCount", node.getItemCount());
				tmp.put("children", buildRecursiveHierarchy(node.getChildren())); // Builds instance subclass hierarchy recursively 
				tmp.put("label", node.getLabel());
				tmp.put("backQuery", node.getBackQuery());
				tmp.put("selectQuery", node.getSelectQuery());
				facetCategory.append("facetClasses", tmp);
			}
			result.append("facets", facetCategory);
		}			
		
		List<String> terms = parameterMap.remove(ResourceIndex.UBER_FIELD_NAME);
		if (terms != null) {
			result.put("terms", terms);
		}
		else {
			result.put("terms", Collections.emptyList());
		}
		
		for (IResult ir : project.getSortedInstances(parameterMap,project.getHakoTypes(),locale,0,300,sort)) {
			JSONObject tmp = new JSONObject();
			tmp.append("uri", ir.getUri());
			tmp.append("label", ir.getLabel());
			result.append("results", tmp);
		}
		if (!result.has("results")) {
			result.put("results", Collections.emptyList());
		}
		
		response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(result.toString());
		
		return null;
	}
	
	private JSONArray buildRecursiveHierarchy(SortedSet<UICategoryNode> nodes) throws Exception {
		if (nodes.size() == 0) {
			return new JSONArray();
		}
		JSONArray result = new JSONArray();
		for (UICategoryNode n : nodes) {
			JSONObject tmp = new JSONObject();
			tmp.put("uri", n.getUri());
			tmp.put("itemCount", n.getItemCount());
			tmp.put("children", buildRecursiveHierarchy(n.getChildren()));
			tmp.put("label", n.getLabel());
			tmp.put("backQuery", n.getBackQuery());
			tmp.put("selectQuery", n.getSelectQuery());
			result.put(tmp);
		}
		return result;
	}
	private String getJSONResponseError(String description) {
		return "{\"error\": \""+ description +"\"}";
	}
	private Map<String,List<String>> toModifiableMap(Map<String,String[]> map) {
		Map<String,List<String>> modifiable = new LinkedHashMap<String,List<String>>();
		
		for (Map.Entry<String,String[]> entry : map.entrySet()) {
			List<String> list = new ArrayList<String>();
			for (String value : entry.getValue()) 
				list.add(value);
			modifiable.put(entry.getKey(),list);
		}
		
		return modifiable;
	}
	
//	@SuppressWarnings("unchecked")
//	@Override
//	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		String servletPath = request.getServletPath();
//		String model = parseModelName(servletPath);
//		Locale locale = RequestContextUtils.getLocale(request);
//		SahaProject project = sahaProjectRegistry.getSahaProject(model);
//		JSONArray result = new JSONArray();
//		Map<String,List<String>> parameterMap = toModifiableMap(request.getParameterMap());
//		
//		parameterMap.remove("lang");
//		parameterMap.remove("model");
//		
//		boolean sort = true;
//		List<String> searchStrings = parameterMap.get(ResourceIndex.UBER_FIELD_NAME);
//		if (searchStrings != null) { 
//			for (String searchString : searchStrings) {		
//				if (!searchString.isEmpty()) {
//					sort = false; // list items by relevance
//				}
//			}
//		}
//		
//		UICategories categories = project.getUICategories(parameterMap,locale);
//		
//		mav.addObject("result",project.getSortedInstances(parameterMap,project.getHakoTypes(),locale,0,300,sort));
//		mav.addObject("categories",categories.getRootNodes());
//		
//		Map<String,UICategoryNode> selected = new HashMap<String,UICategoryNode>();
//		Map<String,UICategoryNode> allNodes = categories.getAllNodes();
//		for (List<String> values : parameterMap.values()) {
//			for (String value : values) {
//				if (allNodes.containsKey(value)) {
//					selected.put(value,allNodes.get(value));
//				}
//			}
//		}
//		mav.addObject("selected",selected);
//		
//		List<String> terms = parameterMap.remove(ResourceIndex.UBER_FIELD_NAME);
//		if (terms != null) {
//			mav.addObject("terms",terms);
//		}
//		else {
//			mav.addObject("terms",Collections.emptyList());
//		}
//		
//		mav.addObject("parameterMap",parameterMap);  // add all parameters (except previously removed labels)
//		
//		response.setContentType("text/plain;charset=UTF-8");
//        response.getWriter().write(result.toString());
//		return null;
//	}
//	public static String parseModelName(String servletPath) {
//		servletPath = servletPath.substring(0, servletPath.lastIndexOf('/'));
//		return servletPath.substring(servletPath.lastIndexOf('/') + 1, servletPath.length());
//	}
//	private Map<String, List<String>> toModifiableMap(Map<String, String[]> map) {
//		Map<String, List<String>> modifiable = new LinkedHashMap<String, List<String>>();
//
//		for (Map.Entry<String, String[]> entry : map.entrySet()) {
//			List<String> list = new ArrayList<String>();
//			for (String value : entry.getValue())
//				list.add(value);
//			modifiable.put(entry.getKey(), list);
//		}
//
//		return modifiable;
//	}
	
}
