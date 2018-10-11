package com.sirma.itt.seip.instance.save.event;

import java.util.Objects;

import com.sirma.itt.seip.domain.event.AbstractTwoPhaseEvent;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Base implementation of events fired, while specific instance is saved.
 *
 * @author A. Kunchev
 */
public abstract class InstanceSaveEvent extends AbstractTwoPhaseEvent<TwoPhaseEvent> {

	private final Instance instanceToSave;

	private final Instance currentInstance;

	private final Operation operation;

	/**
	 * Instantiates a new instance save event.
	 *
	 * @param instanceToSave
	 *            the instance that will be saved/updated, cannot be null
	 * @param currentInstance
	 *            the current instance before the actual save/update is done. It could be used to check, what changes
	 *            will be applied after the save process is done. Could be missing, which means that the instance is new
	 *            and it is just created
	 * @param operation
	 *            the current operation applied to the instanceToSave, cannot be null
	 */
	public InstanceSaveEvent(Instance instanceToSave, Instance currentInstance, Operation operation) {
		this.instanceToSave = Objects.requireNonNull(instanceToSave, "The target instance is required");
		this.currentInstance = currentInstance;
		this.operation = Objects.requireNonNull(operation, "The operation is required argument");
	}

	public Instance getInstanceToSave() {
		return instanceToSave;
	}

	public Instance getCurrentInstance() {
		return currentInstance;
	}

	/**
	 * Getter method for operation.
	 *
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

}
