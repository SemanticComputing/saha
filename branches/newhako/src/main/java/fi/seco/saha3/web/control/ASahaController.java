package fi.seco.saha3.web.control;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.support.RequestContextUtils;

import fi.seco.saha3.infrastructure.SahaHelpManager;
import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.SahaProject;

/**
 * General controller superclass for various SAHA controllers. Loads up the
 * proper template (according to the request URL) and adds general, SAHA-wide
 * properties (project name, language, etc.) to it so there's no need to add 
 * them in every separate controller.
 * 
 */
public abstract class ASahaController extends AbstractController {

	private static final String SAHA_TEMPLATE_BASE = "saha3";

	private SahaProjectRegistry sahaProjectRegistry;

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
		this.sahaProjectRegistry = sahaProjectRegistry;
	}

	protected SahaProjectRegistry getSahaProjectRegistry() {
		return this.sahaProjectRegistry;
	}

	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String pathInfo = request.getPathInfo();
		String model = parseModelName(pathInfo);

		Locale locale = RequestContextUtils.getLocale(request);
		SahaProject project = sahaProjectRegistry.getSahaProject(model);

		if (project != null) {
			String view = parseViewName(pathInfo);
			ModelAndView mav = new ModelAndView(view);
			mav.addObject("model", model);
			mav.addObject("lang", locale.getLanguage());
			mav.addObject("aboutLink", project.getConfig().getAboutLink());
			mav.addObject("helpText", SahaHelpManager.getHelpString(view));
			return handleRequest(request, response, project, locale, mav);
		} else {
			ModelAndView mav = new ModelAndView(SAHA_TEMPLATE_BASE + "/add");
			mav.addObject("model", model);
			mav.addObject("lang", locale.getLanguage());
			return mav;
		}
	}

	public abstract ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response,
			SahaProject project, Locale locale, ModelAndView mav) throws Exception;

	public static String parseModelName(String servletPath) {
		servletPath = servletPath.substring(0, servletPath.lastIndexOf('/'));
		return servletPath.substring(servletPath.lastIndexOf('/') + 1, servletPath.length());
	}

	protected static String parseViewName(String servletPath) {
		return SAHA_TEMPLATE_BASE + servletPath.substring(servletPath.lastIndexOf('/'), servletPath.lastIndexOf('.'));
	}

}
