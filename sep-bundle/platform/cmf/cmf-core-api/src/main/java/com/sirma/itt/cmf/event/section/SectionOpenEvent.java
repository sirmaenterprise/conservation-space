package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on section open operation
 * 
 * @author BBonev
 */
@Documentation("Event fired on section open operation")
public class SectionOpenEvent extends InstanceOpenEvent<SectionInstance> {

	/**
	 * Instantiates a new section open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public SectionOpenEvent(SectionInstance instance) {
		super(instance);
	}

}
