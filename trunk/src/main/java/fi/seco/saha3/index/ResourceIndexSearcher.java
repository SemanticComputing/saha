package fi.seco.saha3.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;

import fi.seco.saha3.index.ResourceIndex.Lock;
import fi.seco.saha3.model.IResults;

public class ResourceIndexSearcher {
	
	public class Results implements IResults {
		private ScoreDoc[] hits;
		private int i;
		private int totalHits;
		private Locale locale;
		private Lock lock;
		
		protected Results(ScoreDoc[] hits, int totalHits, Locale locale, int from, Lock lock) {
			this.hits = hits;
			this.totalHits = totalHits;
			this.locale = locale;
			this.i = from;
			this.lock = lock;
			if (i >= hits.length)
				lock.releaseLock();
		}
		
		public int getSize() {
			return totalHits;
		}
		
		public Locale getLocale() {
			return locale;
		}
		
		public Iterator<IResults.IResult> iterator() {
			return new Iterator<IResults.IResult>() {
				public boolean hasNext() {
					return i < hits.length;
				}
				public IResults.IResult next() {
					if (hasNext()) {
						try {
							IResults.IResult result = buildResult(index.getSearcher().doc(hits[i++].doc));
							if (!hasNext()) lock.releaseLock();
							return result;
						} catch (Exception e) {
							lock.releaseLock();
							log.error("",e);
						}
						return null;
					}
					throw new NoSuchElementException();
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
		protected IResults.IResult buildResult(Document document) throws Exception {
			return new IResults.Result(document.get("uri"),parseLabel(document));
		}
		
		protected String parseLabel(Document document) {
			String label = document.get(ResourceIndex.LABEL_FIELD_NAME + "_" + locale.getLanguage());
			if (label == null) label = document.get(ResourceIndex.LABEL_FIELD_NAME);
			if (label == null) label = document.get("uri");
			return label;
		}
	}
	
	public class HighlightedResults extends Results {
		private Highlighter hiliter;
		
		protected HighlightedResults(ScoreDoc[] hits, int totalHits, Query query, Locale locale, int from, Lock readLock) {
			super(hits,totalHits,locale,from,readLock);
			this.hiliter = new Highlighter(new QueryScorer(query));
		}
		
		@Override
		protected IResults.IResult buildResult(Document document) throws Exception {
			String uri = document.get("uri");
			String label = parseHighlightedLabel(document)+parseTypeLabels(uri);
			List<String> altLabels = parseMatchingFields(document);
			return new IResults.Result(uri,label,altLabels);
		}
		
		private String parseHighlightedLabel(Document document) throws Exception {
			String label = parseLabel(document);
			String name = ResourceIndex.LABEL_FIELD_NAME + "_" + getLocale().getLanguage();
			String frag = hiliter.getBestFragment(index.getAnalyzer(),name,label);
			if (frag != null) label = frag;
			return label;
		}
		
		private String parseTypeLabels(String uri) {
			List<String> typeLabels = getTypeLabels(uri,getLocale());
			StringBuilder buffer = new StringBuilder();
			if (!typeLabels.isEmpty()) {
				Iterator<String> iter = typeLabels.iterator();
				buffer.append(" (");
				while (iter.hasNext()) {
					buffer.append(iter.next());
					if (iter.hasNext()) buffer.append(", ");
				}
				buffer.append(")");
			}
			return buffer.toString();
		}
		
		private List<String> parseMatchingFields(Document document) throws Exception {
			List<String> altLabels = new ArrayList<String>();
			List<Fieldable> fields = document.getFields();
			for (Fieldable field : fields) {
				String name = field.name();
				if (!name.equals("uri") &&
					!name.equals(ResourceIndex.UBER_FIELD_NAME) && 
					!name.equals(ResourceIndex.TYPE_FIELD_NAME) &&
					!name.startsWith(ResourceIndex.LABEL_FIELD_NAME)) 
				{
					String[] fragments = hiliter.getBestFragments(index.getAnalyzer(),name,field.stringValue(),5);
					if (fragments.length > 0) {
						StringBuilder buffer = new StringBuilder();
						buffer.append(getLabel(name,getLocale()) + ": ");
						for (int i=0;i<fragments.length;i++) {
							String frag = fragments[i];
							frag = frag.length() > 120 ? frag.substring(0,120) + "..." : frag;
							buffer.append(frag);
							if (i+1<fragments.length) buffer.append("<br/>&nbsp;&nbsp;");
						}		
						altLabels.add(buffer.toString());
					}
				}
			}
			return altLabels;
		}
	}

	private final Logger log = Logger.getLogger(getClass());
	
	private ResourceIndex index;
	private QueryParser parser;
	
	public ResourceIndexSearcher(ResourceIndex index) {
		this.index = index;
		this.parser = new QueryParser(index.getAnalyzer());
	}

	public QueryParser getParser() {
		return parser;
	}
	
	public String getLabel(String uri, Locale locale) {
		return index.getLabel(uri).getMatchAny(locale);
	}
	
	public String[] getTypeUris(String uri) {
		return index.getTypeUris(uri);
	}
	
	public String[] getTransitiveTypeUris(String uri) {
		return index.getTransitiveTypeUris(uri);
	}
	
	public String getTypeUri(String uri) {
		String[] typeUris = getTypeUris(uri);
		return typeUris.length > 0 ? typeUris[0] : "";
	}
	
	public List<String> getTypeLabels(String uri, Locale locale) {
		List<String> labels = new ArrayList<String>();
		for (String typeUri : getTypeUris(uri))
			labels.add(getLabel(typeUri,locale));
		return labels;
	}
	
	public String[] getAncestors(String uri) {
		return index.getAncestors(uri);
	}
	
	public Set<String> getAllAncestors(String uri) {
		return index.getAllAncestors(uri);
	}
	
	public Set<String> getAllDescendants(String uri) {
		return index.getAllDescendants(uri);
	}
	
	public IResults searchLabel(String label, String type, Locale locale, int from, int to, boolean sort) {
		BooleanQuery query = new BooleanQuery();
		parser.addLabelQuery(query,label);
		parser.addStrictTypeQuery(query,type);
		return search(query,locale,from,to,sort);
	}
	
	public IResults search(String queryString, Collection<String> types, Locale locale, int from, int to) {
		BooleanQuery query = new BooleanQuery();
		if ((queryString != null && queryString.equals("*")) && (types == null || types.isEmpty()))
			query.add(new MatchAllDocsQuery(),Occur.MUST);
		else {
			parser.addUberQuery(query,queryString);
			parser.addTypeQuery(query,types);
		}
		return search(query,locale,from,to,false);
	}
	
	public IResults search(Map<String,List<String>> queryMap, List<String> types, Locale locale, int from, int to, boolean sort) {
		BooleanQuery query = new BooleanQuery();
		
		String label = null;
		List<String> terms = queryMap.get(ResourceIndex.UBER_FIELD_NAME);
		if (terms != null && !terms.isEmpty()) label = terms.get(0);
			
		parser.addUberQuery(query,label);
		parser.addTypeQuery(query,types);
		parser.addFacetQuery(query,queryMap);

		return search(query,locale,from,to,sort);
	}
	
	public IResults search(Query query, Locale locale, int from, int to, boolean sort) {
		Lock lock = index.acquireLock();
		try {
			return searchIndex(query,locale,from,to,sort,lock);
		} catch (IOException e) {
			log.error("",e);
		} finally {
			lock.releaseLock();
		}
		return null;
	}
	
	private IResults searchIndex(Query query, Locale locale, int from, int to, boolean sort, Lock lock) throws IOException {
		TopDocs hits = null;
		if (sort && index.getAcceptedLocales().contains(locale))
			hits = index.getSearcher().search(query,null,to,
					new Sort(new SortField(ResourceIndex.LABEL_FIELD_NAME+"_"+locale.getLanguage(),locale)));
		else
			hits = index.getSearcher().search(query,to);
		return new HighlightedResults(hits.scoreDocs,hits.totalHits,query,locale,from,lock);
	}

	public int getInstanceCount(String type) {
		return getCount(new TermQuery(new Term(ResourceIndex.TYPE_FIELD_NAME,type)));
	}
	
	public int getCount(Query query) {
		Lock lock = index.acquireLock();
		try {
			TopDocs hits = index.getSearcher().search(query,1);
			return hits.totalHits;
		} catch (IOException e) {
			log.error("",e);
			return -1;
		} finally {
			lock.releaseLock();
		}
	}
	
	public void reindex()
	{
	    this.index.reindex();
	}
	
}
