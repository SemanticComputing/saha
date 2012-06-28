package fi.seco.semweb.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class FreeMarkerUtil {

	private static final Log log = LogFactory.getLog(FreeMarkerUtil.class);

	public static String process(Configuration configuration, String template, Map<String,Object> model) {
		StringWriter out = new StringWriter();
		try {
			configuration.getTemplate(template).process(model, out);
		} catch (TemplateException e) {
			log.error("",e);
		} catch (IOException e) {
			log.error("",e);
		}
		return out.toString();
	}
}
