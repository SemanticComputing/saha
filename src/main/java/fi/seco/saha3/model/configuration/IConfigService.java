/**
 * 
 */
package fi.seco.saha3.model.configuration;

import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author jiemakel
 * 
 */
public interface IConfigService {

	public void setPropertyOrder(String typeUri, Collection<String> propertyUris);

	public boolean removeRepositoryConfig(String propertyUri, String sourceName);

	public void addRepositoryConfig(String propertyUri, RepositoryConfig repositoryConfig);

	public boolean toggleDenyInstantiation(String propertyUri);

	public boolean toggleDenyLocalReferences(String propertyUri);

	public boolean toggleHidden(String propertyUri);

	public boolean toggleLocalized(String propertyUri);

	public boolean toggleWordIndices(String propertyUri);

	public boolean togglePictureProperty(String propertyUri);

	public void setAboutLink(String link);

	public PropertyConfig getPropertyConfig(String propertyUri);

	public String getAboutLink();

	public String getPassHash();

	public void setPassHash(String shaHex);

	public Model getModelFromConfig(String projectName);

	public Collection<String> getPropertyOrder(String typeUri);

	public String getNamespace();

}
