package fi.seco.saha3.web.control;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.infrastructure.ResourceLockManager;
import fi.seco.saha3.model.IModelEditor;
import fi.seco.saha3.model.IModelReader;
import fi.seco.saha3.model.configuration.IConfigService;
import freemarker.ext.beans.BeansWrapper;

/**
 * Controller for the resource viewing screen in SAHA. Similar to the editor
 * screen, but without actual editing possibilities.
 * 
 */
public class ResourceController extends ASahaController {

	private ResourceLockManager lockManager;

	@Required
	public void setLockManager(ResourceLockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response, IModelReader reader,
			IModelEditor editor, IConfigService config, Locale locale, ModelAndView mav) throws Exception {

		String uri = request.getParameter("uri");
		String ip = request.getRemoteHost();

		if (uri == null) {
			response.getWriter().write("URI parameter is not set.");
			return null;
		}
		if (!request.getHeader("Accept").contains("html")) mav.setViewName("forward:/project/export.shtml");

		mav.addObject("uri", uri);
		mav.addObject("gmaputil", BeansWrapper.getDefaultInstance().getStaticModels().get("fi.seco.semweb.util.GoogleMapsUtil"));
		mav.addObject("instance", reader.getResource(uri, locale));
		mav.addObject("locked", lockManager.isLocked(uri) && !lockManager.releaseLock(uri, ip));

		mav.addObject("limitInverseProperties", request.getParameter("all") == null);

		return mav;
	}

}
