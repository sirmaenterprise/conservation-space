package com.sirma.itt.pm.schedule.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;

/**
 * Event fired when a {@link ScheduleInstance} is created for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired when a {@link ScheduleInstance} is created for the first time.")
public class ScheduleCreatedEvent extends AbstractInstanceEvent<ScheduleInstance> {

	/** The original instance. */
	private final Instance originalInstance;

	/**
	 * Instantiates a new schedule created event.
	 * 
	 * @param instance
	 *            the instance
	 * @param originalInstance
	 *            the original instance
	 */
	public ScheduleCreatedEvent(ScheduleInstance instance, Instance originalInstance) {
		super(instance);
		this.originalInstance = originalInstance;
	}

	/**
	 * Getter method for originalInstance.
	 * 
	 * @return the originalInstance
	 */
	public Instance getOriginalInstance() {
		return originalInstance;
	}

}
