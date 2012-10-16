package fi.seco.saha3.model;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Common interface for local searches (to the SAHA project model) and ONKI
 * searches.
 * 
 */
public interface IModelReader {

	public IResults search(String query, Collection<String> parentRestrictions, Collection<String> typeRestrictions,
			Locale locale, int maxResults);

	public IResults getSortedInstances(String label, String type, Locale locale, int from, int to);

	public ISahaResource getResource(String resourceUri, Locale locale);

	public Model getWholeProject();

	public Set<UITreeNode> getClassTree(Locale locale);

	public Model describe(String uri);

}
