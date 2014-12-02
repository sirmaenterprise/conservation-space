package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired after deletion of a {@link ProjectInstance}
 * 
 * @author BBonev
 */
@Documentation("Event fired after deletion of a {@link ProjectInstance}")
public class AfterProjectDeleteEvent extends
		AfterInstanceDeleteEvent<ProjectInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new before project persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterProjectDeleteEvent(ProjectInstance instance) {
		super(instance);
	}

}
