package fi.seco.saha3.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.model.configuration.RepositoryConfig;
import fi.seco.semweb.util.FreeMarkerUtil;
import freemarker.template.Configuration;

/**
 * DWR-published class for modifying SAHA and HAKO configurations.
 * 
 */
public class ResourceConfigService {

	private static final String REPOSITORY_CONFIG_TEMPLATE = "saha3/standalone/repositoryConfig.ftl";

	private static final Logger log = LoggerFactory.getLogger(ResourceConfigService.class);

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
		if (log.isDebugEnabled())
			log.debug("setPropertyOrder(" + model + ", " + typeUri + ", " + Arrays.asList(propertyUris) + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.setPropertyOrder(typeUri, Arrays.asList(propertyUris));
	}

	public boolean removeRepositoryConfig(String model, String propertyUri, String sourceName) {
		if (log.isDebugEnabled())
			log.debug("removeRepositoryConfig(" + model + ", " + propertyUri + ", " + sourceName + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		return config.removeRepositoryConfig(propertyUri, sourceName);
	}

	public String addRepositoryConfig(String model, String id, String propertyUri, String sourceName,
			String[] parentRestrictions, String[] typeRestrictions) {
		if (log.isDebugEnabled())
			log.debug("addRepositoryConfig(" + model + ", " + id + ", " + propertyUri + ", " + sourceName + ", " + parentRestrictions + ", " + typeRestrictions + ")");

		RepositoryConfig repositoryConfig = buildRepositoryConfig(sourceName, parentRestrictions, typeRestrictions);

		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.addRepositoryConfig(propertyUri, repositoryConfig);

		Map<String, Object> modelMap = new HashMap<String, Object>();
		modelMap.put("id", id);
		modelMap.put("model", model);
		modelMap.put("propertyUri", propertyUri);
		modelMap.put("repositoryConfig", repositoryConfig);

		return FreeMarkerUtil.process(configuration, REPOSITORY_CONFIG_TEMPLATE, modelMap);
	}

	private RepositoryConfig buildRepositoryConfig(String sourceName, String[] parentRestrictions,
			String[] typeRestrictions) {
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
		if (log.isDebugEnabled()) log.debug("toggleDenyInstantiation(" + model + ", " + propertyUri + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.toggleDenyInstantiation(propertyUri);
	}

	public void toggleDenyLocalReferences(String model, String propertyUri) {
		if (log.isDebugEnabled()) log.debug("toggleDenyLocalReferences(" + model + ", " + propertyUri + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.toggleDenyLocalReferences(propertyUri);
	}

	public void toggleHidden(String model, String propertyUri) {
		if (log.isDebugEnabled()) log.debug("toggleHidden(" + model + ", " + propertyUri + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.toggleHidden(propertyUri);
	}

	public void toggleLocalized(String model, String propertyUri) {
		if (log.isDebugEnabled()) log.debug("toggleLocalized(" + model + ", " + propertyUri + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.toggleLocalized(propertyUri);
	}

	public void toggleWordIndices(String model, String propertyUri) {
		if (log.isDebugEnabled()) log.debug("toggleWordIndices(" + model + ", " + propertyUri + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.toggleWordIndices(propertyUri);
	}

	public void togglePictureProperty(String model, String propertyUri) {
		if (log.isDebugEnabled()) log.debug("togglePictureProperty(" + model + ", " + propertyUri + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.togglePictureProperty(propertyUri);
	}

	public void setAboutLink(String model, String link) {
		if (log.isDebugEnabled()) log.debug("setAboutLink(" + model + ", " + link + ")");
		IConfigService config = sahaProjectRegistry.getConfig(model);
		config.setAboutLink(link);
	}

}
