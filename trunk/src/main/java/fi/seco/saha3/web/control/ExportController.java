package fi.seco.saha3.web.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

public class ExportController extends AbstractController {

	private SahaProjectRegistry sahaProjectRegistry;

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
		this.sahaProjectRegistry = sahaProjectRegistry;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception 
	{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");

		String uri = request.getParameter("uri");
		String lang = parseLang(request.getParameter("l"));
		String projectName = ASahaController.parseModelName(request.getServletPath());

		try
		{
			this.sahaProjectRegistry.getLockForProject(projectName).readLock().lock();
			
			if (uri == null)
				sahaProjectRegistry.getModel(projectName).write(response.getWriter(),lang);
			else {
				Model m = sahaProjectRegistry.getModel(projectName);
				Model output = ModelFactory.createDefaultModel();
				output.add(m.listStatements(m.createResource(uri),null,(RDFNode)null));
				output.write(response.getWriter(),lang);
			}
			return null;
		}
		finally
		{
			this.sahaProjectRegistry.getLockForProject(projectName).readLock().unlock();
		}
	}

	private String parseLang(String lang) {
		if (lang != null && !lang.isEmpty()) {
			if (lang.equalsIgnoreCase("ttl") || lang.equalsIgnoreCase("turtle"))
				return "TTL";
			if (lang.equalsIgnoreCase("nt") || lang.equalsIgnoreCase("n-triple"))
				return "N-TRIPLE";
			if (lang.equalsIgnoreCase("xml") || lang.equalsIgnoreCase("rdf/xml"))
				return "RDF/XML";
		}
		return "TTL";
	}

}
