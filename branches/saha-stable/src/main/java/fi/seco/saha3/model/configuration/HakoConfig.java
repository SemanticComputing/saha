package fi.seco.saha3.model.configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class that holds all the configuration pertaining to HAKO.
 * Separate from <code>ProjectConfig</code> which controls all configuration
 * pertaining to SAHA.
 * 
 */
public class HakoConfig {

	private List<String> types = new ArrayList<String>();
	private List<String> properties = new ArrayList<String>();
	
	public List<String> getTypes() {
		return types;
	}
	public void setTypes(List<String> types) {
		this.types = types;
	}
	public List<String> getProperties() {
		return properties;
	}
	public void setProperties(List<String> properties) {
		this.properties = properties;
	}
	
}
