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

/**
 * Controller for the HAKO config screen and HAKO itself.
 * 
 */
public class HakoController extends ASahaController {

	@SuppressWarnings("unchecked")
	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response, SahaProject project, Locale locale,
			ModelAndView mav) throws Exception {
	    
		if (!project.isHakoConfig() || request.getParameterMap().containsKey("reset") ) {
			mav.addObject("types",project.getSortedInstances(null,OWL.Class.getURI(),locale,0,1000));
			mav.addObject("properties",project.getSortedInstances(null,OWL.ObjectProperty.getURI(),locale,0,1000));
			mav.setViewName("saha3/hako_config");
			return mav;
		}
		
		mav.setViewName( "forward:/app/hako.html" );
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
