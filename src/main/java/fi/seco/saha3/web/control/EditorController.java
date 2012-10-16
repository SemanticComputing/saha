package fi.seco.saha3.web.control;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.saha3.infrastructure.ResourceLockManager;
import fi.seco.saha3.model.IModelEditor;
import fi.seco.saha3.model.IModelReader;
import fi.seco.saha3.model.configuration.IConfigService;

/**
 * Controller for the editor view of SAHA
 * 
 */
public class EditorController extends ASahaController {

	private ResourceLockManager lockManager;

	@Required
	public void setLockManager(ResourceLockManager lockManager) {
		this.lockManager = lockManager;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response, IModelReader reader,
			IModelEditor editor, IConfigService config, Locale locale, ModelAndView mav) throws Exception {
		String uri = request.getParameter("uri");
		if (request.getParameter("image_url") != null) { // file upload controller via media.onki.fi throws us this
			String target = request.getParameter("target");
			String property = request.getParameter("property");
			String url = request.getParameter("image_url");
			editor.addLiteralProperty(target, property, url);
		}
		String sessionId = request.getSession().getId();

		if (uri == null) {
			response.getWriter().write("URI parameter is not set.");
			return null;
		}

		String typeUri = request.getParameter("type");
		if (typeUri != null && !typeUri.isEmpty()) editor.addObjectProperty(uri, RDF.type.getURI(), typeUri, locale);

		mav.addObject("uri", uri);
		mav.addObject("instance", reader.getResource(uri, locale));
		mav.addObject("locked", !lockManager.acquireLock(uri, sessionId));
		mav.addObject("literalFormatter", new LiteralFormatter());
		return mav;
	}

	public static class LiteralFormatter {
		public static String wordIndices(String in) {
			String[] words = in.split("\\s+");
			StringBuffer out = new StringBuffer();
			for (int i = 0; i < words.length; i++) {
				out.append(words[i]);
				out.append("<sub>");
				out.append(i);
				out.append("</sub> ");
			}
			return out.toString();
		}
	}

}
