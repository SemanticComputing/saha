/**
 * 
 */
package fi.seco.saha3.model;

import java.util.Collection;

/**
 * @author jiemakel
 * 
 */
public interface IModelLister {

	public Collection<String> getModels();

	public boolean modelExists(String modelName);
}
