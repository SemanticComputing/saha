package fi.seco.saha3.chat.ai;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.tdb.TDBFactory;

import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

public class MCDriver {
    
	 /**
     * @throws IOException
     */
	public static void main(String[] args) throws IOException {
	    MarkovChain mc = learnDBPediaRDF();
//		MarkovChain mc = learnRDF("/common/scratch/dbpedia/longabstract_fi.nt");
//		MarkovChain mc = learnText("/dump/english.50MB");
	    
	    // Bias term "sairaus"
//		mc.setBias(OntologyNearbyBias.getDefaultKokoBias("http://www.yso.fi/onto/koko/p16845"));
	    
	    for (int i = 0 ; i < 10 ; i++)
	    {
	        System.out.println();
	        String simulation = mc.simulate(100); 
	        System.out.println(simulation.substring(simulation.indexOf('.') + 1, simulation.lastIndexOf('.') + 1).trim());
	    }
	    
		System.out.println(Runtime.getRuntime().totalMemory()/1048576.0);
		
	}
	
	public static MarkovChain learnText(String file) {
		MarkovChain mc = new MarkovChain();
		
		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;
			int i = 0;
			while ((line = input.readLine()) != null) {
				if (i % 20000 == 0) System.out.println();
				if (i % 500 == 0) System.out.print(".");
				mc.learn(line);
				i++;
				if (i > 250000)
					break;
			}
		} catch (IOException e) {
			System.err.println(e);
		}
		System.out.println();
		return mc;
	}
	
	public static MarkovChain learnRDF(Model m)
	{	    
	    MarkovChain mc = new MarkovChain();
	    Random rn = new Random();
	    int i = 0;
	    for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements())) {
	        if (rn.nextBoolean() && rn.nextBoolean());
            if (i % 20000 == 0) System.out.println();
            if (i % 500 == 0) System.out.print(".");
            RDFNode r = s.getObject();
            if (r.isLiteral())
                mc.learn(((Literal)r).getString());
            if (i > 50000)
                break;
            i++;
        }
	    System.out.println();
	    return mc;
	}
	
	public static MarkovChain learnRDF(String file) {
		Model m = ModelFactory.createDefaultModel();
		try {
			m.read(new FileReader(file),null,"N-TRIPLES");
		} catch (Exception e) {
			System.err.println(e);
		}
		
		return learnRDF(m);
	}
	
	public static MarkovChain learnDBPediaRDF() {
        Model m = TDBFactory.createModel("/common/group/finnonto/saha3general/dbpedia_fi/");
        
        MarkovChain mc = learnRDF(m);
        
        m.close();
        m = null;
        
        return mc;
    }
}
