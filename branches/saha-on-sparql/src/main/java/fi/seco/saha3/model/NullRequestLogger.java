/**
 * 
 */
package fi.seco.saha3.model;

/**
 * @author jiemakel
 * 
 */
public class NullRequestLogger implements IRequestLogger {

	@Override
	public void setObjectProperty(String remoteHost, String s, String p, UriLabel object) {}

	@Override
	public void createInstance(String remoteHost, String uri, String type, String label) {}

	@Override
	public void setLiteralProperty(String remoteHost, String s, String p, UriLabel object) {}

	@Override
	public void updateLiteralProperty(String remoteHost, String s, String p, UriLabel removed, UriLabel added) {}

	@Override
	public void setMapProperty(String remoteHost, String s, String fc, String value) {}

	@Override
	public void removeLiteralProperty(String remoteHost, String s, String p, UriLabel object) {}

	@Override
	public void removeResource(String remoteHost, String uri) {}

	@Override
	public void removeObjectProperty(String remoteHost, String s, String p, String o) {}

	@Override
	public void removeProperty(String remoteHost, String s, String p) {}

	@Override
	public String getLastLog(int lines) {
		return "";
	}

}
