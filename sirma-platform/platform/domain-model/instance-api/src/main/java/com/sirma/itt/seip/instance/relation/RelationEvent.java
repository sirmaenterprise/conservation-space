package com.sirma.itt.seip.instance.relation;

import com.sirma.itt.seip.domain.event.AbstractRelationEvent;
import com.sirma.itt.seip.domain.event.OperationEvent;

/**
 * Event, extended by relation events that have a type an id.
 *
 * @author nvelkov
 */
public abstract class RelationEvent extends AbstractRelationEvent<String, String>implements OperationEvent {

	/** The relation type. */
	private String relationType;

	/**
	 * Instantiates a new relation event.
	 *
	 * @param fromId
	 *            the from id
	 * @param toId
	 *            the to id
	 * @param relationType
	 *            the relation type
	 */
	public RelationEvent(String fromId, String toId, String relationType) {
		super(fromId, toId);
		this.relationType = relationType;
	}

	/**
	 * Gets the relation type.
	 *
	 * @return the relation type
	 */
	public String getRelationType() {
		return relationType;
	}

	/**
	 * Sets the relation type.
	 *
	 * @param relationType
	 *            the new relation type
	 */
	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

}
