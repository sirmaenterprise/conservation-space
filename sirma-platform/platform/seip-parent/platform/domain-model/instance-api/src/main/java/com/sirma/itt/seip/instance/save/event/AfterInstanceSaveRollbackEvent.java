package com.sirma.itt.seip.instance.save.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.state.Operation;

/**
 * Event that notifies for failure, while finalize specific instance save process. The event carry the instance, which
 * was suppose to be saved and current instance, which data was suppose to be updated.
 *
 * @author A. Kunchev
 */
public class AfterInstanceSaveRollbackEvent extends InstanceSaveEvent {

	/**
	 * Instantiates a new after instance save rollback event.
	 *
	 * @param instanceToSave
	 *            the instance, which was suppose to be saved/updated
	 * @param currentInstance
	 *            the current instance before the actual save/update. It could be used to check, what changes will be
	 *            applied after the save process is done. Could be missing, which means that the instance is new and it
	 *            is just created
	 * @param operation
	 *            the current operation applied to the instanceToSave
	 */
	public AfterInstanceSaveRollbackEvent(Instance instanceToSave, Instance currentInstance, Operation operation) {
		super(instanceToSave, currentInstance, operation);
	}

	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null; // no next event to create
	}

}
