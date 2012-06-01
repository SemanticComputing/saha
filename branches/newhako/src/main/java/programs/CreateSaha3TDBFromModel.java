package programs;

import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class CreateSaha3TDBFromModel
{
    public static void main(String[] args)
    {
        String inputFile = "/dump/kirjasampo_recover.nt";
        
        Model m = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(inputFile); 
        m.read(in,"","N-TRIPLES");
        
        Model tdbModel = TDBFactory.createModel("/common/group/finnonto/saha3/kirjasampo/");
        
        tdbModel.add(m);
        
        m.close();
        tdbModel.close();
    }
}
