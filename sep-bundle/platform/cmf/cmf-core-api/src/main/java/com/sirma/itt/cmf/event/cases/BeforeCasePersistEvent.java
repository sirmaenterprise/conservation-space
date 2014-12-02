package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link CaseInstance} is persisted for the first time.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired when {@link CaseInstance} is persisted for the first time.")
public class BeforeCasePersistEvent extends
		BeforeInstancePersistEvent<CaseInstance, AfterCasePersistEvent> {

	/**
	 * Instantiates a new before case persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeCasePersistEvent(CaseInstance instance) {
		super(instance);
	}

	@Override
	protected AfterCasePersistEvent createNextEvent() {
		return new AfterCasePersistEvent(getInstance());
	}

}
