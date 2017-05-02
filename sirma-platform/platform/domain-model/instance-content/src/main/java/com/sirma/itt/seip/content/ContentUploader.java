package com.sirma.itt.seip.content;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Provides means of uploading content to remove server
 *
 * @author Nikolay Ch
 */
public interface ContentUploader extends Plugin {
	/** The target name of extension. */
	String TARGET_NAME = "ContentUploader";

	/**
	 * Uploads the content.
	 *
	 * @param instance
	 *            the instance to be uploaded
	 * @param descriptor
	 *            the descriptor of the instance
	 * @return the properties of the uploaded content
	 */
	FileAndPropertiesDescriptor uploadContent(Instance instance, FileDescriptor descriptor);
}
