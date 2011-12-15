package fi.seco.saha3.web.control;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.model.SahaProject;

/**
 * Controller for the main view of SAHA, where the project class hierarchy
 * is shown and the instances can be listed for each.
 * 
 */
public class SahaController extends ASahaController {
	
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, 
			HttpServletResponse response, SahaProject project, Locale locale, ModelAndView mav) throws Exception {
		
		mav.setViewName("saha3/saha3");
		
		mav.addObject("rootClasses",project.getLocalizedRootClasses(locale));
		
		String type = request.getParameter("type");
		String query = request.getParameter("query");
		
		if ((type != null && !type.isEmpty()) || (query != null && !query.isEmpty())) {
			int from = parseInt(request.getParameter("from"));
			if (from < 0) from = 0;
			
			int to = parseInt(request.getParameter("to"));
			if (to < from) to = from + 500;
			
			mav.addObject("result",project.getSortedInstances(query,type,locale,from,to));
			mav.addObject("typeResource",project.getResource(type,locale));
			
			mav.addObject("from",from);
			mav.addObject("to",to);
		}
		
		mav.addObject("type",type != null ? type : "");
		mav.addObject("query",query != null ? query : "");
		
		return mav;
	}

	private int parseInt(String s) {
		if (s == null) return -1;
		if (s.matches("\\d*")) return Integer.parseInt(s);
		return -1;
	}
	
}
