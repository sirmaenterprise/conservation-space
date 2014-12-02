package com.sirma.itt.cmf.event.relation;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when deleting a relation.
 * 
 * @author nvelkov
 */
@Documentation("Event fired when a relation is deleted.")
public class RelationDeleteEvent extends RelationEvent {

	/**
	 * Instantiates a new relation delete event.
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
	public RelationDeleteEvent(String fromId, String toId, String relationType, String relationId) {
		super(fromId, toId, relationType, relationId);
	}
	

	@Override
	public String getOperationId() {
		return ActionTypeConstants.DELETE;
	}

}
