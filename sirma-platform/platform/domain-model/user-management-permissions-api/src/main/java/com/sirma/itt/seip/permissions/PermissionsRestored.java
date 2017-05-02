package com.sirma.itt.seip.permissions;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * The PermissionsRestored is event fired after asynch submission for permissions restore on all children. The event is
 * not related to the actual result of restoration.
 *
 * @author bbanchev
 */
@Documentation("The PermissionsRestored is event fired after asynch submission for permissions restore on all children. The event is not related to the actual result of restoration.")
public class PermissionsRestored extends AbstractInstanceEvent<Instance> {

	/**
	 * Instantiates a new permissions restored event.
	 *
	 * @param instance
	 *            the instance
	 */
	public PermissionsRestored(Instance instance) {
		super(instance);
	}

}
