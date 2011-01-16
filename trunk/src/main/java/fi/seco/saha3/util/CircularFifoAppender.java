package fi.seco.saha3.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;

public class CircularFifoAppender extends AppenderBase<ILoggingEvent> {
	
	private static Map<String,Collection<String>> messageBuffers = 
		new HashMap<String,Collection<String>>();
	
	public static Collection<String> getMessages(String appenderName) {
		if (!messageBuffers.containsKey(appenderName)) 
			messageBuffers.put(appenderName,new CircularFifoBuffer<String>(200));
		return messageBuffers.get(appenderName);
	}

	protected void append(ILoggingEvent e) {
		IThrowableProxy t = e.getThrowableProxy();
		getMessages(name).add(t==null?e.getFormattedMessage():parseThrowableInformation(t));
	}
	
	private String parseThrowableInformation(IThrowableProxy t) {
		StringBuilder buffer = new StringBuilder();
		for (StackTraceElementProxy s : t.getStackTraceElementProxyArray()) buffer.append(s.toString()+'\n');
		return buffer.toString();
	}

}
