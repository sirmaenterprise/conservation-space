package com.sirma.itt.emf.properties.event;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on save of instance properties. The event carry information about the modified
 * instance, added and removed properties and the operation that triggered the change and the change
 * timestamp.
 * 
 * @author BBonev
 */
@SuppressWarnings("rawtypes")
@Documentation("Event fired on save of instance properties. The event carry information about the modified instance, added and removed properties and the operation that triggered the change and the change timestamp.")
public class PropertiesChangeEvent implements OperationEvent {

	/** The added. */
	private final Map<String, Serializable> added;
	/** The removed. */
	private final Map<String, Serializable> removed;
	/** The operation id. */
	private final String operationId;
	/** The timestamp. */
	private final long timestamp;
	/** The entity. */
	private final Entity entity;

	/**
	 * Instantiates a new properties change event.
	 * 
	 * @param entity
	 *            the target modified instance
	 * @param added
	 *            the added properties
	 * @param removed
	 *            the removed properties
	 * @param operation
	 *            the operation executed operation id
	 */
	public PropertiesChangeEvent(Entity entity, Map<String, Serializable> added,
			Map<String, Serializable> removed, String operation) {
		this.entity = entity;
		this.added = added;
		this.removed = removed;
		this.operationId = operation;
		timestamp = System.currentTimeMillis();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperationId() {
		return operationId;
	}

	/**
	 * Returns the added properties
	 * 
	 * @return the added
	 */
	public Map<String, Serializable> getAdded() {
		return added;
	}

	/**
	 * Returns the removed properties
	 * 
	 * @return the removed
	 */
	public Map<String, Serializable> getRemoved() {
		return removed;
	}

	/**
	 * Returns the change timestamp
	 * 
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Getter method for entity.
	 * 
	 * @return the entity
	 */
	public Entity getEntity() {
		return entity;
	}
}
