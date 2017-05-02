package com.sirma.itt.seip.instance.draft.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.instance.draft.DraftService;
import com.sirma.itt.seip.instance.event.AfterInstanceDeleteEvent;

/**
 * Handles drafts deletion when instance is deleted.
 *
 * @author BBonev
 */
@ApplicationScoped
public class InstanceDraftHandler {

	@Inject
	private DraftService draftService;

	/**
	 * Deletes instance drafts(if any), when the instance is deleted.
	 *
	 * @param event
	 *            the event that triggers the deletion and carries the instance that is deleted
	 */
	public void onInstanceDeleted(@Observes AfterInstanceDeleteEvent<?, ?> event) {
		if (event == null || event.getInstance() == null) {
			return;
		}

		draftService.delete((String) event.getInstance().getId());
	}
}
