package com.sirma.itt.sch.web.entity.event;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;

/**
 * The Class ProjectScheduleOpenEvent.
 * 
 * @author svelikov
 */
public class ProjectScheduleOpenEvent extends AbstractInstanceEvent<ScheduleInstance> {

	/**
	 * Instantiates a new project schedule open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public ProjectScheduleOpenEvent(ScheduleInstance instance) {
		super(instance);
	}

}
