package com.sirma.cmf.web.digitalsign;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event that is fired after a document is signed.
 * 
 * @author Ivo Rusev
 * 
 */
@Documentation("Event thrown after successfully digitally signing a document.")
public class AfterDigitalSignEvent implements EmfEvent {

	private CaseInstance caseInstance;

	private DocumentInstance documentInstance;

	/**
	 * @return the caseInstance
	 */
	public CaseInstance getCaseInstance() {
		return caseInstance;
	}

	/**
	 * @param caseInstance
	 *            the caseInstance to set
	 */
	public void setCaseInstance(CaseInstance caseInstance) {
		this.caseInstance = caseInstance;
	}

	/**
	 * @return the documentInstance
	 */
	public DocumentInstance getDocumentInstance() {
		return documentInstance;
	}

	/**
	 * @param documentInstance
	 *            the documentInstance to set
	 */
	public void setDocumentInstance(DocumentInstance documentInstance) {
		this.documentInstance = documentInstance;
	}

}
