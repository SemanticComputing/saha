package fi.seco.saha3.web.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ProjectRedirectInterceptor extends HandlerInterceptorAdapter {
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		
		String requestURI = request.getRequestURI();
		if (requestURI.contains("/project/")) {
			String projectName = requestURI.substring(requestURI.lastIndexOf('/')+1);
			response.sendRedirect("../" + projectName + "/index.shtml");
			return false;
		}
		
		return true;
	}
	
}
