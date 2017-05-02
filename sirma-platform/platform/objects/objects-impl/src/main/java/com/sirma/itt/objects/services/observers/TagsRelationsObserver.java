package com.sirma.itt.objects.services.observers;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.objects.constants.ObjectLinkConstants;
import com.sirma.itt.objects.event.tag.TagAttachedEvent;
import com.sirma.itt.objects.event.tag.TagDetachEvent;
import com.sirma.itt.seip.annotation.DisableAudit;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.relation.RelationCreateEvent;
import com.sirma.itt.seip.instance.relation.RelationDeleteEvent;

/**
 * Observes {@link RelationCreateEvent} and {@link RelationDeleteEvent} related only to tags.
 *
 * @author Mihail Radkov
 */
@ApplicationScoped
public class TagsRelationsObserver {

	@Inject
	private EventService eventService;

	/**
	 * Observes {@link RelationCreateEvent} when a tag is attached to an instance and fires specific event
	 * {@link TagAttachedEvent}.
	 *
	 * @param createEvent
	 *            - the relation create event
	 */
	@DisableAudit
	public void onAttachTag(@Observes RelationCreateEvent createEvent) {
		if (ObjectLinkConstants.HAS_TAG.equals(createEvent.getRelationType())) {
			TagAttachedEvent tagEvent = new TagAttachedEvent(createEvent.getFrom(), createEvent.getTo(),
					createEvent.getRelationType());
			eventService.fire(tagEvent);
		}
	}

	/**
	 * Observes {@link RelationDeleteEvent} when a tag is attached to an instance and fires specific event
	 * {@link TagDetachEvent}.
	 *
	 * @param deleteEvent
	 *            - the relation delete event
	 */
	@DisableAudit
	public void onDetachTag(@Observes RelationDeleteEvent deleteEvent) {
		if (ObjectLinkConstants.HAS_TAG.equals(deleteEvent.getRelationType())) {
			TagDetachEvent tagEvent = new TagDetachEvent(deleteEvent.getFrom(), deleteEvent.getTo(),
					deleteEvent.getRelationType());
			eventService.fire(tagEvent);
		}
	}
}
