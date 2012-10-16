package fi.seco.saha3.util;

import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * SAHA-wide constants should be placed here. Note that all the constant-like
 * variables are not set here, and spread throughout their respective classes
 * instead (e.g. <code>fi.seco.saha3.model.configuration.ConfigService</code>).
 * 
 */
public class SAHA3 {

	public static final Property dateCreated = ResourceFactory.createProperty("http://seco.tkk.fi/saha3/dateCreated");
	public static final Property dateModified = ResourceFactory.createProperty("http://seco.tkk.fi/saha3/dateModified");

	public static final String WGS84_LAT = "http://www.w3.org/2003/01/geo/wgs84_pos#lat";
	public static final String WGS84_LONG = "http://www.w3.org/2003/01/geo/wgs84_pos#long";
	public static final String POLYGON_URI = "http://www.yso.fi/onto/sapo/hasPolygon";
	public static final String ROUTE_URI = "http://www.yso.fi/onto/sapo/hasRoute";

	public static String generateRandomUri(String namespace) {
		return namespace + "u" + UUID.randomUUID().toString();
	}

}
