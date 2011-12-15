package fi.seco.saha3.util;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * SAHA-wide constants should be placed here. Note that all the constant-like
 * variables are not set here, and spread throughout their respective classes
 * instead (e.g. <code>fi.seco.saha3.model.configuration.ConfigService</code>).
 * 
 */
public class SAHA3 {

	public static final Property dateCreated = 
		ResourceFactory.createProperty("http://seco.tkk.fi/saha3/dateCreated");
	public static final Property dateModified = 
		ResourceFactory.createProperty("http://seco.tkk.fi/saha3/dateModified");
	
}
