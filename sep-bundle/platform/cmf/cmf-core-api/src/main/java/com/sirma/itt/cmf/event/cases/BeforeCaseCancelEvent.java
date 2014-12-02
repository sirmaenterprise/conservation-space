package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.instance.BeforeInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before case instance cancellation process to begin. This is the final event for any
 * changes to be saved before the instance is marked as cancelled.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before case instance cancellation process to begin. This is the final event for any changes to be saved before the instance is marked as cancelled.")
public class BeforeCaseCancelEvent extends
		BeforeInstanceCancelEvent<CaseInstance, AfterCaseCancelEvent> {

	/**
	 * Instantiates a new before case cancel event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeCaseCancelEvent(CaseInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterCaseCancelEvent createNextEvent() {
		return new AfterCaseCancelEvent(getInstance());
	}
}
