package com.sirma.itt.cmf.services.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.cmf.event.document.AfterDocumentDeleteEvent;
import com.sirma.itt.cmf.event.document.AfterDocumentRevertEvent;
import com.sirma.itt.cmf.event.document.AfterDocumentUploadEvent;
import com.sirma.itt.emf.rendition.ThumbnailService;

/**
 * Observer to listen for particular events and to register the instance for thumbnail generation
 * 
 * @author BBonev
 */
@ApplicationScoped
public class RenditionRegisterObserver {

	/** The synchronization service. */
	@Inject
	private ThumbnailService synchronizationService;

	/**
	 * Register the newly uploaded document for thumbnail retrieval
	 * 
	 * @param event
	 *            the event
	 */
	public void onDocumentUploaded(@Observes AfterDocumentUploadEvent event) {
		synchronizationService.register(event.getInstance());
	}

	/**
	 * Removes a thumbnail references for all instance that were using the currently removed
	 * document
	 * 
	 * @param event
	 *            the event
	 */
	public void onDocumentDeleted(@Observes AfterDocumentDeleteEvent event) {
		synchronizationService.deleteThumbnail(event.getInstance().getId());
	}

	/**
	 * Resents the thumbnail after revert
	 * 
	 * @param event
	 *            the event
	 */
	public void onDocumentReverted(@Observes AfterDocumentRevertEvent event) {
		synchronizationService.register(event.getInstance());
	}

}
