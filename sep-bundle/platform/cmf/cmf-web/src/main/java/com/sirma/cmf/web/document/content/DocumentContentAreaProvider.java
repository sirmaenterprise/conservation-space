package com.sirma.cmf.web.document.content;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.plugin.PathDefinition;

/**
 * Methods that should be implemented by custom providers.
 * 
 * @author svelikov
 */
public interface DocumentContentAreaProvider extends PathDefinition {

	/** The extension point. */
	String EXTENSION_POINT = "documentContentArea";

	/**
	 * Checks if the current editor could handle the provided {@link DocumentInstance}.
	 * 
	 * @param documentInstance
	 *            document that should be checked.
	 * @return true if the document can be handled by this editor, false otherwise.
	 */
	boolean canHandle(DocumentInstance documentInstance);

	/**
	 * Handles a {@link DocumentInstance} for preview/edit.
	 * 
	 * @param documentInstance
	 *            the document instance
	 */
	void handle(DocumentInstance documentInstance);
}
