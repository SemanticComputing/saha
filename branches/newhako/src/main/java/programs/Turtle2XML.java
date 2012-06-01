package programs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class Turtle2XML {

	public static void main(String[] args) {
//		String fileName = "/home/jhkurki/Desktop/aineistoskeemat.ttl";
		String fileName2 = "/home/jhkurki/Desktop/annotaatioskeema.ttl";
		
		turtle2xml(fileName2);
	}
	
	public static String turtle2xml(String fileName) {
		System.out.println("Converting: " + fileName + " to RDF/XML...");
		Model model = ModelFactory.createDefaultModel();
		model.read(FileManager.get().open(fileName), "", "TTL");
		try {
			String xmlFileName = fileName.substring(0,fileName.indexOf('.')) + ".owl";
			model.write(new FileWriter(new File(xmlFileName)),"RDF/XML-ABBREV");
			System.out.println("Wrote new file: " + xmlFileName);
			return xmlFileName;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
