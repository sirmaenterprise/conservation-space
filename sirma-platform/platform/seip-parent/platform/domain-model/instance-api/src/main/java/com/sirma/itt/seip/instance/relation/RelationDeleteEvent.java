package com.sirma.itt.seip.instance.relation;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;

/**
 * Event fired when deleting a relation.
 *
 * @author nvelkov
 */
@Documentation("Event fired when a relation is deleted.")
public class RelationDeleteEvent extends RelationEvent {

	private String operation;

	/**
	 * Instantiates a new relation delete event.
	 *
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param relationType
	 *            the relation type
	 */
	public RelationDeleteEvent(String fromId, String toId, String relationType) {
		super(fromId, toId, relationType);
		operation = ActionTypeConstants.DELETE;
	}

	/**
	 * Instantiates a new relation delete event.
	 *
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param relationType
	 *            the relation type
	 * @param operation
	 *            the operation
	 */
	public RelationDeleteEvent(String fromId, String toId, String relationType, String operation) {
		super(fromId, toId, relationType);
		this.operation = operation;
	}

	@Override
	public String getOperationId() {
		return operation;
	}

}
