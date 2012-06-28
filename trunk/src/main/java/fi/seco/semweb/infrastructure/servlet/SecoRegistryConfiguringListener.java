package fi.seco.semweb.infrastructure.servlet;

import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;

import fi.seco.semweb.infrastructure.SecoRegistry;

public class SecoRegistryConfiguringListener extends ContextLoaderListener {

	@Override
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		SecoRegistry.setContext(WebApplicationContextUtils.getRequiredWebApplicationContext(event.getServletContext()));
	}

}
