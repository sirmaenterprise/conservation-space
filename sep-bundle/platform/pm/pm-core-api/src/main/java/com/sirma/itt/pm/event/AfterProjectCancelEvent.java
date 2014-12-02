package com.sirma.itt.pm.event;

import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceCancelEvent;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Event fired after cancellation of a {@link ProjectInstance}
 * 
 * @author BBonev
 */
@Documentation("Event fired after cancellation of a {@link ProjectInstance}")
public class AfterProjectCancelEvent extends
		AfterInstanceCancelEvent<ProjectInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after project cancel event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterProjectCancelEvent(ProjectInstance instance) {
		super(instance);
	}

}
