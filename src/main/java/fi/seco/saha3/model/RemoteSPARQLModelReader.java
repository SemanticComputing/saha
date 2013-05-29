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

import org.apache.commons.codec.digest.DigestUtils;
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

	@Override
	public IResults search(String query, Collection<String> parentRestrictions, Collection<String> typeRestrictions,
			Locale locale, int maxResults) {
		List<IResult> ret = new ArrayList<IResult>();
		query = query.replaceAll("\\\\", "");
		if ("".equals(query) || "*".equals(query)) return new Results(ret, 0);
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
		String[] stringMatchesQuery = sparqlConfigService.getStringMatchesQuery().split("</?typelimit>");
		ParameterizedSparqlString q = new ParameterizedSparqlString(stringMatchesQuery[0], pm);
		if (!typeRestrictions.isEmpty()) {
			String[] split = stringMatchesQuery[1].split("\\?typeURIs");
			q.append(split[0]);
			for (String t : typeRestrictions) {
				q.appendIri(t);
				q.append(' ');
			}
			q.append(split[1]);
		}
		q.append(stringMatchesQuery[2]);
		q.setLiteral("query", actualQuery);
		q.setLiteral("lang", locale.toString());
		ResultSet rs = execSelect(q.asQuery());
		Map<RDFNode, Result> rmap = new HashMap<RDFNode, Result>();
		try {
			Highlighter h = new Highlighter(new QueryScorer(qp.parse(actualQuery)));
			while (rs.hasNext()) {
				QuerySolution qs = rs.next();
				Result r = rmap.get(qs.get("item"));
				//FIXME process multiple labels properly
				if (r == null) {
					String label = qs.contains("itemLabel") ? qs.get("itemLabel").asLiteral().getString() : qs.get("item").toString();
					if (qs.contains("itemTypeLabel"))
						label += "(" + qs.get("itemTypeLabel").asLiteral().getString() + ")";
					r = new Result(qs.get("item").toString(), label);
					rmap.put(qs.get("item"), r);
				}
				String hlstring = qs.get("object").asLiteral().getString();
				try {
					TokenStream ts = new ASCIIFoldingFilterWithFinnishExceptions(analyzer.tokenStream("", new StringReader(hlstring)));
					hlstring = h.getBestFragment(ts, hlstring);
				} catch (IOException e) {
					throw new IOError(e);
				} catch (InvalidTokenOffsetsException e) {
					throw new RuntimeException(e);
				}
				if (qs.contains("propertyLabel"))
					r.getAltLabels().add(qs.get("propertyLabel").asLiteral().getString() + ": " + hlstring);
				else r.getAltLabels().add(qs.get("property").asResource().getURI() + ": " + hlstring);
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		if (rmap.size() < maxResults) maxResults = rmap.size();
		Iterator<Result> mi = rmap.values().iterator();
		for (int i = 0; i < maxResults; i++)
			ret.add(mi.next());
		return new Results(ret, maxResults);
	}

	@Override
	public IResults getSortedInstances(String label, String type, Locale locale, int from, int to) {
		if (label == null) {
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
					rl.put(r, ResourceFactory.createPlainLiteral(r.getURI()));
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
		return null;
	}

	private final class SahaSPARQLResultProperty implements ISahaProperty {

		private final Locale locale;

		@SuppressWarnings("unchecked")
		public SahaSPARQLResultProperty(Resource propertyNode, String propertyLabel, RDFNode objectNode,
				String valueLabel, String valueType, String valueTypeLabel, String comment, boolean isLiteral,
				List<UriLabel> ranges, Locale locale) {
			this.propertyNode = propertyNode;
			this.objectNode = objectNode;
			this.propertyLabel = propertyLabel;
			this.valueLabel = valueLabel != null ? valueLabel : "";
			this.isLiteral = isLiteral;
			this.valueType = valueType;
			this.valueTypeLabel = valueTypeLabel;
			this.comment = comment != null ? comment : "";
			this.ranges = ranges != null ? ranges : Collections.EMPTY_LIST;
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
			if (objectNode == null) return "";
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
			if (isLiteral() && objectNode != null) return DigestUtils.sha1Hex(objectNode.asLiteral().toString());
			return DigestUtils.sha1Hex(getValueLabel());
		}

		@Override
		public String getValueLang() {
			return objectNode != null ? objectNode.asLiteral().getLanguage() : "";
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
				if (nodes.get(pl.getResource()) != null) // FIXME: There should not be null resource in the first place 
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
					Map<String, Literal> best = new HashMap<String, Literal>();
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						String ct = qs.get("type").toString();
						Literal cl = best.get(ct);
						if (qs.contains("label")) {
							Literal pl = qs.get("label").asLiteral();
							if (cl == null || ct.equals(cl.getString()) || lstring.equals(pl.getLanguage()))
								best.put(ct, pl);
							else if ("".equals(pl.getLanguage()) && !lstring.equals(cl.getLanguage()))
								best.put(ct, pl);
						} else if (cl == null) best.put(ct, ResourceFactory.createPlainLiteral(ct));

					}
					for (Entry<String, Literal> e : best.entrySet())
						types.add(new UriLabel(e.getKey(), e.getValue().getString()));
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
					Map<Resource, Literal> bestpl = new HashMap<Resource, Literal>();
					Map<RDFNode, Literal> bestol = new HashMap<RDFNode, Literal>();
					Map<Resource, Boolean> pIsLiteral = new HashMap<Resource, Boolean>();
					Set<Pair<Resource, RDFNode>> po = new HashSet<Pair<Resource, RDFNode>>();
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						RDFNode objectNode = qs.get("object");
						Resource property = qs.get("propertyURI").asResource();
						po.add(new Pair<Resource, RDFNode>(property, objectNode));
						Literal cpl = bestpl.get(property);
						if (qs.contains("propertyLabel")) {
							Literal ppl = qs.get("propertyLabel").asLiteral();
							if (cpl == null || property.getURI().equals(cpl.getString()) || lstring.equals(ppl.getLanguage()))
								bestpl.put(property, ppl);
							else if ("".equals(ppl.getLanguage()) && !lstring.equals(cpl.getLanguage()))
								bestpl.put(property, ppl);
						} else if (cpl == null)
							bestpl.put(property, ResourceFactory.createPlainLiteral(property.getURI()));
						Literal col = bestol.get(objectNode);
						if (qs.contains("objectLabel")) {
							Literal pol = qs.get("objectLabel").asLiteral();
							if (col == null || objectNode.asResource().getURI().equals(col.getString()) || lstring.equals(pol.getLanguage()))
								bestol.put(objectNode, pol);
							else if ("".equals(pol.getLanguage()) && !lstring.equals(col.getLanguage()))
								bestol.put(objectNode, pol);
						} else if (col == null)
							if (objectNode.isURIResource())
								bestol.put(objectNode, ResourceFactory.createPlainLiteral(objectNode.asResource().getURI()));
							else if (objectNode.isAnon())
								bestol.put(objectNode, ResourceFactory.createPlainLiteral(objectNode.asResource().getId().getLabelString()));
							else bestol.put(objectNode, objectNode.asLiteral());
						pIsLiteral.put(property, objectNode.isLiteral());
					}
					for (Pair<Resource, RDFNode> p : po)
						properties.add(new SahaSPARQLResultProperty(p.getLeft(), bestpl.get(p.getLeft()).getString(), p.getRight(), p.getRight().isLiteral() ? p.getRight().asLiteral().getString() : bestol.get(p.getRight()).getString(), null, null, null, pIsLiteral.get(p.getLeft()), null, locale));
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
					Map<Resource, Literal> bestpl = new HashMap<Resource, Literal>();
					Map<Resource, Literal> bestol = new HashMap<Resource, Literal>();
					Map<Resource, Resource> otype = new HashMap<Resource, Resource>();
					Map<Resource, Literal> bestotl = new HashMap<Resource, Literal>();
					Map<Resource, Boolean> pIsLiteral = new HashMap<Resource, Boolean>();
					Set<Pair<Resource, Resource>> po = new HashSet<Pair<Resource, Resource>>();
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						Resource objectNode = qs.get("object").asResource();
						Resource property = qs.get("propertyURI").asResource();
						po.add(new Pair<Resource, Resource>(property, objectNode));
						Literal cpl = bestpl.get(property);
						if (qs.contains("propertyLabel")) {
							Literal ppl = qs.get("propertyLabel").asLiteral();
							if (cpl == null || property.getURI().equals(cpl.getString()) || lstring.equals(ppl.getLanguage()))
								bestpl.put(property, ppl);
							else if ("".equals(ppl.getLanguage()) && !lstring.equals(cpl.getLanguage()))
								bestpl.put(property, ppl);
						} else if (cpl == null)
							bestpl.put(property, ResourceFactory.createPlainLiteral(property.getURI()));
						Literal col = bestol.get(objectNode);
						if (qs.contains("objectLabel")) {
							Literal pol = qs.get("objectLabel").asLiteral();
							if (col == null || objectNode.asResource().getURI().equals(col.getString()) || lstring.equals(pol.getLanguage()))
								bestol.put(objectNode, pol);
							else if ("".equals(pol.getLanguage()) && !lstring.equals(col.getLanguage()))
								bestol.put(objectNode, pol);
						} else if (col == null)
							if (objectNode.isURIResource())
								bestol.put(objectNode, ResourceFactory.createPlainLiteral(objectNode.asResource().getURI()));
							else if (objectNode.isAnon())
								bestol.put(objectNode, ResourceFactory.createPlainLiteral(objectNode.asResource().getId().getLabelString()));
							else bestol.put(objectNode, objectNode.asLiteral());
						pIsLiteral.put(property, objectNode.isLiteral());
						if (qs.contains("objectType")) {
							otype.put(objectNode, qs.get("objectType").asResource());
							Literal cotl = bestotl.get(objectNode);
							if (qs.contains("objectTypeLabel")) {
								Literal potl = qs.get("objectTypeLabel").asLiteral();
								if (cotl == null || qs.get("objectType").asResource().getURI().equals(cotl.getString()) || lstring.equals(potl.getLanguage()))
									bestotl.put(objectNode, potl);
								else if ("".equals(potl.getLanguage()) && !lstring.equals(cotl.getLanguage()))
									bestotl.put(objectNode, potl);
							} else if (cotl == null)
								bestotl.put(objectNode, ResourceFactory.createPlainLiteral(qs.get("objectType").asResource().getURI()));
						}
					}
					for (Pair<Resource, Resource> p : po)
						inverseProperties.add(new SahaSPARQLResultProperty(p.getLeft(), bestpl.get(p.getLeft()).getString(), p.getRight(), p.getRight().isLiteral() ? p.getRight().asLiteral().getString() : bestol.get(p.getRight()).getString(), otype.containsKey(p.getRight()) ? otype.get(p.getRight()).getURI() : null, otype.containsKey(p.getRight()) ? bestotl.get(p.getRight()).getString() : null, null, pIsLiteral.get(p.getLeft()), null, locale));
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
					Map<Resource, Literal> bestpl = new HashMap<Resource, Literal>();
					Map<RDFNode, Literal> bestol = new HashMap<RDFNode, Literal>();
					Map<Resource, Literal> bestpcomment = new HashMap<Resource, Literal>();
					Map<Resource, Boolean> pIsLiteral = new HashMap<Resource, Boolean>();
					Map<Resource, Set<Resource>> pranges = new HashMap<Resource, Set<Resource>>();
					Map<Resource, Literal> bestprangel = new HashMap<Resource, Literal>();
					Set<Pair<Resource, RDFNode>> po = new HashSet<Pair<Resource, RDFNode>>();
					while (rs.hasNext()) {
						QuerySolution qs = rs.next();
						RDFNode objectNode = qs.get("object");
						Resource property = qs.get("propertyURI").asResource();
						po.add(new Pair<Resource, RDFNode>(property, objectNode));
						Literal cpl = bestpl.get(property);
						if (qs.contains("propertyLabel")) {
							Literal ppl = qs.get("propertyLabel").asLiteral();
							if (cpl == null || property.getURI().equals(cpl.getString()) || lstring.equals(ppl.getLanguage()))
								bestpl.put(property, ppl);
							else if ("".equals(ppl.getLanguage()) && !lstring.equals(cpl.getLanguage()))
								bestpl.put(property, ppl);
						} else if (cpl == null)
							bestpl.put(property, ResourceFactory.createPlainLiteral(property.getURI()));
						if (objectNode != null) {
							Literal col = bestol.get(objectNode);
							if (qs.contains("objectLabel")) {
								Literal pol = qs.get("objectLabel").asLiteral();
								if (col == null || objectNode.asResource().getURI().equals(col.getString()) || lstring.equals(pol.getLanguage()))
									bestol.put(objectNode, pol);
								else if ("".equals(pol.getLanguage()) && !lstring.equals(col.getLanguage()))
									bestol.put(objectNode, pol);
							} else if (col == null)
								if (objectNode.isURIResource())
									bestol.put(objectNode, ResourceFactory.createPlainLiteral(objectNode.asResource().getURI()));
								else if (objectNode.isAnon())
									bestol.put(objectNode, ResourceFactory.createPlainLiteral(objectNode.asResource().getId().getLabelString()));
								else bestol.put(objectNode, objectNode.asLiteral());
							pIsLiteral.put(property, objectNode.isLiteral());
						} else if (qs.contains("propertyType"))
							pIsLiteral.put(property, qs.get("propertyType").equals(OWL.DatatypeProperty));
						else if (!pIsLiteral.containsKey(property)) pIsLiteral.put(property, false);
						Literal ccl = bestpcomment.get(property);
						if (qs.contains("propertyComment")) {
							Literal pcl = qs.get("propertyComment").asLiteral();
							if (ccl == null || property.asResource().getURI().equals(ccl.getString()) || lstring.equals(pcl.getLanguage()))
								bestpcomment.put(property, pcl);
							else if ("".equals(pcl.getLanguage()) && !lstring.equals(ccl.getLanguage()))
								bestpcomment.put(property, pcl);
						}
						if (qs.contains("propertyRangeURI") && qs.get("propertyRangeURI").isResource()) {
							Set<Resource> cpranges = pranges.get(property);
							if (cpranges == null) {
								cpranges = new HashSet<Resource>();
								pranges.put(property, cpranges);
							}
							Resource rr = qs.get("propertyRangeURI").asResource();
							cpranges.add(rr);
							Literal crl = bestprangel.get(rr);
							if (qs.contains("propertyRangeLabel")) {
								Literal prl = qs.get("propertyRangeLabel").asLiteral();
								if (crl == null || rr.getURI().equals(crl.getString()) || lstring.equals(prl.getLanguage()))
									bestprangel.put(rr, prl);
								else if ("".equals(prl.getLanguage()) && !lstring.equals(crl.getLanguage()))
									bestprangel.put(rr, prl);
							} else if (crl == null)
								bestprangel.put(rr, ResourceFactory.createPlainLiteral(rr.getURI()));
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
					for (Pair<Resource, RDFNode> p : po)
						editorProperties.add(new SahaSPARQLResultProperty(p.getLeft(), bestpl.get(p.getLeft()).getString(), p.getRight(), p.getRight() == null ? null : p.getRight().isLiteral() ? p.getRight().asLiteral().getString() : bestol.get(p.getRight()).getString(), null, null, bestpcomment.containsKey(p.getLeft()) ? bestpcomment.get(p.getLeft()).getString() : null, pIsLiteral.get(p.getLeft()), ranges.get(p.getLeft()), locale));
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
		qe.setSelectContentType(WebContent.contentTypeResultsJSON);
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
