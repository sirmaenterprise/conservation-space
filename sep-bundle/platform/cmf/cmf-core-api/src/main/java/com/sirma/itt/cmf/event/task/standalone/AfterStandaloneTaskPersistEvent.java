package com.sirma.itt.cmf.event.task.standalone;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after {@link StandaloneTaskInstance} is created/persisted for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired after {@link StandaloneTaskInstance} is beening created/perssited for the first time.")
public class AfterStandaloneTaskPersistEvent extends
		AfterInstancePersistEvent<StandaloneTaskInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new before standalone task persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterStandaloneTaskPersistEvent(StandaloneTaskInstance instance) {
		super(instance);
	}

}
