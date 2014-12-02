package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before case instance deletion process to begin. This is the final event for any
 * changes to be saved before the instance is marked as deleted.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before case instance deletion process to begin. This is the final event for any changes to be saved before the instance is marked as deleted.")
public class BeforeCaseDeleteEvent extends
		BeforeInstanceDeleteEvent<CaseInstance, AfterCaseDeleteEvent> {

	/**
	 * Instantiates a new before case delete event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeCaseDeleteEvent(CaseInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterCaseDeleteEvent createNextEvent() {
		return new AfterCaseDeleteEvent(getInstance());
	}
}
