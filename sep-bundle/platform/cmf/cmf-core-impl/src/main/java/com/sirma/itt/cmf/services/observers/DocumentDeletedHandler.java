package com.sirma.itt.cmf.services.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.event.document.AfterDocumentDeleteEvent;
import com.sirma.itt.emf.link.LinkService;

/**
 * Handles events for document deletions. For example removes any existing links for the deleted
 * document.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class DocumentDeletedHandler {

	/** The link service. */
	@Inject
	private LinkService linkService;

	/**
	 * On document deleted.
	 * 
	 * @param event
	 *            the event
	 */
	public void onDocumentDeleted(@Observes AfterDocumentDeleteEvent event) {
		linkService.removeLinksFor(event.getInstance().toReference());
	}
}
