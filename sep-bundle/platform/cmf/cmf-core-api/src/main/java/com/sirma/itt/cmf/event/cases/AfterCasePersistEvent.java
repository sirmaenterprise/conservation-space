package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after {@link CaseInstance} is persisted for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired after {@link CaseInstance} is persisted for the first time.")
public class AfterCasePersistEvent extends AfterInstancePersistEvent<CaseInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new before case persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterCasePersistEvent(CaseInstance instance) {
		super(instance);
	}

}
