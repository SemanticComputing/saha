package fi.seco.saha3.model.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

import fi.seco.saha3.util.SAHA3;
import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

/**
 * A project-specific persistent configuration service. Stores the internal SAHA
 * configuration that is not governed directly by the RDF schema of the project
 * data model.
 * 
 */
public class ConfigService implements IConfigService {

	private final Logger log = Logger.getLogger(getClass());

	private static final String DEFAULT_NAMESPACE = "http://seco.tkk.fi/saha3/";
	private static final String DEFAULT_LABEL_PROPERTY = "http://www.w3.org/2004/02/skos/core#prefLabel";
	private static final String DEFAULT_ABOUT_LINK = "#";
	private static final String CONFIG_FILE_NAME = "project_config.xml";

	Resource sahaProject = ResourceFactory.createResource(DEFAULT_NAMESPACE + "Project");
	Property labelProperty = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "labelProperty");
	Property namespace = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "namespace");
	Property aboutLink = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "aboutLink");
	Property adminPasshash = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "adminPasshash");

	Property hasPropertyOrder = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "hasPropertyOrder");

	Property hasRepositoryConfig = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "hasRepositoryConfig");
	Property denyLocalReferences = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "denyLocalReferences");
	Property denyNewInstances = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "denyNewInstances");
	Property isHidden = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "isHidden");
	Property hasWordIndices = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "hasWordIndices");
	Property isLocalized = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "isLocalized");
	Property isPictureProperty = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "isPictureProperty");

	Resource sahaRepositoryConfig = ResourceFactory.createResource(DEFAULT_NAMESPACE + "RepositoryConfig");
	Property hasRepositorySource = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "hasRepositorySource");
	Property hasTypeRestriction = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "hasTypeRestriction");
	Property hasParentRestriction = ResourceFactory.createProperty(DEFAULT_NAMESPACE + "hasParentRestriction");

	private final XMLConfigStore store;
	private final ProjectConfig config;

	public ConfigService(String path) {
		if (!path.endsWith(("/"))) path += "/";
		this.store = new XMLConfigStore(new File(path + CONFIG_FILE_NAME));
		this.config = store.getProjectConfig();
		if (config.getNamespace() == null) config.setNamespace(DEFAULT_NAMESPACE);
		if (config.getLabelProperty() == null) config.setLabelProperty(DEFAULT_LABEL_PROPERTY);
	}

	@Override
	public PropertyConfig getPropertyConfig(String propertyUri) {
		if (!config.getPropertyConfigMap().containsKey(propertyUri))
			config.getPropertyConfigMap().put(propertyUri, new PropertyConfig());
		return config.getPropertyConfigMap().get(propertyUri);
	}

	public String getLabelProperty() {
		return config.getLabelProperty();
	}

	@Override
	public String getAboutLink() {
		String aboutLink = this.config.getAboutLink();
		return (aboutLink == null) ? DEFAULT_ABOUT_LINK : aboutLink;
	}

	@Override
	public void setAboutLink(String aboutLink) {
		this.config.setAboutLink(aboutLink);
	}

	@Override
	public String getPassHash() {
		String passHash = this.config.getPassHash();

		return (passHash == null) ? "" : passHash;
	}

	@Override
	public void setPassHash(String passHash) {
		this.config.setPassHash(passHash);
	}

	@Override
	public String getNamespace() {
		return config.getNamespace();
	}

	private void setNamespace(String string) {
		this.config.setNamespace(string);
	}

	public void setLabelProperty(String labelProperty) {
		this.config.setLabelProperty(labelProperty);
	}

	public void setPropertyConfig(String propertyUri, PropertyConfig propertyConfig) {
		config.getPropertyConfigMap().put(propertyUri, propertyConfig);
		store.save();
	}

	@Override
	public void setPropertyOrder(String typeUri, Collection<String> propertyUris) {
		config.getPropertyOrderMap().put(typeUri, new ArrayList<String>(propertyUris));
		store.save();
	}

	@Override
	public Collection<String> getPropertyOrder(String typeUri) {
		Collection<String> propertyOrder = config.getPropertyOrderMap().get(typeUri);
		if (propertyOrder != null) return propertyOrder;
		return Collections.emptyList();
	}

	@Override
	public boolean removeRepositoryConfig(String propertyUri, String sourceName) {
		Collection<RepositoryConfig> repositoryConfigs = getPropertyConfig(propertyUri).getRepositoryConfigs();
		for (RepositoryConfig repositoryConfig : repositoryConfigs)
			if (repositoryConfig.getSourceName().equals(sourceName)) {
				repositoryConfigs.remove(repositoryConfig);
				store.save();
				return true;
			}
		return false;
	}

	@Override
	public void addRepositoryConfig(String propertyUri, RepositoryConfig repositoryConfig) {
		getPropertyConfig(propertyUri).getRepositoryConfigs().add(repositoryConfig);
		store.save();
	}

	@Override
	public boolean toggleDenyInstantiation(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setDenyInstantiation(!propertyConfig.isDenyInstantiation());
		store.save();
		return propertyConfig.isDenyInstantiation();
	}

	@Override
	public boolean toggleDenyLocalReferences(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setDenyLocalReferences(!propertyConfig.isDenyLocalReferences());
		store.save();
		return propertyConfig.isDenyLocalReferences();
	}

	@Override
	public boolean toggleHidden(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setHidden(!propertyConfig.isHidden());
		store.save();
		return propertyConfig.isHidden();
	}

	@Override
	public boolean toggleLocalized(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setLocalized(!propertyConfig.isLocalized());
		store.save();
		return propertyConfig.isLocalized();
	}

	@Override
	public boolean toggleWordIndices(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setWordIndices(!propertyConfig.isWordIndices());
		store.save();
		return propertyConfig.isWordIndices();
	}

	@Override
	public boolean togglePictureProperty(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setPictureProperty(!propertyConfig.isPictureProperty());
		store.save();
		return propertyConfig.isPictureProperty();
	}

	/**
	 * Processes the model for SAHA internal configuration handled by this
	 * service, adds the appropriate settings and removes the corresponding
	 * triples from the model.
	 * 
	 * Called before the whole model is reindexed due to the modifications made
	 * by this method.
	 * 
	 * @param m
	 *            The whole project data model
	 */
	public void addConfigFromModel(Model m) {

		Property[] tbd = new Property[] { hasPropertyOrder, hasRepositoryConfig, denyLocalReferences, denyNewInstances, isHidden, isLocalized, isPictureProperty, hasRepositorySource, hasTypeRestriction, hasParentRestriction };

		List<Statement> toBeDeleted = new ArrayList<Statement>();

		try {
			for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, sahaProject))) {
				Resource r = s.getSubject();

				if (r.hasProperty(labelProperty)) this.setLabelProperty(r.getProperty(labelProperty).getString());
				if (r.hasProperty(namespace)) this.setNamespace(r.getProperty(namespace).getString());
				if (r.hasProperty(aboutLink)) this.setAboutLink(r.getProperty(aboutLink).getString());
				if (r.hasProperty(adminPasshash)) this.setPassHash(r.getProperty(adminPasshash).getString());

				for (Statement s2 : new IteratorToIIterableIterator<Statement>(m.listStatements(r, null, (RDFNode) null)))
					toBeDeleted.add(s2);
			}
		} catch (Exception e) {
			log.error("Malformed SAHA project RDF metadata in model:");
			log.error(e.getMessage());
		}

		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, OWL.Class)))
			try {
				Resource r = s.getSubject();

				if (r.hasProperty(hasPropertyOrder)) {
					Resource seq = r.getProperty(hasPropertyOrder).getResource();

					String baseUri = RDF.getURI() + "_";

					List<String> propertyOrder = new ArrayList<String>();

					int i = 1;

					while (seq.hasProperty(m.getProperty(baseUri + i))) {
						Resource next = seq.getProperty(m.getProperty(baseUri + i)).getResource();
						propertyOrder.add(next.getURI());

						log.info("Added property order for position " + i + " : " + next.getURI());
						i++;
					}

					this.setPropertyOrder(r.getURI(), propertyOrder);

					for (Statement s2 : new IteratorToIIterableIterator<Statement>(m.listStatements(seq, null, (RDFNode) null)))
						toBeDeleted.add(s2);
				}
			} catch (Exception e) {
				log.error("Malformed SAHA class RDF metadata in model:");
				log.error(e.getMessage());
				e.printStackTrace();
			}

		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, OWL.ObjectProperty)))
			try {
				Resource r = s.getSubject();

				for (Statement s2 : new IteratorToIIterableIterator<Statement>(m.listStatements(r, hasRepositoryConfig, (RDFNode) null))) {
					Resource r2 = s2.getResource();

					RepositoryConfig rc = new RepositoryConfig();

					if (r2.hasProperty(hasRepositorySource))
						rc.setSourceName(r2.getProperty(hasRepositorySource).getString());

					Set<String> typeRestrictions = new HashSet<String>();
					for (Statement s3 : new IteratorToIIterableIterator<Statement>(m.listStatements(r, hasTypeRestriction, (RDFNode) null)))
						typeRestrictions.add(s3.getString());
					rc.setTypeRestrictions(typeRestrictions);

					Set<String> parentRestrictions = new HashSet<String>();
					for (Statement s3 : new IteratorToIIterableIterator<Statement>(m.listStatements(r, hasParentRestriction, (RDFNode) null)))
						parentRestrictions.add(s3.getString());
					rc.setParentRestrictions(parentRestrictions);

					this.addRepositoryConfig(r.getURI(), rc);
					log.info("Added repository config for " + r.getURI());

					for (Statement s3 : new IteratorToIIterableIterator<Statement>(m.listStatements(r2, null, (RDFNode) null)))
						toBeDeleted.add(s3);
				}

				if (r.hasProperty(denyLocalReferences))
					if (!this.toggleDenyLocalReferences(r.getURI())) this.toggleDenyLocalReferences(r.getURI());

				if (r.hasProperty(denyNewInstances))
					if (!this.toggleDenyInstantiation(r.getURI())) this.toggleDenyInstantiation(r.getURI());
			} catch (Exception e) {
				log.error("Malformed SAHA object property RDF metadata in model:");
				log.error(e.getMessage());
			}

		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, OWL.DatatypeProperty)))
			try {
				Resource r = s.getSubject();

				if (r.hasProperty(isHidden)) if (!this.toggleHidden(r.getURI())) this.toggleHidden(r.getURI());
				if (r.hasProperty(isLocalized))
					if (!this.toggleLocalized(r.getURI())) this.toggleLocalized(r.getURI());
				if (r.hasProperty(isPictureProperty))
					if (!this.togglePictureProperty(r.getURI())) this.togglePictureProperty(r.getURI());
			} catch (Exception e) {
				log.error("Malformed SAHA literal property RDF metadata in model:");
				log.error(e.getMessage());
			}

		for (Property p : tbd)
			for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, p, (RDFNode) null)))
				toBeDeleted.add(s);

		m.remove(toBeDeleted);
	}

	/**
	 * Forms an RDF model from the SAHA configuration for this project. Used for
	 * exporting it since a normal export won't include the configuration.
	 * 
	 * @param projectName
	 *            The name of the project
	 * @return A data model of the project's configuration
	 */
	@Override
	public Model getModelFromConfig(String projectName) {
		log.info("Writing project configuration RDF model for project: " + projectName);

		Model m = ModelFactory.createDefaultModel();

		List<Statement> tba = new ArrayList<Statement>();

		Resource project = m.getResource(DEFAULT_NAMESPACE + projectName);

		tba.add(m.createStatement(project, RDF.type, sahaProject));
		if (this.config.getLabelProperty() != null)
			tba.add(m.createStatement(project, labelProperty, this.config.getLabelProperty()));
		if (this.config.getNamespace() != null)
			tba.add(m.createStatement(project, namespace, this.config.getNamespace()));
		if (this.config.getAboutLink() != null)
			tba.add(m.createStatement(project, aboutLink, this.config.getAboutLink()));
		//		if (this.config.getPassHash() != null)
		//			tba.add(m.createStatement(project, adminPasshash, this.config.getPassHash()));

		for (Entry<String, Collection<String>> entry : config.getPropertyOrderMap().entrySet()) {
			Resource order = m.createResource(SAHA3.generateRandomUri(getNamespace()));

			tba.add(m.createStatement(m.getResource(entry.getKey()), hasPropertyOrder, order));
			tba.add(m.createStatement(order, RDF.type, RDF.Seq));

			String baseUri = RDF.getURI() + "_";
			int i = 0;

			for (String s : entry.getValue())
				tba.add(m.createStatement(order, m.getProperty(baseUri + ++i), m.getResource(s)));

		}

		for (Entry<String, PropertyConfig> entry : config.getPropertyConfigMap().entrySet()) {
			Resource prop = m.createResource(entry.getKey());

			PropertyConfig pConf = entry.getValue();

			if (pConf.isDenyInstantiation())
				tba.add(m.createStatement(prop, denyNewInstances, m.createTypedLiteral(true)));
			if (pConf.isDenyLocalReferences())
				tba.add(m.createStatement(prop, denyLocalReferences, m.createTypedLiteral(true)));
			if (pConf.isHidden()) tba.add(m.createStatement(prop, isHidden, m.createTypedLiteral(true)));
			if (pConf.isLocalized()) tba.add(m.createStatement(prop, isLocalized, m.createTypedLiteral(true)));
			if (pConf.isPictureProperty())
				tba.add(m.createStatement(prop, isPictureProperty, m.createTypedLiteral(true)));

			for (RepositoryConfig rc : pConf.getRepositoryConfigs()) {
				Resource source = m.createResource(SAHA3.generateRandomUri(getNamespace()));

				tba.add(m.createStatement(source, RDF.type, sahaRepositoryConfig));
				tba.add(m.createStatement(source, hasRepositorySource, rc.getSourceName()));

				for (String pr : rc.getParentRestrictions())
					tba.add(m.createStatement(source, hasParentRestriction, pr));
				for (String tr : rc.getTypeRestrictions())
					tba.add(m.createStatement(source, hasTypeRestriction, tr));

				tba.add(m.createStatement(prop, hasRepositoryConfig, source));
			}
		}

		m.add(tba);

		m.setNsPrefix("saha3", DEFAULT_NAMESPACE);
		m.setNsPrefix("xsd", XSD.getURI());
		return m;
	}

}
