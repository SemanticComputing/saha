package fi.seco.saha3.web.control;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.saha3.infrastructure.ResourceLockManager;
import fi.seco.saha3.model.SahaProject;

public class EditorController extends ASahaController {

	private ResourceLockManager lockManager;

	@Required
	public void setLockManager(ResourceLockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response, SahaProject project, Locale locale, ModelAndView mav) throws Exception {
		String uri = request.getParameter("uri");
		if (request.getParameter("image_url") != null) { // file upload controller via media.onki.fi throws us this
			String target = request.getParameter("target");
			String property = request.getParameter("property");
			String url = request.getParameter("image_url");
			project.addLiteralProperty(target, property, url);
		}
		String sessionId = request.getSession().getId();

		if (uri == null) {
			response.getWriter().write("URI parameter is not set.");
			return null;
		}

		String typeUri = request.getParameter("type");
		if (typeUri != null && !typeUri.isEmpty())
			project.addObjectProperty(uri, RDF.type.getURI(), typeUri, locale);

		mav.addObject("uri", uri);
		mav.addObject("instance", project.getResource(uri, locale));
		mav.addObject("locked", !lockManager.acquireLock(uri, sessionId));

		return mav;
	}

}
