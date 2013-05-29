package fi.seco.semweb.infrastructure;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SecoRegistry {

	private static ApplicationContext context;

	private static final Logger log = LoggerFactory.getLogger(SecoRegistry.class);
	private static String path = "context";

	public SecoRegistry() {}

	public static class SecoRegistryConfigurator {
		public Collection<String> getContextLocations() {
			ArrayList<String> tmp = new ArrayList<String>();
			tmp.add(path + "/WEB-INF/classes/applicationContext.xml");
			return tmp;
		}

		public ApplicationContext getContext() {
			FileSystemXmlApplicationContext ret = new FileSystemXmlApplicationContext(getContextLocations().toArray(new String[0]));
			ret.registerShutdownHook();
			return ret;
		}
	}

	private static SecoRegistryConfigurator configurator = new SecoRegistryConfigurator();

	public static void setConfigurator(SecoRegistryConfigurator configurator) {
		SecoRegistry.configurator = configurator;
	}

	public static ApplicationContext getContext() {
		if (context == null) setContext(configurator.getContext());
		return context;
	}

	public static void setContext(ApplicationContext context) {
		SecoRegistry.context = context;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(String beanName) {
		log.warn("SecoRegistry getService called for service " + beanName + ". You really should be using Spring to provide your services.");
		return (T) getContext().getBean(beanName);
	}

	public static Class<?> getType(String beanName) {
		return getContext().getType(beanName);
	}

	public static <T> T getService(Class<T> clazz) {
		return getService(clazz.getName(), clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(String beanName, Class<T> clazz) {
		log.warn("SecoRegistry getService called for service " + beanName + ". You really should be using Spring to provide your services.");
		return getContext().getBean(beanName, clazz);
	}

}
