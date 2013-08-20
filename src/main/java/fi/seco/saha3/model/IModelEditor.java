package fi.seco.saha3.model;

import java.util.Locale;

public interface IModelEditor {

	public UriLabel addObjectProperty(String s, String p, String o, Locale locale);

	public void removeObjectProperty(String s, String p, String o);

	public UriLabel addLiteralProperty(String s, String p, String l);

	public UriLabel addLiteralProperty(String s, String p, String l, String lang);

	public UriLabel removeLiteralProperty(String s, String p, String valueShaHex);

	public void removeProperty(String s, String p);

	public String createResource(String type, String label);

	public String createResource(String uri, String type, String label);

	public void removeResource(String uri);

	public void clear();

	public void setMapProperty(String s, String fc, String value);

}
