package fi.seco.saha3.model.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProjectConfig {

	private String namespace;
	private String labelProperty;
	private String aboutLink;
	private String passHash;
	private Map<String,Collection<String>> propertyOrderMap = new HashMap<String,Collection<String>>();
	private Map<String,PropertyConfig> propertyConfigMap = new HashMap<String,PropertyConfig>();
	
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getLabelProperty() {
		return labelProperty;
	}
	public void setAboutLink(String aboutLink)
    {
        this.aboutLink = aboutLink;
    }
    public String getAboutLink()
    {
        return aboutLink;
    }	
    public void setPassHash(String passHash)
    {
        this.passHash = passHash;
    }   
    public String getPassHash()
    {
        return this.passHash;
    }
	public void setLabelProperty(String labelProperty) {
		this.labelProperty = labelProperty;
	}
	public Map<String,Collection<String>> getPropertyOrderMap() {
		return propertyOrderMap;
	}
	public void setPropertyOrderMap(Map<String,Collection<String>> propertyOrderMap) {
		this.propertyOrderMap = propertyOrderMap;
	}
	public Map<String,PropertyConfig> getPropertyConfigMap() {
		return propertyConfigMap;
	}
	public void setPropertyConfigMap(Map<String,PropertyConfig> propertyConfigMap) {
		this.propertyConfigMap = propertyConfigMap;
	}
    
}
