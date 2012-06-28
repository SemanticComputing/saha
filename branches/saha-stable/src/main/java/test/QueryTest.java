package test;

import java.util.Locale;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.IResults;
import fi.seco.saha3.model.SahaProject;

public class QueryTest {

	public static void main(String[] args) {
		SahaProjectRegistry r = new SahaProjectRegistry();
		r.setProjectBaseDirectory("/common/group/finnonto/saha3/");
		SahaProject project = r.getSahaProject("kaunokki_saha_old");
		
		BooleanQuery query = new BooleanQuery();
		query.add(new TermQuery(new Term(RDF.type.getURI(),"http://www.yso.fi/onto/kaunokki#teos")),Occur.MUST);
		query.add(new PrefixQuery(new Term(RDFS.label.getURI(),"")),Occur.MUST_NOT);
		
		for (IResults.IResult result : project.getSortedInstances(query,new Locale("fi"),0,20))
			System.out.println(result.getLabel());
		
	}
	
}
