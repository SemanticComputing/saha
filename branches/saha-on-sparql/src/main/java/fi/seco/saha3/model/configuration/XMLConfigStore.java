package fi.seco.saha3.model.configuration;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The persistence wrapper for SAHA configuration objects. Serializes the
 * objects as XML to the SAHA project directory each time a change is made.
 * 
 */
public class XMLConfigStore {

	private static final Logger log = LoggerFactory.getLogger(XMLConfigStore.class);

	private final File file;
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
		if (file.exists())
			try {
				if (log.isDebugEnabled()) log.debug("Loading configuration from: " + file.getAbsolutePath());
				XMLDecoder decoder = new XMLDecoder(new FileInputStream(file));
				config = (ProjectConfig) decoder.readObject();
				decoder.close();
				if (log.isDebugEnabled()) log.debug("Done.");
			} catch (FileNotFoundException e) {
				log.error("", e);
			}
		else {
			log.info("Creating a new project configuration.");
			config = new ProjectConfig();
		}
	}

	public synchronized void save() {
		try {
			if (log.isDebugEnabled()) log.debug("Saving configuration to: " + file.getAbsolutePath());
			FileOutputStream out = new FileOutputStream(file);
			XMLEncoder encoder = new XMLEncoder(out);
			encoder.writeObject(config);
			encoder.close();
			if (log.isDebugEnabled()) log.debug("Done.");
		} catch (FileNotFoundException e) {
			log.error("", e);
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
