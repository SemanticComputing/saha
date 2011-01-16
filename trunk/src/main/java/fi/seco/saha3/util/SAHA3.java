package fi.seco.saha3.util;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SAHA3 {

	public static final Property dateCreated = 
		ResourceFactory.createProperty("http://seco.tkk.fi/saha3/dateCreated");
	public static final Property dateModified = 
		ResourceFactory.createProperty("http://seco.tkk.fi/saha3/dateModified");
	
}
