package fi.seco.saha3.model;

import java.io.InputStream;
import java.util.Locale;

import com.hp.hpl.jena.rdf.model.Model;


public interface IModelEditor {
	
	UriLabel addObjectProperty(String s, String p, String o, Locale locale);
	boolean removeObjectProperty(String s, String p, String o);
	
	UriLabel addLiteralProperty(String s, String p, String l);
	UriLabel addLiteralProperty(String s, String p, String l, String lang);
	boolean removeLiteralProperty(String s, String p, String valueShaHex);
	
	boolean removeProperty(String s, String p);
	
	boolean addModel(Model m);
	boolean readModel(InputStream in, String lang);
	
	String createResource(String type, String label);
	String createResource(String uri, String type, String label);
	boolean removeResource(String uri);
	
	boolean clear();
	
}
