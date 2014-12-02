package com.sirma.itt.idoc.web.document;

import java.util.UUID;

import javax.enterprise.inject.Specializes;

import com.sirma.cmf.web.document.CreateHtmlDocumentAction;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;

/**
 * Extends the default CreateHtmlDocumentAction by setting the purpose property that identifies the
 * current documents as an intelligent document.
 * 
 * @author Adrian Mitev
 */
@Specializes
public class CreateIntelligentDocumentAction extends CreateHtmlDocumentAction {

	private static final long serialVersionUID = 3718996791500748242L;

	@Override
	protected DocumentInstance createDocumentInstance() {
		// generate random id for the file name because the user won't specify it
		fileName = UUID.randomUUID().toString();

		DocumentInstance instance = super.createDocumentInstance();

		instance.setPurpose(IntelligentDocumentProperties.DOCUMENT_PURPOSE);
		return instance;
	}

	@Override
	public void prepare(SectionInstance selectedSection) {
		setSectionInstance(selectedSection);

		filterFileTypesByCase(selectedSection, IntelligentDocumentProperties.DOCUMENT_PURPOSE);
	}

}
