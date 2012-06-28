package programs;

import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;

public class DumpTDB {

	public static void main(String[] args) throws IOException {
		System.out.println("Reading...");
		Model model = TDBFactory.createModel("/common/group/finnonto/hako/models/ruukki");
		System.out.println("Writing...");
		model.write(new FileWriter("/common/home/jhkurki/Desktop/ruukki.n3"),"N-TRIPLE");
		System.out.println("Done.");
	}
	
}
