package test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

public class DocumentEqualsTest {

	public static void main(String[] args) {
		String uri = "http://foo.com";
		String data1 = "hello";
		String data2 = "hi";
		
		Document d1 = new Document();
		d1.add(new Field("uri",uri,Field.Store.YES,Field.Index.NOT_ANALYZED));
		d1.add(new Field("data",data1,Field.Store.YES,Field.Index.ANALYZED));
		d1.add(new Field("data",data2,Field.Store.YES,Field.Index.ANALYZED));
		
		Document d2 = new Document();
		d2.add(new Field("uri",uri,Field.Store.YES,Field.Index.NOT_ANALYZED));
		d2.add(new Field("data",data2,Field.Store.YES,Field.Index.ANALYZED));
		d2.add(new Field("data",data1,Field.Store.YES,Field.Index.ANALYZED));
		
		// uses object's equals, returns false
		System.out.println(d1.equals(d2));
		
		List<Fieldable> fields1 = d1.getFields();
		
		Set<String> set1 = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
		
		for (Fieldable f1 : fields1) {
			String name = f1.name();
			for (String value : d1.getValues(name)) set1.add(value);	
			for (String value : d2.getValues(name)) set2.add(value);
			if (!set1.equals(set2))
				System.out.println("Documents are not equal.");
			set1.clear();
			set2.clear();
		}
		
		// null?
		Document d3 = new Document();
		System.out.println(d3.getFields());
	}
	
}
