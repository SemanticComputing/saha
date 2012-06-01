package fi.seco.saha3.util;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A custom log message layout utilized in the admin interface of SAHA to
 * directly show system log messages.
 * 
 */
public class HTMLPatternLayout extends PatternLayout {

	@Override
	public String format(LoggingEvent event) {
		String msg = super.format(event);
		msg = msg.replace("ERROR","<strong style=\"color:red\">ERROR</strong>");
		msg = msg.replace("WARN", "<span style=\"color:red\">WARN</span>");
		msg = msg.replace("INFO", "<span style=\"color:green\">INFO</span>");
		return msg;
	}
	
}
