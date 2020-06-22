package com.sirma.itt.seip.instance.actions.thumbnail;

import java.io.Serializable;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyEvent;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyRemoveEvent;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Automatic action for thumbnail relations. Update thumbnail service based on the added relation {@value LinkConstants#HAS_THUMBNAIL}
 * <br> On adding the relation above the {@link ThumbnailService} will be notified for the thumbnail association change.
 * <br> Note that this observer depends on that the remove relation is called first before the add relation otherwise
 * the thumbnail will not be assigned.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 07/09/2018
 */
@Singleton
public class AssignThumbnailOnRelationChange {

	@Inject
	private ThumbnailService thumbnailService;

	void onRelationAdded(@Observes ObjectPropertyAddEvent event) {
		if (!isAddThumbnailEvent(event)) {
			return;
		}
		Serializable sourceId = event.getSourceId();
		Serializable thumbnailId = event.getTargetId();
		thumbnailService.register(sourceId, thumbnailId);
	}

	void onRelationRemoved(@Observes ObjectPropertyRemoveEvent event) {
		if (!isAddThumbnailEvent(event)) {
			return;
		}
		Serializable sourceId = event.getSourceId();
		// we remove the old thumbnail, if there is any, before register another
		thumbnailService.removeAssignedThumbnail(sourceId);
	}

	private boolean isAddThumbnailEvent(ObjectPropertyEvent event) {
		return event.getObjectPropertyName().equals(LinkConstants.HAS_THUMBNAIL);
	}
}
