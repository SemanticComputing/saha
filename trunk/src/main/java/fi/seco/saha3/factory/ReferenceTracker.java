package fi.seco.saha3.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fi.seco.saha3.model.ISahaResource;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

public class ReferenceTracker {

	private Model model;
	private ResourceFactory resourceFactory;
	
	@Autowired
	public void setModel(Model model) {
		this.model = model;
	}
	
	@Autowired
	public void setResourceFactory(ResourceFactory resourceFactory) {
		this.resourceFactory = resourceFactory;
	}
	
	public List<ISahaResource> getPropertyMissingResources(String propertyUri, Locale locale) {
		List<ISahaResource> list = new ArrayList<ISahaResource>();
		for (String domain : getDomain(propertyUri))
			for (ISahaResource r : execSparql(domain,propertyUri,locale))
				list.add(r);
		return list;
	}
	
	private List<String> getDomain(String propertyUri) {
		List<String> domain = new ArrayList<String>();
		for (Statement s : new IteratorToIIterableIterator<Statement>(
				model.listStatements(model.createResource(propertyUri),RDFS.domain,(RDFNode)null)))
			if (s.getObject().isURIResource()) 
				domain.add(s.getResource().getURI());
		return domain;
	}
	
	private Iterable<ISahaResource> execSparql(String type, String propertyUri, final Locale locale) {
		String query = 
			"SELECT ?instance WHERE { " + 
			"	?instance <" + RDF.type.getURI() + "> <" + type + "> . " + 
			"	OPTIONAL { ?instance <" + propertyUri + "> ?object } . " +
			"	FILTER (!bound(?object)) " + 
			" }";
		
		QueryExecution qe = QueryExecutionFactory.create(QueryFactory.create(query),model);
		final ResultSet resultSet = qe.execSelect();
		
		return new Iterable<ISahaResource>() {
			public Iterator<ISahaResource> iterator() {
				return new Iterator<ISahaResource>() {
					public boolean hasNext() {
						return resultSet.hasNext();
					}
					public ISahaResource next() {
						return resourceFactory.getResource(resultSet.nextSolution().getResource("instance").getURI(),locale);
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	
}
