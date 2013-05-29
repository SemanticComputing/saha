package fi.seco.semweb.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class FreeMarkerUtil {

	private static final Logger log = LoggerFactory.getLogger(FreeMarkerUtil.class);

	public static String process(Configuration configuration, String template, Map<String, Object> model) {
		StringWriter out = new StringWriter();
		try {
			configuration.getTemplate(template).process(model, out);
		} catch (TemplateException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		}
		return out.toString();
	}
}
