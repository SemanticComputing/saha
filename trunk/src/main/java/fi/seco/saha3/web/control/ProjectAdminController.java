package fi.seco.saha3.web.control;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.model.SahaProject;


public class ProjectAdminController extends ASahaController 
{   
    private final static String DEFAULT_PASS = "rhema04";

    private Log log = LogFactory.getLog(getClass());
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
        HttpServletResponse response, SahaProject project, Locale locale,
        ModelAndView mav) throws Exception
    {
        String password = request.getParameter("password");
        String passhash = request.getParameter("passhash");
        
        if ((password == null || password.isEmpty())
            && (passhash == null || passhash.isEmpty()))
        {
            // Need authorization
            mav.addObject("authorized", false);            
            return mav;
        }
        
        String givenPassHash = (passhash == null) ? DigestUtils.shaHex(password) : passhash;        
        String realPassHash = project.getConfig().getPassHash();
        
        if (realPassHash == null || realPassHash.isEmpty())
            realPassHash = DigestUtils.shaHex(DEFAULT_PASS);
                
        if (givenPassHash.equals(realPassHash))
        {
            // Authorized
            mav.addObject("authorized", true);
            mav.addObject("passhash", givenPassHash);
            
            String operation = request.getParameter("operation");
            String confirm = request.getParameter("confirm");
            
            if (operation != null && operation.equals("delete") 
                && confirm != null && confirm.equals("true"))
            {                
                // Delete project
                String model = parseModelName(request.getServletPath());
                log.info("Deleting project: " + model);
                this.getSahaProjectRegistry().deleteProject(model);
                                
                response.sendRedirect("../saha3/main.shtml");
                return null;
            }
            
            String newPass1 = request.getParameter("newPass");
            String newPass2 = request.getParameter("newPass2");
            
            if (newPass1 != null && !newPass1.isEmpty() && newPass1.equals(newPass2))
            {
                // Change password
                log.info("Changing password for project:" + parseModelName(request.getServletPath()));
                project.getConfig().setPassHash(DigestUtils.shaHex(newPass1));
                mav.addObject("message", "Password changed");
            }
        }
        else
        {
            // Access forbidden
            mav.addObject("authorized", false);
            mav.addObject("message", "Invalid password");
        }
        
        return mav;
    }
    
}
