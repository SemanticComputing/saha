package programs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.base.file.FileException;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

public class ExportSaha3ProjectToFile {

	public static void main(String[] args) throws Exception {
	    iterateAndSave();
	    
//		SahaProjectRegistry r = new SahaProjectRegistry();
//		r.setProjectBaseDirectory("/common/group/finnonto/saha3/");
//		r.getModel("kirjasampo").write(new FileWriter(new File("/dump/kirjasampo.ttl")),"TURTLE");
//		r.close();
	}
	
	private static void iterateAndSave() throws IOException
	{
	    SahaProjectRegistry r = new SahaProjectRegistry();
        r.setProjectBaseDirectory("/dump/");
        Model broken = r.getModel("kirjasampo");
        
        Model hopefullyNotBroken = ModelFactory.createDefaultModel();
        
        long stmts = 0;
        StmtIterator it = broken.listStatements();
        
        while (it.hasNext())
        {
            Statement s = null;
            if (stmts % 1000 == 0)
                System.out.print(".");
            if (stmts % 100000 == 0)
                System.out.println();
            try
            {
                s = it.nextStatement();
                stmts++;
                hopefullyNotBroken.add(s);
            }
            catch (FileException e)
            {
                System.out.println("Caught FileException");
                if (s != null) System.out.println(s.getSubject().getURI() + " " + s.getPredicate().getURI());
                System.out.println("Trying to continue...");
            }
        }
        
        hopefullyNotBroken.write(new FileWriter(new File("/dump/kirjasampo_recover.nt")),"N-TRIPLES");
        
        r.close();
	}
	
}
