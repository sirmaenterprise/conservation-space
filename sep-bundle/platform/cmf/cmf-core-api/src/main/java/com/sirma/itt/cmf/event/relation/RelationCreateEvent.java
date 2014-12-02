package com.sirma.itt.cmf.event.relation;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when creating a relation.
 * 
 * @author nvelkov
 */
@Documentation("Event fired when a new relation is created.")
public class RelationCreateEvent extends RelationEvent {

	/**
	 * Instantiates a new relation create event.
	 * 
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param relationType
	 *            the relation type
	 * @param relationId
	 *            the relation id
	 */
	public RelationCreateEvent(String fromId, String toId, String relationType, String relationId) {
		super(fromId, toId, relationType, relationId);
	}
	

	@Override
	public String getOperationId() {
		return ActionTypeConstants.CREATE_LINK;
	}

}
