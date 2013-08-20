/**
 * 
 */
package fi.seco.saha3.model;

/**
 * @author jiemakel
 * 
 */
public interface IRequestLogger {

	public void setObjectProperty(String remoteHost, String s, String p, UriLabel o);

	public void createInstance(String remoteHost, String uri, String type, String label);

	public void setMapProperty(String remoteHost, String s, String fc, String value);

	public void removeLiteralProperty(String remoteHost, String s, String p, UriLabel object);

	public void removeResource(String remoteHost, String uri);

	public void removeObjectProperty(String remoteHost, String s, String p, String o);

	public void removeProperty(String remoteHost, String s, String p);

	public String getLastLog(int lines);

	public void setLiteralProperty(String remoteHost, String s, String p, UriLabel object);

	public void updateLiteralProperty(String remoteHost, String s, String p, UriLabel removed, UriLabel object);
}
