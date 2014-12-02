package com.sirma.cmf.web.document;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.sirma.itt.cmf.beans.model.SectionInstance;

/**
 * Get all available file types for a specific section.
 * 
 * @author stella
 */
@Named("documentFileTypes")
@ApplicationScoped
public class DocumentFileTypes extends NewDocumentAction implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -8163240825770840231L;

	/**
	 * Prepares the creation of new file.
	 * 
	 * @param selectedSection
	 *            section where the new file will be added.
	 */
	public void prepare(SectionInstance selectedSection) {
		setSectionInstance(selectedSection);

		filterFileTypesByCase(selectedSection, "iDoc");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterUploadAction() {
		// TODO Auto-generated method stub
	}

}
