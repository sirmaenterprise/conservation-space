package com.sirma.itt.seip.content;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension to provider means to load a content from different sources
 *
 * @author BBonev
 */
@Documentation("Extension to provider means to load a content from different sources")
public interface ContentLoader extends Plugin {
	/** The target name of extension. */
	String TARGET_NAME = "ContentLoader";

	/**
	 * Checks if is applicable.
	 *
	 * @param object
	 *            the object
	 * @return true, if is applicable and may call the {@link #loadContent(Object)} method.
	 */
	boolean isApplicable(Object object);

	/**
	 * Load the content for the given object.
	 *
	 * @param object
	 *            the object to load the content for. The object is the same passed to {@link #isApplicable(Object)}
	 *            method.
	 * @return the loaded content or <code>null</code> if the loader did not manage to find it
	 */
	String loadContent(Object object);
}
