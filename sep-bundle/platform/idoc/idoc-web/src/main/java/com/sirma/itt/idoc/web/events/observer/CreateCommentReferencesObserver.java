package com.sirma.itt.idoc.web.events.observer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.sirma.itt.emf.forum.event.CommentUpdatedEvent;
import com.sirma.itt.emf.forum.model.CommentInstance;

/**
 * Observer for the {@link CommentUpdatedEvent} event to handle parsing of comment content and
 * creating relationships.
 */
@ApplicationScoped
public class CreateCommentReferencesObserver extends AbstractDocumentLinkHandler {

	/**
	 * Observer method to trigger parsing of the comment content.
	 * 
	 * @param event
	 *            Event payload.
	 */
	public void handleDocumentPersistedEvent(@Observes CommentUpdatedEvent event) {

		CommentInstance oldComment = event.getOldCommentInstance();
		CommentInstance commentInstance = event.getCommentInstance();

		handle(commentInstance, oldComment);
	}

}
