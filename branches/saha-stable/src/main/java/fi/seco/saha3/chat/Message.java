package fi.seco.saha3.chat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
	private static DateFormat df = new SimpleDateFormat("dd.MM HH:mm");
	private String name;
	private String message;
	private long timestamp;
	
	public Message(String name, String message) {
		this.name = name;
		this.message = message;
		this.timestamp = System.currentTimeMillis();
	}
	public String getName() {
		return name;
	}
	public String getMessage() {
		return message;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public String getTime() {
		return df.format(new Date(getTimestamp()));
	}
}
