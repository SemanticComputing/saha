package fi.seco.saha3.web.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import fi.seco.saha3.infrastructure.SahaProjectRegistry;

/**
 * Controller for uploading images in SAHA.
 * 
 */
public class ImageUploadController extends AbstractCommandController {
	private final Log log = LogFactory.getLog(getClass());

	private SahaProjectRegistry registry;

	@Required
	public void setSahaProjectRegistry(SahaProjectRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors) throws Exception {
		ImageBean bean = (ImageBean) command;

		String model = parseModelName(request.getServletPath());
		MultipartFile file = bean.getImage();
		if (file == null) {
			response.sendRedirect("editor.shtml?" + request.getQueryString());
			return null;
		}

		//        BufferedImage image = ImageIO.read(file.getInputStream());

		try {
			// Show the image in the UI
			String fileName = file.getOriginalFilename();
			String fileExtension = fileName.indexOf('.') > 0 ? fileName.substring(fileName.indexOf('.') + 1, fileName.length()) : "jpeg";

			//            log.info("Read an image file: " + fileName + " ("
			//                + image.getWidth() + "x" + image.getHeight() + ", "
			//                + fileExtension + ")");

			if (fileName.lastIndexOf('.') != -1) fileName = fileName.substring(0, fileName.lastIndexOf('.'));

			// Save the image on disk

			File picDir = new File(registry.getProjectBaseDirectory() + model + "/pics/");
			if (!picDir.exists() && !picDir.mkdir()) {
				log.error("No pic directory found and was unable to create: " + picDir.getAbsolutePath());
				return null;
			}

			File fileOnDisk = new File(registry.getProjectBaseDirectory() + model + "/pics/" + fileName + "." + fileExtension);

			// Loop until a non-used filename is found
			int index = 1;
			while (fileOnDisk.exists())
				fileOnDisk = new File(registry.getProjectBaseDirectory() + model + "/pics/" + fileName + "_" + (++index) + "." + fileExtension);

			OutputStream fout = new FileOutputStream(fileOnDisk);
			fileName = fileOnDisk.getName();
			registry.getModelEditor(model).addLiteralProperty(bean.getResource(), bean.getProperty(), fileName);

			fout.write(file.getBytes());
			fout.flush();

			//            ImageIO.write(image, fileExtension, fout);

			fout.close();

			log.info("Image file " + fileName + " successfully saved to: " + fileOnDisk.getAbsolutePath());

		} catch (Exception e) {
			log.error("" + e);
			log.warn("File not uploaded.");
		}

		response.sendRedirect("editor.shtml?" + request.getQueryString());
		return null;
	}

	private String parseModelName(String servletPath) {
		return servletPath.substring(1, servletPath.lastIndexOf('/'));
	}

}

class ImageBean {
	private MultipartFile image;
	private String property;
	private String resource;

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public void setImage(MultipartFile image) {
		this.image = image;
	}

	public MultipartFile getImage() {
		return image;
	}
}
