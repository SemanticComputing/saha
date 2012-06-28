package fi.seco.saha3.web.control;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import fi.seco.saha3.infrastructure.ResourceLockManager;
import fi.seco.saha3.infrastructure.SahaProjectRegistry;
import fi.seco.saha3.util.CircularFifoAppender;

/**
 * Controller for the general admin view of SAHA
 * 
 */
public class AdminController extends AbstractController {

	private SahaProjectRegistry sahaProjectRegistry;
    private ResourceLockManager lockManager;
	private Runtime runtime = Runtime.getRuntime();
	private long startTime = System.currentTimeMillis();
	
	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
		this.sahaProjectRegistry = sahaProjectRegistry;
	}
    
    @Required
    public void setLockManager(ResourceLockManager lockManager) {
    	this.lockManager = lockManager;
    }

	@Override
	public ModelAndView handleRequestInternal(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		
		ModelAndView mav = new ModelAndView();
		
		mav.addObject("allProjects",sahaProjectRegistry.getAllProjects());
		mav.addObject("openedProjects",sahaProjectRegistry.getOpenedProjects());
		mav.addObject("locks",lockManager.getLockedResourcesByOwner().entrySet());
		mav.addObject("messages",CircularFifoAppender.getMessages("saha3admin"));
		mav.addObject("backupMessages",CircularFifoAppender.getMessages("backupAppender"));

		mav.addObject("mem",getMemoryUsage());
		mav.addObject("uptime",getUptime());
		
		return mav;
	}

	private double getMemoryUsage() {
		return (runtime.totalMemory()-runtime.freeMemory())/1048576.0;
	}

	private String getUptime() {
		long uptime = System.currentTimeMillis()-startTime;
		long d = TimeUnit.MILLISECONDS.toDays(uptime);
		long h = TimeUnit.MILLISECONDS.toHours(uptime);
		long m = TimeUnit.MILLISECONDS.toMinutes(uptime);
		if (d > 0) return d + " days "  + (h-TimeUnit.DAYS.toHours(d)) + " hours and " + (m-TimeUnit.HOURS.toMinutes(h)) + " minutes";
		if (h > 0) return h + " hours " + (m-TimeUnit.HOURS.toMinutes(h)) + " minutes";
		return m + " minutes";
	}
	
}
