package com.sirma.itt.seip.content;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Interface for building and providing uris of the uploaded documents.
 *
 * @author Nikolay Ch
 */
public interface ContentAccessProvider extends Plugin {
	/** The target name. */
	String TARGET_NAME = "ContentAccessProvider";

	/**
	 * Provides access uri for the given document.
	 *
	 * @param instance
	 *            is the document to get uri for
	 * @return the builded uri or empty string on error
	 */
	String getContentURI(Instance instance);

	/**
	 * Builds a descriptor for the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the descriptor
	 */
	FileDescriptor getDescriptor(Instance instance);
}
