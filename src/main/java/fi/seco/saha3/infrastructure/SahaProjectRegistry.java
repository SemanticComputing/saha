package fi.seco.saha3.infrastructure;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import fi.seco.saha3.model.IModelEditor;
import fi.seco.saha3.model.IModelLister;
import fi.seco.saha3.model.IModelReader;
import fi.seco.saha3.model.ISahaProject;
import fi.seco.saha3.model.SahaProject;
import fi.seco.saha3.model.configuration.IConfigService;

/**
 * General manager class for SAHA. Controls the creation, deletion and
 * management of SAHA projects.
 * 
 * The singleton bean instance of this class is injected into many other beans
 * and is the main point of entry into SAHA projects and their configuration and
 * data.
 * 
 */
public class SahaProjectRegistry {

	private final Logger log = Logger.getLogger(getClass());

	private String projectBaseDirectory;

	private final Map<String, XmlBeanFactory> beanFactoryMap = new HashMap<String, XmlBeanFactory>();
	private final Map<String, SahaProject> projects = new HashMap<String, SahaProject>();
	private final Map<String, ReentrantReadWriteLock> projectLocks = new HashMap<String, ReentrantReadWriteLock>();

	private IModelLister ml;

	@Required
	public void setModelLister(IModelLister ml) {
		this.ml = ml;
	}

	@Required
	public void setProjectBaseDirectory(String projectBaseDirectory) {
		if (!projectBaseDirectory.endsWith("/")) projectBaseDirectory += "/";
		this.projectBaseDirectory = projectBaseDirectory;
	}

	public String getProjectBaseDirectory() {
		return projectBaseDirectory;
	}

	public IConfigService getConfig(String modelName) {
		return getSahaProject(modelName, false);
	}

	public IModelReader getModelReader(String modelName) {
		return getModelReader(modelName, false);
	}

	public IModelReader getModelReader(String modelName, boolean createNew) {
		return getSahaProject(modelName, createNew);
	}

	private synchronized ISahaProject getSahaProject(String modelName, boolean createNew) {
		if (projects.containsKey(modelName)) return projects.get(modelName);
		if (projectExists(modelName) || createNew) {
			projects.put(modelName, initProject(modelName));
			return projects.get(modelName);
		}
		return null;
	}

	public IModelEditor getModelEditor(String modelName) {
		return getModelEditor(modelName, false);
	}

	public IModelEditor getModelEditor(String modelName, boolean createNew) {
		return getSahaProject(modelName, createNew);
	}

	private SahaProject initProject(String projectName) {
		if (!projects.containsKey(projectName))
			projects.put(projectName, (SahaProject) getBean(projectName, "project"));
		return projects.get(projectName);
	}

	public ReentrantReadWriteLock getLockForProject(String project) {
		if (!this.projectLocks.containsKey(project)) this.projectLocks.put(project, new ReentrantReadWriteLock());

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
			args.addGenericArgumentValue(projectBaseDirectory + projectName);
			bf.registerBeanDefinition("projectPath", new RootBeanDefinition(String.class, args, null));
			if (projectName.contains(";")) {
				String[] addr = projectName.split(";");
				args = new ConstructorArgumentValues();
				args.addGenericArgumentValue(addr[0]);
				bf.registerBeanDefinition("SPARQLService", new RootBeanDefinition(String.class, args, null));
				args = new ConstructorArgumentValues();
				args.addGenericArgumentValue(addr[1]);
				bf.registerBeanDefinition("SPARULService", new RootBeanDefinition(String.class, args, null));
				args = new ConstructorArgumentValues();
				if (addr.length == 2)
					args.addGenericArgumentValue("default");
				else args.addGenericArgumentValue(addr[2]);
				bf.registerBeanDefinition("projectIdentifier", new RootBeanDefinition(String.class, args, null));
				if (addr.length > 3) {
					args = new ConstructorArgumentValues();
					args.addGenericArgumentValue(addr[3]);
				}
				bf.registerBeanDefinition("projectIdentifierUpdate", new RootBeanDefinition(String.class, args, null));
			} else {
				args = new ConstructorArgumentValues();
				args.addGenericArgumentValue(projectName);
				bf.registerBeanDefinition("projectIdentifier", new RootBeanDefinition(String.class, args, null));
				bf.registerBeanDefinition("projectIdentifierUpdate", new RootBeanDefinition(String.class, args, null));
			}
			beanFactoryMap.put(projectName, bf);
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
		return modelName.contains(";") || ml.modelExists(modelName);
	}

	public Set<String> getOpenedProjects() {
		return projects.keySet();
	}

	public Collection<String> getAllProjects() {
		return ml.getModels();
	}

	public boolean deleteProject(String modelName) {
		closeSahaProject(modelName);

		File projectDir = new File(projectBaseDirectory + modelName);
		return deleteDir(projectDir);
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) for (File file : dir.listFiles()) {
			boolean success = deleteDir(file);
			if (!success) return false;
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
