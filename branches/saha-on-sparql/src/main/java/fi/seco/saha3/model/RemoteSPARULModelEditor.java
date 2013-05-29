/**
 * 
 */
package fi.seco.saha3.model;

import java.util.Locale;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.model.configuration.ISPARQLConfigService;
import fi.seco.saha3.util.SAHA3;

/**
 * @author jiemakel
 * 
 */
public class RemoteSPARULModelEditor implements IModelEditor {

	private IConfigService config;

	@Required
	public void setConfigService(IConfigService config) {
		this.config = config;
	}

	private ISPARQLConfigService sparqlConfigService;

	@Required
	public void setSPARQLConfigService(ISPARQLConfigService sparqlConfigService) {
		this.sparqlConfigService = sparqlConfigService;
	}

	private ResultSet execSelect(Query query) {
		if (sparqlConfigService.getGraphURI() != null)
			return QueryExecutionFactory.sparqlService(sparqlConfigService.getSparqlURL(), query, sparqlConfigService.getGraphURI()).execSelect();
		return QueryExecutionFactory.sparqlService(sparqlConfigService.getSparqlURL(), query).execSelect();
	}

	@Override
	public UriLabel addObjectProperty(String s, String p, String o, Locale locale) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getGraphURI() != null) {
			u = new ParameterizedSparqlString("INSERT DATA INTO ?g { ?s ?p ?o }");
			u.setIri("g", sparqlConfigService.getGraphURI());
		} else u = new ParameterizedSparqlString("INSERT DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		u.setIri("o", o);
		execUpdate(u.toString());
		ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getLabelQuery(), RemoteSPARQLModelReader.pm);
		q.setIri("uri", o);
		String lstring = locale.toString();
		q.setLiteral("lang", lstring);
		ResultSet rs = execSelect(q.asQuery());
		String label = null;
		if (!rs.hasNext())
			label = o;
		else while (rs.hasNext()) {
			Literal l = rs.next().get("label").asLiteral();
			if (lstring.equals(l.getLanguage())) {
				label = l.getString();
				break;
			} else if ("".equals(l.getLanguage()))
				label = l.getString();
			else if (label == null) label = l.getString();
		}
		return new UriLabel(o, label);
	}

	@Override
	public void removeObjectProperty(String s, String p, String o) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getGraphURI() != null) {
			u = new ParameterizedSparqlString("DELETE DATA FROM ?g { ?s ?p ?o }");
			u.setIri("g", sparqlConfigService.getGraphURI());
		} else u = new ParameterizedSparqlString("DELETE DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		u.setIri("o", o);
		execUpdate(u.toString());
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getGraphURI() != null) {
			u = new ParameterizedSparqlString("INSERT DATA INTO ?g { ?s ?p ?o }");
			u.setIri("g", sparqlConfigService.getGraphURI());
		} else u = new ParameterizedSparqlString("INSERT DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		u.setLiteral("o", l);
		execUpdate(u.toString());
		return new UriLabel("", "", l);
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l, String lang) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getGraphURI() != null) {
			u = new ParameterizedSparqlString("INSERT DATA INTO ?g { ?s ?p ?o }");
			u.setIri("g", sparqlConfigService.getGraphURI());
		} else u = new ParameterizedSparqlString("INSERT DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		u.setLiteral("o", l, lang);
		execUpdate(u.toString());
		return new UriLabel("", lang, l);
	}

	private static final String getLiteralPropertiesQuery = "SELECT ?value WHERE { ?uri ?propertyURI ?value . FILTER isLITERAL(?value) }";

	@Override
	public void removeLiteralProperty(String s, String p, String valueShaHex) {
		ParameterizedSparqlString q = new ParameterizedSparqlString(getLiteralPropertiesQuery, RemoteSPARQLModelReader.pm);
		q.setIri("uri", s);
		q.setIri("propertyURI", p);
		ResultSet rs = execSelect(q.asQuery());
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			if (DigestUtils.sha1Hex(qs.get("value").toString()).equals(valueShaHex)) {
				Literal l = qs.get("value").asLiteral();
				ParameterizedSparqlString u;
				if (sparqlConfigService.getGraphURI() != null) {
					u = new ParameterizedSparqlString("DELETE DATA FROM ?g { ?s ?p ?o }");
					u.setIri("g", sparqlConfigService.getGraphURI());
				} else u = new ParameterizedSparqlString("DELETE DATA { ?s ?p ?o }");
				u.setIri("s", s);
				u.setIri("p", p);
				u.setLiteral("o", l);
				execUpdate(u.toString());
				break;
			}
		}
	}

	@Override
	public void removeProperty(String s, String p) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getGraphURI() != null) {
			u = new ParameterizedSparqlString("DELETE DATA FROM ?g { ?s ?p ?o }");
			u.setIri("g", sparqlConfigService.getGraphURI());
		} else u = new ParameterizedSparqlString("DELETE DATA { ?s ?p ?o }");
		u.setIri("s", s);
		u.setIri("p", p);
		execUpdate(u.toString());
	}

	@Override
	public String createResource(String type, String label) {
		return createResource(SAHA3.generateRandomUri(config.getNamespace()), type, label);
	}

	@Override
	public String createResource(String uri, String type, String label) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getGraphURI() != null) {
			if (label != null) {
				u = new ParameterizedSparqlString("INSERT DATA INTO ?g { ?s <" + RDF.type.getURI() + "> ?type . ?s ?lprop ?label .}");
				u.setIri("lprop", sparqlConfigService.getLabelURI());
				u.setLiteral("label", label);
			} else u = new ParameterizedSparqlString("INSERT DATA INTO ?g { ?s <" + RDF.type.getURI() + "> ?type }");
			u.setIri("g", sparqlConfigService.getGraphURI());
		} else if (label != null) {
			u = new ParameterizedSparqlString("INSERT DATA { ?s <" + RDF.type.getURI() + "> ?type . ?s ?lprop ?label .}");
			u.setIri("lprop", sparqlConfigService.getLabelURI());
			u.setLiteral("label", label);
		} else u = new ParameterizedSparqlString("INSERT DATA { ?s <" + RDF.type.getURI() + "> ?type }");
		u.setIri("s", uri);
		u.setIri("type", type);
		execUpdate(u.toString());
		return uri;
	}

	@Override
	public void removeResource(String uri) {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getGraphURI() != null) {
			u = new ParameterizedSparqlString("DELETE DATA FROM ?g { { ?s ?p ?o } UNION { ?o ?p ?s } }");
			u.setIri("g", sparqlConfigService.getGraphURI());
		} else u = new ParameterizedSparqlString("DELETE DATA { { ?s ?p ?o } UNION { ?o ?p ?s } }");
		u.setIri("s", uri);
		execUpdate(u.toString());
	}

	@Override
	public void clear() {
		ParameterizedSparqlString u;
		if (sparqlConfigService.getGraphURI() != null) {
			u = new ParameterizedSparqlString("CLEAR GRAPH ?g");
			u.setIri("g", sparqlConfigService.getGraphURI());
		} else u = new ParameterizedSparqlString("CLEAR");
		execUpdate(u.toString());
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

	private void execUpdate(String update) {
		UpdateExecutionFactory.createRemote(UpdateFactory.create(update), sparqlConfigService.getSparulURL()).execute();
	}

}
