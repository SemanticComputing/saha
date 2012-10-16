/**
 * 
 */
package fi.seco.saha3.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author jiemakel
 * 
 */
public class RemoteSPARQLModelLister implements IModelLister {

	private String sparqlService;

	@Required
	public void setSparqlService(String sparqlService) {
		this.sparqlService = sparqlService;
	}

	private static final Query query = QueryFactory.create("SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o . } }");

	@Override
	public Collection<String> getModels() {
		List<String> ret = new ArrayList<String>();
		ResultSet rs = QueryExecutionFactory.sparqlService(sparqlService, query).execSelect();
		while (rs.hasNext())
			ret.add(rs.next().get("g").toString());
		ret.add("default");
		return ret;
	}

	@Override
	public boolean modelExists(String modelName) {
		return "default".equals(modelName) ? true : getModels().contains(modelName);
	}

}
