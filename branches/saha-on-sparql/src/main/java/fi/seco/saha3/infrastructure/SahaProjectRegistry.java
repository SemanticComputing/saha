package fi.seco.saha3.infrastructure;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

import fi.seco.saha3.model.SahaProject;

/**
 * General manager class for SAHA. Controls the creation, deletion and 
 * management of SAHA projects.
 * 
 * The singleton bean instance of this class is injected into many other
 * beans and is the main point of entry into SAHA projects and their
 * configuration and data. 
 * 
 */
public class SahaProjectRegistry {
	
	private Logger log = Logger.getLogger(getClass());
	
	private String projectBaseDirectory;
	
	private Map<String,XmlBeanFactory> beanFactoryMap = new HashMap<String,XmlBeanFactory>();
	private Map<String,SahaProject> projects = new HashMap<String,SahaProject>();
	private final Map<String, ReentrantReadWriteLock> projectLocks = new HashMap<String, ReentrantReadWriteLock>();
	
	@Required
	public void setProjectBaseDirectory(String projectBaseDirectory) {
		if (!projectBaseDirectory.endsWith("/")) projectBaseDirectory += "/";
		this.projectBaseDirectory = projectBaseDirectory;
	}
	
	public String getProjectBaseDirectory() {
		return projectBaseDirectory;
	}
	
	public synchronized SahaProject getSahaProject(String modelName) {
		return getSahaProject(modelName,false);
	}
	
	public synchronized SahaProject getSahaProject(String modelName, boolean createNew) {
		if (projects.containsKey(modelName)) 
			return projects.get(modelName);
		if (projectExists(modelName) || createNew) {
			projects.put(modelName,initProject(modelName));
			return projects.get(modelName);
		}
		return null;
	}
	
	private SahaProject initProject(String projectName) {
		if (!projects.containsKey(projectName))
			projects.put(projectName,(SahaProject)getBean(projectName,"project"));
		return projects.get(projectName);
	}
	
	public Model getModel(String projectName) {
		return (Model)getBean(projectName,"model");
	}
	
	public ReentrantReadWriteLock getLockForProject(String project)
	{
		if (!this.projectLocks.containsKey(project))
			this.projectLocks.put(project, new ReentrantReadWriteLock());
		
		return this.projectLocks.get(project);
	}
	
	private Object getBean(String projectName, String beanName) {
		return getBeanFactory(projectName).getBean(beanName);
	}
	
	private XmlBeanFactory getBeanFactory(String projectName) {
		if (!beanFactoryMap.containsKey(projectName)) {
			XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource("/saha3-project-beans.xml"));
			// register "projectPath"
			ConstructorArgumentValues args = new ConstructorArgumentValues();
			args.addGenericArgumentValue(projectBaseDirectory+projectName);
			bf.registerBeanDefinition("projectPath",new RootBeanDefinition(String.class,args,null));
			beanFactoryMap.put(projectName,bf);
		}
		return beanFactoryMap.get(projectName);
	}
	
	public boolean closeSahaProject(String modelName) {
		if (projects.containsKey(modelName)) {
			beanFactoryMap.get(modelName).destroySingletons();
			beanFactoryMap.remove(modelName);
			projects.remove(modelName);
			return true;
		}
		return false;
	}

	public boolean projectExists(String modelName) {
		File projectDir = new File(projectBaseDirectory+modelName);
		return projectDir.exists() && projectDir.isDirectory();
	}
	
	public Set<String> getOpenedProjects() {
		return projects.keySet();
	}
	
	public Set<String> getAllProjects() {
		Set<String> projectNames = new TreeSet<String>();
		for (File f : new File(projectBaseDirectory).listFiles())
			if (f.isDirectory()) projectNames.add(f.getName());
		return projectNames;
	}
	
	public void importModelFromFile(String inputFile, String modelName) {
		log.info("Reading model from file...");
		String lang = "RDF/XML";
		if (inputFile.endsWith("ttl")) lang = "TTL";
		if (inputFile.endsWith("nt")) lang = "N-TRIPLE";
		importModel(modelName,FileManager.get().open(inputFile),lang);
		log.info("Done.");
	}
	
	public void importModel(String modelName, InputStream in, String lang) {
		log.info("Importing project: " + modelName);
		SahaProject project = getSahaProject(modelName,true);
		project.readModel(in,lang);
		log.info("Done.");
	}
	
	public void rewriteModel(String modelName, InputStream in, String lang) {
		log.info("Rewriting project: " + modelName);
		SahaProject project = getSahaProject(modelName);
		project.clear();
		project.readModel(in,lang);		
		closeSahaProject(modelName);
		log.info("Done.");
	}

	public void mergeModel(String modelName, InputStream in, String lang) {
		log.info("Merging project: " + modelName);
		Model model = ModelFactory.createDefaultModel();
		model.read(in,"",lang);
		SahaProject project = getSahaProject(modelName);
		project.addModel(model);
		log.info("Done.");
	}
	
	public boolean deleteProject(String modelName)
	{
	    closeSahaProject(modelName);
	    
	    File projectDir = new File(projectBaseDirectory+modelName);
        return deleteDir(projectDir);
	}
	
	private static boolean deleteDir(File dir) {
        if (dir.isDirectory())
        {
            for (File file : dir.listFiles()) 
            {
                boolean success = deleteDir(file);
                if (!success) 
                    return false;                
            }
        }

        return dir.delete();
    }
	
	public void gc() {
		Runtime.getRuntime().gc();
	}
	
	public void close() {
		for (String projectName : projects.keySet())
			closeSahaProject(projectName);
	}
	
}
