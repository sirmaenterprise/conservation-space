package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired after first persist of a {@link ProjectInstance}
 * 
 * @author BBonev
 */
@Documentation("Event fired after first persist of a {@link ProjectInstance}")
public class AfterProjectPersistEvent extends
		AfterInstancePersistEvent<ProjectInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new before project persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterProjectPersistEvent(ProjectInstance instance) {
		super(instance);
	}

}
