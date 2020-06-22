package com.sirma.itt.emf.instance.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;

/**
 * Event fired when common instance has been changed.
 *
 * @author BBonev
 */
@Documentation("Event fired when common instance has been changed.")
public class CommonInstanceChangeEvent extends InstanceChangeEvent<CommonInstance> {

	/**
	 * Instantiates a new common instance change event.
	 *
	 * @param instance
	 *            the instance
	 */
	public CommonInstanceChangeEvent(CommonInstance instance) {
		super(instance);
	}

}
