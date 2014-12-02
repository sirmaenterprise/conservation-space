package com.sirma.itt.cmf.event.relation;

import com.sirma.itt.emf.event.AbstractRelationEvent;
import com.sirma.itt.emf.event.OperationEvent;

/**
 * Event, extended by relation events that have a type an id.
 * 
 * @author nvelkov
 */
public abstract class RelationEvent extends AbstractRelationEvent<String, String> implements OperationEvent {

	/** The relation type. */
	private String relationType;

	/** The relation id. */
	private String relationId;

	/**
	 * Instantiates a new relation event.
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
	public RelationEvent(String fromId, String toId, String relationType, String relationId) {
		super(fromId, toId);
		this.relationType = relationType;
		this.relationId = relationId;
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

	/**
	 * Gets the relation id.
	 * 
	 * @return the relation id
	 */
	public String getRelationId() {
		return relationId;
	}

	/**
	 * Sets the relation id.
	 * 
	 * @param relationId
	 *            the new relation id
	 */
	public void setRelationId(String relationId) {
		this.relationId = relationId;
	}

}
