package fi.seco.saha3.web.control;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.factory.ResourceFactory;
import fi.seco.saha3.infrastructure.ResourceLockManager;
import fi.seco.saha3.model.ISahaResource;
import fi.seco.saha3.model.SahaProject;
import fi.seco.semweb.util.BinaryHeap;
import freemarker.ext.beans.BeansWrapper;


public class ResourceController extends ASahaController {

	private ResourceLockManager lockManager;

	@Required
	public void setLockManager(ResourceLockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response, SahaProject project, Locale locale, ModelAndView mav) throws Exception {

		String uri = request.getParameter("uri");
		String sessionId = request.getSession().getId();

		if (uri == null) {
			response.getWriter().write("URI parameter is not set.");
			return null;
		}

		mav.addObject("uri",uri);
		mav.addObject("gmaputil",BeansWrapper.getDefaultInstance().getStaticModels().get("fi.seco.semweb.util.GoogleMapsUtil"));
		mav.addObject("instance",project.getResource(uri,locale));
		mav.addObject("locked",lockManager.isLocked(uri) && !lockManager.releaseLock(uri,sessionId));

		List<ISahaResource> list = project.getPropertyMissingResources(uri,locale);
		mav.addObject("propertyMissingInstances",list);
		mav.addObject("propertyMissingInstancesSorted",new BinaryHeap<ISahaResource>(
				list,ResourceFactory.getResourceComparator()).iterator());

		mav.addObject("limitInverseProperties",request.getParameter("all") == null);

		return mav;
	}

}
