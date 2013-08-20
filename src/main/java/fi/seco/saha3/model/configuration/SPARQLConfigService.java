/**
 * 
 */
package fi.seco.saha3.model.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author jiemakel
 */
public class SPARQLConfigService implements ISPARQLConfigService {

	private static final Logger log = LoggerFactory.getLogger(SPARQLConfigService.class);

	private static final String configFileName = "sparqlConfiguration.ttl";

	private String path;

	public void setPath(String path) {
		this.path = path + "/";
		File file = new File(this.path + configFileName);
		if (file.exists())
			try {
				Model m = ModelFactory.createDefaultModel();
				m.read(new FileInputStream(file), null, "TURTLE");
				Resource r = m.listResourcesWithProperty(RDF.type, ResourceFactory.createResource(ns + "SPARQLConfiguration")).next();
				sparqlURL = r.getRequiredProperty(ResourceFactory.createProperty(ns + "sparqlURL")).getString();
				sparulURL = r.getRequiredProperty(ResourceFactory.createProperty(ns + "sparulURL")).getString();
				if (r.hasProperty(ResourceFactory.createProperty(ns + "graphURI")))
					graphURI = r.getRequiredProperty(ResourceFactory.createProperty(ns + "graphURI")).getString();
				wholeModelQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "wholeModelQuery")).getString();
				topStringMatchesQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "topStringMatchesQuery")).getString();
				inlineStringMatchesQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "inlineStringMatchesQuery")).getString();
				instanceQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "instanceQuery")).getString();
				labelQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "labelQuery")).getString();
				typesQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "typesQuery")).getString();
				propertiesQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "propertiesQuery")).getString();
				inversePropertiesQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "inversePropertiesQuery")).getString();
				editorPropertiesQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "editorPropertiesQuery")).getString();
				propertyTreeQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "propertyTreeQuery")).getString();
				classTreeQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "classTreeQuery")).getString();
				labelURI = r.getRequiredProperty(ResourceFactory.createProperty(ns + "labelURI")).getString();
			} catch (FileNotFoundException e) {
				throw new IOError(e);
			}
	}

	private String labelURI;
	private String sparqlURL;
	private String sparulURL;
	private String graphURI;
	private String wholeModelQuery;
	private String topStringMatchesQuery;
	private String inlineStringMatchesQuery;
	private String instanceQuery;
	private String labelQuery;
	private String typesQuery;
	private String propertiesQuery;
	private String inversePropertiesQuery;
	private String editorPropertiesQuery;
	private String propertyTreeQuery;
	private String classTreeQuery;

	private static final String ns = "http://www.seco.tkk.fi/onto/sparqlconfiguration/";

	@Override
	public void setConfiguration(String sparqlURL, String sparulURL, String graphURI, String labelURI,
			String wholeModelQuery, String topStringMatchesQuery, String inlineStringMatchesQuery,
			String instanceQuery, String labelQuery, String typesQuery, String propertiesQuery,
			String inversePropertiesQuery, String editorPropertiesQuery, String propertyTreeQuery, String classTreeQuery) {
		this.sparqlURL = sparqlURL;
		this.sparulURL = sparulURL;
		this.graphURI = graphURI;
		this.labelURI = labelURI;
		this.wholeModelQuery = wholeModelQuery;
		this.topStringMatchesQuery = topStringMatchesQuery;
		this.inlineStringMatchesQuery = inlineStringMatchesQuery;
		this.instanceQuery = instanceQuery;
		this.labelQuery = labelQuery;
		this.typesQuery = typesQuery;
		this.propertiesQuery = propertiesQuery;
		this.inversePropertiesQuery = inversePropertiesQuery;
		this.propertyTreeQuery = propertyTreeQuery;
		this.editorPropertiesQuery = editorPropertiesQuery;
		this.classTreeQuery = classTreeQuery;
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefix("sparql-conf", ns);
		Resource r = m.createResource();
		r.addProperty(RDF.type, ResourceFactory.createResource(ns + "SPARQLConfiguration"));
		r.addProperty(ResourceFactory.createProperty(ns + "sparqlURL"), sparqlURL);
		r.addProperty(ResourceFactory.createProperty(ns + "sparulURL"), sparulURL);
		if (graphURI != null) r.addProperty(ResourceFactory.createProperty(ns + "graphURI"), graphURI);
		r.addProperty(ResourceFactory.createProperty(ns + "wholeModelQuery"), wholeModelQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "topStringMatchesQuery"), topStringMatchesQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "inlineStringMatchesQuery"), inlineStringMatchesQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "instanceQuery"), instanceQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "labelQuery"), labelQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "typesQuery"), typesQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "propertiesQuery"), propertiesQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "inversePropertiesQuery"), inversePropertiesQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "editorPropertiesQuery"), editorPropertiesQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "propertyTreeQuery"), propertyTreeQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "classTreeQuery"), classTreeQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "labelURI"), labelURI);
		new File(path).mkdirs();
		try {
			m.write(new FileOutputStream(path + configFileName), "TURTLE");
		} catch (FileNotFoundException e) {
			throw new IOError(e);
		}
	}

	public String getPath() {
		return path;
	}

	@Override
	public String getSparqlURL() {
		return sparqlURL;
	}

	@Override
	public String getSparulURL() {
		return sparulURL;
	}

	@Override
	public String getWholeModelQuery() {
		return wholeModelQuery;
	}

	@Override
	public String getInlineStringMatchesQuery() {
		return inlineStringMatchesQuery;
	}

	@Override
	public String getTopStringMatchesQuery() {
		return topStringMatchesQuery;
	}

	@Override
	public String getInstanceQuery() {
		return instanceQuery;
	}

	@Override
	public String getLabelQuery() {
		return labelQuery;
	}

	@Override
	public String getTypesQuery() {
		return typesQuery;
	}

	@Override
	public String getPropertiesQuery() {
		return propertiesQuery;
	}

	@Override
	public String getInversePropertiesQuery() {
		return inversePropertiesQuery;
	}

	@Override
	public String getEditorPropertiesQuery() {
		return editorPropertiesQuery;
	}

	@Override
	public String getPropertyTreeQuery() {
		return propertyTreeQuery;
	}

	@Override
	public String getClassTreeQuery() {
		return classTreeQuery;
	}

	@Override
	public String getGraphURI() {
		return graphURI;
	}

	@Override
	public String getLabelURI() {
		return labelURI;
	}

}
