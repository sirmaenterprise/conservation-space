package com.sirma.itt.seip.instance.save.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Event that notifies that the instance is going to be saved. The event carry the instance, which is going to be saved
 * and current instance, which data will be updated.
 *
 * @author A. Kunchev
 */
public class BeforeInstanceSaveEvent extends InstanceSaveEvent {

	/**
	 * Instantiates a new before instance save event.
	 *
	 * @param instanceToSave
	 *            the instance that will be saved/updated
	 * @param currentInstance
	 *            the current instance before the actual save/update is done. It could be used to check, what changes
	 *            will be applied after the save process is done. Could be missing, which means that the instance is new
	 *            and it is just created
	 * @param operation
	 *            the current operation applied to the instanceToSave
	 */
	public BeforeInstanceSaveEvent(Instance instanceToSave, Instance currentInstance, Operation operation) {
		super(instanceToSave, currentInstance, operation);
	}

	@Override
	protected TwoPhaseEvent createNextEvent() {
		return new AfterInstanceSaveEvent(getInstanceToSave(), getCurrentInstance(), getOperation());
	}

}
