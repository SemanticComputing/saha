package test;

import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class RemoveClassDefinitionsFromRuukki {

	public static void main(String[] args) throws IOException {
		System.out.println("Reading...");
		
		Model model = ModelFactory.createDefaultModel();
		model.read(FileManager.get().open("/common/home/jhkurki/Desktop/ruukki.nt"), "", "N-TRIPLE");
		
		model.removeAll(null, RDF.type, OWL.Class);
		
		model.add(model.createResource("http://www.ruukki.com/onto#datasheet"),RDF.type,OWL.Class);
		model.add(model.createResource("http://www.ruukki.com/onto#fileType"),RDF.type,OWL.Class);
		
		System.out.println("Writing...");
		model.write(new FileWriter("/common/home/jhkurki/Desktop/ruukki2.nt"),"N-TRIPLE");
		System.out.println("Done.");
	}
	
}
