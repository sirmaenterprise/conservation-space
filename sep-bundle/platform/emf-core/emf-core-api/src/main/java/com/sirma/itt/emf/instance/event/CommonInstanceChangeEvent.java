package com.sirma.itt.emf.instance.event;

import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.util.Documentation;

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
