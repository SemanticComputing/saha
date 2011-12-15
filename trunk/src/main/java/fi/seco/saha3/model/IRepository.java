package fi.seco.saha3.model;

import java.util.Collection;
import java.util.Locale;

/**
 * Common interface for local searches (to the SAHA project model) and 
 * ONKI searches.
 *
 */
public interface IRepository {

	public IResults search(String query, Collection<String> parentRestrictions, Collection<String> typeRestrictions, Locale locale, int maxResults);
	
}
