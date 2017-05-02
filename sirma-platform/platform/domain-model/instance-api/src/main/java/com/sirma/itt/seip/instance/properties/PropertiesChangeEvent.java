package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.Map;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Event fired on save of instance properties. The event carry information about the modified instance, added and
 * removed properties and the operation that triggered the change and the change timestamp.
 *
 * @author BBonev
 */
@Documentation("Event fired on save of instance properties. The event carry information about the modified instance, added and removed properties and the operation that triggered the change and the change timestamp.")
public class PropertiesChangeEvent extends AbstractInstanceEvent<Instance>implements OperationEvent {

	/** The added. */
	private final Map<String, Serializable> added;

	/** The removed. */
	private final Map<String, Serializable> removed;

	/** The operation id. */
	private final Operation operation;

	/** The timestamp. */
	private final long timestamp;

	/**
	 * Instantiates a new properties change event.
	 *
	 * @param instance
	 *            the target modified instance
	 * @param added
	 *            the added properties
	 * @param removed
	 *            the removed properties
	 * @param operation
	 *            the operation executed operation id
	 */
	public PropertiesChangeEvent(Instance instance, Map<String, Serializable> added, Map<String, Serializable> removed,
			Operation operation) {
		super(instance);
		this.added = added;
		this.removed = removed;
		this.operation = operation;
		timestamp = System.currentTimeMillis();
	}

	@Override
	public String getOperationId() {
		return Operation.getOperationId(operation);
	}

	@Override
	public Operation getOperation() {
		return operation;
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

}
