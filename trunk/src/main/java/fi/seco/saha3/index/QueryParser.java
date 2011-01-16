package fi.seco.saha3.index;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import com.hp.hpl.jena.vocabulary.RDF;

public class QueryParser {

	private final Logger log = Logger.getLogger(getClass());
	private Analyzer analyzer;
	
	public QueryParser(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	public void addLabelQuery(BooleanQuery query, String label) {
		if (label != null && !label.isEmpty())
			for (String token : tokenizeQuery(label))
				query.add(new PrefixQuery(new Term(ResourceIndex.LABEL_FIELD_NAME,token)),Occur.MUST);
	}
	
	public void addUberQuery(BooleanQuery query, String queryString) {
		if (queryString != null && !queryString.isEmpty())
			for (String token : tokenizeQuery(queryString)) {
				if (queryString.endsWith(";")) {
					query.add(new TermQuery(new Term(ResourceIndex.UBER_FIELD_NAME,token)),Occur.MUST);
					query.add(new TermQuery(new Term(ResourceIndex.LABEL_FIELD_NAME,token)),Occur.SHOULD);
				} else {
					query.add(new PrefixQuery(new Term(ResourceIndex.UBER_FIELD_NAME,token)),Occur.MUST);
					query.add(new PrefixQuery(new Term(ResourceIndex.LABEL_FIELD_NAME,token)),Occur.SHOULD);
				}
			}
	}
	
	public void addFacetQuery(BooleanQuery query, Map<String,List<String>> queryMap) {
		if (queryMap != null && !queryMap.isEmpty())
			for (Map.Entry<String,List<String>> entry : queryMap.entrySet())
				for (String value : entry.getValue())
					if (!entry.getKey().equals(ResourceIndex.LABEL_FIELD_NAME) && !entry.getKey().equals(ResourceIndex.UBER_FIELD_NAME))
						query.add(new TermQuery(new Term(entry.getKey()+ResourceIndex.URI_FIELD,value)),
								BooleanClause.Occur.MUST);
	}
	
	public void addStrictTypeQuery(BooleanQuery query, String type) {
		if (type != null && !type.isEmpty())
			query.add(new TermQuery(new Term(ResourceIndex.TYPE_FIELD_NAME,type)),Occur.MUST);
	}
	
	public void addTypeQuery(BooleanQuery query, Collection<String> types) {
		if (types != null && !types.isEmpty()) {
			if (types.size() == 1)
				query.add(new TermQuery(new Term(RDF.type.getURI()+
						ResourceIndex.URI_FIELD,types.iterator().next())),Occur.MUST);
			else {
				BooleanQuery typeQuery = new BooleanQuery();
				for (String type : types)
					typeQuery.add(new TermQuery(new Term(RDF.type.getURI()+
							ResourceIndex.URI_FIELD,type)),Occur.SHOULD);
				query.add(typeQuery,Occur.MUST);
			}
		}
	}
	
	private List<String> tokenizeQuery(String query) {
		List<String> tokens = new ArrayList<String>();
		if (query != null) {
			TokenStream stream = analyzer.tokenStream(null,new StringReader(query));
			try {
				while (stream.incrementToken()) {
					TermAttribute token = (TermAttribute)stream.addAttribute(TermAttribute.class);
					tokens.add(token.term());
				}
			} catch (IOException e) {
				log.error("",e);
			}
		}
		return tokens;
	}
	
}
