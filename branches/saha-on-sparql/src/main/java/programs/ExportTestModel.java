package programs;

import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ExportTestModel {
	public static void main(String[] args) throws IOException {
		Model m = ModelFactory.createDefaultModel();
		
		m.add(m.createResource("http://www.yso.fi/onto/kaunokki#ateos_55920"),
			m.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"),
			"uusi nimi toisesta rdf-tiedostosta");
		
		m.add(m.createResource("http://www.yso.fi/onto/kaunokki#ateos_55920"),
				m.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"),
				"foo");
		
		m.add(m.createResource("http://www.yso.fi/onto/kaunokki#ateos_55920"),
				m.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"),
				"foo");
		
		System.out.println("Writing model...");
		m.write(new FileWriter("/common/home/jhkurki/Desktop/testi.nt"),"N-TRIPLE");
	}
}
