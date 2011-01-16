package fi.seco.saha3.model.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ConfigService {
	
	private static final String DEFAULT_NAMESPACE = "http://seco.tkk.fi/saha3/";
	private static final String DEFAULT_LABEL_PROPERTY = "http://www.w3.org/2004/02/skos/core#prefLabel";
	private static final String DEFAULT_ABOUT_LINK = "#";
	private static final String CONFIG_FILE_NAME = "project_config.xml";
	
	private XMLConfigStore store;
	private ProjectConfig config;
	
	public ConfigService(String path) {
		if (!path.endsWith(("/"))) path += "/";
		this.store = new XMLConfigStore(new File(path + CONFIG_FILE_NAME));
		this.config = store.getProjectConfig();
		if (config.getNamespace() == null) config.setNamespace(DEFAULT_NAMESPACE);
		if (config.getLabelProperty() == null) config.setLabelProperty(DEFAULT_LABEL_PROPERTY);
	}
	
	public PropertyConfig getPropertyConfig(String propertyUri) {
		if (!config.getPropertyConfigMap().containsKey(propertyUri)) 
			config.getPropertyConfigMap().put(propertyUri,new PropertyConfig());
		return config.getPropertyConfigMap().get(propertyUri);
	}

	public String getLabelProperty() {
		return config.getLabelProperty();
	}
	
	public String getAboutLink()
    {
	    String aboutLink = this.config.getAboutLink(); 
        return (aboutLink == null) ? DEFAULT_ABOUT_LINK : aboutLink;
    }
	
	public void setAboutLink(String aboutLink) {
	    this.config.setAboutLink(aboutLink);
	}
	
	public String getPassHash()
    {        
        String passHash = this.config.getPassHash();
        
        return (passHash == null) ? "" : passHash;
    }
	
	public void setPassHash(String passHash)
	{
	    this.config.setPassHash(passHash);
	}

	public String getNamespace() {
		return config.getNamespace();
	}
	
	public void setLabelProperty(String labelProperty) {
        this.config.setLabelProperty(labelProperty);
    }

	public void setPropertyConfig(String propertyUri, PropertyConfig propertyConfig) {
		config.getPropertyConfigMap().put(propertyUri,propertyConfig);
		store.save();
	}

	public void setPropertyOrder(String typeUri, Collection<String> propertyUris) {
		config.getPropertyOrderMap().put(typeUri,new ArrayList<String>(propertyUris));
		store.save();
	}

	public Collection<String> getPropertyOrder(String typeUri) {
		Collection<String> propertyOrder = config.getPropertyOrderMap().get(typeUri);
		if (propertyOrder != null) return propertyOrder;
		return Collections.emptyList();
	}

	public boolean removeRepositoryConfig(String propertyUri, String sourceName) {
		Collection<RepositoryConfig> repositoryConfigs = getPropertyConfig(propertyUri).getRepositoryConfigs();
		for (RepositoryConfig repositoryConfig : repositoryConfigs) {
			if (repositoryConfig.getSourceName().equals(sourceName)) {
				repositoryConfigs.remove(repositoryConfig);
				store.save();
				return true;
			}
		}
		return false;
	}

	public void addRepositoryConfig(String propertyUri, RepositoryConfig repositoryConfig) {
		getPropertyConfig(propertyUri).getRepositoryConfigs().add(repositoryConfig);
		store.save();
	}

	public void toggleDenyInstantiation(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setDenyInstantiation(!propertyConfig.isDenyInstantiation());
		store.save();
	}

	public void toggleDenyLocalReferences(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setDenyLocalReferences(!propertyConfig.isDenyLocalReferences());
		store.save();
	}

	public void toggleHidden(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setHidden(!propertyConfig.isHidden());
		store.save();
	}

	public void toggleLocalized(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setLocalized(!propertyConfig.isLocalized());
		store.save();
	}

	public void togglePictureProperty(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setPictureProperty(!propertyConfig.isPictureProperty());
		store.save();
	}

}
