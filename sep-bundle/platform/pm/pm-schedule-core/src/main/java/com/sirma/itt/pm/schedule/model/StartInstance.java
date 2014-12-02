package com.sirma.itt.pm.schedule.model;


/**
 * DAO object used to trigger instance starting.
 * 
 * @author BBonev
 */
public class StartInstance {

	/** The schedule entity. */
	private final ScheduleEntry scheduleEntity;

	/**
	 * Instantiates a new start instance.
	 *
	 * @param scheduleEntity the schedule entity
	 */
	public StartInstance(ScheduleEntry scheduleEntity) {
		this.scheduleEntity = scheduleEntity;
	}

	/**
	 * Gets the schedule entry.
	 * 
	 * @return the scheduleEntity
	 */
	public ScheduleEntry getScheduleEntry() {
		return scheduleEntity;
	}
}
