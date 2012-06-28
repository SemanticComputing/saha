package test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;

public class TestTDB {

	public static void main(String[] args) {
		Model m = TDBFactory.createModel("/common/home/jhkurki/Desktop/test_tdb_1");
		
		m.add(m.createResource("r1"),m.createProperty("p1"),"moi");
		m.add(m.createResource("r1"),m.createProperty("p2"),"hei");
		
		Model m2 = TDBFactory.createModel("/common/home/jhkurki/Desktop/test_tdb_2");
		
		m2.add(m.createResource("r1"),m.createProperty("p1"),"moi");
		m2.add(m.createResource("r1"),m.createProperty("p1"),"moi");
		m2.add(m.createResource("r1"),m.createProperty("p1"),"moi");
		m2.add(m.createResource("r1"),m.createProperty("p2"),"heihei");
		
		m.add(m2);
		
		for (Statement s : m.listStatements().toList())
			System.out.println(s);
		
		System.out.println("Done.");
	}
	
}
