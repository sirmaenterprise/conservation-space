package com.sirma.cmf.web.document.editor;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.plugin.PathDefinition;

/**
 * Contains the needed methods that should be provided by a custom content editor.
 * 
 * @author Adrian Mitev
 */
public interface DocumentEditor extends PathDefinition {

	/** The extension point. */
	String EXTENSION_POINT = "documentEditor";

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
	 *            {@link DocumentInstance} to be shown.
	 * @param preview
	 *            true if in preview mode, false otherwise.
	 */
	void handle(DocumentInstance documentInstance, boolean preview);

}
