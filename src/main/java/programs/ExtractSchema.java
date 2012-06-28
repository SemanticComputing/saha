package programs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

public class ExtractSchema {
	public static void main(String[] args) {
//		String fileName = "/common/group/finnonto/kulttuuriSampo/setti-18_07_2008/skstoimijat.ttl";
		String fileName = "/common/scratch/kaunokki/kauno_teosrekisteri.ttl";
		String schemaName = "/common/home/jhkurki/Desktop/extracted_schema.ttl";
		
		System.out.println("Loading model...");
		Model model = ModelFactory.createDefaultModel();
		model.read(FileManager.get().open(fileName), "", "TTL");
		
		Model schemaModel = ModelFactory.createDefaultModel();		
		schemaModel.setNsPrefixes(model.getNsPrefixMap());
		schemaModel.setNsPrefix("owl",OWL.NS);
		
		Set<Resource> addedResources = new HashSet<Resource>();
		Set<Property> addedProperties = new HashSet<Property>();
		
		System.out.print("Processing resources..");
		
		int i=0;
		
		for (Statement statement : new IteratorToIIterableIterator<Statement>(model.listStatements(null,RDF.type,(RDFNode)null))) {
			if (i++ % 5000 == 0) System.out.print(".");
			
			Resource r = statement.getResource();
			
			if (!addedResources.contains(r)) {
				boolean hasType = false;
				for (@SuppressWarnings("unused") Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(r,RDF.type,(RDFNode)null))) hasType = true;
				
				if (!hasType)
					schemaModel.add(r,RDF.type,OWL.Class);
				
				for (Statement s : new IteratorToIIterableIterator<Statement>(model.listStatements(null,RDF.type,r))) {
					Resource instance = s.getSubject();
					for (Statement propertyStatement : new IteratorToIIterableIterator<Statement>(model.listStatements(instance,null,(RDFNode)null))) {
						Property property = propertyStatement.getPredicate();
						if (!addedProperties.contains(property)) {
							if (propertyStatement.getObject().isLiteral()) 
								schemaModel.add(property,RDF.type,OWL.DatatypeProperty);
							else
								schemaModel.add(property,RDF.type,OWL.ObjectProperty);
							addedProperties.add(property);
						}
					}
				}
				
				addedResources.add(r);
			}
		}
		
		System.out.println();
		System.out.println("Writing schema file...");
		
		try {
			schemaModel.write(new FileOutputStream(schemaName),"TTL");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done.");
	}
}
