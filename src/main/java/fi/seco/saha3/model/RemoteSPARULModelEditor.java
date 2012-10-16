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
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.util.SAHA3;

/**
 * @author jiemakel
 * 
 */
public class RemoteSPARULModelEditor implements IModelEditor {

	private String sparulService;
	private String sparqlService;

	private String graphURI;

	private IConfigService config;

	@Required
	public void setConfigService(IConfigService config) {
		this.config = config;
	}

	@Required
	public void setGraphURI(String graphURI) {
		this.graphURI = "default".equals(graphURI) ? null : "<" + graphURI + ">";
	}

	@Required
	public void setSPARULService(String sparulService) {
		this.sparulService = sparulService;
	}

	@Required
	public void setSPARQLService(String sparqlService) {
		this.sparqlService = sparqlService;
	}

	private ResultSet execSelect(Query query) {
		if (graphURI != null) return QueryExecutionFactory.sparqlService(sparqlService, query, graphURI).execSelect();
		return QueryExecutionFactory.sparqlService(sparqlService, query).execSelect();
	}

	@Override
	public UriLabel addObjectProperty(String s, String p, String o, Locale locale) {
		execUpdate("INSERT DATA " + (graphURI != null ? "INTO " + graphURI : "") + " { <" + s + "> <" + p + "> <" + o + "> }");
		ParameterizedSparqlString q = new ParameterizedSparqlString(RemoteSPARQLModelReader.getLabelQuery, RemoteSPARQLModelReader.pm);
		q.setIri("uri", o);
		q.setLiteral("lang", locale.toString());
		ResultSet rs = execSelect(q.asQuery());
		if (!rs.hasNext()) return new UriLabel(o, o);
		return new UriLabel(o, rs.next().get("label").toString());
	}

	@Override
	public void removeObjectProperty(String s, String p, String o) {
		execUpdate("DELETE DATA " + (graphURI != null ? "FROM " + graphURI : "") + " { <" + s + "> <" + p + "> <" + o + "> }");
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l) {
		execUpdate("INSERT DATA " + (graphURI != null ? "INTO " + graphURI : "") + " { <" + s + "> <" + p + "> \"" + l + "\" }");
		return new UriLabel("", "", l);
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l, String lang) {
		execUpdate("INSERT DATA " + (graphURI != null ? "INTO " + graphURI : "") + " { <" + s + "> <" + p + "> \"" + l + "\"@" + lang + " }");
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
			if (DigestUtils.shaHex(qs.get("value").toString()).equals(valueShaHex)) {
				Literal l = qs.get("value").asLiteral();
				String lit = "\"" + l.getString() + "\"";
				if (!"".equals(l.getLanguage()))
					lit += "@" + l.getLanguage();
				else if (l.getDatatypeURI() != null) lit += "^^<" + l.getDatatypeURI() + ">";
				execUpdate("DELETE DATA " + (graphURI != null ? "FROM " + graphURI : "") + " { <" + s + "> <" + p + "> " + lit + " }");
				break;
			}
		}
	}

	@Override
	public void removeProperty(String s, String p) {
		execUpdate("DELETE " + (graphURI != null ? "FROM " + graphURI : "") + " { <" + s + "> <" + p + "> ?o } WHERE { <" + s + "> <" + p + "> ?o }");
	}

	@Override
	public String createResource(String type, String label) {
		return createResource(SAHA3.generateRandomUri(config.getNamespace()), type, label);
	}

	@Override
	public String createResource(String uri, String type, String label) {
		if (label != null)
			execUpdate("INSERT DATA " + (graphURI != null ? "INTO " + graphURI : "") + " { <" + uri + "> <" + RDF.type.getURI() + "> <" + type + "> . <" + uri + "> <" + RDFS.label.getURI() + "> \"" + label + "\"}");
		else execUpdate("INSERT DATA " + (graphURI != null ? "INTO " + graphURI : "") + " { <" + uri + "> <" + RDF.type.getURI() + "> <" + type + "> }");
		return uri;
	}

	@Override
	public void removeResource(String uri) {
		execUpdate("DELETE " + (graphURI != null ? "FROM " + graphURI : "") + " { <" + uri + "> ?p ?o . ?s ?p <" + uri + "> . } WHERE { { <" + uri + "> ?p ?o } UNION { ?s ?p <" + uri + "> } }");
	}

	@Override
	public void clear() {
		execUpdate(graphURI == null ? "CLEAR" : "CLEAR GRAPH " + graphURI);
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
		UpdateExecutionFactory.createRemote(UpdateFactory.create(update), sparulService).execute();
	}

}
