package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after folder instance creation but before method completion.
 *
 * @author BBonev
 */
@Documentation("Event fired after folder instance creation but before method completion.")
public class FolderCreateEvent extends InstanceCreateEvent<FolderInstance> implements
		OperationEvent {

	/**
	 * Instantiates a new folder create event.
	 *
	 * @param instance
	 *            the instance
	 */
	public FolderCreateEvent(FolderInstance instance) {
		super(instance);
	}

	@Override
	public String getOperationId() {
		return "createFolder";
	}

}
