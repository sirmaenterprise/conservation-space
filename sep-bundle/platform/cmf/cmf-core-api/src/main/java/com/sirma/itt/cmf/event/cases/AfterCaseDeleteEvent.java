package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after case instance deletion process. Any calls to the case service using the given
 * instance for loading/saving will fail.
 *
 * @author BBonev
 */
@Documentation("Event fired after case instance deletion process. Any calls to the case service using the given instance for loading/saving will fail.")
public class AfterCaseDeleteEvent extends AfterInstanceDeleteEvent<CaseInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after case delete event.
	 *
	 * @param instance
	 *            the instance
	 */
	public AfterCaseDeleteEvent(CaseInstance instance) {
		super(instance);
	}
}
