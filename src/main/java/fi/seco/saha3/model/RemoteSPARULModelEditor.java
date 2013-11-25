/**
 * 
 */
package fi.seco.saha3.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.saha3.infrastructure.ExternalRepositoryService;
import fi.seco.saha3.infrastructure.IExternalRepository;
import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.model.configuration.ISPARQLConfigService;
import fi.seco.saha3.model.configuration.RepositoryConfig;
import fi.seco.saha3.util.SAHA3;

/**
 * @author jiemakel
 * 
 */
public class RemoteSPARULModelEditor implements IModelEditor {

	private static final Logger log = LoggerFactory.getLogger(RemoteSPARULModelEditor.class);

	private IConfigService config;

	private ExternalRepositoryService externalRepositoryService;

	@Required
	public void setExternalRepositoryService(ExternalRepositoryService externalRepositoryService) {
		this.externalRepositoryService = externalRepositoryService;
	}

	@Required
	public void setConfigService(IConfigService config) {
		this.config = config;
	}

	private ISPARQLConfigService sparqlConfigService;

	@Required
	public void setSPARQLConfigService(ISPARQLConfigService sparqlConfigService) {
		this.sparqlConfigService = sparqlConfigService;
	}

	private boolean execAsk(Query query) {
		if (sparqlConfigService.getQueryGraphURI() != null)
			return QueryExecutionFactory.sparqlService(sparqlConfigService.getSparqlURL(), query, sparqlConfigService.getQueryGraphURI()).execAsk();
		return QueryExecutionFactory.sparqlService(sparqlConfigService.getSparqlURL(), query).execAsk();
	}

	private ResultSet execSelect(Query query) {
		if (sparqlConfigService.getQueryGraphURI() != null)
			return QueryExecutionFactory.sparqlService(sparqlConfigService.getSparqlURL(), query, sparqlConfigService.getQueryGraphURI()).execSelect();
		return QueryExecutionFactory.sparqlService(sparqlConfigService.getSparqlURL(), query).execSelect();
	}

	private String checkAndAddExternalResources(String propertyUri, String objectUri, Locale locale,
			ParameterizedSparqlString u) {
		String label = null;
		List<ISahaProperty> propertiesToProcess = new ArrayList<ISahaProperty>();
		for (RepositoryConfig repositoryConfig : config.getPropertyConfig(propertyUri).getRepositoryConfigs()) {
			IExternalRepository externalRepository = externalRepositoryService.getExternalRepository(repositoryConfig.getSourceName());
			for (ISahaProperty property : externalRepository.getProperties(objectUri, locale))
				if (property.isLiteral()) propertiesToProcess.add(property);
			String potLabel = externalRepository.getLabel(objectUri, locale.toString());
			if (potLabel != null && !"".equals(potLabel)) label = potLabel;
		}
		if (!propertiesToProcess.isEmpty()) {
			if (sparqlConfigService.getReferenceGraphURI() != null) {
				u.append("\nGRAPH ?rg { ");
				u.setIri("rg", sparqlConfigService.getReferenceGraphURI());
			} else u.append("");
			for (ISahaProperty property : propertiesToProcess) {
				u.append(' ');
				u.appendIri(objectUri);
				u.append(' ');
				u.appendIri(property.getUri());
				u.append(' ');
				if ("".equals(property.getValueLang())) {
					if ("".equals(property.getValueDatatypeUri()))
						u.appendLiteral(property.getValueLabel());
					else u.appendLiteral(property.getValueLabel(), TypeMapper.getInstance().getSafeTypeByName(property.getValueDatatypeUri()));
				} else u.appendLiteral(property.getValueLabel(), property.getValueLang());
				u.append(" .");
			}
			if (sparqlConfigService.getReferenceGraphURI() != null) u.append(" }");
		}
		if (label == null) label = objectUri;
		return label;
	}

	@Override
	public UriLabel addObjectProperty(String s, String p, String o, Locale locale) {
		ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getLabelQuery(), RemoteSPARQLModelReader.pm);
		q.setIri("uri", o);
		String lstring = locale.toString();
		q.setLiteral("lang", lstring);
		ResultSet rs = execSelect(q.asQuery());
		String label = null;
		while (rs.hasNext()) {
			Literal l = rs.next().get("label").asLiteral();
			if (lstring.equals(l.getLanguage())) {
				label = l.getString();
				break;
			} else if ("".equals(l.getLanguage()))
				label = l.getString();
			else if (label == null) label = l.getString();
		}
		ParameterizedSparqlString u;
		if (sparqlConfigService.getUpdateGraphURI() != null) {
			u = new ParameterizedSparqlString("INSERT DATA { GRAPH ?g { ?s ?p ?o . } ");
			u.setIri("g", sparqlConfigService.getUpdateGraphURI());
		} else u = new ParameterizedSparqlString("INSERT DATA { ?s ?p ?o . ");
		u.setIri("s", s);
		u.setIri("p", p);
		u.setIri("o", o);
		if (label == null) label = checkAndAddExternalResources(p, o, locale, u);
		u.append(" }");
		System.out.println(u);
		execUpdate(u.asUpdate());
		return new UriLabel(o, label);
	}

	@Override
	public void removeObjectProperty(String s, String p, String o) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getUpdateGraphURI() != null) {
			//Fuseki doesn't support FROM u = new ParameterizedSparqlString("DELETE DATA FROM ?g { ?s ?p ?o }");
			u = new ParameterizedSparqlString("DELETE WHERE { GRAPH ?g { ?s ?p ?o } }");
			u.setIri("g", sparqlConfigService.getUpdateGraphURI());
		} else u = new ParameterizedSparqlString("DELETE DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		u.setIri("o", o);
		execUpdate(u.asUpdate());
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getUpdateGraphURI() != null) {
			u = new ParameterizedSparqlString("INSERT DATA { GRAPH ?g { ?s ?p ?o } }");
			u.setIri("g", sparqlConfigService.getUpdateGraphURI());
		} else u = new ParameterizedSparqlString("INSERT DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		u.setLiteral("o", l);
		execUpdate(u.asUpdate());
		return new UriLabel("", "", l);
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l, String lang) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getUpdateGraphURI() != null) {
			u = new ParameterizedSparqlString("INSERT DATA { GRAPH ?g { ?s ?p ?o } }");
			u.setIri("g", sparqlConfigService.getUpdateGraphURI());
		} else u = new ParameterizedSparqlString("INSERT DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		u.setLiteral("o", l, lang);
		execUpdate(u.asUpdate());
		return new UriLabel("", lang, l);
	}

	private static final String getLiteralPropertiesQuery = "SELECT ?value WHERE { ?uri ?propertyURI ?value . FILTER isLITERAL(?value) }";

	@Override
	public UriLabel removeLiteralProperty(String s, String p, String valueShaHex) {
		ParameterizedSparqlString q = new ParameterizedSparqlString(getLiteralPropertiesQuery, RemoteSPARQLModelReader.pm);
		q.setIri("uri", s);
		q.setIri("propertyURI", p);
		ResultSet rs = execSelect(q.asQuery());
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (new UriLabel(qs.get("value").asLiteral()).getShaHex().equals(valueShaHex)) {
				Literal l = qs.get("value").asLiteral();
				ParameterizedSparqlString u;
				if (sparqlConfigService.getUpdateGraphURI() != null) {
					// Fuseki doesn't support FROM u = new ParameterizedSparqlString("DELETE DATA FROM ?g { ?s ?p ?o }");
					u = new ParameterizedSparqlString("DELETE WHERE { GRAPH ?g { ?s ?p ?o } }");
					u.setIri("g", sparqlConfigService.getUpdateGraphURI());
				} else u = new ParameterizedSparqlString("DELETE DATA { ?s ?p ?o }");
				u.setIri("s", s);
				u.setIri("p", p);
				u.setLiteral("o", l);
				execUpdate(u.asUpdate());
				return new UriLabel(l);
			}
		}
		throw new IllegalArgumentException("Tried to remove nonexistant literal");
	}

	@Override
	public void removeProperty(String s, String p) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getUpdateGraphURI() != null) {
			// Fuseki doesn't support FROM u = new ParameterizedSparqlString("DELETE DATA FROM ?g { ?s ?p ?o }");
			u = new ParameterizedSparqlString("DELETE WHERE { GRAPH ?g { ?s ?p ?o } }");
			u.setIri("g", sparqlConfigService.getUpdateGraphURI());
		} else u = new ParameterizedSparqlString("DELETE DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		execUpdate(u.asUpdate());
	}

	@Override
	public String createResource(String type, String label) {
		return createResource(SAHA3.generateRandomUri(config.getNamespace()), type, label);
	}

	@Override
	public String createResource(String uri, String type, String label) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getUpdateGraphURI() != null) {
			if (label != null && !"".equals(label)) {
				u = new ParameterizedSparqlString("INSERT DATA { GRAPH ?g { ?s <" + RDF.type.getURI() + "> ?type . ?s ?lprop ?label . } }");
				u.setIri("lprop", sparqlConfigService.getLabelURI());
				u.setLiteral("label", label);
			} else u = new ParameterizedSparqlString("INSERT DATA { GRAPH ?g { ?s <" + RDF.type.getURI() + "> ?type } }");
			u.setIri("g", sparqlConfigService.getUpdateGraphURI());
		} else if (label != null && !"".equals(label)) {
			u = new ParameterizedSparqlString("INSERT DATA { ?s <" + RDF.type.getURI() + "> ?type . ?s ?lprop ?label .}");
			u.setIri("lprop", sparqlConfigService.getLabelURI());
			u.setLiteral("label", label);
		} else u = new ParameterizedSparqlString("INSERT DATA { ?s <" + RDF.type.getURI() + "> ?type }");
		u.setIri("s", uri);
		u.setIri("type", type);
		execUpdate(u.asUpdate());
		return uri;
	}

	@Override
	public void removeResource(String uri) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getUpdateGraphURI() != null) {

			u = new ParameterizedSparqlString("DELETE { GRAPH ?g { ?s ?p ?o . ?s2 ?p2 ?s . } } WHERE { { ?s ?p ?o } UNION { ?s2 ?p2 ?s } }");

			u.setIri("g", sparqlConfigService.getUpdateGraphURI());
		} else u = new ParameterizedSparqlString("DELETE { ?s ?p ?o . ?s2 ?p2 ?s . } WHERE { { ?s ?p ?o } UNION { ?s2 ?p2 ?s } }");
		u.setIri("s", uri);
		log.info("Query string: " + u);
		execUpdate(u.asUpdate());
	}

	@Override
	public void clear() {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getUpdateGraphURI() != null) {
			u = new ParameterizedSparqlString("CLEAR GRAPH ?g");
			u.setIri("g", sparqlConfigService.getUpdateGraphURI());
		} else u = new ParameterizedSparqlString("CLEAR");
		if (sparqlConfigService.getReferenceGraphURI() != null) {
			u.append("\nCLEAR GRAPH ?rg");
			u.setIri("rg", sparqlConfigService.getReferenceGraphURI());
		}
		execUpdate(u.asUpdate());
	}

	@Override
	public void setMapProperty(String s, String fc, String value) {
		removeProperty(s, SAHA3.WGS84_LAT);
		removeProperty(s, SAHA3.WGS84_LONG);
		removeProperty(s, SAHA3.POLYGON_URI);
		removeProperty(s, SAHA3.ROUTE_URI);

		// Remove existing
		if (fc == null || value == null || fc.isEmpty() || value.isEmpty()) return;

		if (fc.equals("singlepoint")) {
			String[] parts = value.split(",");
			addLiteralProperty(s, SAHA3.WGS84_LAT, parts[0]);
			addLiteralProperty(s, SAHA3.WGS84_LONG, parts[1]);
		} else if (fc.equals("polygon"))
			addLiteralProperty(s, SAHA3.POLYGON_URI, value);
		else if (fc.equals("route")) addLiteralProperty(s, SAHA3.ROUTE_URI, value);
	}

	private void execUpdate(UpdateRequest update) {
		UpdateExecutionFactory.createRemote(update, sparqlConfigService.getSparulURL()).execute();
	}

}
