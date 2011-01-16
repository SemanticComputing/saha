package fi.seco.saha3.web.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import com.hp.hpl.jena.vocabulary.OWL;

import fi.seco.saha3.index.ResourceIndex;
import fi.seco.saha3.index.category.UICategories;
import fi.seco.saha3.index.category.UICategoryNode;
import fi.seco.saha3.model.SahaProject;

public class HakoController extends ASahaController {

	@SuppressWarnings("unchecked")
	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response, SahaProject project, Locale locale,
			ModelAndView mav) throws Exception {
	    
		if (!project.isHakoConfig()) {
			mav.addObject("types",project.getSortedInstances(null,OWL.Class.getURI(),locale,0,1000));
			mav.addObject("properties",project.getSortedInstances(null,OWL.ObjectProperty.getURI(),locale,0,1000));
			mav.setViewName("saha3/hako_config");
			return mav;
		}
		
		Map<String,List<String>> parameterMap = toModifiableMap(request.getParameterMap());
		
		parameterMap.remove("lang");
		parameterMap.remove("model");
		
		boolean sort = true;
		List<String> searchStrings = parameterMap.get(ResourceIndex.UBER_FIELD_NAME);
		if (searchStrings != null) for (String searchString : searchStrings)
			if (!searchString.isEmpty())
				sort = false; // list items by relevance
		
		UICategories categories = project.getUICategories(parameterMap,locale);
		
		mav.addObject("result",project.getSortedInstances(parameterMap,project.getHakoTypes(),locale,0,300,sort));
		mav.addObject("categories",categories.getRootNodes());
		
		Map<String,UICategoryNode> selected = new HashMap<String,UICategoryNode>();
		Map<String,UICategoryNode> allNodes = categories.getAllNodes();
		for (List<String> values : parameterMap.values()) {
			for (String value : values) {
				if (allNodes.containsKey(value))
					selected.put(value,allNodes.get(value));
			}
		}
		mav.addObject("selected",selected);
		
		List<String> terms = parameterMap.remove(ResourceIndex.UBER_FIELD_NAME);
		if (terms != null) mav.addObject("terms",terms);
		else mav.addObject("terms",Collections.emptyList());
		
		mav.addObject("parameterMap",parameterMap);  // add all parameters (except previously removed labels)
		
		return mav;
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
