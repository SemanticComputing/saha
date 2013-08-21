/**
 * 
 */
package fi.seco.saha3.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author jiemakel
 * 
 */
public class DirectoryModelLister implements IModelLister {
    
        private static final Logger log = LoggerFactory.getLogger(DirectoryModelLister.class);
	private File path;

	@Required
	public void setPath(String path) {
		this.path = new File(path);
	}

	@Override
	public Collection<String> getModels() {
		File[] fs = path.listFiles();
		if ( fs == null ) {
		    log.error("No project configurations found from path: " + path.getAbsolutePath() );
		    return Collections.emptyList();
		}

		Collection<String> models = new ArrayList<String>(fs.length);
		for (File f : fs)
			models.add(f.getName());
		return models;
	}

	@Override
	public boolean modelExists(String modelName) {
		return new File(path.getAbsolutePath() + "/" + modelName).exists();
	}

}
