package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when section or sub element has been modified and the section need to be updated and
 * saved.
 * 
 * @author BBonev
 */
@Documentation("Event fired when section or sub element has been modified and the section need to be updated and saved.")
public class SectionChangeEvent extends InstanceChangeEvent<SectionInstance> {

	/**
	 * Instantiates a new section change event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public SectionChangeEvent(SectionInstance instance) {
		super(instance);
	}

}
