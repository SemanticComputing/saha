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
import com.hp.hpl.jena.vocabulary.RDFS;

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
				stringMatchesQuery = r.getRequiredProperty(ResourceFactory.createProperty(ns + "stringMatchesQuery")).getString();
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
		else {
			log.info("Using a new default SPARQL configuration.");
			wholeModelQuery = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
			stringMatchesQuery = "SELECT ?item ?itemLabel ?property ?propertyLabel ?object WHERE {\n  ?item ?property ?object .\n  FILTER (REGEX(?object,?query))\n  OPTIONAL {\n    ?item rdfs:label|skos:prefLabel ?itemLabel .\n  }\n  OPTIONAL {\\n    ?item a ?itemType.\n    ?itemType rdfs:label|skos:prefLabel ?itemTypeLabel .\\n  }\\n  OPTIONAL {\n    ?property rdfs:label|skos:prefLabel ?propertyLabel .\n  }\n}";
			instanceQuery = "SELECT ?item ?label WHERE {\n  ?item rdf:type ?type .\n  OPTIONAL {\n    ?item rdfs:label|skos:prefLabel ?label\n  }\n}\nORDER BY ?label";
			labelQuery = "SELECT ?label WHERE {\n  ?uri rdfs:label|skos:prefLabel ?label .\n}";
			typesQuery = "SELECT ?type ?label WHERE {\n  ?uri rdf:type ?type .\n  OPTIONAL {\n    ?type rdfs:label|skos:prefLabel ?label .\n  }\n}";
			propertiesQuery = "SELECT ?propertyURI ?propertyLabel ?object ?objectLabel WHERE {\n  ?uri ?propertyURI ?object .\n  OPTIONAL {\n    ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel . \n  }\n  OPTIONAL {\n    ?object rdfs:label|skos:prefLabel ?objectLabel .\n  }\n}";
			inversePropertiesQuery = "SELECT ?propertyURI ?propertyLabel ?object ?objectLabel ?objectType ?objectTypeLabel WHERE {\n  ?object ?propertyURI ?uri .\n  OPTIONAL {\n    ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel . \n  }\n  OPTIONAL {\n    ?object rdfs:label|skos:prefLabel ?objectLabel .\n  }\n   OPTIONAL {\n    ?object rdf:type ?objectType .\n    OPTIONAL {\n      ?objectType rdfs:label|skos:prefLabel ?objectTypeLabel .\n    }\n  }\n}";
			editorPropertiesQuery = "SELECT ?propertyURI ?propertyComment ?propertyLabel ?propertyType ?propertyRangeURI ?propertyRangeLabel ?object ?objectLabel WHERE {\n" + "  { \n" + "    ?uri ?propertyURI ?object \n" + "    OPTIONAL { \n" + "      ?object rdfs:label|skos:prefLabel ?objectLabel . \n" + "    } \n" + "  } UNION { \n" + "    ?uri a/rdfs:subClassOf* ?typeURI .  \n" + "    ?propertyURI rdfs:domain ?typeURI .\n" + "    OPTIONAL { \n" + "      ?propertyURI rdfs:range ?propertyRangeURI \n" + "      OPTIONAL { \n" + "       ?propertyRangeURI rdfs:label|skos:prefLabel ?propertyRangeLabel .\n" + "      }\n" + "    }\n" + "  } \n" + "  OPTIONAL { \n" + "    ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel .\n" + "  } \n" + "  OPTIONAL { \n" + "   ?propertyURI a ?propertyType .\n" + "  }\n" + "  OPTIONAL { \n" + "    ?propertyURI rdfs:comment ?propertyComment . \n" + "  }\n" + "}";
			propertyTreeQuery = "CONSTRUCT { \n" + "  ?propertyURI rdfs:range ?s . \n" + "  ?s rdfs:subClassOf ?oc . \n" + "  ?sc rdfs:subClassOf ?s . \n" + "  ?s rdfs:label ?label . \n" + "} WHERE { \n" + "  { \n" + "    ?propertyURI rdfs:range ?s \n" + "  } UNION { \n" + "    ?s rdfs:subClassOf ?oc\n" + "  } UNION { \n" + "    ?sc rdfs:subClassOf ?s \n" + "  } OPTIONAL { \n" + "    ?s rdfs:label|skos:prefLabel ?label .\n" + "  }\n" + "}";
			classTreeQuery = "CONSTRUCT { \n" + "  ?s rdfs:subClassOf ?oc . \n" + "  ?sc rdfs:subClassOf ?s . \n" + "  ?s rdfs:label ?label . \n" + "  ?s rdf:count ?count . \n" + "} WHERE { \n" + "  { \n" + "    SELECT (count(?foo) as ?count) ?s WHERE { \n" + "      ?foo rdf:type ?s \n" + "    } \n" + "    GROUP BY ?s \n" + "  } UNION { \n" + "    ?s rdf:type owl:Class \n" + "  } UNION { \n" + "    ?s rdf:type rdfs:Class \n" + "  } UNION { \n" + "    ?s rdfs:subClassOf ?oc\n" + "  } UNION { \n" + "    ?sc rdfs:subClassOf ?s\n" + "  } OPTIONAL { \n" + "    ?s rdfs:label|skos:prefLabel ?label . \n" + "  } \n" + "}";
			labelURI = RDFS.label.getURI();
		}
	}

	private String labelURI;
	private String sparqlURL;
	private String sparulURL;
	private String graphURI;
	private String wholeModelQuery;
	private String stringMatchesQuery;
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
			String wholeModelQuery, String stringMatchesQuery, String instanceQuery, String labelQuery,
			String typesQuery, String propertiesQuery, String inversePropertiesQuery, String editorPropertiesQuery,
			String propertyTreeQuery, String classTreeQuery) {
		this.sparqlURL = sparqlURL;
		this.sparulURL = sparulURL;
		this.graphURI = graphURI;
		this.labelURI = labelURI;
		this.wholeModelQuery = wholeModelQuery;
		this.stringMatchesQuery = stringMatchesQuery;
		this.instanceQuery = instanceQuery;
		this.labelQuery = labelQuery;
		this.typesQuery = typesQuery;
		this.propertiesQuery = propertiesQuery;
		this.inversePropertiesQuery = inversePropertiesQuery;
		this.propertyTreeQuery = propertyTreeQuery;
		this.editorPropertiesQuery = editorPropertiesQuery;
		this.classTreeQuery = classTreeQuery;
		Model m = ModelFactory.createDefaultModel();
		Resource r = m.createResource();
		r.addProperty(RDF.type, ResourceFactory.createResource(ns + "SPARQLConfiguration"));
		r.addProperty(ResourceFactory.createProperty(ns + "sparqlURL"), sparqlURL);
		r.addProperty(ResourceFactory.createProperty(ns + "sparulURL"), sparulURL);
		if (graphURI != null) r.addProperty(ResourceFactory.createProperty(ns + "graphURI"), graphURI);
		r.addProperty(ResourceFactory.createProperty(ns + "wholeModelQuery"), wholeModelQuery);
		r.addProperty(ResourceFactory.createProperty(ns + "stringMatchesQuery"), stringMatchesQuery);
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
	public String getStringMatchesQuery() {
		return stringMatchesQuery;
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
