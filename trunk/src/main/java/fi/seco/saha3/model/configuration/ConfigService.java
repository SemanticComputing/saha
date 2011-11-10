package fi.seco.saha3.model.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import fi.seco.semweb.util.iterator.IteratorToIIterableIterator;

public class ConfigService {
	
	private Logger log = Logger.getLogger(getClass());
	
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
	
	private void setNamespace(String string) {
		this.config.setNamespace(string);		
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

	public boolean toggleDenyInstantiation(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setDenyInstantiation(!propertyConfig.isDenyInstantiation());
		store.save();
		return propertyConfig.isDenyInstantiation();
	}

	public boolean toggleDenyLocalReferences(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setDenyLocalReferences(!propertyConfig.isDenyLocalReferences());
		store.save();
		return propertyConfig.isDenyLocalReferences();
	}

	public boolean toggleHidden(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setHidden(!propertyConfig.isHidden());
		store.save();
		return propertyConfig.isHidden();
	}

	public boolean toggleLocalized(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setLocalized(!propertyConfig.isLocalized());
		store.save();
		return propertyConfig.isLocalized();
	}

	public boolean togglePictureProperty(String propertyUri) {
		PropertyConfig propertyConfig = getPropertyConfig(propertyUri);
		propertyConfig.setPictureProperty(!propertyConfig.isPictureProperty());
		store.save();
		return propertyConfig.isPictureProperty();
	}

	public void addConfigFromModel(Model m) {

		Resource sahaProject = m.getResource(DEFAULT_NAMESPACE+"Project");
		Property labelProperty = m.getProperty(DEFAULT_NAMESPACE+"labelProperty");
		Property namespace = m.getProperty(DEFAULT_NAMESPACE+"namespace");
		Property aboutLink = m.getProperty(DEFAULT_NAMESPACE+"aboutLink");
		Property adminPasshash = m.getProperty(DEFAULT_NAMESPACE+"adminPasshash");

		Property hasPropertyOrder = m.getProperty(DEFAULT_NAMESPACE+"hasPropertyOrder");

		Property hasRepositoryConfig = m.getProperty(DEFAULT_NAMESPACE+"hasRepositoryConfig");
		Property denyLocalReferences = m.getProperty(DEFAULT_NAMESPACE+"denyLocalReferences");
		Property denyNewInstances= m.getProperty(DEFAULT_NAMESPACE+"denyNewInstances");
		Property isHidden = m.getProperty(DEFAULT_NAMESPACE+"isHidden");
		Property isLocalized = m.getProperty(DEFAULT_NAMESPACE+"isLocalized");
		Property isPictureProperty = m.getProperty(DEFAULT_NAMESPACE+"isPictureProperty");

		Property hasRepositorySource = m.getProperty(DEFAULT_NAMESPACE+"hasRepositorySource");
		Property hasTypeRestriction = m.getProperty(DEFAULT_NAMESPACE+"hasTypeRestriction");
		Property hasParentRestriction = m.getProperty(DEFAULT_NAMESPACE+"hasParentRestriction");

		Property[] tbd = new Property[] {hasPropertyOrder, hasRepositoryConfig,
				denyLocalReferences, denyNewInstances, isHidden, isLocalized,
				isPictureProperty, hasRepositorySource, hasTypeRestriction,
				hasParentRestriction
		};
		
		List<Statement> toBeDeleted = new ArrayList<Statement>();
		
		try
		{
			for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, sahaProject)))
			{
				Resource r = s.getSubject();

				if (r.hasProperty(labelProperty))
					this.setLabelProperty(r.getProperty(labelProperty).getString());
				if (r.hasProperty(namespace))
					this.setNamespace(r.getProperty(namespace).getString());
				if (r.hasProperty(aboutLink))
					this.setAboutLink(r.getProperty(aboutLink).getString());
				if (r.hasProperty(adminPasshash))
					this.setPassHash(r.getProperty(adminPasshash).getString());
				
				for (Statement s2 : new IteratorToIIterableIterator<Statement>(m.listStatements(r, null, (RDFNode) null)))			
					toBeDeleted.add(s2);
			}		
		}
		catch (Exception e)
		{
			log.error("Malformed SAHA project RDF metadata in model:");
			log.error(e.getMessage());
		}


		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, OWL.Class)))
		{
			try
			{
				Resource r = s.getSubject();

				if (r.hasProperty(hasPropertyOrder))
				{
					Resource seq = r.getProperty(hasPropertyOrder).getResource();
					
					String baseUri = RDF.getURI() + "_";
					
					List<String> propertyOrder = new ArrayList<String>();	

					int i = 1;
					
					while (seq.hasProperty(m.getProperty(baseUri + i)))
					{
						Resource next = seq.getProperty(m.getProperty(baseUri + i)).getResource();
						propertyOrder.add(next.getURI());
						
						log.info("Added property order for position " + i + " : " + next.getURI());
						i++;
					}
					
					this.setPropertyOrder(r.getURI(), propertyOrder);
					
					for (Statement s2 : new IteratorToIIterableIterator<Statement>(m.listStatements(seq, null, (RDFNode) null)))			
						toBeDeleted.add(s2);
				}
			}
			catch (Exception e)
			{
				log.error("Malformed SAHA class RDF metadata in model:");
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}




		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, OWL.ObjectProperty)))
		{
			try
			{
				Resource r = s.getSubject();

				for (Statement s2 : new IteratorToIIterableIterator<Statement>(m.listStatements(r, hasRepositoryConfig, (RDFNode) null)))
				{
					Resource r2 = s2.getResource();

					RepositoryConfig rc = new RepositoryConfig();

					if (r2.hasProperty(hasRepositorySource))
						rc.setSourceName(r2.getProperty(hasRepositorySource).getString());

					Set<String> typeRestrictions = new HashSet<String>();
					for (Statement s3 : new IteratorToIIterableIterator<Statement>(m.listStatements(r, hasTypeRestriction, (RDFNode) null)))
					{
						typeRestrictions.add(s3.getString());
					}				
					rc.setTypeRestrictions(typeRestrictions);

					Set<String> parentRestrictions = new HashSet<String>();
					for (Statement s3 : new IteratorToIIterableIterator<Statement>(m.listStatements(r, hasParentRestriction, (RDFNode) null)))
					{
						parentRestrictions.add(s3.getString());
					}				
					rc.setParentRestrictions(parentRestrictions);

					this.addRepositoryConfig(r.getURI(), rc);
					log.info("Added repository config for " + r.getURI());
					
					for (Statement s3 : new IteratorToIIterableIterator<Statement>(m.listStatements(r2, null, (RDFNode) null)))			
						toBeDeleted.add(s3);
				}

				if (r.hasProperty(denyLocalReferences))
					if (!this.toggleDenyLocalReferences(r.getURI()))
						this.toggleDenyLocalReferences(r.getURI());
				if (r.hasProperty(denyNewInstances))
					if (!this.toggleDenyInstantiation(r.getURI()))
						this.toggleDenyInstantiation(r.getURI());
			}
			catch (Exception e)
			{
				log.error("Malformed SAHA object property RDF metadata in model:");
				log.error(e.getMessage());
			}
		}



		for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, RDF.type, OWL.DatatypeProperty)))
		{
			try
			{
				Resource r = s.getSubject();

				if (r.hasProperty(isHidden))
					if (!this.toggleHidden(r.getURI()))
						this.toggleHidden(r.getURI());
				if (r.hasProperty(isLocalized))
					if (!this.toggleLocalized(r.getURI()))
						this.toggleLocalized(r.getURI());
				if (r.hasProperty(isPictureProperty))
					if (!this.togglePictureProperty(r.getURI()))
						this.togglePictureProperty(r.getURI());
			}
			catch (Exception e)
			{
				log.error("Malformed SAHA literal property RDF metadata in model:");
				log.error(e.getMessage());
			}
		}
		
		for (Property p : tbd)
		{
			for (Statement s : new IteratorToIIterableIterator<Statement>(m.listStatements(null, p, (RDFNode) null)))			
				toBeDeleted.add(s);
		}
		
		m.remove(toBeDeleted);
	}

}
