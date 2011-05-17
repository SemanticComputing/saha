package fi.seco.saha3.infrastructure;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ws.transport.http.WsdlDefinitionHandlerAdapter;

public class SahaWsdlDefinitionHandlerAdapter extends WsdlDefinitionHandlerAdapter {

	private String baseURL;

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	@Override
	protected String transformLocation(String location, HttpServletRequest request) {
		int lp = location.lastIndexOf('/');
		if (lp != -1) location = location.substring(lp + 1);
		if (baseURL != null) return baseURL + "/" + location;
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(request.getServerName());
		sb.append(':');
		sb.append(request.getServerPort());
		String sp = request.getRequestURI();
		lp = sp.lastIndexOf('/');
		if (lp != -1) sp = sp.substring(0, lp + 1);
		sb.append(sp);
		sb.append(location);
		return sb.toString();
	}
}
