package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after deletion of a {@link SectionInstance}.
 * 
 * @author BBonev
 */
@Documentation("Event fired after deletion of a {@link SectionInstance}.")
public class AfterSectionDeleteEvent extends
		AfterInstanceDeleteEvent<SectionInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after section delete event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterSectionDeleteEvent(SectionInstance instance) {
		super(instance);
	}

}
