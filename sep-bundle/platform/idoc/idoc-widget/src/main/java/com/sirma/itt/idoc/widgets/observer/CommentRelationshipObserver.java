package com.sirma.itt.idoc.widgets.observer;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.idoc.web.events.CreateRelationshipFromLinkEvent;

/**
 * Handles the {@link CreateRelationshipFromLinkEvent} for a comment.
 */
@ApplicationScoped
public class CommentRelationshipObserver extends BaseWidgetRelationshipObserver {

	/**
	 * This method checks if in comment are inserted links to documents/objects and if there are it
	 * creates a 'references' relationship.
	 * 
	 * @param event
	 *            Event payload.
	 */
	public void handleCommentRelationships(@Observes CreateRelationshipFromLinkEvent event) {

		if ((event.getInstanceType() != null) && (event.getInstanceId() != null)) {
			if ((event.getNewLinkedInstances() == null)
					|| event.getNewLinkedInstances().contains(event.getInstanceId())) {

				List<InstanceReference> idTypePairs = new ArrayList<>(1);
				idTypePairs
						.add(getInstanceReference(event.getInstanceType(), event.getInstanceId()));
				createObjectReferencesLink(event.getFrom(), idTypePairs);
			}
		}
	}
}
