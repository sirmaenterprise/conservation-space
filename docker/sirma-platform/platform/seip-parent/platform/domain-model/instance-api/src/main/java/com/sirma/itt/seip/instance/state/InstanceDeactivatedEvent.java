package com.sirma.itt.seip.instance.state;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired when the instances transitions from active state to inactive state.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 01/11/2017
 */
@Documentation("Event fired when the instances transitions from active state to inactive state.")
public class InstanceDeactivatedEvent extends AbstractInstanceEvent<Instance> {

	/**
	 * Instantiates a new abstract instance event.
	 *
	 * @param instance the instance that transitioned
	 */
	public InstanceDeactivatedEvent(Instance instance) {
		super(instance);
	}
}
