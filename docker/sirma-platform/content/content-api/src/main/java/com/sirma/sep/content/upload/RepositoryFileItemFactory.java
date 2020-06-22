package com.sirma.sep.content.upload;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.fileupload.FileItemFactory;

/**
 * {@link FileItemFactory} that provides access to repository container. The implementation could provide a way for
 * resetting the instance so it could be reused.
 *
 * @author BBonev
 */
public interface RepositoryFileItemFactory extends FileItemFactory, Serializable {

	/**
	 * Gets the current repository location. To clear the location invoke the {@link #resetRepository()} method.
	 *
	 * @return the repository
	 */
	File getRepository();

	/**
	 * Reset repository. This method could be used to reinitialize the current factory with new temporary repository.
	 */
	void resetRepository();
}
