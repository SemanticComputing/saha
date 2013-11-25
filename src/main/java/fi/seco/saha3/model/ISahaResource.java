package fi.seco.saha3.model;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface ISahaResource {

	public String getUri();

	public String getLabel();

	public List<UriLabel> getTypes();

	/**
	 * Gets all the properties this resource has
	 */
	public List<ISahaProperty> getProperties();

	public List<ISahaProperty> getInverseProperties();

	public Map<UriLabel, Set<ISahaProperty>> getPropertyMap();

	public Set<Map.Entry<UriLabel, Set<ISahaProperty>>> getPropertyMapEntrySet();

	/**
	 * Gets all the properties this resource has along with properties in the
	 * domain of this resource
	 */
	public Set<ISahaProperty> getEditorProperties();

	public Map<UriLabel, Set<ISahaProperty>> getEditorPropertyMap();

	public Set<Entry<UriLabel, Set<ISahaProperty>>> getEditorPropertyMapEntrySet();

}
