package fi.seco.semweb.infrastructure.springmvc;

import java.util.Locale;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

public class SmarterFreeMarkerViewResolver extends FreeMarkerViewResolver {

	@Override
	protected View createView(String viewName, Locale locale) {
		try {
			return super.createView(viewName, locale);
		} catch (Exception e) {
			return null;
		}
	}

}
