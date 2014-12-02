package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after section instance creation but before method completion.
 * 
 * @author BBonev
 */
@Documentation("Event fired after section instance creation but before method completion.")
public class SectionCreateEvent extends InstanceCreateEvent<SectionInstance> implements
		OperationEvent {

	/**
	 * Instantiates a new section create event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public SectionCreateEvent(SectionInstance instance) {
		super(instance);
	}

	@Override
	public String getOperationId() {
		return "createSection";
	}

}
