package com.sirma.itt.seip.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired before improting instance and before the validation is executed.
 *
 * @author smustafov
 */
@Documentation("Event fired before importing instance and before the validation is executed.")
public class BeforeInstanceImportEvent extends AbstractInstanceEvent<Instance> {

	/**
	 * Instantiates a new before instance import event.
	 *
	 * @param instance the instance to be imported
	 */
	public BeforeInstanceImportEvent(Instance instance) {
		super(instance);
	}

}
