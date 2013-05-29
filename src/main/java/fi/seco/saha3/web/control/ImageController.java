package fi.seco.saha3.web.control;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

/**
 * Controller for showing uploaded images in SAHA.
 * 
 */
public class ImageController implements Controller {
	private static final String LEGACY_PREFIX = "http://demo.seco.tkk.fi/";
	private static final Map<String, String> MIME_MAP = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("jpg", "image/jpeg");
			put("jpeg", "image/jpeg");
			put("gif", "image/gif");
			put("png", "image/png");
		}
	};

	private static final Logger log = LoggerFactory.getLogger(ImageController.class);

	private SahaProjectRegistry registry;

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry registry) {
		this.registry = registry;
	}

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String model = request.getParameter("model");
		String picName = request.getParameter("name");

		if (log.isDebugEnabled()) log.debug("Retrieving image " + picName + " from model " + model);

		if (picName.startsWith(LEGACY_PREFIX) && !picName.endsWith("/")) {
			if (log.isDebugEnabled()) log.debug("Omitting legacy picture literal prefix from " + picName);
			picName = picName.substring(picName.lastIndexOf("/") + 1);
		}

		File picDir = new File(registry.getProjectBaseDirectory() + model + "/pics/");
		if (!picDir.exists() && !picDir.mkdir())
			log.error("Error while creating pic directory for project " + model);
		else {
			File picFile = new File(registry.getProjectBaseDirectory() + model + "/pics/" + picName);

			String mime = MIME_MAP.get(picName.substring(picName.lastIndexOf('.') + 1));

			if (!picFile.exists())
				log.warn("Could not find " + picFile.getAbsolutePath());
			else if (mime != null) {
				BufferedImage image = ImageIO.read(new FileInputStream(picFile));

				response.setContentType(mime);

				ImageIO.write(image, picName.substring(picName.lastIndexOf('.') + 1).toLowerCase(), response.getOutputStream());

				if (log.isDebugEnabled()) log.debug("Retrieved image: " + picName);
			} else {
				response.setHeader("Content-Disposition", "attachment; filename=" + picFile.getName());
				FileInputStream fis = new FileInputStream(picFile);
				int next;
				while ((next = fis.read()) != -1) {
					response.getOutputStream().write((byte) next);
					response.getOutputStream().flush();
				}
			}

		}

		return null;
	}

}