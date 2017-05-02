package com.sirma.itt.seip.instance.relation;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;

/**
 * Event fired when a relation is modified.
 *
 * @author nvelkov
 */
@Documentation("Event fired when a relation is modified.")
public class RelationChangeEvent extends RelationEvent {

	/**
	 * Instantiates a new relation change event.
	 *
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param relationType
	 *            the relation type
	 */
	public RelationChangeEvent(String fromId, String toId, String relationType) {
		super(fromId, toId, relationType);
	}

	@Override
	public String getOperationId() {
		return ActionTypeConstants.EDIT_DETAILS;
	}

}
