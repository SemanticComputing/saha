package fi.seco.saha3.web.control;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

/**
 * Controller for exporting data from SAHA. Writes the data model as output in
 * the desired RDF serialization format.
 * 
 */
public class ExportController extends AbstractController {

	private SahaProjectRegistry sahaProjectRegistry;

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
		this.sahaProjectRegistry = sahaProjectRegistry;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");

		String uri = request.getParameter("uri");
		String config = request.getParameter("config");
		String schema = request.getParameter("schema");
		String lang = parseLang(request.getParameter("l"));

		String projectName = ASahaController.parseModelName(request);

		try {
			this.sahaProjectRegistry.getLockForProject(projectName).readLock().lock();

			if (config != null)
				sahaProjectRegistry.getConfig(projectName).getModelFromConfig(projectName).write(response.getWriter(), lang);
			else if (schema != null) {
				// A resource is defined to be a part of the schema here if
				// its own URI or its type URI matches this pattern

				Pattern schemaPattern = Pattern.compile("^" + OWL.getURI() + "\\S*|" + "^" + RDFS.getURI() + "\\S*" + "^" + RDF.getURI() + "\\S*|" + "^" + "http://www.w3.org/2004/02/skos/core#" + "\\S*|");

				Model m = sahaProjectRegistry.getModelReader(projectName).getWholeProject();
				Model output = ModelFactory.createDefaultModel();
				for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, (RDFNode) null)))
					if (schemaPattern.matcher(s.getSubject().getURI()).matches() || (s.getObject().isURIResource() && schemaPattern.matcher(s.getResource().getURI()).matches()))
						output.add(m.listStatements(s.getSubject(), null, (RDFNode) null));
				output.setNsPrefixes(m);
				output.setNsPrefix("rdf", RDF.getURI());
				output.setNsPrefix("rdfs", RDFS.getURI());
				output.setNsPrefix("owl", OWL.getURI());

				output.write(response.getWriter(), lang);
				output.close();
			} else if (uri == null)
				sahaProjectRegistry.getModelReader(projectName).getWholeProject().write(response.getWriter(), lang);
			else {
				Model m = sahaProjectRegistry.getModelReader(projectName).describe(uri);
				Model output = ModelFactory.createDefaultModel();
				output.add(m.listStatements(m.createResource(uri), null, (RDFNode) null));
				output.write(response.getWriter(), lang);
				output.close();
			}
			return null;
		} finally {
			this.sahaProjectRegistry.getLockForProject(projectName).readLock().unlock();
		}
	}

	private String parseLang(String lang) {
		if (lang != null && !lang.isEmpty()) {
			if (lang.equalsIgnoreCase("ttl") || lang.equalsIgnoreCase("turtle")) return "TTL";
			if (lang.equalsIgnoreCase("nt") || lang.equalsIgnoreCase("n-triple")) return "N-TRIPLE";
			if (lang.equalsIgnoreCase("xml") || lang.equalsIgnoreCase("rdf/xml")) return "RDF/XML";
		}
		return "TTL";
	}

}
