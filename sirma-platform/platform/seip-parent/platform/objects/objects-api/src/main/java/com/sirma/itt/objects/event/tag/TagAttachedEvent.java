package com.sirma.itt.objects.event.tag;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.relation.RelationEvent;

/**
 * Event fired after a tag is attached to an instance.
 *
 * @author Mihail Radkov
 */
@Documentation("Event fired after a tag is attached to an instance.")
public class TagAttachedEvent extends RelationEvent {

	/**
	 * Instantiates a new tag attach event.
	 *
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param relationType
	 *            the relation type
	 */
	public TagAttachedEvent(String fromId, String toId, String relationType) {
		super(fromId, toId, relationType);
	}

	@Override
	public String getOperationId() {
		return ActionTypeConstants.ATTACH_OBJECT;
	}

}
