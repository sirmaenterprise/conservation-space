package com.sirma.itt.seip.instance.relation;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;

/**
 * Event fired when creating a relation.
 *
 * @author nvelkov
 */
@Documentation("Event fired when a new relation is created.")
public class RelationCreateEvent extends RelationEvent {

	private String operation;

	/**
	 * Instantiates a new relation create event.
	 *
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param relationType
	 *            the relation type
	 */
	public RelationCreateEvent(String fromId, String toId, String relationType) {
		super(fromId, toId, relationType);
		operation = ActionTypeConstants.CREATE_LINK;
	}

	/**
	 * Instantiates a new relation create event.
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
	public RelationCreateEvent(String fromId, String toId, String relationType, String operation) {
		this(fromId, toId, relationType);
		this.operation = operation;
	}

	@Override
	public String getOperationId() {
		return operation;
	}

}
