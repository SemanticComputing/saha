package fi.seco.saha3.web.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openjena.atlas.logging.Log;
import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.index.ResourceIndex;
import fi.seco.saha3.index.category.UICategories;
import fi.seco.saha3.index.category.UICategoryNode;
import fi.seco.saha3.model.IResults.IResult;
import fi.seco.saha3.model.ISahaProperty;
import fi.seco.saha3.model.SahaProject;
import fi.seco.saha3.model.UriLabel;

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
		response.setContentType("application/json;charset=UTF-8");
		if (!project.isHakoConfig()) {
	        response.getWriter().write(getJSONResponseError("Error: the hako instance is not configured"));
			return null;
		} else if (request.getServletPath().contains("ahako_uri.shtml")) { //XXX: Tight coupling		
			response.getWriter().write(getTimeMapEvents(request, project, locale));
			return null;
		} else {
			logger.warn("servlet path: "+request.getServletPath());
			logger.warn("requested uri: " +request.getRequestURI());
			String pathInfo = request.getPathInfo();
			logger.warn("path info: "+pathInfo);
			JSONObject result = new JSONObject();
			Map<String,List<String>> parameterMap = toModifiableMap(request.getParameterMap());
			
			parameterMap.remove("lang");
			parameterMap.remove("model");
			
            int from = 0, to = 100;
            if (parameterMap.containsKey("from") ) 
                    from = Integer.parseInt(parameterMap.get("from").get(0));
            if (parameterMap.containsKey("to") ) 
                    to = Integer.parseInt(parameterMap.get("to").get(0));
            
            parameterMap.remove("from");
            parameterMap.remove("to");
			
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
			
			for (IResult ir : project.getSortedInstances(parameterMap,project.getHakoTypes(),locale,from,to,sort)) {
				JSONObject tmp = new JSONObject();
				Set<Entry<UriLabel, Set<ISahaProperty>>> propertyMap;
				
				try {
					propertyMap = project.getResource(ir.getUri(), locale).getPropertyMapEntrySet();
				} catch (java.util.NoSuchElementException nsee) {
					continue;
				}
				tmp.append("uri", ir.getUri());
				tmp.append("label", ir.getLabel());
				
				JSONObject tmObj = new JSONObject();
				JSONObject options = new JSONObject();
				options.put("uri", ir.getUri());
				tmObj.put("options", options);
				JSONObject tmpX = new JSONObject();
				for(Entry<UriLabel, Set<ISahaProperty>> key: propertyMap) {
					if( key.getKey().getUri().equals("http://www.w3.org/2000/01/rdf-schema#label") )  {
						for(ISahaProperty entry: key.getValue()) {
							tmObj.put("title", entry.getValueLabel() );
						}
					} else if( key.getKey().getUri().equals("http://www.w3.org/2004/02/skos/core#prefLabel") )  {
						for(ISahaProperty entry: key.getValue()) {
							tmObj.put("title", entry.getValueLabel() );
						}
					} else if ( key.getKey().getUri().equals("http://www.hatikka.fi/havainnot/date_collected") )  {
						for(ISahaProperty entry: key.getValue()) {
							tmObj.put("start", entry.getValueLabel() );					
							tmObj.put("earliestStart", entry.getValueLabel() + " 00:00" );
							tmObj.put("earliestEnd", entry.getValueLabel()  + " 23:59" );					
						}
					} else if ( key.getKey().getUri().equals("http://www.w3.org/2003/01/geo/wgs84_pos#lat") )  { 
						for(ISahaProperty entry: key.getValue()) {
							tmpX.put("latitude", entry.getValueLabel() );
						}
						
					} else if ( key.getKey().getUri().equals("http://www.w3.org/2003/01/geo/wgs84_pos#long") )  { 
						for(ISahaProperty entry: key.getValue()) {
							tmpX.put("longitude", entry.getValueLabel() );
						}
					} 
					else if ( key.getKey().getUri().equals("http://schema.onki.fi/poi#hasPolygon")) {
						for(ISahaProperty entry: key.getValue()) {
							tmObj.append("geometry_polygons", parseCoordinates(entry.getValueLabel()));					
						}
					} else if ( key.getKey().getUri().equals("http://schema.onki.fi/poi#hasPoint")) {
						for(ISahaProperty entry: key.getValue()) {
							tmObj.append("geometry_points", parsePointCoordinates(entry.getValueLabel()));			
						}
					}
				}
				if (tmpX.has("latitude") && tmpX.has("longitude")) {
					JSONObject tmpY = new JSONObject();
					tmpY.put("lat", tmpX.get("latitude"));
					tmpY.put("lon", tmpX.get("longitude"));
					tmObj.append("geometry_points", tmpY);
				}
				if (!tmObj.has("start")) {
					tmObj.put("start", "1000-01-01");					
				}
				tmp.put("tmdata",tmObj);
				result.append("results", tmp);
			}
			if (!result.has("results")) {
				result.put("results", Collections.emptyList());
			}
			
			response.setContentType("application/json;charset=UTF-8");
	        response.getWriter().write(result.toString());

			return null;
		}
	}
	private String getTimeMapEvents(HttpServletRequest request, SahaProject project, Locale locale) {
		String jsonError ="{\"error\": \"error in JSON conversion\"}";
		JSONArray requestUris;
		
		try {
			requestUris = new JSONArray(request.getParameter("uris").split(","));
		} catch(JSONException err) {
			return jsonError;
		} catch(NullPointerException err) {
			return "null";
		}
		
		JSONArray result = new JSONArray();
		try {
			
			for (int i = 0; i < requestUris.length(); i++) {
				JSONObject obj = new JSONObject();
				JSONObject tmpX = new JSONObject();
				JSONObject options = new JSONObject();
				String resourceUri = (String) requestUris.get(i);
				Set<Entry<UriLabel, Set<ISahaProperty>>> propertyMap = project.getResource(resourceUri, locale).getPropertyMapEntrySet();
				options.put("uri", resourceUri);
				obj.put("durationEvent", "false"); //XXX Allow duration events
				for(Entry<UriLabel, Set<ISahaProperty>> key: propertyMap) {
	
					if( key.getKey().getUri().equals("http://www.w3.org/2000/01/rdf-schema#label") )  {
						for(ISahaProperty entry: key.getValue()) {
							obj.put("title", entry.getValueLabel() );
						}
					} else if( key.getKey().getUri().equals("http://www.w3.org/2004/02/skos/core#prefLabel") )  {
						for(ISahaProperty entry: key.getValue()) {
							obj.put("title", entry.getValueLabel() );
						}
					} else if ( key.getKey().getUri().equals("http://www.hatikka.fi/havainnot/date_collected") )  {
						for(ISahaProperty entry: key.getValue()) {
							obj.put("start", entry.getValueLabel() );					
							obj.put("earliestStart", entry.getValueLabel() + " 00:00" );
							obj.put("earliestEnd", entry.getValueLabel()  + " 23:59" );					
						}
					} else if ( key.getKey().getUri().equals("http://www.w3.org/2003/01/geo/wgs84_pos#lat") )  { 
						for(ISahaProperty entry: key.getValue()) {
							tmpX.put("latitude", entry.getValueLabel() );
						}
						
					} else if ( key.getKey().getUri().equals("http://www.w3.org/2003/01/geo/wgs84_pos#long") )  { 
						for(ISahaProperty entry: key.getValue()) {
							tmpX.put("longitude", entry.getValueLabel() );
						}
					} 
					else if ( key.getKey().getUri().equals("http://schema.onki.fi/poi#hasPolygon")) {
						for(ISahaProperty entry: key.getValue()) {
							obj.append("geometry_polygons", parseCoordinates(entry.getValueLabel()));					
						}
					} else if ( key.getKey().getUri().equals("http://schema.onki.fi/poi#hasPoint")) {
						for(ISahaProperty entry: key.getValue()) {
							obj.append("geometry_points", parsePointCoordinates(entry.getValueLabel()));			
						}
					}
				}
				if (tmpX.has("latitude") && tmpX.has("longitude")) {
					JSONObject tmpY = new JSONObject();
					tmpY.put("lat", tmpX.get("latitude"));
					tmpY.put("lon", tmpX.get("longitude"));
					obj.append("geometry_points", tmpY);
				}
				if (!obj.has("start")) {
					obj.put("start", "1000-01-01");					
				}
				obj.put("options", options);
				result.put(obj);
			}
		} catch(JSONException exception) {
			return jsonError;
		}
	
		return result.toString();
	}
	private JSONArray parseCoordinates(String rawString) throws JSONException {
		JSONArray coordArr = new JSONArray();
		for (String pair : rawString.split(" ")) {
			JSONObject tmp = new JSONObject();
			String latlong[] = pair.split(",");
			tmp.put("lat", latlong[0]);
			tmp.put("lon", latlong[1]);
			coordArr.put(tmp);
		}
		return coordArr;
	}
	
	private JSONObject parsePointCoordinates(String rawString) throws JSONException {
		JSONObject tmp = new JSONObject();
		for (String pair : rawString.split(" ")) {
			String latlong[] = pair.split(",");
			tmp.put("lat", latlong[0]);
			tmp.put("lon", latlong[1]);
		}
		return tmp;
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
}
