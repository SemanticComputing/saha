package fi.seco.saha3.model;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.hp.hpl.jena.rdf.model.Model;

import fi.seco.saha3.model.configuration.IConfigService;
import fi.seco.saha3.model.configuration.PropertyConfig;
import fi.seco.saha3.model.configuration.RepositoryConfig;

/**
 * A single SAHA project. Contains the data of the model (in both the RDF graph
 * and Lucene index) and the relevant configuration, both for SAHA and HAKO.
 * 
 */
public class SahaProject implements ISahaProject {

	private IModelEditor modelEditor;
	private IModelReader modelReader;
	private IConfigService config;

	@Autowired
	public void setConfig(IConfigService config) {
		this.config = config;
	}

	@Autowired
	public void setModelEditor(IModelEditor modelEditor) {
		this.modelEditor = modelEditor;
	}

	@Autowired
	public void setModelReader(IModelReader modelReader) {
		this.modelReader = modelReader;
	}

	@Override
	public UriLabel addObjectProperty(String s, String p, String o, Locale locale) {
		return modelEditor.addObjectProperty(s, p, o, locale);
	}

	@Override
	public void removeObjectProperty(String s, String p, String o) {
		modelEditor.removeObjectProperty(s, p, o);
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l) {
		return modelEditor.addLiteralProperty(s, p, l);
	}

	@Override
	public UriLabel addLiteralProperty(String s, String p, String l, String lang) {
		return modelEditor.addLiteralProperty(s, p, l, lang);
	}

	@Override
	public UriLabel removeLiteralProperty(String s, String p, String valueShaHex) {
		return modelEditor.removeLiteralProperty(s, p, valueShaHex);
	}

	@Override
	public void removeProperty(String s, String p) {
		modelEditor.removeProperty(s, p);
	}

	@Override
	public String createResource(String type, String label) {
		return modelEditor.createResource(type, label);
	}

	@Override
	public String createResource(String uri, String type, String label) {
		return modelEditor.createResource(uri, type, label);
	}

	@Override
	public void removeResource(String uri) {
		modelEditor.removeResource(uri);
	}

	@Override
	public void clear() {
		modelEditor.clear();
	}

	@Override
	public void setMapProperty(String s, String fc, String value) {
		modelEditor.setMapProperty(s, fc, value);
	}

	@Override
	public IResults inlineSearch(String query, Collection<String> typeRestrictions, Locale locale, int maxResults) {
		return modelReader.inlineSearch(query, typeRestrictions, locale, maxResults);
	}

	@Override
	public IResults topSearch(String query, Locale locale, int maxResults) {
		return modelReader.topSearch(query, locale, maxResults);
	}

	@Override
	public IResults getSortedInstances(String type, Locale locale, int from, int to) {
		return modelReader.getSortedInstances(type, locale, from, to);
	}

	@Override
	public ISahaResource getResource(String resourceUri, Locale locale) {
		return modelReader.getResource(resourceUri, locale);
	}

	@Override
	public Model getWholeProject() {
		return modelReader.getWholeProject();
	}

	@Override
	public Set<UITreeNode> getClassTree(Locale locale) {
		return modelReader.getClassTree(locale);
	}

	@Override
	public void setPropertyOrder(String typeUri, Collection<String> propertyUris) {
		config.setPropertyOrder(typeUri, propertyUris);
	}

	@Override
	public boolean removeRepositoryConfig(String propertyUri, String sourceName) {
		return config.removeRepositoryConfig(propertyUri, sourceName);
	}

	@Override
	public void addRepositoryConfig(String propertyUri, RepositoryConfig repositoryConfig) {
		config.addRepositoryConfig(propertyUri, repositoryConfig);
	}

	@Override
	public boolean toggleDenyInstantiation(String propertyUri) {
		return config.toggleDenyInstantiation(propertyUri);
	}

	@Override
	public boolean toggleDenyLocalReferences(String propertyUri) {
		return config.toggleDenyLocalReferences(propertyUri);
	}

	@Override
	public boolean toggleHidden(String propertyUri) {
		return config.toggleHidden(propertyUri);
	}

	@Override
	public boolean toggleLocalized(String propertyUri) {
		return config.toggleLocalized(propertyUri);
	}

	@Override
	public boolean toggleWordIndices(String propertyUri) {
		return config.toggleWordIndices(propertyUri);
	}

	@Override
	public boolean togglePictureProperty(String propertyUri) {
		return config.togglePictureProperty(propertyUri);
	}

	@Override
	public void setAboutLink(String link) {
		config.setAboutLink(link);
	}

	@Override
	public PropertyConfig getPropertyConfig(String propertyUri) {
		return config.getPropertyConfig(propertyUri);
	}

	@Override
	public String getAboutLink() {
		return config.getAboutLink();
	}

	@Override
	public String getPassHash() {
		return config.getPassHash();
	}

	@Override
	public void setPassHash(String shaHex) {
		config.setPassHash(shaHex);
	}

	@Override
	public Model getModelFromConfig(String projectName) {
		return config.getModelFromConfig(projectName);
	}

	@Override
	public Model describe(String uri) {
		return modelReader.describe(uri);
	}

	@Override
	public Collection<String> getPropertyOrder(String typeUri) {
		return config.getPropertyOrder(typeUri);
	}

	@Override
	public String getNamespace() {
		return config.getNamespace();
	}

}
