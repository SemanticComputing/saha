package fi.seco.saha3.index;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

public class StandardLatinFilterAnalyzer extends StandardAnalyzer {
	
	public StandardLatinFilterAnalyzer(Set<String> stopWords) {
	    super(Version.LUCENE_30, stopWords);		
	}
	@Override
	public TokenStream reusableTokenStream(String fieldName, Reader reader) {
		return tokenStream(fieldName, reader);
	}
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return new ASCIIFoldingFilter(super.tokenStream(fieldName,reader));
	}
}
