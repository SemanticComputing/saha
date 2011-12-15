package fi.seco.saha3.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.configuration.ConfigService;
import fi.seco.saha3.model.configuration.RepositoryConfig;
import fi.seco.semweb.util.FreeMarkerUtil;
import freemarker.template.Configuration;

/**
 * DWR-published class for modifying SAHA and HAKO configurations.
 * 
 */
public class ResourceConfigService {
	
	private static final String REPOSITORY_CONFIG_TEMPLATE = "saha3/standalone/repositoryConfig.ftl";
	
	private Logger log = Logger.getLogger(getClass());
	
	private SahaProjectRegistry sahaProjectRegistry;
	private Configuration configuration;
	
	@Required
	public void setFreeMarkerConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
		this.sahaProjectRegistry = sahaProjectRegistry;
	}
	
	public void setPropertyOrder(String model, String typeUri, String[] propertyUris) {
		log.debug("setPropertyOrder(" + model + ", " + typeUri + ", " + Arrays.asList(propertyUris) + ")");
		ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
		config.setPropertyOrder(typeUri,Arrays.asList(propertyUris));
	}
	
	public boolean removeRepositoryConfig(String model, String propertyUri, String sourceName) {
		log.debug("removeRepositoryConfig(" + model + ", " + propertyUri + ", " + sourceName + ")");
		ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
		return config.removeRepositoryConfig(propertyUri,sourceName);
	}
	
	public String addRepositoryConfig(String model, String id, String propertyUri, 
			String sourceName, String[] parentRestrictions, String[] typeRestrictions) 
	{
		log.debug("addRepositoryConfig(" + model + ", " + id + ", " + propertyUri + ", " + sourceName + ", " + 
				parentRestrictions + ", " + typeRestrictions + ")");
		
		RepositoryConfig repositoryConfig = 
			buildRepositoryConfig(sourceName,parentRestrictions,typeRestrictions);
		
		ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
		config.addRepositoryConfig(propertyUri,repositoryConfig);
		
		Map<String,Object> modelMap = new HashMap<String,Object>();
		modelMap.put("id",id);
		modelMap.put("model",model);
		modelMap.put("propertyUri",propertyUri);
		modelMap.put("repositoryConfig",repositoryConfig);
		
		return FreeMarkerUtil.process(configuration,REPOSITORY_CONFIG_TEMPLATE,modelMap);
	}
	
	private RepositoryConfig buildRepositoryConfig(String sourceName, 
			String[] parentRestrictions, String[] typeRestrictions) 
	{
		RepositoryConfig repositoryConfig = new RepositoryConfig();
		repositoryConfig.setSourceName(sourceName);
		if (parentRestrictions != null) {
			List<String> rest = new ArrayList<String>();
			for (String p : parentRestrictions) 
				if (!p.isEmpty()) rest.add(p);
			repositoryConfig.setParentRestrictions(rest);
		}
		if (typeRestrictions != null) {
			List<String> rest = new ArrayList<String>();
			for (String p : typeRestrictions)
				if (!p.isEmpty()) rest.add(p);
			repositoryConfig.setTypeRestrictions(rest);
		}
		return repositoryConfig;
	}
	
	public void toggleDenyInstantiation(String model, String propertyUri) {
		log.debug("toggleDenyInstantiation(" + model + ", " + propertyUri + ")");
		ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
		config.toggleDenyInstantiation(propertyUri);
	}
	
	public void toggleDenyLocalReferences(String model, String propertyUri) {
		log.debug("toggleDenyLocalReferences(" + model + ", " + propertyUri + ")");
		ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
		config.toggleDenyLocalReferences(propertyUri);
	}
	
	public void toggleHidden(String model, String propertyUri) {
		log.debug("toggleHidden(" + model + ", " + propertyUri + ")");
		ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
		config.toggleHidden(propertyUri);
	}
	
	public void toggleLocalized(String model, String propertyUri) {
		log.debug("toggleLocalized(" + model + ", " + propertyUri + ")");
		ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
		config.toggleLocalized(propertyUri);
	}
	
	public void togglePictureProperty(String model, String propertyUri) {
        log.debug("togglePictureProperty(" + model + ", " + propertyUri + ")");
        ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
		config.togglePictureProperty(propertyUri);
    }
	
	public void setAboutLink(String model, String link)
	{
	    log.debug("setAboutLink(" + model + ", " + link + ")");
        ConfigService config = sahaProjectRegistry.getSahaProject(model).getConfig();
        config.setAboutLink(link);
	}
	
	public void destroyHako(String model) {
		sahaProjectRegistry.getSahaProject(model).destroyHako();
	}
	
	public void addTypeToHakoConfig(String model, String typeUri) {	    
		sahaProjectRegistry.getSahaProject(model).getHakoConfig().getTypes().add(typeUri);
	}
	
	public void addPropertyToHakoConfig(String model, String propertyUri) {
		sahaProjectRegistry.getSahaProject(model).getHakoConfig().getProperties().add(propertyUri);
	}
	
}
