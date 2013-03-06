/**
 * 
 */
package fi.seco.saha3.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.model.IResults.IResult;
import fi.seco.saha3.model.IResults.Result;
import fi.seco.saha3.model.IResults.Results;
import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.model.configuration.PropertyConfig;

/**
 * @author jiemakel
 * 
 */
public class RemoteSPARQLModelReader implements IModelReader {

	private static final Logger log = LoggerFactory.getLogger(RemoteSPARQLModelReader.class);

	private String sparqlService;

	static final PrefixMapping pm = new PrefixMappingImpl();
	static {
		pm.setNsPrefix("rdf", RDF.getURI());
		pm.setNsPrefix("rdfs", RDFS.getURI());
		pm.setNsPrefix("owl", OWL.getURI());
		pm.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
	}

	private String graphURI;

	@Required
	public void setGraphURI(String graphURI) {
		this.graphURI = "default".equals(graphURI) ? null : graphURI;
	}

	@Required
	public void setSPARQLService(String sparqlService) {
		this.sparqlService = sparqlService;
	}

	private static final Query getWholeProjectQuery = QueryFactory.create("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");

	private static final String getStringMatchesQuery = "SELECT ?item ?itemLabel ?property ?propertyLabel ?object WHERE { ?item ?property ?object . FILTER ((langMatches(lang(?object),?lang) || lang(?object)=\"\") && REGEX(?object,?query)) OPTIONAL { ?item rdfs:label|skos:prefLabel ?itemLabel . FILTER (langMatches(lang(?itemLabel),?lang) || lang(?itemLabel)=\"\")} OPTIONAL { ?property rdfs:label|skos:prefLabel ?propertyLabel . FILTER (langMatches(lang(?propertyLabel),?lang) || lang(?propertyLabel)=\"\")} }";

	@Override
	public IResults search(String query, Collection<String> parentRestrictions, Collection<String> typeRestrictions,
			Locale locale, int maxResults) {
		ParameterizedSparqlString q = new ParameterizedSparqlString(getStringMatchesQuery, pm);
		q.setLiteral("query", query);
		q.setLiteral("lang", locale.toString());
		ResultSet rs = execSelect(q.asQuery());
		Map<RDFNode, Result> rmap = new HashMap<RDFNode, Result>();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			Result r = rmap.get(qs.get("item"));
			if (r == null) {
				r = new Result(qs.get("item").toString(), qs.contains("itemLabel") ? qs.get("itemLabel").toString() : qs.get("item").toString());
				rmap.put(qs.get("item"), r);
			}
			if (qs.contains("propertyLabel"))
				r.getAltLabels().add(qs.get("propertyLabel").asLiteral().getString() + ": " + qs.get("object").asLiteral().getString());
			else r.getAltLabels().add(qs.get("property").asResource().getURI() + ": " + qs.get("object").asLiteral().getString());
		}
		List<IResult> ret = new ArrayList<IResult>();
		if (rmap.size() < maxResults) maxResults = rmap.size();
		Iterator<Result> mi = rmap.values().iterator();
		for (int i = 0; i < maxResults; i++)
			ret.add(mi.next());
		return new Results(ret, maxResults);
	}

	private static final String instanceQuery = "SELECT ?item ?label WHERE { ?item rdf:type ?type . OPTIONAL { ?item rdfs:label|skos:prefLabel ?label FILTER (langMatches(lang(?label),?lang) || lang(?label)=\"\")} }";

	@Override
	public IResults getSortedInstances(String label, String type, Locale locale, int from, int to) {
		if (label == null) {
			ParameterizedSparqlString q = new ParameterizedSparqlString(instanceQuery, pm);
			q.setIri("type", type);
			q.setLiteral("lang", locale.toString());
			ResultSet rs = execSelect(q.asQuery());
			List<IResult> ret = new ArrayList<IResult>();
			for (int i = 0; i < from; i++)
				rs.next();
			for (int i = from; i < to; i++) {
				if (!rs.hasNext()) return new Results(ret, i);
				QuerySolution qs = rs.next();
				ret.add(new Result(qs.get("item").toString(), qs.contains("label") ? qs.get("label").asLiteral().getString() : qs.get("item").toString()));
			}
			while (rs.hasNext()) {
				rs.next();
				to++;
			}
			return new Results(ret, to);
		}
		return null;
	}

	static final String getLabelQuery = "SELECT ?label WHERE { ?uri rdfs:label|skos:prefLabel ?label . FILTER (langMatches(lang(?label),?lang) || LANG(?label)=\"\") }";

	private static final String getTypesQuery = "SELECT ?type ?label WHERE { ?uri rdf:type ?type . OPTIONAL { ?type rdfs:label|skos:prefLabel ?label . FILTER (langMatches(lang(?label),?lang) || LANG(?label)=\"\") } }";

	private static final String getPropertiesQuery = "SELECT ?propertyURI ?propertyLabel ?object ?objectLabel WHERE { ?uri ?propertyURI ?object . OPTIONAL { ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel . FILTER (langMatches(lang(?propertyLabel),?lang) || LANG(?propertyLabel)=\"\") } OPTIONAL { ?object rdfs:label|skos:prefLabel ?objectLabel . FILTER (langMatches(lang(?objectLabel),?lang) || LANG(?objectLabel)=\"\") }}";
	private static final String getInversePropertiesQuery = "SELECT ?propertyURI ?propertyLabel ?object ?objectLabel ?objectType ?objectTypeLabel WHERE { ?object ?propertyURI ?uri . OPTIONAL { ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel . FILTER (langMatches(lang(?propertyLabel),?lang) || LANG(?propertyLabel)=\"\") } OPTIONAL { ?object rdfs:label|skos:prefLabel ?objectLabel . FILTER (langMatches(lang(?objectLabel),?lang) || LANG(?objectLabel)=\"\") } OPTIONAL { ?object rdf:type ?objectType . OPTIONAL {?objectType rdfs:label|skos:prefLabel ?objectTypeLabel . FILTER (langMatches(lang(?objectTypeLabel),?lang) || LANG(?objectTypeLabel)=\"\")}}}";

	private static final String getEditorPropertiesQuery = "SELECT ?propertyURI ?propertyComment ?propertyLabel ?propertyRangeURI ?propertyRangeLabel ?object ?objectLabel WHERE { { ?uri ?propertyURI ?object } UNION { ?propertyURI rdfs:domain ?typeURI OPTIONAL { ?propertyURI rdfs:range ?propertyRangeURI OPTIONAL { ?propertyRangeURI rdfs:label|skos:prefLabel ?propertyRangeLabel . FILTER (langMatches(lang(?propertyRangeLabel),?lang) || LANG(?propertyRangeLabel)=\"\") }}} OPTIONAL { ?propertyURI rdfs:label|skos:prefLabel ?propertyLabel . FILTER (langMatches(lang(?propertyLabel),?lang) || LANG(?propertyLabel)=\"\") } OPTIONAL { ?propertyURI rdfs:comment ?propertyComment . FILTER (langMatches(lang(?propertyComment),?lang) || LANG(?propertyComment)=\"\") } OPTIONAL { ?object rdfs:label|skos:prefLabel ?objectLabel . FILTER (langMatches(lang(?objectLabel),?lang) || LANG(?objectLabel)=\"\") }}";

	private final class SahaSPARQLResultProperty implements ISahaProperty {

		private final Locale locale;

		public SahaSPARQLResultProperty(QuerySolution qs, Locale locale) {
			objectNode = qs.get("object");
			propertyNode = qs.get("propertyURI").asResource();
			propertyLabel = qs.contains("propertyLabel") ? qs.get("propertyLabel").asLiteral().getString() : propertyNode.getURI();
			if (qs.contains("objectLabel"))
				valueLabel = qs.get("objectLabel").asLiteral().getString();
			else if (objectNode == null)
				valueLabel = null;
			else if (objectNode.isLiteral())
				valueLabel = objectNode.asLiteral().getString();
			else valueLabel = objectNode.asResource().getURI();
			if (objectNode != null)
				isLiteral = objectNode.isLiteral();
			else if (qs.contains("propertyRange"))
				isLiteral = qs.get("propertyRange").equals(OWL.DatatypeProperty);
			else isLiteral = false;
			valueType = qs.contains("objectType") ? qs.get("objectType").asResource().getURI() : null;
			valueTypeLabel = qs.contains("objectTypeLabel") ? qs.get("objectTypeLabel").asLiteral().getString() : valueType;
			comment = qs.contains("comment") ? qs.get("comment").asLiteral().getString() : "";
			this.locale = locale;
		}

		private final RDFNode objectNode;
		private final Resource propertyNode;
		private final String propertyLabel;
		private final String valueLabel;

		@Override
		public int compareTo(ISahaProperty o) {
			return compareSahaProperties(this, o);
		}

		private final boolean isLiteral;

		@Override
		public boolean isLiteral() {
			return isLiteral;
		}

		@Override
		public String getValueUri() {
			if (objectNode.isURIResource()) return objectNode.asResource().getURI();
			return "";
		}

		private final String valueType;

		@Override
		public String getValueTypeUri() {
			return valueType;
		}

		private final String valueTypeLabel;

		@Override
		public String getValueTypeLabel() {
			return valueTypeLabel;
		}

		@Override
		public String getValueShaHex() {
			if (isLiteral()) return DigestUtils.shaHex(objectNode.asLiteral().toString());
			return DigestUtils.shaHex(getValueLabel());
		}

		@Override
		public String getValueLang() {
			return (objectNode.asLiteral().getLanguage());
		}

		@Override
		public String getValueLabel() {
			return valueLabel;
		}

		@Override
		public String getValueDatatypeUri() {
			if (objectNode == null) return "";
			String dt = objectNode.asLiteral().getDatatypeURI();
			return dt == null ? "" : dt;
		}

		@Override
		public String getUri() {
			return propertyNode.getURI();
		}

		@Override
		public String getLabel() {
			return propertyLabel;
		}

		private final String comment;

		@Override
		public String getComment() {
			return comment;
		}

		@Override
		public PropertyConfig getConfig() {
			return config.getPropertyConfig(getUri());
		}

		private final List<UriLabel> ranges = new ArrayList<UriLabel>();

		@Override
		public Set<String> getRange() {
			Set<String> ret = new HashSet<String>();
			for (UriLabel range : ranges)
				ret.add(range.getUri());
			return ret;
		}

		@Override
		public List<UriLabel> getRangeUriLabel() {
			return ranges;
		}

		private static final String getPropertyTreeQuery = "CONSTRUCT { ?propertyURI rdfs:range ?s . ?s rdfs:subClassOf ?oc . ?sc rdfs:subClassOf ?s . ?s rdfs:label ?label . } WHERE { { ?propertyURI rdfs:range ?s } UNION { ?s rdfs:subClassOf ?oc } UNION { ? rdfs:subClassOf ?s } OPTIONAL { ?s rdfs:label|skos:prefLabel ?label . FILTER (langMatches(lang(?label),?lang) || lang(?label)=\"\")} }";

		@Override
		public Set<UITreeNode> getRangeTree() {
			Map<Resource, UITreeNode> nodes = new HashMap<Resource, UITreeNode>();
			ParameterizedSparqlString q = new ParameterizedSparqlString(getPropertyTreeQuery, pm);
			q.setLiteral("lang", locale.toString());
			q.setIri("propertyURI", propertyNode.getURI());
			Model m = execConstruct(q.asQuery());
			ResIterator ri = m.listSubjects();
			while (ri.hasNext()) {
				Resource r = ri.next();
				int count = 0;
				Statement s = r.getProperty(cp);
				if (s != null) count = s.getInt();
				String label = r.getURI();
				s = r.getProperty(RDFS.label);
				if (s != null) label = s.getString();
				nodes.put(r, new UITreeNode(r.getURI(), label, count));
			}
			StmtIterator sti = m.listStatements(null, RDFS.subClassOf, (RDFNode) null);
			while (sti.hasNext()) {
				Statement s = sti.next();
				UITreeNode child = nodes.get(s.getSubject());
				nodes.get(s.getResource()).addChild(child);
			}
			sti = m.listStatements(null, RDFS.range, (RDFNode) null);
			Set<UITreeNode> roots = new HashSet<UITreeNode>();
			while (sti.hasNext())
				roots.add(nodes.get(sti.next().getResource()));
			return roots;
		}

		private void addPropertyRanges(QuerySolution qs) {
			if (qs.contains("propertyRangeURI"))
				ranges.add(new UriLabel(qs.get("properyRangeURI").asResource().getURI(), qs.contains("propertyRangeLabel") ? qs.get("propertyRangeLabel").asLiteral().getString() : qs.get("properyRangeURI").asResource().getURI()));
		}
	}

	@Override
	public ISahaResource getResource(final String resourceUri, final Locale locale) {
		return new ISahaResource() {

			private String label;

			@Override
			public String getUri() {
				return resourceUri;
			}

			@Override
			public String getLabel() {
				if (label == null) {
					ParameterizedSparqlString q = new ParameterizedSparqlString(getLabelQuery, pm);
					q.setIri("uri", resourceUri);
					q.setLiteral("lang", locale.toString());
					ResultSet rs = execSelect(q.asQuery());
					if (!rs.hasNext())
						label = resourceUri;
					else label = rs.next().get("label").asLiteral().getString();
				}
				return label;
			}

			private List<UriLabel> types;

			@Override
			public List<UriLabel> getTypes() {
				if (types == null) {
					types = new ArrayList<UriLabel>();
					ParameterizedSparqlString q = new ParameterizedSparqlString(getTypesQuery, pm);
					q.setIri("uri", resourceUri);
					q.setLiteral("lang", locale.toString());
					ResultSet rs = execSelect(q.asQuery());
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						types.add(new UriLabel(qs.get("type").toString(), qs.contains("label") ? qs.get("label").asLiteral().getString() : qs.get("type").toString()));
					}
				}
				return types;
			}

			private List<ISahaProperty> properties;

			@Override
			public List<ISahaProperty> getProperties() {
				if (properties == null) {
					properties = new ArrayList<ISahaProperty>();
					ParameterizedSparqlString q = new ParameterizedSparqlString(getPropertiesQuery, pm);
					q.setIri("uri", resourceUri);
					q.setLiteral("lang", locale.toString());
					ResultSet rs = execSelect(q.asQuery());
					while (rs.hasNext())
						properties.add(new SahaSPARQLResultProperty(rs.next(), locale));
				}
				return properties;
			}

			private List<ISahaProperty> inverseProperties;

			@Override
			public List<ISahaProperty> getInverseProperties() {
				if (inverseProperties == null) {
					inverseProperties = new ArrayList<ISahaProperty>();
					ParameterizedSparqlString q = new ParameterizedSparqlString(getInversePropertiesQuery, pm);
					q.setIri("uri", resourceUri);
					q.setLiteral("lang", locale.toString());
					ResultSet rs = execSelect(q.asQuery());
					while (rs.hasNext())
						inverseProperties.add(new SahaSPARQLResultProperty(rs.next(), locale));
				}
				return inverseProperties;
			}

			@Override
			public Iterator<ISahaProperty> getSortedInverseProperties() {
				return new fi.seco.semweb.util.BinaryHeap<ISahaProperty>(getInverseProperties(), propertyComparator).iterator();
			}

			@Override
			public Map<UriLabel, Set<ISahaProperty>> getPropertyMap() {
				return mapToProperties(getProperties(), getPropertyOrderMap(getTypes()));
			}

			@Override
			public Set<Entry<UriLabel, Set<ISahaProperty>>> getPropertyMapEntrySet() {
				return getPropertyMap().entrySet();
			}

			private Set<ISahaProperty> editorProperties;

			@Override
			public Set<ISahaProperty> getEditorProperties() {
				if (editorProperties == null) {
					Map<RDFNode, SahaSPARQLResultProperty> pmap = new HashMap<RDFNode, SahaSPARQLResultProperty>();
					editorProperties = new HashSet<ISahaProperty>();
					ParameterizedSparqlString q = new ParameterizedSparqlString(getEditorPropertiesQuery, pm);
					q.setIri("uri", resourceUri);
					q.setLiteral("lang", locale.toString());
					ResultSet rs = execSelect(q.asQuery());
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						SahaSPARQLResultProperty sp = pmap.get(qs.get("propertyURI"));
						if (sp == null) {
							sp = new SahaSPARQLResultProperty(qs, locale);
							pmap.put(qs.get("propertyURI"), sp);
						}
						sp.addPropertyRanges(qs);
					}
					editorProperties = new HashSet<ISahaProperty>(pmap.values());
				}
				return editorProperties;
			}

			@Override
			public Map<UriLabel, Set<ISahaProperty>> getEditorPropertyMap() {
				return mapToProperties(getEditorProperties(), getPropertyOrderMap(getTypes()));
			}

			@Override
			public Set<Entry<UriLabel, Set<ISahaProperty>>> getEditorPropertyMapEntrySet() {
				return getEditorPropertyMap().entrySet();
			}

		};
	}

	@Override
	public Model getWholeProject() {
		return execConstruct(getWholeProjectQuery);
	}

	private ResultSet execSelect(Query query) {
		if (graphURI != null) return QueryExecutionFactory.sparqlService(sparqlService, query, graphURI).execSelect();
		return QueryExecutionFactory.sparqlService(sparqlService, query).execSelect();
	}

	private Model execConstruct(Query query) {
		if (graphURI != null)
			return QueryExecutionFactory.sparqlService(sparqlService, query, graphURI).execConstruct();
		return QueryExecutionFactory.sparqlService(sparqlService, query).execConstruct();
	}

	private Model execDescribe(Query query) {
		if (graphURI != null)
			return QueryExecutionFactory.sparqlService(sparqlService, query, graphURI).execDescribe();
		return QueryExecutionFactory.sparqlService(sparqlService, query).execDescribe();
	}

	private static final Property cp = ResourceFactory.createProperty(RDF.getURI() + "count");

	private static final String getClassTreeQuery = "CONSTRUCT { ?s rdfs:subClassOf ?oc . ?sc rdfs:subClassOf ?s . ?s rdfs:label ?label . ?s rdf:count ?count . } WHERE { { SELECT (count(?foo) as ?count) ?s WHERE { ?foo rdf:type ?s } GROUP BY ?s } UNION { ?s rdf:type owl:Class } UNION { ?s rdf:type rdfs:Class } UNION { ?s rdfs:subClassOf ?oc } UNION { ?sc rdfs:subClassOf ?s } OPTIONAL { ?s rdfs:label|skos:prefLabel ?label . FILTER (langMatches(lang(?label),?lang) || lang(?label)=\"\")} }";

	@Override
	public Set<UITreeNode> getClassTree(Locale locale) {
		Map<Resource, UITreeNode> nodes = new HashMap<Resource, UITreeNode>();
		ParameterizedSparqlString q = new ParameterizedSparqlString(getClassTreeQuery, pm);
		q.setLiteral("lang", locale.toString());
		Model m = execConstruct(q.asQuery());
		ExtendedIterator<RDFNode> ri = m.listObjects().andThen(m.listSubjects());
		while (ri.hasNext()) {
			RDFNode rn = ri.next();
			if (!rn.isResource()) continue;
			Resource r = rn.asResource();
			if (nodes.containsKey(r)) continue;
			int count = 0;
			Statement s = r.getProperty(cp);
			if (s != null) count = s.getInt();
			String label = r.getURI();
			s = r.getProperty(RDFS.label);
			if (s != null) label = s.getString();
			nodes.put(r, new UITreeNode(r.getURI(), label, count));
		}
		Set<UITreeNode> roots = new HashSet<UITreeNode>(nodes.values());
		StmtIterator sti = m.listStatements(null, RDFS.subClassOf, (RDFNode) null);
		while (sti.hasNext()) {
			Statement s = sti.next();
			UITreeNode child = nodes.get(s.getSubject());
			nodes.get(s.getResource()).addChild(child);
			roots.remove(child);
		}
		return roots;
	}

	private IConfigService config;

	@Required
	public void setConfigService(IConfigService config) {
		this.config = config;
	}

	private Map<String, Integer> getPropertyOrderMap(Collection<UriLabel> typeUris) {
		for (UriLabel typeUri : typeUris) {
			Collection<String> propertyOrder = config.getPropertyOrder(typeUri.getUri());
			if (!propertyOrder.isEmpty()) return buildPositionMap(propertyOrder);
		}
		return Collections.emptyMap();
	}

	private static Map<String, Integer> buildPositionMap(Collection<String> propertyOrder) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		int i = 0;
		for (String poperty : propertyOrder)
			map.put(poperty, i++);
		return map;
	}

	private static Map<UriLabel, Set<ISahaProperty>> mapToProperties(Collection<ISahaProperty> c,
			Map<String, Integer> propertyOrderMap) {
		Map<UriLabel, Set<ISahaProperty>> map = new TreeMap<UriLabel, Set<ISahaProperty>>(propertyOrderOverridingComparator(propertyOrderMap));
		for (ISahaProperty property : c) {
			UriLabel p = new UriLabel(property.getUri(), property.getLabel());
			if (!map.containsKey(p)) map.put(p, new TreeSet<ISahaProperty>());
			map.get(p).add(property);
		}
		return map;
	}

	private static Comparator<UriLabel> propertyOrderOverridingComparator(final Map<String, Integer> propertyOrderMap) {
		return new Comparator<UriLabel>() {

			@Override
			public int compare(UriLabel u0, UriLabel u1) {
				boolean firstKnown = propertyOrderMap.containsKey(u0.getUri());
				boolean secondKnown = propertyOrderMap.containsKey(u1.getUri());
				if (firstKnown && !secondKnown) return -1;
				if (!firstKnown && secondKnown) return 1;
				if (firstKnown && secondKnown)
					return propertyOrderMap.get(u0.getUri()).compareTo(propertyOrderMap.get(u1.getUri()));
				return u0.compareTo(u1);
			}
		};
	}

	private final static int compareSahaProperties(ISahaProperty o1, ISahaProperty o2) {
		int c = String.CASE_INSENSITIVE_ORDER.compare(o1.getValueLabel(), o2.getValueLabel());
		if (c == 0) c = String.CASE_INSENSITIVE_ORDER.compare(o1.getValueShaHex(), o2.getValueShaHex());
		return c != 0 ? c : o1.getValueUri().compareTo(o2.getValueUri());
	}

	private final static Comparator<ISahaProperty> propertyComparator = new Comparator<ISahaProperty>() {

		@Override
		public int compare(ISahaProperty o1, ISahaProperty o2) {
			return compareSahaProperties(o1, o2);
		}
	};

	@Override
	public Model describe(String uri) {
		return execDescribe(QueryFactory.create("DESCRIBE <" + uri + ">"));
	}

}
