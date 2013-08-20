/**
 * 
 */
package fi.seco.saha3.infrastructure;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import fi.seco.saha3.model.IResults;
import fi.seco.saha3.model.ISahaProperty;

/**
 * @author jiemakel
 *
 */
public interface IExternalRepository {

	public abstract IResults search(String queryTerm, Collection<String> parentUris, Collection<String> typeUris,
			Locale locale, int maxResults);

	public abstract String getLabel(String uri, String lang);

	public abstract Set<ISahaProperty> getProperties(String uri, Locale locale);

}