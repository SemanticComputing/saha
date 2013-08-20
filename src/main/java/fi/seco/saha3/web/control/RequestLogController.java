/**
 * 
 */
package fi.seco.saha3.web.control;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.model.IModelEditor;
import fi.seco.saha3.model.IModelReader;
import fi.seco.saha3.model.IRequestLogger;
import fi.seco.saha3.model.configuration.IConfigService;

/**
 * @author jiemakel
 * 
 */
public class RequestLogController extends ASahaController {

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response, IModelReader reader,
			IModelEditor editor, IConfigService config, Locale locale, ModelAndView mav) throws Exception {
		String model = parseModelName(request);
		IRequestLogger logger = getSahaProjectRegistry().getRequestLogger(model);
		response.getWriter().write(logger.getLastLog(5000).replaceAll("\\|MODEL\\|", model));
		return null;
	}

}
