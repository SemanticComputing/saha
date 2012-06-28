package programs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class CombineAndSerializeModel {

	public static void main(String[] args) throws IOException {
		Model model = ModelFactory.createDefaultModel();
		
		String dir = "/common/scratch/Panulle/";
		
		System.out.println("Reading file...");
		model.read(FileManager.get().open(dir + "teosrekisteri_book.tll"), "", "TTL");
		
		System.out.println("Reading file...");
		model.read(FileManager.get().open(dir + "teosrekisteri_times.tll"), "", "TTL");
		
		System.out.println("Reading file...");
		model.read(FileManager.get().open(dir + "teosrekisteri_writer.tll"), "", "TTL");
		
		System.out.println("Reading file...");
		model.read(FileManager.get().open(dir + "kauno-schema.ttl"), "", "TTL");
		
		System.out.println("Writing...");
		model.write(new FileWriter(new File(dir + "kauno_all.owl")),"RDF/XML-ABBREV");
		
		System.out.println("Done.");
	}
	
}
