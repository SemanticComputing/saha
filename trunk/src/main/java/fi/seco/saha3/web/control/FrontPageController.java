package fi.seco.saha3.web.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

/**
 * Controller for showing the front page (project list and new project prompt)
 * of SAHA.
 * 
 */
public class FrontPageController implements Controller
{    
    private SahaProjectRegistry sahaProjectRegistry;
    
    @Required
    public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
        this.sahaProjectRegistry = sahaProjectRegistry;
    }
    
    public ModelAndView handleRequest(HttpServletRequest request,
        HttpServletResponse response) throws Exception
    {
        ModelAndView mav = new ModelAndView("saha3/main");
        
        mav.addObject("projectList", this.sahaProjectRegistry.getAllProjects());
        
        return mav;
    }
    
}
