package com.sirma.cmf.web.upload;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.fileupload.FileItemFactory;

/**
 * {@link FileItemFactory} that provides access to repository container.
 * 
 * @author BBonev
 */
public interface EmfFileItemFactory extends FileItemFactory, Serializable {

	/**
	 * Gets the repository.
	 * 
	 * @return the repository
	 */
	File getRepository();
}
