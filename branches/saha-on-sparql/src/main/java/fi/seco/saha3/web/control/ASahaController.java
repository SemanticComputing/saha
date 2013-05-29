package fi.seco.saha3.web.control;

import java.net.URLEncoder;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.support.RequestContextUtils;

import fi.seco.saha3.infrastructure.SahaHelpManager;
import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.IModelEditor;
import fi.seco.saha3.model.IModelReader;
import fi.seco.saha3.model.configuration.IConfigService;

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
		String model = parseModelName(request);

		Locale locale = RequestContextUtils.getLocale(request);
		IModelEditor editor = sahaProjectRegistry.getModelEditor(model);
		IModelReader reader = sahaProjectRegistry.getModelReader(model);
		IConfigService config = sahaProjectRegistry.getConfig(model);
		String view = parseViewName(pathInfo);
		if (editor != null || view.equals("saha3/manage")) {
			ModelAndView mav = new ModelAndView(view);
			mav.addObject("model", model);
			mav.addObject("lang", locale.getLanguage());
			if (config != null) {
				mav.addObject("aboutLink", config.getAboutLink());
				mav.addObject("helpText", SahaHelpManager.getHelpString(view));
			}
			return handleRequest(request, response, reader, editor, config, locale, mav);
		} else {
			response.sendRedirect("manage.shtml?model=" + URLEncoder.encode(model, "UTF-8"));
			return null;
			/*			ModelAndView mav = new ModelAndView(SAHA_TEMPLATE_BASE + "/manage");
						mav.addObject("sparqlConfiguration", sahaProjectRegistry.getSPARQLConfig(model));
						mav.addObject("model", model);
						mav.addObject("lang", locale.getLanguage());
						return mav; */
		}
	}

	public abstract ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response,
			IModelReader reader, IModelEditor editor, IConfigService config, Locale locale, ModelAndView mav) throws Exception;

	protected static String parseViewName(String servletPath) {
		return SAHA_TEMPLATE_BASE + servletPath.substring(servletPath.lastIndexOf('/'), servletPath.lastIndexOf('.'));
	}

	public static String parseModelName(HttpServletRequest request) {
		return request.getParameter("model");
	}

}
