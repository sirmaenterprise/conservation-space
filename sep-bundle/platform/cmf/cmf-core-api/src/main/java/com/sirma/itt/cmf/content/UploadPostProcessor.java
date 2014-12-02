package com.sirma.itt.cmf.content;

import java.util.List;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.util.Documentation;

/**
 * The extension of this processor are responsible to update document or provide any additional just
 * before the upload.
 * 
 * @author bbanchev
 */
@Documentation("The Interface UploadPostProcessor is extension for different mail standarts extrators.")
public interface UploadPostProcessor extends Plugin {

	String TARGET_NAME = "UploadPostProcessor";

	/**
	 * Process the given instance using the logic in the extension.
	 * 
	 * @param instance
	 *            is the instance to post processes just before the upload
	 * @return the list of instances that might be created during processing including the instance
	 *         itself
	 * @throws Exception
	 *             on any error
	 */
	List<DocumentInstance> proccess(DocumentInstance instance) throws Exception;

}
