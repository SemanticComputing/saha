package fi.seco.saha3.web.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

@Controller
public class ImportProjectController {

    private SahaProjectRegistry sahaProjectRegistry;
    private Logger log = Logger.getLogger(getClass());
    
    @Required
    public void setSahaProjectRegistry(SahaProjectRegistry sahaProjectRegistry) {
        this.sahaProjectRegistry = sahaProjectRegistry;
    }
    
    @RequestMapping("/service/import_project/")
    protected ModelAndView handle(HttpServletRequest request, 
            HttpServletResponse response, 
            @RequestParam("file") MultipartFile file,
            @RequestParam("modelName") String name,
            @RequestParam("operation") String operation) throws Exception {
        
        log.info("Importing: " + name + " (" + file + ")");
        
        if (file == null) 
            response.getWriter().write("File is missing.");
        else if (name ==null || name.isEmpty())
            response.getWriter().write("Name is missing.");
        else {
            String lang = "RDF/XML";
            if (file.getOriginalFilename().endsWith("ttl")) lang = "TTL";
            if (file.getOriginalFilename().endsWith("nt")) lang = "N-TRIPLE";
            
            if (operation.equals("import"))
                sahaProjectRegistry.importModel(name,file.getInputStream(),lang);
            if (operation.equals("rewrite"))
                sahaProjectRegistry.rewriteModel(name,file.getInputStream(),lang);
            if (operation.equals("merge"))
                sahaProjectRegistry.mergeModel(name,file.getInputStream(),lang);
        }
        response.sendRedirect("../../" + name + "/index.shtml");
        return null;
    }

}


