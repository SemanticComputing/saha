package fi.seco.saha3.web.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.WebContentGenerator;
import fi.seco.saha3.infrastructure.SahaProjectRegistry;

/**
 * Controller for showing the front page (project list and new project prompt)
 * of SAHA.
 * 
 */
public class FrontPageController extends WebContentGenerator
{    
    private SahaProjectRegistry sahaProjectRegistry;
    
    @Required
    public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
        this.sahaProjectRegistry = sahaProjectRegistry;
    }
    
    @RequestMapping("/")
    public String redirectRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
    	return "redirect:/saha3/main.shtml";
    }
    
    @RequestMapping("/saha3/")
    public String redirect2Request(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
    	return "redirect:/saha3/main.shtml";
    }
    
    @RequestMapping("/saha3/main.shtml")
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        ModelAndView mav = new ModelAndView("saha3/main");
        
        mav.addObject("projectList", this.sahaProjectRegistry.getAllProjects());
        
        return mav;
    }
    
}
