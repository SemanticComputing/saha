package fi.seco.saha3.chat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.buffer.CircularFifoBuffer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

public class SahaChat implements DisposableBean {

	private class Channel {
		private Collection<Message> messages;
		private PrintWriter logWriter;
		private Channel(String channelName) {
			this.messages = new CircularFifoBuffer<Message>(15);
			try {
				File logFile = new File(projectDir + channelName + "/chat.log");
				this.logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFile,true)));
				log.info("logging to: " + logFile);
			} catch (IOException e) { 
				this.logWriter = new PrintWriter(System.out);
				log.error(e);
				log.error("Failed to open log file. Logging to System.out.");
			}
		}
		public void addMessage(String name, String message) {
		    name = StringEscapeUtils.escapeHtml(name);
		    message = StringEscapeUtils.escapeHtml(message);
			addMessage(new Message(name,message));
		}
		public void addMessage(Message msg) {
			messages.add(msg);
			logWriter.println(msg.getTime() + " <" + msg.getName() + "> " + msg.getMessage());
		}
		public Collection<Message> getMessages() {
			return messages;
		}
		public void clear() {
			messages.clear();
		}
		public void flush() {
			logWriter.flush();
		}
		public void close() {
			logWriter.close();
		}
	}
	
	private Logger log = Logger.getLogger(getClass());
	
	private Map<String,Channel> channels = new HashMap<String,Channel>();
	private String projectDir;
	
	public void setProjectBaseDirectory(String projectBaseDirectory) {
		if (!projectBaseDirectory.endsWith("/")) projectBaseDirectory += "/";
		this.projectDir = projectBaseDirectory;
	}
	
	public synchronized void broadcast(String name, String message) {
		for (Channel c : channels.values()) c.addMessage(name,message);
	}
	
	public synchronized void addMessage(String channelName, String name, String message) {
		Channel channel = getChannel(channelName);
		if (message != null) {
			message = message.trim();
			if (!message.isEmpty()) {
				if (message.equals("/clear")) channel.clear();
				else if (message.equals("/flush")) channel.flush();
				else channel.addMessage(name,message);
			}
		}
	}
	
	public synchronized Collection<Message> getMessages(String channelName) {
		return getChannel(channelName).getMessages();
	}

	private Channel getChannel(String channelName) {
		if (!channels.containsKey(channelName)) 
			channels.put(channelName,new Channel(channelName));
		return channels.get(channelName);
	}

	public void destroy() throws Exception {
		for (Channel channel : channels.values())
			channel.close();
	}
	
}
