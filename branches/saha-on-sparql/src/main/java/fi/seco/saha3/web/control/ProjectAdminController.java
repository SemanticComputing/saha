package fi.seco.saha3.web.control;

import java.net.URLEncoder;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.model.IModelEditor;
import fi.seco.saha3.model.IModelReader;
import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.model.configuration.ISPARQLConfigService;

/**
 * Controller for the project-specific admin screen in SAHA, accessed through
 * the "manage project" button in the project UI.
 * 
 */
public class ProjectAdminController extends ASahaController {
	private final static String DEFAULT_PASS = "rhema04";

	private static final Logger log = LoggerFactory.getLogger(ProjectAdminController.class);

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response, IModelReader reader,
			IModelEditor editor, IConfigService config, Locale locale, ModelAndView mav) throws Exception {
		String password = request.getParameter("password");
		String passhash = request.getParameter("passhash");

		if (config != null && !"".equals(config.getPassHash()) && (password == null || password.isEmpty()) && (passhash == null || passhash.isEmpty())) {
			// Need authorization
			mav.addObject("authorized", false);
			return mav;
		}
		String realPassHash = "";
		String givenPassHash = "";
		if (config != null && !"".equals(config.getPassHash())) {
			realPassHash = config.getPassHash();
			givenPassHash = (passhash == null) ? DigestUtils.sha1Hex(password) : passhash;
		}
		if (givenPassHash.equals(realPassHash)) {
			// Authorized
			mav.addObject("authorized", true);
			mav.addObject("passhash", givenPassHash);
			ISPARQLConfigService sparqlConfiguration = getSahaProjectRegistry().getSPARQLConfig(parseModelName(request));
			mav.addObject("sparqlConfiguration", sparqlConfiguration);

			String operation = request.getParameter("operation");
			String confirm = request.getParameter("confirm");

			if (operation != null && operation.equals("delete") && confirm != null && confirm.equals("true")) {
				// Delete project

				String model = parseModelName(request);

				log.info("Deleting project: " + model);
				this.getSahaProjectRegistry().deleteProject(model);

				response.sendRedirect("../saha3/main.shtml");
				return null;
			}
			if (operation != null && operation.equals("changeSettings")) {
				sparqlConfiguration.setConfiguration(request.getParameter("sparqlURL"), request.getParameter("sparulURL"), "".equals(request.getParameter("graphURI")) ? null : request.getParameter("graphURI"), request.getParameter("labelURI"), request.getParameter("wholeModelQuery"), request.getParameter("stringMatchesQuery"), request.getParameter("instanceQuery"), request.getParameter("labelQuery"), request.getParameter("typesQuery"), request.getParameter("propertiesQuery"), request.getParameter("inversePropertiesQuery"), request.getParameter("editorPropertiesQuery"), request.getParameter("propertyTreeQuery"), request.getParameter("classTreeQuery"));
				response.sendRedirect("index.shtml?model=" + URLEncoder.encode(parseModelName(request), "UTF-8"));
				return null;
			}

			String newPass1 = request.getParameter("newPass");
			String newPass2 = request.getParameter("newPass2");

			if (newPass1 != null && !newPass1.isEmpty() && newPass1.equals(newPass2)) {
				// Change password
				log.info("Changing password for project:" + parseModelName(request));
				if (config == null) config = getSahaProjectRegistry().getConfig(parseModelName(request), true);
				config.setPassHash(DigestUtils.sha1Hex(newPass1));
				mav.addObject("message", "Password changed");
			}
		} else {
			// Access forbidden
			mav.addObject("authorized", false);
			mav.addObject("message", "Invalid password");
		}

		return mav;
	}

}
