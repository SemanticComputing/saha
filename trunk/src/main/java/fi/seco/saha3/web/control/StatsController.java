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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.hp.hpl.jena.vocabulary.OWL;

import fi.seco.saha3.index.ResourceIndex;
import fi.seco.saha3.index.category.UICategories;
import fi.seco.saha3.index.category.UICategoryNode;
import fi.seco.saha3.model.SahaProject;

/**
 * Controller for the stats
 * 
 */
public class StatsController extends ASahaController {

	@SuppressWarnings("unchecked")
	@Override
	
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response, SahaProject project, Locale locale,
			ModelAndView mav) throws Exception {
			mav.setViewName( "forward:/app/stats.html" );
			return mav;
	}
	
    @RequestMapping("/{model}/stats.shtml")
	public ModelAndView handleRequestSPARQL(HttpServletRequest request,
			HttpServletResponse response, SahaProject project, Locale locale,
			ModelAndView mav) throws Exception {
			mav.setViewName( "forward:/app/stats.html" );
			return mav;
    }
	
	
	
}
