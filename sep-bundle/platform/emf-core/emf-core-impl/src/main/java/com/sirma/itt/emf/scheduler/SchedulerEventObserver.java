package com.sirma.itt.emf.scheduler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.serialization.kryo.KryoSerializationEngine;

/**
 * Default {@link EmfEvent} observer that if for the given event there is are registered action for
 * execution.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SchedulerEventObserver {

	/** The scheduler service. */
	@Inject
	private SchedulerService schedulerService;

	/**
	 * Called on every event in the system
	 *
	 * @param event
	 *            the event
	 */
	public void onEvent(@Observes EmfEvent event) {
		if (!(event instanceof OperationEvent)) {
			// if the event is not registered in the kryo engine then we can't handle it so no need
			// to continue
			return;
		}
		// for optimization we will check only registered events
		Integer eventClassId = KryoSerializationEngine.getClassRegistration(event.getClass());
		if (eventClassId == null) {
			// if the event is not registered in the kryo engine then we can't handle it so no need
			// to continue
			return;
		}

		// check for action on the given event
		schedulerService.onEvent(event);
	}

}
