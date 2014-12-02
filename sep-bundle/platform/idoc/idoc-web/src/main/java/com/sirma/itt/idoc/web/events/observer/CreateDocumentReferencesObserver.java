package com.sirma.itt.idoc.web.events.observer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.event.document.DocumentPersistedEvent;

/**
 * Observer for the {@link DocumentPersistedEvent} event to handle parsing of content and creating
 * relationships.
 * 
 * @author yasko
 */
@ApplicationScoped
public class CreateDocumentReferencesObserver extends AbstractDocumentLinkHandler {

	/**
	 * Observer method to trigger parsing of the document content and firing event for widgets.
	 * 
	 * @param event
	 *            Event payload.
	 */
	public void handleDocumentPersistedEvent(@Observes DocumentPersistedEvent event) {
		DocumentInstance documentInstance = event.getInstance();

		if (Boolean.TRUE.equals(documentInstance.isStandalone()) || !isOperationChanging(event)) {
			return;
		}


		handle(documentInstance, documentInstance);
	}

}
