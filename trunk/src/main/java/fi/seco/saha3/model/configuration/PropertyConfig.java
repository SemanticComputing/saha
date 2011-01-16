package fi.seco.saha3.model.configuration;

import java.util.ArrayList;
import java.util.Collection;

public class PropertyConfig {
	
	private boolean hidden;
	private boolean localized;
	private boolean denyLocalReferences;
	private boolean denyInstantiation;
	private boolean pictureProperty;
	private Collection<RepositoryConfig> repositoryConfigs = new ArrayList<RepositoryConfig>();
	
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	public boolean isLocalized() {
		return localized;
	}
	public void setLocalized(boolean localized) {
		this.localized = localized;
	}
	public boolean isDenyLocalReferences() {
		return denyLocalReferences;
	}
	public void setDenyLocalReferences(boolean denyLocalReferences) {
		this.denyLocalReferences = denyLocalReferences;
	}
	public boolean isDenyInstantiation() {
		return denyInstantiation;
	}
	public void setDenyInstantiation(boolean denyInstantiation) {
		this.denyInstantiation = denyInstantiation;
	}
	public boolean isPictureProperty() {
		return pictureProperty;
	}
	public void setPictureProperty(boolean pictureProperty) {
		this.pictureProperty = pictureProperty;
	}
	public Collection<RepositoryConfig> getRepositoryConfigs() {
		return repositoryConfigs;
	}
	public void setRepositoryConfigs(Collection<RepositoryConfig> repositoryConfigs) {
		this.repositoryConfigs = repositoryConfigs;
	}
	
}
