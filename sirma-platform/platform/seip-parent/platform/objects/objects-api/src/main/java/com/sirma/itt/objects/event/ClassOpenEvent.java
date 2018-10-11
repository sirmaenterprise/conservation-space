package com.sirma.itt.objects.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.instance.event.InstanceOpenEvent;

/**
 * Event fired before object to be visualized on screen.
 *
 * @author BBonev
 */
@Documentation("Event fired before class to be visualized on screen.")
public class ClassOpenEvent extends InstanceOpenEvent<ClassInstance> {

	/**
	 * Instantiates a new object open event.
	 *
	 * @param instance
	 *            the instance
	 */
	public ClassOpenEvent(ClassInstance instance) {
		super(instance);
	}

}
