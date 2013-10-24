/**
 * 
 */
package fi.seco.saha3.model;

import java.io.IOError;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.riot.WebContent;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
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
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.model.IResults.IResult;
import fi.seco.saha3.model.IResults.Result;
import fi.seco.saha3.model.IResults.Results;
import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.model.configuration.ISPARQLConfigService;
import fi.seco.saha3.model.configuration.PropertyConfig;
import fi.seco.semweb.util.ASCIIFoldingFilterWithFinnishExceptions;

/**
 * @author jiemakel
 * 
 */
public class RemoteSPARQLModelReader implements IModelReader {

	private static final Logger log = LoggerFactory.getLogger(RemoteSPARQLModelReader.class);

	private ISPARQLConfigService sparqlConfigService;

	private static final String sns = "http://www.seco.tkk.fi/onto/saha#";

	static final PrefixMapping pm = new PrefixMappingImpl();
	static {
		pm.setNsPrefix("saha", sns);
		pm.setNsPrefix("rdf", RDF.getURI());
		pm.setNsPrefix("rdfs", RDFS.getURI());
		pm.setNsPrefix("owl", OWL.getURI());
		pm.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
	}

	@Required
	public void setSPARQLConfigService(ISPARQLConfigService sparqlConfigService) {
		this.sparqlConfigService = sparqlConfigService;
	}

	private static final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);

	private static final QueryParser qp = new QueryParser(Version.LUCENE_43, "", analyzer);

	private static final Collection<String> topSearchMarker = new ArrayList<String>(0);

	@Override
	public IResults topSearch(String query, Locale locale, int maxResults) {
		return search(query, topSearchMarker, locale, maxResults);
	}

	@Override
	public IResults inlineSearch(String query, Collection<String> typeRestrictions, Locale locale, int maxResults) {
		return search(query, typeRestrictions, locale, maxResults);
	}

	private static void maybeUpdate(QuerySolution qs, String variable, RDFNode o, Map<RDFNode, Literal> map,
			String lstring, boolean fallback) {
		Literal current = map.get(o);
		if (qs.contains(variable)) {
			Literal potential = qs.get(variable).asLiteral();
			if (current == null || (o.isURIResource() && o.asResource().getURI().equals(current.getString())) || (o.isAnon() && o.asResource().getId().getLabelString().equals(current.getString())) || lstring.equals(potential.getLanguage()))
				map.put(o, potential);
			else if ("".equals(potential.getLanguage()) && !lstring.equals(current.getLanguage()))
				map.put(o, potential);
		} else if (fallback) if (current == null) if (o.isURIResource())
			map.put(o, ResourceFactory.createPlainLiteral(o.asResource().getURI()));
		else if (o.isAnon())
			map.put(o, ResourceFactory.createPlainLiteral(o.asResource().getId().getLabelString()));
		else map.put(o, o.asLiteral());
	}

	private IResults search(String query, Collection<String> typeRestrictions, Locale locale, int maxResults) {
		List<IResult> ret = new ArrayList<IResult>();
		query = query.replaceAll("\\\\", "");
		if ("".equals(query) || "*".equals(query))
			if (!typeRestrictions.isEmpty()) {
				if (typeRestrictions.size() == 1)
					return getSortedInstances(typeRestrictions.iterator().next(), locale, 0, maxResults);
				int max = 0;
				for (String type : typeRestrictions) {
					IResults tmp = getSortedInstances(type, locale, 0, maxResults);
					max += tmp.getSize();
					int toAdd = maxResults - ret.size();
					if (tmp.getSize() < toAdd) toAdd = tmp.getSize();
					Iterator<IResult> iri = tmp.iterator();
					for (int i = 0; i < toAdd; i++)
						ret.add(iri.next());
				}
				return new Results(ret, max);
			} else return new Results(ret, 0);
		String actualQuery;
		if (query.endsWith(";"))
			actualQuery = '"' + query.substring(0, query.length() - 1) + '"';
		else try {
			TokenStream ts = new ASCIIFoldingFilterWithFinnishExceptions(analyzer.tokenStream("", new StringReader(query)));
			CharTermAttribute token = ts.addAttribute(CharTermAttribute.class);
			StringBuilder sb = new StringBuilder();
			ts.reset();
			while (ts.incrementToken()) {
				sb.append('+');
				sb.append(token.toString());
				sb.append("* ");
			}
			ts.close();
			sb.setLength(sb.length() - 1);
			actualQuery = sb.toString();
		} catch (IOException e) {
			throw new IOError(e);
		}
		ParameterizedSparqlString q;
		if (typeRestrictions == topSearchMarker)
			q = new ParameterizedSparqlString(sparqlConfigService.getTopStringMatchesQuery(), pm);
		else {
			String[] stringMatchesQuery = sparqlConfigService.getInlineStringMatchesQuery().split("</?typelimit>");
			q = new ParameterizedSparqlString(stringMatchesQuery[0], pm);
			if (typeRestrictions.size() == 1) {
				q.append(stringMatchesQuery[1]);
				q.setIri("typeURI", typeRestrictions.iterator().next());
			} else if (typeRestrictions.size() > 1) {
				Iterator<String> typeIterator = typeRestrictions.iterator();
				for (int i = 0; i < typeRestrictions.size() - 1; i++) {
					q.append("{");
					q.append(stringMatchesQuery[1].replaceAll("\\?typeURI", "?typeURI_" + i));
					q.append("} UNION ");
					q.setIri("typeURI_" + i, typeIterator.next());
				}
				q.append("{");
				q.append(stringMatchesQuery[1]);
				q.append("}");
				q.setIri("typeURI", typeIterator.next());
			}
			q.append(stringMatchesQuery[2]);
		}
		q.setLiteral("query", actualQuery);
		String lstring = locale.toString();
		q.setLiteral("lang", lstring);
		q.setLiteral("limit", maxResults);
		ResultSet rs = execSelect(q.asQuery());
		Map<RDFNode, Literal> bestItemLabel = new HashMap<RDFNode, Literal>();
		Map<RDFNode, Set<RDFNode>> itemTypes = new HashMap<RDFNode, Set<RDFNode>>();
		Map<RDFNode, Literal> bestItemTypeLabel = new HashMap<RDFNode, Literal>();
		Map<RDFNode, Literal> bestPropertyLabel = new HashMap<RDFNode, Literal>();
		Map<RDFNode, Set<Pair<RDFNode, RDFNode>>> propertyObjectMap = new HashMap<RDFNode, Set<Pair<RDFNode, RDFNode>>>();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode item = qs.get("item");
			maybeUpdate(qs, "itemLabel", item, bestItemLabel, lstring, true);
			if (qs.contains("itemType")) {
				RDFNode itemType = qs.get("itemType");
				Set<RDFNode> col = itemTypes.get(item);
				if (col == null) {
					col = new HashSet<RDFNode>();
					itemTypes.put(item, col);
				}
				col.add(itemType);
				maybeUpdate(qs, "itemTypeLabel", itemType, bestItemTypeLabel, lstring, true);
			}
			RDFNode property = qs.get("property");
			maybeUpdate(qs, "propertyLabel", property, bestPropertyLabel, lstring, true);
			Set<Pair<RDFNode, RDFNode>> col = propertyObjectMap.get(item);
			if (col == null) {
				col = new HashSet<Pair<RDFNode, RDFNode>>();
				propertyObjectMap.put(item, col);
			}
			col.add(new Pair<RDFNode, RDFNode>(property, qs.get("object")));
		}
		if (bestItemLabel.size() < maxResults) maxResults = bestItemLabel.size();
		Iterator<Entry<RDFNode, Literal>> itemIterator = bestItemLabel.entrySet().iterator();
		try {
			Highlighter h = new Highlighter(new QueryScorer(qp.parse(actualQuery)));

			for (int i = 0; i < maxResults; i++) {
				Entry<RDFNode, Literal> e = itemIterator.next();
				RDFNode item = e.getKey();
				String itemLabel = e.getValue().getString();
				TokenStream ts = new ASCIIFoldingFilterWithFinnishExceptions(analyzer.tokenStream("", new StringReader(itemLabel)));
				String hlstring = h.getBestFragment(ts, itemLabel);
				if (hlstring == null) hlstring = itemLabel;
				Collection<RDFNode> typesForItem = itemTypes.get(item);
				if (typesForItem != null) {
					StringBuilder itemTypesSB = new StringBuilder();
					itemTypesSB.append(" (");
					for (RDFNode type : typesForItem) {
						itemTypesSB.append(bestItemTypeLabel.get(type).getString());
						itemTypesSB.append(", ");
					}
					itemTypesSB.setLength(itemTypesSB.length() - 2);
					itemTypesSB.append(')');
					hlstring = hlstring + itemTypesSB.toString();
				}
				Result r = new Result(e.getKey().toString(), hlstring);
				Collection<Pair<RDFNode, RDFNode>> col = propertyObjectMap.get(item);
				for (Pair<RDFNode, RDFNode> po : col)
					if (po.getRight().isLiteral()) {
						String lit = po.getRight().asLiteral().getString();
						if (!lit.equals(itemLabel)) {
							ts = new ASCIIFoldingFilterWithFinnishExceptions(analyzer.tokenStream("", new StringReader(lit)));
							hlstring = h.getBestFragment(ts, lit);
							r.getAltLabels().add(bestPropertyLabel.get(po.getLeft()).getString() + ": " + hlstring);
						}
					}
				ret.add(r);
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new IOError(e);
		} catch (InvalidTokenOffsetsException e) {
			throw new RuntimeException(e);
		}
		return new Results(ret, maxResults);
	}

	@Override
	public IResults getSortedInstances(String type, Locale locale, int from, int to) {
		ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getInstanceQuery(), pm);
		q.setIri("type", type);
		String lstring = locale.toString();
		q.setLiteral("lang", lstring);
		ResultSet rs = execSelect(q.asQuery());
		LinkedHashSet<Resource> rets = new LinkedHashSet<Resource>();
		Map<Resource, Literal> rl = new HashMap<Resource, Literal>();
		List<IResult> ret = new ArrayList<IResult>();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			Resource r = qs.get("item").asResource();
			Literal cl = rl.get(r);
			if (qs.contains("label")) {
				Literal pl = qs.get("label").asLiteral();
				if (cl == null || r.getURI().equals(cl.getString()) || lstring.equals(pl.getLanguage())) {
					rets.remove(r);
					rets.add(r);
					rl.put(r, pl);
				} else if ("".equals(pl.getLanguage()) && !lstring.equals(cl.getLanguage())) {
					rets.remove(r);
					rets.add(r);
					rl.put(r, pl);
				}
			} else if (cl == null) {
				rets.add(r);
				if (r.isURIResource())
					rl.put(r, ResourceFactory.createPlainLiteral(r.getURI()));
				else rl.put(r, ResourceFactory.createPlainLiteral(r.getId().getLabelString()));
			}
		}
		Iterator<Resource> ti = rets.iterator();
		for (int i = 0; i < from; i++)
			ti.next();
		for (int i = from; i < to; i++) {
			if (!ti.hasNext()) return new Results(ret, i);
			Resource r = ti.next();
			ret.add(new Result(r.getURI(), rl.get(r).getString()));
		}
		return new Results(ret, rets.size());
	}

	private final class SahaSPARQLResultProperty implements ISahaProperty {

		private final Locale locale;

		@Override
		public String toString() {
			return propertyLabel + value + "/" + valueTypeLabel + ranges;
		}

		@SuppressWarnings("unchecked")
		public SahaSPARQLResultProperty(Resource propertyNode, String propertyLabel, UriLabel value, String valueType,
				String valueTypeLabel, String comment, boolean isLiteral, List<UriLabel> ranges, Locale locale) {
			this.propertyNode = propertyNode;
			this.value = value;
			this.propertyLabel = propertyLabel;
			this.isLiteral = isLiteral;
			this.valueType = valueType;
			this.valueTypeLabel = valueTypeLabel;
			this.comment = comment != null ? comment : "";
			this.ranges = ranges != null ? ranges : Collections.EMPTY_LIST;
			this.locale = locale;
		}

		private final UriLabel value;
		private final Resource propertyNode;
		private final String propertyLabel;

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
			return value.getUri();
		}

		private final String valueType;

		@Override
		public String getValueTypeUri() {
			return valueType;
		}

		@Override
		public String getValueDatatypeUri() {
			return value.getDatatype();
		}

		private final String valueTypeLabel;

		@Override
		public String getValueTypeLabel() {
			return valueTypeLabel;
		}

		@Override
		public String getValueShaHex() {
			return value.getShaHex();
		}

		@Override
		public String getValueLang() {
			return value.getLang();
		}

		@Override
		public String getValueLabel() {
			return value.getLabel();
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

		private final List<UriLabel> ranges;

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

		@Override
		public Set<UITreeNode> getRangeTree() {
			Map<Resource, UITreeNode> nodes = new HashMap<Resource, UITreeNode>();
			ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getPropertyTreeQuery(), pm);
			String lstring = locale.toString();
			q.setLiteral("lang", lstring);
			q.setIri("propertyURI", propertyNode.getURI());
			Model m = execConstruct(q.asQuery());
			ResIterator ri = m.listSubjects();
			while (ri.hasNext()) {
				Resource r = ri.next();
				int count = 0;
				Statement s = r.getProperty(cp);
				if (s != null) count = s.getInt();
				Literal labelLit = null;
				if (!r.hasProperty(RDFS.label))
					labelLit = ResourceFactory.createPlainLiteral(r.getURI());
				else {
					StmtIterator sti = r.listProperties(RDFS.label);
					while (sti.hasNext()) {
						Literal candidate = sti.next().getLiteral();
						if (labelLit == null)
							labelLit = candidate;
						else if (lstring.equals(candidate.getLanguage()) || "".equals(labelLit.getLanguage()) && !labelLit.getLanguage().equals(lstring))
							labelLit = candidate;
					}
				}
				nodes.put(r, new UITreeNode(r.getURI(), labelLit.getString(), count));
			}
			StmtIterator sti = m.listStatements(null, RDFS.subClassOf, (RDFNode) null);
			while (sti.hasNext()) {
				Statement s = sti.next();
				UITreeNode child = nodes.get(s.getSubject());
				UITreeNode parent = nodes.get(s.getResource());
				if (parent == null) {
					parent = new UITreeNode(s.getResource().isURIResource() ? s.getResource().getURI() : s.getResource().getId().getLabelString(), s.getResource().isURIResource() ? s.getResource().getURI() : s.getResource().getId().getLabelString(), 0);
					nodes.put(s.getResource(), parent);
				}
				parent.addChild(child);
			}
			sti = m.listStatements(null, RDFS.range, (RDFNode) null);
			Set<UITreeNode> roots = new HashSet<UITreeNode>();
			while (sti.hasNext()) {
				Statement pl = sti.next();
				roots.add(nodes.get(pl.getResource()));
			}
			return roots;
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
					ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getLabelQuery(), pm);
					q.setIri("uri", resourceUri);
					String lstring = locale.toString();
					q.setLiteral("lang", lstring);
					ResultSet rs = execSelect(q.asQuery());
					if (!rs.hasNext())
						label = resourceUri;
					else while (rs.hasNext()) {
						Literal l = rs.next().get("label").asLiteral();
						if (lstring.equals(l.getLanguage())) {
							label = l.getString();
							break;
						} else if ("".equals(l.getLanguage()))
							label = l.getString();
						else if (label == null) label = l.getString();
					}
				}
				return label;
			}

			private List<UriLabel> types;

			@Override
			public List<UriLabel> getTypes() {
				if (types == null) {
					types = new ArrayList<UriLabel>();
					ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getTypesQuery(), pm);
					q.setIri("uri", resourceUri);
					String lstring = locale.toString();
					q.setLiteral("lang", lstring);
					ResultSet rs = execSelect(q.asQuery());
					Map<RDFNode, Literal> best = new HashMap<RDFNode, Literal>();
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						RDFNode ct = qs.get("type");
						maybeUpdate(qs, "label", ct, best, lstring, true);
					}
					for (Entry<RDFNode, Literal> e : best.entrySet())
						types.add(new UriLabel(e.getKey().asResource().getURI(), e.getValue().getString()));
				}
				return types;
			}

			private List<ISahaProperty> properties;

			@Override
			public List<ISahaProperty> getProperties() {
				if (properties == null) {
					properties = new ArrayList<ISahaProperty>();
					ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getPropertiesQuery(), pm);
					q.setIri("uri", resourceUri);
					String lstring = locale.toString();
					q.setLiteral("lang", lstring);
					ResultSet rs = execSelect(q.asQuery());
					Map<RDFNode, Literal> bestpl = new HashMap<RDFNode, Literal>();
					Map<RDFNode, Literal> bestol = new HashMap<RDFNode, Literal>();
					Map<RDFNode, Boolean> pIsLiteral = new HashMap<RDFNode, Boolean>();
					Set<Pair<Resource, RDFNode>> po = new HashSet<Pair<Resource, RDFNode>>();
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						RDFNode objectNode = qs.get("object");
						Resource property = qs.get("propertyURI").asResource();
						po.add(new Pair<Resource, RDFNode>(property, objectNode));
						maybeUpdate(qs, "propertyLabel", property, bestpl, lstring, true);
						maybeUpdate(qs, "objectLabel", objectNode, bestol, lstring, true);
						pIsLiteral.put(property, objectNode.isLiteral());
					}
					for (Pair<Resource, RDFNode> p : po) {
						UriLabel label;
						if (p.getRight().isLiteral())
							label = new UriLabel(p.getRight().asLiteral());
						else if (p.getRight().isURIResource())
							label = new UriLabel(p.getRight().asResource().getURI(), bestol.get(p.getRight()).getString());
						else label = new UriLabel(p.getRight().asResource().getId().getLabelString(), bestol.get(p.getRight()).getString());
						properties.add(new SahaSPARQLResultProperty(p.getLeft(), bestpl.get(p.getLeft()).getString(), label, null, null, null, pIsLiteral.get(p.getLeft()), null, locale));
					}
				}
				return properties;
			}

			private List<ISahaProperty> inverseProperties;

			@Override
			public List<ISahaProperty> getInverseProperties() {
				if (inverseProperties == null) {
					inverseProperties = new ArrayList<ISahaProperty>();
					ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getInversePropertiesQuery(), pm);
					q.setIri("uri", resourceUri);
					String lstring = locale.toString();
					q.setLiteral("lang", lstring);
					ResultSet rs = execSelect(q.asQuery());
					Map<RDFNode, Literal> bestpl = new HashMap<RDFNode, Literal>();
					Map<RDFNode, Literal> bestol = new HashMap<RDFNode, Literal>();
					Map<Resource, Resource> otype = new HashMap<Resource, Resource>();
					Map<RDFNode, Literal> bestotl = new HashMap<RDFNode, Literal>();
					Map<Resource, Boolean> pIsLiteral = new HashMap<Resource, Boolean>();
					Set<Pair<Resource, Resource>> po = new HashSet<Pair<Resource, Resource>>();
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						Resource objectNode = qs.get("object").asResource();
						Resource property = qs.get("propertyURI").asResource();
						po.add(new Pair<Resource, Resource>(property, objectNode));
						maybeUpdate(qs, "propertyLabel", property, bestpl, lstring, true);
						maybeUpdate(qs, "objectLabel", objectNode, bestol, lstring, true);
						pIsLiteral.put(property, objectNode.isLiteral());
						if (qs.contains("objectType")) {
							otype.put(objectNode, qs.get("objectType").asResource());
							maybeUpdate(qs, "objectTypeLabel", qs.get("objectType"), bestotl, lstring, true);
						}
					}
					for (Pair<Resource, Resource> p : po) {
						UriLabel label;
						if (p.getRight().isLiteral())
							label = new UriLabel(p.getRight().asLiteral());
						else if (p.getRight().isURIResource())
							label = new UriLabel(p.getRight().asResource().getURI(), bestol.get(p.getRight()).getString());
						else label = new UriLabel(p.getRight().asResource().getId().getLabelString(), bestol.get(p.getRight()).getString());
						inverseProperties.add(new SahaSPARQLResultProperty(p.getLeft(), bestpl.get(p.getLeft()).getString(), label, otype.containsKey(p.getRight()) ? otype.get(p.getRight()).getURI() : null, otype.containsKey(p.getRight()) ? bestotl.get(otype.get(p.getRight())).getString() : null, null, pIsLiteral.get(p.getLeft()), null, locale));
					}
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
					editorProperties = new HashSet<ISahaProperty>();
					ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getEditorPropertiesQuery(), pm);
					q.setIri("uri", resourceUri);
					String lstring = locale.toString();
					q.setLiteral("lang", lstring);
					ResultSet rs = execSelect(q.asQuery());
					Map<RDFNode, Literal> bestpl = new HashMap<RDFNode, Literal>();
					Map<RDFNode, Literal> bestol = new HashMap<RDFNode, Literal>();
					Map<RDFNode, Literal> bestpcomment = new HashMap<RDFNode, Literal>();
					Map<Resource, Boolean> pIsLiteral = new HashMap<Resource, Boolean>();
					Map<Resource, Set<Resource>> pranges = new HashMap<Resource, Set<Resource>>();
					Map<RDFNode, Literal> bestprangel = new HashMap<RDFNode, Literal>();
					Set<Pair<Resource, RDFNode>> po = new HashSet<Pair<Resource, RDFNode>>();
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						RDFNode objectNode = qs.get("object");
						Resource property = qs.get("propertyURI").asResource();
						po.add(new Pair<Resource, RDFNode>(property, objectNode));
						maybeUpdate(qs, "propertyLabel", property, bestpl, lstring, true);
						if (objectNode != null) {
							maybeUpdate(qs, "objectLabel", objectNode, bestol, lstring, true);
							pIsLiteral.put(property, objectNode.isLiteral());
						} else if (qs.contains("propertyType"))
							pIsLiteral.put(property, qs.get("propertyType").equals(OWL.DatatypeProperty));
						else if (!pIsLiteral.containsKey(property)) pIsLiteral.put(property, false);
						if (qs.contains("propertyComment"))
							maybeUpdate(qs, "propertyComment", property, bestpcomment, lstring, false);
						if (qs.contains("propertyRangeURI") && qs.get("propertyRangeURI").isResource()) {
							Set<Resource> cpranges = pranges.get(property);
							if (cpranges == null) {
								cpranges = new HashSet<Resource>();
								pranges.put(property, cpranges);
							}
							Resource rr = qs.get("propertyRangeURI").asResource();
							cpranges.add(rr);
							maybeUpdate(qs, "propertyRangeLabel", rr, bestprangel, lstring, true);
						}
					}
					editorProperties = new HashSet<ISahaProperty>(po.size());
					Map<Resource, List<UriLabel>> ranges = new HashMap<Resource, List<UriLabel>>();
					for (Map.Entry<Resource, Set<Resource>> rr : pranges.entrySet()) {
						List<UriLabel> ul = new ArrayList<UriLabel>(rr.getValue().size());
						for (Resource rtr : rr.getValue())
							ul.add(new UriLabel(rtr.getURI(), bestprangel.get(rtr).getLanguage(), bestprangel.get(rtr).getString()));
						ranges.put(rr.getKey(), ul);
					}
					for (Pair<Resource, RDFNode> p : po) {
						UriLabel label;
						if (p.getRight() == null)
							label = new UriLabel();
						else if (p.getRight().isLiteral())
							label = new UriLabel(p.getRight().asLiteral());
						else if (p.getRight().isURIResource())
							label = new UriLabel(p.getRight().asResource().getURI(), bestol.get(p.getRight()).getString());
						else label = new UriLabel(p.getRight().asResource().getId().getLabelString(), bestol.get(p.getRight()).getString());
						editorProperties.add(new SahaSPARQLResultProperty(p.getLeft(), bestpl.get(p.getLeft()).getString(), label, null, null, bestpcomment.containsKey(p.getLeft()) ? bestpcomment.get(p.getLeft()).getString() : null, pIsLiteral.get(p.getLeft()), ranges.get(p.getLeft()), locale));
					}
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
		return execConstruct(QueryFactory.create(sparqlConfigService.getWholeModelQuery()));
	}

	private ResultSet execSelect(Query query) {
		QueryEngineHTTP qe = QueryExecutionFactory.createServiceRequest(sparqlConfigService.getSparqlURL(), query);
		qe.setSelectContentType(WebContent.contentTypeTextTSV);
		if (sparqlConfigService.getGraphURI() != null) qe.addDefaultGraph(sparqlConfigService.getGraphURI());
		return qe.execSelect();
	}

	private Model execConstruct(Query query) {
		QueryEngineHTTP qe = QueryExecutionFactory.createServiceRequest(sparqlConfigService.getSparqlURL(), query);
		qe.setModelContentType("text/turtle");
		if (sparqlConfigService.getGraphURI() != null) qe.addDefaultGraph(sparqlConfigService.getGraphURI());
		return qe.execConstruct();
	}

	private Model execDescribe(Query query) {
		if (sparqlConfigService.getGraphURI() != null)
			return QueryExecutionFactory.sparqlService(sparqlConfigService.getSparqlURL(), query, sparqlConfigService.getGraphURI()).execDescribe();
		return QueryExecutionFactory.sparqlService(sparqlConfigService.getSparqlURL(), query).execDescribe();
	}

	private static final Property cp = ResourceFactory.createProperty(RDF.getURI() + "count");

	@Override
	public Set<UITreeNode> getClassTree(Locale locale) {
		Map<Resource, UITreeNode> nodes = new HashMap<Resource, UITreeNode>();
		ParameterizedSparqlString q = new ParameterizedSparqlString(sparqlConfigService.getClassTreeQuery(), pm);
		String lstring = locale.toString();
		q.setLiteral("lang", lstring);
		Model m = execConstruct(q.asQuery());
		ExtendedIterator<RDFNode> ri = m.listObjects().andThen(m.listSubjects());
		while (ri.hasNext()) {
			RDFNode rn = ri.next();
			if (!rn.isURIResource()) continue;
			Resource r = rn.asResource();
			if (nodes.containsKey(r)) continue;
			int count = 0;
			Statement s = r.getProperty(cp);
			if (s != null) count = s.getInt();
			String label = r.getURI();
			StmtIterator sti = r.listProperties(RDFS.label);
			Literal bl = null;
			while (sti.hasNext()) {
				Literal o = sti.next().getObject().asLiteral();
				if (bl == null || lstring.equals(o.getLanguage()))
					bl = o;
				else if (!lstring.equals(bl.getLanguage())) bl = o;
			}
			if (bl != null) label = bl.getString();
			nodes.put(r, new UITreeNode(r.getURI(), label, count));
		}
		Set<UITreeNode> roots = new HashSet<UITreeNode>(nodes.values());
		StmtIterator sti = m.listStatements(null, RDFS.subClassOf, (RDFNode) null);
		while (sti.hasNext()) {
			Statement s = sti.next();
			UITreeNode child = nodes.get(s.getSubject());
			if (child == null) continue;
			UITreeNode uitn = nodes.get(s.getResource());
			if (uitn == null) continue;
			uitn.addChild(child);
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
