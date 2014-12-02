package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after case instance cancellation process.
 * 
 * @author BBonev
 */
@Documentation("Event fired after case instance cancellation process.")
public class AfterCaseCancelEvent extends AfterInstanceCancelEvent<CaseInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after case cancel event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterCaseCancelEvent(CaseInstance instance) {
		super(instance);
	}
}
