package fi.seco.saha3.model.configuration;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;

public class XMLConfigStore {
	
	private Logger log = Logger.getLogger(getClass());

	private File file;
	private ProjectConfig config;
	
	public XMLConfigStore(String file) {
		this(new File(file));
	}
	
	public XMLConfigStore(File file) {
		this.file = file;
		log.info("Initializing project configuration from: " + file.getAbsolutePath());
		load();
	}

	public void setProjectConfig(ProjectConfig config) {
		this.config = config;
	}
	
	public ProjectConfig getProjectConfig() {
		return config;
	}
	
	public synchronized void load() {
		if (file.exists()) {
			try {
				log.debug("Loading configuration from: " + file.getAbsolutePath());
				XMLDecoder decoder = new XMLDecoder(new FileInputStream(file));
				config = (ProjectConfig)decoder.readObject();
				decoder.close(); 
				log.debug("Done.");
			} catch (FileNotFoundException e) {
				log.error("",e);
			}
		} else {
			log.info("Creating a new project configuration.");
			config = new ProjectConfig();
		}
	}
	
	public synchronized void save() {
		try {
			log.debug("Saving configuration to: " + file.getAbsolutePath());
			FileOutputStream out = new FileOutputStream(file);
			XMLEncoder encoder = new XMLEncoder(out);
			encoder.writeObject(config);
			encoder.close();
			log.debug("Done.");
		} catch (FileNotFoundException e) {
			log.error("",e);
		}
	}
	
	@Override
	public synchronized String toString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(out);
		encoder.writeObject(config);
		encoder.close();
		return out.toString();
	}
	
}
