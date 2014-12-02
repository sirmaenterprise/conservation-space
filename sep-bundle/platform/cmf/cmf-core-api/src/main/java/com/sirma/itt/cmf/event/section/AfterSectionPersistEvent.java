package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after {@link SectionInstance} is persisted for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired after {@link SectionInstance} is persisted for the first time.")
public class AfterSectionPersistEvent extends
		AfterInstancePersistEvent<SectionInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new before case persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterSectionPersistEvent(SectionInstance instance) {
		super(instance);
	}

}
