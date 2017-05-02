package com.sirma.itt.objects.event.tag;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.relation.RelationEvent;

/**
 * Event fired when a tag is detached.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired when a tag is detached.")
public class TagDetachEvent extends RelationEvent {

	/**
	 * Instantiates a new tag detach event.
	 *
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param relationType
	 *            the relation type
	 */
	public TagDetachEvent(String fromId, String toId, String relationType) {
		super(fromId, toId, relationType);
	}

	@Override
	public String getOperationId() {
		return ActionTypeConstants.DETACH_OBJECT;
	}

}
